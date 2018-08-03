package com.rtmap.locationdemo;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rtm.common.model.POI;
import com.rtm.common.model.RMPois;
import com.rtm.common.utils.Constants;
import com.rtm.common.utils.RMLog;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.POILayer;
import com.rtm.frm.map.RouteLayer;
import com.rtm.frm.map.TapPOILayer;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtm.frm.model.RMCityList;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.utils.Handlerlist;
import com.rtm.frm.utils.RMCityListUtil;
import com.rtm.frm.utils.RMCityListUtil.OnGetCityListListener;
import com.rtm.frm.utils.RMNavigationUtil;
import com.rtm.frm.utils.RMSearchPoiUtil;
import com.rtmap.locationdemo.beta.R;

public class BuildListActivity extends Activity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks, OnQueryTextListener {
	private MapView mMapView;// 地图view

	private DrawerLayout mLayout;
	private Gson mGson;

	private TapPOILayer mTapPOILayer;// 点击图层
	private RouteLayer mRouteLayer;// 导航路线图层
	private CompassLayer mCompassLayer;// 指南针图层
	private ArrayList<POI> mNavigationList;
	private ActionBar mActionBar;
	private SearchView mSearchView;
	private Spinner mCitySpinner;

	private Handler mHandler = new Handler() {// 下载地图过程中下载进度消息
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.RTMAP_MAP:
				int progress = msg.arg1;
				Log.e("rtmap", "SDK进度码" + progress);
				if (progress == Constants.MAP_LOAD_START) {// 开始加载
					Log.e("rtmap", "开始加载");
				} else if (progress == Constants.MAP_FailNetResult) {// 校验结果失败
					Log.e("rtmap", "校验结果：" + (String) msg.obj);
				} else if (progress == Constants.MAP_FailCheckNet) {// 联网检测失败
					Log.e("rtmap", "校验联网失败");
				} else if (progress == Constants.MAP_Down_Success) {
					Log.e("rtmap", "地图下载成功");
				} else if (progress == Constants.MAP_Down_Fail) {
					Log.e("rtmap", "地图下载失败");
				} else if (progress == Constants.MAP_LOAD_END) {
					Log.e("rtmap", "地图加载完成");
				}
				break;
			case 1:// 查找城市
				RMCityListUtil.requestCityList(XunluMap.getInstance()
						.getApiKey(), new OnGetCityListListener() {

					@SuppressWarnings("deprecation")
					@Override
					public void onFinished(final RMCityList result) {
						mNavigationDrawerFragment.mLoadDialog.cancel();
						if (result.getError_code() == 0) {
							ArrayAdapter<String> adapter = new ArrayAdapter<String>(
									getApplicationContext(),
									android.R.layout.simple_spinner_dropdown_item,
									result.getCitylist());
							mCitySpinner.setAdapter(adapter);
							mCitySpinner.setSelection(0);
							mCitySpinner
									.setOnItemSelectedListener(new OnItemSelectedListener() {

										@Override
										public void onItemSelected(
												AdapterView<?> arg0, View arg1,
												int itemPosition, long arg3) {
											mNavigationDrawerFragment
													.findBuild(result
															.getCitylist()
															.get(itemPosition));
										}

										@Override
										public void onNothingSelected(
												AdapterView<?> arg0) {

										}
									});
						} else {
							Toast.makeText(getApplicationContext(),
									result.getError_msg(), 5000).show();
						}
					}
				});
				break;
			}
		}
	};
	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;
	private POILayer mPoiLayer;// 搜索结果图层

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;
	private RMSearchPoiUtil mSearchPoiUtil;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.build_map_main);

		XunluMap.getInstance().init(this);// 初始化
		mLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mLayout.openDrawer(Gravity.LEFT);
		
		mGson = new Gson();

		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();
		Handlerlist.getInstance().register(mHandler);
		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

		mActionBar = getActionBar();
		mActionBar.setDisplayShowTitleEnabled(true);
		mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
				| ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
		LayoutInflater infla = LayoutInflater.from(this);

		View view = infla.inflate(R.layout.build_list_title, null);
		mSearchView = (SearchView) view.findViewById(R.id.search);
		int id = mSearchView.getContext().getResources()
				.getIdentifier("android:id/search_src_text", null, null);
		TextView textView = (TextView) mSearchView.findViewById(id);
		textView.setTextColor(Color.WHITE);
		mCitySpinner = (Spinner) view.findViewById(R.id.city_spinner);
		mSearchView.setOnQueryTextListener(this);
		mSearchView.setIconifiedByDefault(true);
		mActionBar.setCustomView(view);

		mSearchPoiUtil = new RMSearchPoiUtil();// 搜索POI工具
		RMLog.LOG_LEVEL = RMLog.LOG_LEVEL_INFO;
		mMapView = (MapView) findViewById(R.id.map_view);

		initLayers();

		Paint routePaint = new Paint();
		routePaint.setStyle(Paint.Style.STROKE);
		routePaint.setStrokeWidth(6.0F);
		routePaint.setAntiAlias(true);
		routePaint.setStrokeCap(Paint.Cap.ROUND);
		routePaint.setStrokeJoin(Paint.Join.ROUND);
		routePaint.setColor(Color.RED);
		mRouteLayer.setRoutePaint(routePaint);// 本楼层导航路线样式

		Paint dotRoutePaint = new Paint();
		dotRoutePaint.setStyle(Paint.Style.STROKE);
		dotRoutePaint.setStrokeWidth(6.0F);
		dotRoutePaint.setAntiAlias(true);
		dotRoutePaint.setStrokeCap(Paint.Cap.ROUND);
		dotRoutePaint.setStrokeJoin(Paint.Join.ROUND);
		dotRoutePaint.setColor(Color.BLACK);
		dotRoutePaint.setPathEffect(new DashPathEffect(new float[] { 10, 20 },
				0));
		mRouteLayer.setOtherFloorRoutePaint(dotRoutePaint);// 其他楼层导航路线样式
		mNavigationList = new ArrayList<POI>();
		mTapPOILayer
				.setOnTapPOIDrawListener(new TapPOILayer.OnTapPOIDrawListener() {
					@Override
					public View onTapPOIDraw(POI poi) {// 回调函数，用于设置点击地图时弹出的气泡view

						LayoutInflater inflater = LayoutInflater
								.from(getApplicationContext());//
						final View mTapPoiView = inflater.inflate(
								R.layout.map_tap_poi, null);

						PointInfo point = mMapView.fromLocation(new Location(
								poi

								.getX(), poi.getY()));
						Bitmap mPoiBitmap = BitmapFactory.decodeResource(
								getResources(), R.drawable.map_poi);

						int offsetHeight = (mPoiBitmap != null && !mPoiBitmap

						.isRecycled()) ? mPoiBitmap.getHeight() : 0;

						TextView tv = (TextView) mTapPoiView.

						findViewById(R.id.poi_name);

						tv.setText(poi.getName());

						mTapPoiView.measure(LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT);

						int left = (int) point.getX()
								- mTapPoiView.getMeasuredWidth() / 2;
						int top = (int) point.getY()
								- mTapPoiView.getMeasuredHeight()
								- offsetHeight;
						int right = (int) left + mTapPoiView.getMeasuredWidth();
						int bottom = (int) point.getY() - offsetHeight;

						mTapPoiView.layout(left, top, right, bottom);
						mTapPoiView.forceLayout();
						mTapPoiView.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {

							}

						});
						mNavigationList.add(poi);
						Log.i("rtmap", mGson.toJson(poi));
						if (mNavigationList.size() >= 2)
							RMNavigationUtil.requestNavigation(
									XunluMap.getInstance().getApiKey(),
									mMapView.getBuildId(),
									mNavigationList.get(0),
									mNavigationList.get(1),
									null,
									false,
									new RMNavigationUtil.OnNavigationListener() {

										@Override
										public void onFinished(RMRoute route) {
											mRouteLayer.setNavigatePoints(route
													.getPointlist());
											mNavigationList.clear();
											mMapView.refreshMap();
										}
									});
						return mTapPoiView;

					}
				});
		mNavigationDrawerFragment.mLoadDialog.show();
		mMapView.initMapConfig("860100010040500017", "F10");
		mHandler.sendEmptyMessageDelayed(1, 800);// 延迟800毫秒，查找城市
	}

	private void initLayers() {// 初始化图层
		mTapPOILayer = new TapPOILayer(mMapView);

		Bitmap mstartBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.da_marker_red);// 起点图片
		Bitmap mendBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.da_marker_red);// 终点图片
		mRouteLayer = new RouteLayer(mMapView, mstartBitmap, mendBitmap,
				null);
		mRouteLayer.setShowOtherFloor(true);
		mPoiLayer = new POILayer(mMapView);
		mMapView.addMapLayer(mPoiLayer);
		mPoiLayer.setPoiIcon(BitmapFactory.decodeResource(getResources(),
				R.drawable.da_marker_red));

		mCompassLayer = new CompassLayer(mMapView);
		mMapView.addMapLayer(mTapPOILayer);
		mMapView.addMapLayer(mRouteLayer);
		mMapView.addMapLayer(mCompassLayer);

		mMapView.refreshMap();
	}

	@Override
	public void onBackPressed() {
		if (mNavigationDrawerFragment.isDrawerOpen()) {
			mLayout.closeDrawer(Gravity.LEFT);
		} else if (!mSearchView.isIconfiedByDefault()) {
			mSearchView.setIconifiedByDefault(true);
		} else
			super.onBackPressed();
	}

	@Override
	public void onNavigationDrawerItemSelected(String buildId, String floor) {
		mMapView.initMapConfig(buildId, floor);// 打开地图（建筑物id，楼层id）
		mTitle = buildId + "-" + floor;
	}

	public void restoreActionBar() {
		mActionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Place an action bar item for searching.
		restoreActionBar();
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Handlerlist.getInstance().remove(mHandler);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		if (RMStringUtils.isEmpty(mMapView.getBuildId()))
			return false;
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
		}
		mSearchView.clearFocus();
		mNavigationDrawerFragment.mLoadDialog.show();
		mSearchPoiUtil.setKey(XunluMap.getInstance().getApiKey())
				.setBuildid(mMapView.getBuildId())
				// .setFloor(mMapView.getFloor())
				.setKeywords(query)
				.setOnSearchPoiListener(
						new RMSearchPoiUtil.OnSearchPoiListener() {

							@Override
							public void onFinished(RMPois result) {
								mNavigationDrawerFragment.mLoadDialog.cancel();
								if (result.getError_code() == 0) {
									mPoiLayer.destroyLayer();
									mPoiLayer.addPoiList(result.getPoilist());
									mMapView.refreshMap();
								} else {
									Toast.makeText(
											getApplicationContext(),
											result.getError_code() + "："
													+ result.getError_msg(),
											Toast.LENGTH_LONG).show();
								}
							}
						}).searchPoi();
		return false;
	}

}
