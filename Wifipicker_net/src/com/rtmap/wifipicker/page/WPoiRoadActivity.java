package com.rtmap.wifipicker.page;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rtm.frm.model.Floor;
import com.rtmap.wifipicker.BuildSession;
import com.rtmap.wifipicker.R;
import com.rtmap.wifipicker.core.DTAsyncTask;
import com.rtmap.wifipicker.core.DTCallBack;
import com.rtmap.wifipicker.core.model.RMLine;
import com.rtmap.wifipicker.core.model.RMPoi;
import com.rtmap.wifipicker.core.model.RMPoiList;
import com.rtmap.wifipicker.data.Coord;
import com.rtmap.wifipicker.util.Constants;
import com.rtmap.wifipicker.util.DTLog;
import com.rtmap.wifipicker.util.DTMathUtils;
import com.rtmap.wifipicker.util.DTStringUtils;
import com.rtmap.wifipicker.util.DTUIUtils;
import com.rtmap.wifipicker.util.FileHelper;
import com.rtmap.wifipicker.util.FileUtil;
import com.rtmap.wifipicker.util.Utils;
import com.rtmap.wifipicker.widget.MapCorrectionLayer;
import com.rtmap.wifipicker.widget.MapWidget;
import com.rtmap.wifipicker.widget.MapWidget.OnMouseListener;
import com.rtmap.wifipicker.widget.MapWidget.WidgetStateListener;
import com.rtmap.wifipicker.widget.PinMark;

/**
 * Poi与路网采集(位图)
 * 
 * @author dingtao
 *
 */
public class WPoiRoadActivity extends WPBaseActivity implements OnClickListener {

	private MapWidget mapWidget;
	private MapCorrectionLayer correctLayer;// 采集点图层
	private ArrayList<RMPoi> mMarkList;
	private PinMark pinMark;
	// private MapInfo mMapInfo;// 地图name,由MapSelectActivity传入
	private TextView mapNameTxtView;// 标题栏，显示采集地图的名称

	private Floor mFloor;
	private String mMapName;

	private String root;
	private float mScale;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_poi_road_activity);
		mFloor = (Floor) getIntent().getExtras().getSerializable(
				Constants.EXTRA_FLOOR);
		mScale = getIntent().getFloatExtra("scale", 1);
		root = Constants.WIFI_PICKER_PATH + mUserName + "/"
				+ mFloor.getBuildid() + "/";
		FileHelper.checkDir(root);
		mMarkList = new ArrayList<RMPoi>();
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapWidget.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapWidget.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapWidget.onPause();
		pinMark = null;
		correctLayer = null;
		mapWidget = null;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (event.getAction() != KeyEvent.ACTION_DOWN) {
			return false;
		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			WPoiRoadActivity.this.finish();
			break;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			mark();
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			markDoor();
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

	private void initContrls() {
		findViewById(R.id.btn_expore_map_correct).setOnClickListener(this);
		findViewById(R.id.btn_del_map_correct).setOnClickListener(this);
		findViewById(R.id.btn_mark_map_correct).setOnClickListener(this);
		findViewById(R.id.door).setOnClickListener(this);
		mapNameTxtView = (TextView) findViewById(R.id.txt_map_name_map_correct);


		final Button up = (Button) findViewById(R.id.button_up);
		final Button down = (Button) findViewById(R.id.button_down);
		final Button left = (Button) findViewById(R.id.button_left);
		final Button right = (Button) findViewById(R.id.button_right);

		View.OnTouchListener touchListener = new View.OnTouchListener() {
			boolean longClick = false;

			// 方向键
			Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.arg1) {
					case R.id.button_up:
						up.setBackgroundResource(R.drawable.icon_top_on);
						mapWidget.getCoordTransformer().bitmapTranslate(0, 2);
						break;
					case R.id.button_down:
						down.setBackgroundResource(R.drawable.icon_bottom_on);
						mapWidget.getCoordTransformer().bitmapTranslate(0, -2);
						break;
					case R.id.button_left:
						left.setBackgroundResource(R.drawable.icon_left_on);
						mapWidget.getCoordTransformer().bitmapTranslate(2, 0);
						break;
					case R.id.button_right:
						right.setBackgroundResource(R.drawable.icon_right_on);
						mapWidget.getCoordTransformer().bitmapTranslate(-2, 0);
						break;
					}

					if (longClick) {// 长按方向键地图持续挪动
						Message message = new Message();
						message.what = msg.what;
						message.arg1 = msg.arg1;
						handler.sendMessageDelayed(message, 100);
					} else {
						handler.removeMessages(0);
						up.setBackgroundResource(R.drawable.icon_top_off);
						down.setBackgroundResource(R.drawable.icon_bottom_off);
						left.setBackgroundResource(R.drawable.icon_left_off);
						right.setBackgroundResource(R.drawable.icon_right_off);
					}
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_expore_map_correct:
			if (mMarkList != null && mMarkList.size() != 0) {
				exportMapCorrection();
			}
			break;
		case R.id.btn_del_map_correct:
			if (mMarkList.size() > 0) {
				mMarkList.remove(mMarkList.size() - 1);
			}
			break;
		case R.id.door:
			markDoor();
			break;
		case R.id.btn_mark_map_correct:
			mark();
			break;
		}
	}
	RMPoi clickPoint = null;
	String key = null;

	private void init() {
		initContrls();

		mMapName = mFloor.getBuildid() + "-" + mFloor.getFloor();
		// 根据ID获取地图名
		mapNameTxtView.setText("POI与路网采集(位图)");
		BuildSession.getInstance().setBuildId(mFloor.getBuildid());// 初始化全局的mapname，在其他地方用到，例如database存储
		BuildSession.getInstance().setFloor(mFloor.getFloor());

		mapWidget = (MapWidget) findViewById(R.id.map_view_map_correct);
		mapWidget.registerWidgetStateListener(new WidgetStateListener() {
			@Override
			public void onMapWidgetCreated(MapWidget map) {
				String bitmap_path = root + mMapName + "-0.jpg";
				mapWidget.openMapFile(bitmap_path);
			}
		});
		// 初始标记点设置，图形、可见、居中
		pinMark = new PinMark(mapWidget, R.drawable.pin48);
		pinMark.setVisiable(true);
		pinMark.setLocation(Utils.getEquipmentWidth(this) / 2,
				Utils.getEquipmentHeight(this) / 2);
		// 添加标记点图层及采集点图层
		mapWidget.addMark(pinMark);
		correctLayer = new MapCorrectionLayer(mScale);
		mapWidget.addMark(correctLayer);
		mapWidget.registerMouseListener(new OnMouseListener() {

			@Override
			public void onSingleTap(MapWidget mw, float x, float y) {
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
					if (!"door".equals(key)) {
						AlertDialog.Builder build = new Builder(
								WPoiRoadActivity.this);
						build.setTitle("提示");
						build.setMessage("确认删除\""
								+ mRouteMap.get(key).get(0).getName() + "\"吗");
						build.setPositiveButton("确认",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										FileUtil.deleteFile(key);
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
					}else{
						AlertDialog.Builder build = new Builder(
								WPoiRoadActivity.this);
						build.setTitle("提示");
						build.setMessage("删除门信息？");
						build.setPositiveButton("确认",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(
											DialogInterface arg0,
											int arg1) {
										correctLayer.getRoute("door").remove(clickPoint);
										File file = new File(root + mMapName + ".door");
										try {
											if (!file.exists())
												file.createNewFile();
											OutputStreamWriter write = new OutputStreamWriter(
													new FileOutputStream(file), DTStringUtils.UTF_8);
											BufferedWriter bw = new BufferedWriter(write);
											Gson gson = new Gson();
											RMPoiList poilist = new RMPoiList();
											poilist.setPoiList(correctLayer.getRoute("door"));
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
									public void onClick(
											DialogInterface arg0,
											int arg1) {
									}
								});
						build.create().show();
					}
				}
			}
		});
		showMapCorrectionHistory();
	}

	private void showMapCorrectionHistory() {

		new DTAsyncTask(new DTCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				File file = new File(root + mMapName + ".door");
				if (!file.exists())
					return null;
				try {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(new FileInputStream(file),
									UTF_8));
					String line, result = "";
					while ((line = br.readLine()) != null) {
						// 将文本打印到控制台
						result += line;
					}
					if (!DTStringUtils.isEmpty(result)) {
						Gson gson = new Gson();
						ArrayList<RMPoi> poiList = gson.fromJson(result,
								RMPoiList.class).getPoiList();
						correctLayer.addRoute("door", poiList);
					}
					br.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					file.delete();
					e.printStackTrace();
				}
				return null;
			}

			@Override
			public void onCallBackFinish(Object obj) {
				importRouteHistory();
			}
		}).run();

	}

	/**
	 * 导出路线历史
	 */
	private void importRouteHistory() {
		new DTAsyncTask(new DTCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				// storage/sdcard0/rtmap/WifiPicker//zizxs/860100010040500002-F2-0_1399275551008_370_437.mc
				String[] files_mc = FileHelper.listFiles(root,
						new FilenameFilter() {

							@Override
							public boolean accept(File dir, String filename) {
								if (filename.contains(mMapName + "_")
										&& (filename.endsWith(".mc"))) {
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
						File file = new File(path);
						BufferedReader br;
						try {
							br = new BufferedReader(new InputStreamReader(
									new FileInputStream(file), UTF_8));
							String line, result = "";
							while ((line = br.readLine()) != null) {
								// 将文本打印到控制台
								result += line;
							}
							if (result != null && !"".equals(result)) {
								Gson gson = new Gson();
								RMLine list = gson.fromJson(result,
										RMLine.class);// 得到线
								correctLayer.addRoute(path, list.getPoiList());
							}
							br.close();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				return null;
			}

			@Override
			public void onCallBackFinish(Object obj) {
				hideLoad();
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
			if (Math.sqrt(Math.pow(p.getX() - x, 2) + Math.pow(p.getY() - y, 2)) < 0.3) {// 两点之间距离必须大于0.3米
				DTUIUtils.showToastSafe("两点之间距离必须大于0.3米");
				return;
			}
		}
		RMPoi point = new RMPoi();
		point.setX(x);
		point.setY(y);
		point.setName("");
		if (mMarkList.size() == 0) {// 说明新建了一条采集路线
			correctLayer.addRoute(ROUTE, mMarkList);
		}
		mMarkList.add(point);
	}

	// private void gatherDoorInfo() {
	// final Coord coord = new Coord();
	// mapWidget.getCoordTransformer().clientToWorld(pinMark.getX(),
	// pinMark.getY(), coord);
	// if (coord.isValid()) {
	// // correctLayer.addCorrectPoint(new WifiPoint(coord.mX, coord.mY,
	// // MapCorrectionLayer.POINT_TYPE_MAP_CORRECT));
	// String path = String.format("%s%s" + DOOR_FILE_EXETENSION, root,
	// mMapName);
	// // ArrayList<WifiPoint> mcPoints = correctLayer.getCorrectPoints();
	// FileUtil.fstream(path,
	// String.format("%d\t%d\n", coord.mX, coord.mY));
	// showMapCorrectionHistory();
	// }
	//
	// }

	/**
	 * 添加门
	 */
	private void markDoor() {
		ArrayList<RMPoi> pois = correctLayer.getRoute("door");

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
		if (pois == null) {
			pois = new ArrayList<RMPoi>();
			correctLayer.addRoute("door", pois);
		}
		pois.add(point);
		File file = new File(root + mMapName + ".door");
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

	private void exportMapCorrection() {
		try {
			LayoutInflater inflater = getLayoutInflater();
			View layout = inflater.inflate(R.layout.poi_classify_dialog,
					(ViewGroup) findViewById(R.id.poi_classify_dialog));
			layout.findViewById(R.id.spin_first).setVisibility(View.GONE);
			layout.findViewById(R.id.spin_second).setVisibility(View.GONE);
			layout.findViewById(R.id.spin_third).setVisibility(View.GONE);
			layout.findViewById(R.id.spin_fourth).setVisibility(View.GONE);

			final EditText name = (EditText) layout.findViewById(R.id.poi_name);
			layout.findViewById(R.id.add_btn).setVisibility(View.GONE);
			layout.findViewById(R.id.delete_btn).setVisibility(View.GONE);

			new AlertDialog.Builder(this)
					.setTitle("POI分类采集")
					.setView(layout)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {

									long time = System.currentTimeMillis();
									String path_tmp = root + mMapName + "_"
											+ time;
									if (mMarkList.size() > 0) {// 名字或者类别都可以为空
										String startPoint = mMapName
												+ "_"
												+ System.currentTimeMillis()
												+ "_"
												+ (int) (mMarkList.get(0)
														.getX() * 1000)
												+ "_"
												+ (int) (mMarkList.get(0)
														.getY() * 1000);
										// _/storage/sdcard0/rtmap/WifiPicker/zizxs/860100010040500002-F2-0_1399275551008_370_437.mc

										String path = String.format("%s%s.mc",
												root, startPoint);

										RMLine line = new RMLine();// 新建一条线
										line.setName(name.getText().toString());
										ArrayList<RMPoi> list = new ArrayList<RMPoi>();
										list.addAll(mMarkList);
										line.setPoiList(list);
										for (RMPoi poi : list)
											// 为每个点增加name
											poi.setName(name.getText()
													.toString());
										File file = new File(path);// 准备写入数据
										try {
											if (!file.exists())
												file.createNewFile();
											OutputStreamWriter write = new OutputStreamWriter(
													new FileOutputStream(file),
													DTStringUtils.UTF_8);
											BufferedWriter bw = new BufferedWriter(
													write);
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
									}
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
								}
							}).show();

		} catch (Exception e) {
			DTUIUtils.showToastSafe("标记失败");
		}
	}

}
