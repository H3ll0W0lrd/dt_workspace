package com.baidu.push.example;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.rtm.gf.R;

@SuppressLint({ "SetJavaScriptEnabled", "DefaultLocale" })
public class DTProtocalActivity extends Activity {
	private WebView web;
	private String url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dt_protocol);
		url = getIntent().getStringExtra("url");
		web = (WebView) findViewById(R.id.tb_protocol_web);
		WebSettings webSettings = web.getSettings();
		webSettings.setSavePassword(false);
		webSettings.setSaveFormData(false);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setAllowFileAccess(true); // 设置允许访问文件数据
		webSettings.setSupportZoom(false);
		web.loadUrl(url);
	}
}
