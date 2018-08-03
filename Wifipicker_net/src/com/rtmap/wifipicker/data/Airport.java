package com.rtmap.wifipicker.data;

import java.util.ArrayList;

public class Airport {
	private String mCity;
	private String mAirport;
	private ArrayList<Terminal> mTerminals;
	
	public String getCity() {
		return mCity;
	}
	
	public void setCity(String city) {
		mCity = city;
	}
	
	public String getAirport() {
		return mAirport;
	}
	
	public void setAirport(String airport) {
		mAirport = airport;
	}
	
	public ArrayList<Terminal> getTerminals() {
		return mTerminals;
	}
	
	public void setTerminals(ArrayList<Terminal> terminals) {
		mTerminals = terminals;
	}
}
