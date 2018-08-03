package com.rtm.frm.utils;

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
//	private static Context mContext;
	public static ExceptionHandler getInstence(Context cont){
//		mContext=cont;
		if(handler==null){
			handler=new ExceptionHandler();
		}
		Thread.setDefaultUncaughtExceptionHandler(handler);
		
		return handler;
	}
	
	@SuppressLint("SimpleDateFormat")
	public void uncaughtException(Thread thread, Throwable ex) {
		try{
////			TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
//			ex.printStackTrace();
//			String fileName=new SimpleDateFormat("dd_HH_mm_ss").format(new Date())+".log";
//			
//			File file=new File(getSDPath()+File.separator+fileName);
//			if(!file.exists()){
//				file.createNewFile();
//			}
//			PrintWriter pw=new PrintWriter(new FileOutputStream(file,true));
//			pw.write("--------------------------");
//			pw.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));  
//			pw.write("-------------------------\n");  
//			ex.printStackTrace(pw);
//			pw.close();
////			Util.ftpUpload("220.162.237.123","21", "yootest","123456", tm.getDeviceId(), getSDPath()+File.separator,fileName);
////			file.delete();
			
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
			SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.CHINA);
			String dateString = formatter.format(currentTime);
			
			//将崩溃日志写到本地
			FileUtil.saveLogToFile(dateString + "\n" + result, "Crash.txt");
			
		}catch(Exception exception){
			ex.printStackTrace();
		}
		
		android.os.Process.killProcess(android.os.Process.myPid());
	}

}
