package com.rtm.frm.net;

import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.ToastUtil;
import com.rtm.frm.utils.XunluUtil;

/**
 * @author liyan 服务器异步请求任务
 * 若网络有问题，msg.arg1 会有网络STATE_NET_ERR_UNUSED标志
 */
public class PostAsyncTask extends
		AsyncTask<List<BasicNameValuePair>, Void, String> {

	private String mUrl;
	
	private Handler mHandler;
	
	private int mMsgWhat;
	
	/**
	 * 请求task构造函数
	 * @param urlStr
	 * @param handler
	 * @param what 
	 * 若网络有问题，msg.arg1 会有网络STATE_NET_ERR_UNUSED标志
	 */
	public PostAsyncTask(String urlStr,Handler handler,int what) {
		mUrl = urlStr;
		mHandler = handler;
		mMsgWhat = what;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPreExecute() 对UI做一些标记
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[]) 后台操作 List参数规则，第0个为URL
	 */
	@Override
	protected String doInBackground(List<BasicNameValuePair>... lists) {
		String result = "";
		if(NetWorkPost.detectInter(XunluApplication.mApp)){
			result = NetWorkPost.postData(lists[0], mUrl);
		} else {
			Message msg = new Message();
			msg.what = mMsgWhat;
			msg.arg1 = ConstantsUtil.STATE_NET_ERR_UNUSED;
			mHandler.sendMessage(msg);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object) 后台操作完成，结果返回
	 */
	@Override
	protected void onPostExecute(String result) {
		if(!XunluUtil.isEmpty(result)){
			Message msg = new Message();
			msg.what = mMsgWhat;
			msg.obj = result;
			mHandler.sendMessage(msg);
		} else {
			ToastUtil.showToast(R.string.toast_post_net_exception, true);
		}
		super.onPostExecute(result);
	}
}
