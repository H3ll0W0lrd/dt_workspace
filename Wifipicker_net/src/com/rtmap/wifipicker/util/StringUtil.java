package com.rtmap.wifipicker.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class StringUtil {
    // private static final String TAG = "StringUtil";

    private static final char[] BASE64_ENCODE_CHARS = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

    public static boolean isEmpty(String str) {
        if (str != null && (!(str.equals("")))) {
            return false;
        }
        return true;
    }
    
    
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
                sb.append(BASE64_ENCODE_CHARS[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
                sb.append(BASE64_ENCODE_CHARS[(b2 & 0x0f) << 2]);
                sb.append("=");
                break;
            }
            b3 = data[i++] & 0xff;
            sb.append(BASE64_ENCODE_CHARS[b1 >>> 2]);
            sb.append(BASE64_ENCODE_CHARS[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
            sb.append(BASE64_ENCODE_CHARS[((b2 & 0x0f) << 2) | ((b3 & 0xc0) >>> 6)]);
            sb.append(BASE64_ENCODE_CHARS[b3 & 0x3f]);
        }
        return sb.toString();
    }

}
