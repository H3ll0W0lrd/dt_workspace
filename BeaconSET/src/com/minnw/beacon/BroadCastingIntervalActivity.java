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

public class BroadCastingIntervalActivity extends FinishActivity {
	String[] arr = new String[4];
	List<BroadCastingInterval> listdata = new ArrayList<BroadCastingInterval>();
	BroadCastingIntervalAdapter adapter;
	private int mposition = 0;
	String data;
	String serviceIndex;
	int position;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.broadcastinginterval);
		data = getIntent().getStringExtra("data");
		position =Integer.parseInt( getIntent().getStringExtra("position"));
		serviceIndex = getIntent().getStringExtra("serviceIndex");
		initdata();

		for (int i = 0; i < listdata.size(); i++) {
			if (listdata.get(i).getValue().equals(data)) {
				mposition = i;
			}
		}
		init();

	}

	private void initdata() {
		if (position==9) {
			BroadCastingInterval p1 = new BroadCastingInterval();
			p1.setText("0    YES");
			p1.setValue("0");
			BroadCastingInterval p2 = new BroadCastingInterval();
			p2.setText("1    NO");
			p2.setValue("1");
			 
			listdata.add(p1);
			listdata.add(p2);
		} else {
			BroadCastingInterval p1 = new BroadCastingInterval();
			p1.setText("100 ms");
			p1.setValue("1");
			BroadCastingInterval p2 = new BroadCastingInterval();
			p2.setText("200 ms");
			p2.setValue("2");
			BroadCastingInterval p3 = new BroadCastingInterval();
			p3.setText("300 ms");
			p3.setValue("3");
			BroadCastingInterval p4 = new BroadCastingInterval();
			p4.setText("400 ms");
			p4.setValue("4");
			BroadCastingInterval p5 = new BroadCastingInterval();
			p5.setText("500 ms");
			p5.setValue("5");
			BroadCastingInterval p6 = new BroadCastingInterval();
			p6.setText("600 ms");
			p6.setValue("6");
			BroadCastingInterval p7 = new BroadCastingInterval();
			p7.setText("700 ms");
			p7.setValue("7");
			BroadCastingInterval p8 = new BroadCastingInterval();
			p8.setText("800 ms");
			p8.setValue("8");
			BroadCastingInterval p9 = new BroadCastingInterval();
			p9.setText("900 ms");
			p9.setValue("9");
			BroadCastingInterval p10 = new BroadCastingInterval();
			p10.setText("1000 ms");
			p10.setValue("10");

			listdata.add(p1);
			listdata.add(p2);
			listdata.add(p3);
			listdata.add(p4);
			listdata.add(p5);
			listdata.add(p6);
			listdata.add(p7);
			listdata.add(p8);
			listdata.add(p9);
			listdata.add(p10);
		}
	}

	private void init() {
		if (position==9) {
			TextView name = (TextView) findViewById(R.id.name);
			name.setText(R.string.connection_mode);
			TextView tip = (TextView) findViewById(R.id.tip);
			tip.setText(R.string.connection_modetip);
		}
		ListView uuidlistview = (ListView) findViewById(R.id.uuidlistview);
		adapter = new BroadCastingIntervalAdapter(getApplicationContext(),
				listdata);
		uuidlistview.setAdapter(adapter);
		adapter.setPosition(mposition);
		setListViewHeightBasedOnChildren(uuidlistview);
		uuidlistview.setOnItemClickListener(mItemDeviceClickListener);
		Button btn_save = (Button) findViewById(R.id.btn_save);
		btn_save.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String s = listdata.get(mposition).getValue();
				UartService.listdataservice.add(position, s);
				UartService.listdataservice.remove(position+1);
				s= Base64Tool.toHextoUpperCase(s,2);
				String vlaues = Base64Tool.HexToBase64(s);
				DeviceListActivity.mService.write_uuid(vlaues,
						Integer.parseInt(serviceIndex.split(";")[0]),
						Integer.parseInt(serviceIndex.split(";")[1]));
                finish();
			}
		});
		ImageButton btn_back = (ImageButton) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();

			}
		});

	}

	public void setListViewHeightBasedOnChildren(ListView listView) {
		// 获取ListView对应的Adapter
		BroadCastingIntervalAdapter listAdapter = (BroadCastingIntervalAdapter) listView
				.getAdapter();
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

	private OnItemClickListener mItemDeviceClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mposition = position;
			adapter.setPosition(mposition);

		}
	};

}
