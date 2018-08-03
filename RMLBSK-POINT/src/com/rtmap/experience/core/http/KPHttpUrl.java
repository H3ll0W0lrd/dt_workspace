package com.rtmap.experience.core.http;

public class KPHttpUrl {

//	 private final static String WEB_URL = "http://api.rtmap.com:8081/rtmap/";
	private final static String WEB_URL = "http://123.56.132.58:8082/";

	public final static String LOGIN = WEB_URL + "exp/user/smsLogin";// 登录
	public final static String REGISTER = WEB_URL + "public/user/register";// 注册
	public final static String GET_BUILD_LIST = WEB_URL
			+ "experience/getBuildByUser";// 获取地图列表
	public final static String ADD_BUILD = WEB_URL + "experience/addBuild";// 添加建筑物
	public final static String DOWN_IMAGE = WEB_URL
			+ "experience/downloadPicture?key=%s&buildId=%s";// 下载图片
	public final static String UPLOAD_IMAGE = WEB_URL
			+ "experience/uploadPictureByUser";// 上传图片
	public final static String UP_BEACON = WEB_URL
			+ "experience/uploadBeaconByUser";// 上传beacon信息
	public final static String DELETE_FILE = WEB_URL + "experience/deleteBuild";// 删除建筑物
	public final static String REFRESH_BEACON = WEB_URL
			+ "public/user/exp/refresh";
	
	public final static String SMS = WEB_URL+"exp/user/smsSend";//发送短信
	public final static String GET_BUILD_ID = WEB_URL + "exp/experience/addBuild";//获取建筑物ID
}
