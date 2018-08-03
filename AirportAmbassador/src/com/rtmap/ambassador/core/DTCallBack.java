package com.rtmap.ambassador.core;

public interface DTCallBack {
	Object onCallBackStart(Object...obj);
	void onCallBackFinish(Object obj);
}