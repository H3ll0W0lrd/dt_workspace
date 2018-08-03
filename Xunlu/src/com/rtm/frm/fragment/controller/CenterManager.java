package com.rtm.frm.fragment.controller;

import com.rtm.frm.XunluApplication;

/**
 * @author hukunge
 * @version 2014-08-18 下午19:58
 */
public class CenterManager extends BaseManager {
	private static CenterManager mInstance;

	private CenterManager(XunluApplication app) {
		super(app);
		initManager();
	}

	public static CenterManager getInstance() {
		synchronized (CenterManager.class) {
			if (mInstance == null) {
				mInstance = new CenterManager(XunluApplication.getApp());
			}
			return mInstance;
		}
	}

	/***
	 * 在此处初始化各种manager==================
	 * **/
	@Override
	protected void initManager() {
		AppManager.getInstance();
	}

	@Override
	protected void DestroyManager() {
	}

	public static void setNullInstance() {
		mInstance = null;
	}
}
