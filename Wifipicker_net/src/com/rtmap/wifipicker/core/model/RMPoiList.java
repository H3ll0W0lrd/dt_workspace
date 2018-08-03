package com.rtmap.wifipicker.core.model;

import java.io.Serializable;
import java.util.ArrayList;

public class RMPoiList implements Serializable{
	private ArrayList<RMPoi> poiList;
	ArrayList<RMPoi> list ;

	public ArrayList<RMPoi> getList() {
		return list;
	}

	public void setList(ArrayList<RMPoi> list) {
		this.list = list;
	}
	public ArrayList<RMPoi> getPoiList() {
		return poiList;
	}

	public void setPoiList(ArrayList<RMPoi> poiList) {
		this.poiList = poiList;
	}
}
