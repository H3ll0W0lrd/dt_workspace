package com.hwasmart.glhwatch.service;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.hwasmart.glhwatch.AlertActivity;
import com.hwasmart.glhwatch.HelpNotificationActivity;
import com.hwasmart.glhwatch.NoWifiActivity;
import com.hwasmart.glhwatch.NotificationActivity;
import com.hwasmart.utils.ByteUtil;
import com.hwasmart.utils.Utils;
import com.hwasmart.utils.WifiConnect;
import com.rtm.location.LocationApp;
import com.rtm.location.entity.ResultEntity;
import com.rtm.location.entity.ResultEntityParcelable;
import com.rtm.location.util.LogUtil;

public class LocationUploadService extends Service {

	private static String TAG = "LocationUploadService";

	private static int CASEINTERNAL = 120;

	public static String DeviceID;

	public static int status = 0; // 0-未工作；1-同步状态；2-位置上报状态；
	public static boolean wifiscan = false;
	public static int wifidisappearindex = 0;

	public static int groupid = 0; // 手表所在家庭组编号
	public static int ischild = 1; // 手表代表儿童（1）还是家长（0），默认儿童
	public static String outOfWifiStr = "您已经离开儿童防走失服务范围，或位置无法获取！";

	private ResultEntity locationInfo;

	private Handler realtimeLocationHandler;
	private UDPHelper udpHelper;
	public static ExecutorService EXECUTOR = Executors.newCachedThreadPool();

	// private BroadcastReceiver batteryChangedReceiver;

	// public static long case1time = -1;
	// public static long case2time = -1;
	// public static long case3time = -1;
	//
	// public static int case1Internal = 60; // 秒
	// public static int case2Internal = 60;
	// public static int case3Internal = 60;

	private static long wifiConnectTime = -1;

	public static HashMap<String, Long> caseTimes = new HashMap<>();

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");

		udpHelper = UDPHelper.getInstance();

		// 定位服务初始化
		LogUtil.LOG_LEVEL = LogUtil.LOG_LEVEL_ERROR;
		super.onCreate();
	}

	private long goOutDoorTime;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand running...");
		if (intent != null) {
			String operation = intent.getExtras().getString("operation");

			// 开启关闭定位状态
			if ("change_state".equals(operation)) {
				String operationStatus = intent.getExtras().getString("status");
				if (operationStatus.equals("on")) {
					udpHelper.start(handler);

					EXECUTOR.execute(new Runnable() {

						@Override
						public void run() {

							while (status==0) {

								// 发送同步信号
								byte[] data = new byte[8];
								data[0] = (byte) 0xA1;
								data[1] = (byte) 0xEE;
								System.arraycopy(DeviceID.getBytes(), 0, data,
										2, 6);
								udpHelper.send(data, 8);
								try {
									Thread.sleep(10000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

							}
						}
					});
					// uploadTimer();
				} else {
					status = 0;
					udpHelper.stop();
				}
			}

			// 发送求助流程的通知其他家长
			if ("call_for_help".equals(operation)) {
				// 向其他家长发出求助信号
				byte[] data = new byte[12];
				data[0] = (byte) 0x3D;
				data[1] = (byte) 0x3E;
				System.arraycopy(DeviceID.getBytes(), 0, data, 2, 6);
				System.arraycopy(ByteUtil.getBytes(groupid), 0, data, 8, 4);
				udpHelper.send(data, 12);
			}
		}

		return super.onStartCommand(intent, flags, startId);
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			long currentTime = new Date().getTime();
			switch (msg.what) {
			case 0: // 状态下发接口
				Long caseTime = caseTimes.get(msg.obj);
				// Toast.makeText(getApplicationContext(), "状态:" + msg.arg1,
				// 1000).show();
				Log.e("rtmap",
						"状态：" + msg.arg1 + "   " + System.currentTimeMillis());
				switch (msg.arg1) {
				case 0: // 正常
					break;
				case 1: // 距离超出
				case 2: // 电量过低
				case 3: // 位置丢失
					if (caseTime == null
							|| currentTime - caseTime > CASEINTERNAL * 1000) {
						Intent intent1 = new Intent(LocationUploadService.this,
								AlertActivity.class);
						intent1.putExtra("type", msg.arg1);
						intent1.putExtra("msg_text", (String) msg.obj);
						intent1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
								| Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent1);
						// caseTimes.put((String) msg.obj, currentTime);
					}
					break;
				case 6: // 出门预警
					goOutDoorTime = System.currentTimeMillis();
					if (caseTime == null
							|| currentTime - caseTime > CASEINTERNAL * 1000) {
						Intent intent6 = new Intent(LocationUploadService.this,
								NotificationActivity.class);
						intent6.putExtra("type", msg.arg1);
						intent6.putExtra("msg_text", (String) msg.obj);
						intent6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent6);
						caseTimes.put((String) msg.obj, currentTime);
					}
					break;
				case 7: // 目标位置丢失
					// if (System.currentTimeMillis() - goOutDoorTime > 60 *
					// 1000
					// || goOutDoorTime == 0) {
					goOutDoorTime = System.currentTimeMillis();
					if (caseTime == null
							|| currentTime - caseTime > CASEINTERNAL * 1000) {
						Intent intent6 = new Intent(LocationUploadService.this,
								NotificationActivity.class);
						intent6.putExtra("type", msg.arg1);
						intent6.putExtra("msg_text", (String) msg.obj);
						intent6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent6);
						caseTimes.put((String) msg.obj, currentTime);
					}
					// }else{
					// goOutDoorTime = System.currentTimeMillis();
					// }
					break;
				}
				break;
			case 1: // 时间同步完成接口
				Log.d(TAG, "service received handler");
				groupid = msg.arg1;
				ischild = msg.arg2;

				if (msg.obj != null) {
					outOfWifiStr = (String) msg.obj;
				}

				// LocationApp.getInstance().stop();
				// LocationApp.getInstance().start();

				// 发送同步完成反馈消息
				byte[] data = new byte[8];
				data[0] = (byte) 0xA1;
				data[1] = (byte) 0xCC;
				System.arraycopy(DeviceID.getBytes(), 0, data, 2, 6);
//				udpHelper.send(data, 8);
				if (realtimeLocationHandler != null) {
					realtimeLocationHandler.sendEmptyMessage(0);
				}
				break;
			case 2: // 收到服务器求助请求确认消息
				Utils.showShotToast(LocationUploadService.this, "求助消息已经发送！");
				break;
			case 3: // 收到服务器发来的求助请求
				Intent intent = new Intent(LocationUploadService.this,
						HelpNotificationActivity.class);
				intent.putExtra("msg_text", (String) msg.obj);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
						| Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				break;
			default:
				break;
			}
		}
	};

	private final IBinder binder = new MyBinder();

	@Override
	public IBinder onBind(Intent intent) {

		return binder;
	}

	public class MyBinder extends Binder {
		public LocationUploadService getService() {
			return LocationUploadService.this;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	};

	public void setRealtimeLocationHandler(Handler handler) {
		realtimeLocationHandler = handler;
	}


}
