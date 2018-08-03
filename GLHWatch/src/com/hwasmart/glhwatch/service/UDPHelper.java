package com.hwasmart.glhwatch.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hwasmart.utils.ByteUtil;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UDPHelper {
	private static UDPHelper instance;

	final private String TAG = "UDPHelper";

	private Handler handler;
	private DatagramSocket udpSocket;
	InetSocketAddress serverAddress;
	public boolean running = false;
	private static String glhServerIP = "123.57.74.38"; // 儿童防丢失服务器IP地址
	private static int glhServerPort = 9094; // 儿童方队是服务器端口
	public static ExecutorService EXECUTOR = Executors.newCachedThreadPool();

	public static UDPHelper getInstance() {
		if (instance == null) {
			instance = new UDPHelper();
		}
		return instance;
	}

	private UDPHelper() {
		try {
			serverAddress = new InetSocketAddress(glhServerIP, glhServerPort);
			udpSocket = new DatagramSocket(37746);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void start(Handler handler) {
		this.handler = handler;
		running = true;
		new Thread(recvRunnable).start();
	}

	public void stop() {
		running = false;
		// udpSocket.close();
	}

	public void send(final byte[] data, final int length) {
		EXECUTOR.execute(new Runnable() {

			@Override
			public void run() {
				if (udpSocket == null)
					return;

				DatagramPacket sendPacket;
				try {
					sendPacket = new DatagramPacket(data, length, serverAddress);
					udpSocket.send(sendPacket);
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

	}

	Runnable recvRunnable = new Runnable() {

		@Override
		public void run() {
			byte[] recvBuf = new byte[1024];
			DatagramPacket recvPacket = new DatagramPacket(recvBuf,
					recvBuf.length);
			while (running) {
				if (udpSocket == null)
					return;

				try {
					udpSocket.receive(recvPacket);
					Log.i(TAG,
							new String(recvPacket.getData(), recvPacket
									.getOffset(), recvPacket.getLength()));
					if (handler != null) {

						// 解析收到的数据向界面上报
						byte[] data = recvPacket.getData();
						int dataLen = data.length;
						if (dataLen < 2)
							continue;

						Message msg = handler.obtainMessage();

						if (data[0] == (byte) 0xA1) {
							if (data[1] == (byte) 0xFD) {
								// 状态下发接口
								Log.i(TAG, "status message received!");
								msg.what = 0;
								msg.arg1 = data[2];
								msg.arg2 = data[3];
								byte[] msgbytes = new byte[msg.arg2];
								System.arraycopy(data, 4, msgbytes, 0, msg.arg2);
								String msgStr = new String(msgbytes, "UTF-8");
								msg.obj = msgStr;
							} else if (data[1] == (byte) 0xEF) {
								// 时间同步完成
								Log.i(TAG, "sync finished!");
								msg.what = 1;
								int groupid = ByteUtil.bytesToInt(data, 8);
								msg.arg1 = groupid;
								msg.arg2 = data[12];
								int length = data[13];
								if (length > 0) {
									byte[] msgbytes = new byte[length];
									System.arraycopy(data, 14, msgbytes, 0,
											length);
									String msgStr = new String(msgbytes,
											"UTF-8");
									msg.obj = msgStr;
									Log.i(TAG, "wifi disconnected word:"
											+ msgStr);
								}
							} else {
								Log.i(TAG, "other A1");
							}
						} else if (data[0] == 0x4F && data[1] == 0x4B) {
							// 收到服务器求助请求确认消息
							Log.i(TAG, "help send success!");
							msg.what = 2;

						} else if (data[0] == 0x40 && data[1] == 0x3F) {
							// 收到服务器发来的求助请求
							Log.i(TAG, "received help message!");
							msg.what = 3;
							msg.arg1 = data[2];
							byte[] msgbytes = new byte[msg.arg1];
							System.arraycopy(data, 3, msgbytes, 0, msg.arg1);
							String msgStr = new String(msgbytes, "UTF-8");
							msg.obj = msgStr;

						} else {
							Log.i(TAG, "unknown message received!");
						}
						handler.sendMessage(msg);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Log.i(TAG, "udp receiver thread close!");
		}
	};
}
