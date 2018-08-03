package com.rtmap.locationcheck.core;

public interface LCCallBack {
	Object onCallBackStart(Object...obj);
	void onCallBackFinish(Object obj);
}