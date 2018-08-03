package com.rtmap.locationcheck.pageNew;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
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
import com.rtm.common.model.RMLocation;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;
import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.LCAsyncTask;
import com.rtmap.locationcheck.core.LCCallBack;
import com.rtmap.locationcheck.core.model.Floor;
import com.rtmap.locationcheck.core.model.RMLine;
import com.rtmap.locationcheck.core.model.RMPoi;
import com.rtmap.locationcheck.core.model.RMPoiList;
import com.rtmap.locationcheck.layer.MapCorrectionLayer;
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

/**
 * Poi与路网采集(位图)
 * 
 * @author dingtao
 *
 */
public class LCPoiRoadActivity extends LCActivity implements OnClickListener,
		RMLocationListener {

	private MapWidget mapWidget;
	private MapCorrectionLayer correctLayer;// 采集点图层
	private ArrayList<RMPoi> mMarkList;
	private PinMark pinMark;
	private Button mDoorBtn, mExportBtn, mDeleteBtn, mCloseBtn;
	// private MapInfo mMapInfo;// 地图name,由MapSelectActivity传入

	private Floor mFloor;
	private String mMapName;

	private String root;
	private float mScale;
	private TextView mStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_poi_road_activity);
		mFloor = (Floor) getIntent().getExtras().getSerializable("floor");
		root = DTFileUtils.getDataDir() + mFloor.getBuildId() + File.separator;
		mScale = mFloor.getScale();
		DTFileUtils.createDirs(root);
		mMarkList = new ArrayList<RMPoi>();
		init(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapWidget.onResume();
		if (isOpenLocation)
			LocationApp.getInstance().start();
		else
			DTUIUtils.showToastSafe("定位未开启");
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapWidget.onPause();
		if (isOpenLocation)
			LocationApp.getInstance().stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isOpenLocation)
			LocationApp.getInstance().unRegisterLocationListener(this);
		correctLayer.clearHistoryCorrectPoints();
		mapWidget.onPause();
		pinMark = null;
		mapWidget = null;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (event.getAction() != KeyEvent.ACTION_DOWN) {
			return false;
		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			LCPoiRoadActivity.this.finish();
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

	private void initContrls() {
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.export:
			if (mMarkList != null && mMarkList.size() != 0) {
				saveLine();
			}
			break;
		case R.id.close:
			if (mMarkList != null && mMarkList.size() >= 3) {
				RMPoi poi = mMarkList.get(0);
				mMarkList.add(poi);
			}
			break;
		case R.id.delete:
			deletePoint();
			break;
		case R.id.door_btn:
			markDoor();
			break;
		case R.id.mark:
			mark();
			break;
		case R.id.poi:
			final Coord coord = new Coord();
			mapWidget.getCoordTransformer().clientToWorld(pinMark.getX(),
					pinMark.getY(), coord);
			float x = coord.mX * mScale;
			float y = coord.mY * mScale;
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
		}
	}

	private void deletePoint() {
		if (mMarkList.size() > 0) {
			mMarkList.remove(mMarkList.size() - 1);
			if (mMarkList.size() == 0) {
				isPicking();
				mExportBtn.setVisibility(View.GONE);
				mDoorBtn.setVisibility(View.VISIBLE);
				mDeleteBtn.setVisibility(View.GONE);
			}
			if (mMarkList.size() < 3) {
				mCloseBtn.setVisibility(View.GONE);
			}
		}
	}

	RMPoi clickPoint = null;
	String key = null;

	private void init(Bundle arg0) {
		initContrls();
		mMapName = mFloor.getBuildId() + "-" + mFloor.getFloor();
		// 根据ID获取地图名
		mStatus = (TextView) findViewById(R.id.status);
		mapWidget = (MapWidget) findViewById(R.id.map_view_map_correct);
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
		// 初始化起终点
		Drawable drawable = getResources().getDrawable(
				R.drawable.sign_route_purple);
		Bitmap pointbmp = DTIOUtils.drawableToBitmap(drawable);// 终点图片
		Drawable black = getResources().getDrawable(R.drawable.sign_poi_gray);
		Bitmap blackBitmap = DTIOUtils.drawableToBitmap(black);
		correctLayer = new MapCorrectionLayer(mScale, pointbmp, pointbmp,
				blackBitmap, blackBitmap);
		mapWidget.addMark(correctLayer);
		mapWidget.registerMouseListener(new OnMouseListener() {

			@Override
			public void onSingleTap(MapWidget mw, float x, float y) {
				if (mDeleteBtn.getVisibility() == View.VISIBLE) {
					return;
				}
				Point temppoi = new Point();
				clickPoint = null;
				key = null;
				float p2p = -1;// 两个点之间的距离
				HashMap<String, ArrayList<RMPoi>> mRouteMap = correctLayer
						.getRouteMap();
				Iterator<String> keySet = mRouteMap.keySet().iterator();
				while (keySet.hasNext()) {
					String str = keySet.next();
					ArrayList<RMPoi> points = mRouteMap.get(str);
					for (int i = 0; i < points.size(); i++) {
						RMPoi p = points.get(i);

						mapWidget.getCoordTransformer().worldToClient(
								p.getX() / mScale, p.getY() / mScale, temppoi);
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
					if (key.endsWith(".door")) {
						AlertDialog.Builder build = new Builder(
								LCPoiRoadActivity.this);
						build.setTitle("提示");
						build.setMessage("删除门信息？");
						build.setPositiveButton("确认",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										correctLayer.getRoute(key).remove(
												clickPoint);
										File file = new File(key);
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
											poilist.setPoiList(correctLayer
													.getRoute(key));
											bw.write(gson.toJson(poilist));
											bw.flush();
											bw.close();
										} catch (IOException e) {
											file.delete();
											e.printStackTrace();
										}
									}
								});
						build.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
									}
								});
						build.create().show();
					} else if (key.endsWith(".mc")) {
						AlertDialog.Builder build = new Builder(
								LCPoiRoadActivity.this);
						build.setTitle("提示");
						build.setMessage("确认删除\""
								+ mRouteMap.get(key).get(0).getName() + "\"吗");
						build.setPositiveButton("确认",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										DTFileUtils.deleteFile(key);
										correctLayer.removeRoute(key);
									}
								});
						build.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
									}
								});
						build.create().show();

					} else if (key.endsWith(".poi")) {
						AlertDialog.Builder build = new Builder(
								LCPoiRoadActivity.this);
						build.setMessage(clickPoint.getName());
						build.setPositiveButton("删除",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										ArrayList<RMPoi> doorList = correctLayer
												.getRoute(key);
										doorList.remove(clickPoint);
										if (!DTStringUtils.isEmpty(clickPoint
												.getImage())) {
											DTFileUtils.deleteFile(DTFileUtils
													.getDataDir()
													+ mFloor.getBuildId()
													+ File.separator
													+ clickPoint.getImage());
										}
										File file = new File(key);
										if (doorList.size() == 0) {
											file.delete();
										} else {
											try {
												if (!file.exists())
													file.createNewFile();
												OutputStreamWriter write = new OutputStreamWriter(
														new FileOutputStream(
																file),
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
									}
								});
						build.setNegativeButton("修改",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										Intent intent = new Intent(
												LCPoiRoadActivity.this,
												LCPoiInfoActivity.class);
										Bundle bundle = new Bundle();
										bundle.putSerializable("poi",
												clickPoint);
										bundle.putInt("sign", 1);//修改
										intent.putExtras(bundle);
										startActivityForResult(intent, 500);
									}
								});
						build.create().show();
					}
				}
			}
		});
		if (isOpenLocation)
			initLocation();
		if (arg0 == null)
			showMapCorrectionHistory();
		else {
			correctLayer.setRouteMap((HashMap<String, ArrayList<RMPoi>>) arg0
					.getSerializable("layer"));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putSerializable("layer", correctLayer.getRouteMap());
	}

	private void showMapCorrectionHistory() {

		new LCAsyncTask(new LCCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				correctLayer.setDraw(false);
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
							ArrayList<RMPoi> poiList = gson.fromJson(result,
									RMPoiList.class).getPoiList();
							correctLayer.addRoute(p, poiList);
						}
					}
				}
				return null;
			}

			@Override
			public void onCallBackFinish(Object obj) {
				correctLayer.setDraw(true);
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
				correctLayer.setDraw(false);
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
					int sizeOfFiles = files_mc.length;
					for (int i = 0; i < sizeOfFiles; i++) {
						String path = String.format("%s%s", root, files_mc[i]);
						DTLog.e(".mc path : " + path);
						String result = DTFileUtils.readFile(path);
						if (!DTStringUtils.isEmpty(result)) {
							Gson gson = new Gson();
							RMLine list = gson.fromJson(result, RMLine.class);// 得到线
							correctLayer.addRoute(path, list.getPoiList());
						}
					}
				}

				return null;
			}

			@Override
			public void onCallBackFinish(Object obj) {
				correctLayer.setDraw(true);
				mLoadDialog.cancel();
			}
		}).run();

	}

	// private void gatherMapCorrection() {
	// final Coord coord = new Coord();
	// mapWidget.getCoordTransformer().clientToWorld(pinMark.getX(),
	// pinMark.getY(), coord);
	// if (coord.isValid()) {
	// correctLayer.addCorrectPoint(new WifiPoint(coord.mX, coord.mY,
	// MapCorrectionLayer.POINT_TYPE_MAP_CORRECT));
	// }
	// }

	/**
	 * 采集路网
	 */
	private void mark() {
		final Coord coord = new Coord();
		mapWidget.getCoordTransformer().clientToWorld(pinMark.getX(),
				pinMark.getY(), coord);
		float x = coord.mX * mScale;
		float y = coord.mY * mScale;
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
			isPicking();
			correctLayer.addRoute(ROUTE, mMarkList);
			mDoorBtn.setVisibility(View.GONE);
			mExportBtn.setVisibility(View.VISIBLE);
			mDeleteBtn.setVisibility(View.VISIBLE);
		}
		mMarkList.add(point);
		if (mMarkList.size() >= 3) {
			mCloseBtn.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 添加门
	 */
	private void markDoor() {
		String door_path = root + mMapName + ".door";
		ArrayList<RMPoi> pois = correctLayer.getRoute(door_path);

		final Coord coord = new Coord();
		mapWidget.getCoordTransformer().clientToWorld(pinMark.getX(),
				pinMark.getY(), coord);

		float x = coord.mX * mScale;
		float y = coord.mY * mScale;
		DTLog.e("X :" + x + "  Y: " + y + " listSize:" + mMarkList.size());

		RMPoi point = new RMPoi();
		point.setX(x);
		point.setY(y);
		point.setName("门");
		point.setDesc("门");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = format.format(new Date(System.currentTimeMillis()));
		point.setTime(time);
		if (pois == null) {
			pois = new ArrayList<RMPoi>();
			correctLayer.addRoute(door_path, pois);
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
		File file = new File(door_path);
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
	}

	/**
	 * 添加POI
	 */
	private void markPoi(RMPoi poi) {
		String filePath = root + mMapName + ".poi";
		ArrayList<RMPoi> pois = correctLayer.getRoute(filePath);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = format.format(new Date(System.currentTimeMillis()));
		poi.setTime(time);
		DTLog.e("标记点id: " + poi.get_id() + "   X:" + poi.getX() + "     Y:"
				+ poi.getY());
		if (pois == null) {
			pois = new ArrayList<RMPoi>();
			correctLayer.addRoute(filePath, pois);
		}
		for (int i = 0; i < pois.size(); i++) {
			if (pois.get(i).get_id() == poi.get_id()) {
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
	}

	private final static String ROUTE = "beta_route";

	private RMLocation mLocation;

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
			correctLayer.removeRoute(ROUTE);
			correctLayer.addRoute(path, mMarkList);
			mMarkList = new ArrayList<RMPoi>();
			mExportBtn.setVisibility(View.GONE);
			mCloseBtn.setVisibility(View.GONE);
			mDoorBtn.setVisibility(View.VISIBLE);
			mDeleteBtn.setVisibility(View.GONE);
			noPicking();
		}
	}

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
