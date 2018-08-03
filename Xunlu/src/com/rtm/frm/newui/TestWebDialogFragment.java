/**
 * @date 2014.08.18 21:15
 * @explain 百度地图页搜索功能
 */
package com.rtm.frm.newui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.rtm.frm.R;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.model.FavorablePoiDbModel;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.tab1.ThumbnailMapFragment;
import com.rtm.frm.view.NavTitle;
import com.umeng.analytics.MobclickAgent;

@SuppressLint("HandlerLeak")
public class TestWebDialogFragment extends BaseFragment implements OnTouchListener {
	private String SAVED_URL = "saved_url";
	private String SAVED_TITLE = "saved_title";

	private NavTitle mNavTitle;

	private String mUrl;

	private WebView mWebView;

	// private ProgressBar mLoadingProgressBar;

	private String mTitleString;

	private Handler mHandler = new Handler();
	
	private FavorablePoiDbModel mFavorablePoiDbModel;

	public TestWebDialogFragment() {

	}

	public TestWebDialogFragment(String url, String title) {
		mUrl = url;
		mTitleString = title;
//		this.setStyle(DialogFragment.STYLE_NORMAL, R.style.dialogfragment_transparent_bg);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle out) {
		if(out != null){
			mUrl = out.getString(SAVED_URL);
			mTitleString = out.getString(SAVED_TITLE);
		}
		return inflater.inflate(R.layout.fragment_web_dialog, null);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		initView(view);
	}

	@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
	private void initView(View contentView) {
		mNavTitle = (NavTitle) contentView.findViewById(R.id.nav_title);
		mNavTitle.unRegisterReceiver();
		mNavTitle.setTitleText(mTitleString);
		mNavTitle.setLeftOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MobclickAgent.onEvent(mContext,"event_click_detail_back");
				
				if(mWebView.canGoBack()){
					mWebView.goBack();
				} else {
//					TestWebDialogFragment.this.dismiss();
					MyFragmentManager.getInstance().backFragment();
				}
			}
		});

		mWebView = (WebView) contentView.findViewById(R.id.web_view);
		showWebView();
		contentView.setOnTouchListener(this);
	}
	@SuppressLint("SetJavaScriptEnabled")
	private void showWebView(){		// webView与js交互代码
		try {
			
			mWebView.requestFocus();
			
			mWebView.setWebChromeClient(new WebChromeClient(){
				@Override
				public void onProgressChanged(WebView view, int progress){
				}
			});
			
			mWebView.setOnKeyListener(new View.OnKeyListener() {		// webview can go back
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if(keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
						mWebView.goBack();
						return true;
					}
					return false;
				}
			});
			
			WebSettings webSettings = mWebView.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setDefaultTextEncodingName("utf-8");

			mWebView.addJavascriptInterface(getHtmlObject(), "jsObj");
//			mWebView.loadUrl("http://192.168.1.5:8080/androidClick/index.html");
			mWebView.loadUrl(mUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Object getHtmlObject(){
		Object insertObj = new Object(){
			@JavascriptInterface
			public void goToMap(){
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
//						try {
//							POI poi = new POI(Integer.valueOf(mFavorablePoiDbModel.poiId), mFavorablePoiDbModel.poiName);
//							poi.setBigPhoto(mFavorablePoiDbModel.adBigUrl);
//							poi.setPhoto(mFavorablePoiDbModel.adUrl);
//							poi.setBuildId(mFavorablePoiDbModel.buildId);
//							poi.setFloor(mFavorablePoiDbModel.floor);
//							poi.setNumber(Integer.valueOf(mFavorablePoiDbModel.number));
//							poi.setIntro(mFavorablePoiDbModel.discription);
//							poi.setX(Float.valueOf(mFavorablePoiDbModel.poiX));
//							poi.setY(Float.valueOf(mFavorablePoiDbModel.poiY));
//							
//							Build build = DBOperation.getInstance().queryBuildById(mFavorablePoiDbModel.buildId);
//							if(build != null) {
//								NewFrameActivity.getInstance().getTab0().showSearchPoiPoint(poi,build.name);
//							}
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//						TestWebDialogFragment.this.dismiss();
						ThumbnailMapFragment thumbnailMapFragment = new ThumbnailMapFragment(mFavorablePoiDbModel);
						MyFragmentManager.getInstance().addFragment(NewFrameActivity.ID_ALL, thumbnailMapFragment, MyFragmentManager.PROCESS_THUMBNAIL_MAP, MyFragmentManager.FRAGMENT_THUMBNAIL_MAP);
					}
				});
			}
			@JavascriptInterface
			public void htmlCancel(){
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
//						TestWebDialogFragment.this.dismiss();
						MyFragmentManager.getInstance().backFragment();
					}
				});
			}
		};
		
		return insertObj;
	}
	
	public void setFavorablePoiDbModel(FavorablePoiDbModel favorablePoiDbModel) {
		mFavorablePoiDbModel = favorablePoiDbModel;
	}
	
	@Override
	public void onDestroy() {
		mWebView.clearCache(true);
		super.onDestroy();
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		return true;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(SAVED_URL, mUrl);
		outState.putString(SAVED_TITLE, mTitleString);
		super.onSaveInstanceState(outState);
	}
}


