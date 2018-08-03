package com.rtmap.wifipicker.core.http;

public class WPHttpUrl {

	public final static String WEB_URL = "192.168.1.10";
	public final static String WEB_MAIN_URL = "http://api.rtmap.com:8081/rtmap/";
//	public final static String WEB_MAIN_URL = "http://101.200.235.150:8081/rtmap/";
	
	public static final String URL_UPLOAD_ZIP = "http://api.rtmap.com/?action=upload&option=wifi&key=%s";
//	public static final String URL_UPLOAD_ZIP = "http://101.200.235.150/?action=upload&option=wifi&key=%s";
	
	public final static String DOWNLOAD_BEACON = WEB_MAIN_URL
			+ "beacon/downloadBeacon.json?key=%s&buildId=%s&floor=%d";// 获取beacon信息数据
	// type=（1：楼层图片(默认) 2：imap文件）
	public final static String MAP_DOWNLOAD_URL = WEB_MAIN_URL
			+ "file/downFloorPicAndImap.json?key=%s&floor=%s&buildId=%s&type=%d";// 下载地图
	public final static String LOGIN = WEB_MAIN_URL + "open2cur/login";// 登录
	// 192.168.1.163：18000 网络采集地址
	
	public final static String FLOOR_INFO = WEB_MAIN_URL
			+ "floors/getList?key=%s&floor=%s&buildId=%s";// 获取楼层详情
}
