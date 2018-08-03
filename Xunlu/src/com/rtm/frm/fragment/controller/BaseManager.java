package com.rtm.frm.fragment.controller;

import android.content.Context;

import com.rtm.frm.XunluApplication;
/** 
 * @author hukunge
 * @version 2014-08-18 下午19:58
 */
public abstract class BaseManager {
    protected XunluApplication mApp;
    protected Context mContext;
    
    protected BaseManager(XunluApplication app) {
	mApp = app;
	mContext = app.getApplicationContext();
    }

    protected abstract void initManager();

    protected abstract void DestroyManager();
    
    public static void destoryManager(){
    	BuildListManager.setNullInstance();
    	CouponsManager.setNullInstance();
    	FindManager.setNullInstance();
    	
    	MyFragmentManager.setNullInstance();
    	AppManager.getInstance().DestroyManager();
    	AppManager.setNullInstance();
    	CenterManager.setNullInstance();
    }
}
