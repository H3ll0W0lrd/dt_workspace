package com.rtmap.locationcheck.core.model;

import java.io.Serializable;

public class Info implements Serializable {
	private String oadaily;
	private String userdaily;

	public String getOadaily() {
		return oadaily;
	}

	public void setOadaily(String oadaily) {
		this.oadaily = oadaily;
	}

	public String getUserdaily() {
		return userdaily;
	}

	public void setUserdaily(String userdaily) {
		this.userdaily = userdaily;
	}
}
