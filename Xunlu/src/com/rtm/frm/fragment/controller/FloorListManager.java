package com.rtm.frm.fragment.controller;

import java.util.ArrayList;
import java.util.List;

import com.rtm.frm.XunluApplication;
import com.rtm.frm.database.DBOperation;
import com.rtm.frm.model.Floor;

public class FloorListManager extends BaseManager {
	private volatile static FloorListManager mInstance;
	
	protected FloorListManager(XunluApplication app) {
		super(app);
		initManager();
	}

	@Override
	protected void initManager() {

	}

	@Override
	protected void DestroyManager() {

	}

	public static FloorListManager getInstance() {
		FloorListManager instance;
		if (mInstance == null) {
			synchronized (FloorListManager.class) {
				if (mInstance == null) {
					instance = new FloorListManager(XunluApplication.getApp());
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
	 * @explain 根据建筑物ID获取楼层信息
	 * @param buildId
	 * @param isPrivate 是否为私有建筑
	 * @return 楼层列表
	 */
	public List<Floor> queryFloorsByBuildId(String buildId) {
		List<Floor> floors = new ArrayList<Floor>();
		
		floors = DBOperation.getInstance().queryFloorByBuildId(buildId);
		
		return floors;
	}

}
