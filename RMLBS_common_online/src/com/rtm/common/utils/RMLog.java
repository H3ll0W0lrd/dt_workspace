/**
 * 地图定位共同使用的工具
 */
package com.rtm.common.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public class RMLog {
	/** 是否开启调试模式，如果不保存任何数据则 >= LOG_LEVEL_ERROR (16) **/
	public static int LOG_LEVEL = 16;
	/** log记录文件 **/
	final static String LOG_FILE_EXT = ".log";
	/** tag标记 **/
	private final static String LOG_TAG_STRING = "LogUtil";

	public final static int LOG_LEVEL_ERROR = 16;
	public final static int LOG_LEVEL_WARN = 8;
	public final static int LOG_LEVEL_INFO = 4;
	public final static int LOG_LEVEL_DEBUG = 2;

	private static boolean DEBUG() {
		return LOG_LEVEL <= LOG_LEVEL_DEBUG;
	}

	private static boolean INFO() {
		return LOG_LEVEL <= LOG_LEVEL_INFO;
	}

	private static boolean WARN() {
		return LOG_LEVEL <= LOG_LEVEL_WARN;
	}

	private static boolean ERROR() {
		return LOG_LEVEL <= LOG_LEVEL_ERROR;
	}

	static PrintStream logStream;
	static boolean initialized = false;
	/** 时间显示格式，eg：[2010-01-22 13:39:1][D][com.a.c]error **/
	final static String LOG_ENTRY_FORMAT = "[%tF %tT][%s][%s]%s";

	public static void d(String tag, String msg) {
		if (DEBUG()) {
			String threadTag = Thread.currentThread().getName() + ":" + tag;
			Log.d(tag, threadTag + " : " + msg);
		}
	}

	public static void d(String tag, String msg, Throwable error) {
		if (DEBUG()) {
			String threadTag = Thread.currentThread().getName() + ":" + tag;
			Log.d(tag, threadTag + " : " + msg, error);
		}
	}

	public static void i(String tag, String msg) {
		if (INFO()) {
			String threadTag = Thread.currentThread().getName() + ":" + tag;
			Log.i(tag, threadTag + " : " + msg);
		}
	}

	public static void i(String tag, String msg, Throwable error) {
		if (INFO()) {
			String threadTag = Thread.currentThread().getName() + ":" + tag;
			Log.i(tag, threadTag + " : " + msg, error);
		}
	}

	public static void w(String tag, String msg) {
		if (WARN()) {
			String threadTag = Thread.currentThread().getName() + ":" + tag;
			Log.w(tag, threadTag + " : " + msg);
		}
	}

	public static void w(String tag, String msg, Throwable error) {
		if (WARN()) {
			String threadTag = Thread.currentThread().getName() + ":" + tag;
			Log.w(tag, threadTag + " : " + msg, error);
		}
	}

	public static void e(String tag, String msg) {
		if (ERROR()) {
			String threadTag = Thread.currentThread().getName() + ":" + tag;
			Log.e(tag, threadTag + " : " + msg);

			if (LOG_LEVEL <= LOG_LEVEL_ERROR)
				write("E", threadTag + "-" + tag, msg, null);
		}
	}

	public static void e(String tag, String msg, Throwable error) {
		if (ERROR()) {
			String threadTag = Thread.currentThread().getName() + ":" + tag;
			Log.e(tag, threadTag + " : " + msg, error);

			if (LOG_LEVEL <= LOG_LEVEL_ERROR)
				write("E", threadTag + "-" + tag, msg, error);
		}
	}

	private static void write(String level, String tag, String msg,
			Throwable error) {
		if (!initialized) {
			init();
		}
		if (logStream == null || logStream.checkError()) {
			initialized = false;
			return;
		}
		Date now = new Date();
		logStream.printf(LOG_ENTRY_FORMAT, now, now, level, tag, msg);
		logStream.println();

		if (error != null) {
			error.printStackTrace(logStream);
			logStream.println();
		}
	}

	public static synchronized void init() {
		if (initialized) {
			return;
		}
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("",
					Locale.SIMPLIFIED_CHINESE);
			sdf.applyPattern("yyyy-MM-dd");
			String filePath = RMFileUtil.getLibsDir()
					+ sdf.format(System.currentTimeMillis()) + "_"
					+ LOG_FILE_EXT;
			File logFile = new File(filePath);
			logFile.createNewFile();

			Log.d(LOG_TAG_STRING, RMLog.class.getName() + " : Log to file : "
					+ logFile);
			if (logStream != null) {
				logStream.close();
			}
			logStream = new PrintStream(new FileOutputStream(logFile, true),
					true);
			initialized = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (logStream != null)
			logStream.close();
	}

}
