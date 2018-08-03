package com.rtmap.locationcheck.core.model;

import java.io.Serializable;

public class LCPick implements Serializable{
	//
	private String time;//时间
	private String buildId;//建筑物ID
	private String floor;//楼层，例：F2
	private float x;
	private float y;
	private double velocity;//速度:m/s
	private double used_time;//用时
	private double distance;//距离
	
	public LCPick(String time, String buildId, String floor, float x, float y,
			double velocity, double used_time, double distance) {
		super();
		this.time = time;
		this.buildId = buildId;
		this.floor = floor;
		this.x = x;
		this.y = y;
		this.velocity = velocity;
		this.used_time = used_time;
		this.distance = distance;
	}

	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
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
	public double getVelocity() {
		return velocity;
	}
	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}
	public double getUsed_time() {
		return used_time;
	}
	public void setUsed_time(double used_time) {
		this.used_time = used_time;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	
}
