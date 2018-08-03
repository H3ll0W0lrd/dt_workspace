/**
 * @date 2014.08.18 21:15
 * @explain 百度地图页搜索功能
 */
package com.rtm.frm.newui;

import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.rtm.frm.R;
import com.rtm.frm.database.DBOperation;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.fragment.map.BaiduMapFragment;
import com.rtm.frm.model.Build;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.ToastUtil;
import com.rtm.frm.utils.XunluUtil;
import com.umeng.analytics.MobclickAgent;

@SuppressLint("HandlerLeak") 
public class TestSearchDialogFragment extends BaseFragment implements
		OnItemClickListener,OnTouchListener {


	private ListView mSearchListView;

	private EditText mSearchEditText;

	private TextWatcher mSearchWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void afterTextChanged(Editable editable) {
			if (editable.length() == 0) {
				// 获取本地所有建筑物列表
				List<Build> builds = DBOperation.getInstance().queryBuildAll();
				mSearchListAdapter.setData(builds);
			} else {
				
				List<Build> builds = DBOperation.getInstance().queryBuildByKeyLike(editable.toString());
				mSearchListAdapter.setData(builds);
//				// 搜索关键词
//				PostData.postSearchPoiByKeyword(mHandler,
//						ConstantsUtil.HANDLER_POST_SEARCH_POI, mBuildId,
//						editable.toString(), null, -1, -1);
				//TODO 全局搜索接口
			}
		}
	};

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ConstantsUtil.HANDLER_POST_SEARCH_POI:
				upadteSearchList(msg.obj.toString());
				break;
			}
		};
	};


	private TestSearchListAdapter mSearchListAdapter;

	public TestSearchDialogFragment() {
//		this.setStyle(DialogFragment.STYLE_NORMAL, R.style.dialogfragment_transparent_bg);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.fragment_test_search_list,
				container, false);
		
		return contentView;
	}
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		initView(view);
	}

	private void initView(View contentView) {
		mSearchListView = (ListView) contentView
				.findViewById(R.id.search_list_view);
		mSearchEditText = (EditText) contentView.findViewById(R.id.search_edit);

		mSearchEditText.addTextChangedListener(mSearchWatcher);


		mSearchListAdapter = new TestSearchListAdapter();
		mSearchListView.setAdapter(mSearchListAdapter);
		mSearchListView.setOnItemClickListener(this);
		//默认显示所有建筑物
		List<Build> builds = DBOperation.getInstance().queryBuildAll();
		if(builds.size() == 0) {
			ToastUtil.shortToast("数据重新加载，请稍后");
			DBOperation.getInstance().copyBuildData();
			builds = DBOperation.getInstance().queryBuildAll();
		}
		mSearchListAdapter.setData(builds);
		contentView.setOnTouchListener(this);
	}


	@Override
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {
		MobclickAgent.onEvent(getActivity(),"event_click_outdoor_search_item");
		
		BaiduMapFragment baiduFrag = (BaiduMapFragment) MyFragmentManager.getFragmentByFlag(MyFragmentManager.PROCESS_BAIDU_MAP,
				MyFragmentManager.FRAGMENT_BAIDU_MAP);
		baiduFrag.mCurrentMode = LocationMode.NORMAL;
		
		//TODO 点击某个item，在地图上显示poi气泡
		TestSearchListAdapter daAdapter = (TestSearchListAdapter) adapterView.getAdapter(); 
		BaiduMapFragment baiduFragment = (BaiduMapFragment) MyFragmentManager.getFragmentByFlag(
				MyFragmentManager.PROCESS_BAIDU_MAP, MyFragmentManager.FRAGMENT_BAIDU_MAP);
		baiduFragment.showBuildBySearch((Build)daAdapter.getItem(position));
		
		MyFragmentManager.getInstance().backFragment();
	}

	/**
	 * 更新searchListview
	 * 
	 * @param data
	 */
	public void upadteSearchList(String data) {
//		// 联想搜索完毕之后填充数据显示
//		SearchPois pois = null;
//		if (data != null && !XunluUtil.isEmpty(data)) {
//			try {
//				pois = new SearchPois(data);
//			} catch (Exception e) {
//				e.printStackTrace();
//				return;
//			}
//		}
//		if (pois != null) {
//			mSearchListAdapter.setData(pois.getPOIs());
//		} else {
//			mSearchListAdapter.setData(new ArrayList<POI>());
//		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN ) {
			MyFragmentManager.getInstance().backFragment();
		}
		return true;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		XunluUtil.hideKeyboard(getActivity());
	}
}