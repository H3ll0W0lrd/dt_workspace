/**
 * @date 2014.08.18 21:15
 */
package com.rtm.frm.fragment.map;

import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
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

import com.rtm.common.model.POI;
import com.rtm.common.model.RMPois;
import com.rtm.common.utils.OnSearchPoiListener;
import com.rtm.frm.R;
import com.rtm.frm.adapter.AssociativeSearchAdapter;
import com.rtm.frm.adapter.RtmapSearchAdapter;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Floor;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.tab0.TestRtmapFragment;
import com.rtm.frm.tab0.TestRtmapFragmentManager;
import com.rtm.frm.utils.RMSearchPoiUtil;
import com.rtm.frm.utils.ToastUtil;
import com.rtm.frm.utils.XunluUtil;
import com.umeng.analytics.MobclickAgent;

@SuppressLint("HandlerLeak")
public class RtmapSearchFragment extends BaseFragment implements
		OnItemClickListener, OnTouchListener {

	private ListView mFloorListView;

	private ListView mSearchListView;

	private EditText mSearchEditText;
	private RMSearchPoiUtil mSearch;

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
				mSearchListView.setVisibility(View.GONE);
				mFloorListView.setVisibility(View.VISIBLE);
			} else {
				mSearchListView.setVisibility(View.VISIBLE);
				mFloorListView.setVisibility(View.GONE);
				// 搜索关键词
				mSearch.setKey(XunluMap.getInstance().getApiKey())
						.setBuildid(mBuildId).setKeywords(editable.toString())
						.searchPoi();
			}
		}
	};

	private RtmapSearchAdapter mRtmapSearchAdapter;

	private AssociativeSearchAdapter mAssociativeListAdapter;

	private String mBuildId;

	public RtmapSearchFragment() {
	}

	public RtmapSearchFragment(String buildId) {
		mBuildId = buildId;
		// setStyle(DialogFragment.STYLE_NORMAL,R.style.dialogfragment_transparent_bg);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.fragment_rtmap_search,
				container, false);
		initView(contentView);
		initData();
		mSearch = new RMSearchPoiUtil();
		mSearch.setOnSearchPoiListener(new OnSearchPoiListener() {

			@Override
			public void onSearchPoi(RMPois result) {
				if (result.getError_code() == 0) {
					upadteSearchList(result);
				} else {
					ToastUtil.shortToast(result.getError_msg());
				}
			}
		});
		return contentView;
	}

	private void initView(View contentView) {
		mFloorListView = (ListView) contentView
				.findViewById(R.id.floor_list_view);
		mSearchListView = (ListView) contentView
				.findViewById(R.id.search_list_view);
		mSearchEditText = (EditText) contentView.findViewById(R.id.search_edit);

		mSearchEditText.addTextChangedListener(mSearchWatcher);

		mRtmapSearchAdapter = new RtmapSearchAdapter();
		mFloorListView.setAdapter(mRtmapSearchAdapter);
		mFloorListView.setOnItemClickListener(this);

		mAssociativeListAdapter = new AssociativeSearchAdapter();
		mSearchListView.setAdapter(mAssociativeListAdapter);
		mSearchListView.setOnItemClickListener(this);

		contentView.setOnTouchListener(this);
	}

	/**
	 * 初始化list数据
	 */
	private void initData() {
		List<Floor> floors = TestRtmapFragmentManager.getInstance()
				.queryFloorsByBuildId(mBuildId);
		mRtmapSearchAdapter.setData(floors);
		mRtmapSearchAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {
		TestRtmapFragment fragment = NewFrameActivity.getInstance().getTab0();
		MyFragmentManager.getInstance().backFragment();
		if (adapterView.getAdapter() == mRtmapSearchAdapter) {
			MobclickAgent.onEvent(mContext, "event_click_indoor_floor");

			Floor floor = (Floor) adapterView.getAdapter().getItem(position);
			fragment.switchBuild(floor.buildName, floor.buildId, floor.floor);
		} else {
			MobclickAgent.onEvent(mContext, "event_click_indoor_search_item");

			POI poi = (POI) adapterView.getAdapter().getItem(position);
			fragment.showSearchPoiPoint(poi, null);
		}
	}

	/**
	 * 更新searchListview
	 * 
	 * @param data
	 */
	public void upadteSearchList(RMPois data) {
		// 联想搜索完毕之后填充数据显示
		if (data.getPoilist() != null && data.getPoilist().size() > 0)
			mAssociativeListAdapter.setData(data.getPoilist());
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
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