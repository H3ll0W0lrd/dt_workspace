package com.rtmap.wifipicker.util;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.widget.Toast;

import com.rtmap.wifipicker.R;
import com.rtmap.wifipicker.core.WPApplication;

public class NetworkService {
	private static final String KEY_USERNAME = "user";
	private static final String KEY_PASSWORD = "password";
	private static final String KEY_VERSION = "version";
	private static final String KEY_VERIFICATION = "verification";
	
	public static boolean checkNetInfo() {
		ConnectivityManager conMan = (ConnectivityManager) WPApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
				.getState();
		State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();
		if (mobile == State.CONNECTED || mobile == State.CONNECTING) {
			return true;
		}
		if (wifi == State.CONNECTED || wifi == State.CONNECTING) {
			return true;
		}
		DTUIUtils.showToastSafe(R.string.net_error);
		return false;
	}
	
	private static NetworkTask createNetworkTask(ArrayList<BasicNameValuePair> params, String url,//http://open2.rtmap.net/application/upload/login_ex.php
			NetworkTask.OnNetworkTaskPrepareListener prepareListener, 
			NetworkTask.OnNetworkTaskCompleteListener completeListener) {
		BasicNameValuePair param = new BasicNameValuePair(KEY_VERSION, Constants.VERSION);
		params.add(param);
		NetworkTask task = new NetworkTask(params, NetworkTask.TASK_POST);
		task.setOnNetworkTaskPrepareListener(prepareListener);
		task.setOnNetworkTaskCompleteListener(completeListener);
		task.execute(url);
		return task;
	}
	
	/**
	 * 下载文件
	 * @param url 网络地址
	 * @param path 本地路径
	 * @param prepareListener
	 * @param completeListener
	 * @return
	 */
	public static DownloadTask downloadFile(String url,String path, DownloadTask.OnDownloadTaskPrepareListener prepareListener,
			DownloadTask.OnDownloadTaskCompleteListener completeListener) {
		DownloadTask task = new DownloadTask();
		task.setOnImageTaskPrepareListener(prepareListener);
		task.setOnDownloadTaskCompleteListener(completeListener);
		task.execute(url,path);
		return task;
	}
}
