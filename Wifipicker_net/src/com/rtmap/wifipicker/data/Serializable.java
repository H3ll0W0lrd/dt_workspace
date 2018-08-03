/**
 * 项目名称:智能泊车系统(Smart Parker）
 * 创建时间:2011.11.1
 * 版本:1.0.0
 * Copyright (c),RTM
 */
package com.rtmap.wifipicker.data;

/**
 *********************************************************
 * @文件:RTMSerializable.java
 * @说明: 
 * @创建日期:2011-11-15
 * @作者:林巍凌
 * @版权:RTM
 * @版本:1.0.0
 * @标签:RTMSerializable
 * @最后更新时间:2011-12-1
 * @最后更新作者:林巍凌
 *********************************************************
 */
public interface Serializable {
	public int deserialize(byte[] b, int from);
	public int getByteSize();
}
