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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class MajorMinorActivity extends FinishActivity {
	String data;
	EditText curr_edittext;
	String serviceIndex;
	int position;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.majorminor);
		data = getIntent().getStringExtra("data") ;
		position = Integer.parseInt(getIntent().getStringExtra("position"));
		serviceIndex = getIntent().getStringExtra("serviceIndex");
		init();

	}
	TextView shijinzi;
	TextView shiliujinzi;
	TextView  majorminor;
	private void init() {
		majorminor = (TextView) findViewById(R.id.majorminor);
		if(position==2){
			majorminor.setText(R.string.major);
		}else{
			majorminor.setText(R.string.minor);
		}
		curr_edittext = (EditText) findViewById(R.id.curr_textview);
		curr_edittext.setText(data);
		curr_edittext.addTextChangedListener(new TextWatcher(){

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				try {
					if(Integer.parseInt(s.toString().trim())>65534){
						Toast.makeText(getApplicationContext(), getString(R.string.majortip), 1).show();
					}
					shijinzi.setText(s.toString());
					shiliujinzi.setText(Integer.toHexString(Integer.parseInt(s.toString())));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}});
		shijinzi = (TextView) findViewById(R.id.shijinzi);
		shijinzi.setText(data);
		shiliujinzi = (TextView) findViewById(R.id.shiliujinzi);
		shiliujinzi.setText(Integer.toHexString(Integer.parseInt(data)));
		Button btn_save = (Button) findViewById(R.id.btn_save);
		btn_save.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String s = curr_edittext.getText().toString().trim();
				UartService.listdataservice.add(position, s);
				UartService.listdataservice.remove(position+1);
				String vlaues = Base64Tool.toHex(s);
				vlaues = Base64Tool.HexToBase64(vlaues);
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

}
