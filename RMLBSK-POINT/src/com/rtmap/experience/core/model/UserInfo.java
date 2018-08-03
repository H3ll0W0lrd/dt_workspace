package com.rtmap.experience.core.model;

import java.io.Serializable;

public class UserInfo implements Serializable {
	private String key;// 认证key
	private int status;
	private String message;
	private String phone;//手机号

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
