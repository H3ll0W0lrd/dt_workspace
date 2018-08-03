package com.rtmap.locationcheck.core.http;

public class LCHttpUrl {

	private final static String WEB_MAIN_URL = "http://api.rtmap.com:8081/rtmap/";
	// private final static String WEB_MAIN_URL =
	// "http://123.56.132.58:8081/rtmap/";// 10.10.10.149
	// 8081

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
			+ "floors/getList?key=%s&floor=%s&id_build=%s";// 获取楼层详情

	public final static String BUILD_INFO = WEB_MAIN_URL
			+ "gatherTool/uploadbuilds.json?key=%s";// 上传建筑物信息修改文件
	public final static String FINGER_INFO = WEB_MAIN_URL
			+ "gatherTool/uploadfingers.json?key=%s";// 上传指纹文件
	public final static String PICK_INFO = WEB_MAIN_URL
			+ "gatherTool/uploadpoi.json?key=%s";

	public final static String INFO_SElECT = WEB_MAIN_URL
			+ "gatherTool/selUserDailyInfo?key=%s&date=%s";// 查询工作量信息
	public final static String INFO_UPLOAD = WEB_MAIN_URL
			+ "gatherTool/insertUserDaily?key=%s&content=%s";// 汇报工作

	// public static final String URL_UPLOAD_ZIP =
	// "http://api.rtmap.com/?action=upload&option=wifi&key=%s";

}
