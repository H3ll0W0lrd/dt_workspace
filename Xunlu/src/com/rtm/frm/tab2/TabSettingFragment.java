/**
 * @author hukunge
 * @date 2014.09.02
 */
package com.rtm.frm.tab2;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.dialogfragment.LoadingFragment;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.BaseFragment.OnFinishListener;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.utils.PreferencesUtil;
import com.rtm.frm.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

@SuppressLint("InflateParams")
public class TabSettingFragment extends BaseFragment implements
		View.OnClickListener, OnFinishListener {
	public static boolean mPushEnabled = true;//地图推送开关，true为打开
	View contentView;
	RelativeLayout relItemSetting;
	RelativeLayout relItemAbout;
	LinearLayout linBack;
	CheckBox pushCheckBox;
	TextView tvCookieSize;
	LoadingFragment loading = null;
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 123:
				loading.dismissAllowingStateLoss();
				tvCookieSize.setText("0KB");
				ToastUtil.shortToast("缓存已清空");
				break;

			default:
				break;
			}
		};
	};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		contentView = inflater.inflate(R.layout.fragment_tab_setting, container,
				false);
		initView(contentView);
		return contentView;
	}

	private void initView(View v) {
		loading = new LoadingFragment(R.string.setting_clear_cache);
		
		relItemSetting = (RelativeLayout)v.findViewById(R.id.setting_item1);
		relItemAbout = (RelativeLayout)v.findViewById(R.id.setting_item2);
		tvCookieSize = (TextView)v.findViewById(R.id.tv_cookie_size);
		tvCookieSize.setText("215KB");
		
		linBack = (LinearLayout)v.findViewById(R.id.lin_back);
		linBack.setOnClickListener(this);
		
		mPushEnabled = PreferencesUtil.getBoolean("isCouponPushOn", true);
		pushCheckBox = (CheckBox)v.findViewById(R.id.setting_push_checkbox);
		pushCheckBox.setChecked(mPushEnabled);
		
		relItemSetting.setOnClickListener(this);
		relItemAbout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting_item1:
			MobclickAgent.onEvent(getActivity(),"event_click_setting_clear");
			pushCheckBox.toggle();
			mPushEnabled = pushCheckBox.isChecked();
			PreferencesUtil.putBoolean("isCouponPushOn", mPushEnabled);
			break; 
		case R.id.setting_item2:
			MobclickAgent.onEvent(getActivity(),"event_click_setting_message");
			MyFragmentManager.showFragmentdialog(loading,
					MyFragmentManager.PROCESS_DIALOGFRAGEMENT_LOADING,
					MyFragmentManager.DIALOGFRAGMENT_LOADING);
			
			//清空tab1页面图片缓存
//			ImageLoader loader = ImageLoader.getInstance();
//			if(loader != null){
//				loader.clearDiscCache();
//			}
//			
//			//清空地图缓存
//			File sdPath = Environment.getExternalStorageDirectory();
//			File file = new File(sdPath + "/rtmap");
//			XunluUtil.DeleteFile(file);
			
			mHandler.postDelayed(run, 400);
			break;
		case R.id.lin_back:
			MyFragmentManager.getInstance().backFragment();
			break;
		}
	}
	
	Runnable run = new Runnable() {
		@Override
		public void run() {
			mHandler.sendEmptyMessage(123);
		}
	};

	@Override
	public void onFinish(String flag, Bundle data) {}
}