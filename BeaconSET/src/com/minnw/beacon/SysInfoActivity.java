/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.minnw.beacon;

import java.util.ArrayList;
import java.util.List;


import com.minnw.beacon.R;
import com.minnw.beacon.adapter.BroadCastingIntervalAdapter;
import com.minnw.beacon.adapter.SysInfoAdapter;
import com.minnw.beacon.adapter.UUIDAdapter;
import com.minnw.beacon.data.BroadCastingInterval;
import com.minnw.beacon.data.TransimssionPower;
import com.minnw.tools.Base64Tool;
import com.minnw.tools.Tools;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class SysInfoActivity extends FinishActivity {
	String[] arr = new String[4];
	List<String> listdata = new ArrayList<String>();
	SysInfoAdapter adapter;
	String data;
	String serviceIndex;
	String position;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.broadcastinginterval);
		data = getIntent().getStringExtra("data");
		position = getIntent().getStringExtra("position");
		serviceIndex = getIntent().getStringExtra("serviceIndex");
		initdata();
		init();

	}

	private void initdata() {
		listdata.add(getString(R.string.manufacturer_name));
		listdata.add(getString(R.string.model_number));
		listdata.add(getString(R.string.serial_number));
		listdata.add(getString(R.string.firmware_revision));

		listdata.add(getString(R.string.hardware_revision));
		listdata.add(getString(R.string.software_revision));
		listdata.add(getString(R.string.system_id));
		listdata.add(getString(R.string.regulatorycertifactiondata));
	}

	private void init() {

		TextView name = (TextView) findViewById(R.id.name);
		name.setText(R.string.device_information);
		TextView tip = (TextView) findViewById(R.id.tip);
		tip.setText("");

		ListView uuidlistview = (ListView) findViewById(R.id.uuidlistview);
		adapter = new SysInfoAdapter(getApplicationContext(), listdata,
				UartService.syslistdata);
		uuidlistview.setAdapter(adapter);
		setListViewHeightBasedOnChildren(uuidlistview);
		ImageButton btn_back = (ImageButton) findViewById(R.id.btn_back);
		Button btn_save = (Button) findViewById(R.id.btn_save);
		btn_save.setVisibility(View.INVISIBLE);
		btn_back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();

			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DetilActivity.isSysInfoActivity = false;
	}

	public void setListViewHeightBasedOnChildren(ListView listView) {
		// 获取ListView对应的Adapter
		SysInfoAdapter listAdapter = (SysInfoAdapter) listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
			// listAdapter.getCount()返回数据项的数目
			View listItem = listAdapter.getView(i, null, listView);
			// 计算子项View 的宽高
			listItem.measure(0, 0);
			// 统计所有子项的总高度
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		// listView.getDividerHeight()获取子项间分隔符占用的高度
		// params.height最后得到整个ListView完整显示需要的高度
		listView.setLayoutParams(params);
	}

}
