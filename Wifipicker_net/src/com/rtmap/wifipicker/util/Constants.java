package com.rtmap.wifipicker.util;

import android.os.Environment;

public class Constants {
	public static final boolean LOG = true;
	public static final boolean TEST = false;
	public static final boolean OFFLINE = false;
	public static final String VERSION = "2";
	
	public static final int GATHER_WALK_COUNT = 1;
	public static final int GATHER_COUNT = 40;
	
	public static final String TYPE_WIFI_NORMAL = "0";
	public static final String TYPE_WIFI_WALK = "1";
	public static final String TYPE_MAP_MODIFY_ROUTE = "map_modify_route";//路线修正的线
	public static final String TYPE_MAP_MODIFY_POINT = "map_modify_point";//路线修正的点
	public static final String TYPE_MAP_NET = "map_net";//路线修正的点
	public static final String TYPE_MAP_ROAD_NET = "map_road_net";//路网采集
	
	public static final int VERIFICATION_SUCCESS = 1;
	
	// 数据库版本
	public static final int DATABASE_VERSION = 0;
	// 数据库名
	public static final String DATABASE_NAME = "WifiPicker.db";
	
	public static final int MESSAGE_GET_LENGTH = 900002;
	public static final int NOTIFY_DOWNLOAD_ID = 10;
	
	public static final String DIR_NAME = Environment.getExternalStorageDirectory() + "/rtmapData0/";
	public static final String WIFI_PICKER_PATH = DIR_NAME + "WifiPicker/";
	public static final String MAP_DATA = Environment.getExternalStorageDirectory() + "/rtmapData0/" + "mdata/";
	
	public static final String URL_ROOT_TEST = "http://wang.rtlbs.com/";
	
	public static final String PREF_RTMAP = "pref_rtmap";
	public static final String PREFS_USERNAME = "prefs_username";
	public static final String PREFS_PASSWORD = "prefs_password";
	public static final String PREFS_TOKEN = "prefs_token";
	
	public static final String EXTRA_FILES = "extra_files";
	public static final String EXTRA_IS_OFFLINE_DATA_GATHER = "extra_offline_data";
	
	public static final String EXTRA_FLOOR = "extra_floor";
	public static final String EXTRA_GATHER_WALK = "extra_gather_walk";
	
	public static final String EXTRA_GATHER_MODE = "extra_gather_mode";
	
}
