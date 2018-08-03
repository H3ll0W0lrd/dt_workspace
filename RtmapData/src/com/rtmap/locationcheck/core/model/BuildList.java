package com.rtmap.locationcheck.core.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 映射建筑物列表
 * @author dingtao
 *
 */
public class BuildList implements Serializable {
	private ArrayList<Build> maplist;

	public ArrayList<Build> getMaplist() {
		return maplist;
	}

	public void setMaplist(ArrayList<Build> maplist) {
		this.maplist = maplist;
	}
}
