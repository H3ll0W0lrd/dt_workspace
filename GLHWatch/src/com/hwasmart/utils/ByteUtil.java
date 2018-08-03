package com.hwasmart.utils;

import java.util.Arrays;
import java.util.Locale;

public class ByteUtil {

	/**
	 * 长整型转成byte数组
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] getBytes(long data) {
		byte[] bytes = new byte[8];
		bytes[0] = (byte) (data & 0xff);
		bytes[1] = (byte) ((data >> 8) & 0xff);
		bytes[2] = (byte) ((data >> 16) & 0xff);
		bytes[3] = (byte) ((data >> 24) & 0xff);
		bytes[4] = (byte) ((data >> 32) & 0xff);
		bytes[5] = (byte) ((data >> 40) & 0xff);
		bytes[6] = (byte) ((data >> 48) & 0xff);
		bytes[7] = (byte) ((data >> 56) & 0xff);
		return bytes;
	}

	/**
	 * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。 和bytesToInt()配套使用
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] getBytes(int data) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (data & 0xff);
		bytes[1] = (byte) ((data >> 8) & 0xff);
		bytes[2] = (byte) ((data >> 16) & 0xff);
		bytes[3] = (byte) ((data >> 24) & 0xff);
		return bytes;
	}

	/**
	 * 将byte转换成16进制
	 * 
	 * @param value
	 * @return
	 * @throws
	 */
	public static String toHexString(byte value) {
		String tmp = Integer.toHexString(value & 0xFF);
		if (tmp.length() == 1) {
			tmp = "0" + tmp;
		}
		return tmp.toUpperCase(Locale.ENGLISH);
	}

	/**
	 * 将byte转换成16进制
	 * 
	 * @param value
	 * @return
	 * @throws
	 */
	public static String toHexString(byte[] value) {
		String result = "";
		for (int i = 0; i < value.length; i++) {
			String tmp = Integer.toHexString(value[i] & 0xFF);
			if (tmp.length() == 1) {
				tmp = "0" + tmp;
			}
			result += tmp;
		}
		return result.toUpperCase(Locale.ENGLISH);
	}

	/**
	 * byte数组截取
	 * 
	 * @param data
	 *            源数组
	 * @param index
	 *            开始位置
	 * @param length
	 *            长度
	 * @return
	 */
	public static byte[] subBytes(byte[] data, int index, int length) {
		byte[] temp = new byte[length];
		System.arraycopy(data, index, temp, 0, length);
		return temp;
	}

	/**
	 * 比较byte数组
	 * 
	 * @param a
	 *            byte数组1
	 * @param b
	 *            byte素组2
	 * @return
	 */
	public static boolean equalsBytes(byte[] a, byte[] b) {
		return Arrays.equals(a, b);
	}

	/**
	 * @param src
	 *            byte数组
	 * @param index
	 *            从数组的第index位开始
	 * @return int数值 byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序,和getBytes(int data)配套使用
	 */
	public static int bytesToInt(byte[] src, int index) {
		int value;
		value = (int) ((src[index] & 0xFF) 
				| ((src[index + 1] & 0xFF) << 8)
				| ((src[index + 2] & 0xFF) << 16) 
				| ((src[index + 3] & 0xFF) << 24));
		return value;
	}
	
	/**
	 * @param src
	 *            byte数组
	 * @param index
	 *            从数组的第index位开始
	 * @return Long数值 byte数组中取Long数值，本方法适用于(低位在前，高位在后)的顺序,和getBytes(long data)配套使用
	 */
	public static long bytesToLong(byte[] src, int index) {
		long value;
		value = (long) ((src[index] & 0xFF) 
				| ((src[index + 1] & 0xFF) << 8)
				| ((src[index + 2] & 0xFF) << 16) 
				| ((src[index + 3] & 0xFF) << 24)
				| ((src[index + 4] & 0xFF) << 32)
				| ((src[index + 5] & 0xFF) << 40)
				| ((src[index + 6] & 0xFF) << 48)
				| ((src[index + 7] & 0xFF) << 56));
		return value;
	}


}
