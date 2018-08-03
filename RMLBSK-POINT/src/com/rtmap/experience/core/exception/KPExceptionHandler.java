package com.rtmap.experience.core.exception;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;

public class KPExceptionHandler implements UncaughtExceptionHandler {

	private static KPExceptionHandler mHandler;
	private static Context mContext;

	private KPExceptionHandler() {
	}

	public synchronized static KPExceptionHandler getInstance(Context context) {
		if (mHandler == null) {
			mHandler = new KPExceptionHandler();
			mContext = context;
		}
		return mHandler;
	}

	@Override
	public void uncaughtException(Thread arg0, Throwable ex) {
//		try {
//			MTExceptionModel model = new MTExceptionModel();
//			StringWriter sw = new StringWriter();
//			PrintWriter pw = new PrintWriter(sw);
//			ex.printStackTrace();
//			ex.printStackTrace(pw);//将异常写入sw
//			PackageManager manager = MTApplication.getInstance()
//					.getPackageManager();
//			PackageInfo info = manager.getPackageInfo(MTApplication.getInstance()
//					.getPackageName(), 0);
//			String version = info.versionName;//android版本名
//			
//			model.setClient("android");//客户端
//			model.setDevice(Build.MODEL);//设备号
//			model.setMessage(sw.toString());//异常信息
//			model.setOs("Android-OS"+Build.VERSION.RELEASE);//OS版本
//			model.setTime(System.currentTimeMillis()/1000+"");//系统时间
//			UserInfo user = MTSqlite.getInstance().getUser();
//			model.setUser_id(user==null?0:user.getUserId());//用户ID
//			model.setVersion("DTClock"+version);//版本号
//			
//			File file = new File(MTFileUtils.getLogDir()+"log.txt");
//			Gson gson = new Gson();
//			FileOutputStream fos = new FileOutputStream(file);
//			fos.write(gson.toJson(model).toString().getBytes());
//			fos.flush();
//			fos.close();
//			MTLog.i("程序发生了异常,但是被哥捕获了:"+sw.toString());
//			android.os.Process.killProcess(android.os.Process.myPid());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

}
