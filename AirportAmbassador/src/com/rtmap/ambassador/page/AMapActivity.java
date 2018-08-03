package com.rtmap.ambassador.page;

import im.fir.sdk.FIR;
import im.fir.sdk.VersionCheckCallback;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.Constants;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.utils.Handlerlist;
import com.rtm.location.LocationApp;
import com.rtm.location.sensor.BeaconSensor;
import com.rtm.location.utils.RMLocationListener;
import com.rtmap.ambassador.R;
import com.rtmap.ambassador.core.DTActivity;
import com.rtmap.ambassador.core.DTApplication;
import com.rtmap.ambassador.core.DTAsyncTask;
import com.rtmap.ambassador.core.DTSqlite;
import com.rtmap.ambassador.http.DTHttpUrl;
import com.rtmap.ambassador.http.DTHttpUtil;
import com.rtmap.ambassador.layer.AreaLayer;
import com.rtmap.ambassador.model.Area;
import com.rtmap.ambassador.model.Request;
import com.rtmap.ambassador.util.DTLog;
import com.rtmap.ambassador.util.DTMathUtil;
import com.rtmap.ambassador.util.DTStringUtil;
import com.rtmap.ambassador.util.DTUIUtil;
import com.rtmap.ambassador.util.QRCodeUtil;

public class AMapActivity extends DTActivity implements RMLocationListener,
		OnClickListener {

	private Handler mHandler = new Handler() {// 下载地图过程中下载进度消息
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 10:
				mMapView.setCenter(
						(mArea.getLeft() + mArea.getRight()) / 2,
						(mArea.getBottom() + mArea.getTop()) / 2, false);
				float a = (mArea.getRight() - mArea.getLeft())
						/ mMapView.getWidth()*1.4f;
				float b = (mArea.getBottom() - mArea.getTop())
						/ mMapView.getHeight()*1.4f;
				mMapView.setScale(a > b ? a : b);
				mLoadDialog.cancel();
				break;
			case Constants.RTMAP_MAP:
				int progress = msg.arg1;
				if (progress == Constants.MAP_LOAD_START) {// 开始加载
					Log.e("rtmap", "开始加载");
				} else if (progress == Constants.MAP_FailNetResult) {// 校验结果失败
					Log.e("rtmap", "地图校验结果：" + (String) msg.obj);
				} else if (progress == Constants.MAP_FailCheckNet) {// 联网检测失败
					Log.e("rtmap", "校验联网失败");
				} else if (progress == Constants.MAP_Down_Success) {
					Log.e("rtmap", "地图下载成功");
					Toast.makeText(getApplicationContext(), "地图下载成功",
							Toast.LENGTH_LONG).show();
				} else if (progress == Constants.MAP_Down_Fail) {
					Log.e("rtmap", "地图下载失败");
					Toast.makeText(getApplicationContext(), "地图下载失败",
							Toast.LENGTH_LONG).show();
				} else if (progress == Constants.MAP_Update_Success) {
					Log.e("rtmap", "地图更新成功");
					Toast.makeText(getApplicationContext(), "地图更新成功",
							Toast.LENGTH_LONG).show();
				} else if (progress == Constants.MAP_Update_Fail) {
					Log.e("rtmap", "地图更新失败");
					Toast.makeText(getApplicationContext(), "地图更新失败",
							Toast.LENGTH_LONG).show();
				} else if (progress == Constants.MAP_LOAD_END) {
					Log.e("rtmap", "地图加载完成");
					sendEmptyMessageAtTime(10, 800);
				} else if (progress == Constants.MAP_LICENSE) {
					Log.e("rtmap", "license校验结果：" + (String) msg.obj);
				}
				break;
			}
		}
	};

	MapView mMapView;
	private Area mArea;
	private AreaLayer mAreaLayer;
	private TextView mSign;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.am_map);
		Handlerlist.getInstance().register(mHandler);
		initLocation();
		XunluMap.getInstance().init(getApplicationContext());// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		mMapView.setResetMapScale(false);
		mMapView.setResetMapCenter(false);
		try {
			mMapView.setLocationIcon(
					BitmapFactory.decodeStream(getAssets().open(
							"icon_locr_normal.png")),
					BitmapFactory.decodeStream(getAssets().open(
							"icon_locr_light.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		mAreaLayer = new AreaLayer(mMapView);
		mMapView.addLayer(mAreaLayer);

		mSign = (TextView) findViewById(R.id.sign);
		findViewById(R.id.center).setOnClickListener(this);
		findViewById(R.id.sacnner).setOnClickListener(this);

		initImageDialog();

		BeaconSensor.getInstance().init(getApplicationContext());
		if (!BeaconSensor.getInstance().isBlueToothOpen()) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent();
							intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							try {
								startActivity(intent);
							} catch (ActivityNotFoundException ex) {
								ex.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
			dialog.setMessage("导航需要蓝牙设备支持，是否打开蓝牙？");
			dialog.create().show();
		}
		checkUpdate();
	}
	
	/**
	 * 检查更新
	 */
	private void checkUpdate() {

		// {"name":"数据采集","version":"27","changelog":"1 修复复杂面上传导致面缺失的问题；\r\n
		// 5 添加照片审核流程。\r\n6 优化beacon状态选择。","updated_at":1472807278,
		// "versionShort":"4.3.1","build":"27",
		// "installUrl":"http://download.fir.im/v2/app/install/56f3b19d748aac1915000028?download_token=7499c5c8337dc38d8df4f718485042f9","install_url":"http://download.fir.im/v2/app/install/56f3b19d748aac1915000028?download_token=7499c5c8337dc38d8df4f718485042f9","direct_install_url":"http://download.fir.im/v2/app/install/56f3b19d748aac1915000028?download_token=7499c5c8337dc38d8df4f718485042f9","update_url":"http://fir.im/xwc3",
		// "binary":{"fsize":5522602}}

		FIR.checkForUpdateInFIR("70cedea02e5dfb7a81d5c6baabb666fc",
				new VersionCheckCallback() {
					@Override
					public void onSuccess(String versionJson) {
						DTLog.i("check from fir.im success! " + "\n"
								+ versionJson);
						try {
							JSONObject o = new JSONObject(versionJson);
							int versionCode = Integer.parseInt(o
									.getString("version"));
							if (versionCode > DTApplication.VERSION_CODE) {
								showUploadDialog(o.getString("changelog"),
										o.getString("installUrl"),
										o.getString("versionShort"),
										versionCode);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onFail(Exception exception) {
						Log.i("fir",
								"check fir.im fail! " + "\n"
										+ exception.getMessage());
					}

					@Override
					public void onStart() {
					}

					@Override
					public void onFinish() {
					}
				});
	}

	@Override
	protected void onResume() {
		super.onResume();
		LocationApp.getInstance().registerLocationListener(this);
		LocationApp.getInstance().start();// 开始定位
		String result = DTApplication.getInstance().getShare()
				.getString(DTStringUtil.PREFS_AREA, null);
		Area area = mGson.fromJson(result, Area.class);
		if (mArea == null || !area.getAreaCode().equals(mArea.getAreaCode())) {
			isFrist = true;
		}
		mArea = area;
		mArea.getCoords();
		mAreaLayer.setArea(mArea);
		if (isFrist){
			mLoadDialog.show();
			mMapView.initMapConfig(mArea.getBuildingId(), mArea.getFloorNo());
		}
		uploadCache();
	}

	private void uploadCache() {
		final ArrayList<Request> list = DTSqlite.getInstance().getRequestList();
		if (list.size() > 0) {
			DTAsyncTask.execute(new Runnable() {

				@Override
				public void run() {
					for (Request q : list) {
						DTLog.e("缓存信息：");
						String result = DTHttpUtil.postConnection(
								q.getUrl(), q.getParams());
						if (result != null) {
							DTSqlite.getInstance().deleteRequest(q.getId());
						}
					}
				}
			});
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocationApp.getInstance().stop();
		LocationApp.getInstance().unRegisterLocationListener(this);
	}

	/**
	 * 初始化定位
	 */
	private void initLocation() {
		LocationApp.getInstance().init(getApplicationContext());// 初始化定位
	}

	private Dialog mScannerDialog;
	private ImageView mImage;

	private void initImageDialog() {
		mScannerDialog = new Dialog(this, R.style.dialog);
		mScannerDialog.setContentView(R.layout.image_layout);
		mImage = (ImageView) mScannerDialog.findViewById(R.id.imageView1);
		mScannerDialog.setCanceledOnTouchOutside(true);
		mImage.setImageBitmap(QRCodeUtil.createQRImage(mUser.getQrCode(), 400,
				400, null));
	}

	private RMLocation mLocation;
	private boolean isOver;// 是否在区域内，true在
	private int count;
	private boolean isFrist = true;

	@Override
	public void onReceiveLocation(final RMLocation result) {
		if (result.getError() == 0) {
			// result.setX(460);
			// result.setY(440);
			// result.setBuildID(mArea.getBuildingId());
			// result.setFloor(mArea.getFloorNo());

			Log.i("rtmap",
					"result : " + result.getCoordX() + "    "
							+ result.getCoordY() + "   " + result.getFloorID());
			boolean c = false;
			if (!mArea.getBuildingId().equals(result.getBuildID())) {
				mSign.setText("您正在其他建筑物内\n请回到执勤区域");
				mSign.setVisibility(View.VISIBLE);
			} else {
				if (!mArea.getFloorNo().equals(result.getFloor())) {
					mSign.setText("您正在" + result.getFloor() + "层\n请回到执勤区域");
					mSign.setVisibility(View.VISIBLE);
				} else {
					if (DTMathUtil.containsPoint(result.x, Math.abs(result.y),
							mArea.getCoords())) {
						mSign.setVisibility(View.GONE);
						c = true;// 在区域内
					} else {
						mSign.setText("请回到执勤区域");
						mSign.setVisibility(View.VISIBLE);
					}
				}
			}
			count++;
			if ((!c && isOver) || (!c && isFrist)) {// 本次不在区域内，上次在区域内或者本次不在区域内但是首次进入页面或者
				boolean open = DTApplication.getInstance().getShare()
						.getBoolean(DTStringUtil.PREFS_ALERT, true);
				if (open) {
					Uri notification = RingtoneManager
							.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
					Ringtone r = RingtoneManager.getRingtone(
							getApplicationContext(), notification);
					r.play();
				}
			}
			isOver = c;
			isFrist = false;
			if (count % 10 == 0) {// 上传
				DTAsyncTask.execute(new Runnable() {

					@Override
					public void run() {
						DTHttpUtil.connInfo(
								DTHttpUtil.POST,
								DTHttpUrl.UPLOAD_LOC,
								new String[] { "deviceId", "staffCode",
										"buildingId", "floorNo", "xCoord",
										"yCoord", "areaCode", "inArea",
										"clientTime" },
								new Object[] { DTApplication.MAC,
										mUser.getStaffCode(),
										result.getBuildID(), result.getFloor(),
										result.getX(), result.getY(),
										mArea.getAreaCode(), isOver,
										System.currentTimeMillis() });
					}
				});
			}
			mLocation = result;
			mMapView.setMyCurrentLocation(result);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.center:
			Intent intent = new Intent(getApplicationContext(),
					AMCenterActivity.class);
			startActivity(intent);
			break;
		case R.id.sacnner:
			mScannerDialog.show();
			break;
		default:
			break;
		}
	}

	@Override
	public String getPageName() {
		return null;
	}
}
