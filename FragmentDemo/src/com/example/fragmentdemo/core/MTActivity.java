package com.example.fragmentdemo.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;

import com.example.fragmentdemo.R;
import com.example.fragmentdemo.util.MTFileUtils;
import com.example.fragmentdemo.util.MTLog;
import com.example.fragmentdemo.util.MTStringUtils;

public abstract class MTActivity extends FragmentActivity {

	public final static int CONDITION = 1;
	public static final String BEAUTY = "beauty";
	public static final String ITEM = "item";
	public static final String SCHEDUL = "schedul";
	public static final String BEAUTY_ID = "beauty_id";
	public static final String TIME = "time";
	public static final String ADDRESS = "address";
	public static final String PAGE_ID = "page_id";
	public static final String HOME_APPOINT = "home_appoint";
	public static final String CHOOSE_BEAUTY = "choose_beauty";
	public static final String ORDER = "order";
	public static final int APPOINT_ADDRESS = 1;
	public static final int ME_ADDRESS = 2;
	public static final int ADDRESS_LIST = 3;
	public static final String Baidu_Push_key = "UTYkRkiH1HMwogCUkGOOmFrt";

//	public UserInfo mUser;
	public final static int PHOTO = 0;// 相册选取
	public final static int CAMERA = 1;// 拍照
	public Dialog mLoadDialog;// 加载框

	public SharedPreferences mInfoShare, mSetShare;// 应用存储页面信息

	/** 记录处于前台的Activity */
	private static MTActivity mForegroundActivity = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MTLog.e("getTaskId = " + getTaskId());
		mInfoShare = getSharedPreferences(MTFileUtils.MT_INFO, 0);
		mSetShare = getSharedPreferences(MTFileUtils.MT_SET, 0);
//		mUser = MTSqlite.getInstance().getUser();
		MTApplication.getInstance().addActivity(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mForegroundActivity = this;
	}

	@Override
	protected void onPause() {
		super.onPause();
		mForegroundActivity = this;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	/**
	 * 设置页面名字
	 */
	public abstract String getPageName();

	public void onViewClick(View v) {

	}

	/**
	 * Tells the StatusBar whether the alarm is enabled or disabled
	 */
	private void setStatusBarIcon(boolean enabled) {
		Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
		alarmChanged.putExtra("alarmSet", enabled);
		sendBroadcast(alarmChanged);
	}

	/**
	 * 得到图片的路径
	 * 
	 * @param fileName
	 * @param requestCode
	 * @param data
	 * @return
	 */
	public String getFilePath(String fileName, int requestCode, Intent data) {
		if (requestCode == CAMERA) {
			return fileName;
		} else if (requestCode == PHOTO) {
			Uri uri = data.getData();
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
			int actual_image_column_index = actualimagecursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			actualimagecursor.moveToFirst();
			String img_path = actualimagecursor
					.getString(actual_image_column_index);
			// 4.0以上平台会自动关闭cursor,所以加上版本判断,OK
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				actualimagecursor.close();
			return img_path;
		}
		return null;
	}

	/**
	 * 验证手机号码
	 * 
	 * @param mobiles
	 * @return [0-9]{5,9}
	 */
	public boolean isMobileNO(String mobiles) {
		try {
			Pattern p = Pattern
					.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
			Matcher m = p.matcher(mobiles);
			return m.matches();
		} catch (Exception e) {
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		MTApplication.getInstance().removeActivity(this);
		super.onDestroy();
	}

	/** 获取当前处于前台的activity */
	public static MTActivity getForegroundActivity() {
		return mForegroundActivity;
	}
}
