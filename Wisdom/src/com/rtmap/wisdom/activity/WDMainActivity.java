package com.rtmap.wisdom.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.rtm.location.LocationApp;
import com.rtmap.wisdom.R;
import com.rtmap.wisdom.core.DTActivity;
import com.rtmap.wisdom.core.DTApplication;
import com.rtmap.wisdom.core.DTSqlite;
import com.rtmap.wisdom.fragment.WDBuildFragment;
import com.rtmap.wisdom.fragment.WDMainFragment;
import com.rtmap.wisdom.fragment.WDSearchFragment;
import com.rtmap.wisdom.model.MyBuild;
import com.rtmap.wisdom.util.DTLog;

/**
 * 主页：建筑物列表，地图，搜索
 * @author dingtao
 *
 */
public class WDMainActivity extends DTActivity {

	private WDBuildFragment mBuildFragment;
	private WDMainFragment mMainFragment;
	private WDSearchFragment mSearchFragment;
	private DrawerLayout mDrawer;
	private Dao<MyBuild, String> mBuildDao;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		DTApplication.getInstance().clearActivity();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_main);

		mBuildFragment = (WDBuildFragment) getFragmentManager()
				.findFragmentById(R.id.navigation_left);
		mMainFragment = (WDMainFragment) getFragmentManager().findFragmentById(
				R.id.navigation_drawer);
		mSearchFragment = (WDSearchFragment) getFragmentManager()
				.findFragmentById(R.id.navigation_right);

		mBuildDao = DTSqlite.getInstance().createBuildTable();

		mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		mBuildFragment.setDrawer(mDrawer, mLoadDialog, mBuildDao);
		mMainFragment.setDrawer(mDrawer, mLoadDialog, mBuildDao);
		mSearchFragment.setDrawer(mDrawer, mLoadDialog);
		int sign = getIntent().getIntExtra("sign", 1);
		if (sign == 0) {
			mDrawer.openDrawer(Gravity.START);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		DTLog.i("屏幕发生改变");
	}

	@Override
	public String getPageName() {
		return null;
	}

	private long exitTime = 0;

	@Override
	public void onBackPressed() {
		if (mDrawer.isDrawerOpen(Gravity.START)) {
			mDrawer.closeDrawer(Gravity.START);
		} else if (mDrawer.isDrawerOpen(Gravity.END)) {
			mDrawer.closeDrawer(Gravity.END);
		} else {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(), "再按一次退出程序",
						Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				finish();
				System.exit(0);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocationApp.getInstance().stop();
	}
}
