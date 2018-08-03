package com.rtmap.wisdom.fragment;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rtm.common.model.BuildInfo;
import com.rtm.common.model.POI;
import com.rtm.common.model.RMPois;
import com.rtm.common.utils.OnSearchPoiListener;
import com.rtm.frm.utils.RMSearchCatePoiUtil;
import com.rtm.frm.utils.RMSearchPoiUtil;
import com.rtm.location.LocationApp;
import com.rtmap.wisdom.R;
import com.rtmap.wisdom.adapter.SearchGridAdapter;
import com.rtmap.wisdom.adapter.SearchListAdapter;
import com.rtmap.wisdom.adapter.SearchShortCutAdapter;
import com.rtmap.wisdom.core.DTBaseFragment;
import com.rtmap.wisdom.util.DTUIUtil;

/**
 * 搜索页
 * @author dingtao
 *
 */
public class WDSearchFragment extends DTBaseFragment implements
		OnItemClickListener, OnSearchPoiListener, OnClickListener, TextWatcher {

	private DrawerLayout mDrawer;

	private EditText mSearchContent;
	private GridView mSearchGrid;
	private RelativeLayout mCateLayout;
	private SearchGridAdapter mGridAdapter;
	private RMSearchPoiUtil mSearchPoiUtil;
	private RMSearchCatePoiUtil mSearchCateUtil;
	private RecyclerView mShortCutView;
	private SearchShortCutAdapter mShortCutAdapter;
	private TextView mBackSearch;
	private ListView mSearchList;
	private LinearLayout mListLayout;
	private SearchListAdapter mListAdapter;
	private ImageView mSearchClear;
	private RelativeLayout mTitlebar, mErrorLayout;
	
	private ProgressDialog mLoadDialog;

	@Override
	protected View createLoadedView() {
		View view = DTUIUtil.inflate(R.layout.main_search);
		mSearchContent = (EditText) view.findViewById(R.id.content);
		mSearchClear = (ImageView) view.findViewById(R.id.search_clear);
		mBackSearch = (TextView) view.findViewById(R.id.back_search);
		mBackSearch.setOnClickListener(this);
		mSearchClear.setOnClickListener(this);

		mErrorLayout = (RelativeLayout) view.findViewById(R.id.no_layout);
		mTitlebar = (RelativeLayout) view.findViewById(R.id.title_bar);

		mSearchGrid = (GridView) view.findViewById(R.id.search_grid);
		mSearchList = (ListView) view.findViewById(R.id.search_list);
		mListAdapter = new SearchListAdapter();
		mSearchList.setAdapter(mListAdapter);
		mSearchList.setOnItemClickListener(this);

		mListLayout = (LinearLayout) view.findViewById(R.id.list_layout);
		mListLayout.setOnClickListener(this);

		mGridAdapter = new SearchGridAdapter();
		mSearchGrid.setOnItemClickListener(this);
		mGridAdapter.addItem(R.drawable.search_sport);
		mGridAdapter.addItem(R.drawable.search_part);
		mGridAdapter.addItem(R.drawable.search_food);
		mGridAdapter.addItem(R.drawable.search_jewelry);
		mGridAdapter.addItem(R.drawable.search_shoeshats);
		mGridAdapter.addItem(R.drawable.search_cosmetics);
		mGridAdapter.addItem(R.drawable.search_movie);
		mGridAdapter.addItem(R.drawable.search_cash);
		mGridAdapter.addItem(R.drawable.search_exit);
		mSearchGrid.setAdapter(mGridAdapter);
		mCateLayout = (RelativeLayout) view.findViewById(R.id.grid_layout);

		mShortCutView = (RecyclerView) view.findViewById(R.id.recycler);

		mShortCutAdapter = new SearchShortCutAdapter(this);
		GridLayoutManager layoutManager = new GridLayoutManager(getActivity(),
				3);
		layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				switch (mShortCutAdapter.getItemViewType(position)) {
				case SearchShortCutAdapter.TYPE_LIST:
					return 3;
				case SearchShortCutAdapter.TYPE_GRID:
					return 1;
				default:
					return -1;
				}
			}
		});
		mShortCutView.setLayoutManager(layoutManager);
		mShortCutView.setAdapter(mShortCutAdapter);

		mSearchContent.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mCateLayout.setVisibility(View.VISIBLE);
					if (mShortCutView.getVisibility() == View.VISIBLE) {
						mShortCutView.setVisibility(View.GONE);
						mShortCutAdapter.clear();
					}
					mListLayout.setVisibility(View.VISIBLE);
					// 查找历史结果
				}
			}
		});

		mSearchContent.addTextChangedListener(this);

		mSearchPoiUtil = new RMSearchPoiUtil();
		mSearchPoiUtil.setPagesize(50)
				.setKey(LocationApp.getInstance().getApiKey())
				.setOnSearchPoiListener(this);

		mSearchCateUtil = new RMSearchCatePoiUtil();
		mSearchCateUtil.setPagesize(50)
				.setKey(LocationApp.getInstance().getApiKey())
				.setOnSearchPoiListener(this);
		return view;
	}

	public void setDrawer(DrawerLayout drawer,ProgressDialog mLoadDialog) {
		this.mDrawer = drawer;
		this.mLoadDialog = mLoadDialog;
	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long arg3) {
		WDMainFragment mMainFragment = (WDMainFragment) getFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		if (arg0.getId() == R.id.search_grid) {
			BuildInfo build = mMainFragment.getBuild();
			// RMBuildPoiCateUtil.requestBuildPoiCate(LocationApp.getInstance().getApiKey(),
			// build.getBuildId(), null);
			if (build == null) {
				DTUIUtil.showToastSafe("缺少建筑物信息无法搜索");
				return;
			}
			mLoadDialog.show();
			if (position == 1 || position == 7 || position == 8) {
				String key = null;
				if (position == 1) {
					key = "停车场";
				} else if (position == 7) {
					key = "收银台";
				} else if (position == 8) {
					key = "出口";
				}
				mSearchPoiUtil.setBuildid(build.getBuildId()).setKeywords(key)
						.searchPoi();// 开启搜索
			} else {
				ArrayList<String> list = new ArrayList<String>();
				if (position == 0) {
					list.add("120501");
				} else if (position == 2) {
					list.add("100208");
					list.add("100122");
					list.add("100103");
					list.add("190301");
					list.add("100503");
					list.add("100125");
					list.add("151500");
				} else if (position == 3) {
					list.add("4101");
				} else if (position == 4) {
					list.add("4402");
				} else if (position == 5) {
					list.add("4102");
				} else if (position == 6) {
					list.add("4301");
				}
				mSearchCateUtil.setBuildid(build.getBuildId()).setClassid(list)
						.searchPoi();
			}
		} else if (arg0.getId() == R.id.search_list) {// list的item点击
			InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

			POI poi = mListAdapter.getItem(position);
			mDrawer.closeDrawer(Gravity.END);
			mMainFragment.addSearchPoi(poi);
		}
	}

	@Override
	public void onSearchPoi(RMPois result) {
		mLoadDialog.cancel();
		if (result.getError_code() == 0) {
			ArrayList<POI> list = result.getPoilist();
			if (list!=null&&list.size() != 0) {
				mErrorLayout.setVisibility(View.GONE);
				if (mListLayout.getVisibility() == View.VISIBLE) {// 搜索列表
					mListAdapter.clearList();
					mListAdapter.addList(list);
					mListAdapter.notifyDataSetChanged();
				} else {
					mCateLayout.setVisibility(View.GONE);
					ArrayList<POI> l = new ArrayList<POI>();
					for (int i = 0; i < list.size(); i++) {
						if (i == 0) {
							POI poi = new POI();
							poi.setType(1);
							poi.setFloor(list.get(i).getFloor());
							l.add(poi);
						} else {
							POI p = list.get(i);
							POI lastp = list.get(i - 1);
							if (!p.getFloor().equals(lastp.getFloor())) {
								POI poi = new POI();
								poi.setType(1);
								poi.setFloor(list.get(i).getFloor());
								l.add(poi);
							}
						}
						l.add(list.get(i));
					}
					mShortCutView.setVisibility(View.VISIBLE);
					mShortCutAdapter.addList(l);
					mBackSearch.setVisibility(View.VISIBLE);
					mShortCutAdapter.notifyDataSetChanged();
				}
			} else {// 没有结果
				mErrorLayout.setVisibility(View.VISIBLE);
			}
		} else {
			mErrorLayout.setVisibility(View.VISIBLE);
			DTUIUtil.showToastSafe(result.getError_msg());
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_search:
			InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			if (mErrorLayout.getVisibility() == View.VISIBLE) {
				mErrorLayout.setVisibility(View.GONE);
			} else if (mCateLayout.getVisibility() == View.VISIBLE) {
				mDrawer.closeDrawer(Gravity.END);
			} else if (mShortCutView.getVisibility() == View.VISIBLE) {
				mShortCutView.setVisibility(View.GONE);
				mCateLayout.setVisibility(View.VISIBLE);
				mShortCutAdapter.clear();
			} else {
				mDrawer.closeDrawer(Gravity.END);
			}
			break;
		case R.id.search_clear:
			mSearchContent.setText("");
			mListAdapter.clearList();
			mListAdapter.notifyDataSetChanged();
			mSearchClear.setVisibility(View.GONE);
			break;
		case R.id.list_layout:
			mListLayout.setVisibility(View.GONE);
			mSearchContent.setText("");
			mListAdapter.clearList();
			mSearchClear.setVisibility(View.GONE);
			mTitlebar.setFocusable(true);
			mTitlebar.setFocusableInTouchMode(true);
			mTitlebar.requestFocus();
			InputMethodManager imm1 = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm1.hideSoftInputFromWindow(v.getWindowToken(), 0);
			break;
		case R.id.search_shortcut_item_layout:
			InputMethodManager imm2 = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm2.hideSoftInputFromWindow(v.getWindowToken(), 0);
			WDMainFragment mMainFragment = (WDMainFragment) getFragmentManager()
					.findFragmentById(R.id.navigation_drawer);
			int position = (Integer) v.getTag();
			POI poi = mShortCutAdapter.getItem(position);
			mDrawer.closeDrawer(Gravity.END);
			mMainFragment.addSearchPoi(poi);
			break;
		default:
			break;
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (s.toString().length() > 0) {
			mSearchClear.setVisibility(View.VISIBLE);
			WDMainFragment mMainFragment = (WDMainFragment) getFragmentManager()
					.findFragmentById(R.id.navigation_drawer);
			BuildInfo build = mMainFragment.getBuild();
			if (build == null) {
				return;
			}
			mSearchPoiUtil.setBuildid(build.getBuildId())
					.setKeywords(s.toString()).searchPoi();
		} else {
			mListAdapter.clearList();
			mListAdapter.notifyDataSetChanged();
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
