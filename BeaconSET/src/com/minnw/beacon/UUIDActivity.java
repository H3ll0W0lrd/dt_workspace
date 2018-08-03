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
import com.minnw.beacon.adapter.UUIDAdapter;
import com.minnw.tools.Base64Tool;
import com.minnw.tools.Tools;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class UUIDActivity extends FinishActivity {
	String[] arr = new String[6];
	List<String> listdata = new ArrayList<String>();
	UUIDAdapter adapter;
	private int mposition = -1;
	String useuuid;
	EditText    curr_textview;
	String serviceIndex;
	int position;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.uuid);
		  useuuid = getIntent().getStringExtra("uuid");
		 serviceIndex = getIntent().getStringExtra("serviceIndex");
		 position = Integer.parseInt(getIntent().getStringExtra("position"));
		arr[0] = "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0;"
				+ getString(R.string.airlocate);
		arr[1] = "FDA50693-A4E2-4FB1-AFCF-C6EB07647825;"
				+ getString(R.string.weixin);
		arr[2] = "B9407F30-F5F8-466E-AFF9-25556B57FE6D;"
				+ getString(R.string.estimote);
		arr[3] = "74278BDA-B644-4520-8F0C-720EAF059935;"
				+ getString(R.string.uuid);
		arr[4] = "701D1040-084A-466A-A67E-11C7BBF4D316;"
				+ getString(R.string.uuid);
		arr[5] = "ECB33B47-781F-4C16-8513-73FCBB7134F2;"
				+"万达";
		for (int i = 0; i < arr.length; i++) {
			listdata.add(arr[i]);
			if (arr[i].split(";")[0].equals(useuuid)) {
				mposition = i;
			}
		}
		
		init();

	}

	private void init() {
		curr_textview=(EditText)findViewById(R.id.curr_textview);
		curr_textview.setText(useuuid);
		ListView uuidlistview = (ListView) findViewById(R.id.uuidlistview);
		adapter = new UUIDAdapter(getApplicationContext(), listdata);
		uuidlistview.setAdapter(adapter);
		adapter.setPosition(mposition);
		uuidlistview.setOnItemClickListener(mItemDeviceClickListener);
		Button   btn_save=(Button)findViewById(R.id.btn_save);
		btn_save.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String  s=curr_textview.getText().toString().trim();
				UartService.listdataservice.add(position, s);
				UartService.listdataservice.remove(position+1);
				String  vlaues=Base64Tool.HexToBase64(s);
				DeviceListActivity.mService.write_uuid(vlaues, Integer.parseInt(serviceIndex.split(";")[0]),Integer.parseInt(serviceIndex.split(";")[1]));
				finish();
			}
		});
		ImageButton   btn_back=(ImageButton)findViewById(R.id.btn_back);
		btn_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				
			}
		});
	}

	private OnItemClickListener mItemDeviceClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mposition = position;
			adapter.setPosition(mposition);
			curr_textview.setText(listdata.get(position).split(";")[0]);
		 
		}
	};

}
