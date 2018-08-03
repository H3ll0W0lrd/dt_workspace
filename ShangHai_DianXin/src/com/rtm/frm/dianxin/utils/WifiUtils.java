package com.rtm.frm.dianxin.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

/**
 * 
 * 项目名称：RtmapAirport1.0 类名称：WifiUtils 类描述：wifi工具类 创建人：fushenghua 创建时间：2015-5-18
 * 下午4:35:57 联系方式：fushenghua2012@126.com 修改人：fushenghua 修改时间：2015-5-18 下午4:35:57
 * 修改备注：
 * 
 * @version
 */
public class WifiUtils {

	/***
	 * 判断wifi是否打开
	 * @param context
	 * @return true已经打开
	 */
	public static boolean isWifiActive(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(
				Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] infos = connectivity.getAllNetworkInfo();
			if (infos != null) {
				for (NetworkInfo ni : infos) {
					if (ni.getTypeName().equals("WIFI") && ni.isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * WIFI网络开关
	 * 
	 * @param enabled
	 *            true：开 false：关
	 */
	public static void toggleWiFi(Context context,boolean enabled) {
		WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if(wm == null)
			return;
		wm.setWifiEnabled(enabled);
	}

	/***
	 * 判断是否有网络连接
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
}
