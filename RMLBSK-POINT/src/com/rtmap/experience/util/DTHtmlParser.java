package com.rtmap.experience.util;

import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.webkit.WebView;

@SuppressLint("NewApi")
public abstract class DTHtmlParser extends AsyncTask<Void, Void, String> {

	private String mUrl;
	private WebView webView;
	
	public DTHtmlParser(WebView wevView, String url, Context context) {
		this.webView = wevView;
		mUrl = url;
	}

	@Override
	protected String doInBackground(Void... params) {

		Document doc = null;
		try {
			doc = Jsoup.parse(new URL(mUrl), 15000);
		} catch (Exception e) {
		}
		if(doc == null)
			return null;

		Elements es = doc.select("script");
		if(es != null){
			es.remove();
		}
		String htmlText = handleDocument(doc);//得到html文本
		return htmlText;
	}
	protected abstract String handleDocument(Document doc);

	@Override
	protected void onPostExecute(String result) {
		webView.loadDataWithBaseURL(null, result, "text/html", "utf-8", null);//重新加载html
		super.onPostExecute(result);
	}
	
}
