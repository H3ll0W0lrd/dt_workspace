package com.dingtao.libs.model;

import java.io.Serializable;

public class DTExceptionModel implements Serializable {
	private String time;// 时间戳
	private String device;// 机器型号
	private String os;// 系统版本号
	private String version;// 应用版本号
	private String message;// 错误信息
	private int user_id;// 用户账号
	private String client;// 客户端类型

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

}
