package com.rtmap.experience.core;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

public class KPAsyncTask extends AsyncTask<Object, Object, Object> {

	public KPCallBack dtBack;

	public KPAsyncTask(KPCallBack dtBack) {
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
	public void run(Object... params) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			executeOnExecutor(KPApplication.EXECUTOR, params);
		} else {
			execute(params);
		}
	}

}
