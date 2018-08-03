package com.rtm.frm.utils;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XmlUtil {
	public static NodeList getNodeListByTag(String data, String tag) {
		StringReader stringReader = new StringReader(data);
		InputSource inputSource = new InputSource(stringReader);

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(inputSource);
			Element root = document.getDocumentElement();
			return root.getElementsByTagName(tag);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-20 上午11:37:53
	 * 
	 * @param data
	 * @return Element
	 */
	public static Element getRootElement(String data) {
		if (data == null || data.equals("")) {
			return null;
		}
		StringReader stringReader = new StringReader(data);
		InputSource inputSource = new InputSource(stringReader);

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(inputSource);
			return document.getDocumentElement();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getValueByTag(Element element, String tag) {
		String value = null;

		if (element == null || tag == null || tag.equals("")) {
			return null;
		}
		try {
			NodeList nl = element.getElementsByTagName(tag);
			if (nl != null && nl.getLength() > 0) {
				Element e = (Element) nl.item(0);
				if (e.getFirstChild() != null) {
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
		if (XunluUtil.isEmpty(value)) {
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
		if (XunluUtil.isEmpty(value)) {
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
