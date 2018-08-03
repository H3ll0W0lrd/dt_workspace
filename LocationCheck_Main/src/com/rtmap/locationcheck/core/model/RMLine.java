package com.rtmap.locationcheck.core.model;

import java.io.Serializable;
import java.util.ArrayList;

public class RMLine implements Serializable{
	private String name;//名字
	private String desc;//类别,描述
	private ArrayList<RMPoi> poiList;//点集合
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public ArrayList<RMPoi> getPoiList() {
		return poiList;
	}
	public void setPoiList(ArrayList<RMPoi> poiList) {
		this.poiList = poiList;
	}
}
