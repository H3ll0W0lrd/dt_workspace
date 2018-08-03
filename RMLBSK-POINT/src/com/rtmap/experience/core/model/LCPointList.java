package com.rtmap.experience.core.model;

import java.io.Serializable;
import java.util.ArrayList;

public class LCPointList implements Serializable{
	private ArrayList<LCPoint> list;

	public ArrayList<LCPoint> getList() {
		return list;
	}

	public void setList(ArrayList<LCPoint> list) {
		this.list = list;
	}
	
}
