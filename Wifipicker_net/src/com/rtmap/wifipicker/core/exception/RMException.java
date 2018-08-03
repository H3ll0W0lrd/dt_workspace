package com.rtmap.wifipicker.core.exception;

import org.apache.http.util.EncodingUtils;

import com.rtmap.wifipicker.core.WPApplication;
import com.rtmap.wifipicker.util.DTLog;

public class RMException extends Exception {

	// public static final int EXCEPTIONSIGN=1;//异常标志为1
	// public static final int CODE=2;//code码的标志为2

	private int code;
	private String msg;

	public RMException() {
	}

	/**
	 * 
	 * @param e
	 */
	public RMException(Exception e) {
		e.printStackTrace();
	}

	public RMException(int codeId) {
		setMsg(WPApplication.getInstance().getString(codeId));
	}

	public RMException(int codeId, int msgId) {
		setCode(Integer.parseInt(WPApplication.getInstance().getString(codeId)));
		setMsg(WPApplication.getInstance().getString(msgId));
	}

	public RMException(int code, String msg) {
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
