package com.rtmap.ambassador.model;


public class LoginUser {
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
		private User staff;

		public User getStaff() {
			return staff;
		}

		public void setStaff(User staff) {
			this.staff = staff;
		}

	}
}
