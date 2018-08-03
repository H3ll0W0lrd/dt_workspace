package com.rtm.frm.fragment.controller;

import com.rtm.frm.XunluApplication;

public class CouponsManager extends BaseManager {
	private volatile static CouponsManager mInstance;

	protected CouponsManager(XunluApplication app) {
		super(app);
		initManager();
	}

	@Override
	protected void initManager() {
	}

	@Override
	protected void DestroyManager() {
	}

	public static CouponsManager getInstance() {
		CouponsManager instance;
		if (mInstance == null) {
			synchronized (CouponsManager.class) {
				if (mInstance == null) {
					instance = new CouponsManager(XunluApplication.getApp());
					mInstance = instance;
				}
			}
		}
		return mInstance;
	}

	public static void setNullInstance() {
		mInstance = null;
	}

	/**
	 * 
	 * 方法描述 : 获取优惠信息 创建者：veekenwong 版本： v1.0 创建时间： 2014-1-21 上午11:30:54 void
	 */
	public void fetchCoupons(boolean isAnimationDisplay) {

	}
}
