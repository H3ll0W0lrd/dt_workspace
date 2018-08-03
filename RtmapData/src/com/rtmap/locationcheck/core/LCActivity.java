package com.rtmap.locationcheck.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rtm.common.utils.RMD5Util;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.location.LocationApp;
import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.core.model.BeaconInfo;
import com.rtmap.locationcheck.core.model.BeaconList;
import com.rtmap.locationcheck.core.model.BroadcastInfo;
import com.rtmap.locationcheck.core.model.Floor;
import com.rtmap.locationcheck.page.LCBeaconActivity;
import com.rtmap.locationcheck.page.LCBeaconBitmapActivity;
import com.rtmap.locationcheck.page.LCBeaconSetActivity;
import com.rtmap.locationcheck.pageNew.LCModifyActivity;
import com.rtmap.locationcheck.pageNew.LCPoiRoadActivity;
import com.rtmap.locationcheck.pageNew.LCTerminalRouteActivity;
import com.rtmap.locationcheck.pageNew.LCTerminalRouteBitmapActivity;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;
import com.rtmap.locationcheck.util.DownloadService;

public class LCActivity extends FragmentActivity implements
		OnItemSelectedListener {

	public final static int PHOTO = 0;// 相册选取
	public final static int CAMERA = 1;// 拍照
	public Dialog mLoadDialog;// 加载框
	public float adjustLength = 5;// 调整距离为1像素
	public static final float PICK_DIATANCE = 0.1f;

	/** 记录处于前台的Activity */
	private static LCActivity mForegroundActivity = null;
	public String USER_NAME;

	public Spinner mFloorSpinner, mTilteSpinner;
	public ImageView mMenu;
	public Dialog mTitleDialog;// 菜单
	public RadioButton mBitmapBtn, mVectorBtn;
	public TextView mCurrentLoc, mManager;
	public boolean isPicking;// 正在采集中
	private Handler hanlder;
	public static final int DOWN_TIME = 100;
	public boolean isOpenLocation;// 是否开启定位
	public Gson mGson = new Gson();
	public static ArrayList<LCActivity> mActivityList = new ArrayList<LCActivity>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		DTLog.e("getTaskId = " + getTaskId());
		mActivityList.add(this);
		USER_NAME = LCApplication.getInstance().getShare()
				.getString(DTFileUtils.PREFS_USERNAME, "");
		initLoad();
		adjustLength = Float.parseFloat(LCApplication.getInstance().getShare()
				.getString("step_adjust", "5"));
		hanlder = new Handler();
		isOpenLocation = LCApplication.getInstance().getShare()
				.getBoolean("open_location", true);
		super.onCreate(savedInstanceState);
	}

	public void initLocation() {
		boolean istext = LCApplication.getInstance().getShare()
				.getBoolean("istest", false);
		if (istext)
			LocationApp.getInstance().setRootFolder(
					DTFileUtils.ROOT_DIR + File.separator + "test");
		else
			LocationApp.getInstance().setRootFolder(
					DTFileUtils.ROOT_DIR + File.separator + "publish");
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
	}

	public void isPicking() {
		isPicking = true;
		mFloorSpinner.setClickable(false);
		mTilteSpinner.setClickable(false);
	}

	public void noPicking() {
		isPicking = false;
		mFloorSpinner.setClickable(true);
		mTilteSpinner.setClickable(true);
	}

	public void initTitleBar(int titleIndex) {
		com.rtmap.locationcheck.core.model.Build build = (com.rtmap.locationcheck.core.model.Build) getIntent()
				.getExtras().getSerializable("build");
		Floor floor = (Floor) getIntent().getExtras().getSerializable("floor");
		mFloorSpinner = (Spinner) findViewById(R.id.layer_type);
		// 绑定适配器和值
		final ArrayAdapter<String> provinceAdapter = new ArrayAdapter<String>(
				this, R.layout.spinner_text, build.getFloor());
		mFloorSpinner.setAdapter(provinceAdapter);
		for (int i = 0; i < build.getFloor().length; i++) {
			if (build.getFloor()[i].equals(floor.getFloor())) {
				mFloorSpinner.setSelection(i, true); // 设置默认选中项，此处为默认选中第4个值
			}
		}
		mFloorSpinner.setOnItemSelectedListener(this);

		mTilteSpinner = (Spinner) findViewById(R.id.title);
		// 绑定适配器和值
		final ArrayAdapter<String> titleAdapter = new ArrayAdapter<String>(
				this, R.layout.spinner_text, new String[] { "beacon标记",
						"POI采集", "指纹采集", "beacon巡检" });
		mTilteSpinner.setAdapter(titleAdapter);
		mTilteSpinner.setSelection(titleIndex, true); // 设置默认选中项，此处为默认选中第4个值
		mTilteSpinner.setOnItemSelectedListener(this);

		mMenu = (ImageView) findViewById(R.id.title_menu);
		mMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isPicking)
					return;
				Window win = mTitleDialog.getWindow();
				LayoutParams params = win.getAttributes();
				params.gravity = Gravity.RIGHT | Gravity.TOP;
				params.y = getResources().getDimensionPixelSize(
						R.dimen.title_height);
				win.setAttributes(params);
				mTitleDialog.show();
			}
		});

		mTitleDialog = new Dialog(this, R.style.dialog_white);
		mTitleDialog.setContentView(R.layout.title_menu_layout);
		mTitleDialog.setCanceledOnTouchOutside(true);
		mBitmapBtn = (RadioButton) mTitleDialog.findViewById(R.id.radio3);
		mVectorBtn = (RadioButton) mTitleDialog.findViewById(R.id.radio0);

		final String vector_path = DTFileUtils.MAP_DATA
				+ RMD5Util.md5(floor.getBuildId() + "_" + floor.getFloor()
						+ ".imap");// /mnt/sdcard/rtmap/mdata/860100010040500002_F2.imap
		final String bitmap_path = DTFileUtils.getImageDir()
				+ floor.getBuildId() + "-" + floor.getFloor() + ".jpg";
		if (LCActivity.this instanceof LCModifyActivity
				|| LCActivity.this instanceof LCBeaconActivity
				|| LCActivity.this instanceof LCTerminalRouteActivity) {
			mVectorBtn.setChecked(true);
		}

		if (LCActivity.this instanceof LCPoiRoadActivity
				|| LCActivity.this instanceof LCBeaconBitmapActivity
				|| LCActivity.this instanceof LCTerminalRouteBitmapActivity) {
			mBitmapBtn.setChecked(true);
		}
		mBitmapBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mTitleDialog.cancel();
				if (DTFileUtils.checkFile(bitmap_path)) {
					if (LCActivity.this instanceof LCModifyActivity) {
						Intent intent = new Intent(getApplicationContext(),
								LCPoiRoadActivity.class);
						intent.putExtras(getIntent().getExtras());
						startActivity(intent);
						finish();
					} else if (LCActivity.this instanceof LCBeaconActivity) {
						Intent intent = new Intent(getApplicationContext(),
								LCBeaconBitmapActivity.class);
						intent.putExtras(getIntent().getExtras());
						startActivity(intent);
						finish();
					} else if (LCActivity.this instanceof LCTerminalRouteActivity) {
						Intent intent = new Intent(getApplicationContext(),
								LCTerminalRouteBitmapActivity.class);
						intent.putExtras(getIntent().getExtras());
						startActivity(intent);
						finish();
					}
				} else {
					DTUIUtils.showToastSafe("当前楼层没有图片");
					hanlder.postDelayed(new Runnable() {

						@Override
						public void run() {
							mVectorBtn.setChecked(true);
						}
					}, 600);
				}
			}
		});

		mVectorBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mTitleDialog.cancel();
				if (DTFileUtils.checkFile(vector_path)) {
					if (LCActivity.this instanceof LCPoiRoadActivity) {
						Intent intent = new Intent(getApplicationContext(),
								LCModifyActivity.class);
						intent.putExtras(getIntent().getExtras());
						startActivity(intent);
						finish();
					} else if (LCActivity.this instanceof LCBeaconBitmapActivity) {
						Intent intent = new Intent(getApplicationContext(),
								LCBeaconActivity.class);
						intent.putExtras(getIntent().getExtras());
						startActivity(intent);
						finish();
					} else if (LCActivity.this instanceof LCTerminalRouteBitmapActivity) {
						Intent intent = new Intent(getApplicationContext(),
								LCTerminalRouteActivity.class);
						intent.putExtras(getIntent().getExtras());
						startActivity(intent);
						finish();
					}
				} else {
					DTUIUtils.showToastSafe("当前楼层没有矢量图");
					hanlder.postDelayed(new Runnable() {

						@Override
						public void run() {
							mBitmapBtn.setChecked(true);
						}
					}, 600);
				}
			}
		});
		mCurrentLoc = (TextView) mTitleDialog.findViewById(R.id.my_location);
		mManager = (TextView) mTitleDialog.findViewById(R.id.manager);
		mManager.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mTitleDialog.cancel();
				Intent intent = new Intent(getApplicationContext(),
						LCBeaconSetActivity.class);
				intent.putExtras(getIntent().getExtras());
				startActivity(intent);
			}
		});
		mCurrentLoc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				moveCenter();
			}
		});
		mManager.setVisibility(View.GONE);
	}

	public void showUploadDialog(String content, final String url,
			final String version, final int code) {
		final Dialog uploadDialog = new Dialog(this, R.style.dialog);
		uploadDialog.setContentView(R.layout.umeng_update_dialog);
		uploadDialog.setCanceledOnTouchOutside(true);
		TextView text = (TextView) uploadDialog
				.findViewById(R.id.umeng_update_content);
		text.setText("版本："+version+"\n"+content);
		uploadDialog.findViewById(R.id.umeng_update_id_ok).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						uploadDialog.cancel();
						Intent intent = new Intent(getApplicationContext(),
								DownloadService.class);
						intent.putExtra("url", url);
						intent.putExtra("version", version);
						intent.putExtra("code", code);
						startService(intent);
					}
				});
		uploadDialog.findViewById(R.id.umeng_update_id_cancel)
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						uploadDialog.cancel();
					}
				});
		uploadDialog.show();
	}

	public void moveCenter() {
		mTitleDialog.cancel();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mForegroundActivity = this;
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mForegroundActivity = this;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mActivityList.remove(this);
	}

	public void onViewClick(View v) {

	}

	/**
	 * 加载beacon数据
	 * 
	 * @param mBeaconFilePath
	 * @return
	 */
	public List<BeaconInfo> loadBeaconData(String mBeaconFilePath) {
		try {
			String result = DTFileUtils.readFile(mBeaconFilePath);
			if (!DTStringUtils.isEmpty(result)) {

				JSONObject o = new JSONObject(result);
				JSONArray array = o.getJSONArray("list");
				if (array.length() > 0) {
					ArrayList<BeaconInfo> list = new ArrayList<BeaconInfo>();
					for (int j = 0; j < array.length(); j++) {
						BeaconInfo info = new BeaconInfo();
						JSONObject obj = array.getJSONObject(j);
						if (obj.has("uuid"))
							info.setUuid(obj.getString("uuid"));
						if (obj.has("minor16"))
							info.setMinor16(obj.getString("minor16"));
						if (obj.has("broadcast_id"))
							info.setBroadcast_id(obj.getString("broadcast_id"));
						if (obj.has("minor"))
							info.setMinor(obj.getString("minor"));
						if (obj.has("major16"))
							info.setMajor16(obj.getString("major16"));
						if (obj.has("major"))
							info.setMajor(obj.getString("major"));
						if (obj.has("mac"))
							info.setMac(obj.getString("mac"));
						if (obj.has("inshop"))
							info.setInshop(obj.getInt("inshop"));
						if (obj.has("finger"))
							info.setFinger(obj.getInt("finger"));
						if (obj.has("edit_status"))
							info.setEdit_status(obj.getInt("edit_status"));
						if (obj.has("Threshold_switch_min"))
							info.setThreshold_switch_min(obj
									.getInt("Threshold_switch_min"));
						if (obj.has("Threshold_switch_max"))
							info.setThreshold_switch_max(obj
									.getInt("Threshold_switch_max"));
						if (obj.has("output_power"))
							info.setOutput_power(obj.getInt("output_power"));
						if (obj.has("rssi"))
							info.setRssi(obj.getInt("rssi"));
						if (obj.has("rssi_max"))
							info.setRssi_max(obj.getInt("rssi_max"));
						if (obj.has("work_status"))
							info.setWork_status(obj.getInt("work_status"));
						if (obj.has("x"))
							info.setX(obj.getInt("x"));
						if (obj.has("y"))
							info.setY(obj.getInt("y"));
						if (obj.has("buildId"))
							info.setBuildId(obj.getString("buildId"));
						if (obj.has("floor"))
							info.setFloor(obj.getString("floor"));
						if (obj.has("isClick"))
							info.setClick(obj.getBoolean("isClick"));
						if (obj.has("maclist")) {
							JSONArray maclistarray = obj
									.getJSONArray("maclist");
							info.setMaclistjson(maclistarray.toString());
//							info.setMaclist(new ArrayList<BroadcastInfo>());
//							for (int i = 0; i < maclistarray.length(); i++) {
//								JSONObject u = maclistarray.getJSONObject(i);
//								BroadcastInfo broad = new BroadcastInfo();
//								broad.setMac(u.getString("mac"));
//								broad.setMajor(u.getString("major"));
//								broad.setMinor(u.getString("minor"));
//								broad.setUuid(u.getString("uuid"));
//								info.getMaclist().add(broad);
//							}
						}
						list.add(info);
					}
					return list;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 初始化加载框
	 */
	private void initLoad() {
		mLoadDialog = new ProgressDialog(this);// 加载框
		mLoadDialog.setCanceledOnTouchOutside(false);
		mLoadDialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (mLoadDialog.isShowing() && keyCode == KeyEvent.KEYCODE_BACK) {
					cancelLoadDialog();
					mLoadDialog.cancel();
				}
				return false;
			}
		});
	}

	public void cancelLoadDialog() {
	}

	/**
	 * 得到图片的路径
	 * 
	 * @param fileName
	 * @param requestCode
	 * @param data
	 * @return
	 */
	public String getFilePath(String fileName, int requestCode, Intent data) {
		if (requestCode == CAMERA) {
			return fileName;
		} else if (requestCode == PHOTO) {
			Uri uri = data.getData();
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
			int actual_image_column_index = actualimagecursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			actualimagecursor.moveToFirst();
			String img_path = actualimagecursor
					.getString(actual_image_column_index);
			// 4.0以上平台会自动关闭cursor,所以加上版本判断,OK
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				actualimagecursor.close();
			return img_path;
		}
		return null;
	}

	/**
	 * 验证手机号码
	 * 
	 * @param mobiles
	 * @return [0-9]{5,9}
	 */
	public boolean isMobileNO(String mobiles) {
		try {
			Pattern p = Pattern
					.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
			Matcher m = p.matcher(mobiles);
			return m.matches();
		} catch (Exception e) {
		}
		return false;
	}

	/** 获取当前处于前台的activity */
	public static LCActivity getForegroundActivity() {
		return mForegroundActivity;
	}

	public void onFloorSelected(String floor) {

	}

	public void onTitleSelected(int position) {

	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View view, int position,
			long arg3) {
		if (isPicking)
			return;
		Floor floor = (Floor) getIntent().getExtras().getSerializable("floor");
		String vector_path = DTFileUtils.MAP_DATA
				+ RMD5Util.md5(floor.getBuildId() + "_" + floor.getFloor()
						+ ".imap");// /mnt/sdcard/rtmap/mdata/860100010040500002_F2.imap
		String bitmap_path = DTFileUtils.getImageDir() + floor.getBuildId()
				+ "-" + floor.getFloor() + ".jpg";
		if (arg0.getId() == R.id.layer_type) {
			com.rtmap.locationcheck.core.model.Build build = (com.rtmap.locationcheck.core.model.Build) getIntent()
					.getExtras().getSerializable("build");
			if (!build.getFloor()[position].equals(floor.getFloor())) {// 如果楼层不一样，则切换地图

				ArrayList<Floor> list = new ArrayList<Floor>();
				for (int i = 0; i < build.getScale().size(); i++) {
					if (build.getFloor()[position].equals(build.getScale()
							.get(i).getFloor())) {
						Floor f = build.getScale().get(i);
						final Intent intent = new Intent();
						final Bundle bundle = new Bundle();
						bundle.putSerializable("floor", f);
						String vector_p = DTFileUtils.MAP_DATA
								+ RMD5Util.md5(f.getBuildId() + "_"
										+ f.getFloor() + ".imap");// /mnt/sdcard/rtmap/mdata/860100010040500002_F2.imap
						String bitmap_p = DTFileUtils.getImageDir()
								+ f.getBuildId() + "-" + f.getFloor() + ".jpg";
						bundle.putSerializable("build", build);
						intent.putExtras(bundle);
						if (DTFileUtils.checkFile(vector_p)) {// 如果有矢量图
							intent.setClass(this, this.getClass());
							startActivity(intent);
							this.finish();
						} else if (DTFileUtils.checkFile(bitmap_p)) {
							if (f.getScale() != 0) {
								intent.setClass(this, this.getClass());
								startActivity(intent);
								this.finish();
							} else {
								DTUIUtils.showToastSafe(f.getFloor() + "比例尺为0");
								for (int j = 0; j < build.getFloor().length; j++) {
									if (build.getFloor()[j].equals(floor
											.getFloor())) {
										mFloorSpinner.setSelection(j, true); // 设置默认选中项，此处为默认选中第4个值
									}
								}
							}
						} else {
							DTUIUtils.showToastSafe(f.getFloor() + "请下载地图");
							for (int j = 0; j < build.getFloor().length; j++) {
								if (build.getFloor()[j]
										.equals(floor.getFloor())) {
									mFloorSpinner.setSelection(j, true); // 设置默认选中项，此处为默认选中第4个值
								}
							}
						}
						break;
					}
				}
				list.add(floor);
				build.setScale(list);
			}

			onFloorSelected(build.getFloor()[position]);
		} else if (arg0.getId() == R.id.title) {

			if (position == 2) {
				if (DTFileUtils.checkFile(vector_path)) {// 如果有矢量图
					if (!(this instanceof LCTerminalRouteActivity)) {
						Intent intent = new Intent(this,
								LCTerminalRouteActivity.class);
						Bundle bundle = getIntent().getExtras();
						intent.putExtras(bundle);
						startActivity(intent);
						this.finish();
					}
				} else if (DTFileUtils.checkFile(bitmap_path)) {
					if (!(this instanceof LCTerminalRouteBitmapActivity)) {
						Intent intent = new Intent(this,
								LCTerminalRouteBitmapActivity.class);
						Bundle bundle = getIntent().getExtras();
						intent.putExtras(bundle);
						startActivity(intent);
						this.finish();
					}
				}
			} else if (position == 1) {
				if (DTFileUtils.checkFile(vector_path)) {// 如果有矢量图
					if (!(this instanceof LCModifyActivity)) {
						Intent intent = new Intent(this, LCModifyActivity.class);
						Bundle bundle = getIntent().getExtras();
						intent.putExtras(bundle);
						startActivity(intent);
						this.finish();
					}
				} else if (DTFileUtils.checkFile(bitmap_path)) {
					if (!(this instanceof LCPoiRoadActivity)) {
						Intent intent = new Intent(this,
								LCPoiRoadActivity.class);
						Bundle bundle = getIntent().getExtras();
						intent.putExtras(bundle);
						startActivity(intent);
						this.finish();
					}
				}
			} else if (position == 0 || position == 3) {

				if (DTFileUtils.checkFile(vector_path)) {// 如果有矢量图
					if (!(this instanceof LCBeaconActivity)) {
						Intent intent = new Intent(this, LCBeaconActivity.class);
						Bundle bundle = getIntent().getExtras();
						bundle.putInt("position", position);
						intent.putExtras(bundle);
						startActivity(intent);
						this.finish();
					} else {
						onTitleSelected(position);
					}
				} else if (DTFileUtils.checkFile(bitmap_path)) {
					if (!(this instanceof LCBeaconBitmapActivity)) {
						Intent intent = new Intent(this,
								LCBeaconBitmapActivity.class);
						Bundle bundle = getIntent().getExtras();
						bundle.putInt("position", position);
						intent.putExtras(bundle);
						startActivity(intent);
						this.finish();
					} else {
						onTitleSelected(position);
					}
				}
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}
}
