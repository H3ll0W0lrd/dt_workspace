package com.rtm.location.entity;

import java.io.Serializable;
import java.util.ArrayList;

import com.rtm.common.model.BuildInfo;

/**
 * 建筑物偏转角结果集
 * @author dingtao
 *
 */
public class BuildAngleList implements Serializable {
	private static final long serialVersionUID = 1L;
	private int error_code = -1;
	private String error_msg;
	private ArrayList<BuildInfo> list;

	/**
	 * 得到错误码
	 * @return
	 */
	public int getError_code() {
		return error_code;
	}

	/**
	 * 设置错误码，默认-1
	 * @param error_code
	 */
	public void setError_code(int error_code) {
		this.error_code = error_code;
	}

	/**
	 * 得到错误信息
	 * @return
	 */
	public String getError_msg() {
		return error_msg;
	}

	/**
	 * 设置错误信息
	 * @param error_msg
	 */
	public void setError_msg(String error_msg) {
		this.error_msg = error_msg;
	}

	public ArrayList<BuildInfo> getList() {
		return list;
	}

	public void setList(ArrayList<BuildInfo> list) {
		this.list = list;
	}
}
