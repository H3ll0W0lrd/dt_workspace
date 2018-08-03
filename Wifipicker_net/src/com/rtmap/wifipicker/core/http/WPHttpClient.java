package com.rtmap.wifipicker.core.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

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

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.rtmap.wifipicker.R;
import com.rtmap.wifipicker.core.WPApplication;
import com.rtmap.wifipicker.core.exception.RMException;
import com.rtmap.wifipicker.util.DTLog;
import com.rtmap.wifipicker.util.DTStringUtils;

public class WPHttpClient {

	public final static String PUT = "put";
	public final static String POST = "post";
	public final static String DELETE = "delete";
	public final static String GET = "get";

	/**
	 * 检测网络连接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkConnection() {
		ConnectivityManager connectivityManager = (ConnectivityManager) WPApplication
				.getInstance().getSystemService(
						WPApplication.getInstance().CONNECTIVITY_SERVICE);
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
	 * @throws RMException
	 */
	public static String getOrDelete(String method, String url, String[] keys,
			String[] values) throws RMException {
		if (!checkConnection()) {
			throw new RMException(R.string.net_error_code, R.string.net_error);
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
		String result = RMHttpCache.loadFromLocal(url);
		if (!DTStringUtils.isEmpty(result))
			return result;
		HttpRequestBase request;
		if (GET.equals(method)) {
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
				throw new RMException(R.string.server_error_code,
						R.string.server_error);
			}
			result = EntityUtils.toString(response.getEntity(), "UTF-8");
			DTLog.i(result);
			RMHttpCache.saveToLocal(result, url);// 数据缓存到本地
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			client.getConnectionManager().shutdown();// 释放连接
			throw new RMException(R.string.server_error_code,
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
	 * @throws RMException
	 */
	public static String post(String url, String[] keys, String[] values)
			throws RMException {
		if (!checkConnection()) {
			throw new RMException(R.string.net_error_code, R.string.net_error);
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
				throw new RMException(R.string.server_error_code,
						R.string.server_error);
			}
			String str = EntityUtils.toString(response.getEntity(), "UTF-8");
			DTLog.i(str);// 打印返回信息
			return str;
		} catch (Exception e) {
			client.getConnectionManager().shutdown();// 释放连接
			throw new RMException(R.string.server_error_code,
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
	 * @throws RMException
	 */
	private static String postOrPut(String method, String url, String value)
			throws RMException {
		if (!checkConnection()) {
			throw new RMException(R.string.net_error_code, R.string.net_error);
		}
		HttpEntityEnclosingRequestBase request;
		DefaultHttpClient client = getHttpClient();// 发送请求
		try {
			DTLog.i(url);
			DTLog.i("method : " + method);
			if (PUT.equals(method)) {
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
				throw new RMException(R.string.server_error_code,
						R.string.server_error);
			}
			String str = EntityUtils.toString(response.getEntity(), "UTF-8");
			DTLog.i(str);// 打印返回信息
			return str;
		} catch (Exception e) {
			e.printStackTrace();
			client.getConnectionManager().shutdown();// 释放连接
			throw new RMException(R.string.server_error_code,
					R.string.server_error);
		}
	}

	/**
	 * 上传文件
	 * 
	 * @param url
	 * @param file
	 * @return
	 * @throws RMException
	 */
	public static String postUpFile(String url, File file) throws RMException {
		if (!checkConnection()) {
			throw new RMException(R.string.net_error_code, R.string.net_error);
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
				throw new RMException(R.string.server_error_code,
						R.string.server_error);
			}
			String str = EntityUtils.toString(response.getEntity(), "UTF-8");
			DTLog.i(str);// 打印返回信息
			return str;
		} catch (Exception e) {
			e.printStackTrace();
			client.getConnectionManager().shutdown();// 释放连接
			throw new RMException(R.string.server_error_code,
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
				FileOutputStream output = new FileOutputStream(file);
				// 得到网络资源并写入文件
				InputStream input = response.getEntity().getContent();
				byte b[] = new byte[1024];
				int j = 0;
				while ((j = input.read(b)) != -1) {
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
}
