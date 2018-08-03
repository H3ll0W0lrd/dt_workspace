/**
 * @date 2014.08.18 21:15
 * @explain 由于程序进行调整，原来当前建筑物搜索与楼层切换都在一个页面，现在分开，FloorChangeFragment只负责楼层切换功能
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
import com.rtm.frm.adapter.FloorChangeAdapter;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Floor;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.tab0.TestRtmapFragment;
import com.rtm.frm.tab0.TestRtmapFragmentManager;
import com.rtm.frm.utils.RMSearchPoiUtil;
import com.rtm.frm.utils.ToastUtil;

@SuppressLint({ "HandlerLeak", "ClickableViewAccessibility" })
public class FloorChangeFragment extends BaseFragment implements
		OnItemClickListener, OnTouchListener {

	private ListView mFloorListView;

	private ListView mSearchListView;

	private EditText mSearchEditText;
	RMSearchPoiUtil mSearch;

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
				mSearch.setBuildid(mBuildId)
						.setKey(XunluMap.getInstance().getApiKey())
						.setKeywords(editable.toString())
						.setOnSearchPoiListener(new OnSearchPoiListener() {

							@Override
							public void onSearchPoi(RMPois result) {
								if (result.getError_code() == 0) {
									upadteSearchList(result);
								} else {
									ToastUtil.shortToast(result.getError_msg());
								}
							}
						});
			}
		}
	};

	private FloorChangeAdapter mFloorListAdapter;

	private AssociativeSearchAdapter mSearchListAdapter;

	private String mBuildId;

	public FloorChangeFragment() {
	}// 空构造参数

	public FloorChangeFragment(String buildId) {
		mBuildId = buildId;
		// setStyle(DialogFragment.STYLE_NORMAL,R.style.dialogfragment_completely_transparent_bg);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.fragment_floor_change,
				container, false);
		initView(contentView);
		initData();
		return contentView;
	}

	private void initView(View contentView) {
		mFloorListView = (ListView) contentView
				.findViewById(R.id.floor_list_view);
		mSearchListView = (ListView) contentView
				.findViewById(R.id.search_list_view);
		mSearchEditText = (EditText) contentView.findViewById(R.id.search_edit);

		mSearchEditText.addTextChangedListener(mSearchWatcher);

		mFloorListAdapter = new FloorChangeAdapter();
		mFloorListView.setAdapter(mFloorListAdapter);
		mFloorListView.setOnItemClickListener(this);

		mSearchListAdapter = new AssociativeSearchAdapter();
		mSearchListView.setAdapter(mSearchListAdapter);
		mSearchListView.setOnItemClickListener(this);
		contentView.setOnTouchListener(this);
	}

	/**
	 * 初始化list数据
	 */
	private void initData() {
		List<Floor> floors = TestRtmapFragmentManager.getInstance()
				.queryFloorsByBuildId(mBuildId);
		mFloorListAdapter.setData(floors);
		mFloorListAdapter.notifyDataSetChanged();
		mSearch = new RMSearchPoiUtil();
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {
		TestRtmapFragment fragment = NewFrameActivity.getInstance().getTab0();
		if (adapterView.getAdapter() == mFloorListAdapter) {
			Floor floor = (Floor) adapterView.getAdapter().getItem(position);
			fragment.switchBuild(floor.buildName, floor.buildId, floor.floor);
		} else {
			POI poi = (POI) adapterView.getAdapter().getItem(position);
			fragment.showSearchPoiPoint(poi, null);
		}
		MyFragmentManager.getInstance().backFragment();
		// dismiss();
	}

	/**
	 * 更新searchListview
	 * 
	 * @param data
	 */
	public void upadteSearchList(RMPois data) {
		// 联想搜索完毕之后填充数据显示
		mSearchListAdapter.setData(data.getPoilist());
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			MyFragmentManager.getInstance().backFragment();
		}
		// this.dismiss();
		return true;
	}

}