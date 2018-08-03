package com.example.fragmentdemo.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;

public class MTApplication extends Application {

	public static final String WX_APP_ID = "wx74bb60eaf0c412d9";
	private static MTApplication instance;
	/** activity管理器 */
	private List<Activity> activityList;
	/** 线程执行器 */
	public static ExecutorService EXECUTOR;
	/** 主线程ID */
	private static int mMainThreadId = -1;
	/** 主线程ID */
	private static Thread mMainThread;
	/** 主线程Handler */
	private static Handler mMainThreadHandler;
	/** 主线程Looper */
	private static Looper mMainLooper;

	public static MTApplication getInstance() {
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
		activityList = new ArrayList<Activity>();
		EXECUTOR = Executors.newCachedThreadPool();
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
	 * 得到activity的size
	 * 
	 * @return
	 */
	public int getActivitySize() {
		return activityList.size();
	}

	/**
	 * 遍历某position之后的Activity并finish
	 */
	public void clearLastActivity(int position) {
		for (int i = position; i < activityList.size(); i++) {
			Activity activity = activityList.get(i);
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
		if (activityList.contains(activity))
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
