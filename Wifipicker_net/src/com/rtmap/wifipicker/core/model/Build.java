package com.rtmap.wifipicker.core.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Build implements Serializable{
	String buildId;
	String buildName;
	String[] floor;
	public String getBuildId() {
		return buildId;
	}
	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}
	public String getBuildName() {
		return buildName;
	}
	public void setBuildName(String buildName) {
		this.buildName = buildName;
	}
	public String[] getFloor() {
		return floor;
	}
	public void setFloor(String[] floor) {
		this.floor = floor;
	}
}
