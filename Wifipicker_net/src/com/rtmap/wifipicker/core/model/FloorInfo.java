package com.rtmap.wifipicker.core.model;

import java.io.Serializable;

import com.rtm.frm.model.Floor;

public class FloorInfo extends Floor {
	private String buildId;
	private String floor;
	private String name;
	private float scale;
	
	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
}
