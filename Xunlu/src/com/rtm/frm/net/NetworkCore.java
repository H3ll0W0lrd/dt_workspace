package com.rtm.frm.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.FileUtil;
import com.rtm.frm.utils.GzipHelper;

/**
 * 封装网络操作的类
 * 
 * @author XiaoYi
 * 
 */
public class NetworkCore {
	static public enum NetworkState {
		UNAVAIL, WIFI, MOBILE
	};

	public final static String NET_TYPE_NET = "1";
	public final static String NET_TYPE_WAP = "2";
	public final static String NET_TYPE_WIFI = "3";

	private static final String END = "\r\n";
	private static final String HYPENS = "--";
	private static final String BOUNDARY = "--------7da3d81520810*";
	private static final int MAX_DATA_LENG = 2097152; // 2*1024*1024
	private static final int BUFFERSIZE = 1024;
	private static final int CONNECTTIMEOUT = 7 * 1000;
	private static final int GETDATATIMEOUT = 7 * 1000;
	private static final int POSTDATATIMEOUT = 10 * 1000;

	private static Handler sHandler = null;
	private static volatile String sProxyUser = null;
	private static volatile boolean sHaveInitProxyUser = false;
	private static Pattern sPattern = Pattern.compile(
			"^[0]{0,1}10\\.[0]{1,3}\\.[0]{1,3}\\.172$", Pattern.MULTILINE);

	private HttpURLConnection mConn;
	private String mUrl;
	private int mNetErrorCode;
	private int mServerErrorCode;
	private int mWapRetryConnt;
	private int mRetryConnt;
	private int mMaxRetryCount;
	private ArrayList<BasicNameValuePair> mPostData;
	private HashMap<String, byte[]> mFileData;
	private boolean mRequestGzip;
	private Context mContext;
	private boolean mIsBDImage;
	private boolean mIsLimited;
	private volatile boolean mIsInterrupte;

	private void initNetWork() {
		mConn = null;
		mUrl = null;
		mNetErrorCode = 0;
		mServerErrorCode = 0;
		mPostData = null;
		mContext = null;
		mRequestGzip = true;
		mWapRetryConnt = 0;
		mIsInterrupte = false;
		mIsBDImage = false;
		mFileData = null;
		mIsLimited = false;
		mRetryConnt = 0;
		mMaxRetryCount = 1;
		initPorxyUser();
	}

	/**
	 * 
	 * 方法描述 : 获取网络类型 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-4-29 下午1:55:39
	 * 
	 * @return String
	 */
	@SuppressWarnings("deprecation")
	public String getNetType() {
		try {
			ConnectivityManager cwjManager = (ConnectivityManager) mContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkinfo = cwjManager.getActiveNetworkInfo();
			boolean netSataus = networkinfo.isAvailable();

			if (!netSataus) {
				return null;
			} else {
				if (networkinfo.getTypeName().equalsIgnoreCase("WIFI")) {
					return NET_TYPE_WIFI;
				} else {
					String proxyHost = android.net.Proxy.getDefaultHost();
					if (proxyHost != null && proxyHost.length() > 0) {
						return NET_TYPE_WAP;
					} else {
						return NET_TYPE_NET;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	static public void initPorxyUser() {
		synchronized (NetworkCore.class) {
			if (!sHaveInitProxyUser) {
				sHaveInitProxyUser = true;
				try {
					Uri uri = Uri
							.parse("content://telephony/carriers/preferapn");
					Cursor apn = XunluApplication.mApp.getContentResolver()
							.query(uri, null, null, null, null);
					if (apn != null && apn.moveToNext()) {
						String name = apn.getString(apn.getColumnIndex("user"));
						String pwd = apn.getString(apn
								.getColumnIndex("password"));
						apn.close();
						String login = name + ":" + pwd;
						String encodedLogin = RMStringUtils.base64Encode(login
								.getBytes());
						sProxyUser = "Basic " + encodedLogin;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	static public void initNetWorkCore() {
		sHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				try {
					NetworkCore network = (NetworkCore) msg.obj;
					if (network != null) {
						network.cancelNetConnect();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};
	}

	public NetworkCore() {
		initNetWork();
		mContext = XunluApplication.mApp;
	}

	public NetworkCore(String url) {
		initNetWork();
		mContext = XunluApplication.mApp;
		mUrl = url;
	}

	/**
	 * 构造函数
	 * 
	 * @param context
	 *            上下文，最好使用应用上下以免发生内存泄露
	 * @param url
	 *            URL地址
	 */
	public NetworkCore(Context context, String url) {
		initNetWork();
		mContext = context;
		mUrl = url;
	}

	/**
	 * 设置是否请求压缩格式，默认请求压缩格式
	 * 
	 * @param requestGzip
	 *            是否请求压缩格式
	 */
	public void setRequestGzip(Boolean requestGzip) {
		mRequestGzip = requestGzip;
	}

	/**
	 * 获得当前是否请求压缩格式
	 * 
	 * @return true：请求； false：不请求
	 */
	public Boolean getRequestGzip() {
		return mRequestGzip;
	}

	/**
	 * 静态方法，获取当前网络的状态信息
	 * 
	 * @param context
	 *            上下文
	 * @return NetworkState 状态
	 */
	public static NetworkState getNetworkState(Context context) {
		boolean netSataus = false;
		NetworkInfo networkinfo = null;
		NetworkState ret = NetworkState.UNAVAIL;
		try {
			ConnectivityManager cwjManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			networkinfo = cwjManager.getActiveNetworkInfo();
			if (networkinfo != null) {
				netSataus = networkinfo.isAvailable();

				if (!netSataus) {
					ret = NetworkState.UNAVAIL;
				} else {
					if (networkinfo.getTypeName().equalsIgnoreCase("WIFI")) {
						ret = NetworkState.WIFI;
					} else {
						ret = NetworkState.MOBILE;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 
	 * 方法描述 : 判断WiFi模块是否打开 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-12
	 * 下午12:05:58
	 * 
	 * @param context
	 * @return boolean
	 */
	@SuppressWarnings("static-access")
	public static boolean checkWifi(Context context) {
		boolean isWifiConnect = true;
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		// check the networkInfos numbers
		NetworkInfo[] networkInfos = cm.getAllNetworkInfo();
		for (int i = 0; i < networkInfos.length; i++) {
			if (networkInfos[i].getState() == NetworkInfo.State.CONNECTED) {
				if (networkInfos[i].getType() == cm.TYPE_MOBILE) {
					isWifiConnect = false;
				}
				if (networkInfos[i].getType() == cm.TYPE_WIFI) {
					isWifiConnect = true;
				}
			}
		}
		return isWifiConnect;
	}

	/**
	 * 
	 * 方法描述 : 判断是否有网络连接 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-4-14
	 * 下午2:08:16
	 * 
	 * @param context
	 * @return boolean
	 */
	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				if (mNetworkInfo.isAvailable()) {
					return ping();
				}
			}
		}
		return false;
	}

	/***
	 * @author hukunge
	 * @return boolean
	 * @param context
	 * 
	 * **/

	public static boolean isNetConnected(Context mContext) {

		ConnectivityManager manager = (ConnectivityManager) mContext
				.getApplicationContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);

		if (manager == null) {
			return false;
		}

		NetworkInfo networkinfo = manager.getActiveNetworkInfo();

		if (networkinfo == null || !networkinfo.isAvailable()) {
			return false;
		}

		return true;
	}

	/**
	 * 设置URL地址
	 * 
	 * @param url
	 */
	public void setUrl(String url) {
		mUrl = url;
	}

	/**
	 * 获得当前的URL地址
	 * 
	 * @return
	 */
	public String getUrl() {
		return mUrl;
	}

	public void setMaxRetryCount(int count) {
		mMaxRetryCount = count;
	}

	/**
	 * 判断是否请求成功
	 * 
	 * @return true：成功； false：失败
	 */
	public boolean isRequestSuccess() {
		if (mNetErrorCode == HttpStatus.SC_OK && mServerErrorCode == 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isNetSuccess() {
		if (mNetErrorCode == HttpStatus.SC_OK) {
			return true;
		} else {
			return false;
		}
	}

	public int getErrorCode() {
		return mServerErrorCode;
	}

	public int getNetErrorCode() {
		return mNetErrorCode;
	}

	public void setErrorCode(int code) {
		mServerErrorCode = code;
	}

	/**
	 * 取消当前的网络请求
	 */
	public void cancelNetConnect() {
		mIsInterrupte = true;
		try {
			if (mConn != null) {
				mConn.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 连接服务器
	 * 
	 * @param url
	 *            地址
	 * @return HttpURLConnection null表示失败
	 */
	@SuppressWarnings("deprecation")
	private HttpURLConnection getConnect(URL url) {
		HttpURLConnection conn = null;
		NetworkState state = getNetworkState(mContext);
		mIsLimited = false;

		try {
			if (state == NetworkState.UNAVAIL) {
				return null;
			} else if (state == NetworkState.MOBILE) {
				String proxyHost = android.net.Proxy.getDefaultHost();
				if (proxyHost != null && proxyHost.length() > 0) {
					if (isCMCCServer(proxyHost)) {
						mIsLimited = true;
						StringBuffer new_address = new StringBuffer(80);
						new_address.append("http://");
						new_address.append(android.net.Proxy.getDefaultHost());
						String file = url.getFile();
						if (file != null && file.startsWith("?")) {
							new_address.append("/");
						}
						new_address.append(file);
						URL new_url = new URL(new_address.toString());
						conn = (HttpURLConnection) new_url.openConnection();
						conn.setRequestProperty("X-Online-Host", url.getHost());
					} else {
						java.net.Proxy p = null;
						p = new java.net.Proxy(java.net.Proxy.Type.HTTP,
								new InetSocketAddress(
										android.net.Proxy.getDefaultHost(),
										android.net.Proxy.getDefaultPort()));
						conn = (HttpURLConnection) url.openConnection(p);
						if (sProxyUser != null) {
							conn.setRequestProperty("Proxy-Authorization",
									sProxyUser);
						}
					}
				}
			}
			if (conn == null) {
				conn = (HttpURLConnection) url.openConnection();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return conn;
	}

	private boolean isCMCCServer(String ip) {
		boolean ret = false;
		Matcher m = sPattern.matcher(ip);
		if (m.find()) {
			ret = true;
		} else {
			ret = false;
		}
		return ret;
	}

	/**
	 * 使用get方法请求网络数据
	 * 
	 * @return 服务器返回的字符串，应该先用isRequestSuccess判断是否请求成功，再使用返回的数据
	 */
	public byte[] getNetData() {
		ByteArrayOutputStream outputstream = null;
		byte[] output = null;
		InputStream in = null;
		boolean isNetError = false;
		try {
			URL url = null;
			if (mPostData != null && mPostData.size() > 0) {
				StringBuffer buffer = new StringBuffer(30);
				buffer.append(mUrl);
				if (mUrl.indexOf("?") < 0) {
					buffer.append("?");
				} else if (!mUrl.endsWith("?") && !mUrl.endsWith("&")) {
					buffer.append("&");
				}
				for (int i = 0; i < mPostData.size(); i++) {
					if (i != 0) {
						buffer.append("&");
					}
					buffer.append(mPostData.get(i).getName());
					buffer.append("=");
					buffer.append(mPostData.get(i).getValue());
				}
				url = new URL(buffer.toString());
			} else {
				Log.i("net", "mUrl = " + mUrl);
				if (mUrl != null) {
					url = new URL(mUrl);
				} else {
					return null;
				}
			}
			mConn = getConnect(url);
			if (mConn == null) {
				throw new java.net.SocketException();
			}
			mConn.setConnectTimeout(CONNECTTIMEOUT);
			mConn.setReadTimeout(GETDATATIMEOUT);
			if (mRequestGzip && !mIsBDImage) {
				mConn.setRequestProperty("Accept-Encoding", "gzip");
			}
			if (mIsInterrupte) {
				return null;
			}
			mConn.connect();
			mNetErrorCode = mConn.getResponseCode();
			if (mNetErrorCode != HttpStatus.SC_OK) {
				throw new java.net.SocketException();
			}

			/**
			 * 判断是否是移动的提示信息
			 */
			if (mConn.getContentType().contains("text/vnd.wap.wml")
					&& mWapRetryConnt < 1) {
				mConn.disconnect();
				mWapRetryConnt++;
				mNetErrorCode = 0;
				return getNetData();
			} else {
				mWapRetryConnt = 0;
			}

			String encodeing = mConn.getContentEncoding();

			in = mConn.getInputStream();

			byte[] buf = new byte[BUFFERSIZE];
			int num = -1;
			outputstream = new ByteArrayOutputStream(BUFFERSIZE);
			int size = 0;

			/**
			 * 图片策略
			 */
			if (mIsBDImage) {
				byte[] b = new byte[23];
				in.read(b, 0, 23);
				String title = new String(b, 0, b.length);
				if (!title.equalsIgnoreCase("app:client;type:0;")) {
					outputstream.write(b, 0, 23);
					size += 23;
				}
			}

			while (!mIsInterrupte && size < MAX_DATA_LENG
					&& (num = in.read(buf)) != -1) {
				outputstream.write(buf, 0, num);
				size += num;
			}
			if (size < MAX_DATA_LENG) {
				output = outputstream.toByteArray();
				if (encodeing != null && encodeing.contains("gzip")) {
					ByteArrayInputStream tmpInput = new ByteArrayInputStream(
							output);
					ByteArrayOutputStream tmpOutput = new ByteArrayOutputStream(
							BUFFERSIZE);
					GzipHelper.decompress(tmpInput, tmpOutput);
					output = tmpOutput.toByteArray();
				}
			} else {
				mNetErrorCode = NetworkErrorCode.DATA_TOO_BIG;
			}

		} catch (java.net.SocketException ex) {
			ex.printStackTrace();
			mNetErrorCode = NetworkErrorCode.NET_SOCKET_ERROR;
			isNetError = true;
		} catch (java.net.SocketTimeoutException ex) {
			ex.printStackTrace();
			mNetErrorCode = NetworkErrorCode.NET_TIMEOUT_ERROR;
			isNetError = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			mNetErrorCode = NetworkErrorCode.NET_ERROR;
		} finally {
			mWapRetryConnt = 0;
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				if (mConn != null) {
					mConn.disconnect();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (!mIsInterrupte && isNetError && mRetryConnt < mMaxRetryCount) {
			mRetryConnt++;
			return getNetData();
		} else {
			mRetryConnt = 0;
			return output;
		}
	}

	/**
	 * 读取服务器返回的数据编码格式
	 * 
	 * @return
	 * @throws Exception
	 */
	private String getCharset() throws Exception {
		String type = null;
		if (mConn != null) {
			type = mConn.getContentType();
		}
		String charset = "utf-8";
		if (type != null) {
			int index = type.indexOf("charset");
			if (index != -1) {
				int end = type.indexOf(' ', index);
				if (end == -1) {
					charset = type.substring(index + 8);
				} else {
					charset = type.substring(index + 8, end);
				}
			}
		}
		return charset;
	}

	public boolean isFileSegSuccess() {
		if (mNetErrorCode != HttpStatus.SC_OK
				&& mNetErrorCode != HttpStatus.SC_PARTIAL_CONTENT) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 下载文件
	 * 
	 * @param path
	 *            保存文件的全路径
	 * @return true：成功； false：失败
	 */
	@SuppressWarnings("resource")
	public Boolean downloadFile(String path, Handler handler) {
		InputStream in = null;
		Boolean ret = false;
		long time = 0;
		FileOutputStream fileStream = null;
		try {
			URL url = new URL(mUrl);
			mConn = getConnect(url);
			if (mConn == null) {
				throw new java.net.SocketException();
			}
			mConn.setConnectTimeout(CONNECTTIMEOUT);
			mConn.setReadTimeout(GETDATATIMEOUT);
			mConn.setInstanceFollowRedirects(false);
			if (mIsInterrupte) {
				return false;
			}
			time = new Date().getTime();

			File file = FileUtil.createFileIfNotFound(path);
			if (file == null) {
				throw new FileNotFoundException();
			}
			long fileLen = file.length();
			fileStream = new FileOutputStream(file, true);
			if (mIsLimited) {
				mConn.addRequestProperty(
						"Range",
						"bytes=" + String.valueOf(fileLen) + "-"
								+ String.valueOf(fileLen + 200000));
			} else {
				mConn.addRequestProperty("Range",
						"bytes=" + String.valueOf(fileLen) + "-");
			}

			mConn.connect();
			mNetErrorCode = mConn.getResponseCode();
			if (!isFileSegSuccess()) {
				throw new java.net.SocketException();
			}

			/**
			 * 判断是否是移动的提示信息
			 */
			if (mConn.getContentType().contains("text/vnd.wap.wml")
					&& mWapRetryConnt < 1) {
				mConn.disconnect();
				mWapRetryConnt++;
				mNetErrorCode = 0;
				return downloadFile(path, handler);
			} else {
				mWapRetryConnt = 0;
			}
			int contentLen = 0;
			String range = mConn.getHeaderField("Content-Range");
			if (range != null) {
				int index = range.indexOf("/");
				if (index != -1) {
					contentLen = Integer.valueOf(range.substring(index + 1));
				}
			}
			if (contentLen == 0 && mNetErrorCode == HttpStatus.SC_OK) {
				String length = mConn.getHeaderField("Content-Length");
				if (length != null) {
					contentLen = Integer.valueOf(length);
				}
			}

			if (fileLen >= contentLen) {
				return true;
			}
			in = mConn.getInputStream();
			byte[] buf = new byte[BUFFERSIZE];
			int num = -1;
			int dataLen = 0;
			int notifyNum = 0;
			if (contentLen > 0) {
				notifyNum = contentLen / 50;
			}
			int notifyTmp = 0;
			if (handler != null && fileLen > 0) {
				handler.sendMessage(handler.obtainMessage(
						ConstantsUtil.MESSAGE_GET_LENGTH, (int) fileLen,
						contentLen));
			}
			while (!mIsInterrupte && (num = in.read(buf)) != -1) {
				try {
					fileStream.write(buf, 0, num);
				} catch (Exception e) {
					e.printStackTrace();
					throw new FileNotFoundException();
				}
				dataLen += num;
				notifyTmp += num;
				if (handler != null
						&& (notifyTmp > notifyNum || dataLen == contentLen)) {
					notifyTmp = 0;
					handler.sendMessage(handler.obtainMessage(
							ConstantsUtil.MESSAGE_GET_LENGTH,
							(int) (dataLen + fileLen), contentLen));
				}
			}
			try {
				fileStream.flush();
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new FileNotFoundException();
			}
			time = new Date().getTime() - time;
			if (dataLen + fileLen >= contentLen) {
				ret = true;
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			mNetErrorCode = NetworkErrorCode.FILE_WRITE_ERROR;
		} catch (Exception ex) {
			ex.printStackTrace();
			mNetErrorCode = NetworkErrorCode.NET_ERROR;
		} finally {
			mWapRetryConnt = 0;
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				if (mConn != null) {
					mConn.disconnect();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				if (fileStream != null) {
					fileStream.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return ret;
	}

	/**
	 * 设置context
	 * 
	 * @param context
	 *            上下文
	 */
	public void setContext(Context context) {
		mContext = context;
	}

	/**
	 * 获得context
	 * 
	 * @return context
	 */
	public Context getContext() {
		return mContext;
	}

	/**
	 * 获得post的数据数组
	 * 
	 * @return 数据
	 */
	public ArrayList<BasicNameValuePair> getPostData() {
		return mPostData;
	}

	/**
	 * 设置需要post的数据
	 * 
	 * @param mPostData
	 *            需要post的数据数组
	 */
	public void setPostData(ArrayList<BasicNameValuePair> postData) {
		if (mPostData != null) {
			mPostData.clear();
		}
		for (int i = 0; i < postData.size(); i++) {
			addPostData(postData.get(i));
		}
	}

	/**
	 * 添加需要post的数据
	 * 
	 * @param k
	 *            key值
	 * @param v
	 *            value值
	 */
	public void addPostData(String k, String v) {
		BasicNameValuePair data = new BasicNameValuePair(k, v);
		addPostData(data);
	}

	/**
	 * 添加需要post的数据
	 * 
	 * @param k
	 *            key值
	 * @param v
	 *            value值
	 */
	public void addPostData(String k, byte[] v) {
		if (mFileData == null) {
			mFileData = new HashMap<String, byte[]>();
		}
		mFileData.put(k, v);
	}

	/**
	 * 添加需要post的数据
	 * 
	 * @param data
	 *            NameValuePair格式数据
	 */
	public void addPostData(BasicNameValuePair data) {
		if (data == null || data.getName() == null) {
			return;
		}
		if (mPostData == null) {
			mPostData = new ArrayList<BasicNameValuePair>();
		}
		mPostData.add(data);
	}

	public void setIsBDImage(boolean isBDImage) {
		mIsBDImage = isBDImage;
	}

	public boolean getIsBDImage() {
		return mIsBDImage;
	}

	public static final boolean ping() {
		try {
			if (InetAddress.getByName("218.106.254.30").isReachable(500)) {
				return true;
			}
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		//
		// String result = null;
		//
		// try {
		//
		// String ip ="www.baidu.com";// 除非百度挂了，否则用这个应该没问题~
		//
		// Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " +
		// ip);//ping3次
		//
		//
		// // // 读取ping的内容，可不加。
		// //
		// // InputStream input = p.getInputStream();
		// //
		// // BufferedReader in = new BufferedReader(new
		// InputStreamReader(input));
		// //
		// // StringBuffer stringBuffer = new StringBuffer();
		// //
		// // String content = "";
		// //
		// // while ((content = in.readLine()) != null) {
		// //
		// // stringBuffer.append(content);
		// //
		// // }
		// //
		// // Log.i("TTT", "result content : " + stringBuffer.toString());
		//
		//
		// // PING的状态
		//
		// int status = p.waitFor();
		//
		// if (status == 0) {
		//
		// result = "successful~";
		//
		// return true;
		//
		// } else {
		//
		// result = "failed~ cannot reach the IP address";
		//
		// }
		//
		// } catch (IOException e) {
		//
		// result = "failed~ IOException";
		//
		// } catch (InterruptedException e) {
		//
		// result = "failed~ InterruptedException";
		//
		// } finally {
		//
		// Log.i("TTT", "result = " + result);
		//
		// }

		return false;

	}
}
