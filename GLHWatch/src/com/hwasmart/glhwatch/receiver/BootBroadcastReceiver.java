package com.hwasmart.glhwatch.receiver;

import com.hwasmart.glhwatch.AlertActivity;
import com.hwasmart.glhwatch.WelcomeActivity;
import com.hwasmart.glhwatch.service.LocationUploadService;
import com.hwasmart.utils.WifiConnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent intent1 = new Intent(context, WelcomeActivity.class);
		intent1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent1);
		
		// 启动后自动连接Wifi
//		WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
//		WifiConnect wifiConnect = new WifiConnect(wifiManager);
//		wifiConnect.connect();
	}
	
}
