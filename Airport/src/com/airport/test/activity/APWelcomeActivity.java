package com.airport.test.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.airport.test.R;
import com.airport.test.core.APActivity;
import com.rtm.location.LocationApp;

public class APWelcomeActivity extends APActivity {
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			OutMapActivity.interActivity(APWelcomeActivity.this);
			finish();
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		mHandler.sendEmptyMessageDelayed(1, 3000);
		LocationApp.getInstance().init(getApplicationContext());
		LocationApp.getInstance().start();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		mHandler.removeMessages(1);
	}
}
