package com.rtm.frm.net;

public class NetworkErrorCode {
	public static final int UNKNOWN_ERROR = -3;
	public static final int FILE_WRITE_ERROR = -2;
	public static final int DATA_TOO_BIG = -1;
	public static final int NET_ERROR = 1;
	public static final int NET_SOCKET_ERROR = 2;
	public static final int NET_TIMEOUT_ERROR = 3;
	
	public static final String MESSAGE_UNKNOWN_ERROR = "UNKNOWN_ERROR";
	public static final String MESSAGE_FILE_WRITE_ERROR = "FILE_WRITE_ERROR";
	public static final String MESSAGE_DATA_TOO_BIG = "DATA_TOO_BIG";
	public static final String MESSAGE_NET_ERROR = "NET_ERROR";
	public static final String MESSAGE_NET_SOCKET_ERROR = "NET_SOCKET_ERROR";
	public static final String MESSAGE_NET_TIMEOUT_ERROR = "NET_TIMEOUT_ERROR";
	
	public static String getNetError(int code) {
		switch(code) {
		case UNKNOWN_ERROR:
			return MESSAGE_UNKNOWN_ERROR;
		case FILE_WRITE_ERROR:
			return MESSAGE_FILE_WRITE_ERROR;
		case DATA_TOO_BIG:
			return MESSAGE_DATA_TOO_BIG;
		case NET_ERROR:
			return MESSAGE_NET_ERROR;
		case NET_SOCKET_ERROR:
			return MESSAGE_NET_SOCKET_ERROR;
		case NET_TIMEOUT_ERROR:
			return MESSAGE_NET_TIMEOUT_ERROR;
		}
		
		return null;
	}
}
