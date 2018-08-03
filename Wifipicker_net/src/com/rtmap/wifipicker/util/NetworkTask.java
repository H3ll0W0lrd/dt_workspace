package com.rtmap.wifipicker.util;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;

import com.rtmap.wifipicker.util.ConstantLoc.UIEventCode;
import com.rtmap.wifipicker.wifi.UIEvent;

import android.os.AsyncTask;

public class NetworkTask extends AsyncTask<String, Integer, String> {
	public static final int TASK_GET = 1;
	public static final int TASK_POST = 2;
	public static final int REQUEST_TIMES_LIMIT = 5;
	public int requestCount = 0;
	
	private ArrayList<BasicNameValuePair> mParams;
	private OnNetworkTaskPrepareListener mPrepareListener;
	private OnNetworkTaskCompleteListener mCompleteListener;
	private Network mNetwork;
	private int mType;
	private boolean mIsInterrupted;
	
	public NetworkTask(ArrayList<BasicNameValuePair> params, int type) {
		mParams = params;
		mType = type;
		mIsInterrupted = false;
	}
	
	public void setOnNetworkTaskCompleteListener(OnNetworkTaskCompleteListener listener) {
		mCompleteListener = listener;
	}
	
	public void setOnNetworkTaskPrepareListener(OnNetworkTaskPrepareListener listener) {
		mPrepareListener = listener;
	}
	
	public void addParam(String name, String value) {
		BasicNameValuePair params = new BasicNameValuePair(name, value);
		mParams.add(params);
	}

	@Override
	protected void onPreExecute() {
		if(mPrepareListener != null) {
			mPrepareListener.onNetworkTaskPrepare();
		}
		super.onPreExecute();
	}

//	@Override
//	protected String doInBackground(String... urls) {
//		if(urls == null || urls.length < 0) {
//			return null;
//		}
//		mNetwork = new Network(urls[0]);
//		mNetwork.addPostData(mParams);
//		if(mType == TASK_GET) {
//			return mNetwork.getNetString();
//		}
//		if(mType == TASK_POST) {
//			return mNetwork.postNetString();
//		}
//		
//		return null;
//	}
	
	@Override
	protected String doInBackground(String... urls) {
		if(urls == null || urls.length < 0) {
			return null;
		}
		String result = null;
		while(requestCount <= REQUEST_TIMES_LIMIT) {
			mNetwork = new Network(urls[0]);
			mNetwork.addPostData(mParams);
			if(mType == TASK_GET) {
				result = mNetwork.getNetString();
			}
			if(mType == TASK_POST) {
				result = mNetwork.postNetString();
			}
			if (result != null) {
				requestCount = 0;
				break;
			}
			requestCount++;
			if(!mNetwork.isFileSegSuccess()) {
				try {
					Thread.sleep(2 * 1000);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			if (requestCount == REQUEST_TIMES_LIMIT) {
                cancel();
                //提示网络异常
                UIEvent.getInstance().notifications(UIEventCode.NO_NET_SIGNAL_REMINDER);
                
            }
		}
		requestCount = 0;
		return result;
	}
	
	@Override
	protected void onPostExecute(String result) {
		if(mCompleteListener != null) {
			mCompleteListener.onNetworkTaskComplete(result);
		}
		super.onPostExecute(result);
	}
	
	public void cancel() {
		mIsInterrupted = true;
		if(mNetwork != null) {
			mNetwork.cancelNetConnect();
		}
	}
	
	public boolean isInterrupted() {
		return mIsInterrupted;
	}
	
	public static interface OnNetworkTaskPrepareListener {
		public void onNetworkTaskPrepare();
	}

	public static interface OnNetworkTaskCompleteListener {
		public void onNetworkTaskComplete(String result);
	}
}
