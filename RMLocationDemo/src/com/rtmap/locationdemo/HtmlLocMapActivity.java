package com.rtmap.locationdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.RMLog;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;
import com.rtmap.locationdemo.beta.R;

public class HtmlLocMapActivity extends Activity implements RMLocationListener {

	private WebView mWebMap;
	private ProgressBar mProBar;
	private Gson mGson;
	private String mBuildId, mFloor;
	private String mUrl;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {// 定义一个Handler，用于处理下载线程与UI间通讯
			if (!Thread.currentThread().isInterrupted()) {
				mProBar.setProgress(msg.what);
				if (msg.what == 100) {
					mProBar.setVisibility(View.GONE);// 隐藏进度对话框，不可使用dismiss()、cancel(),否则再次调用show()时，显示的对话框小圆圈不会动。
				} else {
					mProBar.setVisibility(View.VISIBLE);
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.html_loc_map);

		mProBar = (ProgressBar) findViewById(R.id.progress);
		mUrl = "http://maps.rtmap.com/V3.2/?key=d01XlE3Zpv&floor=%s&buildid=%s";
//http://maps.rtmap.com/V3.2/?key=mDhHVdepiw&buildid=860100010040500017&floor=F10
		mWebMap = (WebView) findViewById(R.id.web);
		WebSettings setting = mWebMap.getSettings();
		setting.setJavaScriptEnabled(true);// 支持js
		mWebMap.setScrollBarStyle(0);
		mWebMap.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		mWebMap.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {// 载入进度改变而触发
				Log.i("dt", "   " + progress);
				handler.sendEmptyMessage(progress);// 如果全部载入,隐藏进度对话框
			}
		});

		mWebMap.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) { // 重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
				view.loadUrl(url);
				return true;
			}
		});

		mWebMap.addJavascriptInterface(this, "SDK_Location");

		LocationApp.getInstance().init(getApplicationContext());
		LocationApp.getInstance().registerLocationListener(this);
		RMLog.LOG_LEVEL = RMLog.LOG_LEVEL_INFO;
		LocationApp.getInstance().setUseRtmapError(true);

		mGson = new Gson();
		mBuildId = "860100010040500017";
		mFloor = "F10";
		mWebMap.loadUrl(String.format(mUrl, mFloor, mBuildId));
	}

	// SDK_Location.start();
	// SDK_Location.stop();

	// Rtmap.Location._startHandler(msgString);
	// Rtmap.Location._stopHandler(msgString);
	// Rtmap.Location._setLocationData(jsonDataString);

	public void start() {
		boolean result = LocationApp.getInstance().start();
		mWebMap.loadUrl("javascript:Rtmap.Location._startHandler(" + result
				+ ")");
		if (result)
			Toast.makeText(this, "开始定位", Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this, "请输入key", Toast.LENGTH_LONG).show();
	}

	public void stop() {
		boolean result = LocationApp.getInstance().stop();
		mWebMap.loadUrl("javascript:Rtmap.Location._stopHandler(" + result
				+ ")");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stop();
		LocationApp.getInstance().unRegisterLocationListener(this);
	}

	@Override
	public void onReceiveLocation(RMLocation location) {
		RMLog.i("rtmap", "\nerror: " + location.getError() + "\nbuild: "
				+ location.getBuildID() + "\nfloor: " + location.getFloor()
				+ "\nx: " + location.getX() + "\ny:  " + location.getY()
				+ "\n精度（米）:" + location.getAccuracy());
		RMLog.i("rtmap", mGson.toJson(location));
		if (location.getError() == 0) {
			mWebMap.loadUrl("javascript:Rtmap.Location._setLocationData("
					+ mGson.toJson(location) + ")");
			if (!location.getBuildID().equals(mBuildId)
					|| !location.getFloor().equals(mFloor)) {
				mBuildId = location.getBuildID();
				mFloor = location.getFloor();
				stop();
				mWebMap.loadUrl(String.format(mUrl, mFloor, mBuildId));
			}
		}
		// Log.i("rtmap", "错误信息："+LocationApp.getInstance().getScannerInfo());
	}
}
