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

package com.rtm.frm.fragment.mine;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.database.DBOperation;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.BaseFragment.OnFinishListener;
import com.rtm.frm.fragment.buildlist.BuildListFragment;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.model.Version;
import com.rtm.frm.net.NetworkCore;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.PreferencesUtil;
import com.rtm.frm.utils.ToastUtil;
import com.rtm.frm.utils.XunluUtil;

/**
 * @author hukunge 我的登陆界面
 */
public class MineFragment extends BaseFragment implements
		View.OnClickListener ,OnFinishListener ,OnTouchListener{
	public static boolean isLogin = false;// 是否已经登陆
	private CheckBox couponswitch;
	private ImageButton button_left;
	private Button mine_loginbtn;
	public static Button button_right;
	private RelativeLayout mine_update, mine_weibo, mine_aboutus, mine_mymap;
	public static LinearLayout mine_showlogin_layout,
			mine_showheadimage_layout;
	public RelativeLayout mine_loginlayout;
	private TextView text_title;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.fragment_mine, container,
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
		button_left = (ImageButton) contentView
				.findViewById(R.id.mine_button_back);
		button_left.setOnClickListener(this);
		mine_loginbtn = (Button) contentView.findViewById(R.id.mine_loginbtn);
		mine_loginbtn.setOnClickListener(this);
		button_right = (Button) contentView.findViewById(R.id.button_right);
		button_right.setOnClickListener(this);
		

		mine_update = (RelativeLayout) contentView
				.findViewById(R.id.mine_update);
		mine_update.setOnClickListener(this);
		mine_weibo = (RelativeLayout) contentView.findViewById(R.id.mine_weibo);
		mine_weibo.setOnClickListener(this);
		mine_aboutus = (RelativeLayout) contentView
				.findViewById(R.id.mine_aboutus);
		mine_aboutus.setOnClickListener(this);
		mine_mymap = (RelativeLayout) contentView.findViewById(R.id.mine_mymap);
		mine_mymap.setOnClickListener(this);

		mine_showlogin_layout = (LinearLayout) contentView
				.findViewById(R.id.mine_showlogin_layout);
		mine_showlogin_layout.setVisibility(View.VISIBLE);
		mine_showheadimage_layout = (LinearLayout) contentView
				.findViewById(R.id.mine_showheadimage_layout);
		mine_showheadimage_layout.setVisibility(View.GONE);
		
		mine_loginlayout = (RelativeLayout)contentView.findViewById(R.id.mine_loginlayout);
		
		// 优惠提醒checkbox
		couponswitch = (CheckBox) contentView
				.findViewById(R.id.mine_couponswitch);
		couponswitch.setChecked(PreferencesUtil.getBoolean("isCouponPushOn",
				true));
		couponswitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				PreferencesUtil.putBoolean("isCouponPushOn", isChecked);
			}
		});

		text_title = (TextView) contentView.findViewById(R.id.text_title);
		text_title.setText(getString(R.string.mine_title));

		if (DBOperation.getInstance().isHaveLocalPrivateBuildsData()) {
			isLogin = true;
			button_right.setVisibility(View.VISIBLE);
			mine_loginlayout.setVisibility(View.GONE);
		} else {
			isLogin = false;
			button_right.setVisibility(View.GONE);
			mine_loginlayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mine_loginbtn: {
			LoginDialogFragment login = new LoginDialogFragment();
			login.setOnFinishListener(this);
			// 弹出登陆接口
			MyFragmentManager.showFragmentdialog(new LoginDialogFragment(),
					MyFragmentManager.PROCESS_DIALOGFRAGEMENT_LOGIN,
					MyFragmentManager.DIALOGFRAGEMENT_LOGIN);
			break;
		}
		case R.id.mine_update: {
			checkUpdate();
			break;
		}
		case R.id.mine_weibo: {
			// 微博链接
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			Uri uri = Uri.parse(getString(R.string.title_about_weibo));
			intent.setData(uri);
			startActivity(intent);
			break;
		}
		case R.id.mine_mymap: {
			if (isLogin) {
				MyFragmentManager.getInstance().addFragment(R.id.map_container,
						new BuildListFragment("",ConstantsUtil.BUILD_TYPE_PRIVATE),
						MyFragmentManager.PROCESS_BUILDLIST,
						MyFragmentManager.FRAGMENT_BUILDLIST);
			} else {
				MyFragmentManager.showFragmentdialog(new LoginDialogFragment(),
						MyFragmentManager.PROCESS_DIALOGFRAGEMENT_LOGIN,
						MyFragmentManager.DIALOGFRAGEMENT_LOGIN);
			}
			break;
		}
		case R.id.mine_aboutus: {
			// 关于我们
			MyFragmentManager.showFragmentdialog(new AboutDialogFragment(),
					MyFragmentManager.PROCESS_DIALOGFRAGEMENT_ABOUT,
					MyFragmentManager.DIALOGFRAGEMENT_ABOUT);
			break;
		}
		case R.id.mine_button_back:
			// 返回
			MyFragmentManager.getInstance().backFragment();
			break;
		case R.id.button_right: {
			if (DBOperation.getInstance().clearAllTableData(true)) {
				ToastUtil.shortToast("您已退出登陆");
				button_right.setVisibility(View.GONE);
				mine_loginlayout.setVisibility(View.VISIBLE);
				PreferencesUtil.putString(ConstantsUtil.PREFS_USER, "");
				PreferencesUtil.putString(ConstantsUtil.PREFS_PASSWORD, "");
			} else {
				ToastUtil.shortToast("退出登陆失败");
			}
			break;
		}
		default:
			break;
		}
	}

	private void checkUpdate() {
		// 点击版本信息，检测升级
		if (NetworkCore.isNetConnected(mContext)) {
			// 判断是否弹出升级提示
			Version version = XunluApplication.versionModel;
			if (version != null) {
				if (version.getRemindNew()
						&& PreferencesUtil.getBoolean("isShowUpdate", false)) {
					XunluUtil.showUpdate(mContext, version);
				} else {
					ToastUtil.shortToast(R.string.message_no_update);
				}
			}
		} else {
			ToastUtil.shortToast(R.string.message_net_error);
		}
	}

	@Override
	public void onFinish(String flag, Bundle data) {
		isLogin = true;
		button_right.setVisibility(View.VISIBLE);
		mine_loginlayout.setVisibility(View.GONE);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}
}
