package com.rtm.location.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

/**
 * 获得手机信息
 * 
 * @author hotstar
 * 
 */
public class PhoneManager {
	/**
	 * 得到手机串号
	 * 
	 * @return
	 */
	public static String getIMEI(Context context) {
		return ((TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
	}
	
	public static String getMac(Context context) {
		String ret = "";
		try {
			WifiManager wifi = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = wifi.getConnectionInfo();
			if (info != null) {
				String mac = info.getMacAddress();
				if (mac != null) {
					ret = mac.replace(":", "");
				}
			}
		} catch (Exception e) {
		}
		return ret;
	}
	
	/**
	 * 网络是否连接
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnected(Context context) {  
	     if (context != null) {  
	         ConnectivityManager mConnectivityManager = (ConnectivityManager) context  
	                 .getSystemService(Context.CONNECTIVITY_SERVICE);  
	         NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();  
	         if (mNetworkInfo != null) {  
	             return mNetworkInfo.isAvailable();
	         }  
	     }  
	     return false;  
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