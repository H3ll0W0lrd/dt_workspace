package com.rtmap.wifipicker.util;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Environment;

public class UtilLoc {
    public static final String LONG_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 检查网络连接状态
     * 
     * @return {@link boolean} true 有效
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }

        return manager.getActiveNetworkInfo().isAvailable();
    }

    /**
     * 检查sd卡是否有效
     * 
     * @return true 有效
     */
    public static boolean isSdcardValid() {
        return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
    }

    /**
     * 得到sd卡的根目录
     * 
     * @return {@link String} 不存在返回空字符串
     */
    public static String getStorageCardPath() {
        File sdDir = null;
        if (isSdcardValid()) {
            sdDir = Environment.getExternalStorageDirectory();// 获取根目录
        } else {
            return "";
        }

        return (sdDir == null) ? "" : sdDir.toString();
    }

    /**
     * 得到毫秒时间
     * 
     * @return
     */
    public static String getTimeMillis() {
        Calendar c = Calendar.getInstance();
        long time = c.getTimeInMillis();
        return String.valueOf(time);
    }

    /**
     * 得到毫秒时间
     * 
     * @return
     */
    public static long getCurTimeMillis() {
        Calendar c = Calendar.getInstance();
        return c.getTimeInMillis();
    }

    /**
     * 判断字符串是否为数字
     * 
     * @param str
     * @return true 为全数字
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 获得UTC时间，主要用于与服务器进行通信
     * 
     * @return utc时间
     */
    public static long getUtcTime() {
        // 1、取得本地时间：
        java.util.Calendar cal = java.util.Calendar.getInstance(Locale.CHINA);
        // 2、取得时间偏移量：
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        // 3、取得夏令时差：
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));

        return cal.getTimeInMillis();
    }
}
