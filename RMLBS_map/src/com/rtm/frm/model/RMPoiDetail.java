package com.rtm.frm.model;

import java.io.Serializable;

import com.rtm.common.model.POI;

/**
 * poi详情
 * @author dingtao
 *
 */
public class RMPoiDetail implements Serializable {
	private static final long serialVersionUID = 1L;
	private int error_code = -1;
	private String error_msg;
	private POI poi;

	/**
	 * 错误码
	 * @return
	 */
	public int getError_code() {
		return error_code;
	}

	public void setError_code(int error_code) {
		this.error_code = error_code;
	}

	/**
	 * 错误信息
	 * @return
	 */
	public String getError_msg() {
		return error_msg;
	}

	public void setError_msg(String error_msg) {
		this.error_msg = error_msg;
	}

	/**
	 * 得到poi信息
	 * @return
	 */
	public POI getPoi() {
		return poi;
	}

	public void setPoi(POI poi) {
		this.poi = poi;
	}
}
