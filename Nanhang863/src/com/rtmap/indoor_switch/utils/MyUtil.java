package com.rtmap.indoor_switch.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.rtmap.indoor_switch.manager.AppContext;

import java.lang.reflect.Field;
import java.util.List;

public class MyUtil {

	/**
	 * 隐藏软键盘
	 */
	public static void hideKeyboard(Activity activity) {
		InputMethodManager manager = (InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (activity.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (activity.getCurrentFocus() != null)
				manager.hideSoftInputFromWindow(activity.getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/**
	 * 
	 * 方法描述 : 获取状态栏的高度
	 * 
	 * @param context
	 * @return int
	 */
	public static int getStatusBarHeight(Context context) {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, statusBarHeight = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = context.getResources().getDimensionPixelSize(x);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return statusBarHeight;
	}

	/**
	 * Drawable转化为Bitmap
	 */
	public static Bitmap drawableToBitmap(Drawable drawable,int width,int height) {
		if(width == 0) {
			width = drawable.getIntrinsicWidth();
		}
		if(height == 0) {
			height = drawable.getIntrinsicHeight();
		}
		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;

	}

	public static int dip2px(float dipValue) {
		final float scale = AppContext.instance().getResources()
				.getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(float px) {
		final float scale = AppContext.instance().getResources()
				.getDisplayMetrics().density;
		return (int) (px / scale + 0.5f);
	}

	public static String getTopActivity(Activity context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

		if (runningTaskInfos != null) {
			String toAcitivityName = (runningTaskInfos.get(0).topActivity)
					.getClassName().toString();
			return toAcitivityName;
		} else {
			return null;
		}

	}

}
