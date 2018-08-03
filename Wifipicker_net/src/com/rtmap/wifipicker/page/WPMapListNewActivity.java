package com.rtmap.wifipicker.page;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rtm.common.utils.RMD5Util;
import com.rtm.frm.model.Floor;
import com.rtm.frm.utils.Handlerlist;
import com.rtmap.wifipicker.R;
import com.rtmap.wifipicker.adapter.WPMapListNewAdapter;
import com.rtmap.wifipicker.core.DTAsyncTask;
import com.rtmap.wifipicker.core.DTCallBack;
import com.rtmap.wifipicker.core.WPApplication;
import com.rtmap.wifipicker.core.exception.RMException;
import com.rtmap.wifipicker.core.http.WPHttpClient;
import com.rtmap.wifipicker.core.http.WPHttpUrl;
import com.rtmap.wifipicker.core.model.Build;
import com.rtmap.wifipicker.core.model.FloorList;
import com.rtmap.wifipicker.core.model.LoginUser;
import com.rtmap.wifipicker.core.model.RMPoint;
import com.rtmap.wifipicker.util.ConstantLoc.UIEventCode;
import com.rtmap.wifipicker.util.Constants;
import com.rtmap.wifipicker.util.DTFileUtils;
import com.rtmap.wifipicker.util.DTLog;
import com.rtmap.wifipicker.util.DTUIUtils;
import com.rtmap.wifipicker.util.FileHelper;
import com.rtmap.wifipicker.util.NetworkService;
import com.rtmap.wifipicker.util.WPDBService;
import com.rtmap.wifipicker.widget.DialogBulder;
import com.rtmap.wifipicker.wifi.UIEvent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

/**
 * 地图列表页
 * 
 * @author zhengnengyuan
 * 
 */
public class WPMapListNewActivity extends WPBaseActivity implements
		OnChildClickListener {
	private static final int MESSAGE_HIDE_LOAD = 102;
	private static final int MESSAGE_SHOW_TIP = 103;
	private static final int MESSAGE_COMMIT_COMPLETE = 105;
	private static final int MESSAGE_SET_DOWNLOAD_STATUS = 107;

	private ExpandableListView mListMap;
	private WPMapListNewAdapter mMapAdapter;
	private WifiManager wm;

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
				} else if (progress == com.rtm.common.utils.Constants.MAP_FailCheckNet) {// 联网检测失败
					Log.e("rtmap", "校验联网失败");
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

		setContentView(R.layout.map_list_new);
		Handlerlist.getInstance().register(mHandler);
		wm = (WifiManager) getSystemService(WIFI_SERVICE);
		init();
		checkUpdate();
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_HIDE_LOAD:
				hideLoad();
				break;
			case MESSAGE_SHOW_TIP:
				showToast((String) msg.obj, Toast.LENGTH_SHORT);
				break;
			case MESSAGE_COMMIT_COMPLETE:
				mMapAdapter.notifyDataSetChanged();
				break;
			case MESSAGE_SET_DOWNLOAD_STATUS:
				if (msg.arg2 == 0) {
					showToast("地图下载中...", Toast.LENGTH_LONG);
				} else if (msg.arg2 == 1) {
					showToast("地图下载完成", Toast.LENGTH_SHORT);
				}
				break;
			case UIEventCode.NO_NET_SIGNAL_REMINDER:// 检测没有网络提示
				NetworkService.checkNetInfo();
			}
			super.handleMessage(msg);
		}

	};

	private void init() {
		mListMap = (ExpandableListView) findViewById(R.id.map_list);
		mMapAdapter = new WPMapListNewAdapter(this);
		mListMap.setAdapter(mMapAdapter);
		mListMap.setOnChildClickListener(this);
		Bundle bundle = getIntent().getExtras();
		LoginUser user = (LoginUser) bundle.getSerializable("map");
		for (Build build : user.getResults()) {
			mMapAdapter.addGroup(build.getBuildName());
			ArrayList<Floor> list = new ArrayList<Floor>();
			for (String f : build.getFloor()) {
				Floor floor = new Floor();
				floor.setBuildid(build.getBuildId());
				floor.setFloor(f);
				floor.setDescription(build.getBuildName());
				list.add(floor);
			}
			mMapAdapter.addChildList(list);
		}
		mMapAdapter.notifyDataSetChanged();

		// 页面标题
		setTitleText(R.string.title_select_map);
		setRightPanelButtonVisibility(View.VISIBLE);
		final String username = WPApplication.getInstance().getShare()
				.getString(Constants.PREFS_USERNAME, "注销");

		// 注销
		setRightPanelButtonText(username);
		setRightPanelButtonListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new DialogBulder(WPMapListNewActivity.this)
						.setTitle("注销")
						.setMessage("当前用户：" + username)
						.setButtons("确定", "取消",
								new DialogBulder.OnDialogButtonClickListener() {
									public void onDialogButtonClick(
											Context context,
											DialogBulder builder,
											Dialog dialog, int dialogId,
											int which) {
										if (which == BUTTON_LEFT) {
											Intent intent = new Intent(
													WPMapListNewActivity.this,
													WPLoginActivity.class);
											intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
											startActivity(intent);
											finish();
										}
									}
								}).create().show();
				;
			}
		});

		// 标题栏左按钮监听，进入设置界面
		setLeftPanelButtonVisibility(View.VISIBLE);
		setLeftPanelButtonText(R.string.setting);
		setLeftPanelButtonListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						WPSettingActivity.class);
				startActivity(intent);
			}
		});

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

	/**
	 * 得到数据库中所有点
	 * 
	 * @param path
	 * @return
	 */
	private ArrayList<RMPoint> getDataToJson(String path) {
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(path, null);
		return WPDBService.getInstance().getAllPoints(db);
	}

	private Dialog mCollectDialog;// 间隔dialog

	/**
	 * show弹出框
	 */
	private void showInter(final Floor floor) {
		mCollectDialog = new Dialog(this, R.style.dialog);
		mCollectDialog.setContentView(R.layout.dialog_collect_layout);
		mCollectDialog.setCanceledOnTouchOutside(true);
		ListView mInterList = (ListView) mCollectDialog
				.findViewById(R.id.set_list);
		String[] interDate = getResources().getStringArray(
				R.array.set_inter_list);
		mInterList.setAdapter(new WPColloctAdapter(this, interDate));
		mInterList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				final Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putSerializable(Constants.EXTRA_FLOOR, floor);
				intent.putExtras(bundle);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				FileHelper.checkDir(Constants.MAP_DATA);
				final String root = String.format("%s%s/",
						Constants.WIFI_PICKER_PATH,
						mUserName + "/" + floor.getBuildid());
				FileHelper.checkDir(root);
				final String vector_path = Constants.MAP_DATA
						+ RMD5Util.md5(floor.getBuildid() + "_"
								+ floor.getFloor() + ".imap");// /mnt/sdcard/rtmap/mdata/MD5(860100010040500002_F2.imap)
				final String bitmap_path = root + floor.getBuildid() + "-"
						+ floor.getFloor() + "-0.jpg";
				DTLog.e("矢量图：" + vector_path);
				DTLog.e("位图：" + bitmap_path);
				if (!FileHelper.checkFile(vector_path)
						&& FileHelper.checkFile(bitmap_path)) {
				} else {
				}
				if (position == 0) {
					if (FileHelper.checkFile(bitmap_path)) {
						intent.setClass(getApplicationContext(),
								WPoiRoadActivity.class);
						showLoad();
						new DTAsyncTask(new DTCallBack() {

							@Override
							public Object onCallBackStart(Object... obj) {
								try {
									String result = WPHttpClient.getOrDelete(
											WPHttpClient.GET,
											String.format(
													WPHttpUrl.FLOOR_INFO,
													WPApplication
															.getInstance()
															.getShare()
															.getString(
																	DTFileUtils.PREFS_TOKEN,
																	""),
													floor.getFloor(),
													floor.getBuildid()), null,
											null);
									Gson gson = new Gson();
									return gson.fromJson(result,
											FloorList.class);
								} catch (RMException e) {
									e.printStackTrace();
								}
								return null;
							}

							@Override
							public void onCallBackFinish(Object obj) {
								hideLoad();
								if (obj != null) {
									FloorList list = (FloorList) obj;
									if (list.getResults() == null
											|| list.getResults().size() == 0) {
										DTUIUtils.showToastSafe("获取位图比例尺失败");
									} else {
										if (list.getResults().get(0).getScale() != 0) {
											mCollectDialog.cancel();
											intent.putExtra("scale",list.getResults()
													.get(0).getScale());
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
						return;
					} else {
						DTUIUtils
								.showToastSafe(R.string.map_download_please_bitmap);
						mCollectDialog.cancel();
						return;
					}
				} else if (position > 0 && position <= 5) {
					if (FileHelper.checkFile(vector_path)) {
						switch (position) {
						case 1:
							intent.setClass(getApplicationContext(),
									WPUnionNewActivity.class);

							if (!wm.isWifiEnabled()) {
								wm.setWifiEnabled(true);
							}
							break;
						case 2:
							intent.setClass(getApplicationContext(),
									WPTerminalActivity.class);
							if (!wm.isWifiEnabled()) {
								wm.setWifiEnabled(true);
							}
							break;
						case 3:
							intent.setClass(getApplicationContext(),
									WPNetActivity.class);
							if (!wm.isWifiEnabled()) {
								wm.setWifiEnabled(true);
							}
							break;
						case 4:
							intent.setClass(getApplicationContext(),
									WPoiActivity.class);
							break;
						case 5:
							intent.setClass(getApplicationContext(),
									WPModifyActivity.class);
							break;
						}
					} else {
						DTUIUtils
								.showToastSafe(R.string.map_download_please_vector);
						mCollectDialog.cancel();
						return;
					}
				} else if (position == 6) {
					intent.setClass(getApplicationContext(),
							WPNoMapActivity.class);
				}
				if (position >= 0 && position < 7) {
					startActivity(intent);
				} else if (position == 7) {
					AlertDialog.Builder build = new Builder(
							WPMapListNewActivity.this);
					build.setTitle("删除后可以下载新数据");
					build.setMessage("删除已存在的矢量图和位图吗？");
					build.setPositiveButton("确认",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									File vector = new File(vector_path);
									if (vector.exists())
										vector.delete();
									File bit = new File(bitmap_path);
									if (bit.exists())
										bit.delete();
									mMapAdapter.notifyDataSetChanged();
								}
							});
					build.setNegativeButton("取消", null);
					build.create().show();
				} else if (position == 8) {// 删除无用walk文件
					AlertDialog.Builder build = new Builder(
							WPMapListNewActivity.this);
					build.setTitle("删除本层全部walk文件");
					build.setMessage("包含终端或无图采集后使用导出功能导出的walk文件");
					build.setPositiveButton("确认",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									String[] files = FileHelper.listFiles(root,
											new FilenameFilter() {// 只需要上传.walk1文件

												@Override
												public boolean accept(File dir,
														String filename) {
													if ((filename.contains(floor
															.getBuildid()
															+ "-"
															+ floor.getFloor()
															+ "-") || filename.contains(floor
															.getBuildid()
															+ "-"
															+ floor.getFloor()
															+ "_"))
															&& (filename
																	.endsWith(".walk1") || filename
																	.endsWith(".sensor"))) {
														return true;
													}
													return false;
												}
											});
									if (files != null && files.length > 0) {
										for (String str : files) {
											File file = new File(root + str);
											if (file.exists()) {
												file.delete();
											}
										}
									}
									mMapAdapter.notifyDataSetChanged();
								}
							});
					build.setNegativeButton("取消", null);
					build.create().show();
				}
				mCollectDialog.cancel();
			}
		});
		mCollectDialog.show();
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
					UmengUpdateAgent.showUpdateDialog(getApplicationContext(),
							updateInfo);
					break;
				}
			}
		});
		UmengUpdateAgent.update(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		UIEvent.getInstance().register(handler);
		mMapAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onPause() {
		super.onPause();
		UIEvent.getInstance().remove(handler);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Handlerlist.getInstance().remove(mHandler);
		UIEvent.getInstance().remove(handler);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		Floor floor = mMapAdapter.getChild(groupPosition, childPosition);
		showInter(floor);
		return false;
	}

}
