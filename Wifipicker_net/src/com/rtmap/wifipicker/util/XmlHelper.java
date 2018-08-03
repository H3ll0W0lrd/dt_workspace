package com.rtmap.wifipicker.util;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XmlHelper {
	public static NodeList getNodeListByTag(String data, String tag) {
		StringReader stringReader = new StringReader(data);
		InputSource inputSource = new InputSource(stringReader);
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(inputSource);
			Element root = document.getDocumentElement();
			return root.getElementsByTagName(tag);
		} catch(Exception e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
	public static Element getRootElement(String data) {
		StringReader stringReader = new StringReader(data);
		InputSource inputSource = new InputSource(stringReader);
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(inputSource);
			return document.getDocumentElement();
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getValueByTag(Element element, String tag) {
		String value = null;
		
		try {
			NodeList nl = element.getElementsByTagName(tag);
			if(nl != null && nl.getLength() > 0) {
				Element e = (Element)nl.item(0);
				if(e.getFirstChild() != null) {
					value = e.getFirstChild().getNodeValue();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return value;
	}
	
	public static int getIntValueByTag(Element element, String tag) {
		String value = getValueByTag(element, tag);
		if(Utils.isEmpty(value)) {
			return -1;
		}
		
		try {
			return Integer.valueOf(value);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public static float getFloatValueByTag(Element element, String tag) {
		String value = getValueByTag(element, tag);
		if(Utils.isEmpty(value)) {
			return -1;
		}
		
		try {
			return Float.valueOf(value);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
}
