package com.rtmap.wifipicker.wifi;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * 获得手机信息
 * @author hotstar
 *
 */
public class PhoneManager {
    /**
     * 得到手机串号
     * @return
     */
    public static String getDeviceId(Context context) {
        return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }
    
    /**
     * 得到手机型号
     * @return
     */
    public static String getPhoneType() {
        return android.os.Build.MODEL;
    }
    
    /**
     * 得到操作系统版本
     * @return
     */
    public static String getOsVersion(Context context) {
        return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceSoftwareVersion();
    }
}