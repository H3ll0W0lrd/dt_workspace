package com.rtm.frm.fragment.controller;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;

import com.rtm.frm.XunluApplication;
import com.rtm.frm.newframe.NewFrameActivity;

/**
 * @author hukunge
 * @version 2014-08-18 下午19:58
 */
public class MyFragmentManager extends BaseManager implements
		OnBackStackChangedListener {
	private static MyFragmentManager mInstance;
	private static ArrayList<String> mFragmentFlagList = new ArrayList<String>();
	@SuppressWarnings("unused")
	private static String mPreviousFragmentFlag = null;// 调用backfragment接口后退，获取调用时的framgnet
	// flag

	public static final String FLAG = "flag";
	/**
	 * FRAGMENT
	 * */

	public static final String PROCESS_LOADING = "process_loading";
	public static final String FRAGMENT_LOADING = "fragment_loading";

	public static final String PROCESS_BUILDLIST = "process_buildlist";
	public static final String FRAGMENT_BUILDLIST = "fragment_buildlist";

	public static final String PROCESS_CITYS = "process_citys";
	public static final String FRAGMENT_CITYS = "fragment_citys";

	public static final String PROCESS_MAP = "process_map";
	public static final String FRAGMENT_MAP = "fragment_map";
	//百度地图
	public static final String PROCESS_BAIDU_MAP = "process_baidu_map";
	public static final String FRAGMENT_BAIDU_MAP = "fragment_baidu_map";
	//百度地图全局搜索
	public static final String PRCOCESS_BAIDU_SEARCH= "process_baidu_Search";
	public static final String FRAGMENT_BAIDU_SEARCH = "fragment_baidu_Search";
	// new edition
	public static final String PROCESS_RT_MAP = "process_rtmap";
	public static final String FRAGMENT_RT_MAP = "fragment_rtmap";
	
	public static final String PROCESS_MINE = "process_mine";
	public static final String FRAGMENT_MINE = "fragment_mine";
	//rtmap 搜索
	public static final String PROCESS_RTMAP_SEARCH = "process_rtmap_search";
	public static final String FRAGMENT_RTMAP_SEARCH = "fragment_rtmap_search";
	//rtmap 楼层切换
	public static final String PROCESS_RTMAP_FLOOR_CHANGE = "process_rtmap_floor_change";
	public static final String FRAGMENT_RTMAP_FLOOR_CHANGE = "fragment_rtmap_floor_change";
	//跨层导航 切换
	public static final String PROCESS_NAV_FLOOR_CHANGE = "process_nav_floor_change";
	public static final String FRAGMENT_NAV_FLOOR_CHANGE = "fragment_nav_floor_change";
	//设置页面
	public static final String PROCESS_SETTING = "process_setting";
	public static final String FRAGMENT_SETTING = "fragment_setting";
	public static final String PROCESS_ABOUT = "process_about";
	public static final String FRAGMENT_ABOUT = "fragment_about";
	//优惠详情缩略图
	public static final String PROCESS_THUMBNAIL_MAP = "process_thumbnail_map";
	public static final String FRAGMENT_THUMBNAIL_MAP = "fragment_thumbnail_map";
	

	/**
	 * DIALOGFRAGMENT
	 * **/
	// 选择城市
	public static final String PROCESS_DIALOGFRAGEMENT_CHOOSECITY = "process_dialogfragment_choosecity";
	public static final String DIALOGFRAGMENT_CHOOSECITY = "dialogfragment_choosecity";
	// 选择类型，如商场，机场，地图
	public static final String PROCESS_DIALOGFRAGEMENT_CHOOSE = "process_dialogfragment_choose";
	public static final String DIALOGFRAGMENT_CHOOSE = "dialogfragment_choose";
	// 进度条
	public static final String PROCESS_DIALOGFRAGEMENT_LOADING = "process_dialogfragment_loading";
	public static final String DIALOGFRAGMENT_LOADING = "dialogfragment_loading";
	// 优惠
	public static final String PROCESS_DIALOGFRAGEMENT_COUPONS = "process_dialogfragment_coupons";
	public static final String DIALOGFRAGMENT_COUPONS = "dialogfragment_coupons";
	// 优惠--进度条
	public static final String PROCESS_DIALOGFRAGEMENT_LOADCOUPONS = "process_dialogfragment_loadcoupons";
	public static final String DIALOGFRAGMENT_LOADCOUPONS = "dialogfragment_loadcoupons";
	//poi详情
	public static final String PROCESS_DIALOGFRAGEMENT_POI_DETAIL = "process_dialogfragment_poi_detail";
	public static final String DIALOGFRAGMENT_POI_DETAIL = "dialogfragment_poi_detail";
	
	//登陆界面
	public static final String PROCESS_DIALOGFRAGEMENT_LOGIN = "process_dialogfragment_login";
	public static final String DIALOGFRAGEMENT_LOGIN  = "dialogfragment_login";
	//关于界面
	public static final String PROCESS_DIALOGFRAGEMENT_ABOUT = "process_dialogfragment_about";
	public static final String DIALOGFRAGEMENT_ABOUT  = "dialogfragment_about";
	//我的主页界面
	public static final String PRCOCESS_DIALOGFRAGMENT_MINE ="process_dialogfragment_mine";
	public static final String DIALOGFRAGMENT_MINE = "dialogfragment_mine";
	//活动
	public static final String PRCOCESS_DIALOGFRAGMENT_PROMOTION="process_dialogfragment_promotion";
	public static final String DIALOGFRAGMENT_PROMOTION = "dialogfragment_promotion";
	//活动管理
	public static final String PRCOCESS_DIALOGFRAGMENT_PROMOTION_MANAGER="process_dialogfragment_promotion_manager";
	public static final String DIALOGFRAGMENT_PROMOTION_MANAGER = "dialogfragment_promotion_manager";
	//导航图片缩略图
	public static final String PRCOCESS_DIALOGFRAGMENT_NAV_IMG ="process_dialogfragment_nav_img";
	public static final String DIALOGFRAGMENT_NAV_IMG = "dialogfragment_nav_img";
	//AR
	public static final String PRCOCESS_DIALOGFRAGMENT_AR= "process_dialogfragment_ar";
	public static final String DIALOGFRAGMENT_AR = "dialogfragment_ar";
	//寻宝
	public static final String PRCOCESS_DIALOGFRAGMENT_FIND= "process_dialogfragment_find";
	public static final String DIALOGFRAGMENT_FIND = "dialogfragment_find";
	//寻宝成功的list
	public static final String PRCOCESS_DIALOGFRAGMENT_FIND_LIST= "process_dialogfragment_find_list";
	public static final String DIALOGFRAGMENT_FIND_LIST = "dialogfragment_find_list";
	//寻宝成功的list
	public static final String PRCOCESS_DIALOGFRAGMENT_FIND_FAIL= "process_dialogfragment_find_fail";
	public static final String DIALOGFRAGMENT_FIND_LIST_FAIL = "dialogfragment_find_list_fail";
	//寻宝far
	public static final String PRCOCESS_DIALOGFRAGMENT_FINDFAR= "process_dialogfragment_findfar";
	public static final String DIALOGFRAGMENT_FINDFAR = "dialogfragment_findfar";
	//寻宝的AR界面
	public static final String PRCOCESS_DIALOGFRAGMENT_FINDAR= "process_dialogfragment_findar";
	public static final String DIALOGFRAGMENT_FINDAR = "dialogfragment_findar";
	//寻宝成功界面
	public static final String PRCOCESS_DIALOGFRAGMENT_SUCC= "process_dialogfragment_succ";
	public static final String DIALOGFRAGMENT_SUCC = "dialogfragment_succ";
	//alertDialogFragment
	public static final String PRCOCESS_DIALOGFRAGMENT_ALERT= "process_dialogfragment_alert";
	public static final String DIALOGFRAGMENT_ALERT = "dialogfragment_alert";
	//地图提示切换页
	public static final String PRCOCESS_DIALOGFRAGMENT_EXCHANGE_BUILD= "process_dialogfragment_exchange_build";
	public static final String DIALOGFRAGMENT_EXCHANGE_BUILD = "dialogfragment_exchange_build";
	//webview页
	public static final String PRCOCESS_DIALOGFRAGMENT_PROMOTION_DETAIL = "process_dialogfragment_promotion_detail";
	public static final String DIALOGFRAGMENT_PROMOTION_DETAIL = "dialogfragment_promotion_detail";
	
	private MyFragmentManager(XunluApplication app) {
		super(app);
		initManager();
	}

	@Override
	protected void initManager() {
	}

	@Override
	protected void DestroyManager() {
	}

	public static MyFragmentManager getInstance() {
		if (mInstance != null) {
			return mInstance;
		}

		synchronized (CenterManager.class) {
			if (mInstance == null) {
				mInstance = new MyFragmentManager(XunluApplication.getApp());
			}
			return mInstance;
		}
	}

	public static void setNullInstance() {
		mInstance = null;
	}

	public static ArrayList<String> getFragmentFlagList() {
		if (mFragmentFlagList == null)
			mFragmentFlagList = new ArrayList<String>();
		return mFragmentFlagList;
	}

	/**
	 * 添加fragment
	 * 
	 * @param id
	 *            容器id
	 * @param fragment
	 * @param flag
	 * @author yi.kang
	 * @date 2014-04-03
	 */
	public void addFragment(int id, Fragment fragment, String process_flag,
			String fragment_flag) {
		
		if(mFragmentFlagList != null && mFragmentFlagList.size() != 0 && mFragmentFlagList.get(mFragmentFlagList.size()-1).equals(process_flag+"-"+fragment_flag)){
			return;
		}
		
		android.support.v4.app.FragmentManager fragmentManager = NewFrameActivity
				.getInstance().getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		String flag = process_flag + "-" + fragment_flag;
		Bundle b = new Bundle();
		b.putString(FLAG, flag);
		fragment.setArguments(b);
		fragmentTransaction.add(id, fragment, flag);
		fragmentTransaction.addToBackStack(flag);
		fragmentTransaction.commitAllowingStateLoss();
		fragmentManager.executePendingTransactions();
		fragmentManager.addOnBackStackChangedListener(this);// 需要每次都调用吗？
	}
	
	
	/**
	 * @author LiYan
	 * @date 2014-9-19 下午6:35:05  
	 * @explain 添加Fragment，有动画
	 * @return void
	 * @param id
	 * @param fragment
	 * @param process_flag
	 * @param fragment_flag
	 * @param inId
	 * @param outId 
	 */
	public void addFragment(int id, Fragment fragment, String process_flag,
			String fragment_flag,int inId,int outId) {
		
		if(mFragmentFlagList != null && mFragmentFlagList.size() != 0 && mFragmentFlagList.get(mFragmentFlagList.size()-1).equals(process_flag+"-"+fragment_flag)){
			return;
		}
		
		android.support.v4.app.FragmentManager fragmentManager = NewFrameActivity
				.getInstance().getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		String flag = process_flag + "-" + fragment_flag;
		Bundle b = new Bundle();
		b.putString(FLAG, flag);
		fragment.setArguments(b);
		fragmentTransaction.setCustomAnimations(inId, outId,inId, outId);
		fragmentTransaction.add(id, fragment, flag);
		fragmentTransaction.addToBackStack(flag);
		fragmentTransaction.commitAllowingStateLoss();
		fragmentManager.executePendingTransactions();
		fragmentManager.addOnBackStackChangedListener(this);// 需要每次都调用吗？
	}
	
	/**
	 * @explain list末尾Fragment
	 * @return
	 */
	public Fragment getLastFragment() {
		if (mFragmentFlagList != null && mFragmentFlagList.size() > 0) {
			android.support.v4.app.FragmentManager fragmentManager = NewFrameActivity.getInstance().getSupportFragmentManager();
			Fragment f = fragmentManager.findFragmentByTag(mFragmentFlagList.get(mFragmentFlagList.size() - 1));
			return f;
		} else {
			return null;
		}
	}
	
	/**
	 * 获取FragmentTransaction
	 * @return
	 */
	public FragmentTransaction getTransaction() {
		android.support.v4.app.FragmentManager fragmentManager = NewFrameActivity
				.getInstance().getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		return fragmentTransaction;
	}

	// 替换fragment add child id, fragment， 不需要参数， 无动画
	public void addChildFragment(int parent_id, Fragment fragment,
			FragmentManager childFragmentManager) {
		FragmentTransaction fragmentTransaction = childFragmentManager
				.beginTransaction();
		fragmentTransaction.add(parent_id, fragment);
		fragmentTransaction.commitAllowingStateLoss();
		childFragmentManager.executePendingTransactions();
	}

	// 替换fragment add child id, fragment， 不需要参数， 有动画
	public void addChildFragment(int parent_id, Fragment fragment,
			FragmentManager childFragmentManager, int[] animators) {
		FragmentTransaction fragmentTransaction = childFragmentManager
				.beginTransaction();
		if (animators.length == 2) {
			fragmentTransaction.setCustomAnimations(animators[0], animators[1]);
		} else {
			fragmentTransaction.setCustomAnimations(animators[0], animators[1],
					animators[2], animators[3]);
		}
		fragmentTransaction.add(parent_id, fragment);
		fragmentTransaction.commitAllowingStateLoss();
		childFragmentManager.executePendingTransactions();
	}

	// 替换fragment replace id, fragment， 不需要参数， 无动画
	public void replaceFragment(int parent_id, Fragment fragment,
			String process_flag, String fragment_flag) {
		if(mFragmentFlagList != null && mFragmentFlagList.size() != 0 && mFragmentFlagList.get(mFragmentFlagList.size()-1).equals(process_flag+"-"+fragment_flag)){
			return;
		}
		android.support.v4.app.FragmentManager fragmentManager = NewFrameActivity
				.getInstance().getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		String flag = process_flag + "-" + fragment_flag;
		Bundle b = new Bundle();
		b.putString(FLAG, flag);
		fragment.setArguments(b);
		fragmentTransaction.replace(parent_id, fragment, flag);
		fragmentTransaction.addToBackStack(flag);
		fragmentTransaction.commitAllowingStateLoss();
		fragmentManager.executePendingTransactions();
		fragmentManager.addOnBackStackChangedListener(this);// 需要每次都调用吗？
	}

	// 替换fragment replace id, fragment， 需要参数， 无动画
	public void replaceFragment(int parent_id, Fragment fragment,
			String process_flag, String fragment_flag, Bundle b) {
		android.support.v4.app.FragmentManager fragmentManager = NewFrameActivity
				.getInstance().getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		String flag = process_flag + "-" + fragment_flag;
		b.putString(FLAG, flag);
		fragment.setArguments(b);
		fragmentTransaction.replace(parent_id, fragment, flag);
		fragmentTransaction.addToBackStack(flag);
		fragmentTransaction.commitAllowingStateLoss();
		fragmentManager.executePendingTransactions();
		fragmentManager.addOnBackStackChangedListener(this);
	}

	// 替换fragment replace id, fragment， 不需要参数， 有动画
	public void replaceFragment(int parent_id, Fragment fragment,
			String process_flag, String fragment_flag, int[] animators) {
		android.support.v4.app.FragmentManager fragmentManager = NewFrameActivity
				.getInstance().getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		String flag = process_flag + "-" + fragment_flag;
		Bundle b = new Bundle();
		b.putString(FLAG, flag);
		fragment.setArguments(b);
		if (animators.length == 2) {
			fragmentTransaction.setCustomAnimations(animators[0], animators[1]);
		} else {
			fragmentTransaction.setCustomAnimations(animators[0], animators[1],
					animators[2], animators[3]);
		}
		fragmentTransaction.replace(parent_id, fragment, flag);
		fragmentTransaction.addToBackStack(flag);
		fragmentTransaction.commitAllowingStateLoss();
		fragmentManager.executePendingTransactions();
		fragmentManager.addOnBackStackChangedListener(this);
	}

	// 替换fragment replace id, fragment， 需要参数， 有动画
	public void replaceFragment(int parent_id, Fragment fragment,
			String process_flag, String fragment_flag, Bundle b, int[] animators) {
		android.support.v4.app.FragmentManager fragmentManager = NewFrameActivity
				.getInstance().getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		String flag = process_flag + "-" + fragment_flag;
		b.putString(FLAG, flag);
		fragment.setArguments(b);
		if (animators.length == 2) {
			fragmentTransaction.setCustomAnimations(animators[0], animators[1]);
		} else {
			fragmentTransaction.setCustomAnimations(animators[0], animators[1],
					animators[2], animators[3]);
		}
		fragmentTransaction.replace(parent_id, fragment, flag);
		fragmentTransaction.addToBackStack(flag);
		fragmentTransaction.commitAllowingStateLoss();
		fragmentManager.executePendingTransactions();
		fragmentManager.addOnBackStackChangedListener(this);
	}

	// 后退到最近的一个fragment
	public void backFragment() {
		android.support.v4.app.FragmentManager fragmentManager = NewFrameActivity
				.getInstance().getSupportFragmentManager();
		mPreviousFragmentFlag = fragmentManager.getBackStackEntryAt(
				fragmentManager.getBackStackEntryCount() - 1).getName();
		fragmentManager.popBackStackImmediate();
	}

	// 弹出指定flag所有的fragment
	public void backFragmentDownFlag(String flag) {
		android.support.v4.app.FragmentManager fragmentManager = NewFrameActivity
				.getInstance().getSupportFragmentManager();
		Fragment fragment = fragmentManager.findFragmentByTag(flag);
		if (fragment != null) {
			mPreviousFragmentFlag = fragmentManager.getBackStackEntryAt(
					fragmentManager.getBackStackEntryCount() - 1).getName();
			fragmentManager
					.popBackStackImmediate(
							flag,
							android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
	}
	
	/**
	 * 弹出指定flags的Fragment
	 * @param flags
	 */
	public void backFragmentByFlags(List<String> flags) {
		android.support.v4.app.FragmentManager fragmentManager = NewFrameActivity
				.getInstance().getSupportFragmentManager();
		for(int i =0;i < flags.size();++i) {
			Fragment fragment = fragmentManager.findFragmentByTag(flags.get(i));
			if(fragment != null) {
				fragmentManager.popBackStack(flags.get(i), android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
				//出栈时，Fragment销毁，父类baseFragment的ondestroy，自动将mFragmentFlagList内容-1。
//				synchronized (mFragmentFlagList) {
//					mFragmentFlagList.remove(flags.get(i));
//				}
			}
		}
	}

	// 弹出指定flag之上所有的fragment
	public void backFragmentUpFlag(String flag) {
		android.support.v4.app.FragmentManager fragmentManager = NewFrameActivity
				.getInstance().getSupportFragmentManager();
		mPreviousFragmentFlag = fragmentManager.getBackStackEntryAt(
				fragmentManager.getBackStackEntryCount() - 1).getName();
		fragmentManager.popBackStackImmediate(flag, 0);
	}

	// 弹出所有的fragment
	public void backFragmentAll() {
		android.support.v4.app.FragmentManager fragmentManager = NewFrameActivity
				.getInstance().getSupportFragmentManager();
		fragmentManager
				.popBackStackImmediate(
						null,
						android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
		mFragmentFlagList.clear();
	}

	/**
	 * 根据flag获取Fragment对象
	 * 
	 * @param process_flag
	 * @param fragment_flag
	 */
	public static Fragment getFragmentByFlag(String process_flag,
			String fragment_flag) {
		android.support.v4.app.FragmentManager fragmentManager = NewFrameActivity
				.getInstance().getSupportFragmentManager();
		String flag = process_flag + "-" + fragment_flag;
		// Bundle b = new Bundle();
		// b.putString(FLAG, flag);
		return fragmentManager.findFragmentByTag(flag);
	}

	// 后退管理刷新
	@Override
	public void onBackStackChanged() {
		if (mFragmentFlagList.size() == 0)
			return;

		// if(mPreviousFragmentFlag.equals("x") &&
		// mFragmentFlagList.get(mFragmentFlagList.size() - 1).equals("x")){
		//
		// }
		//
		// if(mFragmentFlagList.get(mFragmentFlagList.size() -
		// 1).equals("x")){
		//
		// }
	}

	// 显示对话框 show id, fragment， 不需要参数， 无动画
	public static void showFragmentdialog(DialogFragment fragment,
			String process_flag, String fragment_flag) {
		android.support.v4.app.FragmentManager fragmentManager = NewFrameActivity
				.getInstance().getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		String flag = process_flag + "-" + fragment_flag;
		Bundle b = new Bundle();
		b.putString(FLAG, flag);
		fragment.setArguments(b);
		fragmentTransaction.setCustomAnimations(0, 0,
				FragmentTransaction.TRANSIT_NONE,
				FragmentTransaction.TRANSIT_NONE);
		fragment.show(fragmentTransaction, flag);
	}

	// 显示对话框 show id, fragment， 需要参数， 无动画
	public void showFragmentdialog(DialogFragment fragment,
			String process_flag, String fragment_flag, Bundle b) {
		android.support.v4.app.FragmentManager fragmentManager = NewFrameActivity
				.getInstance().getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		String flag = process_flag + "-" + fragment_flag;
		b.putString(FLAG, flag);
		fragment.setArguments(b);
		fragment.show(fragmentTransaction, flag);
	}
}
