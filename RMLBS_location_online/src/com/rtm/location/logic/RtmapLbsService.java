/*
 * RtmapLbsService.java
 * classes : com.rtm.location.logic.RtmapLbsService
 * @author zny
 * V 1.0.0
 * Create at 2015年2月2日 下午5:28:22
 */
package com.rtm.location.logic;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.rtm.common.model.BuildInfo;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMConfig;
import com.rtm.common.utils.RMLog;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.location.JNILocation;
import com.rtm.location.LocationApp;
import com.rtm.location.entity.BeaconEntity;
import com.rtm.location.entity.BuildAngleList;
import com.rtm.location.entity.GpsEntity;
import com.rtm.location.entity.RMUser;
import com.rtm.location.entity.ReceiveLocationMode;
import com.rtm.location.entity.WifiEntity;
import com.rtm.location.sensor.BeaconSensor;
import com.rtm.location.sensor.GpsSensor;
import com.rtm.location.sensor.WifiSensor;
import com.rtm.location.utils.PhoneManager;
import com.rtm.location.utils.RMBuildAngleUtil;
import com.rtm.location.utils.RMSqlite;

public class RtmapLbsService extends Service {

	private static final String TAG = "RtmapLbsService";
	private static final int ONLINE_POSITION_RESULT = 0;
	private static final int LOCATION_MAIN = 33;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == LOCATION_MAIN) {
				RMAsyncTask.EXECUTOR.execute(LocationMain);
			}
		};
	};
	private long counter = 0;
	private boolean isWifiScan = true; // 启动时未判断出建筑物，默认扫描WiFi
	private boolean isBeaconScan = true; // 启动时未判断出建筑，默认扫描Beacon
	public static boolean isActiveReceiverRun = false;

	public static boolean isPdrOpen = true;

	private String lastBuildID = "";
	private int requestLock = 0;

	private StateReceiver stateReceiver;

	private ConnectivityManager cm = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private boolean isNetworkConnected() {
		if (cm == null) {
			return false;
		}
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		RMLog.i(TAG, "Service onCreate");
		stateReceiver = new StateReceiver();
		// setStartTime();
		try {
			cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		} catch (Exception e) {
			RMLog.d(TAG, "Active Socket Init Fail", e);
		}
	}

	// 关闭标记0，关闭，1延迟30s
	private int mStop;
	private int stopCount;// 停止之后定位执行次数
	private RMUser user;
	private boolean isStartLocate;// 是否开启定位

	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		RMLog.i(TAG, "Service onStartCommand");
		user = RMSqlite.getInstance().getUser();
		if (intent != null) {
			mStop = intent.getIntExtra("stop", 0);
			if (mStop == 1) {
				stopCount = 0;
			} else {// 认为手动开启定位
				if (!isStartLocate) {// 如果没有开启
					isStartLocate = true;
					// 蓝牙监听广播注册
					IntentFilter filter = new IntentFilter();
					filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
					registerReceiver(stateReceiver, filter);

					RMUser user = RMSqlite.getInstance().getUser();
					TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
					String str = "";
					String div;
					if (user != null && !RMStringUtils.isEmpty(user.getLbsid())) {
						str += "#ID$" + user.getLbsid();
					}
					String userid = LocationApp.getInstance().getUserid();
					if (!RMStringUtils.isEmpty(userid)) {
						str += "#oID$" + userid;
					}
					div = tm.getDeviceId();
					if (!RMStringUtils.isEmpty(div))
						str += "#imei$" + div;
					div = PhoneManager.getMac(getApplicationContext());
					if (!RMStringUtils.isEmpty(div))
						str += "#mac$" + div;
					div = tm.getLine1Number();
					if (!RMStringUtils.isEmpty(div))
						str += "#tel$" + div;
					div = Settings.Secure.getString(getContentResolver(),
							Settings.Secure.ANDROID_ID);
					if (!RMStringUtils.isEmpty(div))
						str += "#andID$" + div;
					div = android.os.Build.MODEL;
					if (!RMStringUtils.isEmpty(div))
						str += "#devTp$" + div;
					div = android.os.Build.BRAND;
					if (!RMStringUtils.isEmpty(div))
						str += "#devBd$" + div;
					// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO
					// && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO
							&& Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
						str += "#cpuTp$" + android.os.Build.CPU_ABI2;
					else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
						str += "#cpuTp$" + android.os.Build.SUPPORTED_ABIS[0];
					// else if (Build.VERSION.SDK_INT >=
					// Build.VERSION_CODES.LOLLIPOP)
					// str += "#cpuTp$" + android.os.Build.SUPPORTED_ABIS;
					str += "#os$android" + Build.VERSION.RELEASE;
					GatherData.getInstance().setUserInfo(str);

					// 同时需要清空ap和beacon的数据
					WifiEntity.getInstance().clearAp();
					BeaconEntity.getInstance().clearBeacon();
					SensorsLogic.getInstance().start();
					PackageManager pm = getPackageManager();
					boolean permission = (PackageManager.PERMISSION_GRANTED == pm
							.checkPermission(
									"android.permission.ACCESS_FINE_LOCATION",
									getPackageName()));
					if (permission) {
						GpsSensor.getInstance().start();
					}

					if (BeaconSensor.isSuportBeacon(getApplicationContext())) {
						BeaconSensor.getInstance().start();
					}

					isActiveReceiverRun = true;
					RMAsyncTask.EXECUTOR.execute(new ActiveReceiver());
					RMAsyncTask.EXECUTOR.execute(LocationMain);
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void stopLocation() {
		isActiveReceiverRun = false;
		// 蓝牙监听广播注销
		unregisterReceiver(stateReceiver);

		SensorsLogic.getInstance().stop();// 关闭传感器
		PackageManager pm = getPackageManager();
		boolean permission = (PackageManager.PERMISSION_GRANTED == pm
				.checkPermission("android.permission.ACCESS_FINE_LOCATION",
						getPackageName()));
		if (permission) {
			GpsSensor.getInstance().stop();
		}

		if (BeaconSensor.isSuportBeacon(getApplicationContext())) {
			BeaconSensor.getInstance().stop();
		}
		BeaconEntity.getInstance().clearBeacon();
		WifiEntity.getInstance().clearAp();
	}

	/**
	 * 接受定位SDK的广播信息
	 * 
	 * @author dingtao
	 *
	 */
	private class StateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction()
					.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				if (BeaconSensor.isSuportBeacon(context)) {
					BeaconSensor.getInstance().init(context);
					BeaconSensor.getInstance().stop();
					BeaconSensor.getInstance().start();
					RMLog.i("StateReceiver",
							"android.bluetooth.adapter.action.STATE_CHANGED : "
									+ BeaconSensor.getInstance()
											.isBlueToothOpen());
				}
			}
		}
	}

	private Runnable LocationMain = new Runnable() {

		@Override
		public void run() {
			counter++;

			/**
			 * 扫描定位
			 */
			RMAsyncTask.EXECUTOR.execute(new Runnable() {

				@Override
				public void run() {
					if (isWifiScan) {
						WifiSensor.getInstance().scan(true);
					}
				}
			});
			// clearHistoryWifiData();
			if (counter % 2 == 0) {
				activeRequest();
				// locate定位结果

				stopCount++;
				if (mStop == 0
						&& LocationApp.getInstance().getReceiveLocationMode() == ReceiveLocationMode.DATA_RECYCLE
						&& System.currentTimeMillis() - time > 500) {// 当非停止状态并且数据正常回调模式调用
					time = System.currentTimeMillis();
					sendLocation();
				}
			}
			boolean a = user != null && stopCount < user.getLog_record_time()
					&& mStop == 1;
			if (a || mStop == 0) {// 开启定位或者关闭定位后次数满足
				handler.sendEmptyMessageDelayed(LOCATION_MAIN, LocationApp
						.getInstance().getRequestSpanTime() / 2);
			} else {
				isStartLocate = false;
				stopLocation();
			}
		}
	};

	/**
	 * 发送结果
	 */
	private void sendLocation() {
		String onlineResultStr = JNILocation
				.getPosition(ONLINE_POSITION_RESULT);
		// Log.i("rtmap",
		// "离线："+offlineResultStr+"   \n在线："+onlineResultStr);
		final RMLocation onlineResult = new RMLocation();
		onlineResult.decode_jsn(onlineResultStr, GpsEntity.getInstance()
				.getLocation(), GpsEntity.getInstance().getBuild());
		final RMLocation result = new RMLocation(onlineResult);
		if (result.getError() == 0) {
			result.setErrorInfo("定位成功");
		} else {
			result.setErrorInfo(getErrorInfo(result.getError(),
					GatherData.BEACON_COUNT, GatherData.WIFI_COUNT,
					BeaconSensor.getInstance().init(getApplicationContext()),
					BeaconSensor.isSuportBeacon(getApplicationContext()),
					WifiSensor.getInstance().init(getApplicationContext()),
					PhoneManager.isNetworkConnected(getApplicationContext())));
		}
		RMAsyncTask.EXECUTOR.execute(new Runnable() {

			@Override
			public void run() {
				/**
				 * 指纹数据下载及更新
				 */
				if (!RMStringUtils.isEmpty(result.buildID)
						&& !"0".equals(result.buildID)) {
					if (!result.buildID.equals(lastBuildID)) {
						lastBuildID = result.buildID;
						loadMapAngle(lastBuildID);
					}
				}
			}
		});
		handler.post(new Runnable() {

			@Override
			public void run() {
				LocationApp.getInstance().onReceive(result);
			}
		});
	}

	private void loadMapAngle(String buildId) {
		BuildInfo info = RMSqlite.getInstance().getBuildInfo(buildId);
		if (info != null) {
			LocationApp.getInstance().setMapAngle(info.getMapAngle());
		} else {
			LocationApp.getInstance().setMapAngle(0);
		}
		RMBuildAngleUtil.requestBuildAngle(LocationApp.getInstance()
				.getApiKey(), new String[] { buildId },
				new RMBuildAngleUtil.OnGetBuildAngleListener() {

					@Override
					public void onGetBuildAngle(BuildAngleList result) {
						if (result.getError_code() == 0) {
							for (int i = 0; i < result.getList().size(); i++) {
								RMSqlite.getInstance().addInfo(
										result.getList().get(i));
								LocationApp.getInstance().setMapAngle(
										result.getList().get(i).getMapAngle());
							}
						}
					}
				});
	}

	/**
	 * 得到错误信息
	 * 
	 * @param error
	 * @param beaconCount
	 * @param wifiCount
	 * @param isBTopen
	 * @param isBTscan
	 * @param isWifiopen
	 * @param isMobilOpen
	 * @return
	 */
	private String getErrorInfo(int error, int beaconCount, int wifiCount,
			boolean isBTopen, boolean isBTscan, boolean isWifiopen,
			boolean isNetOpen) {
		String info = "";
		if (error == 0) {
			info = "定位成功";
		} else {
			info = "定位失败";
		}
		info += "\n错误码：" + error + "\nwifi网络是否开启：" + isWifiopen
				+ "\n扫描到wifi个数：" + wifiCount + "\n蓝牙是否开启：" + isBTopen
				+ "\n蓝牙硬件是否支持扫描：" + isBTscan + "\n扫描到beacon数量：" + beaconCount
				+ "\n网络是否连接：" + isNetOpen;
		return info;
	}

	private long time;

	private class ActiveReceiver implements Runnable {

		@Override
		public void run() {
			while (isActiveReceiverRun) {
				try {
					JNILocation.serverOutput();
					if (mStop == 0
							&& LocationApp.getInstance()
									.getReceiveLocationMode() == ReceiveLocationMode.SERVER_OUTPUT)// 当非停止状态并且数据正常回调模式调用
						sendLocation();
				} catch (Exception e) {
					RMLog.d(TAG, "Active Position Socket Receiver", e);
				}
			}
		}
	}

	private void activeRequest() {
		if (isWifiScan || isBeaconScan) {
			if (requestLock == 0) {
				requestLock = 1;
				RMAsyncTask.EXECUTOR.execute(new Runnable() {

					@Override
					public void run() {
						String onlineInputXml = GatherData.getInstance()
								.getLocateXml(mStop);
						String key = null, mac = null;
						if (onlineInputXml.contains("<key>")) {
							key = onlineInputXml.substring(
									onlineInputXml.indexOf("<key>") + 5,
									onlineInputXml.indexOf("</key>"));
						}
						if (onlineInputXml.contains("<u>")) {
							mac = onlineInputXml.substring(
									onlineInputXml.indexOf("<u>") + 3,
									onlineInputXml.indexOf("</u>"));
						}
						if (key == null || "".equals(key) || "null".equals(key)
								|| mac == null || "".equals(mac)
								|| "null".equals(mac)) {
							RtmapLbsService.this.stopSelf();
							return;
						}
						LocationApp.getInstance()
								.setScannerInfo(onlineInputXml);
						// RMLog.d(TAG, "Active Positioning Request XML : "
						// + onlineInputXml);
						if (!onlineInputXml.equals("")) {
							if (RMConfig.mac != null
									&& !"".equals(RMConfig.mac)
									&& !"0".equals(RMConfig.mac)) {// 需要判断是否有mac值，如果没有，不能进行在线定位
								if (isNetworkConnected()) {
									JNILocation.serverInput(onlineInputXml);
								}
							} else {// 重新获取mac
								RMConfig.mac = PhoneManager.getMac(LocationApp
										.getInstance().getContext());
							}
						}
						requestLock = 0;
					}
				});
			}
		}
	}

	@Override
	public void onDestroy() {
		RMLog.d(TAG, "Service onDestroy");
		isActiveReceiverRun = false;
		handler.removeCallbacks(LocationMain);
		handler.removeMessages(LOCATION_MAIN);
		isWifiScan = false;
		isBeaconScan = true;
		super.onDestroy();
	}

}
