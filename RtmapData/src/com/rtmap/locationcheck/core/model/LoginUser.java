package com.rtmap.locationcheck.core.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 登录数据解析
 * 
 * @author dingtao
 *
 */
public class LoginUser implements Serializable {
	private int status;
	private String key;
	private String message;
	private ArrayList<Build> results;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public ArrayList<Build> getResults() {
		return results;
	}

	public void setResults(ArrayList<Build> results) {
		this.results = results;
	}
}
