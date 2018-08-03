package com.rtm.frm.dialogfragment.typechoose;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.rtm.frm.R;
import com.rtm.frm.database.DBOperation;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.buildlist.BuildListFragment;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.fragment.map.BaiduMapFragment;
import com.rtm.frm.fragment.mine.MineFragment;
import com.rtm.frm.model.Version;
import com.rtm.frm.net.PostData;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.PreferencesUtil;
import com.rtm.frm.utils.ToastUtil;
import com.rtm.frm.utils.XunluUtil;
import com.umeng.analytics.MobclickAgent;

@SuppressLint("HandlerLeak")
public class ChooseDialogFragment extends BaseFragment implements
		View.OnClickListener, OnTouchListener {
	private View contentView;
	private Button mAirportButton;
	private Button mMallButton;
	private Button mMyMapButton;
	private Button mShowBaiduMap;
	private int currentState = -1;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		contentView = inflater.inflate(R.layout.fragment_choose, container,
				false);

		return contentView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		initViews(view);
//		checkUpdate();
	}

	private void initViews(View view) {
		mAirportButton = (Button) view.findViewById(R.id.btn_airport);
		mMallButton = (Button) view.findViewById(R.id.btn_mall);
		mMyMapButton = (Button) view.findViewById(R.id.btn_my_map);
		mShowBaiduMap = (Button) view.findViewById(R.id.btn_show_baidu_map);
		mAirportButton.setOnClickListener(this);
		mMallButton.setOnClickListener(this);
		mMyMapButton.setOnClickListener(this);
		mShowBaiduMap.setOnClickListener(this);

		if (DBOperation.getInstance().isHaveLocalBuildsData()) {
			currentState = ConstantsUtil.STATE_INIT_FINISHED;
		}

	}

	/**
	 * 检查更新
	 */
	private void checkUpdate() {
		PostData.postCheckUpdate(mHandler,
				ConstantsUtil.HANDLER_POST_CHECK_UPDATE);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ConstantsUtil.HANDLER_POST_CHECK_UPDATE:
				if (msg.arg1 != ConstantsUtil.STATE_NET_ERR_UNUSED) {
					checkUpdateResult((String) msg.obj);
				} else {
					ToastUtil.showToast(R.string.error_net_unuse, true);
				}
				break;
			}
		};
	};

	/**
	 * @author liYan
	 * @explain 处理更新接口返回的信息，根据返回的结果判断是否需要下载新版本
	 * @param data
	 */
	private void checkUpdateResult(String data) {
		Version version = new Version(data);
		if (version.getRemindNew()
				&& PreferencesUtil.getBoolean("isShowUpdate", true)) {
			// 弹出更新确认对话框
			XunluUtil.showUpdate(mContext, version);
		}
	}

	@Override
	public void onClick(View v) {
		if (currentState != ConstantsUtil.STATE_INIT_FINISHED) {
			ToastUtil.showToast(R.string.toast_clear_cache, false);
			return;
		}
		switch (v.getId()) {
		case R.id.btn_airport:
			// 机场导航
			BuildListFragment airportListFragment = new BuildListFragment("北京",
					ConstantsUtil.BUILD_TYPE_AIRPORT);
			MyFragmentManager.getInstance().addFragment(R.id.test_main_container,
					airportListFragment, MyFragmentManager.PROCESS_BUILDLIST,
					MyFragmentManager.FRAGMENT_BUILDLIST);
			break;
		case R.id.btn_mall:
			// 商场导航
			BuildListFragment mallListFragment = new BuildListFragment("北京",
					ConstantsUtil.BUILD_TYPE_MALL);
			MyFragmentManager.getInstance().addFragment(R.id.test_main_container,
					mallListFragment, MyFragmentManager.PROCESS_BUILDLIST,
					MyFragmentManager.FRAGMENT_BUILDLIST);
			break;
		case R.id.btn_my_map:
			// 跳转到我的地图
			MobclickAgent.onEvent(mContext, ConstantsUtil.EVENT_CLICK_MY_MAP);
			// 测试登录
			if (!DBOperation.getInstance().isHaveLocalPrivateBuildsData()) {// 进入我的页面提示登录
				MyFragmentManager.getInstance().addFragment(R.id.test_main_container,
						new MineFragment(),
						MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_MINE,
						MyFragmentManager.DIALOGFRAGMENT_MINE);
			} else {// 进入我的地图列表
				MyFragmentManager.getInstance().addFragment(
						R.id.map_container,
						new BuildListFragment("",
								ConstantsUtil.BUILD_TYPE_PRIVATE),
						MyFragmentManager.PROCESS_BUILDLIST,
						MyFragmentManager.FRAGMENT_BUILDLIST);
			}
			break;
		case R.id.btn_show_baidu_map:
			showBaiduMapFragment();

			break;
		}
	}

	/**
	 * 显示百度地图Fragment
	 */
	private void showBaiduMapFragment() {
		final BaiduMapFragment baiduMapFragment = new BaiduMapFragment();
		MyFragmentManager.getInstance().addFragment(R.id.test_main_container,
				baiduMapFragment, MyFragmentManager.PROCESS_BAIDU_MAP,
				MyFragmentManager.FRAGMENT_BAIDU_MAP);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}
}
