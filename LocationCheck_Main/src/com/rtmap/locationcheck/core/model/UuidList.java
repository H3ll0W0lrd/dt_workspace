package com.rtmap.locationcheck.core.model;

import java.io.Serializable;
import java.util.ArrayList;

public class UuidList implements Serializable{
	ArrayList<String> list;

	public ArrayList<String> getList() {
		return list;
	}

	public void setList(ArrayList<String> list) {
		this.list = list;
	}
	
}
