package com.rtm.frm.fragment.controller;

import com.rtm.frm.XunluApplication;

/**
 * @author hukunge
 * @version 2014-08-18 下午19:58
 */
public class AppManager extends BaseManager {
	private static AppManager mInstance;

	private AppManager(XunluApplication app) {
		super(app);
		initManager();
	}

	public static AppManager getInstance() {
		synchronized (AppManager.class) {
			if (mInstance == null) {
				mInstance = new AppManager(XunluApplication.getApp());
			}
			return mInstance;
		}
	}

	@Override
	protected void initManager() {
		synchronized (AppManager.this) {
		}

	}

	@Override
	protected void DestroyManager() {
	}

	public static void setNullInstance() {
		mInstance = null;
	}

}
