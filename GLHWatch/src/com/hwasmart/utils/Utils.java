package com.hwasmart.utils;

import java.io.UnsupportedEncodingException;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

public class Utils {

	/**
	 * 获取当前链接Wifi的Mac地址
	 * @param context
	 * @return
	 */
	public static String getLocalMacAddress(Context context) {
		
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (wifi == null)
			return null;
		
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}
	
	/**
	 * 弹出短Toast
	 * @param context 上下文
	 * @param content 内容
	 */
	public static void showShotToast (Context context, String content) {
		Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * 弹出长Toast
	 * @param context 上下文
	 * @param content 内容
	 */
	public static void showLongToast (Context context, String content) {
		Toast.makeText(context, content, Toast.LENGTH_LONG).show();
	}
	
    public static Bitmap bitMatrix2Bitmap(BitMatrix matrix) {  
        int w = matrix.getWidth();  
        int h = matrix.getHeight();  
        int[] rawData = new int[w * h];  
        for (int i = 0; i < w; i++) {  
            for (int j = 0; j < h; j++) {  
                int color = Color.WHITE;  
                if (matrix.get(i, j)) {  
                    color = Color.BLACK;  
                }  
                rawData[i + (j * w)] = color;  
            }  
        }  
  
        Bitmap bitmap = Bitmap.createBitmap(w, h, Config.RGB_565);  
        bitmap.setPixels(rawData, 0, w, 0, 0, w, h);  
        return bitmap;  
    }  

    /**
     * 通过字符串生成一维码/二维码的Bitmap
     * @param content 要转化的字符串
     * @param format 一维码/二维码编码格式
     * @param width 转换的bitmap宽度
     * @param height 转换的bitmap高度
     * @return
     */
    public static Bitmap generateQRCode(String content, BarcodeFormat format, int width, int height) {  
        try {  
            // QRCodeWriter writer = new QRCodeWriter();  
            MultiFormatWriter writer = new MultiFormatWriter();  
            BitMatrix matrix = writer.encode(content, format, width, height);  
            return bitMatrix2Bitmap(matrix);  
        } catch (WriterException e) {  
            e.printStackTrace();  
        }  
        return null;  
    }
    
    /**
     * 字符串编码转换的实现方法
     * @param str  待转换编码的字符串
     * @param newCharset 目标编码
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String changeCharset(String str, String newCharset)
      throws UnsupportedEncodingException {
     if (str != null) {
      //用默认字符编码解码字符串。
      byte[] bs = str.getBytes();
      //用新的字符编码生成字符串
      return new String(bs, newCharset);
     }
     return null;
    }
    
    /**
     * 字符串编码转换的实现方法
     * @param str  待转换编码的字符串
     * @param oldCharset 原编码
     * @param newCharset 目标编码
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String changeCharset(String str, String oldCharset, String newCharset)
      throws UnsupportedEncodingException {
     if (str != null) {
      //用旧的字符编码解码字符串。解码可能会出现异常。
      byte[] bs = str.getBytes(oldCharset);
      //用新的字符编码生成字符串
      return new String(bs, newCharset);
     }
     return null;
    }
    
    /**
     * 规范化URL，使之以http://开头，以/结尾
     * @param url
     * @return
     */
    public static String formatURL(String url){
    	StringBuilder sb = new StringBuilder();
    	if (!url.startsWith("http:")){
    		sb.append("http://");
    	}
    	sb.append(url);
    	if (!url.endsWith("/")){
    		sb.append("/");
    	}
    	return sb.toString();
    }
}
