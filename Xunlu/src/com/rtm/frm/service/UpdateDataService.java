package com.rtm.frm.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.rtm.frm.database.Builds;
import com.rtm.frm.database.Floors;
import com.rtm.frm.net.PostData;
import com.rtm.frm.thread.InitBuildsThread;
import com.rtm.frm.thread.InitFavorablesThread;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.PreferencesUtil;
import com.rtm.frm.utils.XunluUtil;

public class UpdateDataService extends Service {
	
	private boolean isRunning = false;
	
	private String TAG = "UpdateDataService";
	
	private boolean initBuildFinish = false;
	
	private boolean initFavorableFinish = false;
	
	private InitBuildsThread mBuildThread;
	
	private InitFavorablesThread mFavorableThread;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			Log.e(TAG, "UpdateDataService:"+msg.what);
			switch (msg.what) {
			case ConstantsUtil.HANDLER_POST_BUILD_LIST:
				if(msg.arg1 != ConstantsUtil.STATE_NET_ERR_UNUSED) {
					String data = (String) msg.obj;
					if(XunluUtil.isEmpty(data)){
						Log.e(TAG, "data is null");
					} else {
						//启动批量插入线程
						if(mFavorableThread == null) {
							mBuildThread = new InitBuildsThread(data, mHandler, ConstantsUtil.HANDLER_THREAD_INIT_OK,Builds.TABLE_NAME,Floors.TABLE_NAME,false);
							mBuildThread.start();
						} else {
							while(mFavorableThread.isAlive()) {
								try {
									Thread.sleep(1000);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							mBuildThread = new InitBuildsThread(data, mHandler, ConstantsUtil.HANDLER_THREAD_INIT_OK,Builds.TABLE_NAME,Floors.TABLE_NAME,false);
							mBuildThread.start();
						}
					}
				} else {
					Log.e(TAG, "STATE_NET_ERR_UNUSED");
				}
				break;
			case ConstantsUtil.HANDLER_POST_FAVORABLE:
				if(msg.arg1 != ConstantsUtil.STATE_NET_ERR_UNUSED) {
					String data = (String) msg.obj;
					if(XunluUtil.isEmpty(data)){
						Log.e(TAG, "data is null");
					} else {
						//启动批量插入线程
						if(mBuildThread == null) {
							mFavorableThread = new InitFavorablesThread(data, mHandler, ConstantsUtil.HANDLER_THREAD_INIT_FAVORABLE_OK);
							mFavorableThread.start();
						} else {
							while(mBuildThread.isRunning) {
								try {
									Thread.sleep(1000);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							mFavorableThread = new InitFavorablesThread(data, mHandler, ConstantsUtil.HANDLER_THREAD_INIT_FAVORABLE_OK);
							mFavorableThread.start();
						}
					}
				} else {
					Log.e(TAG, "STATE_NET_ERR_UNUSED");
				}
				break;
			case ConstantsUtil.HANDLER_THREAD_INIT_OK:
				PreferencesUtil.putString(ConstantsUtil.PREFS_LAST_UPDATE_DATA_DATE, XunluUtil.getCurrentDate());
				initBuildFinish = true;
				break;
			case ConstantsUtil.HANDLER_THREAD_INIT_ERR:
				Log.e(TAG, "HANDLER_THREAD_INIT_BUILD_ERR");
				initBuildFinish = true;
				break;
			case ConstantsUtil.HANDLER_THREAD_INIT_FAVORABLE_OK:
				PreferencesUtil.putString(ConstantsUtil.PREFS_LAST_UPDATE_DATA_DATE, XunluUtil.getCurrentDate());
				initFavorableFinish = true;
				break;
			case ConstantsUtil.HANDLER_THREAD_INIT_FAVORABLE_ERR:
				Log.e(TAG, "HANDLER_THREAD_INIT_FAVORABLE_ERR");
				initFavorableFinish = true;
				break;
			}
				
			
			if(initBuildFinish && initFavorableFinish) {
				UpdateDataService.this.stopSelf();
				Log.d(TAG, "stopSelf");
				isRunning = false;
			}
			
			Log.d(TAG, "isRunning");
		};
	};

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(!isRunning) {
			isRunning = true;
			PostData.postInitData(mHandler, ConstantsUtil.HANDLER_POST_BUILD_LIST);
			String defaultcity = PreferencesUtil.getString("LocateCity","beijing");
			PostData.postFavorableByCity(mHandler, ConstantsUtil.HANDLER_POST_FAVORABLE,defaultcity,0,5,1);
		}
		return super.onStartCommand(intent, flags, startId);
	}

}
