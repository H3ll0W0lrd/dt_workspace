/**
 * @author hukunge
 * @date 2014.08.18 21:15
 */
package com.rtm.frm.fragment.buildlist;

import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.rtm.frm.R;
import com.rtm.frm.adapter.BuildListAdapter;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.controller.BuildListManager;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.model.Build;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.tab0.TestRtmapFragment;
import com.rtm.frm.utils.ConstantsUtil;

public class BuildListFragment extends BaseFragment implements
		OnItemClickListener,View.OnClickListener ,OnTouchListener{
	private Button slidButton;
	private ListView mBuildListView;
	private BuildListAdapter mBuildListAdapter;
	
	private String cityName;
	
	private int buildType;
	
	public BuildListFragment(String cityName,int buildType) {
		this.cityName = cityName;
		this.buildType = buildType;
	}
	
	public BuildListFragment(){}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.fragment_buildlist,
				container, false);
		contentView.setOnTouchListener(this);
		initView(contentView);
		initData();
		return contentView;
	}

	private void initView(View contentView) {
		mBuildListView = (ListView) contentView
				.findViewById(R.id.build_list_view);

		mBuildListAdapter = new BuildListAdapter();
		mBuildListView.setAdapter(mBuildListAdapter);
		mBuildListView.setOnItemClickListener(this);

		slidButton = (Button) contentView.findViewById(R.id.btn_slid);
		slidButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				MyFragmentManager.showFragmentdialog(
					    new CitysFragment(),
					    MyFragmentManager.PROCESS_DIALOGFRAGEMENT_CHOOSECITY,
					    MyFragmentManager.DIALOGFRAGMENT_CHOOSECITY);
			}
		});

	}

	/**
	 * 初始化list数据
	 */
	private void initData() {
		List<Build> builds;
		if(buildType == ConstantsUtil.BUILD_TYPE_PRIVATE) {
			builds = BuildListManager.getInstance()
					.queryPrivateBuildAll();
			slidButton.setText("我的列表");
			slidButton.setClickable(false);
		} else {
			builds = BuildListManager.getInstance()
					.queryBuildByCityName(cityName, buildType);
		}
		mBuildListAdapter.setData(builds);
		mBuildListAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {
		Build build = (Build) adapterView.getAdapter().getItem(position);
		TestRtmapFragment rtmFragment = NewFrameActivity.getInstance().getTab0();
		String defalutFloor  = build.floors.split("_")[0];
		rtmFragment.closeNav();
//		if(buildType == ConstantsUtil.BUILD_TYPE_PRIVATE) {
//			TestRtmapFragment.isMapShowPrivate = true;
//		} else {
//			TestRtmapFragment.isMapShowPrivate = false;
//		}
		rtmFragment.switchBuild(build.name, build.id, defalutFloor);
		MyFragmentManager.getInstance().backFragmentUpFlag(MyFragmentManager.PROCESS_RT_MAP+"-"+MyFragmentManager.FRAGMENT_RT_MAP);
	}
	
	/**
	 * 根据cityName更新build list
	 * @param cityName
	 */
	public void upadteBuilds(String cityName) {
		//改变城市名
		slidButton.setText(cityName);
		List<Build> builds = BuildListManager.getInstance()
				.queryBuildByCityName(cityName, buildType);
		mBuildListAdapter.setData(builds);
		mBuildListAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}
	
	
}