package com.rtm.frm.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 分类列表
 * @author dingtao
 *
 */
public class RMCateList implements Serializable {
	private static final long serialVersionUID = 1L;
	private int error_code = -1;
	private String error_msg;
	private ArrayList<CateInfo> catelist;

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

	/**
	 * 得到分类列表
	 * @return 类别列表，具体使用请查看CateInfo
	 */
	public ArrayList<CateInfo> getCatelist() {
		return catelist;
	}

	/**
	 * 设置分类列表
	 * @param catelist 类别列表，具体使用请查看CateInfo
	 */
	public void setCatelist(ArrayList<CateInfo> catelist) {
		this.catelist = catelist;
	}
}
