package com.rtmap.locationcheck.core;

import com.rtmap.locationcheck.util.DTFileUtils;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

public class LCAsyncTask extends AsyncTask<Object, Object, Object> {

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
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		dtBack.onCallBackFinish(result);
	}

	@SuppressLint("NewApi")
	public void run(Object... params) {
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//			executeOnExecutor(LCApplication.EXECUTOR, params);
//		} else {
			execute(params);
//		}
	}

}
