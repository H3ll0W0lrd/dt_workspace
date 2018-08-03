package com.rtmap.locationcheck.core.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Build implements Serializable{
	String buildId;
	String buildName;
	String[] floor;
	String uuid;
	String major;
	String address;
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getMajor() {
		return major;
	}
	public void setMajor(String major) {
		this.major = major;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
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
