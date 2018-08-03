package com.rtmap.wifipicker.page;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Floor;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.PointInfo;
import com.rtmap.wifipicker.BuildSession;
import com.rtmap.wifipicker.R;
import com.rtmap.wifipicker.core.model.RMPoi;
import com.rtmap.wifipicker.data.Terminal;
import com.rtmap.wifipicker.layer.OnPointClickListener;
import com.rtmap.wifipicker.layer.RouteLayer;
import com.rtmap.wifipicker.util.Constants;
import com.rtmap.wifipicker.util.DTLog;
import com.rtmap.wifipicker.util.FileHelper;
import com.rtmap.wifipicker.util.FileUtil;
import com.rtmap.wifipicker.util.ImgUtil;
import com.rtmap.wifipicker.util.TerminalHelper;
import com.rtmap.wifipicker.widget.RoadNetLayer;

public class WPRoadNetActivity extends WPBaseActivity implements
		OnPointClickListener {

	private MapView mMapView;// mapView
	private String mMapName;// 地图name,由MapSelectActivity传入
	private RouteLayer mRoutelayer;

	private TextView mTitle;// 标题栏，显示采集地图的名称

	private Button exportBtn;// 开始采集
	private Button backBtn;// 返回地图选择界面
	private Button delBtn;// 删除
	private Button markBtn;// 标记
	private int delIndex = -1;// 删除文件索引
	private ArrayList<NavigatePoint> mMarkList;// 标记点数组
	private static final float MARK_ADD_DISTANCE = 0.001f;// 当两个点位置坐标一样，增加1毫米做区别
	private Floor mFloor;
	private String rootPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rm_roadnet_collect_activity);
		mFloor = (Floor) getIntent().getExtras().getSerializable(
				Constants.EXTRA_FLOOR);
		rootPath = Constants.WIFI_PICKER_PATH + File.separator + mUserName
				+ File.separator + mFloor.getBuildid() + File.separator;
		FileUtil.checkDir(rootPath);
		mMarkList = new ArrayList<NavigatePoint>();
		init();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.clearMapLayer();
	}

	@Override
	public void onBackPressed() {
		WPRoadNetActivity.this.finish();
	}

	private void init() {
		initContrls();
		mFloor = (Floor) getIntent().getExtras().getSerializable(
				Constants.EXTRA_FLOOR);
		// 根据ID获取地图名
		mMapName = mFloor.getBuildid() + "-" + mFloor.getFloor();// 地图name,由MapSelectActivity传入

		BuildSession.getInstance().setBuildId(mFloor.getBuildid());// 初始化全局的mapname，在其他地方用到，例如database存储
		BuildSession.getInstance().setFloor(mFloor.getFloor());

		mTitle.setText("路网采集");

		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		mMapView.setUpdateMap(false);
		mMapView.setDoubleTapable(false);
		mMapView.initMapConfig(mFloor.getBuildid(), mFloor.getFloor());// 打开地图（建筑物id，楼层id）
		mMapView.initScale();// 初始化比例尺
		// 初始化起终点
		Drawable drawable = getResources().getDrawable(R.drawable.sign_red);
		Bitmap startBmp = ImgUtil.drawableToBitmap(drawable);// 起点图片
		drawable = getResources().getDrawable(R.drawable.sign_green);
		Bitmap endBmp = ImgUtil.drawableToBitmap(drawable);// 终点图片

		mRoutelayer = new RouteLayer(mMapView, startBmp, endBmp, endBmp);// 路线数组
		mRoutelayer.setOnPointClickListener(this);
		CompassLayer mCompassLayer = new CompassLayer(mMapView);// 指南针图层
		mMapView.addMapLayer(mCompassLayer);
		mMapView.addMapLayer(mRoutelayer);
		showRoadNetHistory();

	}

	private void initContrls() {
		exportBtn = (Button) findViewById(R.id.btn_expore_roadnet);
		backBtn = (Button) findViewById(R.id.btn_back_roadnet);
		delBtn = (Button) findViewById(R.id.btn_del_roadnet);
		markBtn = (Button) findViewById(R.id.btn_mark_roadnet);
		mTitle = (TextView) findViewById(R.id.txt_map_name_roadnet);

		View.OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.btn_expore_roadnet:
					exportRoadNetData();
					break;
				case R.id.btn_back_roadnet:
					WPRoadNetActivity.this.finish();
					break;
				case R.id.btn_del_roadnet:
					if (mMarkList.size() > 0) {
						mMarkList.remove(mMarkList.size() - 1);
						mMapView.refreshMap();
					}
					break;
				case R.id.btn_mark_roadnet:
					mark();
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
	 * 采集路网
	 */
	private void mark() {
		float x = mMapView.getCenter().getX();
		float y = mMapView.getCenter().getY();
		DTLog.e("X :" + x + "  Y: " + y + " listSize:" + mMarkList.size());
		if (mMarkList.size() > 0) {
			NavigatePoint p = mMarkList.get(mMarkList.size() - 1);
			if (p.getX() == x && p.getY() == y) {
				x += MARK_ADD_DISTANCE;
				y += MARK_ADD_DISTANCE;
			}
		}
		NavigatePoint mMarkPoint = new NavigatePoint("", x, y, "",
				mMapView.getFloor(), "", 0);
		if (mMarkList.size() == 0) {// 说明新建了一条采集路线
			String key = x + "" + y;
			mRoutelayer.addRoute(key, mMarkList);
		}
		mMarkList.add(mMarkPoint);
		mMapView.refreshMap();
	}

	/**
	 * 导出路线数据
	 */
	private void exportRoadNetData() {
		if (mMarkList.size() > 0) {
			String path = String.format("%s%s_%d_%d_%d.roadnet", rootPath,
					mMapName, System.currentTimeMillis(),
					(int) (mMarkList.get(0).getX() * 1000), (int) (mMarkList
							.get(0).getY() * 1000));
			for (int i = 0; i < mMarkList.size(); i++) {
				FileUtil.fstream(path, String.format("%d\t%f\t%f\t\n", i + 1,
						mMarkList.get(i).getX(), mMarkList.get(i).getY()));
			}
			mMarkList.clear();
			mRoutelayer.clearAllRoute();
			showRoadNetHistory();
		}

	}

	/**
	 * 导入历史记录
	 */
	private void showRoadNetHistory() {
		String[] files_roadnet = FileHelper.listFiles(rootPath,
				new FilenameFilter() {

					@Override
					public boolean accept(File dir, String filename) {
						if (filename.contains(mMapName)
								&& (filename.endsWith(".roadnet") || filename
										.endsWith(".roadnet.uploaded"))) {
							return true;
						}
						return false;
					}
				});

		if (files_roadnet != null) {
			int sizeOfFiles = files_roadnet.length;
			for (int i = 0; i < sizeOfFiles; i++) {
				InputStream in = null;
				String path = String.format("%s%s", rootPath, files_roadnet[i]);
				DTLog.e(path);
				File file = new File(path);
				try {
					in = new BufferedInputStream(new FileInputStream(file));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));
				String tmp = null;
				try {
					ArrayList<NavigatePoint> points = new ArrayList<NavigatePoint>();
					while (((tmp = br.readLine()) != null)
							&& (tmp.length() > 1)) {
						String[] point = tmp.split("\t");
						NavigatePoint roadnetPoint = new NavigatePoint("",
								Float.parseFloat(point[1]),
								Float.parseFloat(point[2]), "",
								mMapView.getFloor(), "",
								RoadNetLayer.POINT_TYPE_ROADNET);
						points.add(roadnetPoint);
					}
					mRoutelayer.addRoute(points.get(0).getX() + ""
							+ points.get(0).getY(), points);
					br.close();
					if (in != null) {
						in.close();
					}
				} catch (IOException e) {
					file.delete();
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mMapView.refreshMap();
		}
	}

	@Override
	public void onClick(NavigatePoint point, String key) {
		// storage/sdcard0/rtmap/WifiPicker//zizxs/860100010040500002-F2-0_1399275551008_370_437.mc
		final String[] files_mc = FileHelper.listFiles(rootPath,
				new FilenameFilter() {
					@Override
					public boolean accept(File dir, String filename) {
						if (filename.contains(mMapName)
								&& filename.endsWith(".roadnet")) {
							return true;
						}
						return false;
					}
				});
		for (int i = 0; i < files_mc.length; i++) {
			ArrayList<NavigatePoint> route = mRoutelayer.getRoute(key);
			DTLog.e("route_path : " + files_mc[i]);
			DTLog.e("x_y : " + (int) (route.get(0).getX() * 1000) + "_"
					+ (int) (route.get(0).getY() * 1000));
			if (files_mc[i].contains((int) (route.get(0).getX() * 1000) + "_"
					+ (int) (route.get(0).getY() * 1000))) {
				delIndex = i;
				break;
			}
		}
		if (delIndex != -1) {
			AlertDialog.Builder build = new Builder(WPRoadNetActivity.this);
			build.setTitle("提示");
			build.setMessage("确认删除吗");
			build.setPositiveButton("确认",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							FileUtil.deleteFile(rootPath + files_mc[delIndex]);
							mRoutelayer.clearAllRoute();
							delIndex = -1;
							showRoadNetHistory();
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

	@Override
	public void onClick(RMPoi point, String key) {
		// TODO Auto-generated method stub

	}

}
