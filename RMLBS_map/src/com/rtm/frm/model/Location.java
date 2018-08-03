package com.rtm.frm.model;

import java.io.Serializable;

import com.rtm.common.utils.RMStringUtils;

public class Location implements Serializable {
	private static final long serialVersionUID = 1L;
	private float mX;
	private float mY;
	private String mFloor;
	private String mBuildId;

	/**
	 * 构造方法
	 * 
	 * @param x
	 *            横向坐标，单位：米
	 * @param y
	 *            纵向坐标，单位：米
	 */
	public Location(float x, float y) {
		mX = x;
		mY = Math.abs(y);
	}

	/**
	 * 构造方法
	 * 
	 * @param x
	 *            横向坐标，单位：米
	 * @param y
	 *            纵向坐标，单位：米
	 * @param floor
	 *            楼层，例：F1
	 */
	public Location(float x, float y, String floor) {
		mX = x;
		mY = y;
		mFloor = floor;
	}

	/**
	 * 构造方法
	 * 
	 * @param x
	 *            横向坐标，单位：米
	 * @param y
	 *            纵向坐标，单位：米
	 * @param floor
	 *            楼层，例：20010
	 */
	public Location(float x, float y, int floor) {
		mX = x;
		mY = y;
		mFloor = RMStringUtils.floorTransform(floor);
	}

	/**
	 * 构造方法，可用于定位结果封装，用于地图显示定位点
	 * 
	 * @param x
	 *            横向坐标，单位：米
	 * @param y
	 *            纵向坐标，单位：米
	 * @param floor
	 *            楼层，例：F1
	 * @param buildid
	 *            建筑物ID
	 */
	public Location(float x, float y, String floor, String buildid) {
		mX = x;
		mY = y;
		mFloor = floor;
		mBuildId = buildid;
	}

	/**
	 * 构造方法，可用于定位结果封装，用于地图显示定位点
	 * 
	 * @param x
	 *            横向坐标，单位：米
	 * @param y
	 *            纵向坐标，单位：米
	 * @param floor
	 *            楼层，例：20010
	 * @param buildid
	 *            建筑物ID
	 */
	public Location(float x, float y, int floor, String buildid) {
		mX = x;
		mY = y;
		mFloor = RMStringUtils.floorTransform(floor);
		mBuildId = buildid;
	}

	/**
	 * 得到x坐标
	 * 
	 * @return
	 */
	public float getX() {
		return Math.abs(mX);
	}

	/**
	 * 得到y坐标
	 * 
	 * @return
	 */
	public float getY() {
		return Math.abs(mY);
	}

	/**
	 * 设置x坐标
	 * 
	 * @param x
	 */
	public void setX(float x) {
		mX = x;
	}

	/**
	 * 设置y坐标
	 * 
	 * @param y
	 */
	public void setY(float y) {
		mY = y;
	}

	/**
	 * 得到旋转角度
	 * 
	 * @param location
	 * @return
	 */
	public int getRotate(Location location) {
		return (int) (Math.atan((location.mY - mY) / (location.mX - mX))
				/ Math.PI * 180);
	}

	/**
	 * 设置楼层
	 * 
	 * @param floor
	 */
	public void setFloor(String floor) {
		mFloor = floor;
		// mFloorInt=Utils.FloorString2Int(floor);
	}

	/**
	 * 获取位置楼层
	 * 
	 * @return
	 */
	public String getFloor() {
		return mFloor;
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Location) {
			Location location = (Location) o;
			// Log.log("location", String.format("%f %f %f %f", location.mX,
			// location.mY, mX, mY));
			if (location.mX == mX && location.mY == mY) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 位置数据是否为空
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return ((mX == 0) || (mY == 0) || RMStringUtils.isEmpty(mFloor));
	}

	/**
	 * 设置建筑物ID
	 * 
	 * @param id
	 */
	public void setBuildId(String id) {
		mBuildId = id;
	}

	/**
	 * 得到建筑物ID
	 * 
	 * @return
	 */
	public String getBuildId() {
		return mBuildId;
	}

}
