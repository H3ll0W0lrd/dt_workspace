package com.rtmap.wifipicker.core.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.rtm.frm.model.Floor;

public class FloorList implements Serializable{
	private ArrayList<FloorInfo> results;

	public ArrayList<FloorInfo> getResults() {
		return results;
	}

	public void setResults(ArrayList<FloorInfo> results) {
		this.results = results;
	}
	
}
