package com.rtm.frm.newui;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.activity.BaseActivity;
import com.rtm.frm.arar.ARShowActivity;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.controller.FindManager;
import com.rtm.frm.fragment.controller.FragmentManagerTab;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.fragment.controller.FragmentManagerTab.OnFragmentTabChangeListener;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.stack.BackStackManager.OnBackStackChangedListener;
import com.rtm.frm.tab0.FragmentTabItemManager0;
import com.rtm.frm.tab0.TestRtmapFragment;
import com.rtm.frm.tab1.FragmentTabItemManager1;
import com.rtm.frm.tab1.TestMineFragment;
import com.rtm.frm.tab2.FragmentTabItemManager2;
import com.rtm.frm.tab2.PromotionFragment;
import com.rtm.frm.utils.PushUtils;
import com.rtm.frm.utils.ToastUtil;
import com.rtm.frm.utils.XunluUtil;
import com.rtm.location.LocationApp;
import com.umeng.analytics.MobclickAgent;

public class TestMainActivity extends BaseActivity implements
		OnBackStackChangedListener,OnClickListener, OnFragmentTabChangeListener {
	
	private boolean mIsFirstStart = true;
	
	private boolean mRtmapLocateIsRun = false;
	
	private TextView mFlagTextView ;

	@Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        
//        startScanWifi(false);
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
        
        //activity启动时判断是否启动室内定位引擎
        TestRtmapFragment fragment = NewFrameActivity.getInstance().getTab0();
        if(mIsFirstStart || (fragment != null && !fragment.isHidden())) {
        	// 启动定位
        	rtmapLocateStart();
        }
        
//        startScanWifi(true);
        super.onResume();
    }

    public List<View> mClickViews = new ArrayList<View>();

	public static TestMainActivity mInstance = null;
	
	private View mTabLeft;
	
	private View mTabCenter;
	
	private View mTabRight;

	public static TestMainActivity getInstance() {
		return mInstance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInstance = this;
		setContentView(R.layout.activity_test_main);
		initViews();
//		if (!PushUtils.hasBind(getApplicationContext())) {
		PushManager.startWork(getApplicationContext(),
                PushConstants.LOGIN_TYPE_API_KEY,
                PushUtils.getMetaValue(this, "api_key"));
//		}
	}

	@SuppressLint("InflateParams")
	private void initViews() {
		initPageAndTab();
		mFlagTextView = (TextView) this.findViewById(R.id.flag);
	}
	
	/**
	 * @author LiYan
	 * @date 2014-9-23 下午4:07:29  
	 * @explain 启动室内定位
	 * @return void 
	 */
	public void rtmapLocateStart () {
		if(!mRtmapLocateIsRun) {
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

	/**
	 * @author LiYan
	 * @date 2014-9-4 下午2:29:06
	 * @explain 初始化tabbutton
	 * @return void
	 */
	private void initPageAndTab() {
		// 初始化定位
		LocationApp.getInstance().init(XunluApplication.mApp);
		FragmentManagerTab.getInstance().setOnFragmentTabChangeListener(this);
		mTabLeft = this.findViewById(R.id.tab_btn_left);
		mTabLeft.setTag(0);
		final TextView tabLeftText = (TextView) mTabLeft
				.findViewById(R.id.tab_btn_left_name);
		tabLeftText.setText("地图");
		tabLeftText.setTextColor(getResources().getColor(
				R.color.tab_btn_text_press));

		final ImageView tabLeftImg = (ImageView) mTabLeft
				.findViewById(R.id.tab_btn_left_img);
		tabLeftImg.setBackgroundResource(R.drawable.tab_map_press);

		mTabCenter = this.findViewById(R.id.tab_btn_center);
		mTabCenter.setTag(1);
		final TextView tabCenterText = (TextView) mTabCenter
				.findViewById(R.id.tab_btn_center_name);
		tabCenterText.setText("我");
		tabCenterText.setTextColor(getResources()
				.getColor(R.color.tab_btn_text));

		final ImageView tabCenterImg = (ImageView) mTabCenter
				.findViewById(R.id.tab_btn_center_img);
		tabCenterImg.setBackgroundResource(R.drawable.tab_mine);
		TextView msgT = (TextView) mTabCenter
				.findViewById(R.id.tab_btn_center_hint);
		// msgT.setText("15");
		msgT.setVisibility(View.VISIBLE);

		mTabRight = this.findViewById(R.id.tab_btn_right);
		mTabRight.setTag(2);
		final TextView tabRightText = (TextView) mTabRight
				.findViewById(R.id.tab_btn_right_name);
		tabRightText.setText("活动");
		tabRightText
				.setTextColor(getResources().getColor(R.color.tab_btn_text));

		final ImageView tabRightImg = (ImageView) mTabRight
				.findViewById(R.id.tab_btn_right_img);
		tabRightImg.setBackgroundResource(R.drawable.tab_activities);

		
		mTabLeft.setOnClickListener(this);
		mTabCenter.setOnClickListener(this);
		mTabRight.setOnClickListener(this);
		
		mClickViews.add(mTabLeft);
		mClickViews.add(mTabCenter);
		mClickViews.add(mTabRight);

		
		TestRtmapFragment f0 = new TestRtmapFragment();
		FragmentTabItemManager0.getInstance().showItemFragment(f0, MyFragmentManager.PROCESS_RT_MAP,
				MyFragmentManager.FRAGMENT_RT_MAP);

		TestMineFragment f1 = new TestMineFragment();
		FragmentTabItemManager1.getInstance().showItemFragment(f1, MyFragmentManager.PROCESS_MINE,
				MyFragmentManager.FRAGMENT_MINE);

		PromotionFragment f2 = new PromotionFragment();
		FragmentTabItemManager2.getInstance().showItemFragment(f2, MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_PROMOTION, MyFragmentManager.DIALOGFRAGMENT_PROMOTION);

		FragmentManagerTab.getInstance().addFragmentManagerTabItem(FragmentTabItemManager0.getInstance());
		FragmentManagerTab.getInstance().addFragmentManagerTabItem(FragmentTabItemManager1.getInstance());
		FragmentManagerTab.getInstance().addFragmentManagerTabItem(FragmentTabItemManager2.getInstance());

		FragmentManagerTab.getInstance().showFragmentManagerTabItem(0);
//		initFirst();
	}
	
	public void setFlag(String flag) {
		mFlagTextView.setText(flag);
	}
	
	private void initFirst() {
		TestRtmapFragment t = NewFrameActivity.getInstance().getTab0();
		t.initFirst();
	}
	
	/**
	 * @author LiYan
	 * @date 2014-9-8 下午2:43:53  
	 * @explain 产生指定tab的click事件
	 * @return void
	 * @param index 
	 */
	public void clickTab(int index) {
		mClickViews.get(index).performClick();
	}

	@Override
	public void onBackStackCleared() {

	}
	
	private long firstTime = 0;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*if (keyCode == KeyEvent.KEYCODE_BACK) {
			//如果flaglist中小于初始的3个页面，执行推出操作
			int n = 3;
			Fragment baiduFragment = MyFragmentManager.getFragmentByFlag(MyFragmentManager.PROCESS_BAIDU_MAP, MyFragmentManager.FRAGMENT_BAIDU_MAP);
			if(baiduFragment != null) {
				n++;
			}
			Fragment navFloorChangeFragment = MyFragmentManager.getFragmentByFlag(MyFragmentManager.PROCESS_NAV_FLOOR_CHANGE, MyFragmentManager.FRAGMENT_NAV_FLOOR_CHANGE);
			if(navFloorChangeFragment != null) {
				if(navFloorChangeFragment.isHidden()) {
					n++;
				} else {
					MyFragmentManager.getInstance().backFragment();
					return true;
				}
			}
			if (MyFragmentManager.getFragmentFlagList().size() <= n) {
				//如果当前显示rtmap，则先清除导航路线
				TestRtmapFragment rtmapFragment = (TestRtmapFragment) MyFragmentManager.getFragmentByFlag(MyFragmentManager.PROCESS_RT_MAP, MyFragmentManager.FRAGMENT_RT_MAP);
				if(rtmapFragment != null &&!rtmapFragment.isHidden() && rtmapFragment.closeNav()) {
					return true;
				}
				long secondTime = System.currentTimeMillis();
				if (secondTime - firstTime > 2000) { // 如果两次按键时间间隔大于2秒，则不退出
					ToastUtil.showToast("再按一次退出程序", true);
					firstTime = secondTime;// 更新firstTime
					return true;
				} else {
					for (int i = 0; i < mClickViews.size(); ++i) {
						mClickViews.get(i).setClickable(false);
					}
					MyFragmentManager.getInstance().backFragmentAll();
				}
			}
		}
		return super.onKeyDown(keyCode, event);*/
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			BaseFragment baseFragment = (BaseFragment) MyFragmentManager.getInstance().getLastFragment();
			if(baseFragment == null){
				return super.onKeyDown(keyCode, event);
			}
			
			if(baseFragment.isRemoving()) {
				return true;
			}
			
			if (baseFragment != null && baseFragment.isTabFragment()) {
				long secondTime = System.currentTimeMillis();
				if (secondTime - firstTime > 2000) { // 如果两次按键时间间隔大于2秒，则不退出
					ToastUtil.showToast("再按一次退出程序", true);
					firstTime = secondTime;// 更新firstTime
					return true;
				} else {
					for (int i = 0; i < mClickViews.size(); ++i) {
						mClickViews.get(i).setClickable(false);
					}
					MyFragmentManager.getInstance().backFragmentAll();
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private ScanWifiThread ScanWifiThread ;
	
	private void startScanWifi(boolean isStart) {
		if(isStart) {
			if(ScanWifiThread == null) {
				ScanWifiThread = new ScanWifiThread(true);				
				ScanWifiThread.start();
			}
		} else {
			if(ScanWifiThread != null) {
				ScanWifiThread.stopScanThread();
				ScanWifiThread = null;
			}
		}
	}
	
	private class ScanWifiThread extends Thread {
		private boolean run = true;
		public ScanWifiThread(boolean isRun) {
			run = isRun;
		}
		@Override
		public void run() {
			while(run) {
				try {
					XunluUtil.scanWifi(XunluApplication.mApp);
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void stopScanThread() {
			run = false;
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getTag() != null) {
			int index = Integer.parseInt(v.getTag().toString());
			FragmentManagerTab.getInstance().showFragmentManagerTabItem(index);
		} else {

		}		
	}

	@Override
	public void onFragmentTabChanged(int currentIndex, int lastIndex) {
		TextView currentT0 = (TextView) mTabLeft.findViewById(R.id.tab_btn_left_name);
		TextView currentT1 = (TextView) mTabCenter.findViewById(R.id.tab_btn_center_name);
		TextView currentT2 = (TextView) mTabRight.findViewById(R.id.tab_btn_right_name);
		ImageView imgT0 = (ImageView) mTabLeft.findViewById(R.id.tab_btn_left_img);
		ImageView imgT1 = (ImageView) mTabCenter.findViewById(R.id.tab_btn_center_img);
		ImageView imgT2 = (ImageView) mTabRight.findViewById(R.id.tab_btn_right_img);
		switch (currentIndex) {
		case 0:
			currentT0.setTextColor(getResources().getColor(R.color.tab_btn_text_press));
			imgT0.setBackgroundResource(R.drawable.tab_map_press);
			break;
		case 1:
			currentT1.setTextColor(getResources().getColor(R.color.tab_btn_text_press));
			imgT1.setBackgroundResource(R.drawable.tab_mine_press);
			break;
		case 2:
			currentT2.setTextColor(getResources().getColor(R.color.tab_btn_text_press));
			imgT2.setBackgroundResource(R.drawable.tab_activities_press);
			break;
		}
		switch (lastIndex) {
		case 0:
			currentT0.setTextColor(getResources().getColor(R.color.tab_btn_text));
			imgT0.setBackgroundResource(R.drawable.tab_map);
			break;
		case 1:
			currentT1.setTextColor(getResources().getColor(R.color.tab_btn_text));
			imgT1.setBackgroundResource(R.drawable.tab_mine);
			break;
		case 2:
			currentT2.setTextColor(getResources().getColor(R.color.tab_btn_text));
			imgT2.setBackgroundResource(R.drawable.tab_activities);
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 需要加强制退出，否则fragmentManagerFlagList内容不会被销毁，再次启动时，会报空指针。
		System.exit(0);
	}
	
}
