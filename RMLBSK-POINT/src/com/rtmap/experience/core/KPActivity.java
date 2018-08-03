package com.rtmap.experience.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;

import com.rtmap.experience.core.model.UserInfo;
import com.rtmap.experience.util.DTFileUtils;
import com.rtmap.experience.util.DTLog;

public class KPActivity extends FragmentActivity {

	public UserInfo mUser;
	public final static int PHOTO = 0;// 相册选取
	public final static int CAMERA = 1;// 拍照
	public ProgressDialog mLoadDialog;// 加载框
	public float adjustLength = 5;// 调整距离为1像素

	/** 记录处于前台的Activity */
	private static KPActivity mForegroundActivity = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		DTLog.e("getTaskId = " + getTaskId());
		initLoad();
		adjustLength = Float.parseFloat(KPApplication.getInstance().getShare()
				.getString("step_adjust", "5"));
		mUser = new UserInfo();
		mUser.setKey(KPApplication.getInstance().getShare()
				.getString(DTFileUtils.PREFS_TOKEN, null));
		mUser.setPhone(KPApplication.getInstance().getShare()
				.getString(DTFileUtils.PHONE, ""));
		KPApplication.getInstance().addActivity(this);
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
		mLoadDialog.dismiss();
	}

	public void onViewClick(View v) {

	}

	/**
	 * 初始化加载框
	 */
	private void initLoad() {
		mLoadDialog = new ProgressDialog(this);// 加载框
		mLoadDialog.setCanceledOnTouchOutside(false);
		mLoadDialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (mLoadDialog.isShowing() && keyCode == KeyEvent.KEYCODE_BACK) {
					cancelLoadDialog();
					mLoadDialog.cancel();
				}
				return false;
			}
		});
	}

	public void cancelLoadDialog() {
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

	/** 获取当前处于前台的activity */
	public static KPActivity getForegroundActivity() {
		return mForegroundActivity;
	}

	@Override
	protected void onDestroy() {
		KPApplication.getInstance().removeActivity(this);
		super.onDestroy();
	}

}
