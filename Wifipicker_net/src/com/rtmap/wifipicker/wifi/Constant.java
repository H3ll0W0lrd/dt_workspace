package com.rtmap.wifipicker.wifi;

import android.os.Environment;

public class Constant {
    public static final String FILE_MAP = "map/";
    public static final String FILE_BACK = "back/";

    /** 保存poi文件的分隔符 **/
    public static final String FILE_POI_SPIT = "\t";
    /** 保存poi文件的换行符 **/
    public static final String FILE_POI_ENDLINE = "\n";
    /** 保存point文件的分隔符 **/
    public static final String FILE_POINT_SPIT = "\t";
    /** 保存point文件的换行符 **/
    public static final String FILE_POINT_ENDLINE = "\n";
    /** poi文件的后缀名 **/
    public static final String FILE_POI_FILENAME = ".poi";
    /** 文件名分隔符 **/
    public static final String FILE_FILENAME_SPACE = "_";
    /** 历史点记录文件的后缀名 **/
    public static final String FILE_HISTORY_FILESUFFIX = ".pnt";
    public static final String FILE_WALK_FILESUFFIX = ".walk1";
    public static final String FILE_ERROR_FILENAME = ".error";
    /** 历史点记录文件的后缀名 **/
    public static final String FILE_ROOT_MAP = "map/";

    /** 用户与服务器通信的根目录 **/
//    private static final String WEB_SERVICE_ROOT = "http://192.168.1.126/application/upload/";
    private static final String WEB_SERVICE_ROOT = "http://wang.rtlbs.com/application/upload/";
    /** 用户上传数据的地址 **/
    public static final String WEB_SERVICE_GATHER = WEB_SERVICE_ROOT + "upload_file.php";
    /** 用户登录 **/
    public static final String WEB_SERVICE_LOGIN = WEB_SERVICE_ROOT + "login_ex.php";
    /** 用户拉取地图的地址 **/
    public static final String WEB_SERVICE_DOWNLOAD_MAP_ROOT = WEB_SERVICE_ROOT + "img_floor/";
    /** 用户地图分隔符 **/
    public static final String WEB_FILE_SPACE = "-";
    /** 用户地图名之间分隔符 **/
    public static final String WEB_FILES_SPIT = "_";
    /** 用户地图名之间分隔符 **/
    public static final String WEB_FILES_MAP_FILESUFFIX = ".jpg";
    /** 用户地图名之间分隔符 **/
    public static final String WEB_FILES_POI_NAME = "class";
    /** 用户地图名之间分隔符 **/
    public static final String WEB_FILES_POI_FILESUFFIX = ".xml";
    
    public static final String DIR_NAME = Environment.getExternalStorageDirectory() + "/WifiPick/";
    public static final int DATABASE_VERSION = 1;
}
