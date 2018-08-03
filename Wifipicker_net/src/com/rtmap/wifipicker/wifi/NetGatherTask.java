package com.rtmap.wifipicker.wifi;

import android.os.AsyncTask;

public class NetGatherTask extends AsyncTask<String, Integer, String> {
	private CompleteListener mCompleteListener;

	// @Override
	// protected String doInBackground(String... params) {
	// if (params == null || params.length < 0) { return null; }
	//
	// return WebserviceCore.getNetResult(params[0], params[1]);
	// }

	@Override
	protected String doInBackground(String... params) {
		String result = "";
		if (params != null && params.length >= 2) {
			result = WebserviceCore.getNetResult(params[0], params[1]);
		}
		return result;
	}

	@Override
	protected void onPostExecute(String result) {
		if (mCompleteListener != null) {
			mCompleteListener.onComplete(result);
		}
		super.onPostExecute(result);
	}

	public NetGatherTask(CompleteListener listener) {
		mCompleteListener = listener;
	}

	public static interface CompleteListener {
		public void onComplete(String result);
	}
}
