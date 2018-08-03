package com.dingtao.libs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Looper;

import com.dingtao.libs.exception.DTExceptionHandler;

public class DTApplication extends Application {

	private static DTApplication instance;
	private List<Activity> activityList;
	public static ExecutorService EXECUTOR;
	/** 主线程ID */
	private static int mMainThreadId = -1;
	/** 主线程ID */
	private static Thread mMainThread;
	/** 主线程Handler */
	private static Handler mMainThreadHandler;
	/** 主线程Looper */
	private static Looper mMainLooper;
	
	private SharedPreferences mSharedPreferences;
	public static String VERSION;

	public static DTApplication getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		
		mMainThreadId = android.os.Process.myTid();
		mMainThread = Thread.currentThread();
		mMainThreadHandler = new Handler();
		mMainLooper = getMainLooper();
		
		mSharedPreferences = getSharedPreferences("application",
				Context.MODE_PRIVATE);
		
		activityList = new ArrayList<Activity>();
		EXECUTOR = Executors.newCachedThreadPool();
		
		// 版本号
				PackageManager manager = this.getPackageManager();
				PackageInfo info;
				try {
					info = manager.getPackageInfo(this.getPackageName(), 0);
					VERSION = info.versionName;
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
		// 异常注册
		Thread.setDefaultUncaughtExceptionHandler(DTExceptionHandler
				.getInstance(getApplicationContext()));// 处理异常
	}
	
	public SharedPreferences getShare() {
		return mSharedPreferences;
	}

	/**
	 * 添加Activity到容器中
	 * 
	 * @param activity
	 */
	public void addActivity(Activity activity) {
		activityList.add(activity);
	}

	/**
	 * 遍历所有Activity并finish
	 */
	@Deprecated
	public void clearActivity() {
		for (Activity activity : activityList) {
			if (activity != null)
				activity.finish();
		}
		activityList.clear();
	}

	/**
	 * 移除具体的activity对象
	 * 
	 * @param activity
	 */
	public void removeActivity(Activity activity) {
		activityList.remove(activity);
	}

	/**
	 * 退出应用
	 */
	public void exit() {
		clearActivity();
		System.exit(0);
	}
	/** 获取主线程ID */
	public static int getMainThreadId() {
		return mMainThreadId;
	}

	/** 获取主线程 */
	public static Thread getMainThread() {
		return mMainThread;
	}

	/** 获取主线程的handler */
	public static Handler getMainThreadHandler() {
		return mMainThreadHandler;
	}

	/** 获取主线程的looper */
	public static Looper getMainThreadLooper() {
		return mMainLooper;
	}
}
