package com.rtmap.locationdemo.exception;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import com.google.gson.Gson;
import com.rtm.common.utils.RMFileUtil;
import com.rtmap.locationdemo.DemoApplication;
import com.rtmap.locationdemo.model.ExceptionModel;

public class ExceptionHandler implements UncaughtExceptionHandler {

	private static ExceptionHandler mHandler;

	private ExceptionHandler() {
	}

	public synchronized static ExceptionHandler getInstance() {
		if (mHandler == null) {
			mHandler = new ExceptionHandler();
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
			PackageManager manager = DemoApplication.getInstance()
					.getPackageManager();
			PackageInfo info = manager.getPackageInfo(DemoApplication
					.getInstance().getPackageName(), 0);
			String version = info.versionName;// android版本名

			model.setClient("android");// 客户端
			model.setDevice(Build.MODEL);// 设备号
			model.setMessage(sw.toString());// 异常信息
			model.setOs("Android-OS" + Build.VERSION.RELEASE);// OS版本
			model.setTime(System.currentTimeMillis() / 1000 + "");// 系统时间
			model.setVersion("DTClock" + version);// 版本号

			RMFileUtil.createPath(RMFileUtil.getLogDir());
			File file = new File( RMFileUtil.getLogDir()+"app-log-" + System.currentTimeMillis()
					+ ".txt");
			Gson gson = new Gson();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(gson.toJson(model).toString().getBytes());
			fos.flush();
			fos.close();
			android.os.Process.killProcess(android.os.Process.myPid());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
