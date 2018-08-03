/**
 * 地图定位共同使用的工具
 */
package com.rtm.common.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * 字符串工具类，包含楼层类型转换，字符编码等方法
 * @author dingtao
 *
 */
public class RMStringUtils {

	private static final char[] BASE64_ENCODE_CHARS = new char[] { 'A', 'B',
			'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
			'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b',
			'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
			'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1',
			'2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

	/**
	 * 获取URLencode编码
	 * 
	 * @param s
	 * @return
	 */
	public static String getUrlEncode(String s) {
		if (s == null) {
			return null;
		}
		String result = "";
		try {
			result = URLEncoder.encode(s, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 得到部分字符串
	 * @param string
	 * @param maxLength
	 * @return
	 */
	public static String getSubString(String string, int maxLength) {
		if (string == null || maxLength <= 0) {
			return null;
		}
		int length = string.length();
		if (length > maxLength) {
			return String.format("%s%s", string.substring(0, maxLength), " ……");
		} else {
			return string;
		}
	}

	/**
	 * base64编码
	 * 
	 * @param data
	 * @return
	 */
	public static String base64Encode(byte[] data) {
		StringBuffer sb = new StringBuffer();
		int len = data.length;
		int i = 0;
		int b1, b2, b3;

		while (i < len) {
			b1 = data[i++] & 0xff;
			if (i == len) {
				sb.append(BASE64_ENCODE_CHARS[b1 >>> 2]);
				sb.append(BASE64_ENCODE_CHARS[(b1 & 0x3) << 4]);
				sb.append("==");
				break;
			}
			b2 = data[i++] & 0xff;
			if (i == len) {
				sb.append(BASE64_ENCODE_CHARS[b1 >>> 2]);
				sb.append(BASE64_ENCODE_CHARS[((b1 & 0x03) << 4)
						| ((b2 & 0xf0) >>> 4)]);
				sb.append(BASE64_ENCODE_CHARS[(b2 & 0x0f) << 2]);
				sb.append("=");
				break;
			}
			b3 = data[i++] & 0xff;
			sb.append(BASE64_ENCODE_CHARS[b1 >>> 2]);
			sb.append(BASE64_ENCODE_CHARS[((b1 & 0x03) << 4)
					| ((b2 & 0xf0) >>> 4)]);
			sb.append(BASE64_ENCODE_CHARS[((b2 & 0x0f) << 2)
					| ((b3 & 0xc0) >>> 6)]);
			sb.append(BASE64_ENCODE_CHARS[b3 & 0x3f]);
		}
		return sb.toString();
	}

	/**
	 * Convert byte[] to hex
	 * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
	 * 
	 * @param src
	 *            byte[] data
	 * @return hex string
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase(Locale.getDefault());
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/**
	 * Convert char to byte
	 * 
	 * @param c
	 *            char
	 * @return byte
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static boolean isEmpty(String str) {
		if (str == null) {
			return true;
		}
		if (str.equals("")) {
			return true;
		}
		return false;
	}

	/**
	 * 楼层换算:10000是B,20000是F,剩下的数是楼层的10倍
	 * 
	 * @param floor
	 *            整型楼层，例：20100
	 * @return 字符串型楼层，例：F10
	 */
	public static String floorTransform(int floor) {
		String a = null;
		if (floor == 0) {
			return a;
		}
		if (floor / 10000 == 1) {
			a = "B";
		} else {
			a = "F";
		}
		int f = floor % 10;// 看有没有半层
		if (f != 0) {
			a += floor % 10000 / 10f;
		} else {
			a += floor % 10000 / 10;
		}
		return a;
	}

	/**
	 * 楼层换算,B是10000，F是20000，剩下数字乘以10
	 * 
	 * @param floor
	 *            字符串型楼层，例：F1.5
	 * @return 整型楼层，例：20015
	 */
	public static int floorTransform(String floor) {
		if(isEmpty(floor)){
			return 0;
		}
		int a = 0;
		String str1 = floor.substring(0, 1);
		if (floor.contains(".5")) {
			a += Integer.parseInt(floor.substring(1, floor.indexOf("."))) * 10 + 5;
		} else {
			a += Integer.parseInt(floor.substring(1)) * 10;
		}

		if ("B".equals(str1)) {
			a += 10000;
		} else if ("F".equals(str1)) {
			a += 20000;
		}
		return a;
	}

}
