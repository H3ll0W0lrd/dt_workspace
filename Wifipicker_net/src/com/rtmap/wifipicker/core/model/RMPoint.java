package com.rtmap.wifipicker.core.model;

import java.io.Serializable;

public class RMPoint implements Serializable{
	
	private int _id;//ID
	private String buildId;//楼盘编码2398394539475
	private String floor;//楼层号F2
	private float x;//采集点X坐标
	private float y;//采集点Y坐标
	private String type;//类型Constants.TYPE_WIFI_WALK
	private String mapPath;//地图名字   楼盘编码-楼层号:2937239847-F2
	private String wifi;//详细的ap热点信息
	private long time;//采点时间
	
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
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
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMapPath() {
		return mapPath;
	}
	public void setMapPath(String mapPath) {
		this.mapPath = mapPath;
	}
	public String getWifi() {
		return wifi;
	}
	public void setWifi(String wifi) {
		this.wifi = wifi;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
}
