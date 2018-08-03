package com.rtm.frm.model;

import java.io.Serializable;

public class RMLicense implements Serializable {
	private static final long serialVersionUID = 1L;
	private int error_code = -1;
	private String error_msg;
	private String time_limit;
	private String auth_key;

	/**
	 * 错误码
	 * 
	 * @return
	 */
	public int getError_code() {
		return error_code;
	}

	/**
	 * 设置错误码，默认-1
	 * 
	 * @param error_code
	 */
	public void setError_code(int error_code) {
		this.error_code = error_code;
	}

	/**
	 * 错误信息
	 * 
	 * @return
	 */
	public String getError_msg() {
		return error_msg;
	}

	/**
	 * 设置错误信息
	 * 
	 * @param error_msg
	 */
	public void setError_msg(String error_msg) {
		this.error_msg = error_msg;
	}

	/**
	 * key有效期
	 * 
	 * @return
	 */
	public String getTime_limit() {
		return time_limit;
	}

	/**
	 * 设置key有效期
	 * 
	 * @param time_limit
	 */
	public void setTime_limit(String time_limit) {
		this.time_limit = time_limit;
	}

	/**
	 * 得到认证key
	 * 
	 * @return
	 */
	public String getAuth_key() {
		return auth_key;
	}

	/**
	 * 设置认证key
	 * 
	 * @param auth_key
	 */
	public void setAuth_key(String auth_key) {
		this.auth_key = auth_key;
	}

}
