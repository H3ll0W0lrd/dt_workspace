package com.rtmap.locationcheck.page;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rtm.common.utils.RMLog;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtmap.locationcheck.R;
import com.rtmap.locationcheck.adapter.LCBeaconListAdapter;
import com.rtmap.locationcheck.adapter.LCMapDialogAdapter;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.LCAsyncTask;
import com.rtmap.locationcheck.core.LCCallBack;
import com.rtmap.locationcheck.core.http.LCHttpClient;
import com.rtmap.locationcheck.core.http.LCHttpUrl;
import com.rtmap.locationcheck.core.model.BeaconInfo;
import com.rtmap.locationcheck.core.model.BeaconList;
import com.rtmap.locationcheck.core.model.Floor;
import com.rtmap.locationcheck.core.model.LCPoint;
import com.rtmap.locationcheck.layer.BeaconLayer;
import com.rtmap.locationcheck.layer.OnBeaconClickListener;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTIOUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;

/**
 * 巡检beacon页面
 * 
 * @author dingtao
 *
 */
public class LCBeaconNoLBSActivity extends LCActivity implements
		OnClickListener, OnItemClickListener, OnBeaconClickListener,
		OnQueryTextListener {

	private Floor mFloor;// 楼层信息
	private MapView mMapView;// 地图View
	private TextView mTitle, mMenu, mSet;// 标题，菜单键
	private Dialog mMenuDialog, mInfoDialog;// 间隔dialog
	private Dialog mMoveDialog;// 移动Dialog
	private TextView mInfoText;// beacon信息
	private Button mDeleteReStore, mUpdate, mMove;// 删除或者重置，修改按钮，移动按钮
	private BeaconLayer mBeaconLayer;// Beacon图层
	private Button mMark;// 添加按钮
	private BeaconInfo mClickPoint;// 点击Beacon
	private String[] mWorkStatus;// 工作状态
	private boolean isBeaconStart;// 开始
	private SearchView mSearch;// 搜索View,4.0系统才能使用这个控件
	private ListView mBeaconList;// 搜索列表
	private LCBeaconListAdapter mBeaconAdapter;// 搜索的beacon列表
	private ImageView mBack;// 返回
	private LinearLayout mSearchLayout;// 搜索布局
	private TextView mStatus;// 文字提示

	private HashMap<String, BeaconInfo> mBeaconMap;// beacon键值对mac值
	Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_beacon);
		mFloor = (Floor) getIntent().getExtras().getSerializable("floor");
		mTitle = (TextView) findViewById(R.id.title);
		mTitle.setText(mFloor.getName() + "-" + mFloor.getFloor());
		mMenu = (TextView) findViewById(R.id.menu);
		mMenu.setOnClickListener(this);
		mSet = (TextView) findViewById(R.id.start);
		mSet.setText(R.string.manager);
		mSet.setVisibility(View.VISIBLE);
		mSet.setOnClickListener(this);
		mMark = (Button) findViewById(R.id.mark);
		mMark.setText(R.string.add_beacon);
		mMark.setOnClickListener(this);
		findViewById(R.id.start_or_stop).setVisibility(View.GONE);
		mSearch = (SearchView) findViewById(R.id.search);
		mSearch.setOnSearchClickListener(this);
		mSearch.setOnQueryTextListener(this);
		mBeaconList = (ListView) findViewById(R.id.beacon_list);
		mBeaconAdapter = new LCBeaconListAdapter();
		mBeaconList.setAdapter(mBeaconAdapter);
		mBeaconList.setOnItemClickListener(this);
		mBack = (ImageView) findViewById(R.id.back);
		mBack.setOnClickListener(this);
		mSearchLayout = (LinearLayout) findViewById(R.id.search_layout);
		mSearchLayout.setOnClickListener(this);
		mLoadDialog.setCanceledOnTouchOutside(false);
		mStatus = (TextView) findViewById(R.id.status);
		RMLog.LOG_LEVEL = RMLog.LOG_LEVEL_INFO;
		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		mMapView.removeRefreshRunnable();
		mMapView.setUpdateMap(false);

		// beacon状态：-1删除，1新建，2修改,0默认
		Drawable green = getResources().getDrawable(R.drawable.sign_green);
		Drawable blue = getResources().getDrawable(R.drawable.sign_purple);
		Drawable gray = getResources().getDrawable(R.drawable.sign_gray);
		Drawable red = getResources().getDrawable(R.drawable.sign_red);
		Drawable black = getResources().getDrawable(R.drawable.sign_black);
		Drawable click = getResources().getDrawable(R.drawable.location_icon);

		// 编辑状态：0正常，1删除，2新建，3修改
		// 工作状态：0正常，-1低电量，-2故障，-3缺失，-4未知
		HashMap<Integer, Bitmap> bmpMap = new HashMap<Integer, Bitmap>();
		bmpMap.put(0, DTIOUtils.drawableToBitmap(green));
		bmpMap.put(1, DTIOUtils.drawableToBitmap(blue));
		bmpMap.put(2, DTIOUtils.drawableToBitmap(gray));
		bmpMap.put(3, DTIOUtils.drawableToBitmap(red));

		bmpMap.put(4, DTIOUtils.drawableToBitmap(black));
		bmpMap.put(5, DTIOUtils.drawableToBitmap(red));

		mBeaconLayer = new BeaconLayer(mMapView, bmpMap);
		mBeaconLayer.setNameVisibility(true);
		mBeaconLayer.setClickPoint(DTIOUtils.drawableToBitmap(click));
		CompassLayer mCompassLayer = new CompassLayer(mMapView);// 指南针图层
		mMapView.addMapLayer(mCompassLayer);
		mMapView.addMapLayer(mBeaconLayer);
		mBeaconLayer.setOnPointClickListener(this);

		mBeaconMap = new HashMap<String, BeaconInfo>();
		mWorkStatus = getResources().getStringArray(R.array.beacon_work_status);
		initControl();
		initMenuDialog();
		initBeaconInfoDilog();
		initBeaconMoveDialog();
		importBeaconHistory(false);
		mMapView.initMapConfig(mFloor.getBuildId(), mFloor.getFloor());// 打开地图（建筑物id，楼层id）
	}

	/**
	 * 更新beacon数量
	 */
	private void updateBeaconCount() {
		// TODO Auto-generated method stub
		String str = "一共有" + mBeaconLayer.getPointCount() + "个点\n";
		int count = 0;
		for (int i = 0; i < mBeaconLayer.getPointCount(); i++) {
			BeaconInfo info = mBeaconLayer.getPoint(i);
			if (info.getMac().startsWith("C91A"))
				count++;
		}
		str += "定位beacon有" + count + "个，摇一摇有"
				+ (mBeaconLayer.getPointCount() - count) + "个";
		mStatus.setText(str);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!isBeaconStart) {
			beaconNameNoCheck();// 非巡检状态下显示
		}
	}

	/**
	 * 初始化上下左右按钮
	 */
	private void initControl() {
		final Button up = (Button) findViewById(R.id.btn_direction_up);
		final Button down = (Button) findViewById(R.id.btn_direction_down);
		final Button left = (Button) findViewById(R.id.btn_direction_left);
		final Button right = (Button) findViewById(R.id.btn_direction_right);

		View.OnTouchListener touchListener = new View.OnTouchListener() {
			boolean longClick = false;

			// 方向键
			@SuppressLint("HandlerLeak")
			Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					PointInfo point = mMapView.fromLocation(mMapView
							.getCenter());

					if (longClick) {// 长按方向键地图持续挪动
						Message message = new Message();
						message.what = msg.what;
						message.arg1 = msg.arg1;
						handler.sendMessageDelayed(message, 100);
					} else {
						handler.removeMessages(0);
						return;
					}

					switch (msg.arg1) {
					case R.id.btn_direction_up:
						point.setY(point.getY() - adjustLength);
						break;
					case R.id.btn_direction_down:
						point.setY(point.getY() + adjustLength);
						break;
					case R.id.btn_direction_left:
						point.setX(point.getX() - adjustLength);
						break;
					case R.id.btn_direction_right:
						point.setX(point.getX() + adjustLength);
						break;
					}
					Location location = mMapView.fromPixels(point);
					mMapView.setCenter(location.getX(), location.getY(), false);
				}
			};

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					longClick = true;
					Message msg = new Message();
					msg.what = 0;
					msg.arg1 = v.getId();
					handler.sendMessage(msg);
					return true;
				case MotionEvent.ACTION_UP:
					longClick = false;
					return true;
				}
				return false;
			}
		};
		up.setOnTouchListener(touchListener);
		down.setOnTouchListener(touchListener);
		left.setOnTouchListener(touchListener);
		right.setOnTouchListener(touchListener);
	}

	/**
	 * 导入beacon数据
	 */
	private void importBeaconHistory(final boolean isClearBeaconStatus) {
		new LCAsyncTask(new LCCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				String filepath = DTFileUtils.getDataDir()
						+ mFloor.getBuildId() + "_" + mFloor.getFloor()
						+ ".txt";
				return loadBeaconData(filepath);
			}

			@Override
			public void onCallBackFinish(Object obj) {
				if (obj != null) {
					BeaconList list = (BeaconList) obj;
					mBeaconLayer.clearAllPoints();
					mBeaconMap.clear();
					if (list != null && list.getList() != null
							&& list.getList().size() > 0) {
						for (BeaconInfo info : list.getList()) {
							if (info.getMajor16() == null) {// 则没有计算
								String mac = info.getMac();
								String major = mac.substring(4, 8);
								String minor = mac.substring(8, 12);
								info.setMajor(new BigInteger(major, 16)
										.toString());
								info.setMinor(new BigInteger(minor, 16)
										.toString());
								info.setMajor16(major);
								info.setMinor16(minor);
							}
							if (isClearBeaconStatus)
								info.setWork_status(-4);
							mBeaconLayer.addPoint(info);
							mBeaconMap.put(info.getMac(), info);
						}
						beaconNameNoCheck();// 非巡检状态下显示
						mStatus.setText("一共有" + mBeaconLayer.getPointCount()
								+ "个点\n");
						mMapView.refreshMap();
						exportBeaconInfo();
					}
				}
			}
		}).run();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.search:
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) {
				imm.hideSoftInputFromWindow(mSearch.getWindowToken(), 0);
			}
			mSearch.clearFocus();
			break;
		case R.id.back:
			mSearchLayout.setVisibility(View.GONE);
			break;
		case R.id.menu:
			mMenuDialog.show();
			break;
		case R.id.move:// 移动
			mInfoDialog.cancel();
			PointInfo temppoi = mMapView.fromLocation(new Location(mClickPoint
					.getX() / 1000.0f, mClickPoint.getY() / 1000.0f));// 得到点在屏幕上的xy
			// 设置window位置
			Window win = mMoveDialog.getWindow();
			LayoutParams params = win.getAttributes();
			DTLog.e("dialog.x : " + params.x + "   dialog.y: " + params.y);
			WindowManager wm = this.getWindowManager();
			int width = wm.getDefaultDisplay().getWidth();
			params.gravity = Gravity.CENTER;
			if (temppoi.getX() > width / 2)
				params.x = width / 4;
			else
				params.x = -width / 4;
			params.y = 50;
			win.setAttributes(params);
			mMoveDialog.show();
			break;
		case R.id.start:
			Intent intent = new Intent(this, LCBeaconSetActivity.class);
			startActivity(intent);
			break;
		case R.id.mark:
			Intent setBeacon = new Intent(this, LCAddBeaconActivity.class);
			setBeacon.putExtra("x", mMapView.getCenter().getX());
			setBeacon.putExtra("y", mMapView.getCenter().getY());
			setBeacon.putExtra("build", mFloor.getBuildId());
			setBeacon.putExtra("floor", mFloor.getFloor());
			LCAddBeaconActivity.mMacSet = mBeaconMap;
			startActivityForResult(setBeacon, 10);
			break;
		case R.id.delete_restore:
			if (mClickPoint != null) {
				// 编辑状态：0正常，1删除，2新建，3修改
				// 工作状态：0正常，-1低电量，-2故障，-3缺失，-4未知
				if (mClickPoint.getEdit_status() == 1) {// 当为1删除状态时
					mClickPoint.setEdit_status(0);// 还原
					mDeleteReStore.setText(R.string.delete);
					mUpdate.setVisibility(View.VISIBLE);
				} else {
					if (mClickPoint.getEdit_status() == 2) {// 2新建：删除数据，其他则更改为删除状态
						mBeaconLayer.clearPoint(mClickPoint);
						mBeaconMap.remove(mClickPoint.getMac());
					} else {
						mClickPoint.setEdit_status(1);
					}
					mInfoDialog.cancel();
				}
				exportBeaconInfo();
				mMapView.refreshMap();
			}
			break;
		case R.id.update:// 修改按钮点击
			mInfoDialog.cancel();
			Intent update = new Intent(this, LCUpdateBeaconActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("beacon", mClickPoint);
			update.putExtras(bundle);
			startActivityForResult(update, 20);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int arg1, Intent intent) {
		super.onActivityResult(requestCode, arg1, intent);
		if (arg1 == Activity.RESULT_OK) {
			Bundle bundle = intent.getExtras();
			BeaconInfo info = (BeaconInfo) bundle.getSerializable("beacon");
			DTLog.i("info : " + info.getBroadcast_id() + "   " + info.getUuid());
			if (requestCode == 10) {
				mBeaconLayer.addPoint(info);
				mBeaconMap.put(info.getMac(), info);
			} else {
				for (int i = 0; i < mBeaconLayer.getPointCount(); i++) {
					if (mBeaconLayer.getPoint(i).getMac().equals(info.getMac())) {
						mBeaconLayer.clearPoint(i);
						mBeaconMap.put(info.getMac(), info);
						mBeaconLayer.addPoint(info);
						break;
					}
				}
			}
			exportBeaconInfo();
			mMapView.refreshMap();
		}
	}

	/**
	 * 初始化菜单弹出框
	 */
	private void initMenuDialog() {
		mMenuDialog = new Dialog(this, R.style.dialog);
		mMenuDialog.setContentView(R.layout.dialog_map_layout);
		mMenuDialog.setCanceledOnTouchOutside(true);
		ListView mInterList = (ListView) mMenuDialog
				.findViewById(R.id.set_list);
		String[] interDate = getResources().getStringArray(
				R.array.beacon_nolbs_menu);
		mInterList.setAdapter(new LCMapDialogAdapter(this, interDate));
		mInterList.setOnItemClickListener(this);
	}

	/**
	 * 初始化beacon移动
	 */
	private void initBeaconMoveDialog() {
		mMoveDialog = new Dialog(this, R.style.dialog_white);
		mMoveDialog.setContentView(R.layout.layout_direction);
		mMoveDialog.setCanceledOnTouchOutside(true);
		directionBtnListener();
		mMoveDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				exportBeaconInfo();
			}
		});
	}

	/**
	 * 初始化beacon移动上下左右按钮
	 */
	private void directionBtnListener() {
		final Button up = (Button) mMoveDialog.findViewById(R.id.button_up);
		final Button down = (Button) mMoveDialog.findViewById(R.id.button_down);
		final Button left = (Button) mMoveDialog.findViewById(R.id.button_left);
		final Button right = (Button) mMoveDialog
				.findViewById(R.id.button_right);

		View.OnTouchListener touchListener = new View.OnTouchListener() {
			boolean longClick = false;

			// 方向键
			@SuppressLint("HandlerLeak")
			Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					PointInfo point = mMapView.fromLocation(mMapView
							.getCenter());
					PointInfo clickPoint = mMapView.fromLocation(new Location(
							mClickPoint.getX() / 1000.0f,
							mClickPoint.getY() / 1000.0f));

					if (longClick) {// 长按方向键地图持续挪动
						Message message = new Message();
						message.what = msg.what;
						message.arg1 = msg.arg1;
						handler.sendMessageDelayed(message, 100);
					} else {
						handler.removeMessages(0);
						return;
					}

					switch (msg.arg1) {
					case R.id.button_up:
						point.setY(point.getY() - adjustLength);
						clickPoint.setY(clickPoint.getY() - adjustLength);
						break;
					case R.id.button_down:
						point.setY(point.getY() + adjustLength);
						clickPoint.setY(clickPoint.getY() + adjustLength);
						break;
					case R.id.button_left:
						point.setX(point.getX() - adjustLength);
						clickPoint.setX(clickPoint.getX() - adjustLength);
						break;
					case R.id.button_right:
						point.setX(point.getX() + adjustLength);
						clickPoint.setX(clickPoint.getX() + adjustLength);
						break;
					}
					Location location = mMapView.fromPixels(point);
					Location clicklocation = mMapView.fromPixels(clickPoint);
					mClickPoint.setX((int) (clicklocation.getX() * 1000));
					mClickPoint.setY((int) (clicklocation.getY() * 1000));
					if (mClickPoint.getEdit_status() != 2)// 属于新建
						mClickPoint.setEdit_status(3);
					mMapView.setCenter(location.getX(), location.getY(), false);
				}
			};

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					longClick = true;
					Message msg = new Message();
					msg.what = 0;
					msg.arg1 = v.getId();
					handler.sendMessage(msg);
					return true;
				case MotionEvent.ACTION_UP:
					longClick = false;
					return true;
				}
				return false;
			}
		};
		up.setOnTouchListener(touchListener);
		down.setOnTouchListener(touchListener);
		left.setOnTouchListener(touchListener);
		right.setOnTouchListener(touchListener);
	}

	/**
	 * 初始化beacon信息弹出框
	 */
	private void initBeaconInfoDilog() {
		mInfoDialog = new Dialog(this, R.style.dialog_white);
		mInfoDialog.setContentView(R.layout.dialog_text_layout);
		mInfoDialog.setCanceledOnTouchOutside(true);
		mInfoText = (TextView) mInfoDialog.findViewById(R.id.beacon_info);
		mDeleteReStore = (Button) mInfoDialog.findViewById(R.id.delete_restore);
		mUpdate = (Button) mInfoDialog.findViewById(R.id.update);
		mMove = (Button) mInfoDialog.findViewById(R.id.move);
		mMove.setOnClickListener(this);
		mDeleteReStore.setOnClickListener(this);
		mUpdate.setOnClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long arg3) {
		if (arg0.getId() == R.id.beacon_list) {
			mSearchLayout.setVisibility(View.GONE);
			BeaconInfo info = mBeaconAdapter.getItem(position);
			onBeaconClick(info, info.getMac());
		} else {
			switch (position) {
			case 0:// 下载beacon信息
				mLoadDialog.show();
				new LCAsyncTask(new BeaconDownLoadCall()).run();
				break;
			case 1:// 搜索
				for (int i = 0; i < mBeaconLayer.getPointCount(); i++) {
					mBeaconLayer.getPoint(i).setClick(false);
				}
				mSearchLayout.setVisibility(View.VISIBLE);
				break;
			case 2:
				updateBeaconCount();
				mBeaconLayer.setType(true);
				mMapView.refreshMap();
				findViewById(R.id.beacon_status_color_layout).setVisibility(
						View.GONE);
				findViewById(R.id.beacon_type_color_layout).setVisibility(
						View.VISIBLE);
				break;
			case 3:
				mStatus.setText("一共有" + mBeaconLayer.getPointCount() + "个点\n");
				mBeaconLayer.setType(false);
				mMapView.refreshMap();
				findViewById(R.id.beacon_status_color_layout).setVisibility(
						View.VISIBLE);
				findViewById(R.id.beacon_type_color_layout).setVisibility(
						View.GONE);
				break;
			case 4:// 数据还原
				mMenuDialog.cancel();
				String filepath = DTFileUtils.getDataDir()
						+ mFloor.getBuildId() + "_" + mFloor.getFloor()
						+ ".txt";
				String destPath = DTFileUtils.getBackupDir()
						+ mFloor.getBuildId() + "_" + mFloor.getFloor()
						+ ".txt";
				DTFileUtils.copyFile(destPath, filepath, false);
				importBeaconHistory(false);
				break;
			}
			mMenuDialog.cancel();
		}
	}

	@Override
	protected void onDestroy() {
		mMapView.clearMapLayer();
		mBeaconMap.clear();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (mSearchLayout.getVisibility() == View.GONE)
			super.onBackPressed();
		else
			mSearchLayout.setVisibility(View.GONE);
	}

	/**
	 * 导出beacon信息
	 */
	private void exportBeaconInfo() {
		new LCAsyncTask(new LCCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				ArrayList<BeaconInfo> beaconlist = mBeaconLayer.getPointList();
				if (beaconlist == null || beaconlist.size() == 0)
					return null;
				String filepath = DTFileUtils.getDataDir()
						+ mFloor.getBuildId() + "_" + mFloor.getFloor()
						+ ".txt";
				boolean isCopy = false;// 是否备份
				int changeCount = 0;// 修改的beacon点数
				for (BeaconInfo info : beaconlist) {
					if (info.getWork_status() != -4) {
						changeCount++;
					}
					if (changeCount >= 5) {// 一旦修改的点达到5个，则备份数据
						isCopy = true;
						break;
					}
				}
				try {
					File file = new File(filepath);
					if (!file.exists()) {
						file.createNewFile();
					}
					BufferedWriter br = new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream(file),
									"utf-8"));
					BeaconList list = new BeaconList();
					list.setList(beaconlist);
					Gson gson = new Gson();
					String str = gson.toJson(list);
					DTLog.e("result : " + str);
					br.write(str);
					br.flush();
					br.close();
					if (isCopy) {
						String destPath = DTFileUtils.getBackupDir()
								+ mFloor.getBuildId() + "_" + mFloor.getFloor()
								+ ".txt";
						DTFileUtils.copyFile(filepath, destPath, false);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			public void onCallBackFinish(Object obj) {
			}
		}).run();

	}

	/**
	 * 下载beacon信息
	 * 
	 * @author dingtao
	 *
	 */
	class BeaconDownLoadCall implements LCCallBack {

		@Override
		public Object onCallBackStart(Object... obj) {
			String url = String.format(
					LCHttpUrl.DOWNLOAD_BEACON,
					LCApplication.getInstance().getShare()
							.getString(DTFileUtils.PREFS_TOKEN, ""),
					mFloor.getBuildId(),
					DTStringUtils.floorTransform(mFloor.getFloor()));
			String path = DTFileUtils.getDownloadDir() + "data.zip";
			if (LCHttpClient.downloadFile(path, url)) {
				DTUIUtils.showToastSafe(R.string.beacon_down_success);
				String zippath = DTFileUtils.getDownloadDir() + "data.zip";
				String filepath = DTFileUtils.getDataDir()
						+ mFloor.getBuildId() + "_" + mFloor.getFloor()
						+ ".txt";
				DTFileUtils.zipToFile(zippath, filepath);
				return filepath;
			}
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			mLoadDialog.cancel();
			if (obj != null) {
				importBeaconHistory(true);
			}
		}
	}

	@Override
	public void onBeaconClick(LCPoint point, String key) {
		mClickPoint = (BeaconInfo) point;
		mClickPoint.setClick(true);
		PointInfo temppoi = mMapView.fromLocation(new Location(mClickPoint
				.getX() / 1000.0f, mClickPoint.getY() / 1000.0f));// 得到点在屏幕上的xy
		WindowManager wm = this.getWindowManager();

		int width = wm.getDefaultDisplay().getWidth();

		PointInfo centerPoint = mMapView.fromLocation(mMapView.getCenter());
		// 设置window位置
		Window win = mInfoDialog.getWindow();
		LayoutParams params = win.getAttributes();
		DTLog.e("dialog.x : " + params.x + "   dialog.y: " + params.y);
		int difX;
		if (temppoi.getX() > width / 2) {// 点在右边
			difX = (int) (width / 4 - temppoi.getX() + width / 2);// x轴差值
			win.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		} else {
			win.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			difX = (int) (width / 4 - temppoi.getX());// x轴差值
		}
		params.x = width / 4 + 15;// 设置x坐标
		centerPoint.setX(centerPoint.getX() - difX);
		centerPoint.setY(temppoi.getY());
		Location result = mMapView.fromPixels(centerPoint);
		mMapView.setCenter(result.getX(), result.getY(), false);

		win.setAttributes(params);

		String status = "";
		status += "BuildId: " + mClickPoint.getBuildId();
		status += "\nFloor: " + mClickPoint.getFloor();
		status += "\nMac: " + mClickPoint.getMac();
		status += "\nMajor: " + mClickPoint.getMajor();
		status += "\nMinor: " + mClickPoint.getMinor();
		status += "\n信号强度: " + mClickPoint.getRssi_max();
		status += "\nSwitchMax: " + mClickPoint.getThreshold_switch_max();
		status += "\nSwitchMin: " + mClickPoint.getThreshold_switch_min();
		status += "\n状态: "
				+ mWorkStatus[Math.abs(mClickPoint.getWork_status())];
		mInfoText.setText(status);
		if (mClickPoint.getEdit_status() == 1) {// 状态为删除时，请还原后在修改状态
			mDeleteReStore.setText(R.string.restore);
			mUpdate.setVisibility(View.GONE);
		} else {
			mDeleteReStore.setText(R.string.delete);
			mUpdate.setVisibility(View.VISIBLE);
		}
		mInfoDialog.show();
	}

	/**
	 * 非巡检状态显示major和minor
	 */
	private void beaconNameNoCheck() {
		for (int i = 0; i < mBeaconLayer.getPointCount(); i++) {
			BeaconInfo info = mBeaconLayer.getPoint(i);
			String name = "";
			if (LCApplication.getInstance().getShare()
					.getBoolean("major_switch", false)) {// 是否拼接major
				if (LCApplication.getInstance().getShare()
						.getBoolean("int16_switch", false))
					name += " " + info.getMajor16();
				else
					name += " " + info.getMajor();
			}
			if (LCApplication.getInstance().getShare()
					.getBoolean("minor_switch", false)) {
				if (LCApplication.getInstance().getShare()
						.getBoolean("int16_switch", false))
					name += " " + info.getMinor16();
				else
					name += " " + info.getMinor();
			}
			info.setName(name);
		}
		mMapView.refreshMap();
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		DTLog.e("newText : " + newText);
		mBeaconAdapter.clearList();
		if (!DTStringUtils.isEmpty(newText)) {
			for (int i = 0; i < mBeaconLayer.getPointCount(); i++) {
				mBeaconLayer.getPoint(i).setClick(false);
				if (mBeaconLayer.getPoint(i).getMac()
						.contains(newText.toUpperCase())) {
					mBeaconAdapter.addItem(mBeaconLayer.getPoint(i), 0);
				}
				if ((mBeaconLayer.getPoint(i).getMajor() + "")
						.contains(newText)) {
					mBeaconAdapter.addItem(mBeaconLayer.getPoint(i), 0);
				}
				if ((mBeaconLayer.getPoint(i).getMinor() + "")
						.contains(newText)) {
					mBeaconAdapter.addItem(mBeaconLayer.getPoint(i), 0);
				}
			}
		}
		mBeaconAdapter.notifyDataSetChanged();
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(mSearch.getWindowToken(), 0);
		}
		mSearch.clearFocus();
		return true;
	}
}
