package com.rtmap.experience.page;

import org.jsoup.nodes.Document;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.rtmap.experience.R;
import com.rtmap.experience.core.KPActivity;
import com.rtmap.experience.util.DTHtmlParser;
import com.rtmap.experience.util.DTLog;

@SuppressLint({ "SetJavaScriptEnabled", "DefaultLocale" })
public class KProtocalActivity extends KPActivity {
	private WebView web;
	private TextView mTitle;
	private static String url;
	private static String title;

	/**
	 * 
	 * @param context
	 * @param mUrl
	 *            html路径
	 * @param mTitleId
	 *            标题
	 * @param bannerName
	 *            话题名称
	 */
	public static void interActivity(Activity context, String mUrl, String t) {
		url = mUrl;
		title = t;
		Intent intent = new Intent(context, KProtocalActivity.class);
		context.startActivity(intent);
	}

	@SuppressLint("JavascriptInterface")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kp_protocol);
		mTitle = (TextView) findViewById(R.id.title);

		mTitle.setText(title);
		web = (WebView) findViewById(R.id.tb_protocol_web);
		web.getSettings().setJavaScriptEnabled(true);
		web.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
		new DTHtmlParser(web, url, this) {// 随便找了个带图片的网站

			@Override
			protected String handleDocument(Document doc) {
				return doc.html();
			}
		}.execute();
		web.addJavascriptInterface(new JavascriptInterface(), "publish");// 添加js交互接口类，并起别名
																			// imagelistner
		web.setWebViewClient(new MyWebViewClient());

	}

	/**
	 * js通信接口
	 * 
	 * @author dingtao
	 * 
	 */
	public class JavascriptInterface {
		public void handlePublish(String url) {
			DTLog.i("点击" + url);
		}
	}

	/**
	 * 监听webview中的超链接动作
	 * 
	 * @author dingtao
	 * 
	 */
	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return true;// 设置为true，超链接不在加载页面，反之。
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			view.getSettings().setJavaScriptEnabled(true);
			DTLog.i("onPageFinished-url:" + url);
			addHrefClickListner();// html加载完成之后，添加监听href的点击js函数
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			view.getSettings().setJavaScriptEnabled(true);
		}
	}

	/**
	 * 注入js函数监听超链接点击
	 */
	private void addHrefClickListner() {
		// 这段js函数的功能就是，遍历所有的img几点，并添加onclick函数，在还是执行的时候调用本地接口传递url过去
		web.loadUrl("javascript:(function(){"
				+ "var objs = document.getElementsByTagName(\"a\"); "
				+ "for(var i=0;i<objs.length;i++)  " + "{"
				+ "    objs[i].onclick=function()  " + "    {  "
				+ "        window.publish.handlePublish(this.href);  "// 使用publish调用js接口
				+ "    }  " + "}" + "})()");
	}

}
