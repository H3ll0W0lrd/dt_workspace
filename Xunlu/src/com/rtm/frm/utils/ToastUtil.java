package com.rtm.frm.utils;

import android.view.Gravity;
import android.widget.Toast;

import com.rtm.frm.XunluApplication;

public class ToastUtil {
	/**
	 * @author liYan
	 * @version 创建时间：2014-8-14 下午3:38:05
	 * @explain toast消息提示
	 * @param context
	 * @param msg
	 * @param isShort
	 */
	public static void showToast(String msg, boolean isShort) {
		if (isShort) {
			Toast.makeText(XunluApplication.mApp, msg, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		Toast.makeText(XunluApplication.mApp, msg, Toast.LENGTH_LONG).show();
	}

	/**
	 * @author liYan
	 * @version 创建时间：2014-8-14 下午3:38:05
	 * @explain toast消息提示
	 * @param context
	 * @param msgId
	 * @param isShort
	 */
	public static void showToast(int msgId, boolean isShort) {
		if (isShort) {
			Toast.makeText(XunluApplication.mApp, msgId, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		Toast.makeText(XunluApplication.mApp, msgId, Toast.LENGTH_LONG).show();
	}

	/**
	 * @param message
	 *            int型
	 * */
	public static void shortToast(int msg) {
		Toast.makeText(XunluApplication.mApp,
				XunluApplication.mApp.getResources().getString(msg), Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * @param message
	 *            String型
	 * */
	public static void shortToast(String msg) {
		Toast.makeText(XunluApplication.mApp, msg, Toast.LENGTH_SHORT).show();
	}

	/**
	 * @param message
	 *            int型
	 * */
	public static void longToast(int msg) {
		Toast.makeText(XunluApplication.mApp,
				XunluApplication.mApp.getString(msg), Toast.LENGTH_LONG).show();
	}

	/**
	 * @param message
	 *            String型
	 * */
	public static void longToast(String msg) {
		Toast.makeText(XunluApplication.mApp, msg, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * @param message
	 *            int型
	 * */
	public static void shortToastCenter(int msg) {
		Toast toast = Toast.makeText(XunluApplication.mApp,
				XunluApplication.mApp.getString(msg), Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
	/**
	 * @param message
	 *            String型
	 * */
	public static void shortToastCenter(String msg) {
		Toast toast = Toast.makeText(XunluApplication.mApp, msg, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

}
