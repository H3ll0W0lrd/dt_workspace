package com.rtmap.wifipicker.page;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
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
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Floor;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.PointInfo;
import com.rtmap.wifipicker.BuildSession;
import com.rtmap.wifipicker.R;
import com.rtmap.wifipicker.core.DTAsyncTask;
import com.rtmap.wifipicker.core.DTCallBack;
import com.rtmap.wifipicker.core.model.RMPoi;
import com.rtmap.wifipicker.core.model.RMPoiList;
import com.rtmap.wifipicker.layer.OnPointClickListener;
import com.rtmap.wifipicker.layer.PointLayer;
import com.rtmap.wifipicker.util.Constants;
import com.rtmap.wifipicker.util.DTFileUtils;
import com.rtmap.wifipicker.util.DTStringUtils;
import com.rtmap.wifipicker.util.DTUIUtils;
import com.rtmap.wifipicker.util.FileUtil;
import com.rtmap.wifipicker.util.ImgUtil;
import com.rtmap.wifipicker.util.PoiClassify;

public class WPoiActivity extends WPBaseActivity implements
		OnPointClickListener {

	private Button exportBtn;// 开始采集
	private Button backBtn;// 返回地图选择界面
	private Button delBtn;// 删除
	private Button markBtn;// 标记

	private MapView mMapView;// mapView
	private String mMapName;// 地图name,由MapSelectActivity传入
	private TextView mTitle;// 标题栏，显示采集地图的名称
	private PointLayer mPointLayer;// 路线layer
	private Bitmap mMarkBitmap;// 开始结束bitmap
	private Floor mFloor;
	private String rootPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rm_poi_collect_activity);
		mFloor = (Floor) getIntent().getExtras().getSerializable(
				Constants.EXTRA_FLOOR);
		rootPath = Constants.WIFI_PICKER_PATH + File.separator + mUserName
				+ File.separator + mFloor.getBuildid() + File.separator;
		FileUtil.checkDir(rootPath);
		init();
	}

	private void init() {
		initContrls();
		mFloor = (Floor) getIntent().getExtras().getSerializable(
				Constants.EXTRA_FLOOR);
		mMapName = mFloor.getBuildid() + "-" + mFloor.getFloor();// 地图name,由MapSelectActivity传入

		BuildSession.getInstance().setBuildId(mFloor.getBuildid());
		BuildSession.getInstance().setFloor(mFloor.getFloor());
		// 根据ID获取地图名
		mTitle.setText("POI采集");
		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		mMapView.setUpdateMap(false);
		mMapView.setDoubleTapable(false);
		mMapView.initMapConfig(mFloor.getBuildid(), mFloor.getFloor());// 打开地图（建筑物id，楼层id）
		mMapView.initScale();// 初始化比例尺

		// 初始化起终点
		Drawable drawable = getResources().getDrawable(R.drawable.sign_red);
		mMarkBitmap = ImgUtil.drawableToBitmap(drawable);// 起点图片
		mPointLayer = new PointLayer(mMapView, mMarkBitmap);
		CompassLayer mCompassLayer = new CompassLayer(mMapView);// 指南针图层
		mMapView.addMapLayer(mCompassLayer);
		mMapView.addMapLayer(mPointLayer);
		mPointLayer.setOnPointClickListener(this);

		showPOIHistory();// 导入历史数据
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() != KeyEvent.ACTION_DOWN) {
			return false;
		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			gatherPOI();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.clearMapLayer();
	}

	private void initContrls() {
		exportBtn = (Button) findViewById(R.id.btn_expore_poi);
		backBtn = (Button) findViewById(R.id.btn_back_poi);
		delBtn = (Button) findViewById(R.id.btn_del_poi);
		markBtn = (Button) findViewById(R.id.btn_mark_poi);
		mTitle = (TextView) findViewById(R.id.txt_map_name_poi);
		exportBtn.setVisibility(View.GONE);

		View.OnClickListener listener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.btn_back_poi:
					finish();
					break;
				case R.id.btn_del_poi:
					deleteLastPOI();
					break;
				case R.id.btn_mark_poi:
					gatherPOI();
					break;
				}
			}
		};

		exportBtn.setOnClickListener(listener);
		backBtn.setOnClickListener(listener);
		delBtn.setOnClickListener(listener);
		markBtn.setOnClickListener(listener);

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
					if (mMapView == null || mMapView.getCenter() == null)
						return;
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

	/**
	 * 导入历史数据
	 */
	private void showPOIHistory() {
		File file = new File(rootPath + mMapName + ".poi");
		if (!file.exists())
			return;
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String line, result = "";
			while ((line = br.readLine()) != null) {
				// 将文本打印到控制台
				result += line;
			}
			if (!DTStringUtils.isEmpty(result)) {
				Gson gson = new Gson();
				mPointLayer.addPointList(gson.fromJson(result, RMPoiList.class)
						.getPoiList());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			file.delete();
			e.printStackTrace();
		}
	}

	private Spinner provinceSpinner = null; // 省级（省、直辖市）
	private Spinner citySpinner = null; // 地级市
	private Spinner countySpinner = null; // 县级（区、县、县级市）
	private Spinner fourthSpinner = null;
	ArrayAdapter<String> provinceAdapter = null; // 省级适配器
	ArrayAdapter<String> cityAdapter = null; // 地级适配器
	ArrayAdapter<String> countyAdapter = null; // 县级适配器
	ArrayAdapter<String> fourthAdapter = null;
	static int provincePosition = 0;
	static int cityPosition = 0;
	static int countyPosition = 0;
	static int fourthPosition = 0;

	private void gatherPOI() {

		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.poi_classify_dialog,
				(ViewGroup) findViewById(R.id.poi_classify_dialog));
		provinceSpinner = (Spinner) layout.findViewById(R.id.spin_first);
		citySpinner = (Spinner) layout.findViewById(R.id.spin_second);
		countySpinner = (Spinner) layout.findViewById(R.id.spin_third);
		fourthSpinner = (Spinner) layout.findViewById(R.id.spin_fourth);

		// 绑定适配器和值
		provinceAdapter = new ArrayAdapter<String>(WPoiActivity.this,
				android.R.layout.simple_spinner_item, PoiClassify.province);
		provinceSpinner.setAdapter(provinceAdapter);
		provinceSpinner.setSelection(0, true); // 设置默认选中项，此处为默认选中第4个值

		cityAdapter = new ArrayAdapter<String>(WPoiActivity.this,
				android.R.layout.simple_spinner_item, PoiClassify.city[0]);
		citySpinner.setAdapter(cityAdapter);
		citySpinner.setSelection(0, true); // 默认选中第0个

		countyAdapter = new ArrayAdapter<String>(WPoiActivity.this,
				android.R.layout.simple_spinner_item, PoiClassify.county[0][0]);
		countySpinner.setAdapter(countyAdapter);
		countySpinner.setSelection(0, true);

		fourthAdapter = new ArrayAdapter<String>(WPoiActivity.this,
				android.R.layout.simple_spinner_item,
				PoiClassify.fourthStrs[0][0][0]);
		fourthSpinner.setAdapter(fourthAdapter);
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
						cityAdapter = new ArrayAdapter<String>(
								WPoiActivity.this,
								android.R.layout.simple_spinner_item,
								PoiClassify.city[position]);

						// 设置二级下拉列表的选项内容适配器
						citySpinner.setAdapter(cityAdapter);
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
						countyAdapter = new ArrayAdapter<String>(
								WPoiActivity.this,
								android.R.layout.simple_spinner_item,
								PoiClassify.county[provincePosition][position]);
						countySpinner.setAdapter(countyAdapter);
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
						fourthAdapter = new ArrayAdapter<String>(
								WPoiActivity.this,
								android.R.layout.simple_spinner_item,
								PoiClassify.fourthStrs[provincePosition][cityPosition][position]);
						fourthSpinner.setAdapter(fourthAdapter);
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

		final TextView poiInfoTxt = (TextView) layout
				.findViewById(R.id.classify_info);
		final EditText name = (EditText) layout.findViewById(R.id.poi_name);
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
				String cate = poiInfoTxt.getText().toString();
				if (cate.contains(poi)) {
					DTUIUtils.showToastSafe("不能重复添加分类");
				} else {
					cate += poi;
				}
				poiInfoTxt.setText(cate);
			}
		});
		deletePoiBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String cate = poiInfoTxt.getText().toString();
				if (!DTStringUtils.isEmpty(cate)) {
					cate = cate.substring(0, cate.lastIndexOf(";"));
					poiInfoTxt.setText(cate.substring(0,
							cate.lastIndexOf(";") + 1));
				}
			}
		});
		new AlertDialog.Builder(this)
				.setTitle("POI分类采集")
				.setView(layout)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								RMPoi point = new RMPoi();
								point.setX(mMapView.getCenter().getX());
								point.setY(mMapView.getCenter().getY());
								point.setName(name.getText().toString());
								point.setDesc(poiInfoTxt.getText().toString());
								mPointLayer.addPoint(point);
								exportPOIData();
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.show();
	}

	/**
	 * 删除POI点
	 * 
	 * @param poi
	 */
	private void deletePoi(final RMPoi poi) {
		AlertDialog.Builder builder = new Builder(WPoiActivity.this);
		builder.setMessage("确认删除吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				mPointLayer.clearPoint(poi);
				exportPOIData();
				mMapView.refreshMap();
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {

			}
		});

		builder.create().show();
	}

	/**
	 * 删除最后一个点的数据
	 */
	private void deleteLastPOI() {
		if (mPointLayer.getPointCount() != 0) {
			RMPoi poi = mPointLayer.getPoint(mPointLayer.getPointCount() - 1);
			if (poi != null) {
				mPointLayer.clearPoint(poi);
				exportPOIData();
				mMapView.refreshMap();
			}
		}
	}

	/**
	 * 导出数据
	 */
	private synchronized void exportPOIData() {
		new DTAsyncTask(new DTCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				ArrayList<RMPoi> pois = mPointLayer.getPointList();
				File file = new File(rootPath + mMapName + ".poi");
				if (pois == null || pois.size() == 0) {
					file.delete();
					return null;
				}
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
				return null;
			}

			@Override
			public void onCallBackFinish(Object obj) {

			}
		}).execute();
	}

	@Override
	public void onClick(RMPoi point, String key) {
		deletePoi(point);
	}

	@Override
	public void onClick(NavigatePoint point, String key) {

	}

}
