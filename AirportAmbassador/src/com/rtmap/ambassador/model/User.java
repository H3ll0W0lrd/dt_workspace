package com.rtmap.ambassador.model;

import java.io.Serializable;

public class User implements Serializable {
	private int id;
	private String qrCode;
	private String staffCode;
	private String staffName;
	private int login;
	
	public void setLogin(int login) {
		this.login = login;
	}
	public int getLogin() {
		return login;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getQrCode() {
		return qrCode;
	}

	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}

	public String getStaffCode() {
		return staffCode;
	}

	public void setStaffCode(String staffCode) {
		this.staffCode = staffCode;
	}

	public String getStaffName() {
		return staffName;
	}

	public void setStaffName(String staffName) {
		this.staffName = staffName;
	}
}
