package com.rtm.frm.model;

import java.io.Serializable;

import com.rtm.frm.model.Build;
import com.rtm.frm.utils.XunluUtil;

public class XunluLocation implements Serializable {
	private static final long serialVersionUID = 7213302588162598947L;

	private float mX;
	private float mY;
	private double mLatitude;
	private double mLongitude;
	private String mAddress;
	private String mFloor;
	private Build mBuild;

	public XunluLocation() {
		mBuild = new Build();
	}

	public XunluLocation(float x, float y) {
		mX = x;
		mY = y;
		mBuild = new Build();
	}

	public float getX() {
		return Math.abs(mX);
	}

	public float getY() {
		return Math.abs(mY);
	}

	public void setX(float x) {
		mX = x;
	}

	public void setY(float y) {
		mY = y;
	}

	public void setFloor(String floor) {
		mFloor = floor;
	}

	public String getFloor() {
		return mFloor;
	}

	public void setBuild(Build build) {
		mBuild = build;
	}

	public Build getBuild() {
		return mBuild;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(double latitude) {
		mLatitude = latitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setLongitude(double longitude) {
		mLongitude = longitude;
	}

	public String getAddress() {
		return mAddress;
	}

	public void setAddress(String address) {
		mAddress = address;
	}

	public boolean isEmpty() {
		return ((mX == 0) || (mY == 0) || XunluUtil.isEmpty(mFloor));
	}
}
