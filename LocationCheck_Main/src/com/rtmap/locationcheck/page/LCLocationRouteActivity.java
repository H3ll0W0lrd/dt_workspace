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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtm.location.LocationApp;
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
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTIOUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;

public class LCLocationRouteActivity extends LCActivity implements
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_map);
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
				return loadBeaconData(filepath);
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
	}

	@Override
	protected void onResume() {
		super.onResume();
		LocationApp.getInstance().start();// 开始定位
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocationApp.getInstance().stop();
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
		super.onDestroy();
		LocationApp.getInstance().unRegisterLocationListener(this);
		mMapView.clearMapLayer();
		mBeaconMap.clear();
	}

	@Override
	public void onBeaconClick(LCPoint point, final String key) {
		if (!DTStringUtils.isEmpty(key)
				&& mStart.getText().toString()
						.equals(getString(R.string.start))) {
			AlertDialog.Builder builder = new Builder(
					LCLocationRouteActivity.this);
			builder.setMessage("确认删除吗？");
			builder.setTitle("提示");
			builder.setPositiveButton("确认",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							mRouteLayer.removeRoute(key);
							mLocationRouteLayer.removeRoute(key);
							File file = new File(DTFileUtils.getDataDir()
									+ key
									+ "_"
									+ mFloor.getBuildId()
									+ "_"
									+ DTStringUtils.floorTransform(mFloor
											.getFloor()) + "_"
									+ LCApplication.MAC + ".lcrpt1");
							file.delete();
							File offile = new File(DTFileUtils.getDataDir()
									+ key
									+ "_"
									+ mFloor.getBuildId()
									+ "_"
									+ DTStringUtils.floorTransform(mFloor
											.getFloor()) + "_"
									+ LCApplication.MAC + ".off");
							offile.delete();
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

	private String mLcrptFile;
	private String mOffFile;

	@Override
	public void onReceiveLocation(RMLocation result) {
		// TODO Auto-generated method stub
		result.coordY = Math.abs(result.getCoordY());
		if (result.getError() == 0) {
			mMapView.setMyCurrentLocation(result);
		}
		String scanner = LocationApp.getInstance().getScannerInfo();
		if (scanner == null)
			return;
		if (!DTStringUtils.isEmpty(scanner) && scanner.contains("<beacons>")) {
			new LCAsyncTask(new CheckBeaconCall()).run(scanner);
		}
		if (mStart.getText().toString().equals(getString(R.string.end))) {// 如果是停止，说明开始了
			LCPoint mPoint = mRouteList.get(mRouteList.size() - 1);// 最后一个点
			scanner = scanner.replaceFirst(
					"<Locating>",
					"<Locating><build>" + mFloor.getBuildId()
							+ "</build><floor>"
							+ DTStringUtils.floorTransform(mFloor.getFloor())
							+ "</floor><x>" + mPoint.getX() + "</x><y>"
							+ mPoint.getY() + "</y>")
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
			String locResult = "\n" + endTime + "\t" + mFloor.getBuildId()
					+ "\t" + DTStringUtils.floorTransform(mFloor.getFloor())
					+ "\t" + mPoint.getX() + "\t" + mPoint.getY() + "\t"
					+ result.getBuildID() + "\t" + result.getFloorID() + "\t"
					+ result.getCoordX() + "\t" + result.getCoordY() + "\t"
					+ offset + "\t" + error + "\t" + (endTime - startTime)
					+ "\n";
			startTime = System.currentTimeMillis();
			if (mSign) {
				mSign = !mSign;
				DTFileUtils
						.fstream(mLcrptFile,
								"time\tbuild\tfloor\tx\ty\trbuild\trfloor\trx\try\toffset\terr\tdelay\n");
			}
			DTFileUtils.fstream(mLcrptFile, locResult);
			DTFileUtils.fstream(mOffFile, scanner);
		}
		mStatus.setText("定位码：" + result.getError());
	}

}
