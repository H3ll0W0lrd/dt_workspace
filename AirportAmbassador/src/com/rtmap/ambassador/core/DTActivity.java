package com.rtmap.ambassador.core;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.google.gson.Gson;
import com.rtmap.ambassador.R;
import com.rtmap.ambassador.model.User;
import com.rtmap.ambassador.service.DownloadService;
import com.rtmap.ambassador.util.DTLog;

/**
 * 所有activity都要继承的基类
 * @author dingtao
 *
 */
@SuppressLint("NewApi")
public abstract class DTActivity extends FragmentActivity {

	public ProgressDialog mLoadDialog;// 加载框
	public User mUser;
	public Gson mGson = new Gson();

	/** 记录处于前台的Activity */
	private static DTActivity mForegroundActivity = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		DTLog.e("getTaskId = " + getTaskId());
		DTApplication.getInstance().addActivity(this);
		initLoad();
		mUser = DTSqlite.getInstance().getUser();
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

	/**
	 * 设置页面名字
	 */
	public abstract String getPageName();

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
	
	public void showUploadDialog(String content, final String url,
			final String version, final int code) {
		final Dialog uploadDialog = new Dialog(this, R.style.dialog);
		uploadDialog.setContentView(R.layout.umeng_update_dialog);
		uploadDialog.setCanceledOnTouchOutside(true);
		TextView text = (TextView) uploadDialog
				.findViewById(R.id.umeng_update_content);
		text.setText("版本："+version+"\n"+content);
		uploadDialog.findViewById(R.id.umeng_update_id_ok).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						uploadDialog.cancel();
						Intent intent = new Intent(getApplicationContext(),
								DownloadService.class);
						intent.putExtra("url", url);
						intent.putExtra("version", version);
						intent.putExtra("code", code);
						startService(intent);
					}
				});
		uploadDialog.findViewById(R.id.umeng_update_id_cancel)
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						uploadDialog.cancel();
					}
				});
		uploadDialog.show();
	}

	public void cancelLoadDialog() {
	}

	/** 获取当前处于前台的activity */
	public static DTActivity getForegroundActivity() {
		return mForegroundActivity;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		DTApplication.getInstance().removeActivity(this);
	}
}
