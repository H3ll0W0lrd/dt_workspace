package com.rtm.frm.model;


/**
 * @author liyan
 * 我当前的位置
 */
public class MyLocation {
	private static MyLocation mMyLocation;
	private String buildId;
	private String floor;
	private float x;
	private float y;
	private String buildName;
	private boolean isPrivate = false;
	private Build mBuild;
	private int InOutDoorFlg;
	private double gpsLat;
	private double gpsLng;
	
	public synchronized static MyLocation getInstance() {
		if(mMyLocation == null) {
			mMyLocation = new MyLocation();
		}
		return mMyLocation;
	}

	public static MyLocation getMyLocation() {
		return mMyLocation;
	}

	public static void setMyLocation(MyLocation mMyLocation) {
		MyLocation.mMyLocation = mMyLocation;
	}

	public String getBuildId() {
		return buildId;
	}

	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}

	public String getFloor() {
		return floor;
	}

	public void setFloor(String floor) {
		this.floor = floor;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public String getBuildName() {
		return buildName;
	}

	public void setBuildName(String buildName) {
		this.buildName = buildName;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public Build getBuild() {
		return mBuild;
	}

	public void setBuild(Build mBuild) {
		this.mBuild = mBuild;
	}

	public int getInOutDoorFlg() {
		return InOutDoorFlg;
	}

	public void setInOutDoorFlg(int inOutDoorFlg) {
		InOutDoorFlg = inOutDoorFlg;
	}

	public double getGpsLat() {
		return gpsLat;
	}

	public void setGpsLat(double gpsLat) {
		this.gpsLat = gpsLat;
	}

	public double getGpsLng() {
		return gpsLng;
	}

	public void setGpsLng(double gpsLng) {
		this.gpsLng = gpsLng;
	}
}
