package com.rtmap.wifipicker.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.rtmap.wifipicker.R;
import com.rtmap.wifipicker.core.WPApplication;

public class DTDateTimeUtil {

	private static DTDateTimeUtil instance;
	private Date mDate;

	public static final String[] MONTH = new String[] { "1月", "2月", "3月", "4月", "5月", "6月",
			"7月", "8月", "9月", "10月", "11月", "12月" };
	public static final String[] WEEK = new String[] { "周日", "周一", "周二", "周三", "周四", "周五",
			"周六" };
	public static final String[] HOURARRAY = new String[] { "00:00", "01:00", "02:00",
			"03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00",
			"10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00",
			"17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00" };

	private SimpleDateFormat mFormat;

	private DTDateTimeUtil() {
		mDate = new Date();
		mFormat = new SimpleDateFormat();
	}

	public static DTDateTimeUtil getInstance() {
		if (instance == null)
			instance = new DTDateTimeUtil();
		return instance;
	}


	/**
	 * 得到日期
	 * 
	 * @param time
	 * @return
	 */
	public String getDateText(long time,String pattern) {
		mFormat.applyPattern(pattern);
		mDate.setTime(time);
		return mFormat.format(mDate);
	}

	/**
	 * 
	 * @param datestr
	 *            日期
	 * @param day
	 *            相对天数，为正数表示之后，为负数表示之前
	 * @return 指定日期字符串n天之前或者之后的日期
	 */
	public static String[] getBeforeAfterDate(long time, int day) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date olddate = null;
		df.setLenient(false);
		olddate = new Date(time);
		Calendar cal = new GregorianCalendar();
		cal.setTime(olddate);

		int Year = cal.get(Calendar.YEAR);
		int Month = cal.get(Calendar.MONTH);
		int Day = cal.get(Calendar.DAY_OF_MONTH);
		String[] dateArray = new String[day];
		for (int i = 0; i < dateArray.length; i++) {
			cal.set(Calendar.YEAR, Year);
			cal.set(Calendar.MONTH, Month);
			cal.set(Calendar.DAY_OF_MONTH, Day + i);
			Date date = new Date(cal.getTimeInMillis());
			dateArray[i] = df.format(date);
		}
		return dateArray;
	}

	/**
	 * 输入小时与分钟数，输出时间
	 * 
	 * @param hour
	 * @param addMinutes
	 * @return
	 */
	public String getTimeHour(int hour, int addMinutes) {
		mFormat.applyPattern("HH:mm");
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, addMinutes);
		mDate.setTime(cal.getTimeInMillis());
		return mFormat.format(mDate);
	}
}
