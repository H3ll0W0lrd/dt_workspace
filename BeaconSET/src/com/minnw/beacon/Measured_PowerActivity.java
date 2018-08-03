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
import android.text.InputType;
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

public class Measured_PowerActivity extends FinishActivity {
	String data;
	EditText curr_edittext;
	String serviceIndex;
	int position;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.measured_power);
		data = getIntent().getStringExtra("data");
		position = Integer.parseInt(getIntent().getStringExtra("position"));
		serviceIndex = getIntent().getStringExtra("serviceIndex");
		init();

	}

	private void init() {
		curr_edittext = (EditText) findViewById(R.id.curr_textview);
		if (position==7) {
			TextView majorminor = (TextView) findViewById(R.id.majorminor);
			TextView tip = (TextView) findViewById(R.id.tip);
			majorminor.setText(R.string.serial_id);
			curr_edittext.setText(data);
			tip.setText(R.string.serialidtip);
		}else if(position==8){
			curr_edittext.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
			TextView majorminor = (TextView) findViewById(R.id.majorminor);
			TextView tip = (TextView) findViewById(R.id.tip);
			majorminor.setText(R.string.ibeacon_name);
			curr_edittext.setText("rtmapbeacon_");
			tip.setText(getString(R.string.ibeacon_nametip).replaceAll("#", "rtmapbeacon_".length()+""));
		} else
			curr_edittext.setText(data.replaceAll("dBm", "").trim());

		Button btn_save = (Button) findViewById(R.id.btn_save);
		btn_save.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					String s = curr_edittext.getText().toString().trim();
					if (position==7) {
						Integer.parseInt(s.toString());
						String vlaues = Base64Tool.toHex4(s);
						vlaues = Base64Tool.HexToBase64(vlaues);
						DeviceListActivity.mService.write_uuid(vlaues,
								Integer.parseInt(serviceIndex.split(";")[0]),
								Integer.parseInt(serviceIndex.split(";")[1]));
						UartService.listdataservice.add(position, s );
						UartService.listdataservice.remove(position+1);
					} else if(position==8){
						String	vlaues = Base64Tool.ASCIIToBase64(s);
						DeviceListActivity.mService.write_uuid(vlaues,
								Integer.parseInt(serviceIndex.split(";")[0]),
								Integer.parseInt(serviceIndex.split(";")[1]));
						UartService.listdataservice.add(position, s );
						UartService.listdataservice.remove(position+1);
					}else {
						Integer.parseInt(s.toString());
						String vlaues = Base64Tool.toHexMeasuredPower(s);
						vlaues = Base64Tool.HexToBase64(vlaues);
						DeviceListActivity.mService.write_uuid(vlaues,
								Integer.parseInt(serviceIndex.split(";")[0]),
								Integer.parseInt(serviceIndex.split(";")[1]));
						UartService.listdataservice.add(position, s+" dBm");
						UartService.listdataservice.remove(position+1);
					}
				
					finish();
				} catch (NumberFormatException e) {
					Toast.makeText(getApplicationContext(),
							getString(R.string.inputnumber), 1).show();
				}

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
