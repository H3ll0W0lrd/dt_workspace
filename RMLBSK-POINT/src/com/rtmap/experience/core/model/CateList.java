package com.rtmap.experience.core.model;

import java.io.Serializable;
import java.util.ArrayList;

public class CateList implements Serializable{
	private int status;
	private String message;
	private ArrayList<CateInfo> results;
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public ArrayList<CateInfo> getResults() {
		return results;
	}
	public void setResults(ArrayList<CateInfo> results) {
		this.results = results;
	}
	
}
