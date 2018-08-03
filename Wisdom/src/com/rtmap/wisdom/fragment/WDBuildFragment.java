package com.rtmap.wisdom.fragment;

import java.sql.SQLException;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.rtm.common.model.BuildInfo;
import com.rtm.common.model.RMLocation;
import com.rtm.frm.model.CityInfo;
import com.rtm.frm.model.RMBuildDetail;
import com.rtm.frm.model.RMBuildList;
import com.rtm.frm.model.RMCityList;
import com.rtm.frm.utils.RMBuildDetailUtil;
import com.rtm.frm.utils.RMBuildListUtil;
import com.rtm.frm.utils.RMBuildListUtil.OnGetBuildListListener;
import com.rtm.frm.utils.RMCityListUtil;
import com.rtm.frm.utils.RMCityListUtil.OnGetCityListListener;
import com.rtm.location.LocationApp;
import com.rtmap.wisdom.R;
import com.rtmap.wisdom.activity.WDBuildLikeActivity;
import com.rtmap.wisdom.activity.WDMainActivity;
import com.rtmap.wisdom.activity.WDMeActivity;
import com.rtmap.wisdom.activity.WDWelcomeActivity;
import com.rtmap.wisdom.adapter.CityListAdapter;
import com.rtmap.wisdom.adapter.WDBuildAdapter;
import com.rtmap.wisdom.core.DTBaseFragment;
import com.rtmap.wisdom.model.MyBuild;
import com.rtmap.wisdom.model.UIBuildList;
import com.rtmap.wisdom.util.DTLog;
import com.rtmap.wisdom.util.DTUIUtil;
import com.rtmap.wisdom.util.anim.Rotate3dAnimation;
import com.rtmap.wisdom.util.listview.BuildAnimListview;
import com.rtmap.wisdom.util.listview.BuildAnimListview.OnAnimEndListener;
import com.rtmap.wisdom.util.view.DTSatelliteLayout;

/**
 * 建筑物列表页
 * 
 * @author dingtao
 *
 */
public class WDBuildFragment extends DTBaseFragment implements
		OnItemClickListener, OnGetBuildListListener, OnGetCityListListener,
		OnClickListener, TextWatcher {

	private ListView mCityList;
	private BuildAnimListview mBuildList;
	private CityListAdapter mCityAdapter;
	private WDBuildAdapter mBuildAdapter;
	private Dao<MyBuild, String> mBuildDao;
	private DrawerLayout mDrawer;
	private TextView mTitle;
	private Animation mCityVisAnimation, mCityGoneAnimation;
	private TextView mMyLoc;
	private TextView mBuildCancel;
	private RelativeLayout mSearchLayout, mTitleLayout;
	private EditText mSearchContent;
	private ImageView mSearchClear;

	private RelativeLayout mLinearLayout;
	private DTSatelliteLayout mStatelliteLayout;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				for (int i = 0; i < mStatelliteLayout.getCount(); i++) {
					final FrameLayout layout = (FrameLayout) mStatelliteLayout
							.getChildAt(i);
					Animation anim = AnimationUtils.loadAnimation(
							getActivity(), R.anim.alpha_trans_anim);
					anim.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {

						}

						@Override
						public void onAnimationRepeat(Animation animation) {

						}

						@Override
						public void onAnimationEnd(Animation animation) {
							layout.getChildAt(1).setAlpha(0);
						}
					});
					layout.getChildAt(1).startAnimation(anim);
				}
				sendEmptyMessageDelayed(2, 4000);
			} else if (msg.what == 2) {
				for (int i = 0; i < mStatelliteLayout.getCount(); i++) {
					final FrameLayout layout = (FrameLayout) mStatelliteLayout
							.getChildAt(i);
					Animation anim = AnimationUtils.loadAnimation(
							getActivity(), R.anim.alpha_entity_anim);
					anim.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {

						}

						@Override
						public void onAnimationRepeat(Animation animation) {

						}

						@Override
						public void onAnimationEnd(Animation animation) {

						}
					});
					layout.getChildAt(1).setAlpha(1);
					layout.getChildAt(1).startAnimation(anim);
				}
				sendEmptyMessageDelayed(1, 4000);
			} else if (msg.what == 3) {
				View view = getView();
				
				mCityList = (ListView) view.findViewById(R.id.city_list);
				mCityList.setVisibility(View.GONE);
				mCityAdapter = new CityListAdapter();
				mCityList.setAdapter(mCityAdapter);
				mCityList.setOnItemClickListener(WDBuildFragment.this);
				view.findViewById(R.id.build_set).setOnClickListener(
						WDBuildFragment.this);

				// mStatelliteLayout.setImageSize(getActivity().getResources()
				// .getDimensionPixelSize(R.dimen.build_statellite_image));
				for (int i = 0; i < mStatelliteLayout.getChildCount(); i++) {
					mStatelliteLayout.getChildAt(i).setOnClickListener(
							new OnClickListener() {

								@Override
								public void onClick(View v) {
									int index = (Integer) v.getTag();
									if (index == mStatelliteLayout
											.getBuildList().size()) {
										Intent intent = new Intent(
												getActivity(),
												WDBuildLikeActivity.class);
										getActivity().startActivity(intent);
									} else if (index < mStatelliteLayout
											.getBuildList().size()) {
										loadBuildDetail(mStatelliteLayout
												.getBuildList().get(index)
												.getBuild());
									}
								}
							});
				}
				
				mBuildList.setOnAnimEndListener(new OnAnimEndListener() {

					@Override
					public void onAnimEnd() {
						mSearchLayout.setVisibility(View.VISIBLE);
						mTitleLayout.setVisibility(View.GONE);
					}
				});

				mTitleLayout = (RelativeLayout) view
						.findViewById(R.id.build_title_layout);
				initSearch(view);

				mBuildCancel = (TextView) view.findViewById(R.id.build_cancel);
				mBuildCancel.setOnClickListener(WDBuildFragment.this);

				mLinearLayout = (RelativeLayout) view
						.findViewById(R.id.linearlayout1);

				mMyLoc = (TextView) view.findViewById(R.id.build_loc);
				mMyLoc.setOnClickListener(WDBuildFragment.this);

				mCityVisAnimation = AnimationUtils.loadAnimation(getActivity(),
						R.anim.trans_top_to_bottom);
				mCityGoneAnimation = AnimationUtils.loadAnimation(
						getActivity(), R.anim.trans_bottom_to_top);

				mCityGoneAnimation
						.setAnimationListener(new AnimationListener() {

							@Override
							public void onAnimationStart(Animation animation) {

							}

							@Override
							public void onAnimationRepeat(Animation animation) {

							}

							@Override
							public void onAnimationEnd(Animation animation) {
								mCityList.setVisibility(View.GONE);
								mLinearLayout.setVisibility(View.VISIBLE);
								mMyLoc.setVisibility(View.VISIBLE);
								String city = (String) mTitle.getTag();
								mShare.edit().putString("city", city).commit();
								if (!mTitle.getText().toString().equals(city)) {
									mTitle.setText(city);
									mBuildAdapter.clearList();
									loadBuildData(city);
								}
							}
						});
			} else if (msg.what == 4) {
				loadBuildData(mTitle.getText().toString());
				loadCityData();
			}
		};
	};

	@Override
	protected View createLoadedView() {
		View view = DTUIUtil.inflate(R.layout.main_build);
		mTitle = (TextView) view.findViewById(R.id.title);
		mTitle.setOnClickListener(WDBuildFragment.this);
		mStatelliteLayout = (DTSatelliteLayout) DTUIUtil
				.inflate(R.layout.build_statellite_layout);
		mStatelliteLayout.setLineColor(0xff1a1939);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				getActivity().getResources().getDimensionPixelSize(
						R.dimen.build_statellite_height));
		mStatelliteLayout.setLayoutParams(params);
		
		mBuildList = (BuildAnimListview) view
				.findViewById(R.id.build_list);
		mBuildAdapter = new WDBuildAdapter();
		mBuildList.setOnItemClickListener(WDBuildFragment.this);
		mBuildList.addHeaderView(mStatelliteLayout);
		mBuildList.setAdapter(mBuildAdapter);
		mBuildList.setStatelliteView(mStatelliteLayout);
		
		mHandler.sendEmptyMessageDelayed(3, 500);
		mHandler.sendEmptyMessageDelayed(1, 5000);
		return view;
	}

	private void initSearch(View view) {

		mSearchLayout = (RelativeLayout) view
				.findViewById(R.id.build_search_layout);
		mSearchContent = (EditText) view.findViewById(R.id.build_content);
		mSearchClear = (ImageView) view.findViewById(R.id.build_content_clear);
		mSearchClear.setOnClickListener(WDBuildFragment.this);
		mSearchContent.addTextChangedListener(WDBuildFragment.this);
	}

	private void loadBuildData(String cityName) {
		try {
			MyBuild build = mBuildDao.queryForId("city" + cityName);
			if (build != null) {// 有数据
				DTLog.i("有建筑物数据：" + "city" + cityName);
				CityInfo city = mGson.fromJson(build.getContent(),
						CityInfo.class);
				mBuildAdapter.addList(city.getBuildlist());
				mBuildAdapter.notifyDataSetChanged();
				mBuildInfoList.clear();
				mBuildInfoList.addAll(mBuildAdapter.getList());
				if (System.currentTimeMillis() - build.getTime() > 3 * 24 * 60
						* 60 * 1000) {// 超过3天，更新数据
					RMBuildListUtil.requestBuildList(LocationApp.getInstance()
							.getApiKey(), cityName, WDBuildFragment.this);
				}
			} else {// 没有数据
				mLoadDialog.show();
				RMBuildListUtil.requestBuildList(LocationApp.getInstance()
						.getApiKey(), cityName, WDBuildFragment.this);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private ProgressDialog mLoadDialog;

	public void setDrawer(DrawerLayout drawer, ProgressDialog mLoadDialog,
			Dao<MyBuild, String> buildDao) {
		WDBuildFragment.this.mDrawer = drawer;
		WDBuildFragment.this.mBuildDao = buildDao;
		WDBuildFragment.this.mLoadDialog = mLoadDialog;
		String title = mShare.getString("city", "北京");
		mTitle.setText(title);
		mTitle.setTag(title);
		mHandler.sendEmptyMessageDelayed(4, 1000);
	}

	private void loadCityData() {
		try {
			MyBuild build = mBuildDao.queryForId("city" + "all");
			if (build != null) {// 有数据
				DTLog.i("有建筑物数据：" + "city" + "all");
				RMCityList city = mGson.fromJson(build.getContent(),
						RMCityList.class);
				mCityAdapter.addList(city.getCitylist());
				mCityAdapter.notifyDataSetChanged();
				if (System.currentTimeMillis() - build.getTime() > 10 * 24 * 60
						* 60 * 1000) {// 超过3天，更新数据
					RMCityListUtil.requestCityList(LocationApp.getInstance()
							.getApiKey(), WDBuildFragment.this);
				}
			} else {// 没有数据
				RMCityListUtil.requestCityList(LocationApp.getInstance()
						.getApiKey(), WDBuildFragment.this);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(arg0.getWindowToken(), 0);
		if (arg0.getId() == R.id.build_list) {
			if (position <= mBuildList.getHeaderViewsCount() - 1)
				return;
			mDrawer.closeDrawer(Gravity.START);
			BuildInfo b = mBuildAdapter.getItem(position
					- mBuildList.getHeaderViewsCount());
			loadBuildDetail(b);
		} else if (arg0.getId() == R.id.city_list) {
			String city = mCityAdapter.getItem(position);
			mTitle.setTag(city);
			mCityList.startAnimation(mCityGoneAnimation);
		}
	}

	/**
	 * 加载建筑物详情
	 * 
	 * @param b
	 */
	private void loadBuildDetail(BuildInfo b) {
		try {
			MyBuild build = mBuildDao.queryForId(b.getBuildId());
			mDrawer.closeDrawer(Gravity.START);
			WDMainFragment mMainFragment = (WDMainFragment) getFragmentManager()
					.findFragmentById(R.id.navigation_drawer);
			mMainFragment.setAutoFloor(false);
			if (build != null) {// 有数据
				DTLog.i("有建筑物数据：" + b.getBuildId());
				b = mGson.fromJson(build.getContent(), BuildInfo.class);
				if (System.currentTimeMillis() - build.getTime() > 3 * 24 * 60
						* 60 * 1000) {// 超过3天，更新数据
					RMBuildDetailUtil.requestBuildDetail(LocationApp
							.getInstance().getApiKey(), build.getBuildId(),
							mMainFragment);
				}
				RMBuildDetail detail = new RMBuildDetail();
				detail.setError_code(0);
				detail.setBuild(b);
				mMainFragment.onFinished(detail);
			} else {
				mLoadDialog.show();
				RMBuildDetailUtil.requestBuildDetail(LocationApp.getInstance()
						.getApiKey(), b.getBuildId(), mMainFragment);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private ArrayList<BuildInfo> mBuildInfoList = new ArrayList<BuildInfo>();

	@Override
	public void onFinished(RMBuildList result) {
		mLoadDialog.cancel();
		try {
			MyBuild build = mBuildDao.queryForId("city"
					+ mTitle.getText().toString());
			if (result.getError_code() == 0) {
				if (build == null) {// 没有数据
					build = new MyBuild();
				}
				if (mBuildAdapter.getCount() == 0) {
					mBuildAdapter.addList(result.getCitylist().get(0)
							.getBuildlist());
					mBuildAdapter.notifyDataSetChanged();
				}
				build.setBuildId("city" + mTitle.getText().toString());
				build.setContent(mGson.toJson(result.getCitylist().get(0)));
				build.setTime(System.currentTimeMillis());
				mBuildDao.createOrUpdate(build);
			} else {
				if (build == null) { // 没有数据
					DTUIUtil.showToastSafe(result.getError_msg());
				}
			}
			mBuildInfoList.clear();
			mBuildInfoList.addAll(mBuildAdapter.getList());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onFinished(RMCityList result) {
		try {
			MyBuild build = mBuildDao.queryForId("city" + "all");
			if (result.getError_code() == 0) {
				if (build == null) {// 没有数据
					build = new MyBuild();
					mCityAdapter.addList(result.getCitylist());
					mCityAdapter.notifyDataSetChanged();
				}
				build.setBuildId("city" + "all");
				build.setContent(mGson.toJson(result));
				build.setTime(System.currentTimeMillis());
				mBuildDao.createOrUpdate(build);
			} else {
				// if (build == null) // 没有数据
				// DTUIUtil.showToastSafe(result.getError_msg());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.build_set:
			Intent intent = new Intent(getActivity(), WDMeActivity.class);
			getActivity().startActivity(intent);
			break;
		case R.id.title:
			if (mCityList.getVisibility() == View.GONE) {
				mCityList.setVisibility(View.VISIBLE);
				mCityList.startAnimation(mCityVisAnimation);
				mMyLoc.setVisibility(View.GONE);
				mLinearLayout.setVisibility(View.GONE);
			} else {
				mCityList.startAnimation(mCityGoneAnimation);
			}
			break;
		case R.id.build_loc:
			RMLocation location = LocationApp.getInstance()
					.getCurrentLocation();
			if (location.getError() == 0) {
				BuildInfo b = new BuildInfo();
				b.setBuildId(location.getBuildID());
				loadBuildDetail(b);
			}
			mDrawer.closeDrawers();
			break;
		case R.id.build_content_clear:
			clearSearch();
			break;
		case R.id.build_cancel:
			InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			clearSearch();
			mSearchLayout.setVisibility(View.GONE);
			mTitleLayout.setVisibility(View.VISIBLE);
			mBuildList.setSelection(0);
			mBuildList.endAnim(null, null, true);
			break;
		default:
			break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			MyBuild b = mBuildDao.queryForId("build_like");
			if (b != null) {
				mStatelliteLayout.getBuildList().clear();
				mStatelliteLayout.getBuildList().addAll(
						mGson.fromJson(b.getContent(), UIBuildList.class)
								.getList());
			}
			mStatelliteLayout.notifyDataChanged();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void clearSearch() {
		mSearchContent.setText("");
		mBuildAdapter.clearList();
		mBuildAdapter.addList(mBuildInfoList);
		mBuildAdapter.notifyDataSetChanged();
		mSearchClear.setVisibility(View.GONE);
	}

	@Override
	public void afterTextChanged(Editable s) {
		String str = s.toString();
		if (str.length() > 0) {
			mSearchClear.setVisibility(View.VISIBLE);
			mBuildAdapter.clearList();
			mBuildAdapter.addList(mBuildInfoList);
			for (int i = 0; i < mBuildInfoList.size(); i++) {
				BuildInfo info = mBuildInfoList.get(i);
				if (!info.getBuildName().contains(str)
						&& !info.getName_jp().toLowerCase()
								.contains(str.toLowerCase())
						&& !info.getName_qp().toLowerCase()
								.contains(str.toLowerCase())) {
					mBuildAdapter.removeItem(info);
				}
			}
			mBuildAdapter.notifyDataSetChanged();
		} else {
			mSearchClear.setVisibility(View.GONE);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}
}
