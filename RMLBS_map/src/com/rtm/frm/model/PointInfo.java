package com.rtm.frm.model;

import java.io.Serializable;

public class PointInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private float x;
	public float y;
	private String floor;
	private String buildId;

	public PointInfo() {

	}

	public PointInfo(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * 得到楼层
	 * 
	 * @return
	 */
	public String getFloor() {
		return floor;
	}
	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}
	public String getBuildId() {
		return buildId;
	}

	/**
	 * 设置楼层
	 * 
	 * @param floor
	 */
	public void setFloor(String floor) {
		this.floor = floor;
	}

	/**
	 * 得到x坐标
	 * 
	 * @return
	 */
	public float getX() {
		return x;
	}

	/**
	 * 得到y坐标
	 * 
	 * @return
	 */
	public float getY() {
		return y;
	}

	/**
	 * 设置x坐标
	 * 
	 * @param x
	 */
	public void setX(float x) {
		this.x = x;
	}

	/**
	 * 设置y坐标
	 * 
	 * @param y
	 */
	public void setY(float y) {
		this.y = y;
	}
}
