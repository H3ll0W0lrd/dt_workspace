package com.rtmap.wisdom.core;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

public class DTAsyncTask extends AsyncTask<Object, Object, Object>{
	
	public DTCallBack dtBack;
	
	public DTAsyncTask(DTCallBack dtBack) {
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
			executeOnExecutor(DTApplication.EXECUTOR, params);
		} else {
			execute(params);
		}
	}

	@SuppressLint("NewApi")
	public void run(Object... params) {
		runOnExecutor(true, params);
	}
}
