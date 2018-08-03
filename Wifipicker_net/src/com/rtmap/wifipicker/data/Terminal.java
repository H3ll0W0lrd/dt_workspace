package com.rtmap.wifipicker.data;

import java.io.Serializable;

public class Terminal implements Serializable {
	private static final long serialVersionUID = -4726951099929458640L;
	
	private String mId;
	private String mAirportName;
	private String mName;
	private String mTag;
	private String mShortName;
	private boolean mRelease;
	
	public Terminal(String id, String name) {
		mId = id;
		mName = name;
		mTag = null;
	}
	
	public String getId() {
		return mId;
	}
	
	public void setName(String name) {
		mName = name;
	}
	
	public void setTag(String tag) {
		mTag = tag;
	}
	
	public String getName() {
		return mName;
	}
	
	public String getTag() {
		return mTag;
	}
	
	public void setShortName(String name) {
		mShortName = name;
	}
	
	public String getShortName() {
		return mShortName;
	}
	
	public void setRelease(boolean release) {
		mRelease = release;
	}
	
	public boolean getRelease() {
		return mRelease;
	}
	
	public void setAirportName(String name) {
		mAirportName = name;
	}
	
	public String getAirportName() {
		return mAirportName;
	}
}
