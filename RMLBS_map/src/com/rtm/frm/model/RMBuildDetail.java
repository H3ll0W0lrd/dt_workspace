package com.rtm.frm.model;

import java.io.Serializable;

import com.rtm.common.model.BuildInfo;

/**
 * 建筑物详情
 * 
 * @author dingtao
 *
 */
public class RMBuildDetail implements Serializable {
	private static final long serialVersionUID = 1L;
	private int error_code = -1;
	private String error_msg;
	private BuildInfo build;

	/**
	 * 得到错误码
	 * 
	 * @return
	 */
	public int getError_code() {
		return error_code;
	}

	/**
	 * 设置错误码
	 * @param error_code 默认-1
	 */
	public void setError_code(int error_code) {
		this.error_code = error_code;
	}

	/**
	 * 得到错误信息
	 * 
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

	/**
	 * 得到建筑物
	 * 
	 * @return 详情请查看BuildInfo
	 */
	public BuildInfo getBuild() {
		return build;
	}

	/**
	 * 设置建筑物信息
	 * @param build 详情请查看BuildInfo
	 */
	public void setBuild(BuildInfo build) {
		this.build = build;
	}

}
