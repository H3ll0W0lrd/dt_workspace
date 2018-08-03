package com.rtmap.wifipicker.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
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

import com.rtmap.wifipicker.core.WPApplication;
import com.rtmap.wifipicker.data.ErrorData;

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

	private static final int MAX_RETRY_COUNT = 1;

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
		initPorxyUser();
	}

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
		} catch (Exception ex) {
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
					Cursor apn = WPApplication.getInstance()
							.getContentResolver()
							.query(uri, null, null, null, null);
					if (apn != null && apn.moveToNext()) {
						String name = apn.getString(apn.getColumnIndex("user"));
						String pwd = apn.getString(apn
								.getColumnIndex("password"));
						apn.close();
						String login = name + ":" + pwd;
						String encodedLogin = StringUtil.base64Encode(login
								.getBytes());
						sProxyUser = "Basic " + encodedLogin;
					}
				} catch (Exception ex) {
					ex.printStackTrace();

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
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		};
	}

	public NetworkCore() {
		initNetWork();
		mContext = WPApplication.getInstance();
	}

	public NetworkCore(String url) {
		initNetWork();
		mContext = WPApplication.getInstance();
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
			if (networkinfo == null)
				return null;
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
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
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
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 连接服务器
	 * 
	 * @param url
	 *            地址
	 * @return HttpURLConnection null表示失败
	 */
	private HttpURLConnection getConnect(URL url) {
		HttpURLConnection conn = null;
		NetworkState state = getNetworkState(mContext);
		if (state == null)
			return null;
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
		long time = 0;
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
				url = new URL(mUrl);
			}
			mConn = getConnect(url);
			if (mConn == null)
				return null;
			mConn.setConnectTimeout(CONNECTTIMEOUT);
			mConn.setReadTimeout(GETDATATIMEOUT);
			if (mRequestGzip && !mIsBDImage) {
				mConn.setRequestProperty("Accept-Encoding", "gzip");
			}
			if (mIsInterrupte)
				return null;
			time = new Date().getTime();
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
			time = new Date().getTime() - time;
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
			isNetError = true;
		} catch (java.net.SocketTimeoutException ex) {
			isNetError = true;
		} catch (Exception ex) {

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
		if (!mIsInterrupte && isNetError && mRetryConnt < MAX_RETRY_COUNT) {
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

	/**
	 * 解析服务器错误码
	 */
	public void parseServerCode(String data) {
		mServerErrorCode = -1;
		try {
			if (data != null) {
				ErrorData error = new ErrorData();
				error.parserJson(data);
				mServerErrorCode = error.getErrorCode();
				if (mServerErrorCode == -1) {
					mNetErrorCode = NetworkErrorCode.UNKNOWN_ERROR;
				} else if (mServerErrorCode != 0) {
					mNetErrorCode = error.getErrorCode();
				}
			}
		} catch (Exception ex) {
			mNetErrorCode = NetworkErrorCode.UNKNOWN_ERROR;
		}
	}

	/**
	 * 联网获得服务器返回的字符串
	 * 
	 * @return 字符串，应该先用isRequestSuccess判断是否请求成功，再使用返回的字符串
	 */
	public String getNetString() {
		byte[] data = getNetData();
		String retData = null;
		if (mNetErrorCode == HttpStatus.SC_OK) {
			try {
				String charset = getCharset();
				retData = new String(data, 0, data.length, charset);
				parseServerCode(retData);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return retData;
	}

	/**
	 * 以application/x-www-form-urlencoded格式post网络数据
	 * 
	 * @return 服务器返回的字符串
	 */
	public String postNetData() {
		ByteArrayOutputStream outputstream = null;
		byte[] output = null;
		InputStream in = null;
		String retData = null;
		long time = 0;
		boolean isNetError = false;
		try {
			URL url = new URL(mUrl);
			mConn = getConnect(url);
			if (mConn == null) {
				mNetErrorCode = NetworkErrorCode.NET_ERROR;
				return null;
			}

			mConn.setConnectTimeout(CONNECTTIMEOUT);
			mConn.setReadTimeout(POSTDATATIMEOUT);
			mConn.setDoOutput(true);
			mConn.setDoInput(true);
			mConn.setRequestMethod("POST");
			mConn.setRequestProperty("Charset", "UTF-8");
			mConn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");

			if (mRequestGzip) {
				mConn.setRequestProperty("Accept-Encoding", "gzip");
			}
			if (mIsInterrupte) {
				return null;
			}
			time = new Date().getTime();
			mConn.connect();
			DataOutputStream ds = new DataOutputStream(mConn.getOutputStream());
			BasicNameValuePair kv = null;
			StringBuffer build = new StringBuffer(BUFFERSIZE);

			int i = 0;
			int size = 0;
			if (mPostData != null) {
				size = mPostData.size();
			}
			for (i = 0; mPostData != null && i < size; i++) {
				kv = mPostData.get(i);
				if (kv == null) {
					continue;
				}
				String k = kv.getName();
				String v = kv.getValue();
				if (i != 0) {
					build.append("&");
				}
				build.append(k + "=");
				build.append(StringUtil.getUrlEncode(v));
			}

			String postdata = build.toString();
			if (!mIsInterrupte) {
				ds.writeBytes(postdata);
			}
			ds.flush();
			ds.close();

			mNetErrorCode = mConn.getResponseCode();
			if (mNetErrorCode != HttpStatus.SC_OK) {
				throw new java.net.SocketException();
			}

			if (mConn.getContentType().contains("text/vnd.wap.wml")
					&& mWapRetryConnt < 1) {
				mConn.disconnect();
				mWapRetryConnt++;
				mNetErrorCode = 0;
				return postNetData();
			} else {
				mWapRetryConnt = 0;
			}

			String encodeing = mConn.getContentEncoding();
			in = mConn.getInputStream();
			byte[] buf = new byte[BUFFERSIZE];
			int num = -1;
			outputstream = new ByteArrayOutputStream(BUFFERSIZE);
			while (!mIsInterrupte && (num = in.read(buf)) != -1) {
				outputstream.write(buf, 0, num);
			}
			in.close();
			mConn.disconnect();
			time = new Date().getTime() - time;
			output = outputstream.toByteArray();

			if (encodeing != null && encodeing.contains("gzip")) {
				ByteArrayInputStream tmpInput = new ByteArrayInputStream(output);
				ByteArrayOutputStream tmpOutput = new ByteArrayOutputStream(
						BUFFERSIZE);
				GzipHelper.decompress(tmpInput, tmpOutput);
				output = tmpOutput.toByteArray();
			}
			if (mNetErrorCode == HttpStatus.SC_OK) {
				String charset = getCharset();
				retData = new String(output, 0, output.length, charset);
				parseServerCode(retData);
			}
		} catch (java.net.SocketException ex) {
			mNetErrorCode = NetworkErrorCode.NET_ERROR;
			isNetError = true;
		} catch (java.net.SocketTimeoutException ex) {
			mNetErrorCode = NetworkErrorCode.NET_ERROR;
			isNetError = true;
		} catch (Exception ex) {
			mNetErrorCode = NetworkErrorCode.NET_ERROR;
		} finally {
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
			mWapRetryConnt = 0;
		}
		if (!mIsInterrupte && isNetError && mRetryConnt < MAX_RETRY_COUNT) {
			mRetryConnt++;
			return postNetData();
		} else {
			mRetryConnt = 0;
			return retData;
		}
	}

	/**
	 * 以multipart/form-data格式post网络数据
	 * 
	 * @return 服务器返回的字符串
	 */
	public String postMultiNetData() {
		ByteArrayOutputStream outputstream = null;
		byte[] output = null;
		InputStream in = null;
		String retData = null;
		long time = 0;
		boolean isNetError = false;
		DataOutputStream ds = null;
		try {
			URL url = new URL(mUrl);
			mConn = getConnect(url);
			if (mConn == null) {
				mNetErrorCode = NetworkErrorCode.NET_ERROR;
				return null;
			}

			mConn.setConnectTimeout(CONNECTTIMEOUT);
			mConn.setReadTimeout(POSTDATATIMEOUT);
			mConn.setDoOutput(true);
			mConn.setDoInput(true);
			mConn.setRequestMethod("POST");
			mConn.setRequestProperty("Charset", "UTF-8");
			mConn.setRequestProperty("Content-Type",
					"multipart/form-data; BOUNDARY=" + BOUNDARY);

			if (mRequestGzip) {
				mConn.setRequestProperty("Accept-Encoding", "gzip");
			}
			if (mIsInterrupte) {
				return null;
			}
			time = new Date().getTime();
			mConn.connect();
			ds = new DataOutputStream(mConn.getOutputStream());
			BasicNameValuePair kv = null;

			for (int i = 0; mPostData != null && i < mPostData.size()
					&& !mIsInterrupte; i++) {
				kv = mPostData.get(i);
				if (kv == null) {
					continue;
				}
				String k = kv.getName();
				String v = kv.getValue();
				ds.writeBytes(HYPENS + BOUNDARY + END);
				byte[] vbuffer = v.getBytes("UTF-8");
				ds.writeBytes("Content-Disposition: form-data; name=\"" + k
						+ "\"" + END);
				ds.writeBytes(END);
				ds.write(vbuffer);
				ds.writeBytes(END);
			}

			if (mFileData != null) {
				for (Entry<String, byte[]> entry : mFileData.entrySet()) {
					String k = entry.getKey();
					byte[] v = entry.getValue();
					if (mIsInterrupte) {
						break;
					}
					if (v == null) {
						continue;
					}
					ds.writeBytes(HYPENS + BOUNDARY + END);
					ds.writeBytes("Content-Disposition: form-data; name=\"" + k
							+ "\"; filename=\"file\"" + END);
					ds.writeBytes(END);
					ds.write(v);
					ds.writeBytes(END);
				}
			}

			ds.writeBytes(HYPENS + BOUNDARY + HYPENS + END);
			ds.flush();
			ds.close();

			// 发送大文件，使用wap模式会引起getResponseCode无法返回
			if (sHandler != null) {
				sHandler.sendMessageDelayed(sHandler.obtainMessage(0, this),
						POSTDATATIMEOUT * 3);
			}
			mNetErrorCode = mConn.getResponseCode();
			if (sHandler != null) {
				sHandler.removeMessages(0, this);
			}

			if (mNetErrorCode != HttpStatus.SC_OK) {
				throw new java.net.SocketException();
			}

			if (mConn.getContentType().contains("text/vnd.wap.wml")
					&& mWapRetryConnt < 1) {
				mConn.disconnect();
				mWapRetryConnt++;
				mNetErrorCode = 0;
				return postMultiNetData();
			} else {
				mWapRetryConnt = 0;
			}

			String encodeing = mConn.getContentEncoding();

			in = mConn.getInputStream();

			byte[] buf = new byte[BUFFERSIZE];
			int num = -1;
			outputstream = new ByteArrayOutputStream(BUFFERSIZE);
			while (!mIsInterrupte && (num = in.read(buf)) != -1) {
				outputstream.write(buf, 0, num);
			}
			in.close();
			mConn.disconnect();
			time = new Date().getTime() - time;
			output = outputstream.toByteArray();

			if (encodeing != null && encodeing.contains("gzip")) {
				ByteArrayInputStream tmpInput = new ByteArrayInputStream(output);
				ByteArrayOutputStream tmpOutput = new ByteArrayOutputStream(
						BUFFERSIZE);
				GzipHelper.decompress(tmpInput, tmpOutput);
				output = tmpOutput.toByteArray();
			}

			if (mNetErrorCode == HttpStatus.SC_OK) {
				String charset = getCharset();
				retData = new String(output, 0, output.length, charset);
				parseServerCode(retData);
			}
		} catch (java.net.SocketException ex) {
			isNetError = true;
			mNetErrorCode = NetworkErrorCode.NET_ERROR;
		} catch (java.net.SocketTimeoutException ex) {
			mNetErrorCode = NetworkErrorCode.NET_ERROR;
			isNetError = true;
		} catch (Exception ex) {
			mNetErrorCode = NetworkErrorCode.NET_ERROR;
		} finally {
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
				if (ds != null) {
					ds.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (sHandler != null) {
				sHandler.removeMessages(0, this);
			}
			ds = null;
			mWapRetryConnt = 0;
		}
		if (!mIsInterrupte && isNetError && mRetryConnt < MAX_RETRY_COUNT) {
			mRetryConnt++;
			return postMultiNetData();
		} else {
			mRetryConnt = 0;
			return retData;
		}
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
	public Boolean downloadFile(String path, Handler handler) {
		InputStream in = null;
		Boolean ret = false;
		FileOutputStream fileStream = null;
		try {
			URL url = new URL(mUrl);
			mConn = getConnect(url);
			if (mConn == null) {
				return false;
			}
			mConn.setConnectTimeout(CONNECTTIMEOUT);
			mConn.setReadTimeout(GETDATATIMEOUT);
			mConn.setInstanceFollowRedirects(false);
			if (mIsInterrupte) {
				return false;
			}
			DTLog.e("url : " + mUrl + "   path:" + path);
			mConn.connect();
			mNetErrorCode = mConn.getResponseCode();
			DTLog.i("mNetError : "+mNetErrorCode);
			if (mNetErrorCode == HttpStatus.SC_OK) {
				File file = FileHelper.createFileIfNotFound(path);
				if (file == null) {
					throw new FileNotFoundException();
				}
				fileStream = new FileOutputStream(file, true);
				in = mConn.getInputStream();
				byte[] buf = new byte[BUFFERSIZE];
				int num = -1;
				while (!mIsInterrupte && (num = in.read(buf)) != -1) {
					fileStream.write(buf, 0, num);
				}
				fileStream.flush();
				fileStream.close();
				in.close();
				ret = true;
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
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

}
