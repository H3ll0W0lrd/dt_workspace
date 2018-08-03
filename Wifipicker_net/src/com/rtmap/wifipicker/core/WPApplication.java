package com.rtmap.wifipicker.core;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.rtm.common.utils.RMFileUtil;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.XunluMap;
import com.rtmap.wifipicker.core.exception.RMExceptionHandler;
import com.rtmap.wifipicker.util.Constants;
import com.rtmap.wifipicker.util.FileHelper;

public class WPApplication extends Application {
	private SharedPreferences mSharedPreferences;

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
	private static WPApplication instance;
	public static String VERSION ;

	public static String getOwnMac() throws Exception {
		WifiManager wm = (WifiManager) instance
				.getSystemService(Context.WIFI_SERVICE);
		String mac = wm.getConnectionInfo().getMacAddress();
		if (mac != null && mac.length() >= 12) {
			mac = mac.replaceAll(":", "");
			mac = mac.toUpperCase(Locale.getDefault());
			return mac;
		} else {
			wm.setWifiEnabled(true);
			while (true) {
				mac = wm.getConnectionInfo().getMacAddress();
				if (mac != null && mac.length() >= 12) {
					mac = mac.replaceAll(":", "");
					mac = mac.toUpperCase(Locale.getDefault());
					break;
				}
				Thread.sleep(1000);
			}
			return mac;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		mMainThreadId = android.os.Process.myTid();
		mMainThread = Thread.currentThread();
		mMainThreadHandler = new Handler();
		mMainLooper = getMainLooper();
		EXECUTOR = Executors.newCachedThreadPool();
		// CrashHandler crashHandler = CrashHandler.getInstance();
		// crashHandler.init(getApplicationContext());
		mSharedPreferences = getSharedPreferences(Constants.PREF_RTMAP,
				Context.MODE_PRIVATE);
		try {
			String mac = getOwnMac();
			mSharedPreferences.edit().putString("tag1", mac).commit();
		} catch (Exception e) {
			Toast.makeText(this, "获取mac地址失败", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		
		//版本号
				PackageManager manager = this.getPackageManager();
				PackageInfo info;
				try {
					info = manager.getPackageInfo(this.getPackageName(), 0);
					VERSION = info.versionName;
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}

		// 异常注册
		Thread.setDefaultUncaughtExceptionHandler(RMExceptionHandler
				.getInstance());// 处理异常

		initFolder();

		initMapColor();
		RMFileUtil.MAP_FILEROOT = "rtmapData0";
		MapView.MAPTEXT.setTextsize(this,24);// 地图字体大小
	}

	private void initFolder() {
		FileHelper.checkDir(Constants.DIR_NAME);
		FileHelper.checkDir(Constants.MAP_DATA);
		FileHelper.checkDir(Constants.WIFI_PICKER_PATH);
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

	public static WPApplication getInstance() {
		return instance;
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
}
