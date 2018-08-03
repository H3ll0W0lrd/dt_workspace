package com.rtm.frm.dianxin.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DTClockReceiver extends BroadcastReceiver {

	public static final String ALARM_ALERT_ACTION = "me.dtclock.ALARM_ALERT";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("rtmap", "receiver start");
		Intent i = new Intent(context, PollingService.class);
		context.startService(i);
	}
}
