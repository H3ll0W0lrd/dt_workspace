package com.rtmap.wisdom.model;

import java.io.Serializable;
import java.util.ArrayList;

public class UIBuildList implements Serializable {
	private ArrayList<UIBuildInfo> list;

	public void setList(ArrayList<UIBuildInfo> list) {
		this.list = list;
	}

	public ArrayList<UIBuildInfo> getList() {
		return list;
	}
}
