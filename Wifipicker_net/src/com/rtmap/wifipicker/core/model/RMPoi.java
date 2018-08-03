package com.rtmap.wifipicker.core.model;

import java.io.Serializable;

public class RMPoi implements Serializable{
	private int _id;//ID
	private String buildId;//楼盘编号
	private String floor;//楼盘层数
	private float x;//采集点x轴
	private float y;//采集点y轴
	private String name;//poi命名：例饭店
	private String time;//采点时间
	private String desc;//描述或者备注
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
}
