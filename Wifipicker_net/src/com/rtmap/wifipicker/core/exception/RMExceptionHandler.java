package com.rtmap.wifipicker.core.exception;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.google.gson.Gson;
import com.rtmap.wifipicker.core.WPApplication;
import com.rtmap.wifipicker.core.model.ExceptionModel;
import com.rtmap.wifipicker.util.DTFileUtils;
import com.rtmap.wifipicker.util.DTLog;

public class RMExceptionHandler implements UncaughtExceptionHandler {

	private static RMExceptionHandler mHandler;

	private RMExceptionHandler() {
	}

	public synchronized static RMExceptionHandler getInstance() {
		if (mHandler == null) {
			mHandler = new RMExceptionHandler();
		}
		return mHandler;
	}

	@Override
	public void uncaughtException(Thread arg0, Throwable ex) {
		try {
			ExceptionModel model = new ExceptionModel();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace();
			ex.printStackTrace(pw);// 将异常写入sw
			PackageManager manager = WPApplication.getInstance()
					.getPackageManager();
			PackageInfo info = manager.getPackageInfo(WPApplication
					.getInstance().getPackageName(), 0);
			String version = info.versionName;// android版本名

			model.setClient("android");// 客户端
			model.setDevice(Build.MODEL);// 设备号
			model.setMessage(sw.toString());// 异常信息
			model.setOs("Android-OS" + Build.VERSION.RELEASE);// OS版本
			model.setTime(System.currentTimeMillis() / 1000 + "");// 系统时间
			model.setVersion("DTClock" + version);// 版本号

			File file = new File(DTFileUtils.getLogDir() + "log"
					+ System.currentTimeMillis() + ".txt");
			Gson gson = new Gson();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(gson.toJson(model).toString().getBytes());
			fos.flush();
			fos.close();
			DTLog.i("程序发生了异常,但是被哥捕获了:" + sw.toString());
			android.os.Process.killProcess(android.os.Process.myPid());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
