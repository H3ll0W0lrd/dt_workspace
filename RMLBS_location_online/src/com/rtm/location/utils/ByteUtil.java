/*
 * ByteUtil.java
 * classes : com.rtm.location.util.ByteUtil
 * @author zny
 * V 1.0.0
 * Create at 2015年1月16日 上午11:23:34
 */
package com.rtm.location.utils;

/**
 * com.rtm.location.util.ByteUtil
 * 
 * @author zny <br/>
 *         create at 2015年1月16日 上午11:23:34
 */
public class ByteUtil {

	public static byte[] longToByteArray(long data) {
		byte[] byteArray = new byte[8];
		try {
			byteArray[7] = (byte) (0xff & data);
			byteArray[6] = (byte) (0xff & (data >> 8));
			byteArray[5] = (byte) (0xff & (data >> 16));
			byteArray[4] = (byte) (0xff & (data >> 24));
			byteArray[3] = (byte) (0xff & (data >> 32));
			byteArray[2] = (byte) (0xff & (data >> 40));
			byteArray[1] = (byte) (0xff & (data >> 48));
			byteArray[0] = (byte) (0xff & (data >> 56));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return byteArray;
	}

	public static byte[] int2byteArray(int num) {
		byte[] result = new byte[4];
		result[0] = (byte) (num >> 24);// 取最高8位放到0下标
		result[1] = (byte) (num >> 16);// 取次高8为放到1下标
		result[2] = (byte) (num >> 8); // 取次低8位放到2下标
		result[3] = (byte) (num); // 取最低8位放到3下标
		return result;
	}

}
