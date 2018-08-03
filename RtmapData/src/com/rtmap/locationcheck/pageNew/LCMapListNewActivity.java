package com.rtmap.locationcheck.pageNew;

import im.fir.sdk.FIR;
import im.fir.sdk.VersionCheckCallback;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rtm.common.utils.RMD5Util;
import com.rtm.common.utils.RMLog;
import com.rtm.common.utils.RMVersionCommon;
import com.rtm.frm.utils.Handlerlist;
import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.adapter.LCMapListNewAdapter;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.model.Build;
import com.rtmap.locationcheck.core.model.Floor;
import com.rtmap.locationcheck.core.model.LoginUser;
import com.rtmap.locationcheck.page.LCBeaconActivity;
import com.rtmap.locationcheck.page.LCBeaconBitmapActivity;
import com.rtmap.locationcheck.page.LCSetNewActivity;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTUIUtils;

public class LCMapListNewActivity extends LCActivity implements
		OnChildClickListener, OnClickListener {

	public ExpandableListView mList;
	public LCMapListNewAdapter mAdapter;
	public ImageView mUpdateSign;

	private Handler mHandler = new Handler() {// 下载地图过程中下载进度消息
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case com.rtm.common.utils.Constants.RTMAP_MAP:
				int progress = msg.arg1;
				Log.e("rtmap", "SDK进度码" + progress);
				if (progress == com.rtm.common.utils.Constants.MAP_LOAD_START) {// 开始加载
					Log.e("rtmap", "开始加载");
				} else if (progress == com.rtm.common.utils.Constants.MAP_FailNetResult) {// 校验结果失败
					DTUIUtils.showToastSafe((String) msg.obj);
					Log.e("rtmap", "校验结果：" + (String) msg.obj);
				} else if (progress == com.rtm.common.utils.Constants.MAP_FailCheckNet) {// 联网检测失败
					Log.e("rtmap", "校验联网失败");
					DTUIUtils.showToastSafe("校验联网失败");
				} else if (progress == com.rtm.common.utils.Constants.MAP_Down_Success) {
					Log.e("rtmap", "地图下载成功");
				} else if (progress == com.rtm.common.utils.Constants.MAP_Down_Fail) {
					DTUIUtils.showToastSafe("地图下载失败");
				} else if (progress == com.rtm.common.utils.Constants.MAP_LOAD_END) {
					Log.e("rtmap", "地图加载完成");
				}
				break;
			}
		}
	};

	private TextView serverStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_map_list_new);
		DTLog.i(RMVersionCommon.VERSION);
		mList = (ExpandableListView) findViewById(R.id.map_list);

		findViewById(R.id.set).setOnClickListener(this);
		findViewById(R.id.information).setOnClickListener(this);

		serverStatus = (TextView) findViewById(R.id.server_status);

		mUpdateSign = (ImageView) findViewById(R.id.update_sign);
		mAdapter = new LCMapListNewAdapter(this);
		mList.setAdapter(mAdapter);
		if (savedInstanceState != null) {
			mAdapter.setFloorList((ArrayList<Build>) (savedInstanceState
					.getSerializable("floorList")));
		} else {
			LoginUser user = (LoginUser) getIntent().getExtras()
					.getSerializable("map");
			for (Build build : user.getResults()) {
				ArrayList<Floor> list = new ArrayList<Floor>();
				for (String f : build.getFloor()) {
					Floor floor = new Floor();
					floor.setBuildId(build.getBuildId());
					floor.setFloor(f);
					floor.setName(build.getBuildName());
					for (Floor l : build.getScale()) {
						if (l.getFloor().equals(f)) {
							floor.setScale(l.getScale());
							break;
						}
					}
					list.add(floor);
				}
				build.setScale(list);
				mAdapter.addChildList(build);
			}
		}
		mAdapter.notifyDataSetChanged();
		mList.setOnChildClickListener(this);
		checkUpdate();
		mModeArray = getResources().getStringArray(R.array.map_dialog_item);
	}

	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		bundle.putSerializable("floorList", mAdapter.getFloorList());
	}

	private String[] mModeArray;

	@Override
	protected void onResume() {
		super.onResume();
		mAdapter.notifyDataSetChanged();
		boolean istext = LCApplication.getInstance().getShare()
				.getBoolean("istest", false);
		int position = LCApplication.getInstance().getShare()
				.getInt(DTFileUtils.PICK_MODE, 0);
		if (istext)
			serverStatus.setText("测试，" + mModeArray[position]);
		else
			serverStatus.setText("正式，" + mModeArray[position]);
	}

	/**
	 * 检查更新
	 */
	private void checkUpdate() {
		FIR.checkForUpdateInFIR("70cedea02e5dfb7a81d5c6baabb666fc",
				new VersionCheckCallback() {
					@Override
					public void onSuccess(String versionJson) {
						try {
							JSONObject o = new JSONObject(versionJson);
							int versionCode = Integer.parseInt(o
									.getString("version"));
							if (versionCode > LCApplication.VERSION_CODE) {
								mUpdateSign.setVisibility(View.VISIBLE);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onFail(Exception exception) {
						Log.i("fir",
								"check fir.im fail! " + "\n"
										+ exception.getMessage());
					}

					@Override
					public void onStart() {
					}

					@Override
					public void onFinish() {
					}
				});
	}

	private boolean setPage(Floor floor, Intent intent, Class a, Class b) {
		String vector_path = DTFileUtils.MAP_DATA
				+ RMD5Util.md5(floor.getBuildId() + "_" + floor.getFloor()
						+ ".imap");// /mnt/sdcard/rtmap/mdata/860100010040500002_F2.imap
		String bitmap_path = DTFileUtils.getImageDir() + floor.getBuildId()
				+ "-" + floor.getFloor() + ".jpg";
		if (DTFileUtils.checkFile(vector_path)) {// 如果有矢量图
			intent.setClass(this, a);
			return true;
		} else if (DTFileUtils.checkFile(bitmap_path)) {
			if (floor.getScale() != 0) {
				intent.setClass(this, b);
				return true;
			} else {
				DTUIUtils.showToastSafe("位图比例尺为0");
			}
		} else {
			DTUIUtils.showToastSafe("请下载地图");
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.set:
			LCSetNewActivity.interActivity(this);
			break;
		case R.id.information:
			Intent intent = new Intent(this, LCInformationActivity.class);
			startActivity(intent);
			break;
		}
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		Floor floor = mAdapter.getChild(groupPosition, childPosition);
		final Intent intent = new Intent();
		final Bundle bundle = new Bundle();
		bundle.putSerializable("floor", floor);
		bundle.putSerializable("build", mAdapter.getGroup(groupPosition));
		intent.putExtras(bundle);
		int position = LCApplication.getInstance().getShare()
				.getInt(DTFileUtils.PICK_MODE, 0);
		boolean result = false;
		switch (position) {
		case 0:// POI采集
			result = setPage(floor, intent, LCModifyActivity.class,
					LCPoiRoadActivity.class);
			break;
		case 1:// 指纹采集
			result = setPage(floor, intent, LCTerminalRouteActivity.class,
					LCTerminalRouteBitmapActivity.class);
			break;
		case 2:// beacon采集
			result = setPage(floor, intent, LCBeaconActivity.class,
					LCBeaconBitmapActivity.class);
			break;
		}
		if (result) {
			startActivity(intent);
		}
		return false;
	}

	private long exitTime = 0;

	@Override
	public void onBackPressed() {
		if ((System.currentTimeMillis() - exitTime) > 2000) {
			Toast.makeText(getApplicationContext(), "再按一次退出程序",
					Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		} else {
			finish();
			System.exit(0);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Handlerlist.getInstance().register(mHandler);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Handlerlist.getInstance().remove(mHandler);
	}
}
