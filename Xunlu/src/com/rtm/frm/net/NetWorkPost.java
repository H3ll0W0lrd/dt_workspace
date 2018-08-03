package com.rtm.frm.net;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetWorkPost {
	/**
	 * 判断网络是否可用
	 * 
	 * @param act
	 * @return
	 */
	public static boolean detectInter(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null) {
			return false;
		}
		NetworkInfo networkinfo = manager.getActiveNetworkInfo();

		if (networkinfo == null) {
			return false;
		} else {
			boolean isAvailable = networkinfo.isAvailable();
			boolean connected = networkinfo.isConnected();
			if (isAvailable) {
				return true;
			}
			if (connected) {
				return true;
			}
			return false;
		}
	}
	
	/**
	 * @author liYan
	 * @version  创建时间：2014-8-14 上午11:25:20
	 * @explain 请求数据
	 * @param data
	 * @param urlString
	 * @return resultString
	 */
	public static String postData(List<BasicNameValuePair> data, String urlString) {
		String resultString = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(urlString);
			httpPost.setEntity(new UrlEncodedFormEntity(data, "utf-8"));
			client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1000*8);
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 1000*8);
			HttpResponse response = client.execute(httpPost);
			// 若状态码为200 ok
			if (response.getStatusLine().getStatusCode() == 200) {
				// 取出回应字串
				resultString = EntityUtils.toString(response.getEntity());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultString;
	}

}
