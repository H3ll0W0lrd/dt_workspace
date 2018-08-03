package com.rtm.frm.intf;

import com.rtm.frm.model.MyLocation;

/**
 * @author liyan
 * 观察者
 */
public interface LocationObserverInf {
	public void onUpdateLocation(MyLocation myLocation);
}
