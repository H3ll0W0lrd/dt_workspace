package com.rtmap.experience.core.model;

import java.io.Serializable;

public class Floor implements Serializable {
	private String floor;
	private String buildId;
	private int scale;
	private String hasMap;
	private String name;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHasMap() {
		return hasMap;
	}
	public void setHasMap(String hasMap) {
		this.hasMap = hasMap;
	}
	public int getScale() {
		return scale;
	}
	public void setScale(int scale) {
		this.scale = scale;
	}
	public String getFloor() {
		return floor;
	}
	public void setFloor(String floor) {
		this.floor = floor;
	}
	public String getBuildId() {
		return buildId;
	}
	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}
	
}
