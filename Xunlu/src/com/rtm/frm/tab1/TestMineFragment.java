/**
 * File name: SettingsFragment.java 
 *
 * Version information: 1.0.0
 *
 * Date: 2014-3-20 下午4:02:29
 *
 * Copyright 2014 Autonavi Software Co. Ltd. All Rights Reserved.
 *
 */

package com.rtm.frm.tab1;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.BaseFragment.OnFinishListener;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.fragment.mine.MineFragment;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.PreferencesUtil;
import com.rtm.frm.utils.XunluUtil;

/** 
 * ClassName: TestMineFragment 
 * date: 2014-9-4 下午9:21:04 
 * 我的好友页面
 * @author liyan 
 * @version  
 */  
public class TestMineFragment extends BaseFragment implements
		View.OnClickListener ,OnFinishListener ,OnTouchListener{
	
	private ListView mTalkListView;
	
	private ListView mFriendListView;
	
	private TextView mTalkBtnView;
	
	private TextView mFriendBtnView;
	
	private String mUserName;
	
	private String mPassWord;
	
	private RelativeLayout mTitleLayout;
	
	private RelativeLayout mLoginLayout;
	
	private TestMineTalkAdapter mMineTalkAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.fragment_test_mine, container,
				false);
		contentView.setOnTouchListener(this);
		initView(contentView);
		return contentView;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void initView(View contentView) {
		mTitleLayout = (RelativeLayout) contentView.findViewById(R.id.mine_title);
		mLoginLayout = (RelativeLayout) contentView.findViewById(R.id.test_mine_login);
		mTalkListView = (ListView) contentView.findViewById(R.id.mine_talk_list);
		mFriendListView = (ListView) contentView.findViewById(R.id.mine_friend);
		mTalkBtnView = (TextView)contentView.findViewById(R.id.talk);
		mTalkBtnView.setOnClickListener(this);
		mFriendBtnView = (TextView)contentView.findViewById(R.id.friend);
		mFriendBtnView.setOnClickListener(this);
		mUserName = PreferencesUtil.getString(ConstantsUtil.PREFS_USER,"");
		mPassWord = PreferencesUtil.getString(ConstantsUtil.PREFS_PASSWORD,"");
		
		
		mMineTalkAdapter = new TestMineTalkAdapter();
		mTalkListView.setAdapter(mMineTalkAdapter);
		//TODO 先不判断是否登录
//		if(XunluUtil.isEmpty(mUserName) || XunluUtil.isEmpty(mPassWord)){
//			showLogin();
//		}
		
		contentView.findViewById(R.id.login).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MyFragmentManager.getInstance().addFragment(R.id.test_main_container, new MineFragment(), MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_MINE, MyFragmentManager.DIALOGFRAGMENT_MINE);
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.talk:
			mTalkBtnView.setBackgroundResource(R.drawable.mine_tab_left);
			mTalkBtnView.setTextColor(getResources().getColor(R.color.mine_tab_text_white));
			mFriendBtnView.setBackgroundColor(Color.alpha(0));
			mFriendBtnView.setTextColor(getResources().getColor(R.color.mine_tab_text_blue));
			break;
		case R.id.friend:
			mFriendBtnView.setBackgroundResource(R.drawable.mine_tab_right);
			mFriendBtnView.setTextColor(getResources().getColor(R.color.mine_tab_text_white));
			mTalkBtnView.setBackgroundColor(Color.alpha(0));
			mTalkBtnView.setTextColor(getResources().getColor(R.color.mine_tab_text_blue));
			break;
		}
	}


	@Override
	public void onFinish(String flag, Bundle data) {
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}
	
	
	/**
	 * @author LiYan
	 * @date 2014-9-7 上午10:42:24  
	 * @explain 显示登录
	 * @return void 
	 */
	private void showLogin() {
		mTitleLayout.setVisibility(View.GONE);//隐藏title
		mLoginLayout.setVisibility(View.VISIBLE);//显示登录
	}
}
