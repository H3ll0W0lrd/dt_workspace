/**
 * 封装地图定位Http请求
 */
package com.rtmap.ambassador.http;

public class DTHttpUrl {

	private static String WEB_URL = "http://weixin.bcia.com.cn/";// 正式版路径
	// public static String WEB_URL_TEST =
	// "http://airtest.rtmap.com/";//测试版路径

	public final static String LOGIN = WEB_URL
			+ "aup/api/provider/providerLogin";// 获取本设备唯一ID
	public final static String AREA = WEB_URL
			+ "aup/api/provider/getLocationArea";// 查询基础分区
	public final static String LOGOUT = WEB_URL
			+ "aup/api/provider/providerLogout";// 登出
	public final static String UPLOAD_LOC = WEB_URL
			+ "aup/api/provider/addProviderLocation";// 上传位置
	public final static String CHANGE_AREA = WEB_URL
			+ "aup/api/provider/providerChangeArea";//切换区域
}
