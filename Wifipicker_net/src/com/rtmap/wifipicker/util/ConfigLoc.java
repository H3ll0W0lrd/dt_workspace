package com.rtmap.wifipicker.util;

import com.rtmap.wifipicker.util.ConstantLoc.ModeLoc;

public class ConfigLoc {
    /** 实时定位模式 **/
    public static int MODE = 0;
    /** 采集时间间隔 */
    public static int WIFI_PICK_TIME = 1000;
    /** 网络状态 **/
    public static int netStatus = 0;
    /** 实时定位时时间间隔(ms) **/
    public static int LOCATE_TIME = 600;
    /** 相隔多少次上传一次定位结果 **/
    public static int commit_LOCATE_count = 60;
    /** 手机串号 **/
    public static String MIEI = "";
    /** 包名 */
    public static String pakageName = "";

    /** 定位点显示时间间隔(ms) **/
    public static int LOCATE_SHOW_TIME = 150;
    /** 实时定位时最大运动速度，超过该速度则打点不平滑显示(m/s) **/
    public static double LOCATE_VELOCITY = 6.0;
    /** 定位webservice，服务器IP地址与端口 **/
    public static String URL = "http://192.168.0.101:8080/";
    /** 文件路根目录 **/
    private static String FILE_ROOT = "/mnt/sdcard/rtmap/";
    private static String FILE_LIB_ROOT = "libs/";
    private static String FILE_MAP_ROOT = "map/";
    /** 采集的wifi数据保存至文件 **/
    public final static String FILE_WIFI_INPUT = "LocationApp.wifi";

    /**
     * 设置文件根目录
     * @param root
     */
    public static void setFileRoot(String root) {
        FILE_ROOT = UtilLoc.getStorageCardPath() + root;
        FileUtil.createPath(FILE_ROOT);
    }

    public static String getFileRoot() {
        return FILE_ROOT;
    }

    public static String getLibRoot() {
        return getFileRoot() + FILE_LIB_ROOT;
    }

    public static String getMapRoot() {
        return getFileRoot() + FILE_MAP_ROOT;
    }

    /**
     * 当前模式是否有效
     * 
     * @param m
     *        当前模式，只有一个bit位有效
     * @return true 当前模式有效
     */
    public static boolean isMode(int m) {
        return (MODE & m) == m;
    }

    /**
     * 设置模式位
     * 
     * @param m
     *        模式位
     */
    public static void setMode(int m) {
        MODE = MODE | m;

        // 正常模式
        if (ModeLoc.MODE_NORMAL == m) {
            MODE = ModeLoc.MODE_NORMAL;
        }

    }

    public static void setConfig(String sdFile, int mode, int intervalTime) {
        LOCATE_TIME = intervalTime;
        FILE_ROOT = UtilLoc.getStorageCardPath() + "/" + sdFile.replaceAll("/", "") + "/";
        FileUtil.createPath(FILE_ROOT);
        FileUtil.createPath(FILE_ROOT + "libs/");
        MODE = mode;
    }
}
