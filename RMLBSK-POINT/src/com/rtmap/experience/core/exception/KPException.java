package com.rtmap.experience.core.exception;

import org.apache.http.util.EncodingUtils;

import com.rtmap.experience.core.KPApplication;
import com.rtmap.experience.util.DTLog;

public class KPException extends Exception {

	// public static final int EXCEPTIONSIGN=1;//异常标志为1
	// public static final int CODE=2;//code码的标志为2

	private int code;
	private String msg;

	public KPException() {
	}

	/**
	 * 
	 * @param e
	 */
	public KPException(Exception e) {
		e.printStackTrace();
	}

	public KPException(int codeId) {
		setMsg(KPApplication.getInstance().getString(codeId));
	}

	public KPException(int codeId, int msgId) {
		setCode(Integer.parseInt(KPApplication.getInstance().getString(codeId)));
		setMsg(KPApplication.getInstance().getString(msgId));
	}

	public KPException(int code, String msg) {
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
