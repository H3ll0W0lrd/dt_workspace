/**
 * 封装地图定位Http请求
 */
package com.rtm.common.http;

public class RMHttpUrl {

	private static String WEB_URL = "http://lbsapi.rtmap.com/";// 正式版路径
	// public static String WEB_URL_TEST =
	// "http://lbsapitest2.rtmap.com:8080/";//测试版路径
	public static boolean IS_LIS_PASS = true;

	public static void setWEB_URL(String wEB_URL) {
		WEB_URL = wEB_URL;
	}

	public static String getWEB_URL() {
		return WEB_URL;
	}

	/**
	 * 定位用到的三个接口
	 */
	public static String FILE_INFO_URL = "http://lbsdata.rtmap.com:8091/open2project/fileinfor/getFileInfor";
	public static String DOWNLOAD_URL = "http://lbsdata.rtmap.com:8091/open2project/fileinfor/downloadFile";
	public static String UPLOAD_ADDR = "http://lbsdata.rtmap.com:8091/open2project/fileinfor/uploadFile";

	public static final String LOG_SWITCH = WEB_URL
			+ "rtmap_lbs_api/v1/rtmap/sdklog_capability";// log开关url
	public static final String UPLOAD_LOG_FILE = "http://lbssdk.rtmap.com/rtmap_lbs_api/v1/rtmap/sdklog_upload";// 上传文件

	/**
	 * 地图使用的接口
	 */
	public final static String LICENSE = "http://lbsapi.rtmap.com/rtmap_lbs_api/v1/lbslicense";// License验证
	public final static String CITY_LIST = "rtmap_lbs_api/v1/citylist";// 获取服务范围内的城市列表
	public final static String BUILD_LIST = "rtmap_lbs_api/v1/buildlist";// 获取服务范围内的建筑物列表
	public final static String BUILD_DETAIL = "rtmap_lbs_api/v1/build_detail";// 获取某一建筑物详情
	public final static String FLOOR_INFO = "rtmap_lbs_api/v1/floorinfo";// 获取某一建筑物的某一楼层的详细信息
	public final static String BUILD_POI_CATE = "rtmap_lbs_api/v1/build_classificationlist";// 获取某一建筑物下所有POI分类
	public final static String FLOOR_POI_CATE = "rtmap_lbs_api/v1/floor_classificationlist";// 获取某一楼层下所有POI的分类
	public final static String SEARCH_KEYWORDS = "rtmap_lbs_api/v1/search_keywords";// 某一建筑物下关键字搜索
	public final static String CHECK_IMAP = "rtmap_lbs_api/v1/imapinfo";// 根据建筑物和楼层获取imap文件信息
	public final static String SEARCH_ASSOICATION = "rtmap_lbs_api/v1/associationsearch";// 某一建筑物下搜索获取联想列表
	public final static String SEARCH_POI_CITY = "rtmap_lbs_api/v1/top10";// 获取热门店铺列表TOP10
	public final static String SEARCH_POI_CATE = "rtmap_lbs_api/v1/search_classification";// 某一建筑物内的某一分类的所有店铺
	public final static String TOP_FEEDBACK = "rtmap_lbs_api/v1/top_feedback";// 服务器根据APP的反馈统计热门店铺排行榜
	public final static String NAVIGATION = "rtmap_lbs_api/v2/rtmap/navigation";// 根据起点终点获取导航线路
	public final static String POI_INFO = "rtmap_lbs_api/v1/poiinfo";// 获取POI点详情
	public final static String POI_DESC = "rtmap_lbs_api/v1/rtmap/classify_poiinfo";// 获取POI描述
	public final static String LBS_POI = "rtmap_lbs_api/v1/rtmap/locate_poi";// 根据坐标获取周边的POI信息
	public final static String MAP_ANGLE = "rtmap_lbs_api/v1/rtmap/build_angle";// 地图偏转角
	public final static String POSITION_BUILDLIST = "rtmap_lbs_api/v1/rtmap/position_buildlist";// License建筑物列表
	public final static String BUILD_VALIDATE = "rtmap_lbs_api/v1/rtmap/position_validate";// License建筑物是否允许
	public final static String WHITE_LIST = "rtmap_lbs_api/v1/rtmap/sdk_device_signal";// 本手机白名单设置
//	public final static String LBS_USER_ID = "http://lbsapitest2.rtmap.com:8080/rtmap_lbs_api/v1/rtmap/lbsid";// 获取本设备唯一ID
	public final static String LBS_USER_ID = "https://lbsapi.rtmap.com/rtmap_lbs_api/v1/rtmap/lbsid";// 获取本设备唯一ID
}
