package com.baidu.push.example;

import java.util.ArrayList;
import java.util.Collection;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.push.model.ApInfo;
import com.baidu.push.model.BeaconInfo;
import com.baidu.push.util.BeaconSensor;
import com.baidu.push.util.PhoneManager;
import com.baidu.push.util.WifiSensor;
import com.dingtao.libs.DTAsyncTask;
import com.dingtao.libs.DTCallBack;
import com.dingtao.libs.exception.DTException;
import com.dingtao.libs.http.DTHttpClient;
import com.dingtao.libs.util.DTLog;
import com.google.gson.Gson;

public class DTService extends Service {
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				BeaconSensor.getInstance().stop();
				new DTAsyncTask(new SendScanResultCall()).run();
				sendEmptyMessageDelayed(0, 10000);
			} else {
				BeaconSensor.getInstance().init(getApplicationContext());
				BeaconSensor.getInstance().start();
				WifiSensor.getInstance().setContext(getApplicationContext());
				WifiSensor.getInstance().scan();
				sendEmptyMessageDelayed(1, 2000);
			}
		};
	};

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Intent intent = new Intent(getApplicationContext(), DTService.class);
		AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		PendingIntent mPendingIntent = PendingIntent.getService(this, 0,
				intent, Intent.FLAG_ACTIVITY_NEW_TASK);
		mAlarmManager.setInexactRepeating(AlarmManager.RTC,
				System.currentTimeMillis(), 10 * 1000, mPendingIntent);
		PushManager.startWork(getApplicationContext(),

                PushConstants.LOGIN_TYPE_API_KEY,
                Utils.getMetaValue(getApplicationContext(), "api_key"));
		mHandler.sendEmptyMessage(0);
		green.run();
	}

	Runnable green = new Runnable() {

		@Override
		public void run() {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mHandler.post(green);
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub

		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
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
				model.setMac(PhoneManager.getMac(getApplicationContext()));
				model.setBrand(PhoneManager.getPhoneType());
				model.setImei(PhoneManager.getDeviceId(getApplicationContext()));
				model.setKey("IR732EVNPJ");
				SharedPreferences preferences = getApplicationContext()
						.getSharedPreferences("rtmap.xml", 0);
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

	@Override
	public void onDestroy() {
		DTLog.e("杀死了service");
		Intent localIntent = new Intent();
		localIntent.setClass(this, DTService.class); // 销毁时重新启动Service
		this.startService(localIntent);
		super.onDestroy();
	}
}
