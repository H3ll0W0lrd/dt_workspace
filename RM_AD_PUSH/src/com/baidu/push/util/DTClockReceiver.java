package com.baidu.push.util;

import java.util.ArrayList;
import java.util.Collection;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.push.example.MyPushMessageReceiver;
import com.baidu.push.model.ApInfo;
import com.baidu.push.model.BeaconInfo;
import com.dingtao.libs.DTAsyncTask;
import com.dingtao.libs.DTCallBack;
import com.dingtao.libs.exception.DTException;
import com.dingtao.libs.http.DTHttpClient;
import com.google.gson.Gson;

public class DTClockReceiver extends BroadcastReceiver {

	public static final String ALARM_ALERT_ACTION = "com.rtmap.sensor.start";
	public static final int CLOCK_NOTIFY_ID = 818;
	Context mContext;
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				BeaconSensor.getInstance().stop();
				new DTAsyncTask(new SendScanResultCall()).run();
			}
		}
	};

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("rtmap", "闹钟该响了");
		mContext = context;
		BeaconSensor.getInstance().init(context);
		BeaconSensor.getInstance().start();
		WifiSensor.getInstance().setContext(context);
		WifiSensor.getInstance().scan();
		mHandler.sendEmptyMessageDelayed(1, 2000);
	}

	class Model {
		private String mac;
		private String brand;
		private String imei;
		private String key;
		private String token;
		private String latitude;
		private String longitude;
		private String range;
		private Collection<ApInfo> apinfo;
		private Collection<BeaconInfo> beaconinfo;

		public String getMac() {
			return mac;
		}

		public void setMac(String mac) {
			this.mac = mac;
		}

		public String getBrand() {
			return brand;
		}

		public void setBrand(String brand) {
			this.brand = brand;
		}

		public String getImei() {
			return imei;
		}

		public void setImei(String imei) {
			this.imei = imei;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getLatitude() {
			return latitude;
		}

		public void setLatitude(String latitude) {
			this.latitude = latitude;
		}

		public String getLongitude() {
			return longitude;
		}

		public void setLongitude(String longitude) {
			this.longitude = longitude;
		}

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public String getRange() {
			return range;
		}

		public void setRange(String range) {
			this.range = range;
		}

		public Collection<ApInfo> getApinfo() {
			return apinfo;
		}

		public void setApinfo(Collection<ApInfo> apinfo) {
			this.apinfo = apinfo;
		}

		public Collection<BeaconInfo> getBeaconinfo() {
			return beaconinfo;
		}

		public void setBeaconinfo(Collection<BeaconInfo> beaconinfo) {
			this.beaconinfo = beaconinfo;
		}
	}

	/**
	 * 发送扫描信息
	 * 
	 * @author dingtao
	 *
	 */
	class SendScanResultCall implements DTCallBack {

		@Override
		public Object onCallBackStart(Object... obj) {
			try {
				final Gson gson = new Gson();
				final Model model = new Model();
				model.setMac(PhoneManager.getMac(mContext));
				model.setBrand(PhoneManager.getPhoneType());
				model.setImei(PhoneManager.getDeviceId(mContext));
				model.setKey("IR732EVNPJ");
				SharedPreferences preferences = mContext.getSharedPreferences(
						"rtmap.xml", 0);
				model.setToken(preferences.getString("token", null));
				model.setLatitude("" + 113.421234);
				model.setLongitude("" + 22.354231);
				ArrayList<BeaconInfo> beaconList = new ArrayList<BeaconInfo>();
				beaconList.addAll(BeaconSensor.getInstance().getBeaconData()
						.values());
				model.setBeaconinfo(beaconList);
				ArrayList<ApInfo> apList = new ArrayList<ApInfo>();
				apList.addAll(WifiSensor.getInstance().getAPData().values());
				model.setApinfo(apList);
				BeaconSensor.getInstance().getBeaconData().clear();
				WifiSensor.getInstance().getAPData().clear();
				return DTHttpClient
						.postinfo(
								DTHttpClient.POST,
								"http://42.96.128.76:9090/rtmap_lbs_api/v1/lbs_preferential",
								gson.toJson(model));
			} catch (DTException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			// mResultText.setText((String) obj);
		}
	}
}
