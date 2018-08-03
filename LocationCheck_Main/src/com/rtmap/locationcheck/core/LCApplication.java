package com.rtmap.locationcheck.core;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

import com.rtm.common.utils.Constants;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.frm.drawmap.DrawStyle;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.XunluMap;
import com.rtmap.locationcheck.core.exception.LCExceptionHandler;
import com.rtmap.locationcheck.core.model.User;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTStringUtils;

public class LCApplication extends Application {
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
	private static LCApplication instance;
	private SharedPreferences mSharedPreferences;
	public static String MAC;
	private ArrayList<String> blackList;

	public static String VERSION;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		mMainThreadId = android.os.Process.myTid();
		mMainThread = Thread.currentThread();
		mMainThreadHandler = new Handler();
		mMainLooper = getMainLooper();
		EXECUTOR = Executors.newCachedThreadPool();
		mSharedPreferences = getSharedPreferences(DTFileUtils.LC_INFO,
				Context.MODE_PRIVATE);

		blackList = new ArrayList<String>();

		try {

			BufferedReader br = new BufferedReader(new InputStreamReader(
					getAssets().open("sd.txt")));
			String line = "";
			while ((line = br.readLine()) != null) {
				// 将文本打印到控制台
				blackList.add(line);
			}
			br.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		WifiInfo wifiinfo = wifi.getConnectionInfo();

		MAC = wifiinfo.getMacAddress().replaceAll(":", "").toUpperCase();

		// 异常注册
		Thread.setDefaultUncaughtExceptionHandler(LCExceptionHandler
				.getInstance());// 处理异常

		// 版本号
		PackageManager manager = this.getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
			VERSION = info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		initFolder();
		initUser();
		initMapColor();

		MapView.MAPTEXT.setTextsize(30);// 地图字体大小
		RMFileUtil.MAP_FILEROOT = DTFileUtils.ROOT_DIR;
	}
	
	public boolean isBlack(String name) {
		if(DTStringUtils.isEmpty(name)||blackList.contains(name.toLowerCase()))
			return true;
		return false;
	}
	
	private void initFolder() {
		DTFileUtils.getDataDir();
		DTFileUtils.getDownloadDir();
	}

	private void initUser() {
		String user = mSharedPreferences.getString(DTFileUtils.PREFS_USERNAME,
				null);
		String password = mSharedPreferences.getString(
				DTFileUtils.PREFS_PASSWORD, null);
		String token = mSharedPreferences.getString(DTFileUtils.PREFS_TOKEN,
				null);
		if (DTStringUtils.isEmpty(user)) {
			return;
		}
		User.getInstance().setUserName(user);
		User.getInstance().setPassword(password);
		User.getInstance().setToken(token);
	}

	/**
	 * 初始化地图颜色
	 */
	private void initMapColor() {
		MapView.MAPINVALID.setColorfill(0xffF1EADE);
		MapView.MAPUNKNOWN.setColorfill(0xffD2D4D6);
		MapView.MAPPOI.setColorfill(0xffF8F8D7);
		MapView.MAPWC.setColorfill(0xffFCE2E6);
		MapView.MAPSTAIRS.setColorfill(0xffFCDDBD);
		MapView.MAPGROUND.setColorfill(-1);
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

	public static LCApplication getInstance() {
		return instance;
	}
}
