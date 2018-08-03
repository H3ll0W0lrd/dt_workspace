package com.rtm.location.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class UtilLoc {

	// 格式：年－月－日
	public static final String LONG_DATE_FORMAT = "yyyy-MM-dd";

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
	 * 把日期转换为字符串
	 * 
	 * @param date
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String dateToString(java.util.Date date, String format) {
		String result = "";
		SimpleDateFormat formater = new SimpleDateFormat(format);
		try {
			result = formater.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取当前的日期(yyyy-MM-dd)
	 */
	public static String getCurrDay() {
		return UtilLoc.dateToString(new Date(), UtilLoc.LONG_DATE_FORMAT);
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

	/**
	 * 休眠
	 * 
	 * @param time
	 *            休眠时间(ms)
	 */
	public static void Sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String intToIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + (i >> 24 & 0xFF);
	}

	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static String getSystemProperty(String propName) {
		String line;
		BufferedReader input = null;
		try {
			Process p = Runtime.getRuntime().exec("getprop " + propName);
			input = new BufferedReader(
					new InputStreamReader(p.getInputStream()), 1024);
			line = input.readLine();
			input.close();
		} catch (IOException ex) {
			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
		return line;
	}

}
