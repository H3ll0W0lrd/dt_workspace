package com.rtmap.driver.locate;


import com.rtm.common.model.RMLocation;

public interface ObserverInfo {
	public void addObserver(UpdateInfo info);
	public void removeObserver(UpdateInfo info);
	public void noticeAll(RMLocation mydata);
}
