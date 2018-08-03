package com.baidu.push.util;

import com.baidu.push.example.DTService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DTStartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent localIntent = new Intent();
		localIntent.setClass(context, DTService.class); //销毁时重新启动Service
		context.startService(localIntent);
	}
}
