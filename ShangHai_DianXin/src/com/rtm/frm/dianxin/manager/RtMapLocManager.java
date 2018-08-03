package com.rtm.frm.dianxin.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.frm.dianxin.utils.FileHelper;
import com.rtm.frm.dianxin.utils.RMlbsUtils;
import com.rtm.location.utils.RMLocationListener;

import java.util.Stack;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.params.HttpClientParams;

/**
 * Created by liyan on 15/10/26.
 */
public class RtMapLocManager implements RMLocationListener {

	public boolean isOnKeyHomeTouch = true;

	public interface RtMapLocManagerListener {
		public void onRtMapLocListenerReceiver(RMLocation rmLocation,
				boolean isFollowing);
	}

	private static RtMapLocManager instance = null;

	private Stack<RtMapLocManagerListener> mListenerStack = new Stack<RtMapLocManagerListener>();

	private boolean mIsFollowing = false;

	private BroadcastReceiver mHomeKeyEventReceiver = null;

	private BDLocation bdLocation;

	public static RtMapLocManager instance() {
		if (instance == null) {
			instance = new RtMapLocManager();
			RMlbsUtils.getInstance()
					.initLocate(AppContext.instance(), instance);
		}
		return instance;
	}

	public void startLoc() {
		if (isOnKeyHomeTouch) {
			isOnKeyHomeTouch = false;
			LogUtils.allowD = true;
			LogUtils.allowE = true;
			LogUtils.allowW = true;
			LogUtils.allowI = true;
			RMlbsUtils.getInstance().startLocate();
			startUploadTimer();
		}
	}

	public void stopLoc() {
		RMlbsUtils.getInstance().stopLocate();
		stopUploadTimer();
	}

	public void addReceiver(RtMapLocManagerListener listener) {
		registerHomeKeyReceiver();
		mListenerStack.add(listener);
	}

	public void removeReceiver(RtMapLocManagerListener listener) {
		mListenerStack.remove(listener);
	}

	public void destroy() {
		mListenerStack.removeAllElements();
		RMlbsUtils.getInstance().destroyLocate(this);
		unRegisterHomeKeyReceiver();
		// stopLoc();
		instance = null;
		currentRmLocation = null;
		gpsLocationManager = null;
		bdLocation = null;
	}

	public void setFollowMode(boolean isFollow) {
		mIsFollowing = isFollow;
	}

	public boolean isFollowing() {
		return mIsFollowing;
	}

	private RMLocation currentRmLocation = null;

	@Override
	public void onReceiveLocation(final RMLocation rmLocation) {
		currentRmLocation = rmLocation;
		Log.e("loc", "id:" + rmLocation.getBuildID() + " flag:"
				+ rmLocation.inOutDoorFlg + " err:" + rmLocation.error);
		RMAsyncTask.EXECUTOR.execute(new Runnable() {

			@Override
			public void run() {
				upLoadLocation(rmLocation);
			}
		});
		// Log.e("so", "so version:" + RMVersionLocation.SO_VERSION +
		// "  jar version:" + RMVersionLocation.VERSION);
		synchronized (mListenerStack) {
			if (mListenerStack.size() > 0) {
				int listeners = mListenerStack.size();

				RtMapLocManagerListener listener = mListenerStack
						.get(listeners - 1);
				if (listener != null) {
					listener.onRtMapLocListenerReceiver(rmLocation,
							mIsFollowing);
				}
			}
		}
	}

	/***
	 * 注册hone键监听
	 */
	private void registerHomeKeyReceiver() {
		if (mHomeKeyEventReceiver == null) {
			mHomeKeyEventReceiver = new BroadcastReceiver() {
				String SYSTEM_REASON = "reason";
				String SYSTEM_HOME_KEY = "homekey";
				String SYSTEM_HOME_KEY_LONG = "recentapps";

				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
						String reason = intent.getStringExtra(SYSTEM_REASON);
						if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
							// 表示按了home键,程序到了后台
							// isOnKeyHomeTouch = true;
							// stopLoc();
						} else if (TextUtils.equals(reason,
								SYSTEM_HOME_KEY_LONG)) {
							// 表示长按home键,显示最近使用的程序列表
						}
					}
				}
			};
			AppContext.instance().registerReceiver(mHomeKeyEventReceiver,
					new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		}
	}

	/***
	 * 取消home键监听
	 */
	private void unRegisterHomeKeyReceiver() {
		if (mHomeKeyEventReceiver != null) {
			AppContext.instance().unregisterReceiver(mHomeKeyEventReceiver);
			mHomeKeyEventReceiver = null;
		}
	}

	private int uploadTimerDelay = 3000;
	private Handler handler = new Handler();
	private boolean isStop = false;
	private Runnable upLoadRun = new Runnable() {
		@Override
		public void run() {
			if (isStop) {
				return;
			}
			if (currentRmLocation != null) {
				// upLoadLocation(currentRmLocation);
			}
			handler.postDelayed(this, uploadTimerDelay);
		}
	};

	private void startUploadTimer() {
		isStop = false;
		handler.postDelayed(upLoadRun, uploadTimerDelay);
	}

	private void stopUploadTimer() {
		isStop = true;
		handler.removeCallbacks(upLoadRun);
	}

	private double last_indoor_x = 0;
	private double last_indoor_y = 0;
	private double last_outdoor_x = 0;
	private double last_outdoor_y = 0;
	private LocationManager gpsLocationManager;

	/***
	 * 上报当前位置
	 *
	 * @param rmLocation
	 */
	private void upLoadLocation(final RMLocation rmLocation) {
		String url = "http://203.156.198.204:8082/IOPP/Comprehensive/saveData";
		HttpUtils httpUtils = new HttpUtils(5000);
		RequestParams params = null;

		BDLocation bdLocation = null;
		// BDLocation bdLocation = getBdLocation();
		double longitude = 0;
		double latitude = 0;
		if (bdLocation != null) {
			longitude = bdLocation.getLongitude();
			latitude = bdLocation.getLatitude();
		} else {
			longitude = rmLocation.getLongitude();
			latitude = rmLocation.getLatitude();
		}
		if (rmLocation.getInOutDoorFlg() == RMLocation.LOC_INDOOR
				&& rmLocation.error == 0) {// 室内

			double distance = Math.sqrt((last_indoor_x - rmLocation.getX())
					* (last_indoor_x - rmLocation.getX())
					+ (last_indoor_y - rmLocation.getY())
					* (last_indoor_y - rmLocation.getY()));
			// if (distance > 2) {

			last_indoor_x = rmLocation.getX();
			last_indoor_y = rmLocation.getY();
			params = new RequestParams();
			params.addBodyParameter("mac", getWifiMac());

			params.addBodyParameter("buildid", rmLocation.getBuildID());
			params.addBodyParameter("floor", rmLocation.getFloor());
			params.addBodyParameter("x", String.valueOf(rmLocation.getX()));
			params.addBodyParameter("y", String.valueOf(rmLocation.getY()));
			params.addBodyParameter("long", String.valueOf(longitude));
			params.addBodyParameter("lat", String.valueOf(latitude));
			params.addBodyParameter("sign",
					String.valueOf(System.currentTimeMillis()));
			// }

		} else if (rmLocation.getInOutDoorFlg() == RMLocation.LOC_OUTDOOR) {// 室外
			double distance = Math.sqrt((last_outdoor_x - rmLocation
					.getLongitude())
					* (last_outdoor_x - rmLocation.getLongitude())
					+ (last_outdoor_y - rmLocation.getLatitude())
					* (last_outdoor_y - rmLocation.getLatitude()));
			// if (distance > 0.0000001) {
			params = new RequestParams();
			params.addBodyParameter("mac", getWifiMac());
			last_outdoor_x = rmLocation.getLongitude();
			last_outdoor_y = rmLocation.getLatitude();
			params.addBodyParameter("buildid", "");
			params.addBodyParameter("floor", "");
			params.addBodyParameter("x", String.valueOf(-100));
			params.addBodyParameter("y", String.valueOf(-100));
			params.addBodyParameter("long", String.valueOf(longitude));
			params.addBodyParameter("lat", String.valueOf(latitude));
			params.addBodyParameter("sign",
					String.valueOf(System.currentTimeMillis()));
			// }

		}

		LogUtils.e("上传前====" + " x:" + rmLocation.getCoordX() + " y:"
				+ rmLocation.getY() + " long:" + String.valueOf(longitude)
				+ " lat:" + String.valueOf(latitude) + "   楼层："
				+ rmLocation.getFloor() + " error:" + rmLocation.error
				+ "   室内外：" + rmLocation.getInOutDoorFlg());
		FileHelper.saveLogToFile("上传前====" + " x:" + rmLocation.getCoordX() + " y:"
				+ rmLocation.getY() + " long:" + String.valueOf(longitude)
				+ " lat:" + String.valueOf(latitude) + "   楼层："
				+ rmLocation.getFloor() + " error:" + rmLocation.error
				+ "   室内外：" + rmLocation.getInOutDoorFlg(), "hahha.txt");
		if (params != null) {
			httpUtils.send(
					com.lidroid.xutils.http.client.HttpRequest.HttpMethod.POST,
					url, params, new RequestCallBack<String>() {
						@Override
						public void onSuccess(ResponseInfo<String> responseInfo) {
							LogUtils.e("上传成功====" + " x:"
									+ rmLocation.getCoordX() + " y:"
									+ rmLocation.getY());
						}

						@Override
						public void onFailure(HttpException e, String s) {
							LogUtils.e("上传失败====" + e.getExceptionCode() + "  "
									+ e.getMessage());
						}
					});
		}

	}

	public BDLocation getBdLocation() {
		return bdLocation;
	}

	public void setBdLocation(BDLocation bdLocation) {
		this.bdLocation = bdLocation;
	}

	private String getWifiMac() {
		WifiManager wifi = (WifiManager) AppContext.instance()
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}
}
