package com.rtmap.locationcheck.page;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rtm.common.utils.Constants;
import com.rtm.common.utils.RMD5Util;
import com.rtm.common.utils.RMLog;
import com.rtm.common.utils.RMVersionCommon;
import com.rtm.frm.utils.Handlerlist;
import com.rtmap.locationcheck.R;
import com.rtmap.locationcheck.adapter.LCMapDialogAdapter;
import com.rtmap.locationcheck.adapter.LCMapListNewAdapter;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.LCAsyncTask;
import com.rtmap.locationcheck.core.LCCallBack;
import com.rtmap.locationcheck.core.exception.LCException;
import com.rtmap.locationcheck.core.http.LCHttpClient;
import com.rtmap.locationcheck.core.http.LCHttpUrl;
import com.rtmap.locationcheck.core.model.Build;
import com.rtmap.locationcheck.core.model.Floor;
import com.rtmap.locationcheck.core.model.FloorList;
import com.rtmap.locationcheck.core.model.LoginUser;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTUIUtils;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

public class LCMapListActivity extends LCActivity implements
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_map_list_new);
		RMLog.LOG_LEVEL = RMLog.LOG_LEVEL_INFO;
		DTLog.i(RMVersionCommon.VERSION);
		mList = (ExpandableListView) findViewById(R.id.map_list);
		findViewById(R.id.logout).setOnClickListener(this);
		findViewById(R.id.set).setOnClickListener(this);
		mUpdateSign = (ImageView) findViewById(R.id.update_sign);

		LoginUser user = (LoginUser) getIntent().getExtras().getSerializable(
				"map");
		mAdapter = new LCMapListNewAdapter(this);
		mList.setAdapter(mAdapter);
		for (Build build : user.getResults()) {
			mAdapter.addGroup(build.getBuildName());
			ArrayList<Floor> list = new ArrayList<Floor>();
			for (String f : build.getFloor()) {
				Floor floor = new Floor();
				floor.setBuildId(build.getBuildId());
				floor.setFloor(f);
				floor.setName(build.getBuildName());
				list.add(floor);
			}
			mAdapter.addChildList(list);
		}
		mAdapter.notifyDataSetChanged();
		mList.setOnChildClickListener(this);
		checkUpdate();
	}

	/**
	 * 检查更新
	 */
	private void checkUpdate() {
		UmengUpdateAgent.setUpdateAutoPopup(false);
		UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
			@Override
			public void onUpdateReturned(int updateStatus,
					UpdateResponse updateInfo) {
				switch (updateStatus) {
				case UpdateStatus.Yes: // has update
					DTLog.i("更新：" + updateInfo.version);
					mUpdateSign.setVisibility(View.VISIBLE);
					break;
				}
			}
		});
		UmengUpdateAgent.update(this);
	}

	private Dialog mCollectDialog;// 间隔dialog

	/**
	 * show弹出框
	 */
	private void showInter(final Floor floor) {
		mCollectDialog = new Dialog(this, R.style.dialog);
		mCollectDialog.setContentView(R.layout.dialog_map_layout);
		mCollectDialog.setCanceledOnTouchOutside(true);
		ListView mInterList = (ListView) mCollectDialog
				.findViewById(R.id.set_list);
		String[] interDate = getResources().getStringArray(
				R.array.map_dialog_item_new);
		mInterList.setAdapter(new LCMapDialogAdapter(this, interDate));
		mInterList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				final Intent intent = new Intent();
				final Bundle bundle = new Bundle();
				bundle.putSerializable("floor", floor);
				intent.putExtras(bundle);
				switch (position) {
				case 0:
					intent.setClass(LCMapListActivity.this,
							LCBeaconNoLBSActivity.class);
					break;
				case 1:
					intent.setClass(LCMapListActivity.this,
							LCBeaconActivity.class);
					break;
				case 2:
					intent.setClass(LCMapListActivity.this,
							LCLocationPointActivity.class);
					break;
				case 3:
					intent.setClass(LCMapListActivity.this,
							LCLocationRouteActivity.class);
					break;
				case 4:
					AlertDialog.Builder build = new Builder(
							LCMapListActivity.this);
					build.setTitle("删除后可以下载新数据");
					build.setMessage("删除已存在的地图吗？");
					build.setPositiveButton("确认",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									String vector_path = DTFileUtils.MAP_DATA
											+ RMD5Util.md5(floor.getBuildId()
													+ "_" + floor.getFloor()
													+ ".imap");// /mnt/sdcard/rtmap/mdata/MD5(860100010040500002_F2.imap)
									File vector = new File(vector_path);
									String bitmap_path = DTFileUtils
											.getImageDir()
											+ floor.getBuildId()
											+ "-" + floor.getFloor() + ".jpg";
									File bitmap = new File(bitmap_path);
									if (vector.exists())
										vector.delete();
									if (bitmap.exists())
										bitmap.delete();
									mAdapter.notifyDataSetChanged();
								}
							});
					build.setNegativeButton("取消", null);
					build.create().show();
					break;
				case 5://
					intent.setClass(LCMapListActivity.this,
							LCBitampBeaconNoLBSActivity.class);
					break;
				}
				if (position < 4) {
					String vector_path = DTFileUtils.MAP_DATA
							+ RMD5Util.md5(floor.getBuildId() + "_"
									+ floor.getFloor() + ".imap");// /mnt/sdcard/rtmap/mdata/860100010040500002_F2.imap
					if (!DTFileUtils.checkFile(vector_path)) {// 如果有矢量图
						DTUIUtils.showToastSafe("请下载矢量图");
						return;
					}
				}
				if (position == 5) {
					String bitmap_path = DTFileUtils.getImageDir()
							+ floor.getBuildId() + "-" + floor.getFloor()
							+ ".jpg";
					if (!DTFileUtils.checkFile(bitmap_path)) {
						DTUIUtils.showToastSafe("请下载位图");
						return;
					} else {
						mLoadDialog.show();
						new LCAsyncTask(new LCCallBack() {

							@Override
							public Object onCallBackStart(Object... obj) {
								try {
									String result = LCHttpClient.getOrDelete(
											LCHttpClient.GET,
											String.format(
													LCHttpUrl.FLOOR_INFO,
													LCApplication
															.getInstance()
															.getShare()
															.getString(
																	DTFileUtils.PREFS_TOKEN,
																	""),
													floor.getFloor(),
													floor.getBuildId()), null,
											null);
									Gson gson = new Gson();
									return gson.fromJson(result,
											FloorList.class);
								} catch (LCException e) {
									e.printStackTrace();
								}
								return null;
							}

							@Override
							public void onCallBackFinish(Object obj) {
								mLoadDialog.cancel();
								if (obj != null) {
									FloorList list = (FloorList) obj;
									if (list.getResults() == null
											|| list.getResults().size() == 0) {
										DTUIUtils.showToastSafe("获取位图比例尺失败");
									} else {
										if (list.getResults().get(0).getScale() != 0) {
											floor.setScale(list.getResults()
													.get(0).getScale() * 1000);
											startActivity(intent);
										} else {
											DTUIUtils.showToastSafe("位图比例尺为0");
										}
									}
								} else {
									DTUIUtils.showToastSafe("联网失败");
								}
							}

						}).run();
					}
				}
				if (position < 4)
					startActivity(intent);
				mCollectDialog.cancel();
			}
		});
		mCollectDialog.show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.logout:
			LCLoginActivity.interActivity(this);
			finish();
			break;
		case R.id.set:
			LCSetNewActivity.interActivity(this);
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		Floor floor = mAdapter.getChild(groupPosition, childPosition);
		showInter(floor);
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
