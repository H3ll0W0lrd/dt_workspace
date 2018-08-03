package com.airport.test.core;

import android.os.Bundle;

import com.airport.test.model.AirData;
import com.dingtao.libs.DTActivity;
import com.google.gson.Gson;
import com.rtm.common.model.BuildInfo;

public class APActivity extends DTActivity{
	public Gson mGson = new Gson();
	public BuildInfo mBuild;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBuild = mGson.fromJson(AirData.AIR_DATA, BuildInfo.class);
	}
	@Override
	public String getPageName() {
		return null;
	}

}
