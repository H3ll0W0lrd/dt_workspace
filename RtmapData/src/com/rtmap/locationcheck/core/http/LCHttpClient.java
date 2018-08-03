package com.rtmap.locationcheck.core.http;

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
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.utils.RMStringUtils;
import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.exception.LCException;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTStringUtils;

public class LCHttpClient {

	public final static int PUT = 1;
	public final static int POST = 2;
	public final static int DELETE = 3;
	public final static int GET = 4;
	
	public static final Integer TIME_OUT = 10 * 1000;// 超时时间
	public static final String NET_ERROR = "net_error";// 连接失败

	/**
	 * 检测网络连接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkConnection() {
		ConnectivityManager connectivityManager = (ConnectivityManager) LCApplication
				.getInstance().getSystemService(
						LCApplication.getInstance().CONNECTIVITY_SERVICE);
		NetworkInfo networkinfo = connectivityManager.getActiveNetworkInfo();
		if (networkinfo != null) {
			return networkinfo.isAvailable();
		}
		return false;
	}

	private synchronized static DefaultHttpClient getHttpClient() {
		HttpParams params = new BasicHttpParams();
		/* 从连接池中取连接的超时时间 */
		ConnManagerParams.setTimeout(params, 60000);
		/* 连接超时 */
		HttpConnectionParams.setConnectionTimeout(params, 60000);
		/* 请求超时 */
		HttpConnectionParams.setSoTimeout(params, 60000);
		// 设置我们的HttpClient支持HTTP和HTTPS两种模式
		SchemeRegistry reg = new SchemeRegistry();
		SSLSocketFactory sf = null;
		ClientConnectionManager conMgr = null;
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);
			sf = new SSLSocketFactoryEx(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			reg.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			reg.register(new Scheme("https", sf, 443));
			// 使用线程安全的连接管理来创建HttpClient
			conMgr = new ThreadSafeClientConnManager(params, reg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new DefaultHttpClient(conMgr, params);
	}

	/**
	 * get方式请求
	 * 
	 * @param url
	 * @param keys
	 * @param values
	 * @return
	 * @throws LCException
	 */
	public static String getOrDelete(int method, String url, String[] keys,
			String[] values) throws LCException {
		if (!checkConnection()) {
			throw new LCException(R.string.net_error_code, R.string.net_error);
		}
		if (keys != null && keys.length != 0) {
			String params = "";
			for (int i = 0; i < keys.length; i++) {
				DTLog.i("key: " + keys[i] + "  values: " + values[i]);
				if (values[i] != null && !"".equals(values[i])
						&& !"0".equals(values[i]) && !"0.0".equals(values[i])
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
			if (params.length() > 1)
				params = params.substring(0, params.length() - 1);
			if (params != null && !params.equals(""))// 如果参数非空则拼接url
				url += ("?" + params);
		}
		DTLog.i(url);
		// 读取本地缓存，否则网络加载
		String result = LCHttpCache.loadFromLocal(url);
		if (!DTStringUtils.isEmpty(result))
			return result;
		HttpRequestBase request;
		if (GET == method) {
			request = new HttpGet(url);// 创建GET请求
		} else {
			request = new HttpDelete(url);// 创建DELETE请求
		}
		DefaultHttpClient client = getHttpClient();// 发送请求
		try {
			HttpResponse response = client.execute(request);
			DTLog.i("httpurl = " + url + "    response_code = "
					+ response.getStatusLine().getStatusCode());
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				request.abort();
				client.getConnectionManager().shutdown();// 释放连接
				throw new LCException(R.string.server_error_code,
						R.string.server_error);
			}
			result = EntityUtils.toString(response.getEntity(), "UTF-8");
			DTLog.i(result);
			LCHttpCache.saveToLocal(result, url);// 数据缓存到本地
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			client.getConnectionManager().shutdown();// 释放连接
			throw new LCException(R.string.server_error_code,
					R.string.server_error);
		}
	}

	/**
	 * 这个方法是为了适配外部接口（新浪微博，百度地图接口） 若不需要传递参数，则数组可为null
	 * 
	 * @param url
	 * @param keys
	 *            字段名数组集合
	 * @param values
	 *            参数值数组集合
	 * @return
	 * @throws LCException
	 */
	public static String post(String url, String[] keys, String[] values)
			throws LCException {
		if (!checkConnection()) {
			throw new LCException(R.string.net_error_code, R.string.net_error);
		}
		DefaultHttpClient client = getHttpClient();// 发送请求
		try {
			DTLog.i(url);
			HttpPost request = new HttpPost(url);// 创建POST请求
			if (keys != null) {// 请求参数判断
				List<NameValuePair> formparams = new ArrayList<NameValuePair>(); // 请求参数
				for (int i = 0; i < keys.length; i++) {
					if (values[i] != null && !"".equals(values[i])) {
						BasicNameValuePair pair = new BasicNameValuePair(
								keys[i], values[i]);
						DTLog.i("key:" + keys[i] + "    values:" + values[i]);
						formparams.add(pair);
					}
				}
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
						formparams, "UTF-8");// 使用Entity封装参数
				request.setEntity(entity);
			}
			HttpResponse response = client.execute(request);

			DTLog.i(String.format("request.uri=%s,response.statuscode=%d", url,
					response.getStatusLine().getStatusCode()));

			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				request.abort();
				client.getConnectionManager().shutdown();// 释放连接
				throw new LCException(R.string.server_error_code,
						R.string.server_error);
			}
			String str = EntityUtils.toString(response.getEntity(), "UTF-8");
			DTLog.i(str);// 打印返回信息
			return str;
		} catch (Exception e) {
			client.getConnectionManager().shutdown();// 释放连接
			throw new LCException(R.string.server_error_code,
					R.string.server_error);
		}
	}

	/**
	 * 若不需要传递参数，则数组可为null 这个方法适配的是内部接口
	 * 
	 * @param method
	 * @param url
	 * @param value
	 * @return
	 * @throws LCException
	 */
	private static String postOrPut(int method, String url, String value)
			throws LCException {
		if (!checkConnection()) {
			throw new LCException(R.string.net_error_code, R.string.net_error);
		}
		HttpEntityEnclosingRequestBase request;
		DefaultHttpClient client = getHttpClient();// 发送请求
		try {
			DTLog.i(url);
			DTLog.i("method : " + method);
			if (PUT == method) {
				request = new HttpPut(url);// 创建PUT请求
			} else {
				request = new HttpPost(url);// 创建POST请求
			}
			if (value != null) {// 请求参数判断
				DTLog.i("params :" + value);
				StringEntity entity = new StringEntity(value, "UTF-8");// 使用Entity封装参数
				request.setEntity(entity);
			}
			HttpResponse response = client.execute(request);

			DTLog.i(String.format("request.uri=%s,response.statuscode=%d", url,
					response.getStatusLine().getStatusCode()));

			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				request.abort();
				client.getConnectionManager().shutdown();// 释放连接
				throw new LCException(R.string.server_error_code,
						R.string.server_error);
			}
			String str = EntityUtils.toString(response.getEntity(), "UTF-8");
			DTLog.i(str);// 打印返回信息
			return str;
		} catch (Exception e) {
			e.printStackTrace();
			client.getConnectionManager().shutdown();// 释放连接
			throw new LCException(R.string.server_error_code,
					R.string.server_error);
		}
	}

	/**
	 * 上传文件
	 * 
	 * @param url
	 * @param file
	 * @return
	 * @throws LCException
	 */
	public static String postUpFile(String url, File file) throws LCException {
		if (!checkConnection()) {
			throw new LCException(R.string.net_error_code, R.string.net_error);
		}
		HttpClient client = getHttpClient();
		try {
			// 请求处理页面
			HttpPost httppost = new HttpPost(url);
			// 创建待处理的文件
			FileBody body = new FileBody(file);
			// 创建待处理的表单域内容文本
			// StringBody descript = new StringBody("multipart/form-data");

			// 对请求的表单域进行填充
			MultipartEntity reqEntity = new MultipartEntity();
			DTLog.i("file_name :  " + file.getAbsolutePath());
			reqEntity.addPart("uploadFile", body);
			// reqEntity.addPart("enctype", descript);
			httppost.setEntity(reqEntity);// 设置请求
			// 执行
			HttpResponse response = client.execute(httppost);

			DTLog.i(String.format("request.uri=%s,response.statuscode=%d", url,
					response.getStatusLine().getStatusCode()));

			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				httppost.abort();
				client.getConnectionManager().shutdown();// 释放连接
				throw new LCException(R.string.server_error_code,
						R.string.server_error);
			}
			String str = EntityUtils.toString(response.getEntity(), "UTF-8");
			DTLog.i(str);// 打印返回信息
			return str;
		} catch (Exception e) {
			e.printStackTrace();
			client.getConnectionManager().shutdown();// 释放连接
			throw new LCException(R.string.server_error_code,
					R.string.server_error);
		}
	}

	/**
	 * 下载文件
	 * 
	 * @param path
	 * @param url
	 * @return
	 */
	public static boolean downloadFile(String path, String url) {
		return downloadFile(path, url, null);
	}

	/**
	 * 下载文件
	 * 
	 * @param path
	 * @param url
	 * @return
	 */
	public static boolean downloadFile(String path, String url,
			ProgressListener listener) {

		try {
			DTLog.i("down_path: " + path);
			DTLog.i("down_url: " + url);
			HttpClient client = getHttpClient();
			HttpGet httpGet = new HttpGet(url);
			// 执行
			HttpResponse response = client.execute(httpGet);
			int code = response.getStatusLine().getStatusCode();
			DTLog.i(String.format("request.uri=%s,response.statuscode=%d", url,
					response.getStatusLine().getStatusCode()));
			if (code == HttpStatus.SC_OK) {
				File file = new File(path);
				if (!file.exists())
					file.createNewFile();

				HttpEntity entity = response.getEntity();
				final long size = entity.getContentLength();
				CountingInputStream cis = new CountingInputStream(
						entity.getContent(), size, listener);

				FileOutputStream output = new FileOutputStream(file);
				// 得到网络资源并写入文件
				byte b[] = new byte[1024];
				int j = 0;
				while ((j = cis.read(b)) != -1) {
					output.write(b, 0, j);
				}
				output.flush();
				output.close();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 无参数请求
	 * 
	 * @param method
	 * @param url
	 * @return
	 */
	public static String connInfo(int method, String url) {
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
	 */
	public static String connInfo(int method, String url, String[] keys,
			String[] values) {
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
			if (!RMStringUtils.isEmpty(params)) {// 如果参数非空则拼接url
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
	 */
	private static synchronized String getConnection(String url) {
		try {
			URL u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(60000);
			conn.setRequestProperty("Content-Type",
					"application/json;charset=UTF-8");
			conn.connect();
			// DTLog.i(TAG, url + " ; requestCode: " + conn.getResponseCode());
			if (conn.getResponseCode() == 200) {
				InputStream is = conn.getInputStream();
				return convertStreamToString(is);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return NET_ERROR;
	}

	/**
	 * post方式请求
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public static String postConnection(String url, String params) {
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
			if (!RMStringUtils.isEmpty(params)) {
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
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return NET_ERROR;
	}

	/**
	 * post方式请求
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public static String postHttpsConnection(String url, String params) {
		if (!url.endsWith("v1/lbslicense") && !RMHttpUrl.IS_LIS_PASS)
			return null;
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
			conn.setConnectTimeout(60000);
			conn.setRequestProperty("Content-Type",
					"application/json;charset=UTF-8");
			if (!RMStringUtils.isEmpty(params)) {
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
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		return NET_ERROR;
	}

	/**
	 * 
	 * @param file
	 * @param RequestURL
	 * @return
	 */
	public static String uploadFile(File file, String RequestURL) {
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
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return NET_ERROR;
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
	 */
	public static String downloadFileByHttpConnection(String strURL, String filePath) {
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
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "error"; // 自定义错误信息
	}

	/**
	 * 进度监听器接口
	 */
	public interface ProgressListener {
		public void transferred(long transferedBytes, long fileSize);
	}
}
