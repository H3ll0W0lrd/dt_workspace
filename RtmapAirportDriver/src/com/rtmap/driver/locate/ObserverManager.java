package com.rtmap.driver.locate;

import com.rtm.common.model.RMLocation;

import java.util.ArrayList;

/**
 * 
* 项目名称：RtmapAirport1.0   
* 类名称：ObserverManager   
* 类描述：   观察者管理类
* 创建人：fushenghua   
* 创建时间：2015-5-18 下午4:50:15 
* 联系方式：fushenghua2012@126.com  
* 修改人：fushenghua   
* 修改时间：2015-5-18 下午4:50:15   
* 修改备注：   
* @version
 */
public class ObserverManager implements ObserverInfo{
	public static ObserverManager myObserverManager = new ObserverManager();
	public ArrayList<UpdateInfo> observerinfos = new ArrayList<UpdateInfo>();
	
	public static ObserverManager getInstance(){
		return myObserverManager;
	}
	
	@Override
	public void addObserver(UpdateInfo info) {
		observerinfos.add(info);
	}

	@Override
	public void removeObserver(UpdateInfo info) {
		observerinfos.remove(info);
	}

	@Override
	public void noticeAll(RMLocation mydata) {
		for (int i = 0; i < observerinfos.size(); i++) {
			observerinfos.get(i).onUpdateLocation(mydata);
		}
	}

}
