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
import com.minnw.beacon.adapter.TransimssionPowerAdapter;
import com.minnw.beacon.adapter.UUIDAdapter;
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

public class TransimssionPowerActivity extends FinishActivity {
	String[] arr = new String[4];
	List<TransimssionPower> listdata = new ArrayList<TransimssionPower>();
	TransimssionPowerAdapter adapter;
	private int mposition = 0;
	String data;
	String serviceIndex;
     int  position;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transimssionpower);
		data = getIntent().getStringExtra("data");
		serviceIndex = getIntent().getStringExtra("serviceIndex");
		position = Integer.parseInt(getIntent().getStringExtra("position"));
		initdata();

		for (int i = 0; i < listdata.size(); i++) {
			if (listdata.get(i).getValue().equals(data)) {
				mposition = i;
			}
		}
		init();

	}

	private void initdata() {
		TransimssionPower p1 = new TransimssionPower();
		p1.setMeters("2");
		p1.setValue("0");
		p1.setText("-30");
		TransimssionPower p2 = new TransimssionPower();
		p2.setMeters("7");
		p2.setValue("1");
		p2.setText("-20");
		TransimssionPower p3 = new TransimssionPower();
		p3.setMeters("10");
		p3.setValue("2");
		p3.setText("-16");
		TransimssionPower p4 = new TransimssionPower();
		p4.setMeters("15");
		p4.setValue("3");
		p4.setText("-12");
		TransimssionPower p5 = new TransimssionPower();
		p5.setMeters("22");
		p5.setValue("4");
		p5.setText("-8");
		TransimssionPower p6 = new TransimssionPower();
		p6.setMeters("27");
		p6.setValue("5");
		p6.setText("-4");
		TransimssionPower p7 = new TransimssionPower();
		p7.setMeters("50");
		p7.setValue("6");
		p7.setText("0");
		TransimssionPower p8 = new TransimssionPower();
		p8.setMeters("90");
		p8.setValue("7");
		p8.setText("4");

		listdata.add(p1);
		listdata.add(p2);
		listdata.add(p3);
		listdata.add(p4);
		listdata.add(p5);
		listdata.add(p6);
		listdata.add(p7);
		listdata.add(p8);
	}

	private void init() {

		ListView uuidlistview = (ListView) findViewById(R.id.uuidlistview);
		adapter = new TransimssionPowerAdapter(getApplicationContext(),
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
				s=Base64Tool.toHextoUpperCase(s, 2);
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
		TransimssionPowerAdapter listAdapter = (TransimssionPowerAdapter) listView
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
