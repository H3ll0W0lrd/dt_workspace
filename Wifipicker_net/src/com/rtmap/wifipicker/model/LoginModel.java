package com.rtmap.wifipicker.model;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rtmap.wifipicker.data.MapInfo;
import com.rtmap.wifipicker.util.XmlHelper;

public class LoginModel {
	private String mToken;
	private ArrayList<MapInfo> mMapInfos;
	
	public LoginModel(String data) throws Exception {
		Element element = XmlHelper.getRootElement(data);
		mToken = XmlHelper.getValueByTag(element, "verification");
		NodeList nodeList = element.getElementsByTagName("image");
		
		parse(nodeList);
	}
	
	private void parse(NodeList nodeList) {
		mMapInfos = new ArrayList<MapInfo>();
		int length = nodeList.getLength();
		for(int i = 0; i < length; i++) {
			MapInfo info = new MapInfo();
			Node node = nodeList.item(i);
			String name = XmlHelper.getValueByTag((Element)node, "name");
			String md5 = XmlHelper.getValueByTag((Element)node, "check");
			float scale = XmlHelper.getFloatValueByTag((Element)node, "scale");
			try {
			    float offsetX = XmlHelper.getFloatValueByTag((Element)node, "offset_x");
	            float offsetY = XmlHelper.getFloatValueByTag((Element)node, "offset_y");
	            offsetX = (offsetX == -1.0f ? 0f : offsetX);
	            offsetY = (offsetY == -1.0f ? 0f : offsetY);
	            info.setOffsetX(offsetX);
	            info.setOffsetY(offsetY);
            } catch (Exception e) {
                info.setOffsetX(0f);
                info.setOffsetY(0f);
            }
			info.setName(name);
			info.setMD5(md5);
			info.setScale(scale);
			mMapInfos.add(info);
		}
	}
	
	public String getToken() {
		return mToken;
	}
	
	public ArrayList<MapInfo> getMapInfos() {
		return mMapInfos;
	}
}
