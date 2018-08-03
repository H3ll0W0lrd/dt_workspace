package com.rtmap.indoor_switch.utils;

import android.view.Gravity;
import android.widget.Toast;

import com.rtmap.indoor_switch.manager.AppContext;

/**
 * Created by ly on 15-7-30.
 */
public class ToastUtils {

    public static void shortToast(String msg) {
        Toast.makeText(AppContext.instance(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void shortToast(int msgId) {
        Toast.makeText(AppContext.instance(), msgId, Toast.LENGTH_SHORT).show();
    }

    public static void shortToastCenter(String msg) {
        Toast toast = Toast.makeText(AppContext.instance(), msg, Toast.LENGTH_SHORT);
        //可以控制toast显示的位置
        toast.setGravity(Gravity.CENTER, 0, 10);
        toast.show();
    }
    public static void shortToastCenter(int msgId) {
        Toast toast = Toast.makeText(AppContext.instance(), msgId, Toast.LENGTH_SHORT);
        //可以控制toast显示的位置
        toast.setGravity(Gravity.CENTER, 0, 10);
        toast.show();
    }
}
