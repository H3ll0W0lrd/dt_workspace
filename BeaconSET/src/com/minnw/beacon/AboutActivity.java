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

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class AboutActivity extends Activity  {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		ImageButton  btn_back=(ImageButton)findViewById(R.id.btn_back);
		btn_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
				
			}
		});
		TextView   verson=(TextView)findViewById(R.id.verson);
		verson.setText(getVersion());
	}
	
	/**
	 2  * 获取版本号
	 3  * @return 当前应用的版本号
	 4  */
	   public String getVersion() {
	   try {
	           PackageManager manager = this.getPackageManager();
	           PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
	         String version = info.versionName;
	         return this.getString(R.string.version_name) + version;
	      } catch (Exception e) {
	         e.printStackTrace();
	        return this.getString(R.string.can_not_find_version_name);
	   }
	  }
}
