package com.baidu.push.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class PhoneManager {
	/**
	 * 得到手机串号
	 * 
	 * @return
	 */
	public static String getDeviceId(Context context) {
		return ((TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
	}
	
	/**
	 * 得到mac地址
	 * @param context
	 * @return
	 */
	public static String getMac(Context context) {
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}

	/**
	 * 得到手机型号
	 * 
	 * @return
	 */
	public static String getPhoneType() {
		return android.os.Build.MODEL;
	}

	/**
	 * 得到操作系统版本
	 * 
	 * @return
	 */
	public static String getOsVersion(Context context) {
		return ((TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE))
				.getDeviceSoftwareVersion();
	}
}
