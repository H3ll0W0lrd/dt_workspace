package com.rtmap.locationcheck.page;

import java.io.File;
import java.io.IOException;
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
import android.graphics.Point;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.common.utils.RMStringUtils;
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
import com.rtmap.locationcheck.layer.BitmapBeaconLayer;
import com.rtmap.locationcheck.layer.OnLayerPointClickListener;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTIOUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;
import com.rtmap.locationcheck.util.map.Coord;
import com.rtmap.locationcheck.util.map.MapWidget;
import com.rtmap.locationcheck.util.map.MapWidget.OnMapTouchListener;
import com.rtmap.locationcheck.util.map.MapWidget.OnMouseListener;
import com.rtmap.locationcheck.util.map.MapWidget.WidgetStateListener;
import com.rtmap.locationcheck.util.map.PinMark;

/**
 * Poi与路网采集(位图)
 * 
 * @author dingtao
 *
 */
public class LCBeaconBitmapActivity extends LCActivity implements
		OnLayerPointClickListener, OnClickListener, RMLocationListener,
		OnQueryTextListener, OnItemClickListener {

	private MapWidget mapWidget;
	private BitmapBeaconLayer mBeaconLayer;// 采集点图层
	private PinMark pinMark;
	// private MapInfo mMapInfo;// 地图name,由MapSelectActivity传入
	private Button mMark;// 标记
	private Floor mFloor;
	private RelativeLayout mBtnLayout;
	private SearchView mSearch;// 搜索View,4.0系统才能使用这个控件
	private LinearLayout mSearchLayout;// 搜索布局
	private ListView mBeaconList;// 搜索列表
	private boolean isBeaconStart;// 开始
	private LCBeaconListAdapter mBeaconAdapter;// 搜索的beacon列表

	private HashMap<String, BeaconInfo> mBeaconMap;// beacon键值对mac值
	private float mScale;
	private Lock mLock = new ReentrantLock();
	private Dao<BeaconInfo, String> mBeaconDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_beacon_bitmap);
		mFloor = (Floor) getIntent().getExtras().getSerializable("floor");
		mScale = mFloor.getScale();
		mBeaconDao = LCSqlite.getInstance().createBeaconTable(
				mFloor.getBuildId(), mFloor.getFloor());
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapWidget.onResume();
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
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapWidget.onPause();
		LocationApp.getInstance().stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocationApp.getInstance().unRegisterLocationListener(this);
		mapWidget.onPause();
		mBeaconLayer.clearAllCorrectPoints();
	}

	private void initContrls() {

		mSearch = (SearchView) findViewById(R.id.search);
		mSearch.setOnSearchClickListener(this);
		mSearch.setOnQueryTextListener(this);
		mSearchLayout = (LinearLayout) findViewById(R.id.search_layout);
		mSearchLayout.setOnClickListener(this);
		mBeaconList = (ListView) findViewById(R.id.beacon_list);
		mBeaconAdapter = new LCBeaconListAdapter();
		mBeaconList.setAdapter(mBeaconAdapter);
		mBeaconList.setOnItemClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);

		findViewById(R.id.find).setOnClickListener(this);
		mMark = (Button) findViewById(R.id.mark);
		initBeaconInfoDilog();
		initBeaconMoveDialog();

		mMark.setOnClickListener(this);

		final Button up = (Button) findViewById(R.id.btn_direction_up);
		final Button down = (Button) findViewById(R.id.btn_direction_down);
		final Button left = (Button) findViewById(R.id.btn_direction_left);
		final Button right = (Button) findViewById(R.id.btn_direction_right);

		View.OnTouchListener touchListener = new View.OnTouchListener() {
			boolean longClick = false;

			// 方向键
			Handler handler = new Handler() {

				@Override
				public void handleMessage(Message msg) {

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
						mapWidget.getCoordTransformer().bitmapTranslate(0,
								adjustLength);
						break;
					case R.id.btn_direction_down:
						mapWidget.getCoordTransformer().bitmapTranslate(0,
								-adjustLength);
						break;
					case R.id.btn_direction_left:
						mapWidget.getCoordTransformer().bitmapTranslate(
								adjustLength, 0);
						break;
					case R.id.btn_direction_right:
						mapWidget.getCoordTransformer().bitmapTranslate(
								-adjustLength, 0);
						break;
					}
					final Coord coord = new Coord();
					mapWidget.getCoordTransformer().clientToWorld(
							pinMark.getX(), pinMark.getY(), coord);

					float x = coord.mX * mScale;
					float y = coord.mY * mScale;
					mStatus.setText(String.format("x: %.3f   y: %.3f", x, y));
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
					longClick = false;
					v.setBackgroundResource(R.drawable.btn_purple_round_new);
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

	private TextView mStatus;

	private void init() {
		initTitleBar(getIntent().getExtras().getInt("position"));
		mManager.setVisibility(View.VISIBLE);
		initContrls();
		mBtnLayout = (RelativeLayout) findViewById(R.id.btn_layout);
		mStatus = (TextView) findViewById(R.id.status);

		mapWidget = (MapWidget) findViewById(R.id.map_view);
		mapWidget.registerWidgetStateListener(new WidgetStateListener() {
			@Override
			public void onMapWidgetCreated(MapWidget map) {
				String bitmap_path = DTFileUtils.getImageDir()
						+ mFloor.getBuildId() + "-" + mFloor.getFloor()
						+ ".jpg";
				mapWidget.openMapFile(bitmap_path);
			}
		});
		mapWidget.setOnMapTouchListener(new OnMapTouchListener() {

			@Override
			public void onMapTouch(MotionEvent event) {
				final Coord coord = new Coord();
				mapWidget.getCoordTransformer().clientToWorld(pinMark.getX(),
						pinMark.getY(), coord);

				float x = coord.mX * mScale;
				float y = coord.mY * mScale;
				mStatus.setText(String.format("x: %.3f   y: %.3f", x, y));
			}
		});
		// 初始标记点设置，图形、可见、居中
		pinMark = new PinMark(mapWidget, R.drawable.pin48);
		pinMark.setVisiable(true);
		pinMark.setLocation(getResources().getDisplayMetrics().widthPixels / 2,
				getResources().getDisplayMetrics().heightPixels / 2);
		// 添加标记点图层及采集点图层
		mapWidget.addMark(pinMark);
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
		mBeaconLayer = new BitmapBeaconLayer(bmpMap);

		Drawable click = getResources().getDrawable(R.drawable.location_icon);
		mBeaconLayer.setClickPoint(DTIOUtils.drawableToBitmap(click));
		mBeaconLayer.setNameVisibility(true);
		mBeaconLayer.setScale(mScale * 1000);
		mapWidget.addMark(mBeaconLayer);
		mapWidget.registerMouseListener(new OnMouseListener() {

			@Override
			public void onSingleTap(MapWidget mw, float x, float y) {
				mBeaconLayer.onSingleTap(mw, x, y, LCBeaconBitmapActivity.this);
			}
		});
		mBeaconMap = new HashMap<String, BeaconInfo>();

		initLocation();
		LocationApp.getInstance().registerLocationListener(this);
		// 导入beacon信息
		importBeaconHistory(false);
		onTitleSelected(getIntent().getExtras().getInt("position"));
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
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long arg3) {
		if (arg0.getId() == R.id.beacon_list) {
			mSearchLayout.setVisibility(View.GONE);
			BeaconInfo info = mBeaconAdapter.getItem(position);
			onClick(info);
		}
	}

	public void initLocation() {
		boolean istext = LCApplication.getInstance().getShare()
				.getBoolean("istest", false);
		if (istext)
			LocationApp.getInstance().setRootFolder(
					DTFileUtils.ROOT_DIR + File.separator + "test");
		else
			LocationApp.getInstance().setRootFolder(
					DTFileUtils.ROOT_DIR + File.separator + "publish");
		RMFileUtil.createPath(RMFileUtil.getBuildJudgeDir());
		try {
			if (!new File(RMFileUtil.getBuildJudgeDir() + "beacons.bei")
					.exists())
				DTFileUtils.copyAssestToSD(RMFileUtil.getBuildJudgeDir()
						+ "beacons.bei", "beacons.bei");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		LocationApp.getInstance().init(getApplicationContext());// 初始化定位
		LocationApp.getInstance().setUseRtmapError(true);// 设置使用智慧图错误码
		LocationApp.getInstance().setTestStatus(istext);
	}

	private TextView mInfoText;
	private Button mDeleteReStore, mUpdate, mMove;// 删除或者重置，修改按钮

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

	private BeaconInfo mBeaconInfo;

	@Override
	public void onClick(BeaconInfo info) {
		mBeaconInfo = info;
		mBeaconInfo.setClick(true);
		Point temppoi = new Point();
		mapWidget.getLCPointTransformer().worldToClient(
				info.getX() / mScale / 1000, info.getY() / mScale / 1000,
				temppoi);// 得到beaconInfo的屏幕坐标
		WindowManager wm = this.getWindowManager();

		int width = wm.getDefaultDisplay().getWidth();
		// 设置window位置
		Window win = mInfoDialog.getWindow();
		LayoutParams params = win.getAttributes();
		DTLog.e("dialog.x : " + params.x + "   dialog.y: " + params.y);
		int difX;
		if (temppoi.x > width / 2) {// 点在右边
			difX = (int) (width / 4 - temppoi.x + width / 2);// x轴差值
			win.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		} else {
			win.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			difX = (int) (width / 4 - temppoi.x);// x轴差值
		}
		params.x = width / 4 + 15;// 设置x坐标
		DTLog.i("difX : " + difX + "    X : " + pinMark.getX() + "    "
				+ temppoi.x + "     Y : " + pinMark.getY() + "    " + temppoi.y);
		mapWidget.getLCPointTransformer().bitmapTranslate(difX,
				pinMark.getY() - temppoi.y);

		win.setAttributes(params);

		String status = "";
		status += "BuildId: " + mBeaconInfo.getBuildId();
		status += "\nMac: " + mBeaconInfo.getMac();
		status += "\nMajor: " + mBeaconInfo.getMajor();
		status += "\nMinor: " + mBeaconInfo.getMinor();
		status += "\nX: " + mBeaconInfo.getX();
		status += "\nY: " + mBeaconInfo.getY();
		mInfoText.setText(status);
		mInfoDialog.show();
	}

	private Dialog mMoveDialog, mInfoDialog;

	/**
	 * 标记添加beacon
	 */
	private void mark() {
		final Coord coord = new Coord();
		mapWidget.getCoordTransformer().clientToWorld(pinMark.getX(),
				pinMark.getY(), coord);
		if (coord.isValid()) {
			Intent setBeacon = new Intent(this, LCAddBeaconActivity.class);
			setBeacon.putExtra("x", coord.mX * mScale);
			setBeacon.putExtra("y", coord.mY * mScale);
			setBeacon.putExtra("build", mFloor.getBuildId());
			setBeacon.putExtra("floor", mFloor.getFloor());
			LCAddBeaconActivity.mMacSet = mBeaconMap;
			startActivityForResult(setBeacon, 10);
		}

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
					mBeaconLayer.clearAllCorrectPoints();
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
						mBeaconLayer.getPointList().addAll(list);
					}
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
	protected void onActivityResult(int requestCode, int arg1, Intent intent) {
		super.onActivityResult(requestCode, arg1, intent);
		if (arg1 == Activity.RESULT_OK) {
			Bundle bundle = intent.getExtras();
			BeaconInfo info = (BeaconInfo) bundle.getSerializable("beacon");
			DTLog.i("info : " + info.getBroadcast_id() + "   " + info.getUuid());
			try {
				if (requestCode == 10) {
					mBeaconDao.createIfNotExists(info);// 新增一个beacon
					mBeaconLayer.addCorrectPoint(info);
					mBeaconMap.put(info.getMac(), info);
				} else {
					for (int i = 0; i < mBeaconLayer.getPointCount(); i++) {
						if (mBeaconLayer.getPoint(i).getMac()
								.equals(info.getMac())) {
							mBeaconLayer.clearPoint(i);
							mBeaconDao.update(info);
							mBeaconMap.put(info.getMac(), info);
							mBeaconLayer.addCorrectPoint(info);
							break;
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (isBeaconStart) {// 如果正在巡检
			DTUIUtils.showToastSafe("请停止巡检，在进行操作");
			return;
		}
		switch (v.getId()) {
		case R.id.mark:
			mark();
			break;
		case R.id.find:
			for (int i = 0; i < mBeaconLayer.getPointCount(); i++) {
				mBeaconLayer.getPoint(i).setClick(false);
			}
			mSearchLayout.setVisibility(View.VISIBLE);
			break;
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
		case R.id.update:// 修改按钮点击
			mInfoDialog.cancel();
			Intent update = new Intent(this, LCUpdateBeaconActivity.class);
			Bundle bundle1 = new Bundle();
			bundle1.putSerializable("beacon", mBeaconInfo);
			update.putExtras(bundle1);
			startActivityForResult(update, 20);
			break;
		case R.id.delete_restore:
			if (mBeaconInfo != null) {
				// 编辑状态：0正常，1删除，2新建，3修改
				// 工作状态：0正常，-1低电量，-2故障，-3缺失，-4未知
				try {
					if (mBeaconInfo.getEdit_status() == 1) {// 当为1删除状态时
						mBeaconInfo.setEdit_status(0);// 还原
						mDeleteReStore.setText(R.string.delete);
						mUpdate.setVisibility(View.VISIBLE);
						mBeaconDao.update(mBeaconInfo);// 更新
					} else {
						if (mBeaconInfo.getEdit_status() == 2) {// 2新建：删除数据，其他则更改为删除状态
							mBeaconDao.delete(mBeaconInfo);// 删除
							mBeaconLayer.clearPoint(mBeaconInfo);
							mBeaconMap.remove(mBeaconInfo.getMac());
						} else {
							mBeaconInfo.setEdit_status(1);
							mBeaconDao.update(mBeaconInfo);// 更新
						}
						mInfoDialog.cancel();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			break;
		case R.id.move:// 移动
			mInfoDialog.cancel();
			Point temppoi = new Point();
			mapWidget.getLCPointTransformer().worldToClient(
					mBeaconInfo.getX() / mScale / 1000,
					mBeaconInfo.getY() / mScale / 1000, temppoi);// 得到beaconInfo的屏幕坐标;
			// 设置window位置
			Window win = mMoveDialog.getWindow();
			LayoutParams params = win.getAttributes();
			DTLog.e("dialog.x : " + params.x + "   dialog.y: " + params.y);
			WindowManager wm = this.getWindowManager();
			int width = wm.getDefaultDisplay().getWidth();
			params.gravity = Gravity.CENTER;
			if (temppoi.x > width / 2)
				params.x = width / 4;
			else
				params.x = -width / 4;
			params.y = 130;
			win.setAttributes(params);
			mMoveDialog.show();
			break;
		}
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
					mBeaconDao.update(mBeaconInfo);
				} catch (SQLException e) {
					e.printStackTrace();
				}
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
					Point temppoi = new Point();
					mapWidget.getLCPointTransformer().worldToClient(
							mBeaconInfo.getX() / mScale / 1000,
							mBeaconInfo.getY() / mScale / 1000, temppoi);
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
						mapWidget.getCoordTransformer().bitmapTranslate(0,
								adjustLength);
						break;
					case R.id.button_down:
						mapWidget.getCoordTransformer().bitmapTranslate(0,
								-adjustLength);
						break;
					case R.id.button_left:
						mapWidget.getCoordTransformer().bitmapTranslate(
								adjustLength, 0);
						break;
					case R.id.button_right:
						mapWidget.getCoordTransformer().bitmapTranslate(
								-adjustLength, 0);
						break;
					}

					final Coord coord = new Coord();
					mapWidget.getCoordTransformer().clientToWorld(
							pinMark.getX(), pinMark.getY(), coord);

					float x = coord.mX * mScale;
					float y = coord.mY * mScale;
					mStatus.setText(String.format("x: %.3f   y: %.3f", x, y));
					Coord c = new Coord();
					mapWidget.getCoordTransformer().clientToWorld(temppoi.x,
							temppoi.y, c);
					DTLog.e("mBeacon.x1:" + mBeaconInfo.getX() + "   "
							+ mBeaconInfo.getY());
					mBeaconInfo.setX((int) (c.mX * mScale * 1000));
					mBeaconInfo.setY((int) (c.mY * mScale * 1000));
					DTLog.e("mBeacon.x1:" + mBeaconInfo.getX() + "   "
							+ mBeaconInfo.getY());
					if (mBeaconInfo.getEdit_status() != 2)// 属于新建
						mBeaconInfo.setEdit_status(3);

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

	private RMLocation mLocation;

	@Override
	public void moveCenter() {
		super.moveCenter();
		if (mLocation != null && mLocation.getError() == 0) {
			Point p = new Point();
			mapWidget.getCoordTransformer().worldToClient(
					mLocation.getX() / mFloor.getScale(),
					Math.abs(mLocation.getY() / mFloor.getScale()), p);
			mapWidget.getCoordTransformer().bitmapTranslateToCenter(p.x, p.y);
			if (!mLocation.getFloor().equals(mFloor.getFloor())
					|| !mLocation.getBuildID().equals(mFloor.getBuildId()))
				DTUIUtils.showToastSafe("楼层不一致");
		} else {
			DTUIUtils.showToastSafe("无法定位");
		}
	}

	@Override
	public void onReceiveLocation(RMLocation location) {
		mLocation = location;
		String scanner = LocationApp.getInstance().getScannerInfo();
		if (isBeaconStart && !DTStringUtils.isEmpty(scanner)
				&& scanner.contains("<beacons>")) {
			new LCAsyncTask(new CheckBeaconCall())
					.runOnExecutor(false, scanner);
		}
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
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			// exportBeaconInfo();
		}
	}

}
