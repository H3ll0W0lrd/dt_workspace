package com.airport.test.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.airport.test.R;
import com.airport.test.adapter.FloorAdapter;
import com.airport.test.ar.ArManager;
import com.airport.test.ar.ArManager.ArLocation;
import com.airport.test.core.APActivity;
import com.airport.test.core.AirSqlite;
import com.airport.test.model.AirData;
import com.airport.test.model.MsgData;
import com.airport.test.util.VibratorUtil;
import com.dingtao.libs.DTApplication;
import com.dingtao.libs.util.DTIOUtil;
import com.dingtao.libs.util.DTLog;
import com.dingtao.libs.util.DTStringUtil;
import com.dingtao.libs.util.DTUIUtil;
import com.google.gson.Gson;
import com.rtm.common.model.BuildInfo;
import com.rtm.common.model.Floor;
import com.rtm.common.model.POI;
import com.rtm.common.model.RMLocation;
import com.rtm.common.model.RMPois;
import com.rtm.common.style.DrawStyle;
import com.rtm.common.utils.Constants;
import com.rtm.common.utils.OnSearchPoiListener;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.POILayer;
import com.rtm.frm.map.RouteLayer;
import com.rtm.frm.map.TapPOILayer;
import com.rtm.frm.map.TapPOILayer.OnPOITappedListener;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.RMPoiDetail;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.utils.Handlerlist;
import com.rtm.frm.utils.RMNavigationUtil;
import com.rtm.frm.utils.RMNavigationUtil.OnNavigationListener;
import com.rtm.frm.utils.RMPoiDetailUtil;
import com.rtm.frm.utils.RMSearchPoiUtil;
import com.rtm.frm.utils.RMathUtils;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;

/**
 * 定位显示页面 这个页面需要定位功能支持，如果你只需要地图功能，可以移除以下文件和配置信息：1.rtmap_lbs_location_v*.
 * jar和所有libIndoorLoc.so库；2.AndroidManifest.xml中的定位服务配置；
 * 
 * @author dingtao
 *
 */
public class MapActivity extends APActivity implements RMLocationListener,
		OnClickListener, OnQueryTextListener, OnSearchPoiListener,
		OnNavigationListener, OnItemClickListener {

	private MapView mMapView;// 地图view

	private CompassLayer mCompassLayer;// 指南针图层
	private TapPOILayer mTapPoiLayer;
	private RouteLayer mRouteLayer;
	private POILayer mPoiLayer, mNaPoiLayer;

	private RelativeLayout mTitleLayout;
	private TextView mLocTitle;
	private ImageView mCancel;

	private ImageView mLocBtn, mArBtn;

	private BuildInfo mBuild;
	private TextView mFloorText;
	private FloorAdapter mFloorAdapter;
	private ArrayList<POI> mNavigationList;
	private Bitmap mBitmap;
	private POI mAirCuss, mAirCheck, mAirGate;// 登机口数据
	private boolean isFirst = true;
	private Gson mGson = new Gson();
	private POI mAirNotify;// 需要弹出提示的notify
	private TextView mTitle;
	private SearchView mSearch;
	private ImageView mPlan;
	private RMSearchPoiUtil mSearchPoiUtil;

	private Dialog mPoiDialog;
	private RelativeLayout mCome, mGo, mPoiInfo;
	private TextView mPoiName;
	private TextView mPoiFloor;

	private POI mPoiStart, mPoiEnd, mPoi;

	/**
	 * 导航路书布局
	 */
	private RelativeLayout mNaLayout;
	private ImageView mLeft, mRight;
	private TextView mContent;
	private ArrayList<String> mTextList;
	private ArrayList<Integer> mTextIndexList;

	private MsgData mBookmsg;

	private String mType;

	public static void interActivity(Context context) {
		Intent intent = new Intent(context, MapActivity.class);
		context.startActivity(intent);
	}

	private boolean isMapEnd = false;
	private Handler mHandler = new Handler() {// 下载地图过程中下载进度消息
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.RTMAP_MAP:
				int progress = msg.arg1;
				Log.e("rtmap", "SDK进度码" + progress);
				if (progress == Constants.MAP_LOAD_START) {// 开始加载
					mLoadDialog.show();
					Log.e("rtmap", "开始加载");
					isMapEnd = false;
				} else if (progress == Constants.MAP_FailNetResult) {// 校验结果失败
					Log.e("rtmap", "校验结果：" + (String) msg.obj);
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
				} else if (progress == Constants.MAP_Down_Fail) {
					Log.e("rtmap", "地图下载失败");
				} else if (progress == Constants.MAP_LOAD_END) {
					Log.e("rtmap", "地图加载完成");
					mLoadDialog.cancel();
					isMapEnd = true;
					mFloorText.setText(mMapView.getFloor());
					if (mLocation != null && mLocation.error == 0)
						mMapView.setCenter(mLocation.getX(), mLocation.getY());
				} else if (progress == Constants.MAP_LICENSE) {
					Log.e("rtmap", "Liscense校验结果：" + (String) msg.obj);
				}
				break;
			case 1:
				if (!mDialog.isShowing()) {
					mSignText.setText(AirSqlite.getInstance().getMsgInfo(1)
							.getText());
					AirSqlite.getInstance().update(1);
					mDialogOk.setTag(1);
					mDialogOk.setText("前往安检");
					mDialogCancel.setText("取消");
					mDialog.show();
					VibratorUtil.Vibrate(MapActivity.this, 1000); // 震动100ms
				}
				break;
			case 2:
				if (!mDialog.isShowing()) {
					mSignText.setText(AirSqlite.getInstance().getMsgInfo(2)
							.getText());
					AirSqlite.getInstance().update(2);
					mDialogOk.setTag(2);
					mDialogOk.setText("查看");
					mDialogCancel.setText("取消");
					mDialog.show();
					VibratorUtil.Vibrate(MapActivity.this, 1000); // 震动100ms
				}
				break;
			case 4:
				if (!mDialog.isShowing()) {
					mSignText.setText(AirSqlite.getInstance().getMsgInfo(3)
							.getText());
					AirSqlite.getInstance().update(3);
					mDialogOk.setTag(3);
					mDialogOk.setText("前往乘机");
					mDialogCancel.setText("取消");
					mDialog.show();
					VibratorUtil.Vibrate(MapActivity.this, 1000); // 震动100ms
				}
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		mType = DTApplication.getInstance().getShare().getString("type", "C");
		DTUIUtil.showToastSafe("欢迎来到首都国际机场T2航站楼");
		mBuild = mGson.fromJson(AirData.AIR_DATA, BuildInfo.class);

		mTitleLayout = (RelativeLayout) findViewById(R.id.rl_title);
		mCancel = (ImageView) findViewById(R.id.cancel);
		mTitleLayout.setVisibility(View.VISIBLE);
		mCancel.setOnClickListener(this);

		mTextList = new ArrayList<String>();
		mTextIndexList = new ArrayList<Integer>();
		mNaLayout.setVisibility(View.GONE);

		mLeft.setOnClickListener(this);
		mRight.setOnClickListener(this);

		mFloorText.setOnClickListener(this);

		mAirCuss = mGson.fromJson(AirData.AIR_CUSS, POI.class);
		mAirCheck = mGson.fromJson(AirData.AIR_CHECK, POI.class);
		mAirGate = mGson.fromJson(AirData.AIR_GATE, POI.class);
		mAirNotify = mGson.fromJson(AirData.AIR_NOTIFY, POI.class);

		RMSearchPoiUtil.searchPoiByMap("860100010030100003", "F4",
				"登机口;安检;安全检查;值机柜台;国内出发安全检查", false,
				new OnSearchPoiListener() {

					@Override
					public void onSearchPoi(RMPois result) {
						if (result.getError_code() == 0) {
							for (POI poi : result.getPoilist()) {
								poi.setDrawStyle(new DrawStyle(0xffffeaaa,
										0xffc9c5c3, 1));
								mMapView.addCustomPoi(poi);
							}
							mMapView.refreshMap();
						}
					}
				});
		RMSearchPoiUtil.searchPoiByMap("860100010030100003", "F5",
				"登机口;安检;安全检查;值机柜台;国内出发安全检查", false,
				new OnSearchPoiListener() {

					@Override
					public void onSearchPoi(RMPois result) {
						if (result.getError_code() == 0) {
							for (POI poi : result.getPoilist()) {
								poi.setDrawStyle(new DrawStyle(0xffffeaaa,
										0xffc9c5c3, 1));
								mMapView.addCustomPoi(poi);
							}
							mMapView.refreshMap();
						}
					}
				});

		mNavigationList = new ArrayList<POI>();
		mNavigationList.add(mAirCuss);
		mNavigationList.add(mAirCheck);

		mLocBtn = (ImageView) findViewById(R.id.btn_my_location);
		mLocBtn.setOnClickListener(this);
		mArBtn = (ImageView) findViewById(R.id.ar_btn);
		mArBtn.setOnClickListener(this);

		mTitle = (TextView) findViewById(R.id.title);
		mSearch = (SearchView) findViewById(R.id.search);
		mSearch.setOnSearchClickListener(this);
		mSearch.setOnQueryTextListener(this);
		mSearch.setOnCloseListener(new OnCloseListener() {

			@Override
			public boolean onClose() {
				mTitle.setVisibility(View.VISIBLE);
				mPoiLayer.destroyLayer();
				return false;
			}
		});
		mPlan.setOnClickListener(this);
		mSearchPoiUtil = new RMSearchPoiUtil();
		mSearchPoiUtil.setKey("mX0FE6AO7f").setBuildid(mBuild.getBuildId())
				.setPagesize(100).setOnSearchPoiListener(this);

		initPoiDialog();
		initDialog();
		if (AirSqlite.getInstance().getMsgInfo(1).getGone() == 0) {
			mHandler.sendEmptyMessageDelayed(1, 2 * 60 * 1000);
		}
		if (AirSqlite.getInstance().getMsgInfo(2).getGone() == 0)
			mHandler.sendEmptyMessageDelayed(2, 4 * 60 * 1000);
		if (AirSqlite.getInstance().getMsgInfo(4).getGone() == 0)
			mHandler.sendEmptyMessageDelayed(3, 5 * 60 * 1000);

		LocationApp.getInstance().registerLocationListener(this);
		initMap();
	}

	private Dialog mDialog;
	private TextView mSignText;
	private TextView mDialogOk, mDialogCancel;

	private void initDialog() {
		mDialog = new Dialog(this, R.style.dialog);
		mDialog.setContentView(R.layout.msg_dialog);
		mDialog.setCanceledOnTouchOutside(true);
		mSignText = (TextView) mDialog.findViewById(R.id.sign);

		mDialogOk = (TextView) mDialog.findViewById(R.id.ok);
		mDialogCancel = (TextView) mDialog.findViewById(R.id.cancel);

		mDialogOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDialog.cancel();
				int sign = (Integer) v.getTag();
				if (sign == 1) {
					startNavigation();
				} else if (sign == 2) {
				} else if (sign == 3) {
				} else if (sign == 4) {

				}
			}
		});
		;
		mDialogCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDialog.cancel();
			}
		});
	}

	/**
	 * 初始化地图
	 */
	private void initMap() {
		MapView.MAP_SCREEN_SCALE = 5f;
		Handlerlist.getInstance().register(mHandler);
		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		mMapView.setMapSortRule(3);
		mMapView.addDrawPoiHighLevel(100001);
		mMapView.addDrawPoiHighLevel(130000);
		initLayers();// 初始化图层

		Drawable blue = getResources().getDrawable(R.drawable.da_marker_red);
		mBitmap = DTIOUtil.drawableToBitmap(blue);
		mTapPoiLayer.setOnPOITappedListener(new OnPOITappedListener() {

			@Override
			public Bitmap onPOITapped(POI poi) {// 回调函数，用于设置点击地图时弹出的气泡view
				if (mNaLayout.getVisibility() == View.VISIBLE)
					return null;
				DTLog.i(new Gson().toJson(poi));
				// mNavigationList.add(poi);
				mPoiName.setText(poi.getName());
				for (Floor floor : mBuild.getFloorlist()) {
					if (floor.getFloor().equals(poi.getFloor())) {
						mPoiFloor.setText(poi.getFloor() + "-"
								+ floor.getDescription());
					}
				}
				mPoi = poi;
				Window window = mPoiDialog.getWindow();
				window.setGravity(Gravity.BOTTOM); // 此处可以设置dialog显示的位置
				WindowManager windowManager = getWindowManager();
				Display display = windowManager.getDefaultDisplay();
				WindowManager.LayoutParams lp = mPoiDialog.getWindow()
						.getAttributes();
				lp.width = (int) (display.getWidth()); // 设置宽度
				mPoiDialog.getWindow().setAttributes(lp);
				mPoiDialog.show();
				return mBitmap;

			}
		});
		mFloorText.setText(mBuild.getFloorlist().get(0).getFloor());
		mMapView.initMapConfig(mBuild.getBuildId(), mBuild.getFloorlist()
				.get(0).getFloor());// 打开地图（建筑物id，楼层id）
		mMapView.startSensor();// 开启指针方向
	}

	private void initPoiDialog() {
		mPoiDialog = new Dialog(this, R.style.dialog_white);
		mPoiDialog.setContentView(R.layout.poi_tap_dialog);
		mPoiDialog.setCanceledOnTouchOutside(true);
		mCome = (RelativeLayout) mPoiDialog.findViewById(R.id.come);
		mGo = (RelativeLayout) mPoiDialog.findViewById(R.id.go);
		mPoiInfo = (RelativeLayout) mPoiDialog.findViewById(R.id.info);
		mPoiName = (TextView) mPoiDialog.findViewById(R.id.poi_name);
		mPoiFloor = (TextView) mPoiDialog.findViewById(R.id.floor_info);

		mCome.setOnClickListener(this);
		mGo.setOnClickListener(this);
		mPoiInfo.setOnClickListener(this);
	}

	private RMLocation mLocation;

	@Override
	public void onReceiveLocation(final RMLocation result) {

		result.setX(383f);
		result.setY(200f);
		result.setError(0);
		result.setBuildID(mBuild.getBuildId());
		result.setFloor("F2");
		if (result.getError() == 0) {
			ArManager.instance().notifyLocationChanged(
					new ArLocation(mBuild.getBuildId(), result.getFloor(),
							result.getCoordX(), result.getCoordY(), 0));
			// Log.i("rtmap",
			// "result : " + result.getCoordX() + "    "
			// + result.getCoordY() + "   " + result.getFloorID());
			// *********如果固定在某一建筑物的某一楼层定位，则这段代码可以写在onCreate中
			if (mLocation != null && mLocation.getError() == 0
					&& result.getBuildID().equals(mBuild.getBuildId())
					&& !result.getFloor().equals(mLocation.getFloor())) {
				mFloorText.setText(result.getFloor());
				mMapView.initMapConfig(result.getBuildID(), result.getFloor());
			}
			if (isMapEnd) {
				RMPoiDetailUtil.getPoiInfo(result, null,
						new RMPoiDetailUtil.OnGetPoiDetailListener() {

							@Override
							public void onFinished(RMPoiDetail r) {
								if (r.getError_code() == 0) {

									mTitle.setText(mBuild.getBuildName() + "-"
											+ result.getFloor() + "\n"
											+ r.getPoi().getName() + "附近");
									mLocTitle.setText(mBuild.getBuildName()
											+ "-" + result.getFloor() + "\n"
											+ r.getPoi().getName() + "附近");
								}
							}
						});
			}

			double dis = RMathUtils.distance(result.x, result.y,
					mAirNotify.getX(), mAirNotify.getY());
			if (mBookmsg.getGone() == 0 && dis < 20
					&& result.getFloor().equals(mAirNotify.getFloor())) {
				// mHandler.sendEmptyMessageDelayed(8, 1000);
				AirSqlite.getInstance().update(mBookmsg.getMid());
				mBookmsg.setGone(1);
				if (!mDialog.isShowing()) {
					mSignText.setText("中信书店有活动哦");
					AirSqlite.getInstance().update(4);
					mDialogOk.setTag(4);
					mDialogOk.setText("确定");
					mDialogCancel.setText("取消");
					mDialog.show();
					VibratorUtil.Vibrate(MapActivity.this, 1000); // 震动100ms
				}
				DTUIUtil.showToastSafe("你已到达" + mAirNotify.getName());
			}
			if (isFirst) {
				// if (RMStringUtils.isEmpty(mMapView.getBuildId()))
				mMapView.initMapConfig(result.getBuildID(), result.getFloor());
				// if (!RMStringUtils.isEmpty(mMapView.getBuildId())) {
				isFirst = false;
				// POI startPoi = new POI(0, "我的位置", result.getBuildID(),
				// result.getFloor(), result.getX(), result.getY());
				// RMNavigationUtil.requestNavigation(XunluMap.getInstance()
				// .getApiKey(), mBuild.getBuildId(), startPoi,
				// mAirGate, mNavigationList, false, this);
				// }
			}
			// *********
		} else {
			// Log.i("rtmap", result.getErrorInfo());
		}
		mMapView.setMyCurrentLocation(result);
		mLocation = result;
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
		if (arg0 == 111 && arg1 == Activity.RESULT_OK) {
			int code = arg2.getIntExtra("type", 1);
			if (code == 1) {
				mPoiStart = mPoi;
				if (mPoiStart != null) {
					mTapPoiLayer.destroyLayer();
					mNaPoiLayer.destroyLayer();
					mMapView.refreshMap();
				}
			} else {
				mPoiEnd = mPoi;
				startNavigation();
			}
		}
	}

	private void showNotify() {
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		String msg = getString(R.string.app_name);
		Intent i = new Intent(this, MsgActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 1, i, 0);

		// Update the notification to indicate that the alert has been
		// silenced.
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_launcher;// 设置通知的图标
		notification.tickerText = msg; // 显示在状态栏中的文字
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		nm.notify(1, notification);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mBookmsg = AirSqlite.getInstance().getMsgInfo(4);

	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.clearMapLayer();// 移除所有layer
		mHandler.removeMessages(1);
		mHandler.removeMessages(2);
		mHandler.removeMessages(3);
		mHandler.removeMessages(10);
		LocationApp.getInstance().unRegisterLocationListener(this);
		Handlerlist.getInstance().remove(mHandler);// 移除handler提示
	}

	/**
	 * 初始化图层
	 */
	private void initLayers() {
		mCompassLayer = new CompassLayer(mMapView);// 指南针图层
		mTapPoiLayer = new TapPOILayer(mMapView);
		mMapView.addMapLayer(mTapPoiLayer);
		mMapView.addMapLayer(mCompassLayer);
		Drawable poiicon = getResources().getDrawable(
				R.drawable.icon_gcoding_red);
		mPoiLayer = new POILayer(mMapView, DTIOUtil.drawableToBitmap(poiicon));
		mMapView.addMapLayer(mPoiLayer);
		Drawable start = getResources().getDrawable(R.drawable.navi_start);
		mNaPoiLayer = new POILayer(mMapView, DTIOUtil.drawableToBitmap(start));
		mMapView.addLayer(mNaPoiLayer);
		Drawable end = getResources().getDrawable(R.drawable.navi_end);
		mRouteLayer = new RouteLayer(mMapView,
				DTIOUtil.drawableToBitmap(start),
				DTIOUtil.drawableToBitmap(end), null);
		mMapView.addMapLayer(mRouteLayer);
	}

	@Override
	public String getPageName() {
		return null;
	}

	private int mIndex = 0;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_my_location:
			if (mLocation.error == 0) {
				if (!mLocation.getFloor().equals(mMapView.getFloor())) {
					mMapView.initMapConfig(mLocation.getBuildID(),
							mLocation.getFloor());
				} else {
					mMapView.setCenter(mLocation.getX(), mLocation.getY());
				}
			}
			break;
		case R.id.ar_btn:
			break;
		case R.id.cancel:
			mPoiEnd = null;
			mPoiStart = null;
			mRouteLayer.destroyLayer();
			mMapView.refreshMap();
			mTitleLayout.setVisibility(View.VISIBLE);
			mArBtn.setVisibility(View.VISIBLE);
			mLocBtn.setVisibility(View.VISIBLE);
			mNaLayout.setVisibility(View.GONE);
			break;
		// case R.id.left:
		// if (mIndex > 0) {
		// mIndex--;
		// changeNavigationText();
		// }
		// break;
		// case R.id.right:
		// if (mIndex < mTextList.size() - 2) {
		// mIndex++;
		// changeNavigationText();
		// }
		// break;
		// case R.id.airplan:
		// MyPlanActivity.interActivity(this);
		// break;
		case R.id.search:
			mTitle.setVisibility(View.GONE);
			break;
		case R.id.come:
			mPoiDialog.cancel();
			mPoiStart = mPoi;
			if (mPoiStart != null) {
				mTapPoiLayer.destroyLayer();
				mNaPoiLayer.destroyLayer();
			}
			mNaPoiLayer.addPoi(mPoiStart);
			mMapView.refreshMap();
			startNavigation();
			break;
		case R.id.go:
			mPoiDialog.cancel();
			mPoiEnd = mPoi;
			startNavigation();
			break;
		case R.id.info:// 详情
			Intent intent = new Intent(this, PoiInfoActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("poi", mPoi);
			intent.putExtras(bundle);
			startActivityForResult(intent, 111);
			mPoiDialog.cancel();
			break;
		}
	}

	/**
	 * 改变导航路书
	 */
	private void changeNavigationText() {
		NavigatePoint point = mRouteLayer.getNavigatePoints().get(
				mTextIndexList.get(mIndex));
		if (!mMapView.getBuildId().equals(point.getBuildId())
				|| !mMapView.getFloor().equals(point.getFloor())) {
			for (int i = 0; i < mBuild.getFloorlist().size(); i++) {
				if (mBuild.getFloorlist().get(i).getFloor()
						.equals(point.getFloor())) {
					mFloorText.setText(point.getFloor());
					break;
				}
			}
			mMapView.initMapConfig(point.getBuildId(), point.getFloor());
		}
		mRouteLayer.setKeyRouteIndex(mTextIndexList.get(mIndex),
				mTextIndexList.get(mIndex + 1));
		mContent.setText((mIndex + 1) + "/" + (mTextList.size() - 1) + "   从"
				+ mTextList.get(mIndex) + "到" + mTextList.get(mIndex + 1));
	}

	/**
	 * 开始导航
	 */
	private void startNavigation() {
		if (mPoiEnd != null) {
			if (mPoiStart == null && mLocation != null
					&& mLocation.getError() == 0
					&& mLocation.getBuildID().equals(mBuild.getBuildId())) {
				mPoiStart = new POI(0, "我的位置", mLocation.getBuildID(),
						mLocation.getFloor(), mLocation.getX(),
						mLocation.getY());
			}
			if (mPoiStart != null) {
				mTapPoiLayer.destroyLayer();
				mLoadDialog.setMessage("正在导航..");
				mLoadDialog.show();
				RMNavigationUtil.requestNavigation(XunluMap.getInstance()
						.getApiKey(), mBuild.getBuildId(), mPoiStart, mPoiEnd,
						null, false, this);
			}
		}
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}

	@Override
	public void onSearchPoi(RMPois result) {
		mLoadDialog.cancel();
		if (result.getError_code() == 0) {
			boolean isHaveCurrentFloor = false;
			if (result.getPoilist().size() > 0) {
				for (int i = 0; i < result.getPoilist().size(); i++) {
					if (result.getPoilist().get(i).getFloor()
							.equals(mMapView.getFloor())) {
						isHaveCurrentFloor = true;
						break;
					}
				}
				if (!isHaveCurrentFloor) {
					mMapView.initMapConfig(mBuild.getBuildId(), result
							.getPoilist().get(0).getFloor());
				}
			} else {
				DTUIUtil.showToastSafe("未搜索到结果");
			}
			mPoiLayer.addPoiList(result.getPoilist());
		} else {
			DTUIUtil.showToastSafe(result.getError_msg());
		}
	}

	@Override
	public void onFinished(RMRoute route) {
		mLoadDialog.cancel();
		mLoadDialog.setMessage("");
		DTLog.i(mGson.toJson(route));
		if (route.getError_code() == 0) {
			mTitleLayout.setVisibility(View.GONE);
			// mLocTitle.setText("当前位置->\n" + mAirGate.getName());
			mTextList.clear();
			mTextIndexList.clear();
			mNaPoiLayer.destroyLayer();
			for (int i = 0; i < route.getPointlist().size(); i++) {
				NavigatePoint p = route.getPointlist().get(i);
				if (!DTStringUtil.isEmpty(p.getAroundPoiName())) {
					mTextIndexList.add(i);
					mTextList.add(p.getAroundPoiName());
				}
			}
			mRouteLayer.setNavigatePoints(route.getPointlist());
			if (mTextList.size() > 0) {
				mNaLayout.setVisibility(View.VISIBLE);
				mArBtn.setVisibility(View.GONE);
				mLocBtn.setVisibility(View.GONE);
				mIndex = 0;
				changeNavigationText();
			}
			// mNavigationList.clear();
			mMapView.refreshMap();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Floor f = mFloorAdapter.getItem(arg2);
		mMapView.initMapConfig(mBuild.getBuildId(), f.getFloor());
		mFloorText.setText(f.getFloor());
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		// TODO Auto-generated method stub
		return false;
	}

}