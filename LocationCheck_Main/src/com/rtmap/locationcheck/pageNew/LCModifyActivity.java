package com.rtmap.locationcheck.pageNew;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.PointInfo;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;
import com.rtmap.locationcheck.R;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.LCAsyncTask;
import com.rtmap.locationcheck.core.LCCallBack;
import com.rtmap.locationcheck.core.model.BeaconInfo;
import com.rtmap.locationcheck.core.model.BeaconList;
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
import com.rtmap.locationcheck.util.PoiClassify;

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

	private TextView mTitle;// 标题栏，显示采集地图的名称
	private Floor mFloor;
	private String mMapName;

	private int delIndex = -1;// 删除文件索引

	private static final float MARK_ADD_DISTANCE = 0.001f;// 当两个点位置坐标一样，增加1毫米做区别
	private static final int IMPORT_HISTORY = 111;// 加载历史
	private String root;

	private HashMap<String, BeaconInfo> mBeaconMap;// beacon键值对mac值
	private ArrayList<BeaconInfo> mBeaconList;

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
		root = DTFileUtils.getWifiPickerPath() + USER_NAME + File.separator
				+ mFloor.getBuildId() + File.separator;
		DTFileUtils.createDirs(root);// 创建文件夹
		mMarkList = new ArrayList<RMPoi>();
		init();
	}

	@Override
	protected void onStart() {
		super.onStart();
		LocationApp.getInstance().start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		LocationApp.getInstance().stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.clearMapLayer();
	}

	/**
	 * 初始化view
	 */
	private void initView() {
		findViewById(R.id.export).setOnClickListener(this);
		findViewById(R.id.delete).setOnClickListener(this);
		findViewById(R.id.mark).setOnClickListener(this);
		findViewById(R.id.door_btn).setOnClickListener(this);
		mTitle = (TextView) findViewById(R.id.title);

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

	private void init() {
		initView();
		mBeaconMap = new HashMap<String, BeaconInfo>();
		mBeaconList = new ArrayList<BeaconInfo>();
		mMapName = mFloor.getBuildId() + "-" + mFloor.getFloor();
		mTitle.setText(mFloor.getName() + "_" + mFloor.getFloor());
		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		mMapView.setUpdateMap(false);
		mMapView.setDoubleTapable(false);

		// 初始化起终点
		Drawable drawable = getResources().getDrawable(R.drawable.sign_red);
		Bitmap markbmp = DTIOUtils.drawableToBitmap(drawable);// 起点图片
		drawable = getResources().getDrawable(R.drawable.sign_green);
		Bitmap pointbmp = DTIOUtils.drawableToBitmap(drawable);// 终点图片

		mRoutelayer = new RouteTextLayer(mMapView, pointbmp, markbmp);// 路线数组
		mRoutelayer.setOnPointClickListener(this);
		CompassLayer mCompassLayer = new CompassLayer(mMapView);// 指南针图层
		mMapView.addMapLayer(mCompassLayer);
		mMapView.addMapLayer(mRoutelayer);

		initCateDialog();

		initLocation();
		importBeaconHistory(false);
		mMapView.initMapConfig(mFloor.getBuildId(), mFloor.getFloor());// 打开地图（建筑物id，楼层id）
		handler.sendEmptyMessageDelayed(IMPORT_HISTORY, 1000);
	}

	public void initLocation() {
		boolean istext = LCApplication.getInstance().getShare()
				.getBoolean("istest", false);
		if (istext)
			LocationApp.getInstance().setRootFolder(
					"rtmapCheck" + File.separator + "test");
		else
			LocationApp.getInstance().setRootFolder(
					"rtmapCheck" + File.separator + "publish");
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
		LocationApp.getInstance().registerLocationListener(this);
	}

	/**
	 * 导入门数据
	 */
	private void importPointHistory() {
		new LCAsyncTask(new LCCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				File file = new File(root + mMapName + ".door");
				if (!file.exists())
					return null;
				try {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(new FileInputStream(file),
									"utf-8"));
					String line, result = "";
					while ((line = br.readLine()) != null) {
						// 将文本打印到控制台
						result += line;
					}
					if (!DTStringUtils.isEmpty(result)) {
						Gson gson = new Gson();
						mRoutelayer.addRoute("door",
								gson.fromJson(result, RMPoiList.class)
										.getPoiList());
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
				String[] files_mc = DTFileUtils.listFiles(root,
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
									new FileInputStream(file), "utf-8"));
							String line, result = "";
							while ((line = br.readLine()) != null) {
								// 将文本打印到控制台
								result += line;
							}
							if (result != null && !"".equals(result)) {
								Gson gson = new Gson();
								RMLine list = gson.fromJson(result,
										RMLine.class);// 得到线
								mRoutelayer.addRoute(path, list.getPoiList());
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
			mRoutelayer.addRoute(ROUTE, mMarkList);
		}
		mMarkList.add(point);
		mMapView.refreshMap();
	}

	/**
	 * 添加门
	 */
	private void markDoor() {
		DTLog.e("地图宽度：" + mMapView.getConfig().getBuildWidth() + "     "
				+ mMapView.getConfig().getBuildHeight());
		ArrayList<RMPoi> pois = mRoutelayer.getRoute("door");
		RMPoi point = new RMPoi();
		point.setX(mMapView.getCenter().getX());
		point.setY(mMapView.getCenter().getY());
		point.setName("门");
		point.setDesc("门");
		DTLog.e("标记点X:" + point.getX() + "     Y:" + point.getY());
		if (pois == null) {
			pois = new ArrayList<RMPoi>();
			mRoutelayer.addRoute("door", pois);
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
		mMapView.refreshMap();
	}

	static int provincePosition = 0;
	static int cityPosition = 0;
	static int countyPosition = 0;
	static int fourthPosition = 0;
	private AlertDialog mCateDialog;
	private TextView mPoiInfoText;
	private EditText mPoiName;

	/**
	 * 导出路线数据
	 */
	private void initCateDialog() {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.poi_classify_dialog,
				(ViewGroup) findViewById(R.id.poi_classify_dialog));
		final Spinner provinceSpinner = (Spinner) layout
				.findViewById(R.id.spin_first);
		final Spinner citySpinner = (Spinner) layout
				.findViewById(R.id.spin_second);
		final Spinner countySpinner = (Spinner) layout
				.findViewById(R.id.spin_third);
		final Spinner fourthSpinner = (Spinner) layout
				.findViewById(R.id.spin_fourth);

		// 绑定适配器和值
		final ArrayAdapter<String> provinceAdapter = new ArrayAdapter<String>(
				LCModifyActivity.this, android.R.layout.simple_spinner_item,
				PoiClassify.province);
		provinceSpinner.setAdapter(provinceAdapter);
		provinceSpinner.setSelection(0, true); // 设置默认选中项，此处为默认选中第4个值

		citySpinner.setAdapter(new ArrayAdapter<String>(LCModifyActivity.this,
				android.R.layout.simple_spinner_item, PoiClassify.city[0]));
		citySpinner.setSelection(0, true); // 默认选中第0个

		countySpinner.setAdapter(new ArrayAdapter<String>(
				LCModifyActivity.this, android.R.layout.simple_spinner_item,
				PoiClassify.county[0][0]));
		countySpinner.setSelection(0, true);

		fourthSpinner.setAdapter(new ArrayAdapter<String>(
				LCModifyActivity.this, android.R.layout.simple_spinner_item,
				PoiClassify.fourthStrs[0][0][0]));
		fourthSpinner.setSelection(0, true);

		// 省级下拉框监听
		provinceSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					// 表示选项被改变的时候触发此方法，主要实现办法：动态改变地级适配器的绑定值
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						// position为当前省级选中的值的序号

						// 将地级适配器的值改变为city[position]中的值
						citySpinner.setAdapter(new ArrayAdapter<String>(
								LCModifyActivity.this,
								android.R.layout.simple_spinner_item,
								PoiClassify.city[position]));
						provincePosition = position; // 记录当前省级序号，留给下面修改市级适配器时用
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}

				});

		// 地级下拉监听
		citySpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						countySpinner
								.setAdapter(new ArrayAdapter<String>(
										LCModifyActivity.this,
										android.R.layout.simple_spinner_item,
										PoiClassify.county[provincePosition][position]));
						cityPosition = position; // 记录当前市级序号，留给下面修改县级适配器时用

					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}

				});

		countySpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						fourthSpinner
								.setAdapter(new ArrayAdapter<String>(
										LCModifyActivity.this,
										android.R.layout.simple_spinner_item,
										PoiClassify.fourthStrs[provincePosition][cityPosition][position]));
						countyPosition = position;
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});

		fourthSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						fourthPosition = position;
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});

		mPoiInfoText = (TextView) layout.findViewById(R.id.classify_info);
		mPoiName = (EditText) layout.findViewById(R.id.poi_name);
		Button addPoiBtn = (Button) layout.findViewById(R.id.add_btn);
		Button deletePoiBtn = (Button) layout.findViewById(R.id.delete_btn);
		addPoiBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String province = provinceSpinner.getSelectedItem().toString();
				String city = citySpinner.getSelectedItem().toString();
				String county = countySpinner.getSelectedItem().toString();
				String fourth = fourthSpinner.getSelectedItem().toString();
				String poi = province + "-" + city + "-" + county + "-"
						+ fourth + ";";
				String cate = mPoiInfoText.getText().toString();
				if (cate.contains(poi)) {
					DTUIUtils.showToastSafe("不能重复添加分类");
				} else {
					cate += poi;
				}
				mPoiInfoText.setText(cate);
			}
		});
		deletePoiBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String cate = mPoiInfoText.getText().toString();
				if (!DTStringUtils.isEmpty(cate)) {
					cate = cate.substring(0, cate.lastIndexOf(";"));
					mPoiInfoText.setText(cate.substring(0,
							cate.lastIndexOf(";") + 1));
				}
			}
		});
		mCateDialog = new AlertDialog.Builder(this)
				.setTitle("POI分类采集")
				.setView(layout)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {

								if (mMarkList.size() > 0) {// 名字或者类别都可以为空
									String startPoint = mMapName
											+ "_"
											+ System.currentTimeMillis()
											+ "_"
											+ (int) (mMarkList.get(0).getX() * 1000)
											+ "_"
											+ (int) (mMarkList.get(0).getY() * 1000);
									// _/storage/sdcard0/rtmap/WifiPicker/zizxs/860100010040500002-F2-0_1399275551008_370_437.mc

									String path = String.format("%s%s.mc",
											root, startPoint);

									RMLine line = new RMLine();// 新建一条线
									line.setDesc(mPoiInfoText.getText()
											.toString());
									line.setName(mPoiName.getText().toString());
									ArrayList<RMPoi> list = new ArrayList<RMPoi>();
									list.addAll(mMarkList);
									line.setPoiList(list);
									for (RMPoi poi : list)
										// 为每个点增加name
										poi.setName(mPoiName.getText()
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
									mRoutelayer.removeRoute(ROUTE);
									mRoutelayer.addRoute(path, mMarkList);
									mMarkList = new ArrayList<RMPoi>();
									mMapView.refreshMap();
								} else {
									Message msg = new Message();
									msg.what = POI_CLASSIFY_NOT_ADD_MSG;
									handler.sendMessage(msg);
								}
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.create();
	}

	@Override
	public void onClick(final RMPoi point, final String key) {

		if (key.equals("door")) {
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
							File file = new File(root + mMapName + ".door");
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
		} else {
			// storage/sdcard0/rtmap/WifiPicker//zizxs/860100010040500002-F2-0_1399275551008_370_437.mc
			final String[] files_mc = DTFileUtils.listFiles(root,
					new FilenameFilter() {
						@Override
						public boolean accept(File dir, String filename) {
							if (filename.contains(mMapName)
									&& filename.endsWith(".mc")) {
								return true;
							}
							return false;
						}
					});
			for (int i = 0; i < files_mc.length; i++) {
				ArrayList<RMPoi> route = mRoutelayer.getRoute(key);
				// DTLog.e("route_path : " + files_mc[i]);
				// DTLog.e("x_y : " + (int) (route.get(0).getX() * 1000) + "_"
				// + (int) (route.get(0).getY() * 1000));
				if (files_mc[i].contains((int) (route.get(0).getX() * 1000)
						+ "_" + (int) (route.get(0).getY() * 1000))) {
					delIndex = i;
					break;
				}
			}
			if (delIndex != -1) {
				AlertDialog.Builder build = new Builder(LCModifyActivity.this);
				build.setTitle("提示");
				build.setMessage("确认删除\"" + point.getName() + "\"吗");
				build.setPositiveButton("确认",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								DTFileUtils.deleteFile(root
										+ files_mc[delIndex]);
								mRoutelayer.removeRoute(root
										+ files_mc[delIndex]);
								mMapView.refreshMap();
								delIndex = -1;
							}
						});
				build.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								delIndex = -1;
							}
						});
				build.create().show();
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.export:// 导出数据
			if (mMarkList.size() != 0) {
				mPoiInfoText.setText("");
				mPoiName.setText("");
				mCateDialog.show();
			}
			break;
		case R.id.delete:// 删除按钮
			if (mMarkList.size() > 0) {
				mMarkList.remove(mMarkList.size() - 1);
				mMapView.refreshMap();
			}
			break;
		case R.id.mark:// 采集按钮
			mark();
			break;
		case R.id.door_btn:// 标记门口
			markDoor();
			break;
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
						return gson.fromJson(result.toString(),
								BeaconList.class);
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
							mBeaconList.add(info);
							mBeaconMap.put(info.getMac(), info);
							if (info.getMaclist() != null
									&& info.getMaclist().size() > 0) {
								for (int i = 0; i < info.getMaclist().size(); i++) {
									mBeaconMap.put(info.getMaclist().get(i)
											.getMac(), info);
								}
							}
						}
						mMapView.refreshMap();
						exportBeaconInfo();
					}
				}
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
			if (!result.contains("<beacons")) {
				return null;
			}
			result = result.substring(
					result.indexOf("<beacons>") + "<beacons>".length(),
					result.indexOf("</beacons>"));
			String[] b = result.split("#");
			for (int i = 0; i < b.length; i++) {
				if (DTStringUtils.isEmpty(b[i]))
					continue;
				String mac = b[i].substring(0, b[i].indexOf("$")).toUpperCase();
				String rssivalue = b[i].substring(b[i].indexOf("$") + 1);
				if (mBeaconMap.containsKey(mac)) {
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
				}
			}
			mMapView.refreshMap();
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			exportBeaconInfo();
		}
	}

	/**
	 * 导出beacon信息
	 */
	private void exportBeaconInfo() {
		new LCAsyncTask(new LCCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				if (mBeaconList.size() == 0)
					return null;
				String filepath = DTFileUtils.getDataDir()
						+ mFloor.getBuildId() + "_" + mFloor.getFloor()
						+ ".txt";
				boolean isCopy = false;// 是否备份
				int changeCount = 0;// 修改的beacon点数
				for (BeaconInfo info : mBeaconList) {
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
					list.setList(mBeaconList);
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

	@Override
	public void onReceiveLocation(RMLocation location) {
		if (location.getError() == 0) {
			mMapView.setMyCurrentLocation(location);
		}
		// mStatus.setText("定位错误码：" + location.getError());
		String scanner = LocationApp.getInstance().getScannerInfo();
		if (!DTStringUtils.isEmpty(scanner) && scanner.contains("<beacons>")) {
			new LCAsyncTask(new CheckBeaconCall()).run(scanner);
		}
	}
}
