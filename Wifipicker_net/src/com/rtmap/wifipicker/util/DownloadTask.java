package com.rtmap.wifipicker.util;

import java.io.File;

import android.os.AsyncTask;

public class DownloadTask extends AsyncTask<String, Integer, Boolean> {
	private Network mNetwork;
	private OnDownloadTaskPrepareListener mPrepareListener;
	private OnDownloadTaskCompleteListener mCompleteListener;

	public void setOnDownloadTaskCompleteListener(
			OnDownloadTaskCompleteListener listener) {
		mCompleteListener = listener;
	}

	public void setOnImageTaskPrepareListener(
			OnDownloadTaskPrepareListener listener) {
		mPrepareListener = listener;
	}

	@Override
	protected void onPreExecute() {
		if (mPrepareListener != null) {
			mPrepareListener.onDownloadTaskPrepare();
		}
		super.onPreExecute();
	}

	/**
	 * 0是url下载地址 1是本地保存地址
	 */
	@Override
	protected Boolean doInBackground(String... urls) {
		if (urls == null || urls.length < 0) {
			return false;
		}

		mNetwork = new Network(urls[0]);

		return mNetwork.downloadFile(urls[1], null);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (mCompleteListener != null) {
			mCompleteListener.onDownloadTaskComplete(result);
		}
		super.onPostExecute(result);
	}

	public void cancel() {
		if (mNetwork != null) {
			mNetwork.cancelNetConnect();
		}
	}

	public static interface OnDownloadTaskPrepareListener {
		public void onDownloadTaskPrepare();
	}

	public static interface OnDownloadTaskCompleteListener {
		public void onDownloadTaskComplete(Boolean result);
	}
}
