package com.rtmap.locationcheck.page;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rtm.frm.utils.Utils;
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
import com.rtmap.locationcheck.layer.BitmapBeaconLayer;
import com.rtmap.locationcheck.layer.OnLayerPointClickListener;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;
import com.rtmap.locationcheck.util.map.Coord;
import com.rtmap.locationcheck.util.map.MapWidget;
import com.rtmap.locationcheck.util.map.MapWidget.OnMouseListener;
import com.rtmap.locationcheck.util.map.MapWidget.WidgetStateListener;
import com.rtmap.locationcheck.util.map.PinMark;

/**
 * Poi与路网采集(位图)
 * 
 * @author dingtao
 *
 */
public class LCBitampBeaconNoLBSActivity extends LCActivity implements
		OnLayerPointClickListener, OnClickListener {

	private MapWidget mapWidget;
	private BitmapBeaconLayer mBeaconLayer;// 采集点图层
	private PinMark pinMark;
	// private MapInfo mMapInfo;// 地图name,由MapSelectActivity传入
	private TextView mTitle;// 标题栏，显示采集地图的名称

	private TextView mMenu;// 返回地图选择界面
	private Button markBtn;// 标记
	private Floor mFloor;

	private HashMap<String, BeaconInfo> mBeaconMap;// beacon键值对mac值

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_beacon_bitmap);
		mFloor = (Floor) getIntent().getExtras().getSerializable("floor");
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
		mBeaconLayer = null;
		mapWidget = null;
	}

	private void initContrls() {
		mMenu = (TextView) findViewById(R.id.menu);
		markBtn = (Button) findViewById(R.id.mark);
		mTitle = (TextView) findViewById(R.id.title);
		initMenuDialog();
		initBeaconInfoDilog();

		mMenu.setOnClickListener(this);
		markBtn.setOnClickListener(this);

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
					switch (msg.arg1) {
					case R.id.btn_direction_up:
						mapWidget.getCoordTransformer().bitmapTranslate(0, 2);
						break;
					case R.id.btn_direction_down:
						mapWidget.getCoordTransformer().bitmapTranslate(0, -2);
						break;
					case R.id.btn_direction_left:
						mapWidget.getCoordTransformer().bitmapTranslate(2, 0);
						break;
					case R.id.btn_direction_right:
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

	private TextView mStatus;

	private void init() {
		initContrls();
		mStatus = (TextView) findViewById(R.id.status);
		// 根据ID获取地图名
		mTitle.setText(mFloor.getName() + "-" + mFloor.getFloor());

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
		// 初始标记点设置，图形、可见、居中
		pinMark = new PinMark(mapWidget, R.drawable.pin48);
		pinMark.setVisiable(true);
		pinMark.setLocation(getResources().getDisplayMetrics().widthPixels / 2,
				getResources().getDisplayMetrics().heightPixels / 2);
		// 添加标记点图层及采集点图层
		mapWidget.addMark(pinMark);
		mBeaconLayer = new BitmapBeaconLayer();
		mBeaconLayer.setScale(mFloor.getScale());
		mapWidget.addMark(mBeaconLayer);
		mapWidget.registerMouseListener(new OnMouseListener() {

			@Override
			public void onSingleTap(MapWidget mw, float x, float y) {
				mBeaconLayer.onSingleTap(mw, x, y,
						LCBitampBeaconNoLBSActivity.this);
			}
		});
		mBeaconMap = new HashMap<String, BeaconInfo>();
		// 导入beacon信息
		importBeaconHistory(false);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (event.getAction() != KeyEvent.ACTION_DOWN) {
			return false;
		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			mBeaconLayer.setNameVisibility(true);
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			mBeaconLayer.setNameVisibility(false);
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

	private TextView mInfoText;
	private Button mDeleteReStore;

	/**
	 * 初始化beacon信息弹出框
	 */
	private void initBeaconInfoDilog() {
		mInfoDialog = new Dialog(this, R.style.dialog_white);
		mInfoDialog.setContentView(R.layout.dialog_beacon_layout);
		mInfoDialog.setCanceledOnTouchOutside(true);
		mInfoText = (TextView) mInfoDialog.findViewById(R.id.beacon_info);
		mDeleteReStore = (Button) mInfoDialog.findViewById(R.id.delete_restore);
		mInfoDialog.findViewById(R.id.update).setVisibility(View.GONE);
		mDeleteReStore.setOnClickListener(this);
	}

	private BeaconInfo mBeaconInfo;

	@Override
	public void onClick(BeaconInfo info) {
		mBeaconInfo = info;
		mBeaconInfo.setClick(true);
		Point temppoi = new Point();
		mapWidget.getLCPointTransformer().worldToClient(
				info.getX() / mFloor.getScale(),
				info.getY() / mFloor.getScale(), temppoi);// 得到beaconInfo的屏幕坐标
		WindowManager wm = this.getWindowManager();

		int width = wm.getDefaultDisplay().getWidth();
		// 设置window位置
		Window win = mInfoDialog.getWindow();
		LayoutParams params = win.getAttributes();
		DTLog.e("dialog.x : " + params.x + "   dialog.y: " + params.y);
		int difX;
		if (temppoi.x > width / 2) {// 点在右边
			difX = (int) (width / 4 - temppoi.x + width / 2);// x轴差值
			win.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		} else {
			win.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			difX = (int) (width / 4 - temppoi.x);// x轴差值
		}
		params.x = width / 4 + 15;// 设置x坐标
		DTLog.i("difX : " + difX + "    X : " + pinMark.getX() + "    "
				+ temppoi.x + "     Y : " + pinMark.getY() + "    " + temppoi.y);
		mapWidget.getLCPointTransformer().bitmapTranslate(difX,
				pinMark.getY() - temppoi.y);

		win.setAttributes(params);

		String status = "";
		status += "BuildId: " + mBeaconInfo.getBuildId();
		status += "\nMac: " + mBeaconInfo.getMac();
		status += "\nMajor: " + mBeaconInfo.getMajor();
		status += "\nMinor: " + mBeaconInfo.getMinor();
		status += "\nX: " + mBeaconInfo.getX();
		status += "\nY: " + mBeaconInfo.getY();
		mInfoText.setText(status);
		mInfoDialog.show();
	}

	private Dialog mMenuDialog, mInfoDialog;

	/**
	 * 初始化菜单弹出框
	 */
	private void initMenuDialog() {
		mMenuDialog = new Dialog(this, R.style.dialog);
		mMenuDialog.setContentView(R.layout.dialog_map_layout);
		mMenuDialog.setCanceledOnTouchOutside(true);
		ListView mInterList = (ListView) mMenuDialog
				.findViewById(R.id.set_list);
		String[] interDate = getResources().getStringArray(
				R.array.bitmap_beacon_menu);
		mInterList.setAdapter(new LCMapDialogAdapter(this, interDate));
		mInterList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				switch (position) {
				case 0:// 更新beacon
					mMenuDialog.cancel();
					mLoadDialog.show();
					new LCAsyncTask(new BeaconDownLoadCall()).run();
					break;
				case 1:// 数据还原
					mMenuDialog.cancel();
					String filepath = DTFileUtils.getDataDir()
							+ mFloor.getBuildId() + "_" + mFloor.getFloor()
							+ ".txt";
					String destPath = DTFileUtils.getBackupDir()
							+ mFloor.getBuildId() + "_" + mFloor.getFloor()
							+ ".txt";
					DTFileUtils.copyFile(destPath, filepath, false);
					importBeaconHistory(false);
					break;
				}
			}
		});
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
			mLoadDialog.cancel();
			if (obj != null) {
				importBeaconHistory(true);
			}
		}
	}

	/**
	 * 标记添加beacon
	 */
	private void mark() {
		final Coord coord = new Coord();
		mapWidget.getCoordTransformer().clientToWorld(pinMark.getX(),
				pinMark.getY(), coord);
		if (coord.isValid()) {
			Intent setBeacon = new Intent(this, LCAddBeaconActivity.class);
			setBeacon.putExtra("x", coord.mX / 1000f * mFloor.getScale());
			setBeacon.putExtra("y", coord.mY / 1000f * mFloor.getScale());
			setBeacon.putExtra("build", mFloor.getBuildId());
			setBeacon.putExtra("floor", mFloor.getFloor());
			LCAddBeaconActivity.mMacSet = mBeaconMap;
			startActivityForResult(setBeacon, 10);
		}

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
				return loadBeaconData(filepath);
			}

			@Override
			public void onCallBackFinish(Object obj) {
				if (obj != null) {
					BeaconList list = (BeaconList) obj;
					mBeaconLayer.clearAllCorrectPoints();
					mBeaconMap.clear();
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
							mBeaconLayer.addCorrectPoint(info);
							mBeaconMap.put(info.getMac(), info);
							if (info.getMaclist() != null
									&& info.getMaclist().size() > 0) {
								for (int i = 0; i < info.getMaclist().size(); i++) {
									mBeaconMap.put(info.getMaclist().get(i)
											.getMac(), info);
								}
							}
						}
					}
					mStatus.setText("一共有"+mBeaconLayer.getPointCount()+"个点");
				}
			}
		}).run();
	}

	@Override
	protected void onActivityResult(int requestCode, int arg1, Intent intent) {
		super.onActivityResult(requestCode, arg1, intent);
		if (arg1 == Activity.RESULT_OK) {
			Bundle bundle = intent.getExtras();
			BeaconInfo info = (BeaconInfo) bundle.getSerializable("beacon");
			DTLog.i("info : " + info.getBroadcast_id() + "   " + info.getUuid());
			if (requestCode == 10) {
				mBeaconLayer.addCorrectPoint(info);
				mBeaconMap.put(info.getMac(), info);
			}
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
				ArrayList<BeaconInfo> beaconlist = mBeaconLayer.getPointList();
				if (beaconlist == null)// || beaconlist.size() == 0
					return null;
				String filepath = DTFileUtils.getDataDir()
						+ mFloor.getBuildId() + "_" + mFloor.getFloor()
						+ ".txt";
				boolean isCopy = false;// 是否备份
				int changeCount = 0;// 修改的beacon点数
				for (BeaconInfo info : beaconlist) {
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
					list.setList(beaconlist);
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.menu:
			mMenuDialog.show();
			break;
		case R.id.mark:
			mark();
			break;
		case R.id.delete_restore:
			mBeaconLayer.getCorrectPoints().remove(mBeaconInfo);
			mBeaconMap.remove(mBeaconInfo.getMac());
			exportBeaconInfo();
			mInfoDialog.cancel();
			break;
		}
	}
}
