package com.rtmap.wifipicker.wifi;

import android.content.SharedPreferences;

import com.rtmap.wifipicker.core.WPApplication;
import com.rtmap.wifipicker.core.http.WPHttpUrl;

public class NetGatherData {
	public static SharedPreferences sharedPreferences = WPApplication
			.getInstance().getShare();

	/** 手机的IMEI号，或用户id */
	public static String sUserName = sharedPreferences
			.getString("tag1", "test");
	public static String sMap = "testMap";

	/** 服务器ip地址 */
	public static String sServiceIp = sharedPreferences.getString("ip",
			WPHttpUrl.WEB_URL);// 192.168.1.141 18003
	/** 服务器的端口号 */
	public static String sPORT = sharedPreferences.getString("port", "8081");
	/** 采集终端mac */
	public static String sMac1 = sharedPreferences.getString("tag1",
			"000000000000");

	/**
	 * 开启采集功能 gather:start_cap:1.0 R <req> <uid>userId</uid> <mp>mapId</mp>
	 * <time>timeMax</time> <tg>tag1#tag2#...</tg> </req>
	 **/
	public static String startGather(String mapName, String file) {
		return "gather:gather_data_offline:1.0 R<req><uid>" + sUserName
				+ "</uid><file>" + file + "</file><time>"
				+ sharedPreferences.getString("net_time", "60") + "</time><tg>"
				+ sMac1 + "</tg></req>";
	}
}