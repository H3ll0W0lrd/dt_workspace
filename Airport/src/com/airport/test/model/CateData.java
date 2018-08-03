package com.airport.test.model;

import java.io.Serializable;

public class CateData implements Serializable {
	private String name;
	private int iconid;
	private boolean check;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIconid() {
		return iconid;
	}

	public void setIconid(int iconid) {
		this.iconid = iconid;
	}

	public boolean isCheck() {
		return check;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}
}
