package com.rtmap.locationcheck.core.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 指纹文件统计
 * 文件名：walk1_建筑物id_用户名_时间戳.csv，例：eg:walk1_860100010030100007_lisi_20151030T093625.csv
 * @author dingtao
 *
 */
public class LCPickList implements Serializable{
	private ArrayList<LCPick> list;

	public ArrayList<LCPick> getList() {
		return list;
	}

	public void setList(ArrayList<LCPick> list) {
		this.list = list;
	}
}
