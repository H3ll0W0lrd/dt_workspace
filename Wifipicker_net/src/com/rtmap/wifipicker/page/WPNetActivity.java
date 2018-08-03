package com.rtmap.wifipicker.page;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
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
import com.rtmap.wifipicker.core.DTAsyncTask;
import com.rtmap.wifipicker.core.DTCallBack;
import com.rtmap.wifipicker.core.WPApplication;
import com.rtmap.wifipicker.core.model.RMPoi;
import com.rtmap.wifipicker.layer.OnPointClickListener;
import com.rtmap.wifipicker.layer.RouteLayer;
import com.rtmap.wifipicker.util.Constants;
import com.rtmap.wifipicker.util.DTLog;
import com.rtmap.wifipicker.util.DTMathUtils;
import com.rtmap.wifipicker.util.DTUIUtils;
import com.rtmap.wifipicker.util.FileHelper;
import com.rtmap.wifipicker.util.FileUtil;
import com.rtmap.wifipicker.util.ImgUtil;
import com.rtmap.wifipicker.wifi.NetGather;
import com.rtmap.wifipicker.wifi.NetGatherData;
import com.rtmap.wifipicker.wifi.NetGatherTask.CompleteListener;
import com.rtmap.wifipicker.wifi.UIEvent;

/**
 * 网络采集
 * 
 * @author dingtao
 *
 */
public class WPNetActivity extends WPBaseActivity implements
		OnPointClickListener, OnClickListener {

	private static final float MARK_DISTANCE_THRESHOLD = 8f; // 两次标记点距离最大值
	private static final long MARK_TIME_MIN_THRESHOLD = 4; // 两次标记点的时间差最小值(s)
	private static final long MARK_TIME_MAX_THRESHOLD = 20; // 两次标记点时间差最大值(s)

	private int markDisBeyondLimitTimes = 0; // 距离越界次数
	private int markTimeBeyondLimitTimes = 0; // 时间越界次数
	private long mLastMarkPointTime = 0; // 最近一次标记点的时间(ms)

	private static final int MESSSAGE_TIME = 2204;

	private MapView mMapView;// mapView
	private RouteLayer mRoutelayer;// 路线layer
	private ArrayList<NavigatePoint> mMarkList;// 标记点数组
	private static final float MARK_ADD_DISTANCE = 0.001f;// 当两个点位置坐标一样，增加1毫米做区别

	private Button mStartBtn;// 开始采集
	private Button mBack;// 返回地图选择界面
	private Button mMarkBtn;// 标记

	private TextView mTitle;// 标题栏，显示采集地图的名称
	private TextView mTextStatus;// 显示信息
	private TextView mTextGatherRet;// 其他显示信息
	private ImageView mImageNetStatus;// 服务器是否连接标志
	private int count;
	private SharedPreferences sharedPreferences = WPApplication.getInstance()
			.getShare();
	
	private static final int IMPORT_HISTORY = 111;// 加载历史

	private String mMapName;
	private Floor mFloor;

	private String promptStr = "";

	private int serverType = 0; // 0:window 1:linux
	private String rootPath;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSSAGE_TIME:
				count--;
				if (count > 0) {
					mTextStatus.setText(getString(R.string.surplus_second,
							count));
					mHandler.sendEmptyMessageDelayed(MESSSAGE_TIME, 1000);
				} else {
					stop();
				}
				break;
			case IMPORT_HISTORY:
				showLoad();
				importHistoryData();
				break;
			}
			super.handleMessage(msg);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gather_activity_4);
		mDialogLoad.setCancelable(false);
		mDialogLoad.setCanceledOnTouchOutside(false);
		mFloor = (Floor) getIntent().getExtras().getSerializable(
				Constants.EXTRA_FLOOR);
		rootPath = Constants.WIFI_PICKER_PATH + File.separator + mUserName
				+ File.separator + mFloor.getBuildid() + File.separator + "net"
				+ File.separator;
		FileUtil.checkDir(rootPath);
		mMarkList = new ArrayList<NavigatePoint>();
		count = Integer.parseInt(sharedPreferences.getString("net_time", "60"));
		init();
		NetGather.GetInstance();
	}

	@Override
	protected void onResume() {
		super.onResume();
		UIEvent.getInstance().register(mHandler);
	}

	@Override
	protected void onPause() {
		super.onPause();
		UIEvent.getInstance().remove(mHandler);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.clearMapLayer();
		UIEvent.getInstance().remove(mHandler);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() != KeyEvent.ACTION_DOWN) {
			return false;
		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mStartBtn.getText().toString().equals(getString(R.string.stop))) {
				stop();
				return true;
			}
			// exportData();
			finish();
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (mStartBtn.getText().toString().equals(getString(R.string.stop))) {
				mark();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "设置");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST + 1:
			Intent intent = new Intent(this, WPSettingActivity.class);
			startActivity(intent);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void directionBtnListener() {
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

	private void initBtn() {
		directionBtnListener();

		mBack = (Button) findViewById(R.id.btn_back);
		mMarkBtn = (Button) findViewById(R.id.btn_mark);
		mStartBtn = (Button) findViewById(R.id.btn_start);
		findViewById(R.id.btn_pause).setVisibility(View.GONE);
		findViewById(R.id.btn_turn_around).setVisibility(View.GONE);

		mBack.setOnClickListener(this);
		mStartBtn.setOnClickListener(this);
		mMarkBtn.setOnClickListener(this);
		mStartBtn.setEnabled(true);
		mMarkBtn.setEnabled(false);
	}

	private void initIndicator() {
		mTextStatus = (TextView) findViewById(R.id.tv_ap_status);// 采集基本信息，如点个数、ap、macs等
		mTextGatherRet = (TextView) findViewById(R.id.tv_other);// 采集的其他信息
		mImageNetStatus = (ImageView) findViewById(R.id.image_net_status);// 是否连接上服务器的指示灯
		mImageNetStatus.setImageResource(R.drawable.status_red);// 红色连接失败
		mTitle = (TextView) findViewById(R.id.tv_map_name);// 标题栏显示采集地图名称
	}

	private void init() {
		String serverTypeStr = WPApplication.getInstance().getShare()
				.getString("server_type", "0");
		serverType = Integer.parseInt(serverTypeStr);
		initBtn();
		initIndicator();
		// 根据ID获取地图名
		mFloor = (Floor) getIntent().getExtras().getSerializable(
				Constants.EXTRA_FLOOR);
		mMapName = mFloor.getBuildid() + "-" + mFloor.getFloor();

		NetGatherData.sUserName = WPApplication.getInstance().getShare()
				.getString("tag1", "test");

		String[] infos = mMapName.split("-");
		String buildId = infos[0];
		String floor = infos[1];
		BuildSession.getInstance().setBuildId(mFloor.getBuildid());// 初始化全局的mapname，在其他地方用到，例如database存储
		BuildSession.getInstance().setFloor(mFloor.getFloor());

		mTitle.setText("网络采集");
		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		mMapView.setUpdateMap(false);
		mMapView.setDoubleTapable(false);
		mMapView.initMapConfig(buildId, floor);// 打开地图（建筑物id，楼层id）
		mMapView.initScale();// 初始化比例尺
		// 初始化起终点
		Drawable drawable = getResources().getDrawable(R.drawable.sign_red);
		Bitmap startIcon = ImgUtil.drawableToBitmap(drawable);// 起点图片
		drawable = getResources().getDrawable(R.drawable.sign_green);
		Bitmap endIcon = ImgUtil.drawableToBitmap(drawable);// 终点图片

		mRoutelayer = new RouteLayer(mMapView, startIcon, endIcon, endIcon);// 路线数组

		CompassLayer mCompassLayer = new CompassLayer(mMapView);// 指南针图层
		mMapView.addMapLayer(mCompassLayer);
		mMapView.addMapLayer(mRoutelayer);
		mRoutelayer.setOnPointClickListener(this);

//		importHistoryData();// 导入历史数据
		mHandler.sendEmptyMessageDelayed(IMPORT_HISTORY, 600);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			stop();
			finish();
			break;
		case R.id.btn_start:
			if (mStartBtn.getText().toString()
					.equals(getString(R.string.title_gather))) {
				start(serverType);
			} else {
				count = Integer.parseInt(sharedPreferences.getString(
						"net_time", "60"));
				mHandler.removeMessages(MESSSAGE_TIME);
				stop();
			}
			break;
		case R.id.btn_mark:
			mark();
			break;
		}
	}

	String filePath;
	long time;

	private void start(int server) {
		long t = System.currentTimeMillis();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String d = format.format(new Date(t)).replaceAll("-", "T");
		String filename = mMapName + "-0_"
				+ sharedPreferences.getString("tag1", "000000000000") + d;
		filePath = rootPath + filename + "_" + "mark1.csv";
		if (server == 0) {
			NetGather.GetInstance().start(new CompleteListener() {
				@Override
				public void onComplete(String result) {
					handleResult(result);
				}
			}, filename, mMapName + "-0");
		} else if (server == 1) {
			NetGather.GetInstance().start(new CompleteListener() {
				@Override
				public void onComplete(String result) {
					handleResult(result);
				}
			}, filename, mMapName + "-0");
		}
	}

	/**
	 * 处理结果
	 * 
	 * @param result
	 */
	private void handleResult(String result) {
		DTLog.i("net-result: " + result);
		if ("gather:gather_data_offline:1.0 A".equals(result)) {
			mMarkBtn.setEnabled(true);
			mStartBtn.setText(R.string.stop);
			showOtherInfo("start sucess");
			mImageNetStatus.setImageResource(R.drawable.status_green);
			mHandler.sendEmptyMessage(MESSSAGE_TIME);
			time = System.currentTimeMillis();
			mMarkList = new ArrayList<NavigatePoint>();
			mark();
		} else {
			showOtherInfo("sever return error,start fail");
			mImageNetStatus.setImageResource(R.drawable.status_red);
		}
	}

	/**
	 * 添加Point
	 */
	private void addPoint(float x, float y) {
		if (mMarkList.size() > 0) {
			NavigatePoint p = mMarkList.get(mMarkList.size() - 1);
			if (p.getX() == x && p.getY() == y) {
				x += MARK_ADD_DISTANCE;
				y += MARK_ADD_DISTANCE;
			}
		}
		NavigatePoint mMarkPoint = new NavigatePoint();
		mMarkPoint.setX(x);
		mMarkPoint.setY(y);
		mMarkPoint.setFloor(mMapView.getFloor());
		if (mMarkList.size() == 0) {// 说明新建了一条采集路线
			mRoutelayer.addRoute(filePath, mMarkList);
		}
		mMarkList.add(mMarkPoint);
		mMapView.refreshMap();
	}

	/**
	 * 标记
	 * 
	 * @param server
	 */
	private void mark() {
		promptStr = "";
		final float milliX = mMapView.getCenter().getX();
		final float milliY = mMapView.getCenter().getY();
		// 两次采集点距离和时间限制
		promptStr = distanceLimit(promptStr);
		promptStr = timeLimit(promptStr);
		addPoint(milliX, milliY);
		savePoint();// 文件写点
	}

	/**
	 * 距离提示语
	 * 
	 * @param promptStr
	 * @return
	 */
	private String distanceLimit(String promptStr) {
		if (mMarkList.size() > 1) {
			NavigatePoint p1 = mMarkList.get(mMarkList.size() - 1);
			NavigatePoint p2 = mMarkList.get(mMarkList.size() - 2);
			if (DTMathUtils
					.distance(p1.getX(), p1.getY(), p2.getX(), p2.getY()) > MARK_DISTANCE_THRESHOLD) {
				promptStr += "两次标记距离应小于" + MARK_DISTANCE_THRESHOLD + "米,累计违规"
						+ (++markDisBeyondLimitTimes) + "次\n";
			}
		}
		return promptStr;
	}

	private String timeLimit(String promptStr) {
		if (mLastMarkPointTime != 0) {
			long delay = System.currentTimeMillis() - mLastMarkPointTime;
			mLastMarkPointTime = System.currentTimeMillis();
			if (delay < MARK_TIME_MIN_THRESHOLD * 1000
					|| delay > MARK_TIME_MAX_THRESHOLD * 1000) {
				promptStr += "两次标记时间应在[" + MARK_TIME_MIN_THRESHOLD + ","
						+ MARK_TIME_MAX_THRESHOLD + "]s之间,累计违规"
						+ (++markTimeBeyondLimitTimes) + "次\n";
			}
		} else {
			mLastMarkPointTime = System.currentTimeMillis();
		}
		return promptStr;
	}

	private void stop() {
		count = Integer.parseInt(sharedPreferences.getString("net_time", "60"));
		mTextStatus.setText("");
		markDisBeyondLimitTimes = 0; // 距离越界次数清0
		markTimeBeyondLimitTimes = 0;// 时间越界次数清0
		mLastMarkPointTime = 0;
		showOtherInfo("stop sucess");
		mMarkBtn.setEnabled(false);
		mStartBtn.setText(R.string.title_gather);
		// importHistoryData();
	}

	private TextView mDeletePointText;

	/**
	 * 删除路线上的点
	 * 
	 * @param x
	 * @param y
	 */
	private void delete(final String key) {
		final AlertDialog.Builder build = new Builder(this);
		build.setTitle("提示");
		build.setPositiveButton("删除", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {

				FileUtil.deleteFile(key);
				mRoutelayer.removeRoute(key);
				mMapView.refreshMap();
				// importHistoryData();
			}
		});
		View layout = DTUIUtils.inflate(R.layout.gather_data_delete_dialog);// 删除弹出框
		mDeletePointText = (TextView) layout.findViewById(R.id.et_point_list);// 显示所有点的ID
		final ArrayList<NavigatePoint> pointlist = mRoutelayer.getRoute(key);
		setDeleteText(pointlist);// 重新设置文字
		build.setView(layout);
		build.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
			}
		});
		final AlertDialog dialog = build.create();
		layout.findViewById(R.id.btn_delete_last).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if (pointlist.size() > 1) {// 路线移除一个点，index改变
							pointlist.remove(pointlist.size() - 1);
							setDeleteText(pointlist);// 重新设置文字
							InputStream in = null;
							DTLog.e(key);
							File file = new File(key);
							try {
								in = new BufferedInputStream(
										new FileInputStream(file));

								BufferedReader br = new BufferedReader(
										new InputStreamReader(in));
								String tmp = br.readLine() + "\n";
								for (int i = 0; i < pointlist.size(); i++) {
									tmp += br.readLine() + "\n";
								}
								br.close();
								in.close();
								BufferedWriter bw = new BufferedWriter(
										new OutputStreamWriter(
												new BufferedOutputStream(
														new FileOutputStream(
																file))));
								bw.write(tmp);
								bw.flush();
								bw.close();
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								file.delete();
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							dialog.cancel();
							FileUtil.deleteFile(key);
							mRoutelayer.removeRoute(key);
							// importHistoryData();
						}
						mMapView.refreshMap();
					}
				});

		dialog.show();
	}

	/**
	 * 设置删除文案
	 * 
	 * @param pointlist
	 */
	private void setDeleteText(ArrayList<NavigatePoint> pointlist) {
		String index = "";
		for (int i = 0; i < pointlist.size(); i++) {
			if (i == 0) {
				index += i;
			} else
				index += ("-" + i);
		}
		mDeletePointText.setText(index);
	}

	private void showOtherInfo(String info) {
		mTextGatherRet.setText(info);
	}

	private void savePoint() {
		if (mMarkList.size() == 0) {
			return;
		}
		NavigatePoint p = mMarkList.get(mMarkList.size() - 1);
		// 时间(ms) 建筑物id 楼层id x坐标(mm) y坐标(mm)
		if (mMarkList.size() == 1) {
			FileUtil.fstream(filePath,
					"time(ms)\tbuildId\tfloor\tx(mm)\ty(mm)\n");
		}
		String[] infos = mMapName.split("-");
		FileUtil.fstream(filePath,
				System.currentTimeMillis() - time + "\t" + infos[0] + "\t"
						+ floorTransform(infos[1]) + "\t"
						+ (int) (p.getX() * 1000) + "\t"
						+ (int) (p.getY() * 1000) + "\n");
	}

	/**
	 * 楼层换算
	 * 
	 * @param floor
	 * @return
	 */
	public static int floorTransform(String floor) {
		int a = 0;
		String str1 = floor.substring(0, 1);
		if (floor.contains(".5")) {
			a += Integer.parseInt(floor.substring(1, floor.indexOf("."))) * 10 + 5;
		} else {
			a += Integer.parseInt(floor.substring(1)) * 10;
		}

		if ("B".equals(str1)) {
			a += 10000;
		} else if ("F".equals(str1)) {
			a += 20000;
		}
		return a;
	}

	/**
	 * 楼层换算:10000是B,20000是F,剩下的数是楼层的10倍
	 * 
	 * @param floor
	 * @return
	 */
	public static String floorTransform(int floor) {
		String a = null;
		if (floor / 10000 == 1)
			a = "B";
		else
			a = "F";
		int f = floor % 10;// 看有没有半层
		if (f != 0)
			a += floor % 10000 / 10f;
		else
			a += floor % 10000 / 10;
		return a;
	}

	/**
	 * 导入数据
	 */
	private void importHistoryData() {
		new DTAsyncTask(new DTCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				mRoutelayer.clearAllRoute();// 清除所有路线
				String[] files_roadnet = FileHelper.listFiles(rootPath,
						new FilenameFilter() {

							@Override
							public boolean accept(File dir, String filename) {
								if (filename.contains(mMapName + "-0")
										&& filename.endsWith(".csv")) {
									return true;
								}
								return false;
							}
						});

				if (files_roadnet != null) {
					int sizeOfFiles = files_roadnet.length;
					for (int i = 0; i < sizeOfFiles; i++) {
						InputStream in = null;
						String path = String.format("%s%s", rootPath,
								files_roadnet[i]);
						DTLog.e(path);
						File file = new File(path);
						try {
							in = new BufferedInputStream(new FileInputStream(
									file));
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}

						BufferedReader br = new BufferedReader(
								new InputStreamReader(in));
						String tmp = null;
						try {
							// FileUtil.fstream(filePath,
							// "time\tbuildId\tfloor\tx\ty\n");
							ArrayList<NavigatePoint> points = new ArrayList<NavigatePoint>();
							br.readLine();
							while (((tmp = br.readLine()) != null)
									&& (tmp.length() > 1)) {
								String[] point = tmp.split("\t");
								NavigatePoint roadnetPoint = new NavigatePoint();
								roadnetPoint.setX(Integer.parseInt(point[3]) / 1000.0f);
								roadnetPoint.setY(Integer.parseInt(point[4]) / 1000.0f);
								roadnetPoint.setFloor(mMapView.getFloor());
								points.add(roadnetPoint);
							}
							mRoutelayer.addRoute(path, points);
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

				}
				return null;
			}

			@Override
			public void onCallBackFinish(Object obj) {
				mMapView.refreshMap();
				hideLoad();
			}
		}).run();
	}

	@Override
	public void onClick(NavigatePoint point, String key) {
		if (mStartBtn.getText().toString()
				.equals(getString(R.string.title_gather))) {
			delete(key);
		}
	}

	@Override
	public void onClick(RMPoi point, String key) {

	}

}
