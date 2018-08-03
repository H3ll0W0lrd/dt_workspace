package com.rtm.frm.fragment.controller;

import java.util.ArrayList;
import java.util.List;

import com.rtm.frm.XunluApplication;
import com.rtm.frm.database.DBOperation;
import com.rtm.frm.model.Build;
import com.rtm.frm.utils.ConstantsUtil;

public class BuildListManager extends BaseManager {
	private volatile static BuildListManager mInstance;
	
	public static  int BUILD_TYPE = ConstantsUtil.BUILD_TYPE_MALL;
	protected BuildListManager(XunluApplication app) {
		super(app);
		initManager();
	}

	@Override
	protected void initManager() {

	}

	@Override
	protected void DestroyManager() {

	}

	public static BuildListManager getInstance() {
		BuildListManager instance;
		if (mInstance == null) {
			synchronized (BuildListManager.class) {
				if (mInstance == null) {
					instance = new BuildListManager(XunluApplication.getApp());
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
	 * @explain 根据不同的建筑物类型,查询拥有该类型建筑的城市列表
	 * @param cityName
	 * @param buildType
	 * @return 建筑物列表
	 */
	public List<String> queryCitysByBuildType(int buildType) {
		List<String> citys = new ArrayList<String>();
		switch (buildType) {
		case ConstantsUtil.BUILD_TYPE_AIRPORT:
			citys = DBOperation.getInstance().queryAirportCitys();
			break;
		case ConstantsUtil.BUILD_TYPE_MALL:
			citys = DBOperation.getInstance().queryMallCitys();
			break;
		}
		return citys;
	}
	
    
	/**
	 * @explain 根据不同的城市名称，建筑物类型查询
	 * @param cityName
	 * @param buildType
	 * @return 建筑物列表
	 */
	public List<Build> queryBuildByCityName(String cityName,int buildType) {
		List<Build> builds = new ArrayList<Build>();
		switch (buildType) {
		case ConstantsUtil.BUILD_TYPE_AIRPORT:
			builds = DBOperation.getInstance().queryAirportByCityName(cityName);
			break;
		case ConstantsUtil.BUILD_TYPE_MALL:
			builds = DBOperation.getInstance().queryMallByCityName(cityName);
			break;
		case ConstantsUtil.BUILD_TYPE_ALL:
			builds = DBOperation.getInstance().queryBuildByCity(cityName);
			break;
		}
		return builds;
	}
	
	/**
	 * @explain 获取全部私有建筑物列表
	 * @return 建筑物列表
	 */
	public List<Build> queryPrivateBuildAll() {
		List<Build> builds = new ArrayList<Build>();
		builds = DBOperation.getInstance().queryPrivateBuildAll();
		return builds;
	}

}
