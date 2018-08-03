package com.rtmap.locationcheck.core.exception;

import org.apache.http.util.EncodingUtils;

import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.util.DTLog;

public class LCException extends Exception {

	// public static final int EXCEPTIONSIGN=1;//异常标志为1
	// public static final int CODE=2;//code码的标志为2

	private int code;
	private String msg;

	public LCException() {
	}

	/**
	 * 
	 * @param e
	 */
	public LCException(Exception e) {
		e.printStackTrace();
	}

	public LCException(int codeId) {
		setMsg(LCApplication.getInstance().getString(codeId));
	}

	public LCException(int codeId, int msgId) {
		setCode(Integer.parseInt(LCApplication.getInstance().getString(codeId)));
		setMsg(LCApplication.getInstance().getString(msgId));
	}

	public LCException(int code, String msg) {
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
