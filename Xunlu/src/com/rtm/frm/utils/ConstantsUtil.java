package com.rtm.frm.utils;

import android.os.Environment;

public class ConstantsUtil {
	
	public static final int MESSAGE_GET_LENGTH = 900002;
	
	public static final int SERVER_TEST = 0;
	public static final int SERVER_RELEASE = 1;
	
	public static final String DIR_NAME = Environment
			.getExternalStorageDirectory() + "/rtmap/";
	//类型
	public static final String TYPE_AIRPORT = "1";
	public static final String TYPE_MALL = "3";
	
	/* URL部分*/
	public static final String URL_ROOT_TEST = "http://wang.rtlbs.com/";
	public static final String URL_ROOT_RELEASE = "http://open2.rtmap.net/";

	public static final String URL_WEIBO_PROFILE = "https://api.weibo.com/2/users/show.json?uid=%s&source=%s&access_token=%s";
	public static final String URL_INVITE = "location/?request=";
	//奥莱
	public static final String URL_PRIZE_LIST="shopping/api_prize_list.php";
	// 已经放弃使用的搜索联想接口
	public static final String URL_SUGGEST = "shopping/api_search_con.php";
	public static final String URL_QUERY = "api_poi_key_2.php";
	public static final String URL_CLASS = "api_poi_class_list_4.php";
	public static final String URL_GROUP = "version/api_sea_index_class.php";
	public static final String URL_CLASSIFICATION = "version/api_sea_class_list.php";
	public static final String URL_POI_LIST = "api_poi_list_3.php";
	public static final String URL_CHECK_IN_COUNTER = "api_poi_tran_zhjgt.php";
	public static final String URL_POI_DETAIL = "api_poi_m_2.php";
	public static final String URL_BUILD_INFO = "api_fls_tile_info.php";
	public static final String URL_TRAFFIC_LINE = "api_traffic_info.php";
	public static final String URL_TRAFFIC_TYPE = "api_traffic_type.php";
	public static final String URL_POI_QUERY = "api_poi_list_p.php";
	public static final String URL_NAVIGATE_AIR = "navigation/api_dh.php";
	public static final String URL_NAVIGATE_MALL = "navigation/api_dh_2.php";
	/* map页获取活动数据的接口*/
	public static final String URL_GET_HUODONG = "market_active/get_event.php";
	public static final String URL_POIS = "api_poi_list_floor.php";
	public static final String URL_BUILD_LIST = "api_buildlist_all_4.php";
	public static final String URL_VERSION = "api_info.php";
	public static final String URL_NEARBY_BUILDS = "api_location.php";
	public static final String URL_BAOZANG = "shopping/api_poi_tuans_floor.php";
	/* 正在使用的搜索联想接口*/
	public static final String URL_SEARCH_KEY = "api_poi_key_2.php";
	public static final String URL_SEARCH_CLASS = "version/api_sea_class_poi_list.php";
	public static final String URL_FLOOR_INFO = "api_floor.php";
	public static final String URL_LOGIN = "api_login_2.php";
	public static final String URL_OFFLINE_DATA = "api_build_list.php";
	public static final String URL_USER_LOCATION = "api_cir_q_2.php";
	public static final String URL_STATISTICS_FLOOR = "tongji_floor.php";
	// public static final String URL_FRIEND_LIST= "api_friend_list.php";
	public static final String URL_FRIEND_LIST = "api_location_fri_list.php";
	public static final String URL_FRIEND_LOCATIONS = "api_location_fri_list_2.php";
	public static final String URL_FRIEND_LOCATION = "api_location_fri_2.php";
	public static final String URL_INITIATE_SHARE_LOCATION = "api_request_save.php";
	public static final String URL_RESPOND_SHARE_LOCATION = "api_response_share.php";
	public static final String URL_DISCONNECT_SHARE_LOCATION = "api_share_off.php";
	public static final String URL_FEEDBACK = "api_response_opinion.php";
	public static final String URL_DELETE_FRIEND = "api_friend_del.php";
	public static final String URL_GET_FRIEND_LOCATION = "api_location_fri_2.php";
	public static final String URL_SPLASH_LOGO = "api_holiday.php";
	public static final String URL_BIND_WEIBO = "api_weibo.php";
	public static final String URL_CHECKIN_COUNTER = "api_poi_zhjgt.php";
	public static final String URL_EXCEPTION_LOG = "api_response_bug.php";
	public static final String URL_OFFLINE_MAP = "maps_tile/zip_tile/%s.zip";
	public static final String URL_OFFLINE_MAP_EN = "maps_tile_en/zip_tile/%s.zip";
	public static final String URL_FLOOR_COUPON = "shopping/api_poi_tuans_floor.php";
	public static final String URL_USER_LOGIN = "api_ext_login.php";
	public static final String URL_IMAGE = "http://open2.rtmap.net/shopping/logo/%s.png";
	public static final String URL_FAVORABLE_BY_CITY = "shopping/api_poi_tuans_city.php";
	
	/*推送使用的接口*/
	public static final String URL_PUSH_BIND="api_getUserId.php";
	
	
	/*post请求时需要用到的key*/
	public static final String KEY_DEVICE_ID = "id_phone";
	public static final String KEY_PHONE_MODEL = "phone_model";
	public static final String KEY_BUILD_ID = "id_build";
	public static final String KEY_NAVIGATE_START_FLOOR = "start_floor";
	public static final String KEY_NAVIGATE_START_X = "start_x";
	public static final String KEY_NAVIGATE_START_Y = "start_y";
	public static final String KEY_NAVIGATE_END_FLOOR = "finish_floor";
	public static final String KEY_NAVIGATE_END_X = "finish_x";
	public static final String KEY_NAVIGATE_END_Y = "finish_y";
	public static final String KEY_ID_CLASS = "id_class";
	public static final String KEY_VERSION = "version";
	public static final String KEY_GENDER = "gender";
	public static final String KEY_AGE = "age";
	public static final String KEY_FEEDBACK = "opinion";
	public static final String KEY_KEYWORDS = "keywords";
	public static final String KEY_PLACE_NAME = "placename";
	public static final String KEY_POI_CLASS = "poi_class";
	public static final String KEY_ID_POI_CLASS = "id_poi_class";
	public static final String KEY_FLOOR = "floor";
	public static final String KEY_CURRENT_FLOOR = "current_floor";
	public static final String KEY_POI_NO = "poi_no";
	public static final String KEY_ID_AIRPORT = "id_airport";
	public static final String KEY_INTERNATION = "internation";
	public static final String KEY_ID_SUBCATALOG = "id_class_two";
	public static final String KEY_WEIBO = "weibo";
	public static final String KEY_LANGUAGE = "language";
	public static final String KEY_RELEASE = "apk_release_no";
	public static final String KEY_ID_FRIEND = "id_phone_friend";
	public static final String KEY_REQUEST_ID = "id_request";
	public static final String KEY_FRIEND_NO = "phone_fri_no";
	public static final String KEY_FRIEND_NAME = "name_friend";
	public static final String KEY_AGREE = "agree";
	public static final String KEY_PHONE_NO = "phone_no";
	public static final String KEY_NAME = "name";
	public static final String KEY_AIRLINE_COMPANY = "air_company";
	public static final String KEY_ROOT_CATALOG = "id_one";
	public static final String KEY_X = "coordinate_x";
	public static final String KEY_Y = "coordinate_y";
	public static final String KEY_TYPE_BUILD = "type_build";
	public static final String KEY_ID_APK = "id_apk";
	public static final String KEY_LATITUDE = "lat";
	public static final String KEY_LONGITUDE = "long";
	public static final String KEY_USER = "user";
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_PAGE_NO = "page_no";
	public static final String KEY_PAGE_SIZE = "page_size";
	public static final String KEY_PAGE_PER = "per_page";
	public static final String KEY_KEY = "key";
	public static final String KEY_BAOZANG_BUILDID = "id_build";
	public static final String KEY_PUSH_USERID="userId";
	public static final String KEY_PUSH_CHANNELID="channelId";
	public static final String KEY_PUSH_MAC="mac";
	public static final String KEY_PAGE = "page";
	public static final String KEY_NAME_CITY="name_city";
	public static final String KEY_ID_CITY="id_city";
	public static final String KEY_PAGESIZE="pagesize";
	public static final String KEY_IMPORTANT="important";
	
	//发送位置给服务器
	public static final String KEY_MAC="mac";
	public static final String KEY_TOSERVICE_BUILDID= "buildid";
	public static final String KEY_TOSERVICE_FLOOR= "floor";
	public static final String KEY_TOSERVICE_X= "x";
	public static final String KEY_TOSERVICE_Y= "y";
	public static final String KEY_TOSERVICE_LNG= "long";
	public static final String KEY_TOSERVICE_LAT= "lat";
	public static final String KEY_BZ_SUCC_USER_ID= "userid";
	public static final String KEY_BZ_SUCC_POI_ID= "id_poi";
	public static final String KEY_BZ_SUCC_SITE_ID= "id_site";
	public static final String KEY_BZ_SUCC_BRIDGE_ID= "id_bridge";
	
	/*SharedPreferences 存储key值*/
	public static final String PREF_RTMAP = "pref_rtmap";
	public static final String PREFS_AIRPORT_LIST = "prefs_airport_list";
	public static final String PREFS_MALL_LIST = "prefs_mall_list";
	public static final String PREFS_AIRPORT_LIST_EN = "prefs_airport_list_en";
	public static final String PREFS_UPDATE_TIME = "prefs_update_time";
	public static final String PREFS_GUIDE = "prefs_guide";
	public static final String PREFS_LAST_BUILD = "prefs_last_build";
	public static final String PREFS_LAST_FLOOR = "prefs_last_floor";
	public static final String PREFS_LANGUAGE = "prefs_language";
	public static final String PREFS_SPLASH_LOGO = "prefs_splash_logo";
	public static final String PREFS_WEIBO_NAME = "prefs_weibo_name";
	public static final String PREFS_FRIENDS = "prefs_friends";
	public static final String PREFS_CATALOG_LIST = "prefs_catalog_list";
	public static final String PREFS_SERVER = "prefs_server";
	public static final String PREFS_LOCATE = "prefs_locate";
	public static final String PREFS_MALL_CATALOGS = "PREFS_MALL_CATALOGS";
	public static final String PREFS_AIRPORT_CATALOGS = "PREFS_AIRPORT_CATALOGS";
	public static final String PREFS_TYPE = "PREFS_TYPE";
	public static final String PREFS_USER_BUILDS = "PREFS_USER_BUILDS";
	public static final String PREFS_USER = "PREFS_USER";
	public static final String PREFS_PASSWORD = "PREFS_PASSWORD";
	public static final String PREFS_AIRPORT_CITY = "PREFS_AIRPORT_CITY";
	public static final String PREFS_MALL_CITY = "PREFS_MALL_CITY";
	public static final String PREFS_LAST_UPDATE_DATA_DATE = "PREFS_LAST_UPDATE_DATA_DATE";
	public static final String PREFS_LAST_BUILD_ID = "PREFS_LAST_BUILD_ID";
	public static final String PREFS_LAST_BUILD_FLOOR = "PREFS_LAST_BUILD_FLOOR";
	public static final String PREFS_LAST_BUILD_NAME = "PREFS_LAST_BUILD_NAME";
	public static final String PREFS_LAST_CITY_NAME = "PREFS_LAST_CITY_NAME";
	public static final String PREFS_LAST_BUILD_IS_PRIVATE = "PREFS_LAST_BUILD_IS_PRIVATE";
	
	/*Handler 请求what常量*/
	public static final int HANDLER_POST_BUILD_LIST = 0x1000;
	
	public static final int HANDLER_POST_CHECK_UPDATE = 0x1001;
	
	public static final int HANDLER_POST_FETCH_COUPONS = 0x1002;//请求当前楼层共有几个优惠店铺

	public static final int HANDLER_POST_LOGIN = 0x1005;//用户登录
	
	public static final int HANDLER_POST_NAV_ROUTE = 0x1006;//导航路线
	
	public static final int HANDLER_POST_ARROUTE = 0x1007;//导航路线
	
	public static final int HANDLER_POST_BAOZANG = 0x1008;//寻宝
	
	public static final int HANDLER_POST_PUSH_BIND=0x1009;
	
	public static final int HANDLER_POST_BAOZANG_NO = 0x1010;////寻宝
	
	public static final int HANDLER_POST_BAOZANG_POSITION = 0x1011;//寻宝
	
	public static final int HANDLER_POST_FAVORABLE = 0x1012;//请求优惠数据
	
	
	public static final int HANDLER_POST_SEARCH_POI = 6;//导航路线
	
	public static final int HANDLER_THREAD_INIT_OK = 100;
	
	public static final int HANDLER_THREAD_INIT_ERR = 101;
	
	public static final int HANDLER_POST_TO_SERVICE= 102;
	
	public static final int HANDLER_POST_TO_SUCC= 103;
	
	public static final int HANDLER_THREAD_INIT_FAVORABLE_OK = 104;
	
	public static final int HANDLER_THREAD_INIT_FAVORABLE_ERR = 105;
	
	public static final int HANDLER_POST_FAVORABLE_BY_CITY = 106;
	
	public static final int HANDLER_POST_FAVORABLE_BY_BUILD_ID_FLOOR = 107;
	
	public static final int HANDLER_POST_USER_POSITION = 108;
	
	public static final int HANDLER_POST_BZ_LIST = 109;
	
	/*状态标识*/
	public static final int STATE_INIT = 1;//正在初始化
	
	public static final int STATE_INIT_FINISHED = 2;//初始化完成
	
	public static final int STATE_NET_ERR_UNUSED = 3;//网络故障
	
	public static final int STATE_CONN_ERR = 4;//服务连接错误
	
	/*建筑物类型*/
	public static final int BUILD_TYPE_AIRPORT = 1;//机场
	
	public static final int BUILD_TYPE_MALL = 3;//商场
	
	public static final int BUILD_TYPE_ALL = 4;//所有
	
	public static final int BUILD_TYPE_PRIVATE = 5;//私有
	
	
	public static final String EVENT_CATALOG = "event_catalog";
	public static final String EVENT_CLICK_AIRPORT = "event_click_airport";
	public static final String EVENT_CLICK_BUILD_ITEM = "event_click_build_item";
	public static final String EVENT_CLICK_FLOOR = "event_click_floor";
	public static final String EVENT_CLICK_MALL = "event_click_mall";
	public static final String EVENT_CLICK_MENU = "event_click_menu";
	public static final String EVENT_CLICK_MY_MAP = "event_click_my_map";
	public static final String EVENT_CLICK_POI = "event_click_indoor_item";
	public static final String EVENT_CLICK_SEARCH = "event_click_search";
	public static final String EVENT_CLICK_SWITCH_CITY = "event_click_switch_city";
	public static final String EVENT_CLICK_TOOL = "event_click_tool";
	public static final String EVENT_LOGIN = "event_login";
	public static final String EVENT_NAVIGATE = "event_navigate";
	public static final String EVENT_SHARE_POI = "event_share_poi";
	public static final String EVENT_SWITCH_FLOOR = "event_switch_floor";
	public static final String EVENT_CLICK_TITLE = "event_click_title";
	public static final String EVENT_CLICK_SWITCH_FLOOR = "event_click_switch_floor";
	
}
