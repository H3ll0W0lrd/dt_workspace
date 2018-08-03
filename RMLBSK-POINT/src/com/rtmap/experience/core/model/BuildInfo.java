package com.rtmap.experience.core.model;

import java.io.Serializable;
import java.util.ArrayList;

public class BuildInfo implements Serializable {

	private String buildId;
	private String name;
	private ArrayList<Floor> floorlist;
	private float lat;
	private float lng;
	private String desc;
	private String address;
	
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<Floor> getFloorlist() {
		return floorlist;
	}
	public void setFloorlist(ArrayList<Floor> floorlist) {
		this.floorlist = floorlist;
	}
	public float getLat() {
		return lat;
	}
	public void setLat(float lat) {
		this.lat = lat;
	}
	public float getLng() {
		return lng;
	}
	public void setLng(float lng) {
		this.lng = lng;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
}
