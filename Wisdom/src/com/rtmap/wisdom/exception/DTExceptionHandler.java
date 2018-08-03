package com.rtmap.wisdom.exception;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.google.gson.Gson;
import com.rtmap.wisdom.core.DTApplication;
import com.rtmap.wisdom.model.DTExceptionModel;
import com.rtmap.wisdom.util.DTFileUtil;
import com.rtmap.wisdom.util.DTLog;

public class DTExceptionHandler implements UncaughtExceptionHandler {

	private static DTExceptionHandler mHandler;
	private static Context mContext;

	private DTExceptionHandler() {
	}

	public synchronized static DTExceptionHandler getInstance(Context context) {
		if (mHandler == null) {
			mHandler = new DTExceptionHandler();
			mContext = context;
		}
		return mHandler;
	}

	@Override
	public void uncaughtException(Thread arg0, Throwable ex) {
		try {
			DTExceptionModel model = new DTExceptionModel();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace();
			ex.printStackTrace(pw);//将异常写入sw
			PackageManager manager = DTApplication.getInstance()
					.getPackageManager();
			PackageInfo info = manager.getPackageInfo(DTApplication.getInstance()
					.getPackageName(), 0);
			String version = info.versionName;//android版本名
			
			model.setClient("android");//客户端
			model.setDevice(Build.MODEL);//设备号
			model.setMessage(sw.toString());//异常信息
			model.setOs("Android-OS"+Build.VERSION.RELEASE);//OS版本
			model.setTime(System.currentTimeMillis()/1000+"");//系统时间
			model.setVersion("DTClock"+version);//版本号
			DTFileUtil.createPath(DTFileUtil.getLogDir());
			File file = new File(DTFileUtil.getLogDir()+"log-"+System.currentTimeMillis()+".txt");
			Gson gson = new Gson();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(gson.toJson(model).toString().getBytes());
			fos.flush();
			fos.close();
			DTLog.i("程序发生了异常,但是被哥捕获了");
			android.os.Process.killProcess(android.os.Process.myPid());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
