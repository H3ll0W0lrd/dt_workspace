package com.rtmap.locationcheck.core.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 建筑物信息
 * 文件名：建筑物ID_info.txt,860001000017_info.txt
 * 
 *建筑物信息zip包含txt和图片jpg，图片名称格式为建筑物ID_name.jpg和建筑物ID_address.jpg
 * @author dingtao
 *
 */
public class Build implements Serializable{
	String buildId;
	String buildName;
	String address;
	String[] floor;
	ArrayList<Floor> scale;
	
	public ArrayList<Floor> getScale() {
		return scale;
	}
	public void setScale(ArrayList<Floor> scale) {
		this.scale = scale;
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
