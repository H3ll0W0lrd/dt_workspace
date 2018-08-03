package com.rtmap.locationcheck.core.model;

import java.io.Serializable;
import java.util.ArrayList;

public class BeaconList implements Serializable{
	ArrayList<BeaconInfo> list ;

	public ArrayList<BeaconInfo> getList() {
		return list;
	}

	public void setList(ArrayList<BeaconInfo> list) {
		this.list = list;
	}
}
