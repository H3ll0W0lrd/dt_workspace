package com.rtmap.experience.page;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.model.RMLocation;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;
import com.rtmap.experience.R;
import com.rtmap.experience.adapter.KPBeaconDialogAdapter;
import com.rtmap.experience.core.KPActivity;
import com.rtmap.experience.core.KPApplication;
import com.rtmap.experience.core.model.BeaconInfo;
import com.rtmap.experience.core.model.BeaconList;
import com.rtmap.experience.core.model.BuildInfo;
import com.rtmap.experience.core.model.Floor;
import com.rtmap.experience.core.model.LCPoint;
import com.rtmap.experience.layer.LocationLayer;
import com.rtmap.experience.layer.MapCorrectionLayer;
import com.rtmap.experience.layer.OnLayerPointClickListener;
import com.rtmap.experience.layer.PinMark;
import com.rtmap.experience.layer.PointLayer;
import com.rtmap.experience.util.DTFileUtils;
import com.rtmap.experience.util.DTLog;
import com.rtmap.experience.util.DTMathUtils;
import com.rtmap.experience.util.DTStringUtils;
import com.rtmap.experience.util.DTUIUtils;
import com.rtmap.experience.util.map.MapWidget;
import com.rtmap.experience.util.map.MapWidget.OnActionUpListener;
import com.rtmap.experience.util.map.MapWidget.OnMouseListener;
import com.rtmap.experience.util.map.MapWidget.WidgetStateListener;
import com.rtmap.experience.util.view.DTWheelView;
import com.rtmap.experience.util.view.NumericWheelAdapter;

public class KPMapActivity extends KPActivity implements OnClickListener,
		OnLayerPointClickListener, RMLocationListener, OnActionUpListener {

	private MapWidget mMapView;
	private MapCorrectionLayer mRuleLayer;// 采集点图层
	private PointLayer mBeaconLayer;
	private static Floor mFloor;
	private ImageView mMark;// 标记比例尺
	private TextView mRuleText;// 比例
	private PinMark pinMark;
	private Dialog mRuleDialog;
	private BeaconList mBeaconList;
	// private Button mLocation;
	private Gson mGson;
	private LocationLayer mLocationLayer;
	private static BuildInfo mBuild;

	private TextView mInfoText;// beacon信息
	private Button mDeleteReStore;// 删除或者重置，修改按钮

	public static void interActivity(Context context, Floor floor,
			BuildInfo build) {
		mBuild = build;
		mFloor = floor;
		Intent intent = new Intent(context, KPMapActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kp_map);
		mLoadDialog.setCanceledOnTouchOutside(false);
		mGson = new Gson();
		mMark = (ImageView) findViewById(R.id.mark);
		mRuleText = (TextView) findViewById(R.id.rule_text);
		// mLocation = (Button) findViewById(R.id.location);
		((TextView) findViewById(R.id.title)).setText(mBuild.getName() + "-"
				+ mFloor.getFloor());
		if (mFloor.getScale() == 0)
			mMark.setVisibility(View.VISIBLE);
		else
			mMark.setVisibility(View.GONE);

		mRuleText
				.setText(getString(R.string.rule_text, mFloor.getScale() * 55));
		mRuleText.setOnClickListener(this);
		mMark.setOnClickListener(this);
		// mLocation.setOnClickListener(this);
		mMapView = (MapWidget) findViewById(R.id.map_view);
		mMapView.registerWidgetStateListener(new WidgetStateListener() {
			@Override
			public void onMapWidgetCreated(MapWidget map) {
				String bitmap_path = DTFileUtils.getDataDir()
						+ mFloor.getBuildId() + File.separator
						+ mFloor.getBuildId() + "-" + mFloor.getFloor()
						+ ".jpg";
				mMapView.openMapFile(bitmap_path);
			}
		});
		pinMark = new PinMark(mMapView, R.drawable.pin48);
		pinMark.setVisiable(true);
		pinMark.setLocation(getResources().getDisplayMetrics().widthPixels / 2,
				getResources().getDisplayMetrics().heightPixels / 2);
		// 添加标记点图层及采集点图层
		mMapView.addMark(pinMark);
		// 添加标记点图层及采集点图层
		mRuleLayer = new MapCorrectionLayer();
		mMapView.addMark(mRuleLayer);
		mBeaconLayer = new PointLayer(mFloor.getScale());
		mMapView.addMark(mBeaconLayer);
		mLocationLayer = new LocationLayer(mFloor, mMapView);
		mMapView.addMark(mLocationLayer);
		mMapView.registerMouseListener(new OnMouseListener() {
			@Override
			public void onSingleTap(MapWidget mw, float x, float y) {
				mBeaconLayer.onSingleTap(mw, x, y, KPMapActivity.this);
			}
		});
		initDownDialog();
		initBeaconInfo();
		initBeaconInfoDilog();
		initADDialog();
		mMapView.setOnActionUpListener(this);

		// if (mBeaconLayer.getCorrectPoints().size() > 0) {
		// mLocation.setVisibility(View.VISIBLE);
		// } else {
		// mLocation.setVisibility(View.GONE);
		// }
		initLocation();
	}

	private void initLocation() {
		LocationApp.getInstance().init(getApplicationContext());
		LocationApp.getInstance().registerLocationListener(this);
		LocationApp.getInstance().setRootFolder(DTFileUtils.ROOT_DIR);
		LocationApp.getInstance().setLbsSign(LocationApp.OFFLINE);
		RMHttpUrl.FILE_INFO_URL = "http://api.rtmap.com:8081/rtmap/experience/getLastestOfflineVersionInfo";
		RMHttpUrl.DOWNLOAD_URL = "http://api.rtmap.com:8081/rtmap/experience/downloadFile";
	}

	private Dialog mInfoDialog;

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

	private Dialog mABDialog;

	/**
	 * show弹出框
	 */
	private void initADDialog() {
		mABDialog = new Dialog(this, R.style.dialog_white);
		mABDialog.setContentView(R.layout.beacon_control_dialog);
		mABDialog.setCanceledOnTouchOutside(false);
		mABDialog.setCancelable(false);
		WindowManager.LayoutParams lp = mABDialog.getWindow().getAttributes();
		lp.width = WindowManager.LayoutParams.MATCH_PARENT; // 设置宽度
		lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		mABDialog.getWindow().setAttributes(lp);

		(mABDialog.findViewById(R.id.edit)).setOnClickListener(this);
		(mABDialog.findViewById(R.id.delete)).setOnClickListener(this);
	}

	/**
	 * 导入beacon数据
	 */
	private void initBeaconInfo() {

		String result = KPApplication.getInstance().getShare()
				.getString(DTFileUtils.BEACON_INFO, null);
		if (!DTStringUtils.isEmpty(result)) {
			mBeaconList = mGson.fromJson(result, BeaconList.class);
			if (mBeaconList != null && mBeaconList.getBeacons() != null)
				for (int i = 0; i < mBeaconList.getBeacons().size(); i++) {
					BeaconInfo info = mBeaconList.getBeacons().get(i);
					if (mFloor.getBuildId().equals(info.getBuildId()))
						mBeaconLayer.addCorrectPoint(info);
				}
		}
	}

	/**
	 * 比例尺信息框
	 */
	private void initDownDialog() {
		mRuleDialog = new Dialog(this, R.style.dialog);
		mRuleDialog.setContentView(R.layout.dialog_map_rule);
		final EditText edit = (EditText) mRuleDialog.findViewById(R.id.number);
		mRuleDialog.findViewById(R.id.ok).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						String number = edit.getText().toString();
						if (DTStringUtils.isEmpty(number))
							DTUIUtils.showToastSafe("请输入长度");
						else {
							mRuleDialog.cancel();
							int length = Integer.parseInt(number);
							LCPoint point1 = mRuleLayer.getCorrectPoints().get(
									0);
							LCPoint point2 = mRuleLayer.getCorrectPoints().get(
									1);
							float px = DTMathUtils.distance(point1.getX(),
									point2.getY(), point2.getX(), point2.getY());
							int pxToMeter = (int) (length / px);
							mFloor.setScale(pxToMeter);
							String path = DTFileUtils.getDataDir()
									+ mBuild.getBuildId() + File.separator;
							DTFileUtils.writeFile(mGson.toJson(mBuild), path
									+ mBuild.getBuildId() + ".build", false);
							mRuleLayer.clearAllCorrectPoints();
							mRuleText.setText(getString(R.string.rule_text,
									mFloor.getScale() * 55));
						}
					}
				});
		mRuleDialog.findViewById(R.id.cancel).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						mRuleDialog.cancel();
					}
				});
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
		if (mBeaconList != null)
			KPApplication
					.getInstance()
					.getShare()
					.edit()
					.putString(DTFileUtils.BEACON_INFO,
							mGson.toJson(mBeaconList)).commit();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onPause();
		LocationApp.getInstance().unRegisterLocationListener(this);
		LocationApp.getInstance().stop();
		mRuleLayer = null;
		mMapView = null;
	}

	@Override
	public void onClick(View v) {
		// if (v.getId() != R.id.location
		// && !mLocation.getText().equals(getString(R.string.startLocate))) {
		// DTUIUtils.showToastSafe("请停止定位");
		// return;
		// }
		switch (v.getId()) {
		case R.id.delete:
			mBeaconLayer.removeLastBeacon();
			mABDialog.cancel();
			break;

		case R.id.edit:// 添加beacon
			Intent intent = new Intent(KPMapActivity.this,
					KPAddBeaconActivity.class);

			Bundle bundle = new Bundle();
			bundle.putSerializable("beacon", mBeaconLayer.getCorrectPoints()
					.get(mBeaconLayer.getCount() - 1));
			intent.putExtras(bundle);
			mBeaconLayer.removeLastBeacon();
			mABDialog.cancel();
			startActivityForResult(intent, 1);
			break;
		// case R.id.location:
		// if (mBeaconLayer.getCorrectPoints().size() == 0) {
		// DTUIUtils.showToastSafe("请添加beacon并上传");
		// }
		// if (mLocation.getText().equals(getString(R.string.startLocate))) {
		// boolean result = LocationApp.getInstance().start();
		// if (result)
		// mLocation.setText(getString(R.string.stopLocate));
		// else
		// Toast.makeText(this, "请输入key", Toast.LENGTH_LONG).show();
		// } else {
		// LocationApp.getInstance().stop(); // 停止定位
		// mLocation.setText(getString(R.string.startLocate));
		// }
		// break;
		case R.id.mark:
			if (mRuleLayer.getCorrectPoints().size() == 2) {// 弹出比例尺框
				mRuleDialog.show();
			} else {
				mark();
				if (mRuleLayer.getCorrectPoints().size() == 2)
					mRuleDialog.show();
			}
			break;
		case R.id.rule_text:
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setMessage("重建比例尺吗？");
			dialog.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							mRuleText.setText(getString(R.string.rule_text, 0));
							mMark.setVisibility(View.VISIBLE);
						}
					});
			dialog.setNegativeButton("取消", null);
			dialog.create().show();
			break;
		case R.id.delete_restore:
			mBeaconInfo.setThreshold_switch_max(0);
			mBeaconInfo.setThreshold_switch_min(0);
			mBeaconInfo.setFloor(0);
			mBeaconInfo.setX(0);
			mBeaconInfo.setY(0);
			mBeaconInfo.setBuildId(null);
			if (mBeaconList == null) {
				mBeaconList = new BeaconList();
				mBeaconList.setBeacons(new ArrayList<BeaconInfo>());
				mBeaconList.setBroadcasts(new ArrayList<BeaconInfo>());
			}
			for (BeaconInfo i : mBeaconList.getBeacons()) {
				if (i.getMajor() == mBeaconInfo.getMajor()
						&& i.getMinor() == mBeaconInfo.getMinor()) {
					mBeaconList.getBeacons().remove(i);
					break;
				}
			}
			for (BeaconInfo i : mBeaconList.getBroadcasts()) {
				if (i.getMajor() == mBeaconInfo.getMajor()
						&& i.getMinor() == mBeaconInfo.getMinor()) {
					mBeaconList.getBroadcasts().remove(i);
					break;
				}
			}
			mBeaconList.getBroadcasts().add(mBeaconInfo);
			mBeaconLayer.getCorrectPoints().remove(mBeaconInfo);
			KPApplication
					.getInstance()
					.getShare()
					.edit()
					.putString(DTFileUtils.BEACON_INFO,
							mGson.toJson(mBeaconList)).commit();
			mInfoDialog.cancel();
			break;
		}
	}

	private void mark() {
		LCPoint point = new LCPoint();
		mMapView.getLCPointTransformer().clientToWorld(pinMark.getX(),
				pinMark.getY(), point);
		mRuleLayer.addCorrectPoint(point);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == Activity.RESULT_OK) {
			Bundle bundle = intent.getExtras();
			BeaconInfo info = (BeaconInfo) bundle.getSerializable("beacon");
			if (requestCode == 1) {
				if (mBeaconList == null) {
					mBeaconList = new BeaconList();
					mBeaconList.setBeacons(new ArrayList<BeaconInfo>());
					mBeaconList.setBroadcasts(new ArrayList<BeaconInfo>());
				}
				if (mBeaconList.getBeacons() != null) {
					for (BeaconInfo i : mBeaconList.getBeacons()) {
						if (i.getMajor() == info.getMajor()
								&& i.getMinor() == info.getMinor()) {
							mBeaconList.getBeacons().remove(i);
							break;
						}
					}
					mBeaconList.getBeacons().add(info);
				} else {
					mBeaconList.setBeacons(new ArrayList<BeaconInfo>());
				}
				if (mBeaconList.getBroadcasts() != null) {
					for (BeaconInfo i : mBeaconList.getBroadcasts()) {
						if (i.getMajor() == info.getMajor()
								&& i.getMinor() == info.getMinor()) {
							mBeaconList.getBroadcasts().remove(i);
							break;
						}
					}
				} else {
					mBeaconList.setBroadcasts(new ArrayList<BeaconInfo>());
				}
				for (BeaconInfo i : mBeaconLayer.getCorrectPoints()) {
					if (i.getMajor() == info.getMajor()
							&& i.getMinor() == info.getMinor()) {
						mBeaconLayer.getCorrectPoints().remove(i);
						break;
					}
				}
				mBeaconLayer.addCorrectPoint(info);
			}
		}
	}

	private BeaconInfo mBeaconInfo;

	@Override
	public void onClick(BeaconInfo info) {
		mBeaconInfo = info;
		mBeaconInfo.setClick(true);
		Point temppoi = new Point();
		mMapView.getLCPointTransformer().worldToClient(
				info.getX() * 1.0f / mFloor.getScale(),
				info.getY() * 1.0f / mFloor.getScale(), temppoi);// 得到beaconInfo的屏幕坐标
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
		mMapView.getLCPointTransformer().bitmapTranslate(difX,
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

	@Override
	public void onReceiveLocation(RMLocation location) {
		DTLog.e("\nerror: " + location.getError() + "\nbuild: "
				+ location.getBuildID() + "\nfloor: " + location.getFloorID()
				+ "\nx: " + location.getCoordX() + "\ny:  "
				+ location.getCoordY() + "\naccuracy:" + location.getAccuracy()
				+ "\ntime:" + System.currentTimeMillis());
		if (location.getError() == 0
				&& mFloor.getBuildId().equals(location.getBuildID())) {
			mLocationLayer.setLocation(location.getCoordX(),
					location.getCoordY());
		}
	}

	@Override
	public void onActionUp() {
		if (mFloor.getScale() != 0) {
			LCPoint point = new LCPoint();

			mMapView.getLCPointTransformer().clientToWorld(pinMark.getX(),
					pinMark.getY(), point);
			BeaconInfo info = new BeaconInfo();
			info.setX(point.getX() * mFloor.getScale());
			info.setY(point.getY() * mFloor.getScale());
			info.setBuildId(mBuild.getBuildId());
			info.setFloor(DTStringUtils.floorTransform(mFloor.getFloor()));
			mBeaconLayer.addCorrectPoint(info);
			mABDialog.show();
		}
	}
}
