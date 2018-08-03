package com.rtmap.wifipicker.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.Handler;

public class Network {
	
	private NetworkCore mNet = null;
	
	public Network() {
		initNetWork();
	}
	
	public Network(String url) {
		initNetWork();
		mNet.setUrl(url);
	}
	
	public Network(Context context, String url) {
		initNetWork();
		mNet.setContext(context);
		mNet.setUrl(url);
	}
	
	public Network(Context context,String url, ArrayList<BasicNameValuePair> PostData) {
		initNetWork();
		mNet.setContext(context);
		mNet.setUrl(url);
		mNet.setPostData(PostData);
	}
	
	private void initNetWork() {
		mNet = new NetworkCore();
	}
	
	public void setUrl(String url) {
		mNet.setUrl(url);
	}
	
	public String postNetString() {
		return mNet.postNetData();
	}
	
	public String getNetString() {
		return mNet.getNetString();
	}
	
	public byte[] getNetData() {
		return mNet.getNetData();
	}
	
	public boolean downloadFile(String path, Handler handler) {
		return mNet.downloadFile(path, handler);
	}
	
	public boolean isNetSuccess() {
		return mNet.isNetSuccess();
	}
	
	public void cancelNetConnect() {
		if (mNet != null) {
			mNet.cancelNetConnect();
		}
	}
	
	public void addPostData(BasicNameValuePair data) {
		mNet.addPostData(data);
	}
	
	public void addPostData(ArrayList<BasicNameValuePair> data) {
		mNet.setPostData(data);
	}
	
	public void addPostData(String key, byte[] value) {
		mNet.addPostData(key, value);
	}
	
	public void addPostData(String key, String value) {
		mNet.addPostData(key, value);
	}
	
	public int getErrorCode() {
		return mNet.getErrorCode();
	}
	
	public int getNetErrorCode() {
		return mNet.getNetErrorCode();
	}
	
	public boolean isFileSegSuccess(){
		return mNet.isFileSegSuccess();
	}
	
	public String getErrorMsg() {
		String msg = null;
		switch(mNet.getErrorCode()) {
		//TODO
		}
		
		return msg;
	}
	
	public String uploadFile(final String path) {
		byte[] data = null;
		try {
			InputStream in = FileHelper.getStreamFromFile(path);
			byte[] buffer = new byte[1024 * 5];
			int number = -1;
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024 * 5);
			while((number = in.read(buffer)) != -1) {
				outputStream.write(buffer, 0, number);
			}
			
			data = outputStream.toByteArray();
		} catch(Exception e) {
			e.printStackTrace();
		}
		addPostData("uploadedfile", data);
		return mNet.postMultiNetData();
	}
}
