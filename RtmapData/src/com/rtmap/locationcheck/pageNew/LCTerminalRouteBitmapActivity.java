package com.rtmap.locationcheck.pageNew;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.j256.ormlite.dao.Dao;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.utils.RMathUtils;
import com.rtm.location.LocationApp;
import com.rtm.location.entity.GpsEntity;
import com.rtm.location.utils.RMLocationListener;
import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.LCAsyncTask;
import com.rtmap.locationcheck.core.LCCallBack;
import com.rtmap.locationcheck.core.LCSqlite;
import com.rtmap.locationcheck.core.http.LCHttpClient;
import com.rtmap.locationcheck.core.http.LCHttpUrl;
import com.rtmap.locationcheck.core.model.BeaconInfo;
import com.rtmap.locationcheck.core.model.BeaconList;
import com.rtmap.locationcheck.core.model.BroadcastInfo;
import com.rtmap.locationcheck.core.model.CellInfo;
import com.rtmap.locationcheck.core.model.Floor;
import com.rtmap.locationcheck.core.model.LCPick;
import com.rtmap.locationcheck.core.model.LCPickList;
import com.rtmap.locationcheck.core.model.LCPoint;
import com.rtmap.locationcheck.core.model.LCRouteMap;
import com.rtmap.locationcheck.layer.BitmapBeaconLayer;
import com.rtmap.locationcheck.layer.BitmapRouteLayer;
import com.rtmap.locationcheck.layer.OnBeaconClickListener;
import com.rtmap.locationcheck.util.CellUtils;
import com.rtmap.locationcheck.util.CollectAppendInfo;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTIOUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTMathUtils;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;
import com.rtmap.locationcheck.util.map.Coord;
import com.rtmap.locationcheck.util.map.MapWidget;
import com.rtmap.locationcheck.util.map.MapWidget.OnMapTouchListener;
import com.rtmap.locationcheck.util.map.MapWidget.OnMouseListener;
import com.rtmap.locationcheck.util.map.MapWidget.WidgetStateListener;
import com.rtmap.locationcheck.util.map.PinMark;

public class LCTerminalRouteBitmapActivity extends LCActivity implements
		OnClickListener, OnBeaconClickListener, RMLocationListener {

	private Floor mFloor;
	private MapWidget mMapView;
	private BitmapRouteLayer mRouteLayer;
	private TextView mStatus;// 状态，开始按钮
	private Button mMark, mStart;// 标记
	private long startTime, endTime;
	private HashMap<String, BeaconInfo> mBeaconMap;
	private boolean mSign;// 是否是定位开始的第一次

	private CollectAppendInfo appendInfo;
	private WifiManager mWifiManager;
	private PinMark pinMark;
	private float mScale;
	LCPoint clickPoint = null;
	String key = null;
	private String WALK_FILE;
	private long time;
	private LCPickList mPickList;
	private Dao<BeaconInfo, String> mBeaconDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_map_bitmap);
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mFloor = (Floor) getIntent().getExtras().getSerializable("floor");
		mScale = mFloor.getScale();
		
		mBeaconDao = LCSqlite.getInstance().createBeaconTable(
				mFloor.getBuildId(), mFloor.getFloor());
		
		mStart = (Button) findViewById(R.id.start);
		mStart.setVisibility(View.VISIBLE);

		String[] mapFiles = DTFileUtils.listFiles(DTFileUtils.getDataDir()
				+ mFloor.getBuildId() + File.separator, new FilenameFilter() {
			// 860100010040500002-F2*.*
			@Override
			public boolean accept(File dir, String filename) {
				if (filename.contains(mFloor.getBuildId() + "-"
						+ mFloor.getFloor() + "_")
						&& filename.endsWith(".json")) {
					return true;
				}
				return false;
			}
		});
		if (mapFiles != null && mapFiles.length > 0) {
			WALK_FILE = DTFileUtils.getDataDir() + mFloor.getBuildId()
					+ File.separator + mapFiles[0];//
			DTFileUtils.createDirs(DTFileUtils.getDataDir()
					+ mFloor.getBuildId() + File.separator);
			importPickHistory();
		} else {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss");
			String time = format.format(new Date(System.currentTimeMillis()))
					.replaceAll("-", "T");
			WALK_FILE = DTFileUtils.getDataDir() + mFloor.getBuildId()
					+ File.separator + mFloor.getBuildId() + "-"
					+ mFloor.getFloor() + "_" + USER_NAME + "_" + time
					+ ".json";
			mPickList = new LCPickList();
			mPickList.setList(new ArrayList<LCPick>());
		}

		initTitleBar(2);

		mStatus = (TextView) findViewById(R.id.status);// 标记
		mMark = (Button) findViewById(R.id.mark);
		mMark.setVisibility(View.INVISIBLE);
		mMark.setOnClickListener(this);
		mStart.setOnClickListener(this);

		mMapView = (MapWidget) findViewById(R.id.map_view);

		mMapView.registerWidgetStateListener(new WidgetStateListener() {
			@Override
			public void onMapWidgetCreated(MapWidget map) {
				String bitmap_path = DTFileUtils.getImageDir()
						+ mFloor.getBuildId() + "-" + mFloor.getFloor()
						+ ".jpg";
				mMapView.openMapFile(bitmap_path);
			}
		});
		mMapView.setOnMapTouchListener(new OnMapTouchListener() {

			@Override
			public void onMapTouch(MotionEvent event) {
				final Coord coord = new Coord();
				mMapView.getCoordTransformer().clientToWorld(pinMark.getX(),
						pinMark.getY(), coord);

				float x = coord.mX * mScale;
				float y = coord.mY * mScale;
				mStatus.setText(String.format("x: %.3f   y: %.3f", x, y));
			}
		});
		// 初始标记点设置，图形、可见、居中
		pinMark = new PinMark(mMapView, R.drawable.pin48);
		pinMark.setVisiable(true);
		pinMark.setLocation(getResources().getDisplayMetrics().widthPixels / 2,
				getResources().getDisplayMetrics().heightPixels / 2);
		// 添加标记点图层及采集点图层
		mMapView.addMark(pinMark);
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

		Drawable click = getResources().getDrawable(R.drawable.location_icon);
		// mMapView.addMark(mBeaconLayer);
		mMapView.registerMouseListener(new OnMouseListener() {

			@Override
			public void onSingleTap(MapWidget mw, float x, float y) {
				Point temppoi = new Point();
				clickPoint = null;
				key = null;
				float p2p = -1;// 两个点之间的距离
				HashMap<String, ArrayList<LCPoint>> mRouteMap = mRouteLayer
						.getRouteMap();
				Iterator<String> keySet = mRouteMap.keySet().iterator();
				while (keySet.hasNext()) {
					String str = keySet.next();
					ArrayList<LCPoint> points = mRouteMap.get(str);
					for (int i = 0; i < points.size(); i++) {
						LCPoint p = points.get(i);

						mMapView.getCoordTransformer().worldToClient(
								p.getX() / mFloor.getScale() / 1000,
								p.getY() / mFloor.getScale() / 1000, temppoi);
						if (temppoi.x < 0 || temppoi.y < 0)// 屏幕外的不用计算
							continue;
						float reduceX = Math.abs(temppoi.x - x);
						float reduceY = Math.abs(temppoi.y - y);
						if (reduceX > 20 || reduceY > 20)// 超出手指同一水平线范围
							continue;
						float dis = DTMathUtils.distance(x, y, temppoi.x,
								temppoi.y);// 计算两点之间的距离
						if (p2p < 0 || p2p > dis) {// 距离比他大
							clickPoint = p;// 保存距离范围内的点
							p2p = dis;
							key = str;
						}
					}
				}
				if (p2p > -1) {// 说明点击在点的范围内
					DTLog.e("点击事件clickPointX : " + clickPoint.getX()
							+ "     Y : " + clickPoint.getY());
					onBeaconClick(clickPoint, key);
				}
			}
		});

		// 初始化起终点
		mRouteLayer = new BitmapRouteLayer(mFloor.getScale() * 1000);
		mMapView.addMark(mRouteLayer);

		mBeaconMap = new HashMap<String, BeaconInfo>();
		initControl();
		initLocation();
		// LocationApp.getInstance().setRequestSpanTime(1000);
		LocationApp.getInstance().registerLocationListener(this);
		importLocationHistory();
		importBeaconHistory(false);
		appendInfo = new CollectAppendInfo();
		appendInfo.deviceKind = android.os.Build.MODEL;
		appendInfo.floorid = mFloor.getBuildId();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (event.getAction() != KeyEvent.ACTION_DOWN) {
			return false;
		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			finish();
			break;
		case KeyEvent.KEYCODE_VOLUME_UP:
			start();
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (!mStart.getText().toString().equals(getString(R.string.start))) {// 如果是开始
				mark();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

	/**
	 * 导入指纹采集历史
	 */
	private void importPickHistory() {
		new LCAsyncTask(new LCCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				String result = DTFileUtils.readFile(WALK_FILE);
				Gson gson = new Gson();
				if (!DTStringUtils.isEmpty(result)) {
					mPickList = gson.fromJson(result, LCPickList.class);
				}
				return null;
			}

			@Override
			public void onCallBackFinish(Object obj) {
			}
		}).run();
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
			}
		}).run();
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
						mMapView.getCoordTransformer().bitmapTranslate(0,
								adjustLength);
						break;
					case R.id.btn_direction_down:
						mMapView.getCoordTransformer().bitmapTranslate(0,
								-adjustLength);
						break;
					case R.id.btn_direction_left:
						mMapView.getCoordTransformer().bitmapTranslate(
								adjustLength, 0);
						break;
					case R.id.btn_direction_right:
						mMapView.getCoordTransformer().bitmapTranslate(
								-adjustLength, 0);
						break;
					}

					final Coord coord = new Coord();
					mMapView.getCoordTransformer().clientToWorld(
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

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		LocationApp.getInstance().start();// 开始定位
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
		LocationApp.getInstance().stop();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	/**
	 * 加载路线历史
	 */
	private void importLocationHistory() {
		new LCAsyncTask(new LCCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				String path = DTFileUtils.getDataDir() + mFloor.getBuildId()
						+ File.separator + mFloor.getBuildId() + "_"
						+ mFloor.getFloor() + ".road";
				DTLog.e(".road path : " + path);
				LCRouteMap map = readFile(path);
				if (map != null) {
					mRouteLayer.addRouteMap(map.getRouteMap());
				}
				return null;
			}

			@Override
			public void onCallBackFinish(Object obj) {
			}
		}).run();
	}

	/**
	 * 读取地图文件
	 * 
	 * @param path
	 * @return
	 */
	private LCRouteMap readFile(String path) {
		String result = DTFileUtils.readFile(path);
		if (!DTStringUtils.isEmpty(result)) {
			Gson gson = new Gson();
			LCRouteMap map = gson.fromJson(result, LCRouteMap.class);// 得到线
			return map;
		}
		return null;
	}

	private ArrayList<LCPoint> mRouteList;
	private String mKey;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mark:// 标记
			if (mStart.getText().toString().equals(getString(R.string.end)))// 如果是停止
				mark();
			break;
		case R.id.start:
			start();
			break;
		}
	}

	private void start() {
		if (mStart.getText().toString().equals(getString(R.string.start))) {// 如果是开始
			mRouteList = new ArrayList<LCPoint>();
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss");
			mKey = format.format(date).replaceAll("-", "T");
			mRouteLayer.addRoute(mKey, mRouteList);// 新添加一条路线
			mark();// 开始标记第一个点
			startTime = System.currentTimeMillis();
			String filename = mKey + "_" + mFloor.getBuildId() + "_"
					+ DTStringUtils.floorTransform(mFloor.getFloor()) + "_"
					+ LCApplication.MAC;
			mLcrptFile = DTFileUtils.getDataDir() + mFloor.getBuildId()
					+ File.separator + filename + ".lcrpt1";
			mOffFile = DTFileUtils.getDataDir() + mFloor.getBuildId()
					+ File.separator + filename + ".off";
			String walkPath = DTFileUtils.getDataDir() + mFloor.getBuildId()
					+ File.separator;
			DTFileUtils.createDirs(walkPath);
			mWalkFile = walkPath + mFloor.getBuildId() + "-"
					+ mFloor.getFloor() + "-0_" + mKey + ".walk1";
			mSensorFile = walkPath + mFloor.getBuildId() + "-"
					+ mFloor.getFloor() + "-0_" + mKey + ".sensor";

			mSign = true;
			isPicking();
			mMark.setVisibility(View.VISIBLE);
			mStart.setText(R.string.end);
		} else {
			mMark.setVisibility(View.INVISIBLE);
			noPicking();
			mStart.setText(R.string.start);
			// 20140821T131201-2345-6432_860100010040300001_20040_BBBBBBBBBB20.lcrpt1
		}
	}

	/**
	 * 标记
	 */
	private void mark() {
		final Coord coord = new Coord();
		mMapView.getCoordTransformer().clientToWorld(pinMark.getX(),
				pinMark.getY(), coord);
		float x = coord.mX * mFloor.getScale();
		float y = coord.mY * mFloor.getScale();
		DTLog.e("X :" + x + "  Y: " + y + " listSize:" + mRouteList.size());
		if (mRouteList.size() > 0) {
			LCPoint p = mRouteList.get(mRouteList.size() - 1);
			if (Math.sqrt(Math.pow(p.getX() / 1000f - x, 2)
					+ Math.pow(p.getY() / 1000f - y, 2)) < PICK_DIATANCE) {// 两点之间距离必须大于0.3米
				DTUIUtils.showToastSafe("两点之间距离必须大于" + PICK_DIATANCE + "米");
				return;
			}
		}
		LCPoint mPoint = new LCPoint();
		mPoint.setX((int) (x * 1000));
		mPoint.setY((int) (y * 1000));
		mRouteList.add(mPoint);
		// 2015-10-30 09:36:25 860100010030100007 20010 10531 9575 0 0 0
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (mRouteList.size() == 1) {
			LCPick pick = new LCPick(format.format(date), mFloor.getBuildId(),
					mFloor.getFloor(), mPoint.getX(), mPoint.getY(), 0, 0, 0);
			time = System.currentTimeMillis();
			mPickList.getList().add(pick);
		} else {
			LCPoint point = mRouteList.get(mRouteList.size() - 2);
			float distance = RMathUtils.distance(point.getX(), point.getY(),
					mPoint.getX(), mPoint.getY());
			double t = (System.currentTimeMillis() - time) / 1000f;
			time = System.currentTimeMillis();
			LCPick pick = new LCPick(format.format(date), mFloor.getBuildId(),
					mFloor.getFloor(), mPoint.getX(), mPoint.getY(), distance
							/ t, t * 1000, distance);
			mPickList.getList().add(pick);
		}
		DTFileUtils.writeFile(mGson.toJson(mPickList), WALK_FILE, false);
		exportRoadFile();
	}

	@Override
	protected void onDestroy() {
		LocationApp.getInstance().unRegisterLocationListener(this);
		mBeaconMap.clear();
		mRouteLayer.clearHistoryCorrectPoints();
		mMapView.onPause();
		super.onDestroy();
	}

	@Override
	public void onBeaconClick(LCPoint point, final String key) {
		if (!DTStringUtils.isEmpty(key)
				&& mStart.getText().toString()
						.equals(getString(R.string.start))) {
			AlertDialog.Builder builder = new Builder(
					LCTerminalRouteBitmapActivity.this);
			builder.setMessage("确认删除吗？");
			builder.setTitle("提示");
			builder.setPositiveButton("确认",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							mRouteLayer.removeRoute(key);
							DTFileUtils.deleteFile(DTFileUtils.getDataDir()
									+ mFloor.getBuildId()
									+ File.separator
									+ key
									+ "_"
									+ mFloor.getBuildId()
									+ "_"
									+ DTStringUtils.floorTransform(mFloor
											.getFloor()) + "_"
									+ LCApplication.MAC + ".lcrpt1");
							DTFileUtils.deleteFile(DTFileUtils.getDataDir()
									+ mFloor.getBuildId()
									+ File.separator
									+ key
									+ "_"
									+ mFloor.getBuildId()
									+ "_"
									+ DTStringUtils.floorTransform(mFloor
											.getFloor()) + "_"
									+ LCApplication.MAC + ".off");
							String walkPath = DTFileUtils.getDataDir()
									+ mFloor.getBuildId() + File.separator;
							DTFileUtils.deleteFile(walkPath
									+ mFloor.getBuildId() + "-"
									+ mFloor.getFloor() + "-0_" + key
									+ ".walk1");
							DTFileUtils.deleteFile(walkPath
									+ mFloor.getBuildId() + "-"
									+ mFloor.getFloor() + "-0_" + key
									+ ".sensor");
							exportRoadFile();// 导出路线数据
						}
					});
			builder.setNegativeButton("取消", null);
			builder.create().show();
		}
	}

	/**
	 * 导出路线文件
	 */
	private void exportRoadFile() {
		LCRouteMap map = new LCRouteMap();
		map.setRouteMap(mRouteLayer.getRouteMap());
		String path = DTFileUtils.getDataDir() + mFloor.getBuildId()
				+ File.separator + mFloor.getBuildId() + "_"
				+ mFloor.getFloor() + ".road";
		writeFile(map, path);
	}

	private Gson mGson = new Gson();

	/**
	 * 写文件
	 * 
	 * @param map
	 */
	private void writeFile(LCRouteMap map, String path) {
		try {
			File roadfile = new File(path);
			if (!roadfile.exists())
				roadfile.createNewFile();
			BufferedWriter roadbw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(roadfile), DTStringUtils.UTF_8));
			roadbw.write(mGson.toJson(map));
			roadbw.flush();
			roadbw.close();
		} catch (IOException e) {
			e.printStackTrace();
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
		}
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
						+ mFloor.getBuildId() + File.separator
						+ mFloor.getBuildId() + "_" + mFloor.getFloor()
						+ ".txt";
				DTFileUtils.zipToFile(zippath, filepath);
				return filepath;
			}
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			if (obj != null) {
				importBeaconHistory(true);
			}
		}
	}

	private String mOffFile, mWalkFile, mLcrptFile, mSensorFile;

	private RMLocation mLocation;

	@Override
	public void moveCenter() {
		super.moveCenter();
		if (mLocation != null && mLocation.getError() == 0) {
			Point p = new Point();
			mMapView.getCoordTransformer().worldToClient(
					mLocation.getX() / mFloor.getScale(),
					Math.abs(mLocation.getY() / mFloor.getScale()), p);
			mMapView.getCoordTransformer().bitmapTranslateToCenter(p.x, p.y);
			if (!mLocation.getFloor().equals(mFloor.getFloor())
					|| !mLocation.getBuildID().equals(mFloor.getBuildId()))
				DTUIUtils.showToastSafe("楼层不一致");
		} else {
			DTUIUtils.showToastSafe("无法定位");
		}
	}

	@Override
	public void onReceiveLocation(final RMLocation result) {
		// TODO Auto-generated method stub
		result.coordY = Math.abs(result.getCoordY());
		mLocation = result;
		final String scannerinfo = LocationApp.getInstance().getScannerInfo();
		if (scannerinfo == null)
			return;
		if (!DTStringUtils.isEmpty(scannerinfo)
				&& scannerinfo.contains("<beacons>")) {
			new LCAsyncTask(new CheckBeaconCall()).runOnExecutor(false,scannerinfo);
		}

		if (mStart.getText().toString().equals(getString(R.string.end))) {// 如果是停止，说明开始了
			RMAsyncTask.EXECUTOR.execute(new Runnable() {

				@Override
				public void run() {
					LCPoint mPoint = mRouteList.get(mRouteList.size() - 1);// 最后一个点
					android.location.Location location = GpsEntity
							.getInstance().getLocation();
					StringBuilder gps = new StringBuilder();
					gps.append("<gps84>");
					if (location != null) {
						gps.append(location.getLongitude() + "\\$"
								+ location.getLatitude() + "\\$"
								+ location.getAccuracy());
					} else {
						gps.append("0\\$0\\$0");
					}
					gps.append("</gps84>");
					ArrayList<CellInfo> cellList = CellUtils
							.init(getApplicationContext());
					if (cellList != null && cellList.size() > 0) {
						gps.append("<gsm>");
						// <gsm>#MCC$MNC$LAC$CID$BSSS#MCC$MNC2$LAC2$CID2$BSSS2...</gsm>
						for (int i = 0; i < cellList.size(); i++) {
							CellInfo info = cellList.get(i);
							gps.append("#" + info.getMobileCountryCode() + "\\$"
									+ info.getMobileNetworkCode() + "\\$"
									+ info.getLocationAreaCode() + "\\$"
									+ info.getCellId() + "\\$" + info.getRssi());
						}
						gps.append("</gsm>");
					}
					Log.i("rtmap", gps.toString());
					String scanner = scannerinfo.replaceFirst(
							"<Locating>",
							"<Locating><build>"
									+ mFloor.getBuildId()
									+ "</build><floor>"
									+ DTStringUtils.floorTransform(mFloor
											.getFloor()) + "</floor><x>"
									+ mPoint.getX() + "</x><y>" + mPoint.getY()
									+ "</y>" + gps.toString())
							+ "\n";
					// time build floor x y rbuild rfloor rx ry offset err delay
					long offx = result.getCoordX() - mPoint.getX();
					long offy = result.getCoordY() - mPoint.getY();
					long offset = (long) Math.sqrt(offx * offx + offy * offy);
					int error = result.getError();
					if (error == 0) {
						if (mFloor.getBuildId().equals(result.getBuildID())) {
							if (DTStringUtils.floorTransform(mFloor.getFloor()) != result
									.getFloorID())
								error = 100002;
						} else {
							error = 100001;
						}
					}
					endTime = System.currentTimeMillis();
					String locResult = "\n" + endTime + "\t"
							+ mFloor.getBuildId() + "\t"
							+ DTStringUtils.floorTransform(mFloor.getFloor())
							+ "\t" + mPoint.getX() + "\t" + mPoint.getY()
							+ "\t" + result.getBuildID() + "\t"
							+ result.getFloorID() + "\t" + result.getCoordX()
							+ "\t" + result.getCoordY() + "\t" + offset + "\t"
							+ error + "\t" + (endTime - startTime) + "\n";
					StringBuilder walkStr = new StringBuilder();
					StringBuilder sensorStr = new StringBuilder();
					String head = "<point><pid>"
							+ String.format("%05d%05d%05d", DTStringUtils
									.floorTransform(mFloor.getFloor()), mPoint
									.getX() / 1000, mPoint.getY() / 1000)
							+ "</pid><coord>" + mPoint.getX() + ","
							+ mPoint.getY() + "</coord><count>1</count>";
					walkStr.append(head);
					sensorStr.append(head);
					boolean isOpen = mWifiManager.isWifiEnabled();
					if (mWifiManager != null && isOpen) {
						mWifiManager.startScan();
						List<ScanResult> lsScanResult = mWifiManager
								.getScanResults();
						String wifidata = getWifiData(lsScanResult);
						if (wifidata != null) {
							walkStr.append("<sample time="
									+ System.currentTimeMillis() + ">"
									+ wifidata + "</sample></point>\n");
						}
					}
					if (scanner.contains("<beacons>")) {
						String beacons = scanner.substring(
								scanner.indexOf("<beacons>"),
								scanner.indexOf("</beacons>")
										+ "</beacons>".length());
						sensorStr.append(beacons);
					}
					if (scanner.contains("<acc>")) {
						String acc = scanner.substring(
								scanner.indexOf("<acc>"),
								scanner.indexOf("</acc>") + "</acc>".length());
						sensorStr.append(acc);
					}
					if (scanner.contains("<mag>")) {
						String mag = scanner.substring(
								scanner.indexOf("<mag>"),
								scanner.indexOf("</mag>") + "</mag>".length());
						sensorStr.append(mag);
					}
					if (scanner.contains("<cp>")) {
						String cp = scanner.substring(scanner.indexOf("<cp>"),
								scanner.indexOf("</cp>") + "</cp>".length());
						sensorStr.append(cp);
					}
					if (scanner.contains("<pre>")) {
						String pre = scanner.substring(
								scanner.indexOf("<pre>"),
								scanner.indexOf("</pre>") + "</pre>".length());
						sensorStr.append(pre + "</point>\n");
					}
					startTime = System.currentTimeMillis();
					if (mSign) {
						mSign = !mSign;
						DTFileUtils
								.fstream(mLcrptFile,
										"time\tbuild\tfloor\tx\ty\trbuild\trfloor\trx\try\toffset\terr\tdelay\n");
						DTFileUtils.fstream(mWalkFile, getWalkHead());
						DTFileUtils.fstream(mSensorFile, getWalkHead());
					}

					DTFileUtils.fstream(mWalkFile, walkStr.toString());
					DTFileUtils.fstream(mSensorFile, sensorStr.toString());
					DTFileUtils.fstream(mLcrptFile, locResult);
					DTFileUtils.fstream(mOffFile, scanner);
				}
			});
		}
	}

	private static final int MAX_SSID_LENGTH = 16;
	private static final String spitData = "#", spitSpace = "$", replace = "_";

	/**
	 * 得到采集到的wifi信息</br> 格式： #mac$rssi$ssid$chnl
	 * 
	 * @param lsScanResult
	 * @return
	 */
	protected String getWifiData(List<ScanResult> lsScanResult) {

		if (lsScanResult == null)
			return null;
		// 扫描的得到的ap个数必须要大于0
		if (lsScanResult.size() < 1)
			return null;
		// 记录每一个采集到的信息
		StringBuffer strBuffer = new StringBuffer();
		for (ScanResult result : lsScanResult) {
			int rssi = result.level;
			int Chanel = (Integer.valueOf(result.frequency).intValue() - 2412) / 5 + 1;
			String SSID = result.SSID.trim();
			if (SSID.length() > MAX_SSID_LENGTH) {
				SSID = SSID.substring(0, MAX_SSID_LENGTH);
			}
			String MAC = result.BSSID.replace(":", "").toUpperCase();
			strBuffer.append(spitData + MAC + spitSpace + rssi + spitSpace
					+ SSID + spitSpace + Chanel);
		}
		return strBuffer.toString();
	}

	/**
	 * 保存.wifi的头信息
	 * 
	 * @param fileName
	 *            文件名
	 * @param otherInfo
	 */
	public String getWalkHead() {
		StringBuffer inf = new StringBuffer();
		inf.append(String.format("#%d#\n", appendInfo.secretcod));
		inf.append(String.format("%s=%s\n", "fileVersion",
				appendInfo.fileVersion));
		inf.append(String
				.format("%s=%s\n", "deviceKind", appendInfo.deviceKind));
		inf.append(String.format("%s=%s\n", "deviceId", appendInfo.deviceId));
		inf.append(String.format("%s=%s\n", "mapId", appendInfo.floorid));
		inf.append(String.format("%s=%s\n", "user", USER_NAME));
		inf.append(String.format("%s=%s\n", "refPtLeftUp",
				appendInfo.longitude_1 + "," + appendInfo.latitude_1 + ","
						+ appendInfo.refPlatForm));
		inf.append(String.format("%s=%s\n", "refPtLeftDown",
				appendInfo.longitude_1 + "," + appendInfo.latitude_1 + ","
						+ appendInfo.refPlatForm));
		inf.append(String.format("%s=%s\n", "refCoordLeftDown", appendInfo.x_2
				+ "," + appendInfo.y_2));
		inf.append(String.format("%s=%s\n", "refPtRightDown",
				appendInfo.longitude_2 + "," + appendInfo.latitude_2 + ","
						+ appendInfo.refPlatForm));
		inf.append(String.format("%s=%s\n", "refCoordRightDown",
				+appendInfo.x_3 + "," + appendInfo.y_3));
		return inf.toString();
	}

}
