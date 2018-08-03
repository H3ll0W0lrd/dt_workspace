package com.airport.test.model;

import java.io.Serializable;

public class MsgData implements Serializable{
	String avatar;
	String text;
	int poiNo;
	int mid;
	int gone;
	
	public MsgData(String avatar, String text, int poiNo, int mid,int gone) {
		super();
		this.avatar = avatar;
		this.text = text;
		this.poiNo = poiNo;
		this.mid = mid;
		this.gone = gone;
	}
	public MsgData() {
	}
	
	public int getGone() {
		return gone;
	}
	public void setGone(int gone) {
		this.gone = gone;
	}
	public int getMid() {
		return mid;
	}
	public void setMid(int mid) {
		this.mid = mid;
	}
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getPoiNo() {
		return poiNo;
	}
	public void setPoiNo(int poiNo) {
		this.poiNo = poiNo;
	}
}
