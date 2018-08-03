package com.rtmap.driver.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class DialogUtil {

	public static Dialog showDialog(Context context, String title, String msg) {

		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		// builder.create().show();

		return builder.create();
	}

	/**
	 * 
	 * @param context
	 * @param title
	 * @param msg
	 * @param ok
	 * @param cancel
	 * @param callback
	 * @return
	 */
	public static Dialog showDialog(Context context, String title, String msg, String ok, String cancel,
			final DialogCallBack callback) {

		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton(ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				if (callback != null) {
					callback.ok();
				}
			}
		});
		builder.setNegativeButton(cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				if (callback != null) {
					callback.cancel();
				}
			}
		});

		return builder.create();
	}

	public static interface DialogCallBack {
		public void ok();

		public void cancel();
	}
	
	/***
	 * @param context
	 * @param cancelable
	 * @param rCancel
	 * @return
	 */
	public static Dialog getLoadingDialog(Context context, boolean cancelable, Runnable rCancel) {
		ProgressDialog dialog = new ProgressDialog(context);
		dialog.setTitle("Loading");
		dialog.setMessage("Please wait...");
		// dialog.show();

		return dialog;
	}

}
