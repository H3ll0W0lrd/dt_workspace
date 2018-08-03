package com.rtmap.locationcheck.page;

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
import java.text.SimpleDateFormat;
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
import com.rtmap.locationcheck.layer.LocationBeaconLayer;
import com.rtmap.locationcheck.layer.OnBeaconClickListener;
import com.rtmap.locationcheck.layer.PointLayer;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTIOUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;

/**
 * 静态测试（单点测试）
 * 
 * @author dingtao
 *
 */
public class LCLocationPointActivity extends LCActivity implements
		OnClickListener, OnItemClickListener, OnBeaconClickListener,
		RMLocationListener {

	private Floor mFloor;// 楼层信息
	private MapView mMapView;
	private TextView mTitle, mMenu;// 标题，菜单键
	private Dialog mMenuDialog, mPointDialog;// 间隔dialog
	private PointLayer mPointLayer;
	private LocationBeaconLayer mBeaconLayer;// beacon
	private TextView mStatus;// 状态
	private Button mMark;// 标记
	private LCPoint mPoint;// 标记点
	private long startTime, endTime;
	private boolean isBeaconStart;// 开始
	private HashMap<String, BeaconInfo> mBeaconMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_map);
		mFloor = (Floor) getIntent().getExtras().getSerializable("floor");
		mTitle = (TextView) findViewById(R.id.title);
		mTitle.setText(mFloor.getName() + "-" + mFloor.getFloor());
		mMenu = (TextView) findViewById(R.id.menu);
		mMenu.setOnClickListener(this);
		mStatus = (TextView) findViewById(R.id.status);// 标记
		mMark = (Button) findViewById(R.id.mark);

		mMark.setOnClickListener(this);

		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		mMapView.removeRefreshRunnable();
		mMapView.setUpdateMap(false);

		Drawable green = getResources().getDrawable(R.drawable.sign_green);
		Drawable red = getResources().getDrawable(R.drawable.sign_red);
		// 编辑状态：0正常，1删除，2新建，3修改
		// 工作状态：0正常，-1低电量，-2故障，-3缺失，-4未知
		HashMap<Integer, Bitmap> bmpMap = new HashMap<Integer, Bitmap>();
		bmpMap.put(0, DTIOUtils.drawableToBitmap(red));
		bmpMap.put(1, DTIOUtils.drawableToBitmap(green));

		mBeaconLayer = new LocationBeaconLayer(mMapView, bmpMap);
		mBeaconLayer.setNameVisibility(true);
		Drawable blue = getResources().getDrawable(R.drawable.sign_purple);

		mPointLayer = new PointLayer(mMapView, DTIOUtils.drawableToBitmap(blue));
		mMapView.addMapLayer(mBeaconLayer);
		mMapView.addMapLayer(mPointLayer);
		CompassLayer mCompassLayer = new CompassLayer(mMapView);// 指南针图层
		mMapView.addMapLayer(mCompassLayer);
		mPointLayer.setOnPointClickListener(this);

		mBeaconMap = new HashMap<String, BeaconInfo>();

		initMenuDialog();
		initControl();
		initLocation();
		startLocation();
		importLocationHistory();
		importBeaconHistory(false);
		mMapView.initMapConfig(mFloor.getBuildId(), mFloor.getFloor());// 打开地图（建筑物id，楼层id）
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
					mMapView.setCenter(location.getX(), location.getY(),false);
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
	 * 初始化定位
	 */
	private void startLocation() {
		LocationApp.getInstance().registerLocationListener(this);
		
	}

	/**
	 * 加载定位历史
	 */
	private void importLocationHistory() {
		FilenameFilter filter = new FilenameFilter() {
			// 860100010040500002-F2*.*
			@Override
			public boolean accept(File dir, String filename) {
				if (filename.contains(mFloor.getBuildId() + "_"
						+ DTStringUtils.floorTransform(mFloor.getFloor()))
						&& filename.endsWith(".lcrpt1")) {
					String[] str = filename.split("_");
					String[] array = str[0].split("-");
					if (array.length == 3)
						return true;
				}
				return false;
			}
		};
		String[] filePaths = new File(DTFileUtils.getDataDir()).list(filter);
		for (String path : filePaths) {
			DTLog.e("path : " + path);
			String str = path.substring(0, path.indexOf("_"));
			String[] s = str.split("-");
			LCPoint point = new LCPoint();
			point.setX(Integer.parseInt(s[1]));
			point.setY(Integer.parseInt(s[2]));
			point.setName(DTFileUtils.getDataDir() + path);
			mPointLayer.addPoint(point);
		}
		mStatus.setText("已测试" + mPointLayer.getPointCount()
				+ "点\n标记自己位置后定位20次结束");
		mMapView.refreshMap();
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
							if (info.getMaclist() != null
									&& info.getMaclist().size() > 0) {
								for (int i = 0; i < info.getMaclist().size(); i++) {
									mBeaconMap.put(info.getMaclist().get(i)
											.getMac(), info);
								}
							}
							mBeaconLayer.addPoint(info);
						}
						mMapView.refreshMap();
					}
				}
			}
		}).run();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.menu:
			mMenuDialog.show();
			break;
		case R.id.mark:
			isBeaconStart = !isBeaconStart;
			float x = mMapView.getCenter().getX();
			float y = mMapView.getCenter().getY();
			mPoint = new LCPoint();
			mPoint.setX((int) (x * 1000));
			mPoint.setY((int) (y * 1000));
			mPointLayer.addPoint(mPoint);
			mMapView.refreshMap();
			gather();
			break;
		}
	}

	/**
	 * 采集
	 */
	private void gather() {
		startTime = System.currentTimeMillis();
		mMark.setEnabled(false);
		mStatus.setText("正在定位....");
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

	/**
	 * 初始化弹出框
	 */
	private void showPointDialog(final LCPoint point) {
		mPointDialog = new Dialog(this, R.style.dialog);
		mPointDialog.setContentView(R.layout.dialog_map_layout);
		mPointDialog.setCanceledOnTouchOutside(true);
		ListView mInterList = (ListView) mPointDialog
				.findViewById(R.id.set_list);
		String[] interDate = getResources().getStringArray(R.array.point_menu);
		mInterList.setAdapter(new LCMapDialogAdapter(this, interDate));
		mInterList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				switch (position) {
				case 0:// 重录
					mPointDialog.cancel();
					if (isBeaconStart) {
						DTUIUtils.showToastSafe("正在录制..");
						return;
					}
					File file = new File(point.getName());
					file.delete();
					String off = point.getName().subSequence(0,
							point.getName().lastIndexOf("."))
							+ ".off";
					File offile = new File(off);
					offile.delete();
					point.setName(null);
					mPoint = point;
					isBeaconStart = !isBeaconStart;
					gather();
					break;
				case 1:// 删除
					mPointDialog.cancel();
					AlertDialog.Builder builder = new Builder(
							LCLocationPointActivity.this);
					builder.setMessage("确认删除吗？");
					builder.setTitle("提示");
					builder.setPositiveButton("确认",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									mPointLayer.clearPoint(point);
									DTLog.e(point.getName());
									File file = new File(point.getName());
									file.delete();
									String off = point.getName().subSequence(0,
											point.getName().lastIndexOf("."))
											+ ".off";
									File offile = new File(off);
									offile.delete();
									mStatus.setText("已测试"
											+ mPointLayer.getPointCount()
											+ "点\n标记自己位置后定位20次结束");
									mMapView.refreshMap();
								}
							});
					builder.setNegativeButton("取消", null);
					builder.create().show();
					break;
				}
			}
		});
		mPointDialog.show();
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocationApp.getInstance().unRegisterLocationListener(this);
		mMapView.clearMapLayer();
		mBeaconMap.clear();
	}

	@Override
	public void onBeaconClick(LCPoint point, String key) {
		if (!DTStringUtils.isEmpty(point.getName())) {
			showPointDialog(point);
		}
	}

	private int count;

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
	 * 导出文件
	 * 
	 * @author dingtao
	 *
	 */
	// class ExportFil implements LCCallBack {
	//
	// @Override
	// public Object onCallBackStart(Object... obj) {
	// try {
	// Date date = new Date(System.currentTimeMillis());
	// SimpleDateFormat format = new SimpleDateFormat(
	// "yyyyMMdd-HHmmss");
	// String d = format.format(date);
	// String filename = d.replaceAll("-", "T") + "-" + mPoint.getX()
	// + "-" + mPoint.getY() + "_" + mFloor.getBuildId() + "_"
	// + DTStringUtils.floorTransform(mFloor.getFloor()) + "_"
	// + LCApplication.MAC;
	// File file = new File(DTFileUtils.getDataDir() + filename
	// + ".lcrpt1");
	// file.createNewFile();
	// OutputStreamWriter write = new OutputStreamWriter(
	// new FileOutputStream(file), DTStringUtils.UTF_8);
	// BufferedWriter bw = new BufferedWriter(write);
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
	// DTUIUtils.showToastSafe("保存成功");
	// mPoint.setName(file.getAbsolutePath());
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
	// count = 0;
	// mLoadDialog.cancel();
	// mMark.setEnabled(true);
	// mStatus.setText("已测试" + mPointLayer.getPointCount()
	// + "点\n标记自己位置后定位20次结束");
	// }
	// }

	private String mLcrptFile;
	private String mOffFile;

	@Override
	public void onReceiveLocation(RMLocation result) {
		result.coordY = Math.abs(result.getCoordY());
		result.x = Math.abs(result.getX());
		if (result.getError() == 0) {
			mMapView.setMyCurrentLocation(result);
		}
		String scanner = LocationApp.getInstance().getScannerInfo();
		if (scanner == null)
			return;
		if (!DTStringUtils.isEmpty(scanner) && scanner.contains("<beacons>")) {
			new LCAsyncTask(new CheckBeaconCall()).run(scanner);
		}

		if (isBeaconStart) {
			// <Locating><build>860100010040500017</build><floor>20100</floor><x>12229</x><y>10960</y>
			count++;
			if (count <= 20) {
				// time build floor x y rbuild rfloor rx ry offset err
				// delay
				mStatus.setText("定位第" + count + "次\n定位码：" + result.getError());
				long offx = result.getCoordX() - mPoint.getX();
				long offy = result.getCoordY() - mPoint.getY();
				long offset = (long) Math.sqrt(offx * offx + offy * offy);
				endTime = System.currentTimeMillis();
				int error = result.getError();
				if (error == 0) {
					if (mFloor.getBuildId().equals(result.getBuildID())) {
						if (DTStringUtils.floorTransform(mFloor.getFloor()) != result
								.getFloorID())
							error = 100002;
					} else {
						error = 100001;
					}
				}
				String mLocResult = endTime + "\t" + mFloor.getBuildId() + "\t"
						+ DTStringUtils.floorTransform(mFloor.getFloor())
						+ "\t" + mPoint.getX() + "\t" + mPoint.getY() + "\t"
						+ result.getBuildID() + "\t" + result.getFloorID()
						+ "\t" + result.getCoordX() + "\t" + result.getCoordY()
						+ "\t" + offset + "\t" + result.getError() + "\t"
						+ (endTime - startTime) + "\n";
				scanner = scanner.replaceFirst(
						"<Locating>",
						"<Locating><build>"
								+ mFloor.getBuildId()
								+ "</build><floor>"
								+ DTStringUtils.floorTransform(mFloor
										.getFloor()) + "</floor><x>"
								+ mPoint.getX() + "</x><y>" + mPoint.getY()
								+ "</y>")
						+ "\n";
				if (count == 1) {
					Date date = new Date(System.currentTimeMillis());
					SimpleDateFormat format = new SimpleDateFormat(
							"yyyyMMdd-HHmmss");
					String d = format.format(date);
					String filename = d.replaceAll("-", "T") + "-"
							+ mPoint.getX() + "-" + mPoint.getY() + "_"
							+ mFloor.getBuildId() + "_"
							+ DTStringUtils.floorTransform(mFloor.getFloor())
							+ "_" + LCApplication.MAC;
					mLcrptFile = DTFileUtils.getDataDir() + filename
							+ ".lcrpt1";
					mOffFile = DTFileUtils.getDataDir() + filename + ".off";
					DTFileUtils
							.fstream(mLcrptFile,
									"time\tbuild\tfloor\tx\ty\trbuild\trfloor\trx\try\toffset\terr\tdelay\n");// 写头文件
				}
				DTFileUtils.fstream(mLcrptFile, mLocResult);
				DTFileUtils.fstream(mOffFile, scanner);
				if (count >= 20) {// 如果20次够了
					isBeaconStart = !isBeaconStart;
					count = 0;
					mMark.setEnabled(true);
					mStatus.setText("已测试" + mPointLayer.getPointCount()
							+ "点\n标记自己位置后定位20次结束\n定位码：" + result.getError());
				}
			}
			startTime = System.currentTimeMillis();
		} else {
			mStatus.setText("已测试" + mPointLayer.getPointCount()
					+ "点\n标记自己位置后定位20次结束\n定位码：" + result.getError());
		}
	}
}
