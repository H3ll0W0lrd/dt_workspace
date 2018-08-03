package com.rtmap.wisdom.activity;

import java.sql.SQLException;
import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.rtm.common.model.BuildInfo;
import com.rtm.frm.model.CityInfo;
import com.rtmap.wisdom.R;
import com.rtmap.wisdom.adapter.LikeGridAdapter;
import com.rtmap.wisdom.adapter.WDBuildAdapter;
import com.rtmap.wisdom.core.DTActivity;
import com.rtmap.wisdom.core.DTSqlite;
import com.rtmap.wisdom.model.MyBuild;
import com.rtmap.wisdom.model.UIBuildInfo;
import com.rtmap.wisdom.model.UIBuildList;
import com.rtmap.wisdom.util.DTLog;
import com.rtmap.wisdom.util.DTUIUtil;
import com.rtmap.wisdom.util.listview.BuildAnimListview;
import com.rtmap.wisdom.util.listview.BuildAnimListview.OnAnimEndListener;
import com.rtmap.wisdom.util.view.DTSatelliteLayout;

/**
 * 建筑物标签
 * @author dingtao
 *
 */
public class WDBuildLikeActivity extends DTActivity implements
		OnItemClickListener, OnClickListener, TextWatcher {

	private BuildAnimListview mBuildList;
	private WDBuildAdapter mBuildAdapter;
	private Dao<MyBuild, String> mBuildDao;
	private TextView mBuildUpdate;
	private TextView mBuildCancel;
	private RelativeLayout mSearchLayout, mTitleLayout;
	private EditText mSearchContent;
	private ImageView mSearchClear;

	private DTSatelliteLayout mStatelliteLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_build_like);

		mBuildDao = DTSqlite.getInstance().createBuildTable();

		mBuildList = (BuildAnimListview) findViewById(R.id.build_list);
		mBuildAdapter = new WDBuildAdapter();
		mBuildList.setOnItemClickListener(this);

		mStatelliteLayout = (DTSatelliteLayout) DTUIUtil
				.inflate(R.layout.build_statellite_layout);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				getResources().getDimensionPixelSize(
						R.dimen.build_statellite_height));
		mStatelliteLayout.setLayoutParams(params);
		// mStatelliteLayout.setImageSize(getActivity().getResources()
		// .getDimensionPixelSize(R.dimen.build_statellite_image));
		for (int i = 0; i < mStatelliteLayout.getChildCount(); i++) {
			mStatelliteLayout.getChildAt(i).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							int index = (Integer) v.getTag();
							if (mStatelliteLayout.isChildUpdate()) {// 更改状态下
								if (index < mStatelliteLayout.getCount()) {
									if (mStatelliteLayout.getCount() == 7) {
										mBuildAdapter.addList(mBuildInfoList);
										mBuildAdapter.notifyDataSetChanged();
									}
									mStatelliteLayout.getBuildList().remove(
											index);
									mStatelliteLayout.notifyDataChanged();
									saveBuild();
								}
							} else {
								if (index == mStatelliteLayout.getCount()) {
									mBuildList.startAnim();
								}
							}
						}
					});
		}
		mBuildList.addHeaderView(mStatelliteLayout);
		mBuildList.setAdapter(mBuildAdapter);
		mBuildList.setStatelliteView(mStatelliteLayout);
		mBuildList.setOnAnimEndListener(new OnAnimEndListener() {

			@Override
			public void onAnimEnd() {
				mSearchLayout.setVisibility(View.VISIBLE);
				mTitleLayout.setVisibility(View.GONE);
			}
		});

		mTitleLayout = (RelativeLayout) findViewById(R.id.build_title_layout);
		findViewById(R.id.back).setOnClickListener(this);
		initSearch();

		mBuildCancel = (TextView) findViewById(R.id.build_cancel);
		mBuildCancel.setOnClickListener(this);

		mBuildUpdate = (TextView) findViewById(R.id.build_update);
		mBuildUpdate.setOnClickListener(this);

		String cityName = mShare.getString("city", "北京");
		try {

			MyBuild b = mBuildDao.queryForId("build_like");
			if (b != null) {
				mStatelliteLayout.getBuildList().clear();
				mStatelliteLayout.addList(mGson.fromJson(b.getContent(),
						UIBuildList.class).getList());
			}
			mStatelliteLayout.notifyDataChanged();

			MyBuild build = mBuildDao.queryForId("city" + cityName);

			if (build != null) {// 有数据
				DTLog.i("有建筑物数据：" + "city" + cityName);
				CityInfo city = mGson.fromJson(build.getContent(),
						CityInfo.class);
				if (mStatelliteLayout.getCount() < 7) {
					mBuildAdapter.addList(city.getBuildlist());
					mBuildAdapter.notifyDataSetChanged();
				} else {
					mBuildList.setRunAnim(true);
				}
				mBuildInfoList.clear();
				if (city.getBuildlist() != null)
					mBuildInfoList.addAll(city.getBuildlist());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		initLikeDialog();
	}

	private void initSearch() {
		mSearchLayout = (RelativeLayout) findViewById(R.id.build_search_layout);
		mSearchContent = (EditText) findViewById(R.id.build_content);
		mSearchClear = (ImageView) findViewById(R.id.build_content_clear);
		mSearchClear.setOnClickListener(this);
		mSearchContent.addTextChangedListener(this);
	}

	@Override
	public String getPageName() {
		return null;
	}

	private Dialog mLikeDialog;
	private GridView mLikeGrid;
	private TextView mLikeSave;

	/**
	 * 初始化beacon信息弹出框
	 */
	private void initLikeDialog() {
		mLikeDialog = new Dialog(this, R.style.dialog_white);
		mLikeDialog.setContentView(R.layout.dialog_like_cate);
		mLikeDialog.setCanceledOnTouchOutside(true);
		mLikeGrid = (GridView) mLikeDialog.findViewById(R.id.build_grid);
		mLikeSave = (TextView) mLikeDialog.findViewById(R.id.build_save);
		mLikeGrid.setAdapter(new LikeGridAdapter());
		mLikeSave.setOnClickListener(this);
		mLikeDialog.setCanceledOnTouchOutside(false);
		mLikeDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				saveBuild();
			}
		});
		mLikeGrid.setOnItemClickListener(this);

		Window win = mLikeDialog.getWindow();
		android.view.WindowManager.LayoutParams params = win.getAttributes();
		DTLog.e("dialog.x : " + params.x + "   dialog.y: " + params.y);
		WindowManager wm = this.getWindowManager();
		int width = wm.getDefaultDisplay().getWidth();
		params.width = LayoutParams.MATCH_PARENT;
		params.height = LayoutParams.MATCH_PARENT;
		win.setAttributes(params);
	}
	
	private void saveBuild() {
		try {
			MyBuild build = new MyBuild();
			UIBuildList l = new UIBuildList();
			l.setList(mStatelliteLayout.getBuildList());
			build.setContent(mGson.toJson(l));
			build.setBuildId("build_like");
			build.setTime(System.currentTimeMillis());
			mBuildDao.createOrUpdate(build);
			if (!mBuildList.isOpen()) {
				startListOpenAnim();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(arg0.getWindowToken(), 0);
		if (arg0.getId() == R.id.build_list) {
			if (position <= mBuildList.getHeaderViewsCount() - 1)
				return;
			if (mStatelliteLayout.isChildUpdate()) {
				DTUIUtil.showToastSafe("请保存后再添加");
				return;
			}
			BuildInfo b = mBuildAdapter.getItem(position
					- mBuildList.getHeaderViewsCount());
			UIBuildInfo info = new UIBuildInfo();
			info.setBuild(b);
			mStatelliteLayout.addItem(info);
			mStatelliteLayout.notifyDataChanged();
			mBuildUpdate.setVisibility(View.GONE);
			mLikeDialog.show();
			if (mStatelliteLayout.getCount() == 7) {
				mBuildAdapter.clearList();
				mBuildAdapter.notifyDataSetChanged();
			}
			mLikeDialog.show();
			if (!mBuildList.isOpen()) {
				startListOpenAnim();
			}
		} else if (arg0.getId() == R.id.build_grid) {
			mStatelliteLayout.getItem(mStatelliteLayout.getCount() - 1)
					.setBgIndex(position);
			mStatelliteLayout.notifyDataChanged();
		}
	}

	private ArrayList<BuildInfo> mBuildInfoList = new ArrayList<BuildInfo>();

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.build_update:// 编辑
			if (mStatelliteLayout.isChildUpdate()) {// 如果处于修改状态中
				mBuildUpdate.setText("编辑");
				if (mStatelliteLayout.getCount() == 7) {
					mBuildList.setRunAnim(true);
				} else {
					mBuildList.setRunAnim(false);
				}
				mStatelliteLayout.setChildUpdate(false);
				mStatelliteLayout.notifyDataChanged();
			} else {
				mBuildUpdate.setText("保存");
				mBuildList.setRunAnim(true);
				mStatelliteLayout.setChildUpdate(true);
				mStatelliteLayout.notifyDataChanged();
			}
			break;
		case R.id.build_content_clear:
			clearSearch();
			break;
		case R.id.build_cancel:
			startListOpenAnim();
			break;
		case R.id.build_save:
			mLikeDialog.cancel();
			mBuildUpdate.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	public void startListOpenAnim() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mTitleLayout.getWindowToken(), 0);
		clearSearch();
		mSearchLayout.setVisibility(View.GONE);
		mTitleLayout.setVisibility(View.VISIBLE);
		mBuildList.setSelection(0);
		mBuildList.endAnim(null, null, true);
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
