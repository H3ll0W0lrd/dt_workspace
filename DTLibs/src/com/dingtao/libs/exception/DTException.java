package com.dingtao.libs.exception;

import org.apache.http.util.EncodingUtils;

import com.dingtao.libs.DTApplication;
import com.dingtao.libs.util.DTLog;

public class DTException extends Exception {

	// public static final int EXCEPTIONSIGN=1;//异常标志为1
	// public static final int CODE=2;//code码的标志为2

	private int code;
	private String msg;

	public DTException() {
	}

	/**
	 * 
	 * @param e
	 */
	public DTException(Exception e) {
		e.printStackTrace();
	}

	public DTException(int codeId) {
		setMsg(DTApplication.getInstance().getString(codeId));
	}

	public DTException(int codeId, int msgId) {
		setCode(Integer.parseInt(DTApplication.getInstance().getString(codeId)));
		setMsg(DTApplication.getInstance().getString(msgId));
	}

	public DTException(int code, String msg) {
		if ("4".equals(code)) {
			
		} else {
			setCode(code);
			String str = EncodingUtils.getString(msg.getBytes(), "utf-8");
			setMsg(str);
			DTLog.i(str);
		}
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

}
