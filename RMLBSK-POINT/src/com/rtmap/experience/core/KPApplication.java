package com.rtmap.experience.core;

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
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.rtmap.experience.core.model.CateInfo;
import com.rtmap.experience.core.model.CateList;
import com.rtmap.experience.util.DTFileUtils;
import com.rtmap.experience.util.DTStringUtils;

public class KPApplication extends Application {
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
	private static KPApplication instance;
	private SharedPreferences mSharedPreferences;
	public static String MAC;
	private List<Activity> activityList;

	public static String VERSION;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		activityList = new ArrayList<Activity>();
		mMainThreadId = android.os.Process.myTid();
		mMainThread = Thread.currentThread();
		mMainThreadHandler = new Handler();
		mMainLooper = getMainLooper();
		EXECUTOR = Executors.newCachedThreadPool();
		mSharedPreferences = getSharedPreferences(DTFileUtils.LC_INFO,
				Context.MODE_PRIVATE);

		// 版本号
		PackageManager manager = this.getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
			VERSION = info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		WifiInfo wifiinfo = wifi.getConnectionInfo();

		MAC = wifiinfo.getMacAddress().replaceAll(":", "").toUpperCase();
		initFolder();
		if (DTStringUtils.isEmpty(mSharedPreferences.getString(
				DTFileUtils.BUILD_CATE, null))) {
			Gson gson = new Gson();
			String[] str = new String[] { "购物", "写字楼", "餐饮", "住宿", "娱乐", "其他" };
			CateList cateList = new CateList();
			cateList.setResults(new ArrayList<CateInfo>());
			for (String s : str) {
				CateInfo i = new CateInfo();
				i.setName(s);
				cateList.getResults().add(i);
			}
			mSharedPreferences.edit()
					.putString(DTFileUtils.BUILD_CATE, gson.toJson(cateList))
					.commit();
		}
	}

	private void initFolder() {
		DTFileUtils.getDataDir();
		DTFileUtils.getDownloadDir();
	}

	public SharedPreferences getShare() {
		return mSharedPreferences;
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

	public static KPApplication getInstance() {
		return instance;
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
}
