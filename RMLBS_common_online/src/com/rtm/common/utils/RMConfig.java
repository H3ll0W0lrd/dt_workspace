/**
 * 地图定位共同使用的工具
 */
package com.rtm.common.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

public class RMConfig {
	private static RMConfig instance;
	private String mServer;

	/** 手机串号 **/
	public static String imei = "";

	/** 手机mac **/
	public static String mac = "";

	/** 包名 */
	public static String pakageName = "1.1";

	/** 设备类型 */
	public static String deviceType = "1.1";

	private RMConfig() {
		mServer = "";
	}

	/** 返回空字符串说明对应的key值不存在，或者解释错误 */
	public static String getMetaData(Context c, String key) {
		String msg = null;
		try {
			ApplicationInfo info = c.getPackageManager().getApplicationInfo(
					c.getPackageName(), PackageManager.GET_META_DATA);
			msg = info.metaData.getString(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return msg;
	}

	public static RMConfig getInstance() {
		if (instance == null) {
			instance = new RMConfig();
		}
		return instance;
	}


	public String getServer() {
		return mServer;
	}

	public void setServer(String server) {
		mServer = server;
	}

//	public static byte[] getBeaconDecodeKey() {
//		String keyStr = "A5B5C146ADA7291E7FF5579539C04181B2E3F58C232641D741D03EED5932409D";
//		byte[] key = new byte[keyStr.length() / 2];
//		for (int i = 0; i < keyStr.length(); i += 2) {
//			String tmp = keyStr.substring(i, i + 2);
//			int j = i / 2;
//			key[j] = (byte) Integer.parseInt(tmp, 16);
//		}
//		return key;
//	}
}
