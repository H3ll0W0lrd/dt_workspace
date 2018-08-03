package com.rtmap.experience.core.model;

import java.io.Serializable;

public class LCPoint implements Serializable {
	private int x;// 采集点X坐标
	private int y;// 采集点Y坐标
	private String name;// 点的名字
	private String buildId;
	private int floor;
	private boolean isClick;//是否点击

	public boolean isClick() {
		return isClick;
	}

	public void setClick(boolean isClick) {
		this.isClick = isClick;
	}

	public String getBuildId() {
		return buildId;
	}

	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}

	public int getFloor() {
		return floor;
	}

	public void setFloor(int floor) {
		this.floor = floor;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
