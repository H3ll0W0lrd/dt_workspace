package com.rtmap.wifipicker;

public class BuildSession {
	private static BuildSession instance;

	private String mBuildId;
	private String mFloor;// 楼层f5

	public static BuildSession getInstance() {
		if (instance == null) {
			instance = new BuildSession();
		}

		return instance;
	}

	public void setBuildId(String id) {
		mBuildId = id;
	}

	public void setFloor(String floor) {
		mFloor = floor;
	}

	public String getBuildId() {
		return mBuildId;
	}

	public String getFloor() {
		return mFloor;
	}
}
