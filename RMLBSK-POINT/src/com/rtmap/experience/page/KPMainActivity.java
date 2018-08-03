package com.rtmap.experience.page;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.rtmap.experience.R;
import com.rtmap.experience.core.KPActivity;
import com.rtmap.experience.core.KPApplication;
import com.rtmap.experience.core.KPAsyncTask;
import com.rtmap.experience.core.KPBaseFragment;
import com.rtmap.experience.core.KPCallBack;
import com.rtmap.experience.core.exception.KPException;
import com.rtmap.experience.core.http.KPHttpClient;
import com.rtmap.experience.core.http.KPHttpUrl;
import com.rtmap.experience.core.model.BuildInfo;
import com.rtmap.experience.fragment.KPFragmentFactory;
import com.rtmap.experience.util.DTFileUtils;
import com.rtmap.experience.util.DTUIUtils;

public class KPMainActivity extends KPActivity implements OnClickListener {

	private Button mFind;
	private Button mSet;
	private Button mRecord;

	public static void interActivity(Context context) {
		Intent intent = new Intent(context, KPMainActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		KPApplication.getInstance().clearActivity();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kp_main);
		mFind = (Button) findViewById(R.id.make);
		mSet = (Button) findViewById(R.id.set);
		mRecord = (Button) findViewById(R.id.record);

		mFind.setOnClickListener(this);
		mSet.setOnClickListener(this);
		mRecord.setOnClickListener(this);
		setPage(R.id.make);
	}

	/**
	 * 改变页面
	 * 
	 * @param position
	 */
	private void setPage(int position) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		// ViewPager页面被选中的回调
		KPBaseFragment fragment = KPFragmentFactory.createFragment(position);
		// 当页面被选中 再显示要加载的页面....防止ViewPager提前加载(ViewPager一般加载三个，自己，左一个，右一个)
		fragmentManager.beginTransaction().replace(R.id.main_layout, fragment)
				.commit();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.set:
		case R.id.record:// 记录
		case R.id.make:
			setPage(v.getId());
		}
	}

}
