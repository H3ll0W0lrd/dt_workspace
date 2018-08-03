package com.dingtao.shp.model;

import java.io.Serializable;

public class RMPoint implements Serializable {
	private int _id;
	private String buildId;
	private String floor;
	private float x;
	private float y;
	private String type;
	private String mapPath;
	private String wifi;
	private long time;

	public int get_id() {
		return this._id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getBuildId() {
		return this.buildId;
	}

	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}

	public String getFloor() {
		return this.floor;
	}

	public void setFloor(String floor) {
		this.floor = floor;
	}

	public float getX() {
		return this.x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return this.y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMapPath() {
		return this.mapPath;
	}

	public void setMapPath(String mapPath) {
		this.mapPath = mapPath;
	}

	public String getWifi() {
		return this.wifi;
	}

	public void setWifi(String wifi) {
		this.wifi = wifi;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(long time) {
		this.time = time;
	}
}