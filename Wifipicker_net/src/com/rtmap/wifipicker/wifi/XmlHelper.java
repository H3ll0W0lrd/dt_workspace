package com.rtmap.wifipicker.wifi;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class XmlHelper {
    private static String TAG = "XmlHelper";

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
        } catch (Exception e) {
            return null;
        }
    }

    public static String getValueByTag(Element element, String tag) {
        String value = null;

        try {
            NodeList nl = element.getElementsByTagName(tag);
            if (nl != null && nl.getLength() > 0) {
                Element e = (Element) nl.item(0);
                if (e.getFirstChild() != null) {
                    value = e.getFirstChild().getNodeValue();
                }
            }
        } catch (Exception e) {}

        return value;
    }

    public static int getIntValueByTag(Element element, String tag) {
        String value = getValueByTag(element, tag);
        if (value.equals("")) { return -1; }

        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            return -1;
        }
    }

    public static float getFloatValueByTag(Element element, String tag) {
        String value = getValueByTag(element, tag);
        if (value.equals("")) { return -1; }

        try {
            return Float.valueOf(value);
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getValue(String xml, String key) {
        String ret = "";
        if (xml == null) { return ""; }

        XmlPullParser parser = Xml.newPullParser();
        Reader in = new StringReader(xml);
        try {
            parser.setInput(in);
            int event = parser.getEventType();// 产生第一个事件
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:// 判断当前事件是否是文档开始事件
                        break;
                    case XmlPullParser.START_TAG:// 判断当前事件是否是标签元素开始事件
                        if (key.equals(parser.getName())) {
                            ret = parser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:// 判断当前事件是否是标签元素结束事件
                        break;
                }
                event = parser.next(); // 进入下一个元素
            }
        } catch (XmlPullParserException e) {} catch (IOException e) {}
        return ret;
    }
}
