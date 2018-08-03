package com.dingtao.shp.model;

import java.io.Serializable;

public class RMPoi implements Serializable {
	private int _id;
	private String buildId;
	private String floor;
	private float x;
	private float y;
	private String name;
	private String time;
	private String desc;

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

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTime() {
		return this.time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDesc() {
		return this.desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}