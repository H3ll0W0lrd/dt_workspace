/*
 * StringUtils.java
 * classes : com.rtm.location.util.StringUtils
 * @author zny
 * V 1.0.0
 * Create at 2014年11月24日 下午5:34:33
 */
package com.dingtao.libs.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * com.rtm.location.util.StringUtils
 * 
 * @author zny <br/>
 *         create at 2014年11月24日 下午5:34:33
 */
public class DTStringUtil {

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
			// Log.log(e);
		}
		return result;
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
	/**
	 * 字符串是否为空
	 * @param str 字符串
	 * @return 是否为空
	 */
	public static boolean isEmpty(String str) {
		if (str == null) {
			return true;
		}
		if (str.equals("")) {
			return true;
		}
		if ("null".equals(str)){
			return true;
		}
		return false;
	}
	
	/**
	 * 验证手机号码
	 * 
	 * @param mobiles
	 * @return [0-9]{5,9}
	 */
	public boolean isMobileNO(String mobiles) {
		try {
			Pattern p = Pattern
					.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
			Matcher m = p.matcher(mobiles);
			return m.matches();
		} catch (Exception e) {
		}
		return false;
	}

}
