package com.hwasmart.glhwatch;

import com.hwasmart.glhwatch.service.LocationUploadService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class NoWifiActivity extends Activity {
	
	private BroadcastReceiver wifiReceiver;
	
	private Button okBtn;
	
	private TextView contentTxt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_no_wifi);
		
		okBtn = (Button)findViewById(R.id.ok_btn);
		
		okBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				NoWifiActivity.this.finish();
			}
		});
		
		
		contentTxt = (TextView)findViewById(R.id.msg_content);
		contentTxt.setText(LocationUploadService.outOfWifiStr);
		
		wifiReceiver = new WifiBroadcastReceiver();
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.net.wifi.STATE_CHANGE");   //为BroadcastReceiver指定action，使之用于接收同action的广播
		registerReceiver(wifiReceiver, intentFilter);
		
		Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(1000);
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(wifiReceiver);
		super.onDestroy();
	}
	
	private class WifiBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) { 
				NetworkInfo networkInfo_ = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO); 
	            if(networkInfo_.isConnected()) {
	            	NoWifiActivity.this.finish();
	            }
			}
		}
		
	}
}
