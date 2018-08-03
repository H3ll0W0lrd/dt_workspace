/**
 * 地图定位共同使用的工具
 */
package com.rtm.common.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

public class RMAsyncTask extends AsyncTask<Object, Object, Object> {

	public RMCallBack dtBack;
	public static ExecutorService EXECUTOR = Executors.newCachedThreadPool();

	public RMAsyncTask(RMCallBack dtBack) {
		this.dtBack = dtBack;
	}

	@Override
	protected Object doInBackground(Object... params) {
		return dtBack.onCallBackStart(params);
	}

	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		dtBack.onCallBackFinish(result);
	}

	@SuppressLint("NewApi")
	public void runOnExecutor(boolean executeOnExecutor, Object... params) {
		if (executeOnExecutor
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			executeOnExecutor(EXECUTOR, params);
		} else {
			execute(params);
		}
	}

	@SuppressLint("NewApi")
	public void run(Object... params) {
		runOnExecutor(true, params);
	}

}
