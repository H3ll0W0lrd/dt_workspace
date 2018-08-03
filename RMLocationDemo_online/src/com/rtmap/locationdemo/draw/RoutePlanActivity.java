package com.rtmap.locationdemo.draw;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.rtm.common.model.POI;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.POILayer;
import com.rtm.frm.map.RouteLayer;
import com.rtm.frm.map.TapPOILayer;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.utils.RMNavigationUtil;
import com.rtm.frm.utils.RMSearchPoiUtil;
import com.rtmap.locationdemo.beta.R;

public class RoutePlanActivity extends Activity {

	private MapView mMapView;// 地图view

	private TapPOILayer mTapPOILayer;// 点击图层
	private RouteLayer mRouteLayer;// 导航路线图层
	private POILayer mPoiLayer;// 搜索结果图层
	private CompassLayer mCompassLayer;// 指南针图层
	private RMSearchPoiUtil mSearchPoiUtil;// 搜索POI工具
	private TextView start, end, click;// poi信息

	private ArrayList<POI> mNavigationList;

	private String buildString = "860100010020300001";// 建筑物id，测试用
	private String[] floorArray = new String[] { "F1", "F2", "F3", "F4", "F5",
			"F6", "F7" };
	private int count = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_navigation);

		start = (TextView) findViewById(R.id.start);
		end = (TextView) findViewById(R.id.end);
		click = (TextView) findViewById(R.id.click);
		findViewById(R.id.change).setOnClickListener(new OnClickListener() {// 切换楼层

					@Override
					public void onClick(View v) {
						count++;
						mMapView.initMapConfig(buildString,
								floorArray[count % 7]);
						mMapView.initScale();
					}
				});
		// RMFileUtil.FILEROOT = "TestDingtao";
		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		initLayers();// 初始化图层

		mSearchPoiUtil = new RMSearchPoiUtil();

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

		mTapPOILayer
				.setOnPOITappedListener(new TapPOILayer.OnPOITappedListener() {

					@Override
					public Bitmap onPOITapped(POI poi) {// 回调函数，用于设置点击地图时弹出的图标
						poi.getY();
						poi.getX();
						poi.getFloor();
						poi.getName();
						Bitmap mBitmap = BitmapFactory.decodeResource(
								RoutePlanActivity.this.getResources(),
								R.drawable.map_poi);
						mNavigationList.add(poi);
						if (mNavigationList.size() == 1)
							start.setText("起点坐标楼层：" + poi.getFloor() + "  x:"
									+ poi.getX() + "    y:" + poi.getY());
						if (mNavigationList.size() == 2)
							end.setText("终点坐标楼层：" + poi.getFloor() + "  x:"
									+ poi.getX() + "    y:" + poi.getY());
						click.setText("点击坐标：x:" + poi.getX() + "    y:"
								+ poi.getY());
						if (mNavigationList.size() >= 2)
							RMNavigationUtil.requestNavigation(
									XunluMap.getInstance().getApiKey(),
									mMapView.getBuildId(),
									mNavigationList.get(0),
									mNavigationList.get(1),
									null,false,
									new RMNavigationUtil.OnNavigationListener() {

										@Override
										public void onFinished(RMRoute route) {
											if (route.getError_code() == 0)
												mRouteLayer
														.setNavigatePoints(route
																.getPointlist());
											mNavigationList.clear();
											mMapView.refreshMap();
										}
									});
						return mBitmap;
					}
				});

		mNavigationList = new ArrayList<POI>();
		mPoiLayer.setOnPOIDrawListener(new POILayer.OnPOIDrawListener() {

			@Override
			public Bitmap onPOIDraw(POI poi) {// 回调函数，设置poi搜索时显示的气泡

				return BitmapFactory.decodeResource(
						RoutePlanActivity.this.getResources(),
						R.drawable.da_marker_red);

			}
		});

		mMapView.initMapConfig(buildString, floorArray[0]);// 打开地图（建筑物id，楼层id）
		mMapView.initScale();// 初始化比例尺
	}

	@Override
	protected void onDestroy() {
		mMapView.clearMapLayer(); // 清除图层
		super.onDestroy();

	}

	/**
	 * 初始化图层
	 */
	private void initLayers() {
		mTapPOILayer = new TapPOILayer(mMapView);

		Bitmap mstartBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.da_marker_red);// 起点图片
		Bitmap mendBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.da_marker_red);// 终点图片
		mRouteLayer = new RouteLayer(mMapView, mstartBitmap, mendBitmap,
				mendBitmap);

		mPoiLayer = new POILayer(mMapView);
		mCompassLayer = new CompassLayer(mMapView);
		mMapView.addMapLayer(mPoiLayer);
		mMapView.addMapLayer(mTapPOILayer);
		mMapView.addMapLayer(mRouteLayer);
		mMapView.addMapLayer(mCompassLayer);

		mMapView.refreshMap();
	}

}