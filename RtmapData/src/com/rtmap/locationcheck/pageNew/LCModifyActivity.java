package com.rtmap.locationcheck.pageNew;

import java.io.BufferedWriter;
import java.io.File;
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
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.j256.ormlite.dao.Dao;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.MapView.OnMapModeChangedListener;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.PointInfo;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;
import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.LCAsyncTask;
import com.rtmap.locationcheck.core.LCCallBack;
import com.rtmap.locationcheck.core.LCSqlite;
import com.rtmap.locationcheck.core.model.BeaconInfo;
import com.rtmap.locationcheck.core.model.BroadcastInfo;
import com.rtmap.locationcheck.core.model.Floor;
import com.rtmap.locationcheck.core.model.RMLine;
import com.rtmap.locationcheck.core.model.RMPoi;
import com.rtmap.locationcheck.core.model.RMPoiList;
import com.rtmap.locationcheck.layer.OnPointClickListener;
import com.rtmap.locationcheck.layer.RouteTextLayer;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTIOUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;

/**
 * 地图修正采集
 * 
 * @author dingtao
 *
 */
public class LCModifyActivity extends LCActivity implements
		OnPointClickListener, OnClickListener, RMLocationListener {

	private static final int POI_CLASSIFY_GATHER_EXCEPTION_MSG = 1;
	private static final int POI_CLASSIFY_NOT_ADD_MSG = 2;

	private MapView mMapView;// mapView
	private RouteTextLayer mRoutelayer;// 路线
	private ArrayList<RMPoi> mMarkList;// 标记点数组

	private TextView mStatus;
	private Floor mFloor;
	private String mMapName;
	private Dao<BeaconInfo, String> mBeaconDao;

	@Override
	protected void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putSerializable("layer", mRoutelayer.getRouteMap());
		arg0.putSerializable("list", mMarkList);
	}

	private void onReceiver(Bundle arg0) {
		mMarkList = (ArrayList<RMPoi>) arg0.getSerializable("list");
	}


	private static final int IMPORT_HISTORY = 111;// 加载历史
	private String root;
	private Button mDoorBtn, mExportBtn, mDeleteBtn, mCloseBtn;

	private HashMap<String, BeaconInfo> mBeaconMap;// beacon键值对mac值

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case POI_CLASSIFY_GATHER_EXCEPTION_MSG:
				DTUIUtils.showToastSafe("标记失败,请重新标记");
				break;
			case POI_CLASSIFY_NOT_ADD_MSG:
				DTUIUtils.showToastSafe("分类信息未添加");
				break;
			case IMPORT_HISTORY:
				mLoadDialog.show();
				importPointHistory();
				break;
			}
		}

	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_modify);
		mFloor = (Floor) getIntent().getExtras().getSerializable("floor");
		root = DTFileUtils.getDataDir() + mFloor.getBuildId() + File.separator;
		DTFileUtils.createDirs(root);// 创建文件夹
		mBeaconDao = LCSqlite.getInstance().createBeaconTable(
				mFloor.getBuildId(), mFloor.getFloor());
		mMarkList = new ArrayList<RMPoi>();
		mMapName = mFloor.getBuildId() + "-" + mFloor.getFloor();
		init(savedInstanceState);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isOpenLocation)
			LocationApp.getInstance().start();
		else
			DTUIUtils.showToastSafe("定位未开启");
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isOpenLocation)
			LocationApp.getInstance().stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isOpenLocation)
			LocationApp.getInstance().unRegisterLocationListener(this);
		mRoutelayer.destroyLayer();
		mMapView.clearMapLayer();
		mBeaconMap.clear();
	}

	/**
	 * 初始化view
	 */
	private void initView() {
		mExportBtn = (Button) findViewById(R.id.export);
		mExportBtn.setOnClickListener(this);
		mCloseBtn = (Button) findViewById(R.id.close);
		mCloseBtn.setOnClickListener(this);
		mDeleteBtn = (Button) findViewById(R.id.delete);
		mDeleteBtn.setOnClickListener(this);
		findViewById(R.id.mark).setOnClickListener(this);
		mDoorBtn = (Button) findViewById(R.id.door_btn);
		mDoorBtn.setOnClickListener(this);
		findViewById(R.id.poi).setOnClickListener(this);

		initTitleBar(1);

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
					mMapView.setCenter(location.getX(), location.getY());
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

	private void init(Bundle arg0) {
		initView();
		mBeaconMap = new HashMap<String, BeaconInfo>();
		mStatus = (TextView) findViewById(R.id.status);
		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		mMapView.setUpdateMap(false);
		mMapView.setDoubleTapable(false);
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
		Drawable black_gray = getResources().getDrawable(
				R.drawable.sign_poi_gray);
		Bitmap blackBitmap = DTIOUtils.drawableToBitmap(black_gray);
		HashMap<Integer, Bitmap> bmpMap = new HashMap<Integer, Bitmap>();
		bmpMap.put(0, DTIOUtils.drawableToBitmap(green));
		bmpMap.put(1, DTIOUtils.drawableToBitmap(blue));
		bmpMap.put(2, DTIOUtils.drawableToBitmap(black));
		bmpMap.put(3, DTIOUtils.drawableToBitmap(red));
		// mMapView.addMapLayer(mBeaconLayer);
		// 初始化起终点
		Drawable drawable = getResources().getDrawable(
				R.drawable.sign_route_purple);
		Bitmap pointbmp = DTIOUtils.drawableToBitmap(drawable);// 终点图片
		mRoutelayer = new RouteTextLayer(mMapView, pointbmp, pointbmp,
				blackBitmap, blackBitmap);// 路线数组
		mRoutelayer.setOnPointClickListener(this);
		CompassLayer mCompassLayer = new CompassLayer(mMapView);// 指南针图层
		mMapView.addMapLayer(mCompassLayer);
		mMapView.addMapLayer(mRoutelayer);

		if (isOpenLocation)
			initLocation();
		importBeaconHistory(false);
		mMapView.initMapConfig(mFloor.getBuildId(), mFloor.getFloor());// 打开地图（建筑物id，楼层id）
		if (arg0 == null)
			handler.sendEmptyMessageDelayed(IMPORT_HISTORY, 1000);
		else {
			onReceiver(arg0);
			mRoutelayer.setRouteMap((HashMap<String, ArrayList<RMPoi>>) arg0
					.getSerializable("layer"));
		}
		mMapView.refreshMap();
	}

	/**
	 * 导入门数据
	 */
	private void importPointHistory() {
		new LCAsyncTask(new LCCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				mRoutelayer.setDraw(false);
				String[] files_mc = DTFileUtils.listFiles(root,
						new FilenameFilter() {

							@Override
							public boolean accept(File dir, String filename) {
								if (filename.equals(mFloor.getBuildId() + "-"
										+ mFloor.getFloor() + ".door")) {
									return true;
								}
								if (filename.equals(mFloor.getBuildId() + "-"
										+ mFloor.getFloor() + ".door_upload")) {
									return true;
								}
								if (filename.startsWith(mFloor.getBuildId()
										+ "-" + mFloor.getFloor() + "_")
										&& filename.endsWith(".door_upload")) {
									return true;
								}
								if (filename.equals(mFloor.getBuildId() + "-"
										+ mFloor.getFloor() + ".poi")) {
									return true;
								}
								if (filename.startsWith(mFloor.getBuildId()
										+ "-" + mFloor.getFloor() + "_")
										&& filename.endsWith(".poi_upload")) {
									return true;
								}
								return false;
							}
						});
				if (files_mc != null && files_mc.length > 0) {
					for (String path : files_mc) {
						String p = root + path;
						String result = DTFileUtils.readFile(p);
						if (!DTStringUtils.isEmpty(result)) {
							Gson gson = new Gson();
							mRoutelayer.addRoute(p,
									gson.fromJson(result, RMPoiList.class)
											.getPoiList());
						}
					}
				}
				return null;
			}

			@Override
			public void onCallBackFinish(Object obj) {
				mRoutelayer.setDraw(true);
				mMapView.refreshMap();
				importRouteHistory();
			}
		}).run();
	}

	/**
	 * 导出路线历史
	 */
	private void importRouteHistory() {
		new LCAsyncTask(new LCCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				// storage/sdcard0/rtmap/WifiPicker//zizxs/860100010040500002-F2-0_1399275551008_370_437.mc
				mRoutelayer.setDraw(false);
				String[] files_mc = DTFileUtils.listFiles(root,
						new FilenameFilter() {

							@Override
							public boolean accept(File dir, String filename) {
								if (filename.contains(mFloor.getBuildId() + "-"
										+ mFloor.getFloor() + "_")
										&& (filename.endsWith(".mc") || filename
												.endsWith(".mc_upload"))) {
									return true;
								}
								return false;
							}
						});

				if (files_mc != null) {
					for (int i = 0; i < files_mc.length; i++) {
						String path = String.format("%s%s", root, files_mc[i]);
						DTLog.e(".mc path : " + path);
						String result = DTFileUtils.readFile(path);
						if (!DTStringUtils.isEmpty(result)) {
							Gson gson = new Gson();
							RMLine list = gson.fromJson(result, RMLine.class);// 得到线
							mRoutelayer.addRoute(path, list.getPoiList());
						}
					}
				}

				return null;
			}

			@Override
			public void onCallBackFinish(Object obj) {
				mRoutelayer.setDraw(true);
				mMapView.refreshMap();
				mLoadDialog.cancel();
			}
		}).run();

	}

	private final static String ROUTE = "beta_route";

	/**
	 * 采集路网
	 */
	private void mark() {
		float x = mMapView.getCenter().getX();
		float y = mMapView.getCenter().getY();
		DTLog.e("X :" + x + "  Y: " + y + " listSize:" + mMarkList.size());
		if (mMarkList.size() > 0) {
			RMPoi p = mMarkList.get(mMarkList.size() - 1);
			if (Math.sqrt(Math.pow(p.getX() - x, 2) + Math.pow(p.getY() - y, 2)) < PICK_DIATANCE) {// 两点之间距离必须大于0.3米
				DTUIUtils.showToastSafe("两点之间距离必须大于" + PICK_DIATANCE + "米");
				return;
			}
		}
		RMPoi point = new RMPoi();
		point.setX(x);
		point.setY(y);
		point.setName("");
		if (mMarkList.size() == 0) {// 说明新建了一条采集路线
			mRoutelayer.addRoute(ROUTE, mMarkList);
			mExportBtn.setVisibility(View.VISIBLE);
			mDoorBtn.setVisibility(View.GONE);
			mDeleteBtn.setVisibility(View.VISIBLE);
			isPicking();
		}
		mMarkList.add(point);
		if (mMarkList.size() >= 3) {
			mCloseBtn.setVisibility(View.VISIBLE);
		}
		mMapView.refreshMap();
	}

	/**
	 * 添加门
	 */
	private void markDoor() {
		DTLog.e("地图宽度：" + mMapView.getConfig().getBuildWidth() + "     "
				+ mMapView.getConfig().getBuildHeight());
		String filePath = root + mMapName + ".door";
		ArrayList<RMPoi> pois = mRoutelayer.getRoute(filePath);
		RMPoi point = new RMPoi();
		point.setX(mMapView.getCenter().getX());
		point.setY(mMapView.getCenter().getY());
		point.setName("门");
		point.setDesc("门");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = format.format(new Date(System.currentTimeMillis()));
		point.setTime(time);
		DTLog.e("标记点X:" + point.getX() + "     Y:" + point.getY());
		if (pois == null) {
			pois = new ArrayList<RMPoi>();
			mRoutelayer.addRoute(filePath, pois);
		}
		for (int i = 0; i < pois.size(); i++) {
			RMPoi p = pois.get(i);
			if (Math.abs(p.getX() - point.getX()) < 0.1
					&& Math.abs(p.getY() - point.getY()) < 0.1) {
				DTUIUtils.showToastSafe("标记门重复");
				return;
			}
		}
		pois.add(point);
		File file = new File(filePath);
		try {
			if (!file.exists())
				file.createNewFile();
			OutputStreamWriter write = new OutputStreamWriter(
					new FileOutputStream(file), DTStringUtils.UTF_8);
			BufferedWriter bw = new BufferedWriter(write);
			Gson gson = new Gson();
			RMPoiList poilist = new RMPoiList();
			poilist.setPoiList(pois);
			bw.write(gson.toJson(poilist));
			bw.flush();
			bw.close();
		} catch (IOException e) {
			file.delete();
			e.printStackTrace();
		}
		mMapView.refreshMap();
	}

	/**
	 * 添加POI
	 */
	private void markPoi(RMPoi poi) {
		String filePath = root + mMapName + ".poi";
		ArrayList<RMPoi> pois = mRoutelayer.getRoute(filePath);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = format.format(new Date(System.currentTimeMillis()));
		poi.setTime(time);
		DTLog.e("标记点id: " + poi.get_id() + "   X:" + poi.getX() + "     Y:"
				+ poi.getY());
		if (pois == null) {
			pois = new ArrayList<RMPoi>();
			mRoutelayer.addRoute(filePath, pois);
		}
		for(int i=0;i<pois.size();i++){
			if(pois.get(i).get_id()==poi.get_id()){
				pois.remove(i);
				break;
			}
		}
		pois.add(poi);
		File file = new File(filePath);
		try {
			if (!file.exists())
				file.createNewFile();
			OutputStreamWriter write = new OutputStreamWriter(
					new FileOutputStream(file), DTStringUtils.UTF_8);
			BufferedWriter bw = new BufferedWriter(write);
			Gson gson = new Gson();
			RMPoiList poilist = new RMPoiList();
			poilist.setPoiList(pois);
			bw.write(gson.toJson(poilist));
			bw.flush();
			bw.close();
		} catch (IOException e) {
			file.delete();
			e.printStackTrace();
		}
		mMapView.refreshMap();
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
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			mark();
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			deletePoint();
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

	private void saveLine() {
		if (mMarkList.size() > 0) {// 名字或者类别都可以为空
			String startPoint = mMapName + "_" + System.currentTimeMillis()
					+ "_" + (int) (mMarkList.get(0).getX() * 1000) + "_"
					+ (int) (mMarkList.get(0).getY() * 1000);
			// _/storage/sdcard0/rtmap/WifiPicker/zizxs/860100010040500002-F2-0_1399275551008_370_437.mc

			String path = String.format("%s%s.mc", root, startPoint);
			RMLine line = new RMLine();// 新建一条线
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String time = format.format(new Date(System.currentTimeMillis()));
			line.setTime(time);
			ArrayList<RMPoi> list = new ArrayList<RMPoi>();
			list.addAll(mMarkList);
			line.setPoiList(list);
			File file = new File(path);// 准备写入数据
			try {
				if (!file.exists())
					file.createNewFile();
				OutputStreamWriter write = new OutputStreamWriter(
						new FileOutputStream(file), DTStringUtils.UTF_8);
				BufferedWriter bw = new BufferedWriter(write);
				Gson gson = new Gson();
				bw.write(gson.toJson(line));
				bw.flush();
				bw.close();
			} catch (IOException e) {
				file.delete();
				e.printStackTrace();
			}
			mRoutelayer.removeRoute(ROUTE);
			mRoutelayer.addRoute(path, mMarkList);
			mMarkList = new ArrayList<RMPoi>();
			mMapView.refreshMap();
			mExportBtn.setVisibility(View.GONE);
			mCloseBtn.setVisibility(View.GONE);
			mDoorBtn.setVisibility(View.VISIBLE);
			mDeleteBtn.setVisibility(View.GONE);
			noPicking();
		} else {
			Message msg = new Message();
			msg.what = POI_CLASSIFY_NOT_ADD_MSG;
			handler.sendMessage(msg);
		}
	}

	@Override
	public void onClick(final RMPoi point, final String key) {
		if (mDeleteBtn.getVisibility() == View.VISIBLE) {
			return;
		}

		if (key.endsWith(".door")) {
			AlertDialog.Builder build = new Builder(LCModifyActivity.this);
			build.setTitle("提示");
			build.setMessage("确认删除门吗");
			build.setPositiveButton("确认",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							ArrayList<RMPoi> doorList = mRoutelayer
									.getRoute(key);
							doorList.remove(point);
							File file = new File(key);
							if (doorList.size() == 0) {
								file.delete();
							} else {
								try {
									if (!file.exists())
										file.createNewFile();
									OutputStreamWriter write = new OutputStreamWriter(
											new FileOutputStream(file),
											DTStringUtils.UTF_8);
									BufferedWriter bw = new BufferedWriter(
											write);
									Gson gson = new Gson();
									RMPoiList poilist = new RMPoiList();
									poilist.setPoiList(doorList);
									bw.write(gson.toJson(poilist));
									bw.flush();
									bw.close();
								} catch (IOException e) {
									file.delete();
									e.printStackTrace();
								}
							}
							mMapView.refreshMap();
						}
					});
			build.setNegativeButton("取消", null);
			build.create().show();
		} else if (key.endsWith(".mc")) {
			// storage/sdcard0/rtmap/WifiPicker//zizxs/860100010040500002-F2-0_1399275551008_370_437.mc
			AlertDialog.Builder build = new Builder(LCModifyActivity.this);
			build.setTitle("提示");
			build.setMessage("确认删除\"" + point.getName() + "\"吗");
			build.setPositiveButton("确认",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							DTFileUtils.deleteFile(key);
							mRoutelayer.removeRoute(key);
							mMapView.refreshMap();
						}
					});
			build.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
						}
					});
			build.create().show();
		} else if (key.endsWith(".poi")) {
			AlertDialog.Builder build = new Builder(LCModifyActivity.this);
			build.setMessage(point.getName());
			build.setPositiveButton("删除",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							ArrayList<RMPoi> doorList = mRoutelayer
									.getRoute(key);
							doorList.remove(point);
							if (!DTStringUtils.isEmpty(point.getImage())) {
								DTFileUtils.deleteFile(DTFileUtils.getDataDir()
										+ mFloor.getBuildId() + File.separator
										+ point.getImage());
							}
							File file = new File(key);
							if (doorList.size() == 0) {
								file.delete();
							} else {
								try {
									if (!file.exists())
										file.createNewFile();
									OutputStreamWriter write = new OutputStreamWriter(
											new FileOutputStream(file),
											DTStringUtils.UTF_8);
									BufferedWriter bw = new BufferedWriter(
											write);
									Gson gson = new Gson();
									RMPoiList poilist = new RMPoiList();
									poilist.setPoiList(doorList);
									bw.write(gson.toJson(poilist));
									bw.flush();
									bw.close();
								} catch (IOException e) {
									file.delete();
									e.printStackTrace();
								}
							}
							mMapView.refreshMap();
						}
					});
			build.setNegativeButton("修改",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							Intent intent = new Intent(LCModifyActivity.this,
									LCPoiInfoActivity.class);
							Bundle bundle = new Bundle();
							bundle.putSerializable("poi", point);
							bundle.putInt("sign", 1);//修改
							intent.putExtras(bundle);
							startActivityForResult(intent, 500);
						}
					});
			build.create().show();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.export:// 导出数据
			if (mMarkList.size() != 0) {
				saveLine();
				// mPoiInfoText.setText("");
				// mPoiName.setText("");
				// mCateDialog.show();
				// mCateDialog.getButton(AlertDialog.BUTTON_POSITIVE)
				// .setOnClickListener(new OnClickListener() {
				//
				// @Override
				// public void onClick(View v) {
				// saveLine();
				// }
				// });
			}
			break;
		case R.id.close:
			if (mMarkList != null && mMarkList.size() >= 3) {
				RMPoi poi = mMarkList.get(0);
				mMarkList.add(poi);
				mMapView.refreshMap();
			}
			break;
		case R.id.poi:
			float x = mMapView.getCenter().getX();
			float y = mMapView.getCenter().getY();
			RMPoi poi = new RMPoi();
			poi.set_id((int) System.currentTimeMillis());
			poi.setBuildId(mFloor.getBuildId());
			poi.setName("");
			poi.setX(x);
			poi.setY(y);
			poi.setFloor(mFloor.getFloor());
			String desc = LCApplication.getInstance().getShare()
					.getString("poi_desc", null);
			poi.setDesc(desc);
			Intent intent = new Intent(this, LCPoiInfoActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("poi", poi);
			bundle.putInt("sign", 2);//修改
			intent.putExtras(bundle);
			startActivityForResult(intent, 500);
			break;
		case R.id.delete:// 删除按钮
			deletePoint();
			break;
		case R.id.mark:// 采集按钮
			mark();
			break;
		case R.id.door_btn:// 标记门口
			markDoor();
			break;
		}
	}

	private void deletePoint() {
		if (mMarkList.size() > 0) {
			mMarkList.remove(mMarkList.size() - 1);
			if (mMarkList.size() == 0) {
				mExportBtn.setVisibility(View.GONE);
				mDoorBtn.setVisibility(View.VISIBLE);
				mDeleteBtn.setVisibility(View.GONE);
				noPicking();
			}
			if (mMarkList.size() < 3) {
				mCloseBtn.setVisibility(View.GONE);
			}
			mMapView.refreshMap();
		}
	}

	@Override
	public void onClick(NavigatePoint point, String key) {

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
		// mStatus.setText("定位错误码：" + location.getError());
		String scanner = LocationApp.getInstance().getScannerInfo();
		if (!DTStringUtils.isEmpty(scanner) && scanner.contains("<beacons>")) {
			new LCAsyncTask(new CheckBeaconCall()).runOnExecutor(false,scanner);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK)
			return;
		if (requestCode == 500) {
			RMPoi poi = (RMPoi) data.getExtras().getSerializable("poi");
			markPoi(poi);
		}
	}
}
