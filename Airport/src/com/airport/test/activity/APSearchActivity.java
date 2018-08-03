package com.airport.test.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.airport.test.R;
import com.airport.test.adapter.POIAdapter;
import com.airport.test.core.AirSqlite;
import com.airport.test.model.AirData;
import com.dingtao.libs.DTActivity;
import com.dingtao.libs.util.DTStringUtil;
import com.dingtao.libs.util.DTUIUtil;
import com.google.gson.Gson;
import com.rtm.common.model.BuildInfo;
import com.rtm.common.model.POI;
import com.rtm.common.model.RMPois;
import com.rtm.common.utils.OnSearchPoiListener;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.utils.RMSearchPoiUtil;

public class APSearchActivity extends DTActivity implements OnClickListener,
		OnItemClickListener {

	private EditText edtKeyWord;
	private ListView mList;
	private ImageView imgBack, mCancel;
	private BuildInfo mBuild;
	private POIAdapter mAdapter;
	private RMSearchPoiUtil mSearchUtil;
	private ArrayList<POI> mSearchList;
	private View mFooterView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		Gson gson = new Gson();
		mBuild = gson.fromJson(AirData.AIR_DATA, BuildInfo.class);

		mSearchList = AirSqlite.getInstance().getSearchList();

		mCancel = (ImageView) findViewById(R.id.search_cancel);
		mCancel.setOnClickListener(this);
		edtKeyWord = (EditText) findViewById(R.id.edt_key_word);
		edtKeyWord.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (RMStringUtils.isEmpty(s.toString())) {
					mCancel.setVisibility(View.GONE);
				} else {
					mCancel.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		edtKeyWord
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {

						if (event != null
								&& event.getAction() == KeyEvent.ACTION_DOWN
								&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
							String str = edtKeyWord.getText().toString();
							if (DTStringUtil.isEmpty(str)) {
								DTUIUtil.showToastSafe("请输入搜索内容");
							} else {
								edtKeyWord.setSelection(str.length());
								searchpoi(str);
								return true;
							}
						}
						return false;
					}
				});
		mList = (ListView) findViewById(R.id.lv_search_result);

		View view = DTUIUtil.inflate(R.layout.search_header);
		view.findViewById(R.id.wenxuntai_layout).setOnClickListener(this);
		view.findViewById(R.id.weishengjian_layout).setOnClickListener(this);
		view.findViewById(R.id.zhijiguitai_layout).setOnClickListener(this);
		view.findViewById(R.id.anjian_layout).setOnClickListener(this);
		view.findViewById(R.id.meishi_layout).setOnClickListener(this);
		view.findViewById(R.id.dianti_layout).setOnClickListener(this);
		mList.addHeaderView(view);

		mFooterView = DTUIUtil.inflate(R.layout.search_delete_text);
		mList.addFooterView(mFooterView);

		mSearchUtil = new RMSearchPoiUtil();
		mSearchUtil.setBuildid(mBuild.getBuildId());
		mSearchUtil.setPagesize(50);
		mSearchUtil.setKey(XunluMap.getInstance().getApiKey());
		mSearchUtil.setOnSearchPoiListener(new OnSearchPoiListener() {

			@Override
			public void onSearchPoi(RMPois result) {
				mLoadDialog.cancel();
				if (result.getError_code() == 0) {
					mFooterView.setVisibility(View.GONE);
					mAdapter.clearList();
					mAdapter.setCurrentIndex(true);
					mAdapter.addList(result.getPoilist());
					mAdapter.notifyDataSetChanged();
				} else {
					DTUIUtil.showToastSafe(result.getError_code() + ":"
							+ result.getError_msg());
				}
			}
		});

		imgBack = (ImageView) findViewById(R.id.img_back);
		mAdapter = new POIAdapter();
		mAdapter.setCurrentIndex(false);
		if (mSearchList.size() == 0) {
			mFooterView.setVisibility(View.GONE);
		} else {
			mFooterView.setVisibility(View.VISIBLE);
		}
		mAdapter.addList(mSearchList);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(this);

		imgBack.setOnClickListener(this);
	}

	private void searchpoi(String str) {
		mLoadDialog.show();
		InputMethodManager imm1 = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm1.hideSoftInputFromWindow(edtKeyWord.getWindowToken(), 0);
		boolean isadd = true;
		for (int i = 0; i < mSearchList.size(); i++) {
			if (str.equals(mSearchList.get(i).getName())) {
				isadd = false;
				break;
			}
		}
		if (isadd) {
			AirSqlite.getInstance().insertSearchHistory(str);
			POI poi = new POI();
			mSearchList.add(poi);
		}
		mSearchUtil.setKeywords(str).searchPoi();
	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_back:
			finish();
			break;
		case R.id.search_cancel:
			edtKeyWord.setText("");
			mCancel.setVisibility(View.GONE);
			break;
		case R.id.wenxuntai_layout:
			edtKeyWord.setText("问讯处");
			searchpoi("问讯处");
			break;
		case R.id.weishengjian_layout:
			edtKeyWord.setText("卫生间");
			searchpoi("卫生间");
			break;
		case R.id.zhijiguitai_layout:
			edtKeyWord.setText("值机柜台");
			searchpoi("值机柜台");
			break;
		case R.id.anjian_layout:
			edtKeyWord.setText("安检");
			searchpoi("安检");
			break;
		case R.id.dianti_layout:
			edtKeyWord.setText("电梯");
			searchpoi("电梯");
			break;
		case R.id.meishi_layout:
			edtKeyWord.setText("美食");
			searchpoi("美食");
			break;
		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (arg2 > 0 && arg2 <= mAdapter.getCount()) {
			if (!mAdapter.isHistory()) {
				searchpoi(mSearchList.get(arg2 - 1).getName());
			} else {
				POI poi = mAdapter.getItem(arg2-1);
				Bundle bundle = new Bundle();
				bundle.putSerializable("poi", poi);
				getIntent().putExtras(bundle);
				setResult(Activity.RESULT_OK, getIntent());
				finish();
			}
		}
		if (arg2 == mAdapter.getCount() + 1) {
			mAdapter.clearList();
			mSearchList.clear();
			mFooterView.setVisibility(View.GONE);
			mAdapter.notifyDataSetChanged();
			AirSqlite.getInstance().deleteSearch();
		}
	}
}
