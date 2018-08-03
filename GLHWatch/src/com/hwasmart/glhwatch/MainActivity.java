package com.hwasmart.glhwatch;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;

import com.hwasmart.glhwatch.service.LocationUploadService;
import com.hwasmart.glhwatch.service.UDPHelper;

public class MainActivity extends Activity {

	private Button monitorBtn;
	private Button getidBtn;

	private LocationUploadService locationUploadService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// WifiManager wifiManager = (WifiManager)
		// getSystemService(Context.WIFI_SERVICE);
		// WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		// String macstr = wifiInfo.getMacAddress().replaceAll(":", "").trim();
		// long macnumber = Long.parseLong(macstr, 16);
		// String macdecstr = "" + macnumber;
		// int len = macdecstr.length();
		// if (len < 6) {
		// LocationUploadService.DeviceID = String.format("%06d", macnumber);
		// } else {
		// LocationUploadService.DeviceID = macdecstr.substring(len - 6);
		// }

		long macnumber = System.currentTimeMillis();
		String macdecstr = "" + macnumber;
		SharedPreferences share = getSharedPreferences("id_info",
				Context.MODE_PRIVATE);
		String id = share.getString("id", null);
		if (id == null || "".equals(id)) {
			LocationUploadService.DeviceID = macdecstr.substring(
					macdecstr.length() - 6, macdecstr.length());
			share.edit().putString("id", LocationUploadService.DeviceID)
					.commit();
		} else {
			LocationUploadService.DeviceID = id;
		}

		monitorBtn = (Button) findViewById(R.id.monitor_btn);
		getidBtn = (Button) findViewById(R.id.getid_btn);

		monitorBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (UDPHelper.getInstance().running) {
					monitorBtn.setBackgroundColor(0xFFED7D31);
					monitorBtn.setText("上报关闭\n（点击开启）");
					Intent intent0 = new Intent(MainActivity.this,
							LocationUploadService.class);
					intent0.putExtra("operation", "change_state");
					intent0.putExtra("status", "off");
					startService(intent0);
				} else {
					monitorBtn.setBackgroundColor(0xFF70AD47);
					monitorBtn.setText("上报开启\n（点击关闭）");
					Intent intent1 = new Intent(MainActivity.this,
							LocationUploadService.class);
					intent1.putExtra("operation", "change_state");
					intent1.putExtra("status", "on");
					startService(intent1);
				}
			}
		});

		getidBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, IDActivity.class);
				startActivity(intent);
			}
		});

		Intent bindIntent = new Intent(this, LocationUploadService.class);
		bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onResume() {
		if (!UDPHelper.getInstance().running) {
			monitorBtn.setBackgroundColor(0xFFED7D31);
			monitorBtn.setText("上报关闭\n（点击开启）");
		} else {
			monitorBtn.setBackgroundColor(0xFF70AD47);
			monitorBtn.setText("上报开启\n（点击关闭）");
		}
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		Intent intent1 = new Intent(this, WelcomeActivity.class);
		intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent1);
		super.onBackPressed();
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// Called when the connection is made.
			locationUploadService = ((LocationUploadService.MyBinder) service)
					.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			// Received when the service unexpectedly disconnects.
			locationUploadService = null;
		}
	};

	protected void onDestroy() {

		if (locationUploadService != null) {
			locationUploadService.setRealtimeLocationHandler(null);
			unbindService(mConnection);
		}
		super.onDestroy();
	};
}
