package com.rtmap.wifipicker.page;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.rtmap.wifipicker.R;

/**
 * 欢迎界面
 * */
public class SplashActivity extends WPBaseActivity {
	private static final long TIME_DELAYED = 2000L;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			jump();
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_activity);
		mHandler.sendEmptyMessageDelayed(0, TIME_DELAYED);
	}
	
	private void jump() {
		Intent intent = new Intent();
		intent.setClass(this, WPLoginActivity.class);//未保存跳转到登录界面
		startActivity(intent);
		finish();
	}
}
