package com.rtm.frm;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

import com.baidu.frontia.FrontiaApplication;
import com.baidu.mapapi.SDKInitializer;
import com.rtm.frm.model.Version;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.ExceptionHandler;
import com.rtm.frm.utils.PreferencesUtil;

public class XunluApplication extends FrontiaApplication {
	public static XunluApplication mApp;
	private static List<Activity> mList = new LinkedList<Activity>();
	public String mCurrentVersion; //当前版本信息
	// 从服务器获取的版本信息，用来判断是否升级
	public static Version versionModel = null;
	
	private TelephonyManager mTelephonyManager;
	
	private String mRootUrl;
	
	public static boolean LocationIsRun;
	
	private String buildType;
	
	ExceptionHandler exceptionHandler;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		// 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
		SDKInitializer.initialize(this);
		mApp = this;
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		
		//初始化崩溃时抓log助手
		exceptionHandler = ExceptionHandler.getInstence(this);
		
		//异步初始化数据,仅在第一次启动时，获取db对象有延迟
		//由于百度推送sdk初始化，将会使用db helper，所以将这块注释掉
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				SQLiteDatabase db = XunluDbHelper.getInstance(mApp).getWritableDatabase();  
//				db.close();
//			}
//		}).start();
		
		initVersion();
		initServer();
	}
	
	/**
	 * @author liYan
	 * @version  创建时间：2014-8-14 下午2:50:48
	 * @explain 初始化请求服务器参数
	 */
	public void initServer() {
		int server = PreferencesUtil.getInt(ConstantsUtil.PREFS_SERVER, -1);
		switch (server) {
		case -1:
		case ConstantsUtil.SERVER_RELEASE:
			mRootUrl = ConstantsUtil.URL_ROOT_RELEASE;
			break;
		case ConstantsUtil.SERVER_TEST:
			mRootUrl = ConstantsUtil.URL_ROOT_TEST;
			break;
		default:
			break;
		}
	}
	
	/**
	 * @author liYan
	 * @version  创建时间：2014-8-14 上午10:12:14
	 * @explain 初始化当前版本信息
	 */
	private void initVersion() {
		try {
			PackageInfo pinfo = this.getPackageManager().getPackageInfo(
					getPackageName(), PackageManager.GET_CONFIGURATIONS);
			setVersion(pinfo.versionName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getCurrentVersion() {
		return mCurrentVersion;
	}

	public void setVersion(String mVersion) {
		this.mCurrentVersion = mVersion;
	}
	
	public String getDeviceId() {
		if (mTelephonyManager == null) {
			return null;
		}
		return mTelephonyManager.getDeviceId();
	}
	
	public String getRootUrl() {
		return mRootUrl;
	}
	
	public static XunluApplication getApp() {
		return mApp;
	}
	
	// add Activity
	public void addActivity(Activity activity) {
		mList.add(activity);
	}
	
	//退出时关闭activity
	public static void exit() {
		try {
			for (Activity activity : mList) {
				if (activity != null)
					activity.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

	//内存过低时回收
	public void onLowMemory() {
		super.onLowMemory();
		System.gc();
	}

	public String getBuildType() {
		return buildType;
	}

	public void setBuildType(String buildType) {
		this.buildType = buildType;
	}
	
}
