package com.airport.test.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.airport.test.R;
import com.airport.test.adapter.SearchAdapter;
import com.airport.test.model.AirData;
import com.baidu.mapapi.model.LatLng;
import com.dingtao.libs.DTActivity;
import com.dingtao.libs.util.DTStringUtil;
import com.dingtao.libs.util.DTUIUtil;
import com.google.gson.Gson;
import com.rtm.common.model.BuildInfo;

public class SearchActivity extends DTActivity implements OnClickListener,OnItemClickListener{
	
	 private EditText edtKeyWord;
	    private TextView tvSearch;
	    private ListView lvSearchResult;
	    private ImageView imgBack;
	    private BuildInfo mBuild;
	    private SearchAdapter mAdapter;
	    private static LatLng mLatlng;
	    
	    public static void interActivity(Context context,LatLng latlng) {
	    	mLatlng = latlng;
			Intent intent = new Intent(context, SearchActivity.class);
			context.startActivity(intent);
		}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_build);
		
		Gson gson = new Gson();
		mBuild = gson.fromJson(AirData.AIR_DATA, BuildInfo.class);
		
		edtKeyWord = (EditText) findViewById(R.id.edt_key_word);
        tvSearch = (TextView) findViewById(R.id.tv_search);
        lvSearchResult = (ListView) findViewById(R.id.lv_search_result);
        imgBack = (ImageView) findViewById(R.id.img_back);
        mAdapter = new SearchAdapter(mLatlng);
        lvSearchResult.setAdapter(mAdapter);
        lvSearchResult.setOnItemClickListener(this);
     
        imgBack.setOnClickListener(this);
        tvSearch.setOnClickListener(this);
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
		case R.id.tv_search:
			String str = edtKeyWord.getText().toString();
			if(DTStringUtil.isEmpty(str)){
				DTUIUtil.showToastSafe("请输入搜索内容");
			}else{
				if(mBuild.getBuildName().contains(str)){
					mAdapter.clearList();
					mAdapter.addItemLast(mBuild);
					mAdapter.notifyDataSetChanged();
				}
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		APMapActivity.interActivity(this);
		finish();
	}

}
