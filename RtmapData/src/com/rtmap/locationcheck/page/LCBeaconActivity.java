package com.rtmap.locationcheck.page;

import java.io.File;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.MapView.OnMapModeChangedListener;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;
import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.adapter.LCBeaconListAdapter;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.LCAsyncTask;
import com.rtmap.locationcheck.core.LCCallBack;
import com.rtmap.locationcheck.core.LCSqlite;
import com.rtmap.locationcheck.core.model.BeaconInfo;
import com.rtmap.locationcheck.core.model.BroadcastInfo;
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
public class LCBeaconActivity extends LCActivity implements OnClickListener,
		OnItemClickListener, OnBeaconClickListener, OnQueryTextListener,
		RMLocationListener {

	private Floor mFloor;// 楼层信息
	private MapView mMapView;// 地图View
	private TextView mStatus;// 标题，菜单键
	private Dialog mInfoDialog;// 间隔dialog
	private Dialog mMoveDialog;// 移动Dialog
	private TextView mInfoText;// beacon信息
	private Button mDeleteReStore, mUpdate, mMove;// 删除或者重置，修改按钮
	private BeaconLayer mBeaconLayer;// Beacon图层
	private Button mMark, mFind;// 添加按钮
	private BeaconInfo mClickPoint;// 点击Beacon
	private String[] mWorkStatus;// 工作状态
	private boolean isBeaconStart;// 开始
	private SearchView mSearch;// 搜索View,4.0系统才能使用这个控件
	private ListView mBeaconList;// 搜索列表
	private LCBeaconListAdapter mBeaconAdapter;// 搜索的beacon列表
	private ImageView mBack;// 返回
	private LinearLayout mSearchLayout;// 搜索布局
	private RelativeLayout mBtnLayout;

	private HashMap<String, BeaconInfo> mBeaconMap;// beacon键值对mac值

	private Dao<BeaconInfo, String> mBeaconDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_beacon);
		mFloor = (Floor) getIntent().getExtras().getSerializable("floor");
		initTitleBar(getIntent().getExtras().getInt("position"));

		mBeaconDao = LCSqlite.getInstance().createBeaconTable(
				mFloor.getBuildId(), mFloor.getFloor());

		mManager.setVisibility(View.VISIBLE);
		mMark = (Button) findViewById(R.id.mark);
		mMark.setText(R.string.add_beacon);
		mMark.setOnClickListener(this);
		mBtnLayout = (RelativeLayout) findViewById(R.id.btn_layout);
		mFind = (Button) findViewById(R.id.find);
		mFind.setOnClickListener(this);
		mSearch = (SearchView) findViewById(R.id.search);
		mSearch.setOnSearchClickListener(this);
		mSearch.setOnQueryTextListener(this);
		mBeaconList = (ListView) findViewById(R.id.beacon_list);
		mBeaconAdapter = new LCBeaconListAdapter();
		mBeaconList.setAdapter(mBeaconAdapter);
		mBeaconList.setOnItemClickListener(this);
		mBack = (ImageView) findViewById(R.id.back);
		mBack.setOnClickListener(this);
		mStatus = (TextView) findViewById(R.id.status);
		mSearchLayout = (LinearLayout) findViewById(R.id.search_layout);
		mSearchLayout.setOnClickListener(this);
		mLoadDialog.setCanceledOnTouchOutside(false);

		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		mMapView.setUpdateMap(false);
		mMapView.setOnMapModeChangedListener(new OnMapModeChangedListener() {

			@Override
			public void onMapModeChanged() {
				float x = mMapView.getCenter().getX();
				float y = mMapView.getCenter().getY();
				mStatus.setText(String.format("x: %.3f   y: %.3f", x, y));
			}
		});

		// beacon状态：-1删除，1新建，2修改,0默认
		Drawable green = getResources().getDrawable(R.drawable.sign_green);
		Drawable blue = getResources().getDrawable(R.drawable.sign_purple);
		Drawable black = getResources().getDrawable(R.drawable.sign_gray);
		Drawable red = getResources().getDrawable(R.drawable.sign_red);

		// 编辑状态：0正常，1删除，2新建，3修改
		// 工作状态：0正常，-1低电量，-2故障，-3缺失，-4未知
		HashMap<Integer, Bitmap> bmpMap = new HashMap<Integer, Bitmap>();
		bmpMap.put(0, DTIOUtils.drawableToBitmap(green));
		bmpMap.put(1, DTIOUtils.drawableToBitmap(blue));
		bmpMap.put(2, DTIOUtils.drawableToBitmap(black));
		bmpMap.put(3, DTIOUtils.drawableToBitmap(red));

		mBeaconLayer = new BeaconLayer(mMapView, bmpMap);
		mBeaconLayer.setNameVisibility(true);
		Drawable click = getResources().getDrawable(R.drawable.location_icon);
		mBeaconLayer.setClickPoint(DTIOUtils.drawableToBitmap(click));
		CompassLayer mCompassLayer = new CompassLayer(mMapView);// 指南针图层
		mMapView.addMapLayer(mCompassLayer);
		mMapView.addMapLayer(mBeaconLayer);
		mBeaconLayer.setOnPointClickListener(this);

		mBeaconMap = new HashMap<String, BeaconInfo>();
		mWorkStatus = getResources().getStringArray(R.array.beacon_work_status);
		initControl();
		initLocation();
		LocationApp.getInstance().registerLocationListener(this);
		initBeaconInfoDilog();
		initBeaconMoveDialog();
		importBeaconHistory(false);
		mMapView.initMapConfig(mFloor.getBuildId(), mFloor.getFloor());// 打开地图（建筑物id，楼层id）
		onTitleSelected(getIntent().getExtras().getInt("position"));
	}

	@Override
	public void onResume() {
		super.onResume();
		if (LCBeaconSetActivity.isUpdate) {
			LCBeaconSetActivity.isUpdate = false;
			importBeaconHistory(false);
		}
		if (!isBeaconStart) {
			beaconNameNoCheck();// 非巡检状态下显示
		}
		if (isOpenLocation)
			LocationApp.getInstance().start();
		else
			DTUIUtils.showToastSafe("定位未开启");
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
						handler.sendMessageDelayed(message, DOWN_TIME);
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
					mStatus.setText(String.format("x: %.3f   y: %.3f",
							location.getX(), location.getY()));
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
					v.setBackgroundResource(R.drawable.btn_purple);
					return true;
				case MotionEvent.ACTION_UP:
					v.setBackgroundResource(R.drawable.btn_purple_round_new);
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
						+ mFloor.getBuildId() + File.separator
						+ mFloor.getBuildId() + "_" + mFloor.getFloor()
						+ ".txt";
				try {
					List<BeaconInfo> list = null;
					if (DTFileUtils.checkFile(filepath)) {
						list = loadBeaconData(filepath);
						DTFileUtils.deleteFile(filepath);// 读取完数据，删除文件
						mBeaconDao.delete(mBeaconDao.queryForAll());// 数据库删除全部数据
					} else {// 查询数据
						list = mBeaconDao.queryForAll();
					}
					mBeaconMap.clear();
					if (list != null && list.size() > 0) {
						for (BeaconInfo info : list) {
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
							mBeaconDao.createOrUpdate(info);// 根据读取的状态存放数据，因为不确定是首次读取文件还是已经在数据库中，因为要清楚状态
							mBeaconMap.put(info.getMac(), info);
							if (!RMStringUtils.isEmpty(info.getMaclistjson())) {
								JSONArray maclistarray = new JSONArray(info
										.getMaclistjson());
								info.setMaclist(new ArrayList<BroadcastInfo>());
								for (int i = 0; i < maclistarray.length(); i++) {// 将包含的广播id加载到HashMap中
									JSONObject u = maclistarray
											.getJSONObject(i);
									BroadcastInfo broad = new BroadcastInfo();
									broad.setMac(u.getString("mac"));
									broad.setMajor(u.getString("major"));
									broad.setMinor(u.getString("minor"));
									broad.setUuid(u.getString("uuid"));
									info.getMaclist().add(broad);
									mBeaconMap.put(broad.getMac(), info);
								}
							}
						}
					}
					mBeaconLayer.clearAllPoints();
					mBeaconLayer.addPointList(list);
					return list;
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			public void onCallBackFinish(Object obj) {
				if (obj != null) {
					beaconNameNoCheck();// 非巡检状态下显示
					// exportBeaconInfo();
					mStatus.setText("一共有" + mBeaconLayer.getPointCount() + "个点");
				}
			}
		}).run();
	}

	@Override
	public void onClick(View v) {
		if (isBeaconStart) {// 如果正在巡检
			DTUIUtils.showToastSafe("请停止巡检，在进行操作");
			return;
		}
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
		case R.id.find:
			for (int i = 0; i < mBeaconLayer.getPointCount(); i++) {
				mBeaconLayer.getPoint(i).setClick(false);
			}
			mSearchLayout.setVisibility(View.VISIBLE);
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
				try {
					if (mClickPoint.getEdit_status() == 1) {// 当为1删除状态时
						mClickPoint.setEdit_status(0);// 还原
						mDeleteReStore.setText(R.string.delete);
						mUpdate.setVisibility(View.VISIBLE);
						mBeaconDao.update(mClickPoint);// 更新
					} else {
						if (mClickPoint.getEdit_status() == 2) {// 2新建：删除数据，其他则更改为删除状态
							mBeaconDao.delete(mClickPoint);// 删除
							mBeaconLayer.clearPoint(mClickPoint);
							mBeaconMap.remove(mClickPoint.getMac());
						} else {
							mClickPoint.setEdit_status(1);
							mBeaconDao.update(mClickPoint);// 更新
						}
						mInfoDialog.cancel();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				// exportBeaconInfo();
				mMapView.refreshMap();
			}
			break;
		case R.id.update:// 修改按钮点击
			mInfoDialog.cancel();
			Intent update = new Intent(this, LCUpdateBeaconActivity.class);
			Bundle bundle1 = new Bundle();
			bundle1.putSerializable("beacon", mClickPoint);
			update.putExtras(bundle1);
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
			try {
				if (requestCode == 10) {// 新增beacon
					mBeaconDao.createIfNotExists(info);// 新增一个beacon
					mBeaconLayer.addPoint(info);
					mBeaconMap.put(info.getMac(), info);
				} else {// 修改beacon
					for (int i = 0; i < mBeaconLayer.getPointCount(); i++) {
						if (mBeaconLayer.getPoint(i).getMac()
								.equals(info.getMac())) {
							mBeaconLayer.clearPoint(i);
							mBeaconDao.update(info);
							mBeaconMap.put(info.getMac(), info);
							mBeaconLayer.addPoint(info);
							break;
						}
					}
				}
				mMapView.refreshMap();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			// exportBeaconInfo();
		}
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
				try {
					mBeaconDao.update(mClickPoint);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				// exportBeaconInfo();
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
						handler.sendMessageDelayed(message, DOWN_TIME);
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

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long arg3) {
		if (arg0.getId() == R.id.beacon_list) {
			mSearchLayout.setVisibility(View.GONE);
			BeaconInfo info = mBeaconAdapter.getItem(position);
			onBeaconClick(info, info.getMac());
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onPause() {
		super.onPause();
		DTLog.e("onPause");
		LocationApp.getInstance().stop();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		LocationApp.getInstance().unRegisterLocationListener(this);
		mBeaconLayer.destroyLayer();
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
		// status += "BuildId: " + mClickPoint.getBuildId();
		// status += "\nFloor: " + mClickPoint.getFloor();
		status += "Mac: " + mClickPoint.getMac();
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
	 * 检查beacon
	 * 
	 * @author dingtao
	 *
	 */
	class CheckBeaconCall implements LCCallBack {

		@Override
		public Object onCallBackStart(Object... obj) {
			String result = (String) obj[0];
			if (!result.contains("<beacons"))
				return null;
			result = result.substring(
					result.indexOf("<beacons>") + "<beacons>".length(),
					result.indexOf("</beacons>"));
			String[] b = result.split("#");
			for (int i = 0; i < b.length; i++) {
				if (DTStringUtils.isEmpty(b[i]))
					continue;
				String mac = b[i].substring(0, b[i].indexOf("$")).toUpperCase();
				String rssivalue = b[i].substring(b[i].indexOf("$") + 1);
				if (mBeaconMap.containsKey(mac)) {// 如果页面中的beacon包含扫描的
					BeaconInfo info = mBeaconMap.get(mac);
					boolean open = LCApplication.getInstance().getShare()
							.getBoolean("threshold_switch", false);// 是否增加阈值判断
					int rssi = Integer.parseInt(rssivalue);
					if (info.getRssi_max() == 0 || info.getRssi_max() < rssi)
						info.setRssi_max(rssi);
					info.setRssi(rssi);
					info.setName(rssivalue);// 设置名字
					if (open) {// 如果开启阈值判断
						int mValue = LCApplication.getInstance().getShare()
								.getInt("threshold", -99);
						if (mValue == -25) {
							if (rssi == -25) {
								info.setWork_status(0);
							}
						} else {
							if (rssi >= mValue && rssi <= -25) {
								info.setWork_status(0);
							}
						}
					} else {// 没有开启则只要有mac便是正常
						info.setWork_status(0);
					}
					try {
						mBeaconDao.update(info);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			mMapView.refreshMap();
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			// exportBeaconInfo();
		}
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
	public void onTitleSelected(int position) {
		super.onTitleSelected(position);
		if (position == 0) {
			isBeaconStart = false;
			mBtnLayout.setVisibility(View.VISIBLE);
			if (!isOpenLocation) {
				LocationApp.getInstance().stop();
			}

		} else if (position == 3) {
			LocationApp.getInstance().start();
			isBeaconStart = true;
			mBtnLayout.setVisibility(View.GONE);
		}
		if (isBeaconStart) {
			if (mBeaconLayer.getPointCount() > 0) {
				for (BeaconInfo info : mBeaconLayer.getPointList()) {
					info.setName("");
				}
			}
		} else {
			beaconNameNoCheck();
		}
		mMapView.refreshMap();
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

	private RMLocation mLocation;

	@Override
	public void moveCenter() {
		super.moveCenter();
		if (mLocation != null && mLocation.getError() == 0) {
			mMapView.setCenter(mLocation.getX(), mLocation.getY());
			if (!mLocation.getFloor().equals(mFloor.getFloor())
					|| !mLocation.getBuildID().equals(mFloor.getBuildId()))
				DTUIUtils.showToastSafe("楼层不一致");
		} else {
			DTUIUtils.showToastSafe("无法定位");
		}
	}

	@Override
	public void onReceiveLocation(RMLocation location) {
		if (location.getError() == 0) {
			mMapView.setMyCurrentLocation(location);
		}
		mLocation = location;
		String scanner = LocationApp.getInstance().getScannerInfo();
		if (isBeaconStart && !DTStringUtils.isEmpty(scanner)
				&& scanner.contains("<beacons>")) {
			new LCAsyncTask(new CheckBeaconCall())
					.runOnExecutor(false, scanner);
		}
	}

}
