package com.rtmap.locationcheck.core.model;

import java.io.Serializable;

public class InfoModel implements Serializable {
	private String message;
	private String status;
	private Info result;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Info getResult() {
		return result;
	}

	public void setResult(Info result) {
		this.result = result;
	}
}
