package com.rtmap.locationcheck.pageNew;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtm.location.LocationApp;
import com.rtm.location.entity.WifiEntity;
import com.rtm.location.utils.RMLocationListener;
import com.rtmap.locationcheck.R;
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
import com.rtmap.locationcheck.core.model.LCRouteMap;
import com.rtmap.locationcheck.layer.LocationBeaconLayer;
import com.rtmap.locationcheck.layer.OnBeaconClickListener;
import com.rtmap.locationcheck.layer.RouteLayer;
import com.rtmap.locationcheck.util.CollectAppendInfo;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTIOUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;

public class LCTerminalRouteActivity extends LCActivity implements
		OnClickListener, OnBeaconClickListener, OnItemClickListener,
		RMLocationListener {

	private Floor mFloor;
	private MapView mMapView;
	private TextView mTitle;// 标题
	private RouteLayer mRouteLayer;
	private RouteLayer mLocationRouteLayer;
	private Dialog mMenuDialog;
	private LocationBeaconLayer mBeaconLayer;// beacon
	private TextView mStatus, mStart;// 状态，开始按钮
	private Button mMark;// 标记
	private long startTime, endTime;
	private HashMap<String, BeaconInfo> mBeaconMap;
	private boolean mSign;// 是否是定位开始的第一次

	private CollectAppendInfo appendInfo;
	private WifiManager mWifiManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_map);
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mFloor = (Floor) getIntent().getExtras().getSerializable("floor");
		mTitle = (TextView) findViewById(R.id.title);
		mTitle.setText(mFloor.getName() + "-" + mFloor.getFloor());
		mStart = (TextView) findViewById(R.id.start);
		mStart.setVisibility(View.VISIBLE);
		findViewById(R.id.menu).setOnClickListener(this);
		mStatus = (TextView) findViewById(R.id.status);// 标记
		mMark = (Button) findViewById(R.id.mark);

		mStatus.setText("点击开始，标记路线");
		mMark.setOnClickListener(this);
		mStart.setOnClickListener(this);

		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		mMapView.removeRefreshRunnable();
		mMapView.setUpdateMap(false);

		// 初始化起终点
		Drawable black = getResources().getDrawable(R.drawable.sign_black);
		// 初始化起终点
		Drawable purple = getResources().getDrawable(R.drawable.sign_purple);
		mRouteLayer = new RouteLayer(mMapView,
				DTIOUtils.drawableToBitmap(purple),
				DTIOUtils.drawableToBitmap(purple),
				DTIOUtils.drawableToBitmap(purple));
		mLocationRouteLayer = new RouteLayer(mMapView,
				DTIOUtils.drawableToBitmap(black),
				DTIOUtils.drawableToBitmap(black),
				DTIOUtils.drawableToBitmap(black));
		mMapView.addMapLayer(mRouteLayer);
		mMapView.addMapLayer(mLocationRouteLayer);

		Drawable green = getResources().getDrawable(R.drawable.sign_green);
		Drawable red = getResources().getDrawable(R.drawable.sign_red);
		// 编辑状态：0正常，1删除，2新建，3修改
		// 工作状态：0正常，-1低电量，-2故障，-3缺失，-4未知
		HashMap<Integer, Bitmap> bmpMap = new HashMap<Integer, Bitmap>();
		bmpMap.put(0, DTIOUtils.drawableToBitmap(red));
		bmpMap.put(1, DTIOUtils.drawableToBitmap(green));

		mBeaconLayer = new LocationBeaconLayer(mMapView, bmpMap);
		mBeaconLayer.setNameVisibility(true);

		mMapView.addMapLayer(mBeaconLayer);
		CompassLayer mCompassLayer = new CompassLayer(mMapView);// 指南针图层
		mMapView.addMapLayer(mCompassLayer);

		mRouteLayer.setOnPointClickListener(this);
		mLocationRouteLayer.setOnPointClickListener(this);

		mBeaconMap = new HashMap<String, BeaconInfo>();
		initControl();
		initLocation();
		// LocationApp.getInstance().setRequestSpanTime(1000);
		startLocation();
		importLocationHistory();
		initMenuDialog();
		importBeaconHistory(false);
		appendInfo = new CollectAppendInfo();
		appendInfo.deviceKind = android.os.Build.MODEL;
		appendInfo.floorid = mFloor.getBuildId();
		mMapView.initMapConfig(mFloor.getBuildId(), mFloor.getFloor());// 打开地图（建筑物id，楼层id）
	}

	/**
	 * 导入beacon数据
	 */
	private void importBeaconHistory(final boolean isClearSave) {
		new LCAsyncTask(new LCCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				String filepath = DTFileUtils.getDataDir()
						+ mFloor.getBuildId() + "_" + mFloor.getFloor()
						+ ".txt";
				File file = new File(filepath);
				if (!file.exists())
					return null;
				try {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(new FileInputStream(file),
									"utf-8"));
					String line = "";
					StringBuilder result = new StringBuilder();
					while ((line = br.readLine()) != null) {
						// 将文本打印到控制台
						// result += line;
						result.append(line);
					}
					br.close();
					if (!DTStringUtils.isEmpty(result.toString())) {
						Gson gson = new Gson();
						BeaconList list = gson.fromJson(result.toString(),
								BeaconList.class);
						if (list.getList() != null && isClearSave) {// 如果是下载，需要将所有数据变成-4未知
							for (int i = 0; i < list.getList().size(); i++) {
								list.getList().get(i).setWork_status(-4);
							}
							BufferedWriter bw = new BufferedWriter(
									new OutputStreamWriter(
											new FileOutputStream(file), "utf-8"));
							String str = gson.toJson(list);
							DTLog.e("result : " + str);
							bw.write(str);
							bw.flush();
							bw.close();
						}
						return list;
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
				if (obj != null) {
					BeaconList list = (BeaconList) obj;
					mBeaconLayer.clearAllPoints();
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
							info.setName("");
							info.setWork_status(-4);
							mBeaconMap.put(info.getMac(), info);
							mBeaconLayer.addPoint(info);
							if (info.getMaclist() != null
									&& info.getMaclist().size() > 0) {
								for (int i = 0; i < info.getMaclist().size(); i++) {
									mBeaconMap.put(info.getMaclist().get(i)
											.getMac(), info);
								}
							}
						}
						mMapView.refreshMap();
					}
				}
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
	 * 初始化弹出框
	 */
	private void initMenuDialog() {
		mMenuDialog = new Dialog(this, R.style.dialog);
		mMenuDialog.setContentView(R.layout.dialog_map_layout);
		mMenuDialog.setCanceledOnTouchOutside(true);
		ListView mInterList = (ListView) mMenuDialog
				.findViewById(R.id.set_list);
		String[] interDate = getResources().getStringArray(R.array.map_menu);
		mInterList.setAdapter(new LCMapDialogAdapter(this, interDate));
		mInterList.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long arg3) {
		switch (position) {
		case 0:
			new LCAsyncTask(new BeaconDownLoadCall()).run();
			break;
		case 1:
			mBeaconLayer.setPointVisibility(true);
			mMapView.refreshMap();
			break;
		case 2:
			mBeaconLayer.setPointVisibility(false);
			mMapView.refreshMap();
			break;
		}
		mMenuDialog.cancel();
	}

	/**
	 * 初始化定位
	 */
	private void startLocation() {
		LocationApp.getInstance().registerLocationListener(this);
		LocationApp.getInstance().start();// 开始定位
	}

	/**
	 * 加载路线历史
	 */
	private void importLocationHistory() {
		new LCAsyncTask(new LCCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				String path = DTFileUtils.getDataDir() + mFloor.getBuildId()
						+ "_" + mFloor.getFloor() + ".road";
				DTLog.e(".road path : " + path);
				LCRouteMap map = readFile(path);
				if (map != null) {
					mRouteLayer.setRouteMap(map.getRouteMap());
				}
				String p = DTFileUtils.getDataDir() + mFloor.getBuildId() + "_"
						+ mFloor.getFloor() + ".locroad";
				DTLog.e(".road path : " + p);
				LCRouteMap m = readFile(p);
				if (m != null) {
					mLocationRouteLayer.setRouteMap(m.getRouteMap());
				}
				return null;
			}

			@Override
			public void onCallBackFinish(Object obj) {
			}
		}).run();
		mMapView.refreshMap();
	}

	/**
	 * 读取地图文件
	 * 
	 * @param path
	 * @return
	 */
	private LCRouteMap readFile(String path) {

		File file = new File(path);
		if (!file.exists())
			return null;
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), "utf-8"));
			String line, result = "";
			while ((line = br.readLine()) != null) {
				// 将文本打印到控制台
				result += line;
			}
			br.close();
			if (result != null && !"".equals(result)) {
				Gson gson = new Gson();
				LCRouteMap map = gson.fromJson(result, LCRouteMap.class);// 得到线
				return map;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private ArrayList<LCPoint> mRouteList;
	private ArrayList<LCPoint> mLocationRouteList;
	private String mKey;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.menu:
			mMenuDialog.show();
			break;
		case R.id.mark:// 标记
			if (mStart.getText().toString().equals(getString(R.string.end)))// 如果是停止
				mark();
			break;
		case R.id.start:
			if (mStart.getText().toString().equals(getString(R.string.start))) {// 如果是开始
				mRouteList = new ArrayList<LCPoint>();
				mLocationRouteList = new ArrayList<LCPoint>();
				Date date = new Date(System.currentTimeMillis());
				SimpleDateFormat format = new SimpleDateFormat(
						"yyyyMMdd-HHmmss");
				mKey = format.format(date).replaceAll("-", "T");
				mLocationRouteLayer.addRoute(mKey, mLocationRouteList);
				mRouteLayer.addRoute(mKey, mRouteList);// 新添加一条路线
				mark();// 开始标记第一个点
				startTime = System.currentTimeMillis();
				mStatus.setText("开始记录定位数据");
				String filename = mKey + "_" + mFloor.getBuildId() + "_"
						+ DTStringUtils.floorTransform(mFloor.getFloor()) + "_"
						+ LCApplication.MAC;
				mLcrptFile = DTFileUtils.getDataDir() + filename + ".lcrpt1";
				mOffFile = DTFileUtils.getDataDir() + filename + ".off";
				String walkPath = DTFileUtils.getWifiPickerPath() + USER_NAME
						+ File.separator + mFloor.getBuildId() + File.separator;
				DTFileUtils.createDirs(walkPath);
				mWalkFile = walkPath + mFloor.getBuildId() + "-"
						+ mFloor.getFloor() + "-0_" + mKey + ".walk1";
				mSensorFile = walkPath + mFloor.getBuildId() + "-"
						+ mFloor.getFloor() + "-0_" + mKey + ".sensor";

				mSign = true;
				mStart.setText(R.string.end);
			} else {
				mStart.setText(R.string.start);
				// 20140821T131201-2345-6432_860100010040300001_20040_BBBBBBBBBB20.lcrpt1
			}
			break;
		}
	}

	/**
	 * 标记
	 */
	private void mark() {
		float x = mMapView.getCenter().getX();
		float y = mMapView.getCenter().getY();
		LCPoint mPoint = new LCPoint();
		mPoint.setX((int) (x * 1000));
		mPoint.setY((int) (y * 1000));
		mRouteList.add(mPoint);
		exportRoadFile();
		mMapView.refreshMap();
	}

	@Override
	protected void onDestroy() {
		LocationApp.getInstance().unRegisterLocationListener(this);
		LocationApp.getInstance().stop();
		super.onDestroy();
	}

	@Override
	public void onBeaconClick(LCPoint point, final String key) {
		if (!DTStringUtils.isEmpty(key)
				&& mStart.getText().toString()
						.equals(getString(R.string.start))) {
			AlertDialog.Builder builder = new Builder(
					LCTerminalRouteActivity.this);
			builder.setMessage("确认删除吗？");
			builder.setTitle("提示");
			builder.setPositiveButton("确认",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							mRouteLayer.removeRoute(key);
							mLocationRouteLayer.removeRoute(key);
							DTFileUtils.deleteFile(DTFileUtils.getDataDir()
									+ key
									+ "_"
									+ mFloor.getBuildId()
									+ "_"
									+ DTStringUtils.floorTransform(mFloor
											.getFloor()) + "_"
									+ LCApplication.MAC + ".lcrpt1");
							DTFileUtils.deleteFile(DTFileUtils.getDataDir()
									+ key
									+ "_"
									+ mFloor.getBuildId()
									+ "_"
									+ DTStringUtils.floorTransform(mFloor
											.getFloor()) + "_"
									+ LCApplication.MAC + ".off");
							String walkPath = DTFileUtils.getWifiPickerPath()
									+ USER_NAME + File.separator
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
							mMapView.refreshMap();
						}
					});
			builder.setNegativeButton("取消", null);
			builder.create().show();
		}
	}

	// /**
	// * 导出文件
	// *
	// * @author dingtao
	// *
	// */
	// class ExportFile implements LCCallBack {
	//
	// @Override
	// public Object onCallBackStart(Object... obj) {
	// try {
	// String filename = mKey + "_" + mFloor.getBuildId() + "_"
	// + DTStringUtils.floorTransform(mFloor.getFloor()) + "_"
	// + LCApplication.MAC;
	// File file = new File(DTFileUtils.getDataDir() + filename
	// + ".lcrpt1");
	// if (!file.exists())
	// file.createNewFile();
	// BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
	// new FileOutputStream(file), DTStringUtils.UTF_8));
	// bw.write(mLocResult);
	// bw.flush();
	// bw.close();
	//
	// File off = new File(DTFileUtils.getDataDir() + filename
	// + ".off");
	// off.createNewFile();
	// BufferedWriter offbw = new BufferedWriter(
	// new OutputStreamWriter(new FileOutputStream(off),
	// DTStringUtils.UTF_8));
	// offbw.write(mLocResoures);
	// offbw.flush();
	// offbw.close();
	//
	// exportRoadFile();// 导出路线文件
	//
	// DTUIUtils.showToastSafe("保存成功");
	// return "";
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return null;
	// }
	//
	// @Override
	// public void onCallBackFinish(Object obj) {
	// mLocResult = "";
	// mLocResoures = "";
	// mLoadDialog.cancel();
	// }
	// }

	/**
	 * 导出路线文件
	 */
	private void exportRoadFile() {
		LCRouteMap map = new LCRouteMap();
		map.setRouteMap(mRouteLayer.getRouteMap());
		String path = DTFileUtils.getDataDir() + mFloor.getBuildId() + "_"
				+ mFloor.getFloor() + ".road";
		writeFile(map, path);
		String p = DTFileUtils.getDataDir() + mFloor.getBuildId() + "_"
				+ mFloor.getFloor() + ".locroad";
		LCRouteMap m = new LCRouteMap();
		m.setRouteMap(mLocationRouteLayer.getRouteMap());
		writeFile(m, p);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (event.getAction() != KeyEvent.ACTION_DOWN) {
			return false;
		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			mLocationRouteLayer.setDraw(true);
			mMapView.refreshMap();
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			mLocationRouteLayer.setDraw(false);
			mMapView.refreshMap();
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

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
			roadbw.write(new Gson().toJson(map));
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
			result = result.substring(
					result.indexOf("<beacons>") + "<beacons>".length(),
					result.indexOf("</beacons>"));
			String[] b = result.split("#");
			for (int i = 0; i < mBeaconLayer.getPointCount(); i++) {
				mBeaconLayer.getPoint(i).setName("");
				mBeaconLayer.getPoint(i).setWork_status(-4);
			}
			for (int i = 0; i < b.length; i++) {
				if (DTStringUtils.isEmpty(b[i]))
					continue;
				String mac = b[i].substring(0, b[i].indexOf("$")).toUpperCase();
				String rssivalue = b[i].substring(b[i].indexOf("$") + 1);
				if (mBeaconMap.containsKey(mac)) {
					BeaconInfo info = mBeaconMap.get(mac);
					info.setName(rssivalue);// 设置名字
					info.setWork_status(0);
				}
			}
			mMapView.refreshMap();
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

	@Override
	public void onReceiveLocation(final RMLocation result) {
		// TODO Auto-generated method stub
		result.coordY = Math.abs(result.getCoordY());
		if (result.getError() == 0) {
			mMapView.setMyCurrentLocation(result);
		}
		final String scannerinfo = LocationApp.getInstance().getScannerInfo();
		if (scannerinfo == null)
			return;
		if (!DTStringUtils.isEmpty(scannerinfo)
				&& scannerinfo.contains("<beacons>")) {
			new LCAsyncTask(new CheckBeaconCall()).run(scannerinfo);
		}

		if (mStart.getText().toString().equals(getString(R.string.end))) {// 如果是停止，说明开始了
			RMAsyncTask.EXECUTOR.execute(new Runnable() {

				@Override
				public void run() {
					LCPoint mPoint = mRouteList.get(mRouteList.size() - 1);// 最后一个点
					String scanner = scannerinfo.replaceFirst(
							"<Locating>",
							"<Locating><build>"
									+ mFloor.getBuildId()
									+ "</build><floor>"
									+ DTStringUtils.floorTransform(mFloor
											.getFloor()) + "</floor><x>"
									+ mPoint.getX() + "</x><y>" + mPoint.getY()
									+ "</y>")
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
							else {
								LCPoint point = new LCPoint();
								point.setX(result.getCoordX());
								point.setY(result.getCoordY());
								mLocationRouteList.add(point);
								exportRoadFile();
							}
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
									+ wifidata + "</sample><point>");
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
						sensorStr.append(pre + "</point>");
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
		mStatus.setText("定位码：" + result.getError());
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
