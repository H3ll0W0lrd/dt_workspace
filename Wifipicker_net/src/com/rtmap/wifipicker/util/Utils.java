package com.rtmap.wifipicker.util;

import java.security.MessageDigest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rtmap.wifipicker.R;

public class Utils {

    private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f' };

    public static boolean isEmpty(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        return false;
    }

    public static String toMd5(String s) {
        try {
            byte[] bytes = s.getBytes("UTF-8");
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(bytes);
            return toHexString(algorithm.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap decodeBitmap(String file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(file, options);
    }

    public static int getEquipmentWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getEquipmentHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static void showToast(Context context, int resId, int duration) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_toast, null);
        TextView text = (TextView) view.findViewById(R.id.text_toast);
        text.setText(resId);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM, 0, (int) context.getResources().getDimension(R.dimen.toast_margin));
        toast.setDuration(duration);
        toast.setView(view);
        toast.show();
    }

    public static void showToast(Context context, String message, int duration) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_toast, null);
        TextView text = (TextView) view.findViewById(R.id.text_toast);
        text.setText(message);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM, 0, (int) context.getResources().getDimension(R.dimen.toast_margin));
        toast.setDuration(duration);
        toast.setView(view);
        toast.show();
    }

    public static String toHexString(byte[] b) {
        if (b == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.toString();
    }

}
