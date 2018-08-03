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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Floor;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.PointInfo;
import com.rtmap.wifipicker.BuildSession;
import com.rtmap.wifipicker.R;
import com.rtmap.wifipicker.core.WPApplication;
import com.rtmap.wifipicker.layer.RouteLayer;
import com.rtmap.wifipicker.util.ConstantLoc.UIEventCode;
import com.rtmap.wifipicker.util.Constants;
import com.rtmap.wifipicker.util.DTFileUtils;
import com.rtmap.wifipicker.util.DTLog;
import com.rtmap.wifipicker.util.DTStringUtils;
import com.rtmap.wifipicker.util.FileHelper;
import com.rtmap.wifipicker.util.FileUtil;
import com.rtmap.wifipicker.util.ImgUtil;
import com.rtmap.wifipicker.util.WPDBService;
import com.rtmap.wifipicker.wifi.CollectFingerPoint;
import com.rtmap.wifipicker.wifi.NetGather;
import com.rtmap.wifipicker.wifi.NetGatherData;
import com.rtmap.wifipicker.wifi.NetGatherTask.CompleteListener;
import com.rtmap.wifipicker.wifi.UIEvent;
import com.rtmap.wifipicker.wifi.WifiRecorder;

/**
 * 网络采集
 * 
 * @author dingtao
 *
 */
public class WPUnionNewActivity extends WPBaseActivity implements
		OnClickListener {

	private static final int MESSAGE_GATHER_ON_WALK = 1101;
	private static final int MESSAGE_SHOW_OTHER_INFO = 1106;
	private static final int MESSAGE_EXPORT_DATA_REMIND = 1108;

	private static final int MESSSAGE_TIME = 2204;

	private MapView mMapView;// mapView
	private RouteLayer mRoutelayer;// 路线layer
	private ArrayList<NavigatePoint> mMarkList;// 标记点数组
	private static final float MARK_ADD_DISTANCE = 0.001f;// 当两个点位置坐标一样，增加1毫米做区别

	private Button mStartBtn;// 开始采集
	private Button mBack;// 返回地图选择界面
	private Button mMarkBtn;// 标记

	private String retInfo = "";// 其他显示信息

	private TextView mTitle;// 标题栏，显示采集地图的名称
	private TextView mTextStatus;// 显示信息
	private TextView mTextGatherRet;// 其他显示信息
	private ImageView mImageNetStatus;// 服务器是否连接标志
	private int count;
	private SharedPreferences sharedPreferences = WPApplication.getInstance()
			.getShare();

	private CollectFingerPoint mFingerCollect;
	private String mMapName;
	private Floor mFloor;

	private int serverType = 0; // 0:window 1:linux


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
			case UIEventCode.WIFI_SCAN_END:
				mTextGatherRet.setText("points:" + mMarkList.size() + ", aps:"
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
				mTextStatus.setText(remindStr);
				mTextGatherRet.setText("stop sucess");
				break;
			}
			super.handleMessage(msg);
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
		mFingerCollect = new CollectFingerPoint();
		mMarkList = new ArrayList<NavigatePoint>();
		count = Integer.parseInt(sharedPreferences.getString("net_time", "60"));
		init();
		NetGather.GetInstance();
		UIEvent.getInstance().register(mHandler);
		WifiRecorder.getInstance().onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		WifiRecorder.getInstance().onPause();
		WifiRecorder.getInstance().onDestroy();
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
			WPUnionNewActivity.this.finish();
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (mStartBtn.getText().toString().equals(getString(R.string.stop))) {
				mark();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
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
		mFloor = (Floor) getIntent().getExtras().getSerializable(
				Constants.EXTRA_FLOOR);
		// 根据ID获取地图名
		mMapName = mFloor.getBuildid() + "-" + mFloor.getFloor() + "-0";// 地图name,由MapSelectActivity传入

		NetGatherData.sUserName = WPApplication.getInstance().getShare()
				.getString("tag1", "test");

		BuildSession.getInstance().setBuildId(mFloor.getBuildid());// 初始化全局的mapname，在其他地方用到，例如database存储
		BuildSession.getInstance().setFloor(mFloor.getFloor());

		mTitle.setText("联合采集");
		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		mMapView.setUpdateMap(false);
		mMapView.setDoubleTapable(false);
		mMapView.initMapConfig(mFloor.getBuildid(), mFloor.getFloor());// 打开地图（建筑物id，楼层id）
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

		importHistoryData();// 导入历史数据
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			stop();
			WPUnionNewActivity.this.finish();
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
			markTerminal();
			break;
		}
	}

	String filePath;
	long time;

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
	}

	/**
	 * 标记按钮
	 */
	private void markTerminal() {
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
	 * 将临时文件保存成正式版文件，拼接xy是int单位是毫米
	 */
	private void saveDatabase() {
		try {
			String dstPath = DTFileUtils.getDataBasePath(mMapName, mMarkList
					.get(0).getX(), mMarkList.get(0).getY(),mFloor.getBuildid());// 数据库路径
			String srcPath = String.format("%s%s", Constants.WIFI_PICKER_PATH,
					Constants.DATABASE_NAME);// 缓存打点数据库路径
			FileHelper.copyFile(srcPath, dstPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		WPDBService.getInstance().deleteAllWifis();
	}

	private void export() {
		mTextStatus.setText("正在导出采集数据，请稍后...");
		mTextGatherRet.setText("stop sucess");
		new Thread(new Runnable() {

			@Override
			public void run() {
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
				Message msg1 = new Message();
				msg1.what = MESSAGE_EXPORT_DATA_REMIND;
				msg1.arg1 = 1;
				mHandler.sendMessage(msg1);
			}
		}).start();
	}

	private void start(int server) {
		String root = rootPath + "net" + File.separator;
		FileUtil.checkDir(root);
		long t = System.currentTimeMillis();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String d = format.format(new Date(t)).replaceAll("-", "T");
		String filename = mMapName + "_"
				+ sharedPreferences.getString("tag1", "000000000000") + d;
		filePath = root + filename + "_" + "mark1.csv";
		if (server == 0) {
			NetGather.GetInstance().start(new CompleteListener() {
				@Override
				public void onComplete(String result) {
					handleResult(result);
				}
			}, filename, mMapName);
		} else if (server == 1) {
			NetGather.GetInstance().start(new CompleteListener() {
				@Override
				public void onComplete(String result) {
					handleResult(result);
				}
			}, filename, mMapName);
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
			mTextGatherRet.setText("start sucess");
			mImageNetStatus.setImageResource(R.drawable.status_green);
			mHandler.sendEmptyMessage(MESSSAGE_TIME);
			time = System.currentTimeMillis();
			mark();
			startTerminal();
		} else {
			mTextStatus.setText("sever return error,start fail");
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
	 * 标记
	 * 
	 * @param server
	 */
	private void mark() {
		final float milliX = mMapView.getCenter().getX();
		final float milliY = mMapView.getCenter().getY();
		addPoint(milliX, milliY);
		savePoint();// 文件写点
	}

	/**
	 * 开始标记
	 */
	private void startTerminal() {
		WPDBService.getInstance().deleteAllWifis();
		WifiRecorder.getInstance().onResume();
		gather();
	}

	private void stop() {
		count = Integer.parseInt(sharedPreferences.getString("net_time", "60"));
		mTextStatus.setText("");
		mTextGatherRet.setText("stop sucess");
		mMarkBtn.setEnabled(false);
		mStartBtn.setText(R.string.title_gather);
		mHandler.removeMessages(MESSAGE_GATHER_ON_WALK);
		WifiRecorder.getInstance().onStop();
		if (mMarkList.size() == 0)
			return;
		saveDatabase();// wifipicker.db备份并清空
		mMarkList = new ArrayList<NavigatePoint>();// 将标记点数组重建
		importHistoryData();
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
		mRoutelayer.clearAllRoute();// 清除所有路线
		String root = rootPath + "net" + File.separator;
		String[] files_roadnet = FileHelper.listFiles(root,
				new FilenameFilter() {

					@Override
					public boolean accept(File dir, String filename) {
						if (filename.contains(mMapName)
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
				String path = String.format("%s%s", root, files_roadnet[i]);
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
					// FileUtil.fstream(filePath,
					// "time\tbuildId\tfloor\tx\ty\n");
					ArrayList<NavigatePoint> points = new ArrayList<NavigatePoint>();
					br.readLine();
					while (((tmp = br.readLine()) != null)
							&& (tmp.length() > 1)) {
						String[] point = tmp.split("\t");
						NavigatePoint roadnetPoint = new NavigatePoint("",
								Integer.parseInt(point[3]) / 1000.0f,
								Integer.parseInt(point[4]) / 1000.0f, "",
								mMapView.getFloor(), "", 0);
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
			mMapView.refreshMap();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

}
