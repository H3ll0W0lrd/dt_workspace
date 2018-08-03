package com.rtmap.locationcheck.core.http;

public class LCHttpUrl {

	private final static String WEB_MAIN_URL = "http://api.rtmap.com:8081/rtmap/";
//	public final static String WEB_MAIN_URL = "http://101.200.235.150:8081/rtmap/";
	// private final static String WEB_MAIN_URL =
	// "http://123.56.132.58:8081/rtmap/";

	// type=（1：楼层图片(默认) 2：imap文件）
	public final static String MAP_DOWNLOAD_URL = WEB_MAIN_URL
			+ "file/downFloorPicAndImap.json?key=%s&floor=%s&buildId=%s&type=%s";// 下载地图

	public final static String DOWNLOAD_BEACON = WEB_MAIN_URL
			+ "beacon/downloadBeacon.json?key=%s&buildId=%s&floor=%d";// 获取beacon信息数据
	public final static String UPLOAD_BEACON = WEB_MAIN_URL
			+ "beacon/uploadAppBeacon.json?key=%s&buildId=%s&floor=%d";// 上传beacon数据
	public final static String LOGIN = WEB_MAIN_URL + "open2cur/login";// 登录
	public final static String UPLOAD_DATA = WEB_MAIN_URL
			+ "beacon/uploadPrecisionFiles.json?key=%s&buildId=%s";// 上传数据接口
	public final static String BEACON_MAPPING = WEB_MAIN_URL
			+ "beacon/getWandaBeaconMapping.json";// 商场映射接口
	public final static String BEACON_MAC = WEB_MAIN_URL
			+ "beacon/getRelationBeacon.json";// beacon的映射mac列表
	public final static String FLOOR_INFO = WEB_MAIN_URL
			+ "floors/getList?key=%s&floor=%s&buildId=%s";// 获取楼层详情

}
