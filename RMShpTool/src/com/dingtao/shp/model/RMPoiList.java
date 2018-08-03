package com.dingtao.shp.model;

import java.io.Serializable;
import java.util.ArrayList;

public class RMPoiList implements Serializable {
	private ArrayList<RMPoi> poiList;

	public ArrayList<RMPoi> getPoiList() {
		return this.poiList;
	}

	public void setPoiList(ArrayList<RMPoi> poiList) {
		this.poiList = poiList;
	}
}