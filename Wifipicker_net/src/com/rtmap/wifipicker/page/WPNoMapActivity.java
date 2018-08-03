package com.rtmap.wifipicker.page;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rtm.common.utils.RMD5Util;
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
import com.rtmap.wifipicker.layer.OnPointClickListener;
import com.rtmap.wifipicker.layer.RouteLayer;
import com.rtmap.wifipicker.util.ConstantLoc.UIEventCode;
import com.rtmap.wifipicker.util.Constants;
import com.rtmap.wifipicker.util.DTFileUtils;
import com.rtmap.wifipicker.util.DTLog;
import com.rtmap.wifipicker.util.DTMathUtils;
import com.rtmap.wifipicker.util.DTStringUtils;
import com.rtmap.wifipicker.util.DTUIUtils;
import com.rtmap.wifipicker.util.FileHelper;
import com.rtmap.wifipicker.util.FileUtil;
import com.rtmap.wifipicker.util.ImgUtil;
import com.rtmap.wifipicker.util.WPDBService;
import com.rtmap.wifipicker.wifi.CollectFingerPoint;
import com.rtmap.wifipicker.wifi.UIEvent;
import com.rtmap.wifipicker.wifi.WifiRecorder;

/**
 * 终端采集与离线数据采集
 * 
 * @author zhengnengyuan
 * 
 */
public class WPNoMapActivity extends WPBaseActivity implements OnClickListener,
		OnPointClickListener {

	private static final float MARK_DISTANCE_THRESHOLD = 8f; // 两次标记点距离最大值
	private static final long MARK_TIME_MIN_THRESHOLD = 4; // 两次标记点的时间差最小值(s)
	private static final long MARK_TIME_MAX_THRESHOLD = 20; // 两次标记点时间差最大值(s)
	private static final long TURN_AROUND_DELAY = 3; // 折返延时(s)
	private static final long GATHER_TIME_THRESHOLD = 1200; // 单向采集时间限制(s)
	private static final float MARK_ADD_DISTANCE = 0.001f;// 当两个点位置坐标一样，增加1毫米做区别

	private int markDisBeyondLimitTimes = 0; // 距离越界次数
	private int markTimeBeyondLimitTimes = 0; // 时间越界次数
	private long mLastMarkPointTime = 0; // 最近一次标记点的时间(ms)
	private long gatherStartTime = 0; // 开始采集时间

	private static final int MESSAGE_GATHER_ON_WALK = 1101;
	private static final int MESSAGE_SHOW_OTHER_INFO = 1106;
	private static final int MESSAGE_EXPORT_DATA_REMIND = 1108;

	private Floor mFloor;
	private String mMapName;

	private MapView mMapView;// mapView
	private RouteLayer mRoutelayer;// 路线layer
	private String mKey;// 标记key
	private ArrayList<NavigatePoint> mMarkList;// 标记点数组

	private Button mStartBtn;// 开始采集
	private Button mBack;// 返回地图选择界面
	private Button mMarkBtn;// 标记
	private Button mPauseBtn; // 暂停采集
	private TextView mTitle;// 标题栏，显示采集地图的名称
	private TextView mTextStatus;// AP信息
	private TextView mTextGatherRet;// 其他显示信息
	private ImageView mImageNetStatus;// 服务器是否连接标志

	private CollectFingerPoint mFingerCollect;

	private SQLiteDatabase dataBase4Delete; // 待删除数据库文件
	private TextView mDeletePointText;

	private String retInfo = "";// 其他显示信息

	private static final String ILLEGAL_COMMON_MARK_COORD = "1"; // 标记坐标不合法
	// private static final String ILLEGAL_TURN_AROUND_MARK_COORD = "2";//
	// 折返标记坐标不合法
	private static final String ILLEGAL_MARK_TIME = "3";// 标记时间不合法
	private static final String ILLEGAL_COLLECTE_TIME_DELAY = "4";// 单次采集时间不合法

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UIEventCode.WIFI_SCAN_END:
				mTextStatus.setText("points:" + mMarkList.size() + ", aps:"
						+ msg.arg1);
				break;
			case UIEventCode.NO_WIFI_SIGNAL_REMINDER:
				showToast("WIFI信号异常，WIFI重启中...", Toast.LENGTH_SHORT);
				stop();
				break;
			case MESSAGE_GATHER_ON_WALK:
				gather();
				break;
			case MESSAGE_SHOW_OTHER_INFO:
				mTextGatherRet.setText(retInfo);
				break;
			case MESSAGE_EXPORT_DATA_REMIND:
				String remindStr = "数据导出中。。。请勿退出";
				if (msg.arg1 == 1) {
					remindStr = "数据导出完毕";
				}
				showToast(remindStr, Toast.LENGTH_SHORT);
				break;
			}
		}

	};
	private String rootPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gather_activity_4);
		mFloor = (Floor) getIntent().getExtras().getSerializable(
				Constants.EXTRA_FLOOR);
		rootPath = Constants.WIFI_PICKER_PATH + File.separator + mUserName
				+ File.separator + mFloor.getBuildid() + File.separator;
		FileUtil.checkDir(rootPath);

		mFloor = (Floor) getIntent().getExtras().getSerializable(
				Constants.EXTRA_FLOOR);
		mMapName = mFloor.getBuildid() + "-" + mFloor.getFloor() + "-0";

		mFingerCollect = new CollectFingerPoint();
		mMarkList = new ArrayList<NavigatePoint>();

		initBtn();
		initIndicator();
		BuildSession.getInstance().setBuildId(mFloor.getBuildid());// 初始化全局的mapname，在其他地方用到，例如database存储
		BuildSession.getInstance().setFloor(mFloor.getFloor());
		mTitle.setText("无图采集");

		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		mMapView.setUpdateMap(false);
		mMapView.setDoubleTapable(false);
		new DTAsyncTask(new LoadMapCall()).run();

		UIEvent.getInstance().register(mHandler);
		WifiRecorder.getInstance().onResume();
	}

	/**
	 * 加载地图
	 * 
	 * @author dingtao
	 *
	 */
	class LoadMapCall implements DTCallBack {

		@Override
		public Object onCallBackStart(Object... obj) {
			String vector_path = Constants.MAP_DATA
					+ RMD5Util.md5("dingtao_F1.imap");// /mnt/sdcard/rtmap/mdata/860100010040500002_F2.imap
			DTLog.e("woqu : " + vector_path);
			File file = new File(vector_path);
			if (!file.exists()) {
				try {
					DTFileUtils.assetsDataToSD(getApplicationContext(),
							RMD5Util.md5("dingtao_F1.imap"), vector_path);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			mMapView.initMapConfig("dingtao", "F1");// 打开地图（建筑物id，楼层id）
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
			mRoutelayer.setOnPointClickListener(WPNoMapActivity.this);

			importHistoryData();// 导入历史数据
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {

		}
	}

	/**
	 * 初始化地图操作按钮
	 */
	private void initBtn() {
		directionBtnListener();
		mBack = (Button) findViewById(R.id.btn_back);// 返回
		mMarkBtn = (Button) findViewById(R.id.btn_mark);// 标记
		mStartBtn = (Button) findViewById(R.id.btn_start);// 开始或者停止录制按钮
		mPauseBtn = (Button) findViewById(R.id.btn_pause);// 暂停或者继续
		findViewById(R.id.btn_turn_around).setVisibility(View.GONE);;// 折返

		mBack.setOnClickListener(this);
		mStartBtn.setOnClickListener(this);
		mMarkBtn.setOnClickListener(this);
		mPauseBtn.setOnClickListener(this);

		mStartBtn.setEnabled(true);
		mMarkBtn.setEnabled(false);
		mPauseBtn.setEnabled(false);
	}

	private void initIndicator() {
		mTextStatus = (TextView) findViewById(R.id.tv_ap_status);// 采集基本信息，如点个数、ap、macs等
		mTextGatherRet = (TextView) findViewById(R.id.tv_other);// 采集的其他信息
		mImageNetStatus = (ImageView) findViewById(R.id.image_net_status);// 是否连接上服务器的指示灯
		mImageNetStatus.setImageResource(R.drawable.status_green);// 终端采集无需实时连接服务器
		mTitle = (TextView) findViewById(R.id.tv_map_name);// 标题栏显示采集地图名称
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:// 返回
			v.setBackgroundColor(Color.RED);
			stop();
			WPNoMapActivity.this.finish();
			break;
		case R.id.btn_start:// 开始或者停止按钮
			if (mStartBtn.getText().toString()
					.equals(getString(R.string.title_gather))) {
				start();
			} else {
				stop();
			}
			break;
		case R.id.btn_mark:// 标记按钮
			mark();
			break;
		case R.id.btn_pause:// 暂停或者继续按钮
			if (mPauseBtn.getText().toString().equals("暂停")) {
				WifiRecorder.getInstance().onPause();
				UIEvent.getInstance().remove(mHandler);
				mPauseBtn.setText("继续");
				mMarkBtn.setEnabled(false);
			} else {
				mMapView.refreshMap();
				UIEvent.getInstance().register(mHandler);
				WifiRecorder.getInstance().onResume();
				mPauseBtn.setText("暂停");
				if (mStartBtn.getText().toString()
						.equals(getString(R.string.stop))) {
					mMarkBtn.setEnabled(true);
				}
			}
			break;
		case R.id.btn_turn_around:
			gatherStartTime = System.currentTimeMillis();
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() != KeyEvent.ACTION_DOWN) {
			return false;
		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (WifiRecorder.getInstance().isGathering()
					|| mStartBtn.getText().toString()
							.equals(getString(R.string.stop))) {
				stop();
				return true;
			}
			WPNoMapActivity.this.finish();
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
		menu.add(Menu.NONE, Menu.FIRST + 2, 2, "导出");
		menu.add(Menu.NONE, Menu.FIRST + 3, 3, "恢复");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST + 1:
			Intent intent = new Intent(this, WPSettingActivity.class);
			startActivity(intent);
			break;
		case Menu.FIRST + 2:
			export();
			break;
		case Menu.FIRST + 3:
			String[] exportDbFiles = FileHelper.listFiles(rootPath,
					new FilenameFilter() {
						@Override
						public boolean accept(File dir, String filename) {
							if (filename.endsWith(".db.export")
									&& filename.startsWith(mMapName)) {
								return true;
							}
							return false;
						}
					});
			if (exportDbFiles != null && exportDbFiles.length > 0) {
				for (int i = 0; i < exportDbFiles.length; i++) {
					String exportDbPath = rootPath + exportDbFiles[i];
					String renamePath = exportDbPath.substring(0,
							exportDbPath.lastIndexOf("."));
					File exportDbFile = new File(exportDbPath);
					exportDbFile.renameTo(new File(renamePath));
				}
				importHistoryData();
			}
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	String idStr;// 记录显示的ID

	/**
	 * 删除路线上的点
	 * 
	 * @param x
	 * @param y
	 */
	private void delete(String path) {
		HashMap<String, SQLiteDatabase> dataMap = new HashMap<String, SQLiteDatabase>();
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(path, null);
		dataMap.put(path, db);
		// 目前只读取第一个数据库的数据，多个数据库情况先不做处理
		final String dataName = dataMap.keySet().iterator().next();
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.gather_data_delete_dialog,
				(ViewGroup) findViewById(R.id.gather_data_delete_dialog));// 删除弹出框
		mDeletePointText = (TextView) layout.findViewById(R.id.et_point_list);// 显示所有点的ID
		dataBase4Delete = SQLiteDatabase.openOrCreateDatabase(dataName, null);
		final ArrayList<NavigatePoint> wps = WPDBService.getInstance().getAllPoints(
				dataBase4Delete, mFloor.getFloor());// 查找数据库中所有的点

		idStr = "";
		for (int i = 0; i < wps.size(); i++) {
			if (i == 0) {
				idStr += (i + 1);
			} else {
				NavigatePoint p = wps.get(i);
				if (p.getY() == wps.get(i - 1).getY()
						&& p.getX() == wps.get(i - 1).getX())
					continue;
				idStr += "-" + (i + 1);
			}
		}
		mDeletePointText.setText(idStr);

		layout.findViewById(R.id.btn_delete_last).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if (idStr.length() > 0) {// 保证没有删除完呢
							if (idStr.length() > 1) {
								String lastId = idStr.substring(idStr
										.lastIndexOf("-") + 1);
								idStr = idStr.substring(0,
										idStr.lastIndexOf("-"));
								for (int i = Integer.parseInt(lastId) - 1; i < wps
										.size(); i++) {
									WPDBService.getInstance().deletePointsById(
											dataBase4Delete, Integer
													.parseInt(wps.get(i)
															.getId()));
								}
								mDeletePointText.setText(idStr);
							} else {
								FileUtil.deleteFile(dataName);// 直接删除文件
							}
						}
						importHistoryData();// 重新导入
					}
				});

		AlertDialog.Builder build = new Builder(WPNoMapActivity.this);
		build.setTitle("删除");
		build.setView(layout);
		build.setPositiveButton("全部", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				FileUtil.deleteFile(dataName);
				importHistoryData();// 重新导入
			}
		});
		build.setNegativeButton("返回", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.cancel();
			}
		});
		build.create().show();
	}

	/**
	 * 初始化上下左右按钮
	 */
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

	/**
	 * 开始标记
	 */
	private void start() {
		String srcPath = String.format("%s%s", Constants.WIFI_PICKER_PATH,
				Constants.DATABASE_NAME);// 缓存打点数据库路径
		FileHelper.deleteFile(srcPath);
		gatherStartTime = System.currentTimeMillis();
		mMarkBtn.setEnabled(true);
		mPauseBtn.setEnabled(true);
		mStartBtn.setText(R.string.stop);
		WifiRecorder.getInstance().onResume();
		gather();
		showOtherInfo("gather start");
	}

	/**
	 * 采集坐标点
	 */
	private void gather() {
		if (mMarkList.size() != 0) {
			mFingerCollect.sPointX = mMarkList.get(mMarkList.size() - 1).getX();
			mFingerCollect.sPointY = mMarkList.get(mMarkList.size() - 1).getY();
			mFingerCollect.sPointFloor = DTStringUtils.floorTransform(mMapView
					.getFloor());
			mFingerCollect.sPointScanCount = Constants.GATHER_WALK_COUNT;
			mFingerCollect.sPointFingerId = mFingerCollect.onGetPointId();
			WifiRecorder.getInstance().setGatherOnWalk(true);
			WifiRecorder.getInstance().onStart(mFingerCollect);
		}
		mHandler.sendEmptyMessageDelayed(MESSAGE_GATHER_ON_WALK, 1000);
		if (System.currentTimeMillis() - gatherStartTime > GATHER_TIME_THRESHOLD * 1000) {
			showOtherInfo("单次采集超时,请折返！");
		}
	}

	/**
	 * 标记按钮
	 */
	private void mark() {
		// 两次采集点距离不能重合，否则key则替换
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
		NavigatePoint mMarkPoint = new NavigatePoint();
		mMarkPoint.setX(x);
		mMarkPoint.setY(y);
		mMarkPoint.setFloor(mFloor.getFloor());
		if (mMarkList.size() == 0) {// 说明新建了一条采集路线
			mKey = x + "" + y;
			mRoutelayer.addRoute(mKey, mMarkList);
		}
		mMarkList.add(mMarkPoint);
		mMapView.refreshMap();

		// 两次采集点距离和时间限制
		String promptStr = "";
		promptStr = distanceLimit(promptStr);
		promptStr = timeLimit(promptStr);
		showOtherInfo(promptStr);
	}

	/**
	 * 停止标记
	 */
	private void stop() {
		markDisBeyondLimitTimes = 0; // 距离越界次数清0
		markTimeBeyondLimitTimes = 0;// 时间越界次数清0
		mLastMarkPointTime = 0;
		showOtherInfo("gather stop");
		mMarkBtn.setEnabled(false);
		mPauseBtn.setEnabled(false);
		mStartBtn.setText(R.string.title_gather);
		mHandler.removeMessages(MESSAGE_GATHER_ON_WALK);
		WifiRecorder.getInstance().onStop();
		if (mMarkList.size() == 0)
			return;
		String dstPath = saveDatabase();// wifipicker.db备份并清空
		mRoutelayer.removeRoute(mKey);// 移除临时路线
		// 从数据库重新读取
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dstPath, null);
		ArrayList<NavigatePoint> points = WPDBService.getInstance().getAllPoints(db,
				mFloor.getFloor());
		db.close();
		ArrayList<NavigatePoint> markPoints = new ArrayList<NavigatePoint>();
		for (int i = 0; i < points.size(); i++) {
			NavigatePoint wp = new NavigatePoint();
			wp.setX(points.get(i).getX());
			wp.setY(points.get(i).getY());
			wp.setFloor(mFloor.getFloor());
			if (markPoints.size() == 0
					|| !(markPoints.get(markPoints.size() - 1).getX() == wp
							.getX() && markPoints.get(markPoints.size() - 1)
							.getY() == wp.getY())) {
				markPoints.add(wp);
			}
		}
		if (markPoints.size() > 0) {// 添加历史路线
			mRoutelayer.addRoute(dstPath, markPoints);
		}
		mMarkList = new ArrayList<NavigatePoint>();// 将标记点数组重建
		mMapView.refreshMap();
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

	/**
	 * 将临时文件保存成正式版文件，拼接xy是int单位是毫米
	 */
	private String saveDatabase() {
		String dstPath = DTFileUtils.getDataBasePath(mMapName, mMarkList.get(0)
				.getX(), mMarkList.get(0).getY(),mFloor.getBuildid());// 数据库路径
		try {
			String srcPath = String.format("%s%s", Constants.WIFI_PICKER_PATH,
					Constants.DATABASE_NAME);// 缓存打点数据库路径
			FileHelper.copyFile(srcPath, dstPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		WPDBService.getInstance().deleteAllWifis();
		return dstPath;
	}

	/**
	 * 设置文本提示信息
	 * 
	 * @param info
	 */
	private void showOtherInfo(String info) {
		retInfo = info;
		mHandler.sendEmptyMessage(MESSAGE_SHOW_OTHER_INFO);
	}

	/**
	 * 导入数据
	 */
	private void importHistoryData() {
		mRoutelayer.clearAllRoute();// 清除所有路线
		HashMap<String, SQLiteDatabase> databases = WPDBService.getInstance()
				.getDataBases4ShowView(mMapName,mFloor.getBuildid());
		Iterator<Entry<String, SQLiteDatabase>> it = databases.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<String, SQLiteDatabase> entry = it.next();
			ArrayList<NavigatePoint> points = WPDBService.getInstance().getAllPoints(
					entry.getValue(), mFloor.getFloor());
			entry.getValue().close();
			ArrayList<NavigatePoint> markPoints = new ArrayList<NavigatePoint>();
			for (int i = 0; i < points.size(); i++) {
				NavigatePoint wp = new NavigatePoint();
				wp.setX(points.get(i).getX());
				wp.setY(points.get(i).getY());
				wp.setFloor(mFloor.getFloor());
				if (markPoints.size() == 0
						|| !(markPoints.get(markPoints.size() - 1).getX() == wp
								.getX() && markPoints
								.get(markPoints.size() - 1).getY() == wp.getY())) {
					markPoints.add(wp);
				}
			}
			if (markPoints.size() > 0) {// 添加历史路线
				mRoutelayer.addRoute(entry.getKey(), markPoints);
			}
		}
		mMapView.refreshMap();
	}

	private void export() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				showOtherInfo("正在导出采集数据，请稍后...");
				Message msg0 = new Message();
				msg0.what = MESSAGE_EXPORT_DATA_REMIND;
				msg0.arg1 = 0;
				mHandler.sendMessage(msg0);
				SimpleDateFormat sdf = new SimpleDateFormat("",
						Locale.SIMPLIFIED_CHINESE);
				sdf.applyPattern("yyyyMMdd_HHmmss");// 20140925_114245
				long time = System.currentTimeMillis();
				HashMap<String, SQLiteDatabase> databases = WPDBService.getInstance()
						.getBackupDataBases(rootPath,mMapName);
				Iterator<Entry<String, SQLiteDatabase>> it = databases
						.entrySet().iterator();
				while (it.hasNext()) {
					time += 1000;
					String timeString = sdf.format(time);// 20140925_115232
					String path = String.format("%s%s_%s.walk1",
							rootPath, mMapName,
							timeString.replace("_", "T"));
					Entry<String, SQLiteDatabase> entry = it.next();
					ArrayList<String> wifis = WPDBService.getInstance().getAllWifis(entry
							.getValue());
					entry.getValue().close();
					String dbPath = entry.getValue().getPath();
					new File(dbPath).renameTo(new File(dbPath + ".export"));
					FileUtil.fstream(path, WifiRecorder.getInstance()
							.getWifiHead());
					for (int i = 0; i < wifis.size(); i++) {
						FileUtil.fstream(path, wifis.get(i));
					}
				}
				showOtherInfo("数据导出完成！");
				Message msg1 = new Message();
				msg1.what = MESSAGE_EXPORT_DATA_REMIND;
				msg1.arg1 = 1;
				mHandler.sendMessage(msg1);
				importHistoryData();
			}
		}).start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		WifiRecorder.getInstance().onPause();
		mMapView.clearMapLayer();
		UIEvent.getInstance().remove(mHandler);
		WifiRecorder.getInstance().onDestroy();
	}

	@Override
	public void onClick(NavigatePoint point, String key) {
		if (!mStartBtn.getText().toString()
				.equals(getString(R.string.title_gather))) {
			DTUIUtils.showToastSafe("请停止之后操作");
			return;
		}
		delete(key);
	}

	@Override
	public void onClick(RMPoi point, String key) {

	}
}
