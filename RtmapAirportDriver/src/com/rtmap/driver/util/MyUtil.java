package com.rtmap.driver.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class MyUtil {

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources()
                .getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float px) {
        final float scale = context.getResources()
                .getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    /**
     * 判断SDCard是否存在 [当没有外挂SD卡时，内置ROM也被识别为存在sd卡]
     *
     * @return
     */
    public static boolean isSdCardExist() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡根目录路径
     *
     * @return
     */
    public static String getSdCardPath() {
        boolean exist = isSdCardExist();
        String sdpath = "";
        if (exist) {
            sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            T.sCustom("请插入SD卡后再试-_-");
        }
        return sdpath;
    }


    /***
     * @param dir
     * @return
     */
    public static String getAppSDCardFileDir(String dir) {
        File sdDir = null;
        String childPath = PreferencesUtil.getString(KEY_LOG_PARENT_DIR, "");
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {

            File f = Environment.getExternalStorageDirectory();
            if (childPath != null && childPath.length() != 0){
                dir = dir+"/"+childPath;
            }
            sdDir = new File(f, dir);
            if (!sdDir.exists()) {
                sdDir.mkdirs();
            }
        }
        if (sdDir == null)
            return null;

        return sdDir.toString();
    }

    /***
     * 获取APP在sd卡存储的根目录
     * @param dir
     * @return
     */
    public static String getAppSDCardFileRootDir(String dir) {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            File f = Environment.getExternalStorageDirectory();
            sdDir = new File(f, dir);
            if (!sdDir.exists()) {
                sdDir.mkdirs();
            }
        }
        if (sdDir == null)
            return null;

        return sdDir.toString();
    }

    /***
     * 获取根据时间、司机id、车辆id生成的路径名称
     * @return
     */
    public static String getChildPathName() {
        String childPath = PreferencesUtil.getString(KEY_LOG_PARENT_DIR, "");
        return childPath;
    }

    private static final String KEY_LOG_PARENT_DIR = "KEY_LOG_PARENT_DIR";
    /***
     * 时间（精确到分钟）+司机id+车辆id
     */
    public static void doLogin(String diverId,String carId,String deviceId,String terminalNo) {
        String date = TimeUtil.getTime3();
        PreferencesUtil.putString(KEY_LOG_PARENT_DIR,date+"_"+diverId+"_"+carId+"_"+deviceId+"_"+terminalNo);

        FileUtil.saveSpecialEventToFile("login");
    }

    public static void doLogout() {
        FileUtil.saveSpecialEventToFile("logout");

        PreferencesUtil.putString(KEY_LOG_PARENT_DIR,"");
    }

}
