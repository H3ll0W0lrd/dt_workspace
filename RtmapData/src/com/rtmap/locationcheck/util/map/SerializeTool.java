/**
 * 项目名称:智能泊车系统(Smart Parker）
 * 创建时间:2011.11.1
 * 版本:1.0.0
 * Copyright (c),RTM
 */
package com.rtmap.locationcheck.util.map;


/**
 *********************************************************
 * @文件:RTMSerializeTool.java
 * @说明: 
 * @创建日期:2011-11-15
 * @作者:林巍凌
 * @版权:RTM
 * @版本:1.0.0
 * @标签:RTMSerializeTool
 * @最后更新时间:2011-12-1
 * @最后更新作者:林巍凌
 *********************************************************
 */
public class SerializeTool {
	public static byte[] charToBytes(char c){
		byte[] bt = new byte[2]; 
		bt[0] = (byte) (0xff & c); 
		bt[1] = (byte) ((0xff00 & c) >> 8);
		return bt;
	}
	
	public static char bytesToChar(byte[] b) throws NullPointerException, ArrayIndexOutOfBoundsException {
		int num = b[0] & 0xFF; 
		num |= ((b[1] << 8) & 0xFF00); 
		return (char)num;
	}
	
	public static byte[] shortToBytes(short i){
		byte[] bt = new byte[2]; 
		bt[0] = (byte) (0xff & i); 
		bt[1] = (byte) ((0xff00 & i) >> 8);
		return bt;
	}
	
	public static short bytesToShort(byte[] b) throws NullPointerException, ArrayIndexOutOfBoundsException {
		int num = b[0] & 0xFF; 
		num |= ((b[1] << 8) & 0xFF00); 
		return (short)num;
	}
	
	public static byte[] intToBytes(int n){
		byte[] bt = new byte[4]; 
		bt[0] = (byte) (0xff & n); 
		bt[1] = (byte) ((0xff00 & n) >> 8); 
		bt[2] = (byte) ((0xff0000 & n) >> 16); 
		bt[3] = (byte) ((0xff000000 & n) >> 24); 
		return bt;
	}
	
	public static int bytesToInt(byte[] b) throws NullPointerException, ArrayIndexOutOfBoundsException {
		int num = b[0] & 0xFF; 
		num |= ((b[1] << 8) & 0xFF00); 
		num |= ((b[2] << 16) & 0xFF0000); 
		num |= ((b[3] << 24) & 0xFF000000); 
		return num;
	}
	
	public static byte[] longToBytes(long l){
		byte[] b = new byte[8];
		b[0] = (byte) (l >> 56);
        b[1] = (byte) (l >> 48);
        b[2] = (byte) (l >> 40);
        b[3] = (byte) (l >> 32);
        b[4] = (byte) (l >> 24);
        b[5] = (byte) (l >> 16);
        b[6] = (byte) (l >> 8);
        b[7] = (byte) (l >> 0);
		return b;
	}
	
	public static long bytesToLong(byte[] b) throws NullPointerException, ArrayIndexOutOfBoundsException {
		 return ((((long) b[0] & 0xff) << 56) 
	                | (((long) b[1] & 0xff) << 48) 
	                | (((long) b[2] & 0xff) << 40) 
	                | (((long) b[3] & 0xff) << 32) 
	                | (((long) b[4] & 0xff) << 24) 
	                | (((long) b[5] & 0xff) << 16) 
	                | (((long) b[6] & 0xff) << 8) | (((long) b[7] & 0xff) << 0));
	}
	
	public static byte[] doubleToBytes(double d){
		return longToBytes(Double.doubleToLongBits(d));
	}
	
	public static double bytesToDouble(byte[] b) throws NullPointerException, ArrayIndexOutOfBoundsException {
		return Double.longBitsToDouble(bytesToLong(b));
	}
	
	public static byte[] stringToBytes(char[] str, int length){
		byte[] b = new byte[length * 2/*utf-16,2byte*/ + 8];
		int size = str.length;
		int nPos = 0;
		// 首先写入数组实际大小
		byte[] buf = intToBytes(size);
		System.arraycopy(buf, 0, b, nPos, 4);
		nPos += 4;
		// 再写入数组最大长度
		buf = intToBytes(length);
		System.arraycopy(buf, 0, b, nPos, 4);
		nPos += 4;
		for(int i = 0; i < length; i++){
			if(i < size){
				buf = charToBytes(str[i]);
				System.arraycopy(buf, 0, b, nPos, 2);
				nPos += 2;
			}else{
				b[nPos++] = 0;
				b[nPos++] = 0;
			}
		}
		return b;
	}
	
	public static char[] bytesToString(byte[] b) throws NullPointerException, ArrayIndexOutOfBoundsException {
		int nPos = 0;
		byte[] temp = new byte[4];
		System.arraycopy(b, 0, temp, 0, 4);
		int size = bytesToInt(temp);
		nPos += 4;
		
		int length = bytesToInt(temp);
		System.arraycopy(b, nPos, temp, 0, 4);
		nPos += 4;
		
		char[] strRet = new char[length];
		int n = 0;
		for (int i = 0; i < length; i++) {
			if (n < size) {
				System.arraycopy(b, nPos, temp, 0, 2);
				strRet[n++] = bytesToChar(temp);
				i++;
			}
		}
		return strRet;
	}
	
	public static boolean bytesToString(byte[] b, char[] str)throws NullPointerException, ArrayIndexOutOfBoundsException{
		if(b == null || str == null)
			throw new NullPointerException();
		int nPos = 0;
		byte[] buf = new byte[4];
		System.arraycopy(b, 0, buf, 0, 4);
		int size = bytesToInt(buf);
		nPos += 4;
		
		int length = bytesToInt(buf);
		System.arraycopy(b, nPos, buf, 0, 4);
		nPos += 4;
		
		if(str.length < length)
			throw new ArrayIndexOutOfBoundsException("string buffer is not enough.");
		
		int n = 0;
		for (int i = 0; i < length; i++) {
			if (n < size) {
				System.arraycopy(b, nPos, buf, 0, 2);
				str[n++] = bytesToChar(buf);
				i++;
			}
		}
		return true;
	}
	
	public static int putInt(byte[] b, int from, int n){
		b[from + 0] = (byte) (0xff & n); 
		b[from + 1] = (byte) ((0xff00 & n) >> 8); 
		b[from + 2] = (byte) ((0xff0000 & n) >> 16); 
		b[from + 3] = (byte) ((0xff000000 & n) >> 24);
		return 4;
	}
	
	public static int getInt(byte[] b, int from){
		int num = b[from + 0] & 0xFF; 
		num |= ((b[from + 1] << 8) & 0xFF00); 
		num |= ((b[from + 2] << 16) & 0xFF0000); 
		num |= ((b[from + 3] << 24) & 0xFF000000);
		return num;
	}
	
	public static int putLong(byte[] b, int from, long l){
		b[from + 0] = (byte) (l >> 56);
        b[from + 1] = (byte) (l >> 48);
        b[from + 2] = (byte) (l >> 40);
        b[from + 3] = (byte) (l >> 32);
        b[from + 4] = (byte) (l >> 24);
        b[from + 5] = (byte) (l >> 16);
        b[from + 6] = (byte) (l >> 8);
        b[from + 7] = (byte) (l >> 0);
        return 8;
	}
	
	public static long getLong(byte[] b, int from){
		return ((((long) b[from + 0] & 0xff) << 56) 
                | (((long) b[from + 1] & 0xff) << 48) 
                | (((long) b[from + 2] & 0xff) << 40) 
                | (((long) b[from + 3] & 0xff) << 32) 
                | (((long) b[from + 4] & 0xff) << 24) 
                | (((long) b[from + 5] & 0xff) << 16) 
                | (((long) b[from + 6] & 0xff) << 8)
                | (((long) b[from + 7] & 0xff) << 0));
	}
	
	public static int putChar(byte[] b, int from, char c){
		b[from + 0] = (byte) (0xff & c); 
		b[from + 1] = (byte) ((0xff00 & c) >> 8);
		return 2;
	}
	
	public static char getChar(byte[] b, int from){
		int num = b[from + 0] & 0xFF; 
		num |= ((b[from + 1] << 8) & 0xFF00);
		return (char)num;
	}
	
	public static int putBytes(byte[] b, int from, final byte[] buffer){
		System.arraycopy(buffer, 0, b, from, buffer.length);
		return buffer.length;
	}
	
	public static int getBytes(byte[] src, int from, byte[] dst){
		System.arraycopy(src, from, dst, 0, dst.length);
		return dst.length;
	}
	
	public static int putCharArray(byte[] b, int from, char[] str, int length){
		int nPos = from;
		// add by linweiling at 20110410 begin
		// 如果字符串长度超过length，则截断，只存储length长度的字符串
		int str_len = str.length;
		if(str_len > length)
			str_len = length;
		// add end.
		putInt(b, nPos, str_len);
		nPos += 4;
		putInt(b, nPos, length);
		nPos += 4;
		for(int i = 0; i < length; i++){
			if(i < str.length){
				putChar(b, nPos, str[i]);
				nPos += 2;
			}
		}
		return length * 2 + 8;
	}
	
	public static char[] getCharArray(byte[] b, int from){
		int nPos = from;
		int nSize = getInt(b, nPos);
		nPos += 4;
		int nLength = getInt(b, nPos);
		nPos += 4;
		char[] str = new char[nLength];
		int n = 0;
		
		for (int i = 0; i < nLength; i++) {
			if (n < nSize && n < nLength) {
				str[n++] = getChar(b, nPos);
				nPos += 2;
			}
		}
		return str;
	}
	
	public static int getCharArray(byte[] b, int from, char[] str){
		int nPos = from;
		int nSize = getInt(b, nPos);
		nPos += 4;
		int nLength = getInt(b, nPos);
		nPos += 4;

		int n = 0;		
		for (int i = 0; i < nLength; i++) {
			if (n < nSize && n < nLength) {
				str[n++] = getChar(b, nPos);
				nPos += 2;
			}
		}
		
		return nLength * 2 + 8;
	}
}
