package com.rtm.frm.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.utils.XunluUtil;

public class SplashActivity extends Activity {
	TextView tvVision;

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Intent i = new Intent(SplashActivity.this, NewFrameActivity.class);
			startActivity(i);
			finish();
		};
	};

	// private ImageView planeView;
	//
	// private TextView versionView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_splash);
		// planeView = (ImageView) findViewById(R.id.plane);
		// versionView = (TextView) findViewById(R.id.version);
		// String version = XunluApplication.getApp().getCurrentVersion();
		// versionView.setText("正式版"+version);
		// initAppDefaultSet();
		tvVision = (TextView) findViewById(R.id.tv_vision);
		try {
			tvVision.setText("RTMAP V"+XunluUtil.getVersionName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		mHandler.sendEmptyMessageDelayed(1, 3000);
	}

	// private void delay(ImageView planeView) {
	// AnimationSet as = new AnimationSet(true);
	// DisplayMetrics dm = new DisplayMetrics();
	// // 取得窗口属性
	// getWindowManager().getDefaultDisplay().getMetrics(dm);
	//
	// float w = dm.widthPixels;
	// float h = dm.heightPixels - XunluUtil.dip2px(this, 70);
	// TranslateAnimation al = new TranslateAnimation(0, w, h, 0);
	// al.setDuration(2000);
	// as.addAnimation(al);
	// as.setFillAfter(true);
	// as.setAnimationListener(new AnimationListener() {
	//
	// @Override
	// public void onAnimationStart(Animation animation) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onAnimationRepeat(Animation animation) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onAnimationEnd(Animation animation) {
	// // Intent mapIntent = new Intent(SplashActivity.this,MapActivity.class);
	// // SplashActivity.this.startActivity(mapIntent);
	// // SplashActivity.this.finish();
	// }
	// });
	// planeView.startAnimation(as);
	// }

	@Override
	protected void onResume() {
		super.onResume();
		// new Handler().post(new Runnable() {
		// @Override
		// public void run() {
		// delay(planeView);
		// }
		// });

	}

	/**
	 * 
	 * 方法描述 : 应用的初始化设置 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-21 下午4:14:21
	 * void
	 */
	// private void initAppDefaultSet() {
	// // 控制导航地图下边框的高度
	// com.rtm.frm.map.utils.Constants.BUTTON_PADDING = 50;
	// // 弹出导航中地图的倾斜角度
	// com.rtm.frm.map.utils.Constants.MAP_SKEW_ANGLE = 45;
	// // 线宽
	// com.rtm.frm.map.utils.Constants.ROUTE_BITMAP_LINE_WIDTH = 2;
	// // 虚线中实线的长度
	// com.rtm.frm.map.utils.Constants.ROUTE_BITMAP_LINE_INTERVAL_ON = 8;
	// // 虚线中空白的长度
	// com.rtm.frm.map.utils.Constants.ROUTE_BITMAP_LINE_INTERVAL_OFF = 8;
	// // 设置靠近推送距离
	// com.rtm.frm.map.utils.Constants.COUPON_DISTANCE = 20;
	//
	// // 每次进来之后都清除网络设置的显示
	// // PreferencesUtil.putBoolean("isShowNetSet", false);
	// // wifi配置页面
	// PreferencesUtil.putBoolean("isWiFiPageSetTiped", false);
	// // 如果有新版本，提示升级信息
	// PreferencesUtil.putBoolean("isShowUpdate", true);
	// // 默认每次进来之后关闭AR导航
	// PreferencesUtil.putBoolean("ar_isopen", true);
	// // 默认每次进来之后关闭beacon 扫描
	// PreferencesUtil.putBoolean("beacon_isopen", false);
	// // 默认每次进来之后打开pdr
	// PreferencesUtil.putBoolean("pdr_isopen", true);
	// // 每次进来之后清楚beacon扫描的提示
	// PreferencesUtil.putBoolean("isBeaconPageSetTiped", false);
	// // 蓝牙侧定位的补偿值清零
	// PreferencesUtil.putInt("beaconOffsetValue", 0);
	// // 默认的使用手机侧定位方式
	// PreferencesUtil.putString("LOCATE_MODE", "shouji");
	// // 默认不适用实时规划导航路线
	// PreferencesUtil.putBoolean("netset_refetchroute", false);
	//
	// // 初始化网络设置
	// PreferencesUtil.putString("MAC", XunluUtil.getMac()
	// .replaceAll(":", ""));
	// PreferencesUtil.putString("IPAddress", "position.rtmap.net");
	// PreferencesUtil.putString("PORT", "8092");
	//
	// PreferencesUtil.putString("NETLOC_MAC",
	// XunluUtil.getMac().replaceAll(":", ""));
	// PreferencesUtil.putString("NETLOC_IPAddress", "115.28.171.71");
	// PreferencesUtil.putString("NETLOC_PORT", "29003");
	// PreferencesUtil.putString("NETLOC_PDR_PORT", "1144");
	//
	// PreferencesUtil.putString("NETLOC_MAC_HTTP",
	// XunluUtil.getMac().replaceAll(":", ""));
	// PreferencesUtil.putString("NETLOC_IPAddress_HTTP", "115.28.171.71");
	// PreferencesUtil.putString("NETLOC_PORT_HTTP", "29003");
	// }

}
