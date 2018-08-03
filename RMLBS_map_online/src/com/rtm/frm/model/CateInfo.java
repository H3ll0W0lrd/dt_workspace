package com.rtm.frm.model;

import java.io.Serializable;

/**
 * 分类信息类
 * 
 * @author dingtao
 *
 */
public class CateInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private String name;

	/**
	 * 得到分类ID
	 * 
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * 设置分类ID
	 * 
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * 得到分类name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置分类name
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

}
