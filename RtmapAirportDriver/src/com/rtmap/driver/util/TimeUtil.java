package com.rtmap.driver.util;

import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {

	public static int getYear() {

		Time t = new Time();
		t.setToNow();

		return t.year;
	}
	public static int getMonth() {
		
		Time t = new Time();
		t.setToNow();
		
		return t.month + 1;
	}
	public static int getDay() {
		
		Time t = new Time();
		t.setToNow();
		
		return t.monthDay;
	}
	public static int getHour() {
		
		Time t = new Time();
		t.setToNow();
		
		return t.hour;
	}
	public static int getMinute() {
		
		Time t = new Time();
		t.setToNow();
		
		return t.minute;
	}
	public static int getSec() {
		
		Time t = new Time();
		t.setToNow();
		
		return t.second;
	}
	
	public static String getTime1(){

		Time t = new Time();
		t.setToNow();
		String time = t.year + "-" + (t.month + 1) + "-" +t.monthDay + " " + t.hour + ":" + t.minute + ":" + t.second;
		return time;
	}
	
	public static String getFormatNowDate() {
		Date nowTime = new Date(System.currentTimeMillis());
		SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String retStrFormatNowDate = sdFormatter.format(nowTime);
		return retStrFormatNowDate;
	}
	
	public static String getTime2(){
		
		Date nowTime = new Date(System.currentTimeMillis());
		SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String retStrFormatNowDate = sdFormatter.format(nowTime);
		return retStrFormatNowDate;
	}

	public static String getTime3(){

		Date nowTime = new Date(System.currentTimeMillis());
		SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyyMMddHHmm");
		String retStrFormatNowDate = sdFormatter.format(nowTime);
		return retStrFormatNowDate;
	}

	/***
	 * 获取当前时间，并根据需要的时间格式，进行格式化
	 * @param format yyyyMMddHHmmss
	 * @return
	 */
	public static String getFormatNowDate(String format) {
		Date nowTime = new Date(System.currentTimeMillis());
		SimpleDateFormat sdFormatter = new SimpleDateFormat(format);
		String retStrFormatNowDate = sdFormatter.format(nowTime);
		return retStrFormatNowDate;
	}

}
