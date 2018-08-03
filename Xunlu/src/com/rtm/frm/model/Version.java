package com.rtm.frm.model;

import org.w3c.dom.Element;

import com.rtm.frm.utils.XmlUtil;

public class Version {
	private int mVersionData;
	private int mVersionMap;
	private int mRemindNew;
	private int mForceUpdate;
	private String mUrl;
	private String mClientVersion;
	private String mNewFeatures;

	public Version(String data) {
		try{
			Element element = XmlUtil.getRootElement(data);
			int versionData = XmlUtil.getIntValueByTag(element, "version_data");
			int versionMap = XmlUtil.getIntValueByTag(element, "version_map");
			int remind = XmlUtil.getIntValueByTag(element, "prompt");
			int force = XmlUtil.getIntValueByTag(element, "forced");
			String url = XmlUtil.getValueByTag(element, "url_apk");
			String clientVersion = XmlUtil.getValueByTag(element, "currentversion");
			String features = XmlUtil.getValueByTag(element, "description");
			
			setVersionData(versionData, versionMap);
			setRemindNew(remind);
			setForceUpdate(force);
			setNewClientUrl(url);
			setNewClientVersion(clientVersion);
			setNewFeatures(features);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setVersionData(int data,int map) {
		mVersionData = data;
		mVersionMap = map;
	}

	public int getVersionData() {
		return mVersionData;
	}

	public int getVersionMap() {
		return mVersionMap;
	}

	public void setRemindNew(int remindNew) {
		mRemindNew = remindNew;
	}

	public boolean getRemindNew() {
		return (mRemindNew == 1);
	}

	public void setForceUpdate(int forceUpdate) {
		mForceUpdate = forceUpdate;
	}

	public boolean getForceUpdate() {
		return (mForceUpdate == 1);
	}

	public void setNewClientUrl(String url) {
		mUrl = url;
	}

	public String getNewClientUrl() {
		return mUrl;
	}

	public void setNewClientVersion(String version) {
		mClientVersion = version;
	}

	public String getNewClientVersion() {
		return mClientVersion;
	}

	public void setNewFeatures(String features) {
		mNewFeatures = features;
	}

	public String getNewFeatures() {
		return mNewFeatures;
	}

	public String getNewFile() {
		return String.format("%s.apk", mClientVersion);
	}
}
