package com.rtmap.wisdom.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.rtmap.wisdom.R;
import com.rtmap.wisdom.core.DTApplication;
import com.rtmap.wisdom.exception.DTException;
import com.rtmap.wisdom.util.DTLog;
import com.rtmap.wisdom.util.DTStringUtil;

public class DTHttpUtil {

	public final static int PUT = 1;
	public final static int POST = 2;
	public final static int DELETE = 3;
	public final static int GET = 4;

	public static final Integer TIME_OUT = 10 * 1000;// 超时时间

	/**
	 * 检测网络连接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkConnection() {
		ConnectivityManager connectivityManager = (ConnectivityManager) DTApplication
				.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null) {
			return networkInfo.isAvailable();
		}
		return false;
	}

	/**
	 * 无参数请求
	 * 
	 * @param method
	 * @param url
	 * @return
	 * @throws DTException
	 */
	public static String connInfo(int method, String url) throws DTException {
		return connInfo(method, url, null, null);
	}

	/**
	 * 带参数请求
	 * 
	 * @param method
	 * @param url
	 * @param keys
	 * @param values
	 * @return
	 * @throws DTException
	 */
	public static String connInfo(int method, String url, String[] keys,
			String[] values) throws DTException {
		if (method == GET) {
			String params = "";
			if (keys != null && keys.length != 0) {
				for (int i = 0; i < keys.length; i++) {
					if (values[i] != null && !"".equals(values[i])
							&& !"0".equals(values[i])
							&& !"0.0".equals(values[i])
							&& !"null".equals(values[i])) {// 进行参数拼接
						try {
							params += keys[i] + "="
									+ URLEncoder.encode(values[i], "utf-8");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						params += "&";
					}
				}
				if (params.length() > 1) {
					params = params.substring(0, params.length() - 1);
				}
			}
			if (!DTStringUtil.isEmpty(params)) {// 如果参数非空则拼接url
				url += ("?" + params);
			}
			return getConnection(url);
		} else {
			JSONObject json = new JSONObject();
			if (keys != null && keys.length != 0) {
				for (int i = 0; i < keys.length; i++) {
					if (values[i] != null && !"".equals(values[i])
							&& !"0".equals(values[i])
							&& !"0.0".equals(values[i])
							&& !"null".equals(values[i])) {// 进行参数拼接
						try {
							// json.put(keys[i],
							// URLEncoder.encode(values[i], "utf-8"));
							json.put(keys[i], values[i]);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			}
			return postConnection(url, json.toString());
		}
	}

	/**
	 * Get方式请求
	 * 
	 * @param url
	 * @return
	 * @throws DTException
	 */
	private static String getConnection(String url) throws DTException {
		try {
			URL u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(60000);
			conn.setRequestProperty("Content-Type",
					"application/json;charset=UTF-8");
			conn.connect();
			DTLog.i(url + " ; requestCode: " + conn.getResponseCode());
			if (conn.getResponseCode() == 200) {
				InputStream is = conn.getInputStream();
				return convertStreamToString(is);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DTException(R.string.server_error_code,
					R.string.server_error);
		}
		return null;
	}

	/**
	 * post方式请求
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @throws DTException
	 */
	public static String postConnection(String url, String params)
			throws DTException {
		try {
			URL u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setConnectTimeout(60000);
			conn.setRequestProperty("Content-Type",
					"application/json;charset=UTF-8");
			if (!DTStringUtil.isEmpty(params)) {
				// DTLog.i(TAG, "post-params: " + params);
				OutputStreamWriter osw = new OutputStreamWriter(
						conn.getOutputStream(), "UTF-8");
				osw.write(params);
				osw.flush();
				osw.close();
			} else {
				conn.connect();
			}
			// DTLog.i(TAG, url + " ; requestCode: " + conn.getResponseCode());
			if (conn.getResponseCode() == 200) {
				InputStream is = conn.getInputStream();
				return convertStreamToString(is);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DTException(R.string.server_error_code,
					R.string.server_error);
		}
		return null;
	}

	/**
	 * post方式请求
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @throws DTException
	 */
	public static String postHttpsConnection(String url, String params)
			throws DTException {
		try {
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs,
						String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs,
						String authType) {
				}
			} };

			// Install the all-trusting trust manager

			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, null);
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());

			URL u = new URL(url);
			HttpsURLConnection conn = (HttpsURLConnection) u.openConnection();
			conn.setSSLSocketFactory(sc.getSocketFactory());
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setConnectTimeout(TIME_OUT);
			conn.setRequestProperty("Content-Type",
					"application/json;charset=UTF-8");
			if (!DTStringUtil.isEmpty(params)) {
				// DTLog.i(TAG, "post-params: " + params);
				OutputStreamWriter osw = new OutputStreamWriter(
						conn.getOutputStream(), "UTF-8");
				osw.write(params);
				osw.flush();
				osw.close();
			} else {
				conn.connect();
			}
			// DTLog.i(TAG, url + " ; requestCode: " + conn.getResponseCode());
			if (conn.getResponseCode() == 200) {
				InputStream is = conn.getInputStream();
				return convertStreamToString(is);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DTException(R.string.server_error_code,
					R.string.server_error);
		}
		return null;
	}

	/**
	 * 
	 * @param file
	 * @param RequestURL
	 * @return
	 * @throws DTException
	 */
	public static String uploadFile(File file, String RequestURL)
			throws DTException {
		String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
		String header = "--"
				+ BOUNDARY
				+ ""
				+ "\r\nContent-Disposition: form-data; name=\"sdklog\"; filename=\""
				+ file.getName() + "\""
				+ "\r\nContent-Type: application/x-zip-compressed"
				+ "\r\nContent-Transfer-Encoding: binary\r\n\r\n";
		String footer = "\r\n--" + BOUNDARY + "--\r\n";
		try {
			URL url = new URL(RequestURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10 * 1000);
			conn.setDoInput(true); // 允许输入流
			conn.setDoOutput(true); // 允许输出流
			conn.setUseCaches(false); // 不允许使用缓存
			conn.setRequestMethod("POST"); // 请求方式
			conn.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + BOUNDARY
							+ "; charset=UTF-8");
			if (file != null) {
				/** * 当文件不为空，把文件包装并且上传 */
				OutputStream outputSteam = conn.getOutputStream();
				DataOutputStream dos = new DataOutputStream(outputSteam);
				dos.write(header.getBytes());
				InputStream is = new FileInputStream(file);
				byte[] bytes = new byte[1024];
				int len = 0;
				while ((len = is.read(bytes)) != -1) {
					dos.write(bytes, 0, len);
				}
				dos.write(footer.getBytes());
				is.close();
				dos.flush();
				dos.close();
				if (conn.getResponseCode() == 200) {
					InputStream ias = conn.getInputStream();
					return convertStreamToString(ias);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DTException(R.string.server_error_code,
					R.string.server_error);
		}
		return null;
	}

	/**
	 * 将InputStream转换成某种字符编码的String
	 * 
	 * @param in
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public synchronized static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		DTLog.i("result : " + sb.toString());
		return sb.toString();
	}

	/**
	 * 下载文件
	 * 
	 * @param strURL
	 *            下载地址
	 * @param filePath
	 *            文件保存路径
	 * @return 错误信息
	 * @throws DTException
	 */
	public static String downloadFileByHttpConnection(String strURL,
			String filePath) throws DTException {
		try {
			URL url = new URL(strURL);// 创建连接
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.setUseCaches(true);
			connection.setConnectTimeout(TIME_OUT);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestMethod("GET"); // 设置请求方式
			connection.connect();

			// 读取响应
			int length = (int) connection.getContentLength();// 获取长度
			InputStream is = connection.getInputStream();
			if (length != -1) {
				File file = new File(filePath);
				if (length == 0) {
					if (file.exists()) {
						file.delete();
					}
					file.createNewFile();
				} else {
					FileOutputStream output = new FileOutputStream(file);
					// 得到网络资源并写入文件
					byte b[] = new byte[4096];
					int j = 0;
					while ((j = is.read(b)) != -1) {
						output.write(b, 0, j);
					}
					output.flush();
					output.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DTException(R.string.server_error_code,
					R.string.server_error);
		}
		return null;
	}

	/**
	 * 进度监听器接口
	 */
	public interface ProgressListener {
		public void transferred(long transferedBytes, long fileSize);
	}
}
