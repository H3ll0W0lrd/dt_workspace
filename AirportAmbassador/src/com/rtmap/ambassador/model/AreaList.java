package com.rtmap.ambassador.model;

import java.util.ArrayList;

public class AreaList {
	private int code;
	private String msg;
	private Result rst;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Result getRst() {
		return rst;
	}

	public void setRst(Result rst) {
		this.rst = rst;
	}

	public class Result {
		private ArrayList<Area> areaList;

		public ArrayList<Area> getAreaList() {
			return areaList;
		}

		public void setAreaList(ArrayList<Area> areaList) {
			this.areaList = areaList;
		}

	}
}
