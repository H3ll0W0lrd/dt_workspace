package demo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.JavascriptInterface;
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
import com.example.location.R;

/**
 * html与定位相结合
 * 
 * @author dingtao
 *
 */
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
		/**
		 * 此链接为智慧图web地图链接，参数说明和使用文档可以联系智慧图web开发人员，或者发送到lbsmanage@rtmap.com
		 */
		mUrl = "http://maps.rtmap.com/latest/?key=d01XlE3Zpv&floor=%s&buildid=%s";// html页面

		mWebMap = (WebView) findViewById(R.id.web);
		WebSettings setting = mWebMap.getSettings();
		setting.setJavaScriptEnabled(true);// 支持js
		mWebMap.setScrollBarStyle(0);
		mWebMap.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		mWebMap.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {// 载入进度改变而触发
				handler.sendEmptyMessage(progress);// 如果全部载入,隐藏进度对话框
			}
		});

		mWebMap.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) { // 重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
				view.loadUrl(url);
				return true;
			}
		});

		/**
		 * 将本类对象注入JS变量SDK_Location，web地图将通过SDK_Location调用start()和stop()方法
		 */
		mWebMap.addJavascriptInterface(this, "SDK_Location");

		LocationApp.getInstance().registerLocationListener(this);
		LocationApp.getInstance().init(getApplicationContext());

		mGson = new Gson();
		mBuildId = "860100010040500017";
		mFloor = "F10";
		/**
		 * 加载地图
		 */
		mWebMap.loadUrl(String.format(mUrl, mFloor, mBuildId));
	}

	/**
	 * 必须有start()方法，因为web地图JavaScript需要调用
	 */
	@JavascriptInterface
	public void start() {
		boolean result = LocationApp.getInstance().start();
		/**
		 * 通知web地图定位是否成功开启
		 */
		mWebMap.loadUrl("javascript:Rtmap.Location._startHandler(" + result
				+ ")");
		if (result)
			Toast.makeText(this, "开始定位", Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this, "请输入key", Toast.LENGTH_LONG).show();
	}

	/**
	 * 必须有stop()方法，因为web地图JavaScript需要调用
	 */
	@JavascriptInterface
	public void stop() {
		// V2.3版本没有返回值，下个版本将对stop方法增加返回值
		// boolean result = LocationApp.getInstance().stop();
		LocationApp.getInstance().stop();
		/**
		 * 通知web地图定位是否成功关闭
		 */
		mWebMap.loadUrl("javascript:Rtmap.Location._stopHandler(" + true + ")");
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
			/**
			 * 将定位结果以json格式注入JS
			 */
			mWebMap.loadUrl("javascript:Rtmap.Location._setLocationData("
					+ mGson.toJson(location) + ")");
			if (!location.getBuildID().equals(mBuildId)
					|| !location.getFloor().equals(mFloor)) {
				mBuildId = location.getBuildID();
				mFloor = location.getFloor();
				stop();
				/**
				 * 切换web地图
				 */
				mWebMap.loadUrl(String.format(mUrl, mFloor, mBuildId));
			}
		}
		// Log.i("rtmap", "错误信息："+LocationApp.getInstance().getScannerInfo());
	}
}
