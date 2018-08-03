package com.rtm.frm.intf;

import com.rtm.frm.model.MyLocation;

public interface LocationInf {
	public void addObserver(LocationObserverInf locationObserver);
	public void removeObserver(LocationObserverInf locationObserver);
	public void notfilyObserver(MyLocation myLocation);
}
