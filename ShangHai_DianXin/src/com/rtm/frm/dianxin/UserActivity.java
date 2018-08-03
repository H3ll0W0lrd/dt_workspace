package com.rtm.frm.dianxin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.TextView;

import com.rtm.location.utils.RMVersionLocation;

public class UserActivity extends Activity {

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);
		TextView mTextView = (TextView) findViewById(R.id.textView1);
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		String str = "";
		str += "apikey:R3ovVkOBQU";
		str += "\npakagename:" + getPackageName();
		str += "\nsdkversion:" + RMVersionLocation.VERSION;
		str += "\nmac:" + info.getMacAddress();
		str += "\nimei:" + tm.getDeviceId();//
		str += "\ntel:" + tm.getLine1Number();
		str += "\ndevice_type:" + android.os.Build.MODEL;
		str += "\ndevice_brand:" + android.os.Build.BRAND;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO
				&& Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
			str += "\ncpu_type:" + android.os.Build.CPU_ABI2;
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			str += "\ncpu_type:" + android.os.Build.SUPPORTED_ABIS[0];
		str += "\nos_version:" + Build.VERSION.RELEASE;
		str += "\nandroid_id:"
				+ Settings.Secure.getString(getContentResolver(),
						Settings.Secure.ANDROID_ID);
		mTextView.setText(str);
	}
}
