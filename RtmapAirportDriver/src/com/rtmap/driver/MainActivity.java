package com.rtmap.driver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;
import com.rtmap.driver.beans.LoginBean;
import com.rtmap.driver.scan.Util;
import com.rtmap.driver.util.BuildUitl;
import com.rtmap.driver.util.DialogUtil;
import com.rtmap.driver.util.DialogUtil.DialogCallBack;
import com.rtmap.driver.util.FileUtil;
import com.rtmap.driver.util.MyUtil;
import com.rtmap.driver.util.PreferencesUtil;
import com.rtmap.driver.util.SoundUtil;
import com.rtmap.driver.util.T;
import com.rtmap.driver.util.TimeUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import cn.pda.scan.ScanThread;

@SuppressLint("HandlerLeak")
public class MainActivity extends BaseActivity implements RMLocationListener {
	public static MainActivity instance = null;

	private static final int TEST_LOCATION = 0x100;

	private static final int UPLOAD_LOACATION = 200;

	private long firstTime = 0;

	private boolean isDebug = false;

	/*****
	 * view
	 ****/
	private TextView tvTime;
	private TextView tvBlueToothTime;
	private TextView tvCoord;
	private TextView tvPassenger;
	private TextView tvPassengerWarn;

	/*****
	 * view
	 ****/
	private ScanThread scanThread;

	/***
	 * locate to sd card
	 ****/
	private boolean isStopLocateRun = false;
	private static final int RecordLocateDelayed = 1 * 1000;

	/**
	 * just for test
	 **/
	private Timer scanTimer = null;

	/***
	 * passenger
	 ***/
	private static String PrePassenger = "pre_passenger";
	private static int passengerCount = 0;

	/**
	 * passenage image
	 **/
	private String imagePath;

	public static final String TYPE_QR = "0";
	public static final String TYPE_QR_ERR = "3";
	private static final String TYPE_IMAGE = "1";
	private static final String TYPE_COUNT = "2";
	private static final String AIRPORT_TAG = "PEK";

	// Receiver
	private BroadcastReceiver mScanDataReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("com.android.scancontext")) {
				String str = intent.getStringExtra("Scan_context");

				// T.s(str);
				Message msg = mHandler.obtainMessage();
				msg.what = ScanThread.SCAN;
				Bundle data = new Bundle();
				data.putString("data", str);
				msg.setData(data);
				mHandler.sendMessage(msg);

				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						Intent intent1 = new Intent(
								"android.intent.action.FUNCTION_BUTTON_DOWN",
								null);
						context.sendBroadcast(intent1);
					}
				}, 1500);

			}
		}
	};

	private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 判断它是否是为电量变化的Broadcast Action
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				// 获取当前电量
				int level = intent.getIntExtra("level", 0);
				// 电量的总刻度
				int scale = intent.getIntExtra("scale", 100);
				// 把它转成百分比
				App.getInstance().batteryScale = (level * 100) / scale;
			}
		}
	};
	private TextView tvLocationUpdata;

	public static MainActivity getInstance() {
		return instance;
	}
	
	private String mFilePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		isDebug = FileUtil.isDebug();

		instance = this;

		initView();

		IntentFilter scanDataIntentFilter = new IntentFilter();
		scanDataIntentFilter.addAction("com.android.scancontext");
		registerReceiver(mScanDataReceiver, scanDataIntentFilter);
		// 启动扫描服务
		Intent scanIntent = new Intent("com.android.scanservice.scan.on");
		sendBroadcast(scanIntent);

		// 注册电量广播
		IntentFilter intentFilter = new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED);

		// 注册receiver
		registerReceiver(batteryReceiver, intentFilter);
		//文件名YYYYMMDDHHMM_upload_司机ID.txt
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
		mFilePath = format.format(new Date(System.currentTimeMillis()))+"_upload_"+PreferencesUtil.getString(
				"driverId", "")+".txt";
	}

	private void initView() {
		tvTime = (TextView) findViewById(R.id.tv_time);
		tvBlueToothTime = (TextView) findViewById(R.id.tv_blue_tooth_time);
		tvCoord = (TextView) findViewById(R.id.tv_coord);
		tvPassenger = (TextView) findViewById(R.id.passenger);
		tvPassengerWarn = (TextView) findViewById(R.id.passenger_warn);
		tvLocationUpdata = (TextView) findViewById(R.id.location_updata);

		if (isDebug) {
			// test
			findViewById(R.id.btn_go_map).setVisibility(View.VISIBLE);
			findViewById(R.id.btn_go_map).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							Intent intent = new Intent(MainActivity.this,
									MapActivity.class);
							MainActivity.this.startActivity(intent);
						}
					});
		}

		initLocation();

		mHandler.sendEmptyMessageDelayed(UPLOAD_LOACATION, 5000);
	}

	@Override
	protected void onStart() {
		init();
		initScan();
		if (isDebug) {
			startTimer();
		}
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		LocationApp.getInstance().start();
		startLocTimer();
	}

	private void initLocation() {
		// LocationApp.getInstance().setRestartTime(2 * 60 * 60);//两小时重启一次
		LocationApp.getInstance().init(getApplicationContext());// 定位服务初始化
		LocationApp.getInstance().registerLocationListener(this);
		startRecordLocate();
	}

	private void initScan() {
		// try {
		// scanThread = new ScanThread(mHandler);
		// } catch (Exception e) {
		// // 出现异常
		// T.s("serialport init fail");
		// e.printStackTrace();
		// return;
		// }
		// scanThread.start();
		// init sound
		Util.initSoundPool(this);
	}

	private void init() {
		LoginBean b = (LoginBean) PreferencesUtil.getObj(LoginBean.PRE_LOGIN);
		if (b != null) {
			tvTime.setText("上岗时间：" + b.getLoginTime());
		}

		passengerCount = PreferencesUtil.getInt(PrePassenger, 0);
		tvPassenger.setText(passengerCount + "");
	}

	@Override
	protected void onDestroy() {
		mHandler.removeMessages(UPLOAD_LOACATION);
		stopScan();
		stopLocate();
		stopTimer();
		stopLocTimer();
		stopLocErrMedia();
		LocationApp.getInstance().unRegisterLocationListener(this);

		// 关闭”扫描服务“
		// Intent scanIntent = new Intent("com.android.scanservice.scan.off");
		// sendBroadcast(scanIntent);
		try {
			// 注销Receiver
			unregisterReceiver(mScanDataReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// 注销Receiver
			unregisterReceiver(batteryReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	private void stopTimer() {
		if (scanTimer != null) {
			scanTimer.cancel();
		}
	}

	private void stopLocate() {
		stopRecordLocate();
		LocationApp.getInstance().stop(); // 停止定位
	}

	private void stopScan() {
		if (scanThread != null) {
			scanThread.interrupt();
			scanThread.close();
			scanThread = null;
		}
	}

	public void myClick(View v) {
		switch (v.getId()) {
		case R.id.logout:// logout
			clickToLogOut();

			break;
		case R.id.img_qr:
			// startActivity(new Intent(MainActivity.this,
			// CaptureActivity.class));
			// scanThread.scan();
			// 触发扫描
			Intent intent = new Intent(
					"android.intent.action.FUNCTION_BUTTON_DOWN", null);
			sendBroadcast(intent);
			break;
		case R.id.img_camera:

			clickToCamera();

			break;
		case R.id.img_count:
			clickToAddManually();

			break;
		case R.id.img_event:
			showPopEvent(v);
			break;
		case R.id.img_close_sound:
			stopLocErrMedia();
			break;
		case R.id.location_updata:
			startLocationUpdata();
			break;
		default:
			break;
		}
	}

	/***
	 * 位置上传时间
	 *
	 *
	 */
	private void startLocationUpdata() {
		Intent intent = new Intent(MainActivity.this, FileUploadActivity.class);
		MainActivity.this.startActivity(intent);
	}

	private PopupWindow mPopupWindow;

	/***
	 * 显示特殊事件popwindow
	 *
	 * @param dropView
	 */
	private void showPopEvent(View dropView) {
		View contentView = LayoutInflater.from(this).inflate(
				R.layout.pop_event, null);

		mPopupWindow = new PopupWindow(findViewById(R.id.rl_main_root),
				MyUtil.dip2px(this, 150), MyUtil.dip2px(this, 100));

		mPopupWindow.setContentView(contentView);

		mPopupWindow.setFocusable(true);
		mPopupWindow.setBackgroundDrawable(new PaintDrawable());

		int[] locations = new int[2];
		dropView.getLocationOnScreen(locations);

		int x = locations[0] - mPopupWindow.getWidth() - 3;
		int y = locations[1] + dropView.getHeight() / 2
				- mPopupWindow.getHeight() / 2;
		mPopupWindow.showAtLocation(dropView, Gravity.NO_GRAVITY, x, y);

		contentView.findViewById(R.id.tv_start_charging).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						DialogUtil.showDialog(MainActivity.this, "提示",
								"确认开始充电？", "确定", "取消", new DialogCallBack() {
									@Override
									public void ok() {
										FileUtil.saveSpecialEventToFile("charging-start");
										T.s("已开始充电");

										mPopupWindow.dismiss();
									}

									@Override
									public void cancel() {
									}
								}).show();

					}
				});
		contentView.findViewById(R.id.tv_end_charging).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						DialogUtil.showDialog(MainActivity.this, "提示",
								"确认结束充电？", "确定", "取消", new DialogCallBack() {
									@Override
									public void ok() {
										FileUtil.saveSpecialEventToFile("charging-end");
										T.s("已结束充电");
										mPopupWindow.dismiss();
									}

									@Override
									public void cancel() {
									}
								}).show();
					}
				});
	}

	private void clickToAddManually() {
		DialogUtil.showDialog(MainActivity.this, "提示", "是否确认将乘客数加1？", "确定",
				"取消", new DialogCallBack() {
					@Override
					public void ok() {
						updatePassengerByPlusOne();

						String xy_count = App.getInstance().coordX + "_"
								+ App.getInstance().coordY + "_"
								+ App.getInstance().floor;
						FileUtil.savePassengerToFile(TYPE_COUNT, xy_count,
								"manual");
					}

					@Override
					public void cancel() {
					}
				}).show();
	}

	private void clickToLogOut() {
		DialogUtil.showDialog(MainActivity.this, "提示", "是否确认退出？", "退出", "取消",
				new DialogCallBack() {
					@Override
					public void ok() {
						logOut();
					}

					@Override
					public void cancel() {
					}
				}).show();
	}

	private void clickToCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		imagePath = getPhotoPath();
		Uri uri = Uri.fromFile(new File(imagePath));

		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

		startActivityForResult(intent, 1);
	}

	public void updatePassengerByPlusOne() {
		int last = PreferencesUtil.getInt(PrePassenger, 0);
		int curr = last + 1;
		PreferencesUtil.putInt(PrePassenger, curr);
		tvPassenger.setText(curr + "");
	}

	private void logOut() {
		stopLocate();
		PreferencesUtil.putInt(PrePassenger, 0);
		PreferencesUtil.putObj(LoginBean.PRE_LOGIN, null);
		MyUtil.doLogout();
		toLoginActivity();
		this.finish();
	}

	private void toLoginActivity() {

		startActivity(new Intent(MainActivity.this, LoginActivity.class));
	}

	private String getPhotoPath() {
		String dir = MyUtil.getSdCardPath() + "/" + FileUtil.DIR + "/"
				+ FileUtil.DIR_PASSENGER;
		String fName = TimeUtil.getTime2() + ".jpg";
		File f = new File(dir);
		if (!f.exists()) {
			f.mkdirs();
		}
		return dir + "/" + fName;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK) {
			if (!MyUtil.isSdCardExist()) {
				T.sCustom("请插入SD卡后再试-_-");
				return;
			}

			updatePassengerByPlusOne();
			String xy_camera = App.getInstance().coordX + "_"
					+ App.getInstance().coordY + "_" + App.getInstance().floor;
			FileUtil.savePassengerToFile(TYPE_IMAGE, xy_camera, imagePath);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			long secondTime = System.currentTimeMillis();
			if (secondTime - firstTime > 2000) { // 如果两次按键时间间隔大于2秒，则不退出
				T.sCustom("再按一次退出程序");
				firstTime = secondTime;// 更新firstTime
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == ScanThread.SCAN) {
				// 拿到扫描结果
				String data = msg.getData().getString("data");
				// 播放提示音
				Util.play(1, 0);
				String xy_qr = App.getInstance().coordX + "_"
						+ App.getInstance().coordY + "_"
						+ App.getInstance().floor;
				if (data.contains(AIRPORT_TAG)) {
					// 只有返回结果包含PEK才增加数值
					// 刷新计数器
					MainActivity.getInstance().updatePassengerByPlusOne();
					FileUtil.savePassengerToFile(MainActivity.TYPE_QR, xy_qr,
							data);
				} else {
					FileUtil.savePassengerToFile(MainActivity.TYPE_QR_ERR,
							xy_qr, data);
				}
			} else if (msg.what == TEST_LOCATION) {
				String timeString = TimeUtil.getFormatNowDate();
				tvBlueToothTime.setText("时间：" + timeString);
			} else if (msg.what == UPLOAD_LOACATION) {
				mHandler.sendEmptyMessageDelayed(UPLOAD_LOACATION, 5000);
				if (mLocation != null) {
					RMAsyncTask.EXECUTOR.execute(new Runnable() {

						@Override
						public void run() {
							JSONObject obj = new JSONObject();
							try {
								obj.put("deviceId", PreferencesUtil.getString(
										"deviceId", ""));// 设备编号
								obj.put("userCode", PreferencesUtil.getString(
										"driverId", ""));
								obj.put("buildingId", mLocation.getBuildID());// 建筑物ID
								obj.put("floorNo", mLocation.getFloor());//
								obj.put("xCoord", mLocation.getX());//
								obj.put("yCoord", mLocation.getY());//
								obj.put("locationTime",
										System.currentTimeMillis());// 页面端定位完成的时间戳
								Log.i("rtmap", obj.toString());
								String str = RMHttpUtil
										.postConnection("http://10.40.13.32:8080/aup/api/location/electricTruck",
//												"http://weixin.bcia.com.cn/aup/api/location/electricTruck",
//												"http://airtest.rtmap.com/aup/api/location/electricTruck",
//												"http://10.40.13.32/aup/api/location/electricTruck",
												obj.toString());
								Log.i("rtmap", "相应结果："+str);
								String content = TimeUtil.getFormatNowDate()+"###"+System.currentTimeMillis()+"###"+obj.toString()+"###"+str+"\n";
								Log.i("rtmap", "文件地址："+mFilePath+"    内容："+content);
								FileUtil.saveLogToFile(content, mFilePath);
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}// 用户编号（建筑物编号）
						}
					});

				}
			}

		}

		;
	};

	private Runnable run = new Runnable() {
		@Override
		public void run() {
			if (!isStopLocateRun) {
				String xy_camera = App.getInstance().coordX + "_"
						+ App.getInstance().coordY + "_"
						+ App.getInstance().floor + "_"
						+ App.getInstance().error + "_"
						+ App.getInstance().batteryScale;
				FileUtil.saveLocationToFile(xy_camera);

				mHandler.postDelayed(this, RecordLocateDelayed);
			}
		}
	};

	private void startRecordLocate() {
		isStopLocateRun = false;
		mHandler.post(run);
	}

	private void stopRecordLocate() {
		isStopLocateRun = true;
	}

	// @Override
	// public void onUpdateLocation(final RMLocation mydata) {
	//
	// }

	private void startTimer() {
		scanTimer = new Timer();
		scanTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				mHandler.sendEmptyMessage(TEST_LOCATION);
			}
		}, 0, 1000);
	}

	private RMLocation mLocation;

	@Override
	public void onReceiveLocation(final RMLocation rmLocation) {

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (rmLocation == null || rmLocation.error != 0) {
					tvCoord.setText("error:" + rmLocation.error);
					tvPassengerWarn.setVisibility(View.VISIBLE);
					App.getInstance().coordX = 0;
					App.getInstance().coordY = 0;
					App.getInstance().error = rmLocation.error;
					App.getInstance().floor = "F0";
					return;
				}
				mLocation = rmLocation;
				tvCoord.setText("");
				stopLocErrMedia();
				tvPassengerWarn.setVisibility(View.GONE);
				startLocTimer();
				if (timerStep != -1) {
					timerStep = 1;
				}

				App.getInstance().coordX = rmLocation.coordX;
				App.getInstance().coordY = rmLocation.coordY;
				App.getInstance().error = rmLocation.error;
				App.getInstance().floor = BuildUitl.floorTransform(rmLocation
						.getFloorID());

				if (isDebug) {
					// just for test
					int errorCode = rmLocation.error;
					if (errorCode == 0) {
						String floor = BuildUitl.floorTransform(rmLocation
								.getFloorID());
						int x = rmLocation.coordX;
						int y = rmLocation.coordY;
						String timeString = TimeUtil.getFormatNowDate();
						tvCoord.setText("楼层：" + floor + "  X:" + x + "  Y:" + y
								+ "\n  battery:"
								+ App.getInstance().batteryScale + " "
								+ timeString.split(" ")[1]);
					} else {
						tvCoord.setText("error:" + errorCode);
					}
				}
			}
		});
	}

	private Handler locTimerOutHandler = new Handler();
	private int timerOutSum = 60 * 1;// 60 * 3 3分钟
	private int timerStep = 0;
	private Runnable locTimerOutRun = new Runnable() {
		@Override
		public void run() {
			if (timerStep < timerOutSum) {
				if (timerStep == -1) {
					return;
				}
				timerStep++;
				locTimerOutHandler.postDelayed(locTimerOutRun, 1000);
			} else {
				// 定位超时
				timerStep = 0;
				// TODO 播放声音，显示关闭声音按钮
				startLocErrMedia();
			}
		}
	};

	private void startLocTimer() {
		if (timerStep == 0) {
			timerStep++;
			locTimerOutHandler.postDelayed(locTimerOutRun, 1000);
		}
	}

	private void stopLocTimer() {
		timerStep = -1;
		locTimerOutHandler.removeCallbacks(locTimerOutRun);
	}

	private void startLocErrMedia() {
		findViewById(R.id.img_close_sound).setVisibility(View.VISIBLE);
		SoundUtil.playSound(this, R.raw.loc_err_warn, false);
		FileUtil.saveSpecialEventToFile("error-sound");
	}

	private void stopLocErrMedia() {
		findViewById(R.id.img_close_sound).setVisibility(View.GONE);
		SoundUtil.stopSound();
	}

}
