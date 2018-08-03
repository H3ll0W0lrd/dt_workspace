package com.rtmap.locationdemo;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.rtm.common.model.POI;
import com.rtm.common.utils.Constants;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.RouteLayer;
import com.rtm.frm.map.TapPOILayer;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.utils.Handlerlist;
import com.rtm.frm.utils.RMNavigationUtil;
import com.rtmap.mapdemo.R;

/**
 * 打开地图页面
 */
public class BuildListActivity extends Activity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {
	private MapView mMapView;// 地图view

	private TapPOILayer mTapPOILayer;// 点击图层
	private RouteLayer mRouteLayer;// 导航路线图层
	private CompassLayer mCompassLayer;// 指南针图层
	private ArrayList<POI> mNavigationList;

	/**
	 * 在地图加载过程中，如果没有具体操作，可以去掉mHandler
	 */
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
				} else if (progress == Constants.MAP_LICENSE) {
					Log.e("rtmap", "Liscense校验结果：" + (String) msg.obj);
				}
				break;
			}
		}
	};
	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.build_map_main);

		XunluMap.getInstance().init(this);// 地图初始化
		Handlerlist.getInstance().register(mHandler);// 如果需要Handler得到地图的加载过程，那么请使用此方法注册Handler

		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();
		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
		mMapView = (MapView) findViewById(R.id.map_view);

		initLayers();

		// Paint routePaint = new Paint();
		// routePaint.setStyle(Paint.Style.STROKE);
		// routePaint.setStrokeWidth(6.0F);
		// routePaint.setAntiAlias(true);
		// routePaint.setStrokeCap(Paint.Cap.ROUND);
		// routePaint.setStrokeJoin(Paint.Join.ROUND);
		// routePaint.setColor(Color.RED);
		// mRouteLayer.setRoutePaint(routePaint);//
		// 本楼层导航路线样式，RouteLayer路线有默认样式，可以根据自己需要设置
		//
		// Paint dotRoutePaint = new Paint();
		// dotRoutePaint.setStyle(Paint.Style.STROKE);
		// dotRoutePaint.setStrokeWidth(6.0F);
		// dotRoutePaint.setAntiAlias(true);
		// dotRoutePaint.setStrokeCap(Paint.Cap.ROUND);
		// dotRoutePaint.setStrokeJoin(Paint.Join.ROUND);
		// dotRoutePaint.setColor(Color.BLACK);
		// dotRoutePaint.setPathEffect(new DashPathEffect(new float[] { 10, 20
		// },
		// 0));
		// mRouteLayer.setOtherFloorRoutePaint(dotRoutePaint);//
		// 其他楼层导航路线样式，RouteLayer路线有默认样式，可以根据自己需要设置

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
						Log.i("rtmap", "0000");
						if (mNavigationList.size() >= 2)
							Toast.makeText(getApplicationContext(), "导航中。。",
									5000).show();
						RMNavigationUtil.requestNavigation(XunluMap
								.getInstance().getApiKey(), mMapView
								.getBuildId(), mNavigationList.get(0),
								mNavigationList.get(1), null, true,
								new RMNavigationUtil.OnNavigationListener() {

									@Override
									public void onFinished(RMRoute route) {
										if (route.getError_code() == 0) {
											mRouteLayer.setNavigatePoints(route
													.getPointlist());
											mNavigationList.clear();
											mMapView.refreshMap();
										} else {
											Toast.makeText(
													getApplicationContext(),
													route.getError_code()
															+ " : "
															+ route.getError_msg(),
													5000).show();
										}
									}
								});
						return mTapPoiView;

					}
				});
	}

	/**
	 * 初始化图层
	 */
	private void initLayers() {
		// POI点击图层
		mTapPOILayer = new TapPOILayer(mMapView);

		Bitmap mstartBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.da_marker_red);// 起点图片
		Bitmap mendBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.da_marker_red);// 终点图片
		// 路线图层
		mRouteLayer = new RouteLayer(mMapView, mstartBitmap, mendBitmap,
				mendBitmap);
		// 指南针图层
		mCompassLayer = new CompassLayer(mMapView);
		mMapView.addMapLayer(mTapPOILayer);
		mMapView.addMapLayer(mRouteLayer);
		mMapView.addMapLayer(mCompassLayer);

		mMapView.refreshMap();
	}

	@Override
	public void onNavigationDrawerItemSelected(String buildId, String floor) {
		mMapView.initMapConfig(buildId, floor);// 打开地图（建筑物id，楼层id）
		mMapView.initScale();// 初始化比例尺
		mTitle = buildId + "-" + floor;
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Handlerlist.getInstance().remove(mHandler);// 移除Handler
	}
}
