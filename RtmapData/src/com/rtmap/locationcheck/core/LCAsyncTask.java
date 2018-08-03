package com.rtmap.locationcheck.core;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

import com.rtmap.locationcheck.util.DTFileUtils;

public class LCAsyncTask extends AsyncTask<Object, Integer, Object> {

	public LCCallBack dtBack;

	public LCAsyncTask(LCCallBack dtBack) {
		this.dtBack = dtBack;
	}

	@Override
	protected Object doInBackground(Object... params) {
		if (LCApplication.getInstance().isBlack(
				LCApplication.getInstance().getShare()
						.getString(DTFileUtils.PREFS_USERNAME, ""))) {
			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return dtBack.onCallBackStart(params);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
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
			executeOnExecutor(LCApplication.EXECUTOR, params);
		} else {
			execute(params);
		}
	}

	@SuppressLint("NewApi")
	public void run(Object... params) {
		runOnExecutor(true, params);
	}

}
