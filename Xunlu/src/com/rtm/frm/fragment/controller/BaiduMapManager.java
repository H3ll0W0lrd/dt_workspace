package com.rtm.frm.fragment.controller;

import java.util.List;

import com.baidu.mapapi.model.LatLng;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.database.DBOperation;
import com.rtm.frm.model.Build;

/**
 * @author liyan 20140819
 */
public class BaiduMapManager extends BaseManager {

	private volatile static BaiduMapManager mInstance;

	protected BaiduMapManager(XunluApplication app) {
		super(app);
	}

	public static BaiduMapManager getInstance() {
		BaiduMapManager instance;
		if (mInstance == null) {
			synchronized (BuildListManager.class) {
				if (mInstance == null) {
					instance = new BaiduMapManager(XunluApplication.getApp());
					mInstance = instance;
				}
			}
		}
		return mInstance;
	}

	public static void setNullInstance() {
		mInstance = null;
	}

	@Override
	protected void initManager() {

	}

	@Override
	protected void DestroyManager() {

	}

	/**
	 * @explain 显示指定城市名称的建筑物
	 * @param cityName
	 */
	public List<Build> getBuildsByCityName(String cityName) {
		List<Build> builds = DBOperation.getInstance().queryBuildByCity(cityName);
		return builds;
	}
	 
	/**
	 * @author LiYan
	 * @date 2014-9-10 下午8:27:54  
	 * @explain 根据指定位置，筛选符合半径内的商场
	 * @return List<Build>
	 * @param latLng
	 * @param radius
	 * @return 
	 */
	public List<Build> getBuildsByLngLat(LatLng latLng,double radius) {
		List<Build> builds = DBOperation.getInstance().queryBuildsByLngLat(latLng,radius);
		return builds;
	}
}