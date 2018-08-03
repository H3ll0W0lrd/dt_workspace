package com.rtmap.wisdom.fragment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView.FindListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.rtm.common.model.BuildInfo;
import com.rtm.common.model.Floor;
import com.rtm.common.model.POI;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.Constants;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.MapView.OnMapModeChangedListener;
import com.rtm.frm.map.POILayer;
import com.rtm.frm.map.RouteAnimatorLayer;
import com.rtm.frm.map.TapPOILayer;
import com.rtm.frm.map.TapPOILayer.OnPOITappedListener;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.PointInfo;
import com.rtm.frm.model.RMBuildDetail;
import com.rtm.frm.model.RMPoiDetail;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.utils.Handlerlist;
import com.rtm.frm.utils.OnMapTapedListener;
import com.rtm.frm.utils.RMBuildDetailUtil;
import com.rtm.frm.utils.RMBuildDetailUtil.OnGetBuildDetailListener;
import com.rtm.frm.utils.RMNavigationUtil;
import com.rtm.frm.utils.RMNavigationUtil.OnNavigationListener;
import com.rtm.frm.utils.RMPoiDetailUtil;
import com.rtm.frm.utils.RMathUtils;
import com.rtm.location.LocationApp;
import com.rtm.location.sensor.BeaconSensor;
import com.rtm.location.utils.RMLocationListener;
import com.rtmap.wisdom.R;
import com.rtmap.wisdom.activity.WDMeActivity;
import com.rtmap.wisdom.core.DTApplication;
import com.rtmap.wisdom.core.DTAsyncTask;
import com.rtmap.wisdom.core.DTBaseFragment;
import com.rtmap.wisdom.core.DTCallBack;
import com.rtmap.wisdom.exception.DTException;
import com.rtmap.wisdom.http.DTHttpUtil;
import com.rtmap.wisdom.layer.CheLayer;
import com.rtmap.wisdom.layer.FrameAnimationLayer;
import com.rtmap.wisdom.layer.FrameAnimationLayer.OnFreshListener;
import com.rtmap.wisdom.layer.ImageLayer;
import com.rtmap.wisdom.model.MyBuild;
import com.rtmap.wisdom.util.DTFileUtil;
import com.rtmap.wisdom.util.DTLog;
import com.rtmap.wisdom.util.DTUIUtil;
import com.rtmap.wisdom.util.statellite.FanItem;
import com.rtmap.wisdom.util.statellite.FanView;
import com.rtmap.wisdom.util.statellite.FanView.OnItemSelectedListener;
import com.rtmap.wisdom.util.statellite.TouchPadLayout;
import com.rtmap.wisdom.util.wheelview.DTHorizontalWheelView;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

/**
 * 地图页
 * 
 * @author dingtao
 *
 */
public class WDMainFragment extends DTBaseFragment implements
		RMLocationListener, OnClickListener, OnMapTapedListener,
		OnNavigationListener, OnGetBuildDetailListener, OnItemSelectedListener {

	private MapView mMapView;
	private RouteAnimatorLayer mRouteLayer;
	private TapPOILayer mTapPoiLayer;
	private POILayer mPoiLayer;
	private FrameAnimationLayer mCaoLayer;
	private CheLayer mCheLayer;
	private ImageLayer mImageLayer;// 气泡
	private TextView mFloorText;

	private DTHorizontalWheelView mFloorList;

	private ImageView mDianti, mFuti, mWC, mLocBtn, mTingChe;
	private HashMap<String, Bitmap> mIconMap = new HashMap<String, Bitmap>();

	private TouchPadLayout mPadLayout;
	private FanView mMapTouchSelfView;
	private FanView mMapTouchOutsideView;

	// private DTSatelliteView mSatelliteView;

	private ImageView mMyLocSign;
	private ImageView mBack;// 建筑物列表按钮
	private TextView mEndNavigate;// 结束导航按钮
	private TextView mTitle;// 标题按钮
	private DrawerLayout mDrawer;
	private ArrayList<Bitmap> mBitmapList;
	private ProgressDialog mLoadDialog;
	private BuildInfo mBuild;
	private TextView mNaviText;
	private TextView mCheText;

	private TextView mLocText;
	private TextView mLocBtnSign;

	private TranslateAnimation mShakeAnim;

	private Dao<MyBuild, String> mBuildDao;

	public final static int LOC_TEXT = 2;
	public final static int NAVI_TEXT = 3;
	public final static int NAVI_ICON = 4;
	public final static int BLUE_DIALOG = 5;
	private Handler mHandler = new Handler() {// 下载地图过程中下载进度消息
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.RTMAP_MAP:
				int progress = msg.arg1;
				if (progress == Constants.MAP_LOAD_START) {// 开始加载
					mPadLayout.setNoLong(true);
					Log.e("rtmap", "开始加载");
					if (!mLoadDialog.isShowing()) {
						mLoadDialog.show();
					}
				} else if (progress == Constants.MAP_FailNetResult) {// 校验结果失败
					Log.e("rtmap", "地图校验结果：" + (String) msg.obj);
				} else if (progress == Constants.MAP_FailCheckNet) {// 联网检测失败
					Log.e("rtmap", "校验联网失败");
				} else if (progress == Constants.MAP_Down_Success) {
					Log.e("rtmap", "地图下载成功");
				} else if (progress == Constants.MAP_Down_Fail) {
					Log.e("rtmap", "地图下载失败");
				} else if (progress == Constants.MAP_Update_Success) {
					Log.e("rtmap", "地图更新成功");
				} else if (progress == Constants.MAP_Update_Fail) {
					Log.e("rtmap", "地图更新失败");
				} else if (progress == Constants.MAP_LOAD_END) {
					Log.e("rtmap", "地图加载完成");
					mLoadDialog.cancel();
					mFloorText.setText(mMapView.getFloor());
					mMapView.setResetMapCenter(true);
					count = 0;
					mCheText.setVisibility(View.GONE);
					mMapView.removeMapLayer(mCheLayer);
					ArrayList<Floor> fl = mBuild.getFloorlist();
					for (int i = 0; i < fl.size(); i++) {
						Floor floor = fl.get(i);
						if (floor.getFloor().equals(mMapView.getFloor())) {
							if (floor.getDescription() != null
									&& floor.getDescription().contains("停车")) {
								if (mCheLayer.getChe() == null) {
									mCheText.setVisibility(View.VISIBLE);
									mCheText.setText("我要停车");
								} else {
									mMapView.addMapLayer(mCheLayer);
								}
								break;
							}
						}
					}

					if (isLocNavigate) {// 正在导航中
						mPadLayout.setNoLong(true);
					} else {
						mPadLayout.setNoLong(false);
					}

					mMapView.refreshMap();
				} else if (progress == Constants.MAP_LICENSE) {
					Log.e("rtmap", "license校验结果：" + (String) msg.obj);
				}
				break;
			case 1:
				POI poi = (POI) msg.obj;
				if (poi.getBuildId().equals(mMapView.getBuildId())) {
					mMapView.setCenter(poi.getX(), poi.getY());
					mCaoLayer.addBitmapList(poi, mBitmapList);
					if (!poi.getBuildId().equals(mMapView.getBuildId())
							|| !poi.getFloor().equals(mMapView.getFloor())) {
						mMapView.setResetMapCenter(false);
						mMapView.initMapConfig(poi.getBuildId(), poi.getFloor());
					}
				} else {
					DTUIUtil.showToastSafe("非本建筑物POI，请手动切换建筑物");
				}
				break;
			case LOC_TEXT:
				mLocText.setVisibility(View.GONE);
				break;
			case NAVI_ICON:
				mImageLayer.destroyLayer();
				mMapView.refreshMap();
				break;
			case NAVI_TEXT:
				Animation anim = AnimationUtils.loadAnimation(getActivity(),
						R.anim.trans_bottom_to_top);
				anim.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {

					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						mNaviText.setVisibility(View.GONE);
					}
				});
				mNaviText.startAnimation(anim);
				break;
			case BLUE_DIALOG:
				if (!BeaconSensor.getInstance().isBlueToothOpen()) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(
							getActivity());
					dialog.setNegativeButton("否", null);
					dialog.setPositiveButton("是",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
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
					dialog.setMessage("定位需要蓝牙设备支持，是否打开蓝牙？");
					dialog.create().show();
				}
				break;
			}
		}
	};

	/**
	 * 返回此建筑物
	 * 
	 * @return
	 */
	public BuildInfo getBuild() {
		return mBuild;
	}

	RelativeLayout layout;
	private Tencent mTencent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Handlerlist.getInstance().register(mHandler);
		mTencent = Tencent.createInstance("1105831699", getActivity());
		mHandler.sendEmptyMessageDelayed(BLUE_DIALOG, 1000);
	}

	public void setDrawer(DrawerLayout drawer, ProgressDialog mLoadDialog,
			Dao<MyBuild, String> buildDao) {
		this.mDrawer = drawer;
		this.mLoadDialog = mLoadDialog;
		this.mBuildDao = buildDao;

		Intent intent = getActivity().getIntent();
		int sign = intent.getIntExtra("sign", 0);
		String buildid;
		if (sign == 0) {// 定位没有成功
			String b = mShare.getString("buildinfo", null);
			if (b == null) {
				buildid = "860100010040500017";
			} else {
				mBuild = mGson.fromJson(b, BuildInfo.class);
				buildid = mBuild.getBuildId();
			}
		} else {
			RMLocation location = LocationApp.getInstance()
					.getCurrentLocation();
			buildid = location.getBuildID();
		}

		try {
			MyBuild myChe = mBuildDao.queryForId("停车");
			if (myChe != null) {
				POI poi = mGson.fromJson(myChe.getContent(), POI.class);
				mCheLayer.setChe(poi);
				mCheText.setVisibility(View.GONE);
				mCheText.setText("更改车位");
				mTingChe.setVisibility(View.VISIBLE);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		loadBuild(buildid);
		mDrawer.bringToFront();
		mMapView.setZOrderOnTop(true);
		mMapView.getSurfaceView().setZOrderMediaOverlay(true);
	}

	private void loadBuild(String buildid) {
		try {
			MyBuild build = mBuildDao.queryForId(buildid);
			if (build != null) {
				mBuild = mGson.fromJson(build.getContent(), BuildInfo.class);
				updateBuild(mBuild);
				if (System.currentTimeMillis() - build.getTime() > 3 * 24 * 60
						* 60 * 1000) {// 超过3天，更新数据
					RMBuildDetailUtil.requestBuildDetail(XunluMap.getInstance()
							.getApiKey(), buildid, this);
				} else {
					mLoadDialog.cancel();
				}
			} else {
				mLoadDialog.show();
				RMBuildDetailUtil.requestBuildDetail(XunluMap.getInstance()
						.getApiKey(), buildid, this);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private boolean isLocNavigate;

	@Override
	protected View createLoadedView() {
		View view = DTUIUtil.inflate(R.layout.main_map);
		layout = (RelativeLayout) view.findViewById(R.id.map_layout);
		mMapTouchOutsideView = (FanView) view
				.findViewById(R.id.map_touch_outside_view);
		mMapTouchOutsideView.setOnItemSelectedListener(this);
		mMapTouchSelfView = (FanView) view
				.findViewById(R.id.map_touch_self_view);
		mMapTouchSelfView.setOnItemSelectedListener(this);
		mPadLayout = (TouchPadLayout) view.findViewById(R.id.map_touch_layout);
		mPadLayout.setNoLong(true);

		ArrayList<FanItem> l = mMapTouchOutsideView.getFanItems();
		l.add(new FanItem(R.drawable.map_long_other_me1,
				R.drawable.map_long_other_me2, "位置", 3));
		l.add(new FanItem(R.drawable.map_long_other_cao1,
				R.drawable.map_long_other_cao2, "终点", 4));
		l.add(new FanItem(R.drawable.map_long_share1,
				R.drawable.map_long_share2, "分享", 5));

		l = mMapTouchSelfView.getFanItems();
		l.add(new FanItem(R.drawable.map_long_me1, R.drawable.map_long_me2,
				"我的", 0));
		l.add(new FanItem(R.drawable.map_long_share1,
				R.drawable.map_long_share2, "分享", 1));
		l.add(new FanItem(R.drawable.map_long_flash1,
				R.drawable.map_long_flash2, "闪现", 2));

		// mSatelliteView = (DTSatelliteView) view
		// .findViewById(R.id.dTSatelliteView1);
		// mSatelliteView.addItem(new SatelliteIitem(0, "位置",
		// getBit(R.drawable.map_long_other_me1),
		// getBit(R.drawable.map_long_other_me2)));
		// mSatelliteView.addItem(new SatelliteIitem(1, "终点",
		// getBit(R.drawable.map_long_other_cao1),
		// getBit(R.drawable.map_long_other_cao2)));
		// mSatelliteView.addItem(new SatelliteIitem(2, "分享",
		// getBit(R.drawable.map_long_share1),
		// getBit(R.drawable.map_long_share2)));

		mTitle = (TextView) view.findViewById(R.id.title);

		view.findViewById(R.id.search).setOnClickListener(this);
		mBack = (ImageView) view.findViewById(R.id.back);// 建筑物列表
		mBack.setOnClickListener(this);
		mEndNavigate = (TextView) view.findViewById(R.id.end_navigate);
		mEndNavigate.setOnClickListener(this);
		mLocText = (TextView) view.findViewById(R.id.map_sign_text);
		mLocText.setVisibility(View.GONE);

		mCheText = (TextView) view.findViewById(R.id.map_che);
		mCheText.setVisibility(View.GONE);
		mCheText.setOnClickListener(this);

		mNaviText = (TextView) view.findViewById(R.id.navigate_text);

		mMyLocSign = (ImageView) view.findViewById(R.id.map_loc_sign);
		mMyLocSign.setVisibility(View.VISIBLE);

		mFuti = (ImageView) view.findViewById(R.id.futi);
		mFuti.setOnClickListener(this);
		mDianti = (ImageView) view.findViewById(R.id.dianti);
		mDianti.setOnClickListener(this);
		mWC = (ImageView) view.findViewById(R.id.cesuo);
		mWC.setOnClickListener(this);
		mFloorText = (TextView) view.findViewById(R.id.floor_text);
		mFloorText.setOnClickListener(this);

		mTingChe = (ImageView) view.findViewById(R.id.tingche);
		mTingChe.setOnClickListener(this);

		mLocBtn = (ImageView) view.findViewById(R.id.map_loc_btn);
		mLocBtn.setOnClickListener(this);
		mLocBtnSign = (TextView) view.findViewById(R.id.map_loc_btn_sign);

		mShakeAnim = new TranslateAnimation(5, -5, 0, 0);
		mShakeAnim.setInterpolator(new OvershootInterpolator());
		mShakeAnim.setDuration(200);
		mShakeAnim.setRepeatCount(100);
		mShakeAnim.setRepeatMode(Animation.REVERSE);

		initShareDialog();// 初始化分享弹出框
		initNaviOkDialog();
		initFloorDialog();// 楼层弹出框

		mBitmapList = new ArrayList<Bitmap>();// 小草集合
		mBitmapList.add(BitmapFactory.decodeResource(getResources(),
				R.drawable.grass1));
		mBitmapList.add(BitmapFactory.decodeResource(getResources(),
				R.drawable.grass2));
		mBitmapList.add(BitmapFactory.decodeResource(getResources(),
				R.drawable.grass3));
		mBitmapList.add(BitmapFactory.decodeResource(getResources(),
				R.drawable.grass4));
		mBitmapList.add(BitmapFactory.decodeResource(getResources(),
				R.drawable.grass5));
		mBitmapList.add(BitmapFactory.decodeResource(getResources(),
				R.drawable.grass6));
		mBitmapList.add(BitmapFactory.decodeResource(getResources(),
				R.drawable.grass7));
		mBitmapList.add(BitmapFactory.decodeResource(getResources(),
				R.drawable.grass8));
		mBitmapList.add(BitmapFactory.decodeResource(getResources(),
				R.drawable.grass9));
		mBitmapList.add(BitmapFactory.decodeResource(getResources(),
				R.drawable.grass10));
		mBitmapList.add(BitmapFactory.decodeResource(getResources(),
				R.drawable.grass11));
		mBitmapList.add(BitmapFactory.decodeResource(getResources(),
				R.drawable.grass12));
		mBitmapList.add(BitmapFactory.decodeResource(getResources(),
				R.drawable.grass13));

		mIconMap.put("电梯", BitmapFactory.decodeResource(getResources(),
				R.drawable.map_dianti4));
		mIconMap.put("扶梯", BitmapFactory.decodeResource(getResources(),
				R.drawable.map_futi4));
		mIconMap.put("卫生间", BitmapFactory.decodeResource(getResources(),
				R.drawable.map_cesuo4));
		mIconMap.put("电梯1", BitmapFactory.decodeResource(getResources(),
				R.drawable.map_dianti3));
		mIconMap.put("扶梯1", BitmapFactory.decodeResource(getResources(),
				R.drawable.map_futi3));
		mIconMap.put("卫生间1", BitmapFactory.decodeResource(getResources(),
				R.drawable.map_cesuo3));
		mIconMap.put("导航", BitmapFactory.decodeResource(getResources(),
				R.drawable.map_navigation));

		XunluMap.getInstance().init(getActivity());
		mMapView = (MapView) view.findViewById(R.id.map_view);
		mMapView.setMapBackgroundColor(0xfff9f5f2);
		mMapView.setResetMapScale(true);
		mMapView.setDrawLogo(false);
		mMapView.setLocationIcon(R.drawable.default_location,
				R.drawable.default_location);
		mRouteLayer = new RouteAnimatorLayer(mMapView, null, null,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.map_navi_icon));
		mMapView.addMapLayer(mRouteLayer);
		mTapPoiLayer = new TapPOILayer(mMapView);
		mTapPoiLayer.setOnMapTapedListener(this);
		mMapView.addMapLayer(mTapPoiLayer);

		mImageLayer = new ImageLayer(mMapView);
		mImageLayer.setOnMapTapedListener(new OnPOITappedListener() {

			@Override
			public Bitmap onPOITapped(POI poi) {
				mImageLayer.destroyLayer();
				mMapView.refreshMap();
				Location l = mMapView.getMyCurrentLocation();
				POI start = new POI(0, "我的位置", mMapView.getBuildId(), l
						.getFloor(), l.getX(), l.getY());
				mLoadDialog.show();
				isAutoFloor = true;
				RMNavigationUtil.requestNavigation(XunluMap.getInstance()
						.getApiKey(), mMapView.getBuildId(), start, poi, null,
						WDMainFragment.this);
				return null;
			}
		});
		mMapView.addMapLayer(mImageLayer);

		mCheLayer = new CheLayer(mMapView);

		mCaoLayer = new FrameAnimationLayer(mMapView);
		mCaoLayer.setListener(new OnFreshListener() {

			@Override
			public void onFresh() {
				mWC.setImageResource(R.drawable.map_cesuo2);
				mFuti.setImageResource(R.drawable.map_futi2);
				mDianti.setImageResource(R.drawable.map_dianti2);
			}
		});
		mMapView.addLayer(mCaoLayer);

		mPoiLayer = new POILayer(mMapView, BitmapFactory.decodeResource(
				getResources(), R.drawable.default_location));
		mMapView.addLayer(mPoiLayer);

		CompassLayer layer = new CompassLayer(mMapView,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.map_compass));
		layer.setPosition(Constants.TOP_LEFT);
		layer.setDrawX(20);
		layer.setDrawY(20);
		mMapView.addMapLayer(layer);

		mMapView.setOnMapModeChangedListener(new OnMapModeChangedListener() {

			@Override
			public void onMapModeChanged() {
				if (mCheText.getVisibility() == View.VISIBLE
						&& mMapView.contains(mCheLayer)
						&& mCheLayer.getChe() == null) {
					Location l = mMapView.fromPixels(mCheLayer.getCenter());
					POI poi = mMapView.getAroundPOI("", l.getX(), l.getY());
					if (poi != null) {
						mCheText.setText("您的车位于" + poi.getName() + "附近");
					} else {
						mCheText.setText("好像无法停车，移动地图试试..");
					}
				}
			}
		});

		mPadLayout.setMapView(mMapView);
		return view;
	}

	private Bitmap getBit(int id) {
		return BitmapFactory.decodeResource(getResources(), id);
	}

	/**
	 * 添加搜索的poi
	 * 
	 * @param poi
	 */
	public void addSearchPoi(POI poi) {
		isAutoFloor = false;
		Message msg = new Message();
		msg.what = 1;
		msg.obj = poi;
		mHandler.sendMessageDelayed(msg, 800);

	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public void onResume() {
		super.onResume();
		int sign = mShare.getInt("navigate_icon", 0);
		if (sign == 0) {
			mRouteLayer.setNavigationIcon(BitmapFactory.decodeResource(
					getResources(), R.drawable.navi_gou));
		} else if (sign == 1) {
			mRouteLayer.setNavigationIcon(BitmapFactory.decodeResource(
					getResources(), R.drawable.navi_gui));
		} else {
			mRouteLayer.setNavigationIcon(BitmapFactory.decodeResource(
					getResources(), R.drawable.navi_yu));
		}
		LocationApp.getInstance().registerLocationListener(this);
		// mMapView.startSensor();
	}

	@Override
	public void onPause() {
		super.onPause();
		LocationApp.getInstance().unRegisterLocationListener(this);
		// mMapView.removeSensor();
	}

	private int locCount = 0;// 定位次数
	private final static int MAX_LOC_COUNT = 20;
	private boolean isFirstSign = true;

	private boolean isAutoFloor = true;

	public void setAutoFloor(boolean isAutoFloor) {
		this.isAutoFloor = isAutoFloor;
	}

	@Override
	public void onReceiveLocation(RMLocation location) {
		location.setAccuracy(0);
		if (location.getError() == 0) {
			locCount = 0;
			if (isLocNavigate) {// 是否处于正在导航的状态下
				ArrayList<NavigatePoint> l = mRouteLayer.getRoute("key");
				if (l != null && l.size() > 0) {
					NavigatePoint end = l.get(l.size() - 1);
					if (RMathUtils.distance(end.getX(), end.getY(),
							location.getX(), location.getY()) < 10) {
						mNaviOkDialog.show();// 显示弹出款
						stopNavigate();// 结束导航
					}
				}
			}
			if (isAutoFloor) {
				if (!location.getBuildID().equals(mMapView.getBuildId())
						|| !mMapView.getFloor().equals(location.getFloor())) {
					if (!location.getBuildID().equals(mMapView.getBuildId())) {
						loadBuild(location.getBuildID());
					} else {
						mMapView.initMapConfig(location.getBuildID(),
								location.getFloor());
					}
				}
			}
		} else {
			locCount++;
			DTLog.i(location.getErrorInfo() + "\n" + location.getLbsid());
		}
		if (locCount > MAX_LOC_COUNT) {// 超限停定位节能
			LocationApp.getInstance().stop();
			mLocBtn.startAnimation(mShakeAnim);
			if (isFirstSign) {
				isFirstSign = false;
				mLocBtnSign.setVisibility(View.VISIBLE);
			}
		}
		mMapView.setMyCurrentLocation(location);
		Location loc = mMapView.getMyCurrentLocation();
		if (loc != null && loc.getBuildId().equals(mMapView.getBuildId())
				&& loc.getFloor().equals(mMapView.getFloor())) {
			mMyLocSign.setVisibility(View.VISIBLE);
		} else {
			mMyLocSign.setVisibility(View.GONE);
		}
	}

	private Dialog mShareDialog, mFloorDialog, mNaviOkDialog;

	/**
	 * 导航成功
	 */
	private void initNaviOkDialog() {
		mNaviOkDialog = new Dialog(getActivity(), R.style.dialog);
		mNaviOkDialog.setContentView(R.layout.dialog_navigate_ok);
		mNaviOkDialog.setCanceledOnTouchOutside(true);
		mNaviOkDialog.findViewById(R.id.navigate_ok).setOnClickListener(this);
		mNaviOkDialog.findViewById(R.id.dialog_back).setOnClickListener(this);
	}

	/**
	 * show时间间隔
	 */
	private void initShareDialog() {
		mShareDialog = new Dialog(getActivity(), R.style.dialog);
		mShareDialog.setContentView(R.layout.dialog_share_layout);
		mShareDialog.setCanceledOnTouchOutside(true);
		mShareDialog.findViewById(R.id.weibo).setOnClickListener(this);
		mShareDialog.findViewById(R.id.wx).setOnClickListener(this);
		mShareDialog.findViewById(R.id.qq).setOnClickListener(this);
		mShareDialog.findViewById(R.id.dialog_back).setOnClickListener(this);
	}

	private void initFloorDialog() {
		mFloorDialog = new Dialog(getActivity(), R.style.dialog_white);
		mFloorDialog.setContentView(R.layout.floor_layout);
		mFloorDialog.setCanceledOnTouchOutside(true);
		mFloorList = (DTHorizontalWheelView) mFloorDialog
				.findViewById(R.id.floor_list);
		mFloorDialog.findViewById(R.id.floor_ok).setOnClickListener(this);
		mFloorList.setItems(new ArrayList<String>());
		Window win = mFloorDialog.getWindow();
		android.view.WindowManager.LayoutParams params = win.getAttributes();
		DTLog.e("dialog.x : " + params.x + "   dialog.y: " + params.y);
		WindowManager wm = getActivity().getWindowManager();
		int width = wm.getDefaultDisplay().getWidth();
		params.gravity = Gravity.BOTTOM | Gravity.LEFT;
		params.width = width;
		win.setAttributes(params);
	}

	private int count;

	private boolean isTingche;// 是从停车按钮点的切换建筑物

	@Override
	public void onClick(View v) {
		if (mEndNavigate.getVisibility() == View.GONE) {
			switch (v.getId()) {
			case R.id.map_loc_btn://  我的位置
				mShakeAnim.cancel();
				mLocBtnSign.setVisibility(View.GONE);
				if (!LocationApp.getInstance().isStartLocate()) {
					locCount = 0;
					LocationApp.getInstance().start();
				}
				Location loc = mMapView.getMyCurrentLocation();
				if (loc != null) {
					if (!loc.getBuildId().equals(mMapView.getBuildId())
							|| !loc.getFloor().equals(mMapView.getFloor())) {// 不一样
						count++;
						if (count == 1) {
							try {
								MyBuild build = mBuildDao.queryForId(loc
										.getBuildId());
								if (build != null) {
									BuildInfo info = mGson
											.fromJson(build.getContent(),
													BuildInfo.class);
									mLocText.setText("您现在正在"
											+ info.getBuildName()
											+ loc.getFloor() + "，再次点击将返回。");
									mLocText.setVisibility(View.VISIBLE);
									mLocText.startAnimation(AnimationUtils
											.loadAnimation(
													getActivity(),
													R.anim.map_text_trans_b_to_t));
									mHandler.sendEmptyMessageDelayed(LOC_TEXT,
											5000);
								} else {
									mLoadDialog.show();
									isAutoFloor = true;
									loadBuild(loc.getBuildId());
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}
						} else if (count >= 2) {
							mLoadDialog.show();
							isAutoFloor = true;
							// loadBuild(loc.getBuildId());
						}
					} else {
						isAutoFloor = true;
						if (loc.getFloor().equals(mMapView.getFloor())) {//
							mMapView.setCenter(loc);
						}
						// else {
						// mLoadDialog.show();
						// loadBuild(loc.getBuildId());
						// }
					}
				}
				break;
			case R.id.tingche:// 停车按钮
				Location location = mMapView.getMyCurrentLocation();
				POI poi1 = mCheLayer.getChe();
				if (location != null
						&& location.getBuildId().equals(poi1.getBuildId())) {
					POI start = new POI(0, "我的位置", location.getBuildId(),
							location.getFloor(), location.getX(),
							location.getY());
					mPadLayout.setNoLong(true);
					mLoadDialog.show();
					if (!location.getBuildId().equals(mMapView.getBuildId())) {
						isAutoFloor = true;
					}
					RMNavigationUtil.requestNavigation(XunluMap.getInstance()
							.getApiKey(), start.getBuildId(), start, poi1,
							null, this);
				} else {
					isAutoFloor = false;
					if (!poi1.getBuildId().equals(mMapView.getBuildId())) {
						isTingche = true;
						loadBuild(poi1.getBuildId());
					} else {
						mMapView.setCenter(poi1.getX(), poi1.getY());
						mMapView.setResetMapCenter(false);
						mMapView.setResetMapScale(false);
						mMapView.initMapConfig(poi1.getBuildId(),
								poi1.getFloor());
					}
				}
				break;
			case R.id.map_che:// 我要停车
				if (mCheText.getText().toString().equals("我要停车")) {
					mMapView.addLayer(mCheLayer);
					Location l = mMapView.fromPixels(mCheLayer.getCenter());
					POI poi = mMapView.getAroundPOI("", l.getX(), l.getY());
					if (poi != null) {
						mCheText.setText("您的车位于" + poi.getName() + "附近");
					} else {
						mCheText.setText("好像无法停车，移动地图试试..");
					}
				} else if (mCheText.getText().toString().equals("更改车位")) {
					mCheLayer.destroyLayer();
					mTingChe.setVisibility(View.GONE);
					try {
						mBuildDao.deleteById("停车");
					} catch (SQLException e) {
						e.printStackTrace();
					}
					Location l = mMapView.fromPixels(mCheLayer.getCenter());
					POI poi = mMapView.getAroundPOI("", l.getX(), l.getY());
					if (poi != null) {
						mCheText.setText("您的车位于" + poi.getName() + "附近");
					} else {
						mCheText.setText("好像无法停车，移动地图试试..");
					}
				} else {
					Location l = mMapView.fromPixels(mCheLayer.getCenter());
					POI poi = mMapView.getAroundPOI("", l.getX(), l.getY());
					if (poi != null) {
						mCheLayer.setChe(poi);
						MyBuild myChe = new MyBuild();
						myChe.setBuildId("停车");
						myChe.setTime(System.currentTimeMillis());
						myChe.setContent(mGson.toJson(poi));
						try {
							mBuildDao.createOrUpdate(myChe);
						} catch (SQLException e) {
							e.printStackTrace();
						}
						mCheText.setVisibility(View.GONE);
						mTingChe.setVisibility(View.VISIBLE);
					} else {
						DTUIUtil.showToastSafe("无法停车");
						mMapView.removeMapLayer(mCheLayer);
						mCheText.setText("我要停车");
					}
				}
				mMapView.refreshMap();
				break;
			case R.id.weibo:
				mShareDialog.cancel();
				DTUIUtil.showToastSafe("暂不支持微博分享");
				break;
			case R.id.wx:
				mShareDialog.cancel();
				mLoadDialog.show();
				new DTAsyncTask(new DTCallBack() {

					@Override
					public Object onCallBackStart(Object... obj) {
						try {
							String str = DTHttpUtil
									.connInfo(
											DTHttpUtil.GET,
											"http://lbsapi.rtmap.com/rtmap_lbs_api/v1/rtmap/token",
											new String[] { "key", "buildid" },
											new String[] {
													XunluMap.getInstance()
															.getApiKey(),
													mBuild.getBuildId() });
							DTLog.i(str);
							JSONObject json = new JSONObject(str);
							if (json.has("access_token")) {
								return json.getString("access_token");
							}
						} catch (DTException e) {
							e.printStackTrace();
						} catch (JSONException e) {
							e.printStackTrace();
						}
						return null;
					}

					@Override
					public void onCallBackFinish(Object obj) {
						mLoadDialog.cancel();
						if (obj != null) {
							shareWx((String) obj);
						} else {
							DTUIUtil.showToastSafe("分享失败");
						}
					}
				}).run();
				break;
			case R.id.qq:
				mShareDialog.cancel();
				mLoadDialog.show();
				new DTAsyncTask(new DTCallBack() {

					@Override
					public Object onCallBackStart(Object... obj) {
						try {
							String str = DTHttpUtil
									.connInfo(
											DTHttpUtil.GET,
											"http://lbsapi.rtmap.com/rtmap_lbs_api/v1/rtmap/token",
											new String[] { "key", "buildid" },
											new String[] {
													XunluMap.getInstance()
															.getApiKey(),
													mBuild.getBuildId() });
							DTLog.i(str);
							JSONObject json = new JSONObject(str);
							if (json.has("access_token")) {
								return json.getString("access_token");
							}
						} catch (DTException e) {
							e.printStackTrace();
						} catch (JSONException e) {
							e.printStackTrace();
						}
						return null;
					}

					@Override
					public void onCallBackFinish(Object obj) {
						mLoadDialog.cancel();
						if (obj != null) {
							shareQQ((String) obj);
						} else {
							DTUIUtil.showToastSafe("分享失败");
						}
					}
				}).run();
				break;
			case R.id.dialog_back:
				mNaviOkDialog.cancel();
				mShareDialog.cancel();
				break;
			case R.id.navigate_ok:
				mNaviOkDialog.cancel();
				break;
			case R.id.back:
				mDrawer.openDrawer(Gravity.START);
				break;
			case R.id.search:
				mDrawer.openDrawer(Gravity.END);
				break;
			case R.id.futi:
				aroundPOI("扶梯");
				break;
			case R.id.dianti:
				aroundPOI("电梯");
				break;
			case R.id.cesuo:
				aroundPOI("卫生间");
				break;
			}
		} else {
			if (v.getId() == R.id.end_navigate) {
				stopNavigate();
			} else {
				if (v.getId() != R.id.floor_ok && R.id.floor_text != v.getId())
					DTUIUtil.showToastSafe("请结束导航");
			}
		}
		if (v.getId() == R.id.floor_ok) {
			mFloorDialog.cancel();
			DTLog.i("楼层是"
					+ mFloorList.getItems().get(
							mFloorList.getSelectedPosition()));
			isAutoFloor = false;
			mMapView.initMapConfig(mBuild.getBuildId(), mFloorList.getItems()
					.get(mFloorList.getSelectedPosition()));
		} else if (R.id.floor_text == v.getId()) {
			if (mBuild != null) {
				if (mMapView.contains(mCheLayer) && mCheLayer.getChe() == null) {
					mMapView.removeMapLayer(mCheLayer);
					mMapView.refreshMap();
					mCheText.setText("我要停车");
				}
				mFloorDialog.show();
				String floor = mFloorText.getText().toString();
				for (int i = 0; i < mFloorList.getItems().size(); i++) {
					if (floor.equals(mFloorList.getItems().get(i))) {
						mFloorList.selectIndex(i);
						break;
					}
				}
			} else {
				DTUIUtil.showToastSafe("数据缺失");
			}
		}
	}

	private void shareWx(String token) {
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = "http://maps.rtmap.com:8080/latest/?token="
				+ token + "&points=[[" + mShareLocation.getX() + ","
				+ -mShareLocation.getY() + "]]&floor="
				+ mShareLocation.getFloor();
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = "我在这里";
		msg.description = "我把位置分享给你了，快来找我~";
		Bitmap thumb = BitmapFactory.decodeResource(getResources(),
				R.drawable.map_share_image);
		msg.setThumbImage(thumb);
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = System.currentTimeMillis() + "";
		req.message = msg;
		DTApplication.mWX.sendReq(req);
	}

	public void shareQQ(String token) {
		Bundle bundle = new Bundle();
		bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE,
				QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
		bundle.putString(QQShare.SHARE_TO_QQ_TITLE, "我在这里");
		bundle.putString(
				QQShare.SHARE_TO_QQ_TARGET_URL,
				"http://maps.rtmap.com:8080/latest/?token=" + token
						+ "&points=[[" + mShareLocation.getX() + ","
						+ -mShareLocation.getY() + "]]&floor="
						+ mShareLocation.getFloor());
		bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, "我把位置分享给你了，快来找我~");
		bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL,
				DTFileUtil.getImageDir() + "map_share_image.png");
		bundle.putString(QQShare.SHARE_TO_QQ_APP_NAME,
				getActivity().getString(R.string.app_name));
		mTencent.shareToQQ(getActivity(), bundle, new IUiListener() {

			@Override
			public void onError(UiError arg0) {
				DTUIUtil.showToastSafe("分享失败");
			}

			@Override
			public void onComplete(Object arg0) {
				DTUIUtil.showToastSafe("分享完成");
			}

			@Override
			public void onCancel() {
				DTUIUtil.showToastSafe("取消分享");
			}
		});
	}

	private void stopNavigate() {
		mHandler.removeMessages(NAVI_TEXT);
		isLocNavigate = false;
		mEndNavigate.setVisibility(View.GONE);
		mBack.setVisibility(View.VISIBLE);
		mRouteLayer.destroyLayer();
		mCaoLayer.destroyLayer();
		mPoiLayer.destroyLayer();
		mMapView.refreshMap();
		mPadLayout.setNoLong(false);
		mNaviText.setVisibility(View.GONE);
	}

	public MapView getMapView() {
		return mMapView;
	}

	private boolean isLocFloor;// 是否是当前定位楼层

	private void aroundPOI(final String name) {
		final Location myLoc = mMapView.getMyCurrentLocation();
		RMLocation location = new RMLocation();
		location.setError(0);
		location.setBuildID(mMapView.getBuildId());
		location.setFloor(mMapView.getFloor());
		if (myLoc != null && myLoc.getBuildId().equals(mMapView.getBuildId())
				&& myLoc.getFloor().equals(mMapView.getFloor())) {
			isLocFloor = true;
			location.setX(myLoc.getX());
			location.setY(myLoc.getY());
		} else {
			isLocFloor = false;
			location.setX(mMapView.getCenter().getX());
			location.setY(mMapView.getCenter().getY());
		}
		RMPoiDetailUtil.getPoiInfo(location, name,
				new RMPoiDetailUtil.OnGetPoiDetailListener() {

					@Override
					public void onFinished(RMPoiDetail result) {
						if (result.getError_code() == 0) {
							mCaoLayer.addBitmap(result.getPoi(),
									mIconMap.get(name));
							if ("卫生间".equals(name)) {
								mWC.setImageResource(R.drawable.map_cesuo1);
							} else if ("扶梯".equals(name)) {
								mFuti.setImageResource(R.drawable.map_futi1);
							} else if ("电梯".equals(name)) {
								mDianti.setImageResource(R.drawable.map_dianti1);
							}

							if (isLocFloor) {// 是定位楼层，显示name图标，显示点我导航图标
								mImageLayer.addLocation(result.getPoi(),
										mIconMap.get("导航"));
								POI poi = new POI(0, "我的位置",
										myLoc.getBuildId(), myLoc.getFloor(),
										myLoc.getX(), myLoc.getY());
								mHandler.sendEmptyMessageDelayed(NAVI_ICON,
										5000);
								// if ("卫生间".equals(name)) {
								// mImageLayer.addLocation(poi,
								// mIconMap.get(name + "1"));
								// } else if ("扶梯".equals(name)) {
								// mImageLayer.addLocation(poi,
								// mIconMap.get(name + "1"));
								// } else if ("电梯".equals(name)) {
								// mImageLayer.addLocation(poi,
								// mIconMap.get(name + "1"));
								// }
							} else {// 是其他楼层，显示name图标
							}
							mMapView.setCenter(result.getPoi().getX(), result
									.getPoi().getY_abs());
							mMapView.refreshMap();
						} else {
							DTUIUtil.showToastSafe("本层没有" + name);
						}
					}
				});
	}

	public ArrayList<NavigatePoint> getArrayList(
			ArrayList<NavigatePoint> points, int distance) {

		if (points == null || points.size() < 3 || distance <= 0) {
			return points;
		}
		ArrayList<NavigatePoint> result = new ArrayList<NavigatePoint>();
		result.add(points.get(0));
		for (int i = 1; i < points.size() - 1; i++) {
			NavigatePoint point = points.get(i);
			if (!point.getFloor().equals(points.get(i + 1).getFloor())
					|| !point.getFloor().equals(points.get(i - 1).getFloor())) {
				result.add(point);
				continue;
			}
			int isOneLine = isoneline(point.getX(), point.getY(),
					points.get(i - 1).getX(), points.get(i - 1).getY(), points
							.get(i + 1).getX(), points.get(i + 1).getY());
			if (isOneLine != 0) {// point为拐点
				result.add(point);
				continue;
			}
			NavigatePoint lastResult = result.get(result.size() - 1);
			double distanceWithLastResult = RMathUtils.distance(point.getX(),
					point.getY(), lastResult.getX(), lastResult.getY());
			if (distanceWithLastResult >= distance) {
				result.add(point);
				continue;
			}
		}
		result.add(points.get(points.size() - 1));
		return result;

	}

	public static int isoneline(double a1, double b1, double a2, double b2,
			double a3, double b3) {
		double x1 = a2 - a1;
		double y1 = b2 - b1;
		double x2 = a3 - a1;
		double y2 = b3 - b1;

		double angle = computeAngle(a1, b1, a2, b2, a3, b3);

		double result = (x2) * (y1) - (y2) * (x1);
		if (Math.abs(angle) < Math.PI + Math.PI / 4
				&& Math.abs(angle) > Math.PI - Math.PI / 4) {

			return 0;
		} else if (Math.abs(angle) < Math.PI / 4) {
			return 2;
		}
		if (result > 0) {
			return 1;
		} else {
			return -1;
		}

	}

	public static double computeAngle(double a1, double b1, double a2,
			double b2, double a3, double b3) {
		double x1 = a2 - a1;
		double y1 = b2 - b1;
		double x2 = a3 - a1;
		double y2 = b3 - b1;
		double v1 = (x1 * x2) + (y1 * y2);
		double ma_val = Math.sqrt(x1 * x1 + y1 * y1);
		double mb_val = Math.sqrt(x2 * x2 + y2 * y2);
		double cosM = v1 / (ma_val * mb_val);
		double angle = Math.acos(cosM);

		return angle;
	}

	@Override
	public void onFinished(RMRoute route) {
		mLoadDialog.cancel();
		if (route.getError_code() == 0) {
			NavigatePoint start = route.getPointlist().get(0);
			NavigatePoint end = route.getPointlist().get(0);
			for (int i = 1; i < route.getPointlist().size(); i++) {
				if (!route.getPointlist().get(i).getFloor()
						.equals(start.getFloor())) {
					break;
				} else {
					NavigatePoint r = route.getPointlist().get(i);
					if (Math.abs(r.getX() - start.getX()) > Math.abs(end.getX()
							- start.getX())) {
						end = r;
					}
				}
			}
			WindowManager wm = getActivity().getWindowManager();
			int width = wm.getDefaultDisplay().getWidth();
			float xScale = Math.abs(end.getX() - start.getX()) / (width - 250);
			float yScale = Math.abs(end.getY() - start.getY()) / (width - 250);
			DTLog.e("比例为：" + xScale + "   " + yScale);

			mMapView.setScale(xScale > yScale ? xScale : yScale);
			mMapView.setCenter((end.getX() + start.getX()) / 2,
					(end.getY() + start.getY()) / 2);
			mRouteLayer.addRoute("key", getArrayList(route.getPointlist(), 5));
			mRouteLayer.startAnimation("key");
			mBack.setVisibility(View.GONE);
			mEndNavigate.setVisibility(View.VISIBLE);
			mMapView.refreshMap();
			NavigatePoint point = route.getPointlist().get(0);
			NavigatePoint point1 = route.getPointlist().get(
					route.getPointlist().size() - 1);
			POI poi = mMapView.getAroundPOI("", point.getX(), point.getY());
			POI poi1 = mMapView.getAroundPOI("", point1.getX(), point1.getY());
			String poiname = "", poiname1 = "";
			if (poi != null)
				poiname = poi.getName();
			if (poi1 != null)
				poiname1 = poi1.getName();
			mNaviText.setText("正为您导向" + poiname1 + "\n目标点在" + poiname + "附近");
			mNaviText.setVisibility(View.VISIBLE);
			mNaviText.startAnimation(AnimationUtils.loadAnimation(
					getActivity(), R.anim.trans_top_to_bottom));
			mHandler.sendEmptyMessageDelayed(NAVI_TEXT, 5000);
			if (start.getFloor().equals(mMapView.getFloor())) {// 如果楼层一样
				isLocNavigate = true;
			} else {// 如果楼层不一样
				isLocNavigate = true;
				mMapView.setResetMapCenter(false);
				mMapView.setResetMapScale(false);
				mMapView.initMapConfig(start.getBuildId(), start.getFloor());
			}
		} else {
			mPoiLayer.destroyLayer();
			mCaoLayer.destroyLayer();
			mMapView.refreshMap();
			mPadLayout.setNoLong(false);
			DTUIUtil.showToastSafe(route.getError_msg());
		}
	}

	@Override
	public void onFinished(RMBuildDetail result) {
		mLoadDialog.cancel();
		if (result.getError_code() == 0) {
			updateBuild(result.getBuild());
		} else {
			// if (build != null) {// 更新失败不做提示
			// } else {// 首次拉数据失败
			DTUIUtil.showToastSafe(result.getError_msg());
			// }
		}
	}

	private void updateBuild(BuildInfo buildinfo) {
		MyBuild build = null;
		try {
			build = mBuildDao.queryForId(buildinfo.getBuildId());
			if (build != null) {// 有建筑物历史信息
				build.setContent(mGson.toJson(buildinfo));
			} else {
				build = new MyBuild();
				build.setContent(mGson.toJson(buildinfo));
				build.setBuildId(buildinfo.getBuildId());
			}
			build.setTime(System.currentTimeMillis());
			mBuildDao.createOrUpdate(build);
			mFloorList.getItems().clear();
			mBuild = buildinfo;
			mShare.edit().putString("buildinfo", mGson.toJson(mBuild)).commit();

			ArrayList<Floor> fl = mBuild.getFloorlist();
			boolean isHaveF1 = false;
			for (int i = fl.size() - 1; i >= 0; i--) {
				if ("F1".equals(fl.get(i).getFloor()))
					isHaveF1 = true;
				mFloorList.getItems().add(fl.get(i).getFloor());
			}
			if (isTingche) {// 是从停车按钮点过来的
				isTingche = false;
				POI poi1 = mCheLayer.getChe();
				mMapView.setCenter(poi1.getX(), poi1.getY());
				mMapView.setResetMapCenter(false);
				mMapView.initMapConfig(poi1.getBuildId(), poi1.getFloor());
			} else {
				Location loc = mMapView.getMyCurrentLocation();
				if (loc != null && loc.getBuildId().equals(mBuild.getBuildId())) {
					mMapView.setCenter(loc);
					mMapView.setResetMapCenter(false);
					mMapView.initMapConfig(mBuild.getBuildId(), loc.getFloor());
				} else {
					if (!mBuild.getBuildId().equals(mMapView.getBuildId())) {// 建筑物不同
						if (isHaveF1) {// 如果有F1
							mMapView.initMapConfig(mBuild.getBuildId(), "F1");
						} else {// 如果没有F1
							mMapView.initMapConfig(mBuild.getBuildId(), mBuild
									.getFloorlist().get(0).getFloor());
						}
					}
				}
			}
			if (mCheLayer.getChe() != null
					&& mBuild.getBuildId().equals(
							mCheLayer.getChe().getBuildId())) {
				mTingChe.setVisibility(View.VISIBLE);
			} else {
				mTingChe.setVisibility(View.GONE);
			}

			// mFloorList.setItems(mFloorList.getItems());
			mFloorList.notifyDataUpdate();
			mTitle.setText(mBuild.getBuildName());
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Handlerlist.getInstance().remove(mHandler);
	}

	private Location mShareLocation;

	@Override
	public void onItemSelected(FanItem fanItem, PointF pointF) {
		switch (fanItem.getType()) {
		case 0:// 我的页面
			Intent intent = new Intent(getActivity(), WDMeActivity.class);
			getActivity().startActivity(intent);
			break;
		case 1:// 分享
		case 5:
			mShareLocation = mMapView.fromPixels(pointF.x, pointF.y);
			mShareLocation.setFloor(mMapView.getFloor());
			mShareDialog.show();
			break;
		case 2:// 闪现
			Location location = mMapView.getMyCurrentLocation();
			PointInfo point = mMapView.fromLocation(location.getX(),
					location.getY());
			break;
		case 3:// 位置
			mPoiLayer.destroyLayer();
			mImageLayer.destroyLayer();
			Location l = mMapView.fromPixels(pointF.x, pointF.y);
			mPoiLayer.addPoi(new POI(0, "我的位置", mMapView.getBuildId(), mMapView
					.getFloor(), l.getX(), l.getY()));
			startNavigate();
			break;
		case 4:// 终点
			mImageLayer.destroyLayer();
			Location li = mMapView.fromPixels(pointF.x, pointF.y);
			mCaoLayer.addBitmapList(new POI(0, "我的位置", mMapView.getBuildId(),
					mMapView.getFloor(), li.getX(), li.getY()), mBitmapList);
			startNavigate();
			break;
		}
	}

	private void startNavigate() {
		if (mPoiLayer.getPoiList().size() > 0) {
			if (mCaoLayer.getLocation() != null) {
				POI start = mPoiLayer.getPoiList().get(0);
				POI end = mCaoLayer.getLocation();
				if (start.getBuildId().equals(end.getBuildId())) {
					mPadLayout.setNoLong(true);
					mLoadDialog.show();
					RMNavigationUtil.requestNavigation(XunluMap.getInstance()
							.getApiKey(), start.getBuildId(), start, end, null,
							this);
				}
			} else {
				if (mCheLayer.getChe() != null) {
					POI start = mPoiLayer.getPoiList().get(0);
					POI end = mCheLayer.getChe();
					if (start.getBuildId().equals(end.getBuildId())) {
						mPadLayout.setNoLong(true);
						mLoadDialog.show();
						RMNavigationUtil.requestNavigation(XunluMap
								.getInstance().getApiKey(), start.getBuildId(),
								start, end, null, this);
					}
				}
			}
		}
	}

	private int clickCount;

	@Override
	public void onMapTaped(POI poi, Location location) {
		
		if (isLocNavigate && mNaviText.getVisibility() == View.GONE) {// 如果是导航状态
			mNaviText.setVisibility(View.VISIBLE);
			mNaviText.startAnimation(AnimationUtils.loadAnimation(
					getActivity(), R.anim.trans_top_to_bottom));
			mHandler.sendEmptyMessageDelayed(NAVI_TEXT, 5000);
		}
		if (poi != null) {
			clickCount = 0;
			if (mEndNavigate.getVisibility() == View.GONE) {
				POI che = mCheLayer.getChe();
				if (mMapView.contains(mCheLayer) && che != null
						&& che.getPoiNo() == poi.getPoiNo()
						&& poi.getFloor().equals(che.getFloor())
						&& poi.getBuildId().equals(che.getBuildId())) {// 确定点击了停车位
					mCheText.setText("更改车位");
					mCheText.setVisibility(View.VISIBLE);
				} else {
					Location l = mMapView.getMyCurrentLocation();
					if (l != null
							&& l.getBuildId().equals(mMapView.getBuildId())) {
						mCaoLayer.addBitmapList(poi, mBitmapList);
						mImageLayer.addLocation(poi, mIconMap.get("导航"));
					}
				}
			}
		} else {
			clickCount++;
			// DTUIUtil.showToastSafe(""+clickCount);
			if (mEndNavigate.getVisibility() == View.GONE && clickCount >= 2) {
				if (mImageLayer.hasData()) {
					mImageLayer.destroyLayer();
				} else {
					mCaoLayer.destroyLayer();
				}
			}
		}
	}
}
