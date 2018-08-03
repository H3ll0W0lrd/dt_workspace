package com.rtm.frm.newframe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.rtm.common.utils.Constants;
import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.activity.BaseActivity;
import com.rtm.frm.arar.ARShowActivity;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.controller.FindManager;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.model.Version;
import com.rtm.frm.net.PostData;
import com.rtm.frm.service.UpdateVersionService;
import com.rtm.frm.stack.BackStackManager.OnBackStackChangedListener;
import com.rtm.frm.tab0.TestRtmapFragment;
import com.rtm.frm.tab1.TabMineFragment;
import com.rtm.frm.tab2.Tab2Fragment;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.PreferencesUtil;
import com.rtm.frm.utils.PushUtils;
import com.rtm.frm.utils.ToastUtil;
import com.rtm.frm.utils.XunluUtil;
import com.rtm.location.LocationApp;
import com.umeng.analytics.MobclickAgent;

/*** kunge.hu:新的布局，采用tab + activity方式 **/
public class NewFrameActivity extends BaseActivity implements OnClickListener, OnBackStackChangedListener {
	public static int ID_ALL = R.id.container_new_frame;
	public static int ID_TOP_HALF = R.id.show;
	LinearLayout tab0, tab1, tab2;
	ImageView img0, img1, img2;
	TextView tv0, tv1, tv2;
	android.support.v4.app.FragmentManager fm;
	android.support.v4.app.FragmentTransaction ft;
	TestRtmapFragment frag0;
	TabMineFragment frag1;
	Tab2Fragment frag2;

	// 屏幕常量
	private PowerManager pm = null;
	private PowerManager.WakeLock wake = null;
	
	// 百度地图定位
	private LocationClient mLocClient;
	private MyLocationListenner mLocationListenner = new MyLocationListenner();
	public String mCurrentGpsCity = "北京";// 默认北京

	/**
	 * 定位启动相关
	 */

	private boolean mRtmapLocateIsRun = false;

	public static NewFrameActivity instance = null;

	public static NewFrameActivity getInstance() {
		if (instance == null) {
			instance = new NewFrameActivity();
		}
		return instance;
	}

	final Handler handler = new Handler();

	/**
	 * @author liYan
	 * @explain 处理更新接口返回的信息，根据返回的结果判断是否需要下载新版本
	 * @param data
	 */

	public void openBlueTooth() {
		if (!XunluUtil.isHaveBlueTooth() || XunluUtil.getAndroidVersion() < 18) {
			return;
		}
		XunluUtil.openBlueTooth();
	}

	public void keepScreenOn() {
		pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		wake = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MainActivity_Tag");
		wake.acquire();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_frame_main);

		instance = this;

		openBlueTooth();//默认打开蓝牙

		keepScreenOn();// 保持屏幕常亮

		if (!PushUtils.hasBind(this)) {
			PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, PushUtils.getMetaValue(this, "api_key"));
		}

		// 初始化室内定位
		LocationApp.getInstance().init(XunluApplication.mApp);
		
		// 初始化百度定位
		initBaiduLocate();
		// 初始化image loader
		XunluUtil.initImageLoader(this);

		initView();

	}

	private void initBaiduLocate() {
		// 初始化百度定位
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(mLocationListenner);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		// option.setAddrType("all");
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();
	}

	private void initView() {
		tab0 = (LinearLayout) findViewById(R.id.lin_tab0);
		tab1 = (LinearLayout) findViewById(R.id.lin_tab1);
		tab2 = (LinearLayout) findViewById(R.id.lin_tab2);

		img0 = (ImageView) findViewById(R.id.image_tab0);
		img1 = (ImageView) findViewById(R.id.image_tab1);
		img2 = (ImageView) findViewById(R.id.image_tab2);

		tv0 = (TextView) findViewById(R.id.tv_tab0);
		tv1 = (TextView) findViewById(R.id.tv_tab1);
		tv2 = (TextView) findViewById(R.id.tv_tab2);

		frag0 = new TestRtmapFragment();
		frag1 = new TabMineFragment();
		frag2 = new Tab2Fragment();

		fm = getSupportFragmentManager();
		ft = fm.beginTransaction();
		ft.add(ID_TOP_HALF, frag0);
		ft.add(ID_TOP_HALF, frag1);
		ft.add(ID_TOP_HALF, frag2);

		ft.show(frag0);
		ft.hide(frag1);
		ft.hide(frag2);

		tab0.setSelected(true);
		img0.setImageResource(R.drawable.tab_map_press);
		tv0.setTextColor(getResources().getColor(R.color.tab_btn_text_press));
		ft.commit();
	}

	@Override
	public void onBackStackCleared() {
	}

	@Override
	public void onClick(View arg0) {
		if (getTab0().mRouteLayer.hasData()) {// 如果室内地图中得routelayer有数据，说明处于导航模式
			return;
		}
		switch (arg0.getId()) {
		case R.id.lin_tab0:
			ft = fm.beginTransaction();
			if (frag0.isHidden()) {
				ft.show(frag0);
				ft.hide(frag1);
				ft.hide(frag2);

				tab0.setSelected(true);
				tab1.setSelected(false);
				tab2.setSelected(false);

				img0.setImageResource(R.drawable.tab_map_press);
				img1.setImageResource(R.drawable.tab_activities);
				img2.setImageResource(R.drawable.tab_mine);

				tv0.setTextColor(getResources().getColor(R.color.tab_btn_text_press));
				tv1.setTextColor(getResources().getColor(R.color.tab_btn_text));
				tv2.setTextColor(getResources().getColor(R.color.tab_btn_text));
			}
			ft.commit();

			MobclickAgent.onEvent(this, "event_map");
			break;

		case R.id.lin_tab1:
			ft = fm.beginTransaction();
			if (frag1.isHidden()) {
				ft.hide(frag0);
				ft.show(frag1);
				ft.hide(frag2);

				tab0.setSelected(false);
				tab1.setSelected(true);
				tab2.setSelected(false);

				img0.setImageResource(R.drawable.tab_map);
				img1.setImageResource(R.drawable.tab_activities_press);
				img2.setImageResource(R.drawable.tab_mine);

				tv0.setTextColor(getResources().getColor(R.color.tab_btn_text));
				tv1.setTextColor(getResources().getColor(R.color.tab_btn_text_press));
				tv2.setTextColor(getResources().getColor(R.color.tab_btn_text));
			}
			ft.commit();

			MobclickAgent.onEvent(this, "event_activities");
			
//			checkTabMineShowState();
			break;

		case R.id.lin_tab2:
			ft = fm.beginTransaction();
			if (frag2.isHidden()) {
				ft.hide(frag0);
				ft.hide(frag1);
				ft.show(frag2);

				tab0.setSelected(false);
				tab1.setSelected(false);
				tab2.setSelected(true);

				img0.setImageResource(R.drawable.tab_map);
				img1.setImageResource(R.drawable.tab_activities);
				img2.setImageResource(R.drawable.tab_mine_press);

				tv0.setTextColor(getResources().getColor(R.color.tab_btn_text));
				tv1.setTextColor(getResources().getColor(R.color.tab_btn_text));
				tv2.setTextColor(getResources().getColor(R.color.tab_btn_text_press));
			}
			ft.commit();

			MobclickAgent.onEvent(this, "event_mycenter");
			break;
		}
	}


	public TestRtmapFragment getTab0() {
		return frag0;
	}

	public TabMineFragment getTab1() {
		return frag1;
	}

	public Tab2Fragment getTab2() {
		return frag2;
	}

	private long firstTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 如果栈里还有未退出的fragment，则先处理退出fragment
			BaseFragment baseFragment = (BaseFragment) MyFragmentManager.getInstance().getLastFragment();
			if (baseFragment != null) {
				return super.onKeyDown(keyCode, event);
			}

			long secondTime = System.currentTimeMillis();
			if (secondTime - firstTime > 2000) { // 如果两次按键时间间隔大于2秒，则不退出
				ToastUtil.showToast("再按一次退出程序", true);
				firstTime = secondTime;// 更新firstTime
				return true;
			} else {
				MyFragmentManager.getInstance().backFragmentAll();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onPause() {
		// 不在ar模式下停止定位,需要增加判断
		if (ARShowActivity.isInARMode || FindManager.isFindShowing) {

		} else {
			rtmapLocateStop();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		MobclickAgent.onResume(this);

		rtmapLocateStart();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		Intent updateIntent = new Intent(NewFrameActivity.this, UpdateVersionService.class);
		this.stopService(updateIntent);
		
		// 退出时关闭蓝牙
		XunluUtil.closeBlueTooth();
		// 退出时销毁定位
		mLocClient.stop();
		super.onDestroy();
	}

	/**
	 * @author LiYan
	 * @date 2014-9-23 下午4:07:29
	 * @explain 启动室内定位
	 * @return void
	 */
	public void rtmapLocateStart() {
		if (!mRtmapLocateIsRun) {
			mRtmapLocateIsRun = true;
			LocationApp.getInstance().start();
		}
	}

	/**
	 * @author LiYan
	 * @date 2014-9-23 下午4:07:38
	 * @explain 停止室内定位
	 * @return void
	 */
	public void rtmapLocateStop() {
		if (!ARShowActivity.isInARMode && !FindManager.isFindShowing) {
			mRtmapLocateIsRun = false;
			LocationApp.getInstance().stop();
		}
	}

	public BDLocation mCurrentBdLocation;

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(final BDLocation location) {
			if (location != null && !XunluUtil.isEmpty(location.getCity())) {
				mCurrentGpsCity = location.getCity().replaceAll("市", "");
				PreferencesUtil.putString("BDLocateCity", mCurrentGpsCity);
				mCurrentBdLocation = location;
				mLocClient.stop();
			}

		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}
}