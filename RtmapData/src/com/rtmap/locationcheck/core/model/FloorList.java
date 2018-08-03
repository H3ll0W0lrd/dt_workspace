package com.rtmap.locationcheck.core.model;

import java.io.Serializable;
import java.util.ArrayList;

public class FloorList implements Serializable{
	private ArrayList<Floor> results;

	public ArrayList<Floor> getResults() {
		return results;
	}

	public void setResults(ArrayList<Floor> results) {
		this.results = results;
	}
	
}
