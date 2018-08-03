package com.rtm.frm.intf;

import java.util.ArrayList;
import java.util.List;

import com.rtm.frm.model.MyLocation;

/**
 * @author liyan
 * 被观察者实现
 */
public class LocationImpl implements LocationInf {

	private List<LocationObserverInf> mObservers = new ArrayList<LocationObserverInf>();
	
	private static LocationImpl mLocationImpl;
	
	private LocationImpl(){}
	
	public static LocationImpl  getInstance(){
		synchronized (LocationImpl.class) {
			if (mLocationImpl == null) {
				mLocationImpl = new LocationImpl();
			}
			return mLocationImpl;
		}
	}
			
	@Override
	public void addObserver(LocationObserverInf locationObserver) {
		mObservers.add(locationObserver);
	}

	@Override
	public void removeObserver(LocationObserverInf locationObserver) {
		mObservers.remove(locationObserver);
	}

	@Override
	public void notfilyObserver(MyLocation myLocation) {
		final ArrayList<LocationObserverInf> observers = new ArrayList<LocationObserverInf>(mObservers);
		for(LocationObserverInf observer : observers) {
			observer.onUpdateLocation(myLocation);
		}
	}

}
