package com.dingtao.shp.model;

import java.io.Serializable;
import java.util.ArrayList;

public class RMLine implements Serializable {
	private String name;
	private String desc;
	private ArrayList<RMPoi> poiList;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return this.desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public ArrayList<RMPoi> getPoiList() {
		return this.poiList;
	}

	public void setPoiList(ArrayList<RMPoi> poiList) {
		this.poiList = poiList;
	}
}