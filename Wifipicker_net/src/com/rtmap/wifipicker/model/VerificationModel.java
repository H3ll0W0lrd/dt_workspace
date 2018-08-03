package com.rtmap.wifipicker.model;

import org.w3c.dom.Element;

import com.rtmap.wifipicker.util.XmlHelper;

public class VerificationModel {
	private int mError;

	public VerificationModel(String data) throws Exception {
		Element element = XmlHelper.getRootElement(data);
		mError = Integer.valueOf(XmlHelper.getValueByTag(element, "error"));
	}
	
	public int getError() {
		return mError;
	}
}
