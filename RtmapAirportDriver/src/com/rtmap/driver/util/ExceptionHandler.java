package com.rtmap.driver.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import android.annotation.SuppressLint;
import android.content.Context;

public class ExceptionHandler implements UncaughtExceptionHandler {

	private static ExceptionHandler handler;

	// private static Context mContext;
	public static ExceptionHandler getInstence(Context cont) {
		// mContext=cont;
		if (handler == null) {
			handler = new ExceptionHandler();
		}
		Thread.setDefaultUncaughtExceptionHandler(handler);

		return handler;
	}

	@SuppressLint("SimpleDateFormat")
	public void uncaughtException(Thread thread, Throwable ex) {

		try {
			Properties mDeviceCrashInfo = new Properties();

			Writer info = new StringWriter();
			PrintWriter printWriter = new PrintWriter(info);
			ex.printStackTrace(printWriter);

			Throwable cause = ex.getCause();
			while (cause != null) {
				cause.printStackTrace(printWriter);
				cause = cause.getCause();
			}

			String result = info.toString();
			printWriter.close();

			mDeviceCrashInfo.put("STACK_TRACE", result);

			Date currentTime = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
			String dateString = formatter.format(currentTime);

			FileUtil.saveLogToFile(dateString + "\n" + result, FileUtil.LOG_NAME);

		} catch (Exception exception) {
			ex.printStackTrace();
		}

		android.os.Process.killProcess(android.os.Process.myPid());
	}

}
