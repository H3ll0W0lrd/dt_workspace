package com.rtmap.locationdemo;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.rtm.common.model.POI;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.RouteAnimatorLayer;
import com.rtm.frm.map.TapPOILayer;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.utils.RMNavigationUtil;
import com.rtmap.mapdemo.R;

/**
 * 路线动画展示页面
 * 
 * @author dingtao
 *
 */
public class AnimNavigationActivity extends Activity {

	private MapView mMapView;// 地图view

	private TapPOILayer mTapPOILayer;// 点击图层
	private RouteAnimatorLayer mRouteLayer;// 导航路线图层,当前仅支持同一层导航路线
	private CompassLayer mCompassLayer;// 指南针图层
	private TextView start, end, click;// poi信息

	private ArrayList<POI> mNavigationList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_navigation);

		start = (TextView) findViewById(R.id.start);
		end = (TextView) findViewById(R.id.end);
		click = (TextView) findViewById(R.id.click);
		Button button = (Button) findViewById(R.id.change);
		button.setText("重新执行动画");
		button.setOnClickListener(new OnClickListener() {// 切换楼层

			@Override
			public void onClick(View v) {
				mRouteLayer.startAnimation("test");
			}
		});
		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		initLayers();// 初始化图层

		// Paint routePaint = new Paint();
		// routePaint.setStyle(Paint.Style.STROKE);
		// routePaint.setStrokeWidth(6.0F);
		// routePaint.setAntiAlias(true);
		// routePaint.setStrokeCap(Paint.Cap.ROUND);
		// routePaint.setStrokeJoin(Paint.Join.ROUND);
		// routePaint.setColor(Color.RED);
		// mRouteLayer.setRoutePaint(routePaint);//
		// 本楼层导航路线样式，RouteLayer路线有默认样式，可以根据自己需要设置

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

		mTapPOILayer
				.setOnPOITappedListener(new TapPOILayer.OnPOITappedListener() {

					@Override
					public Bitmap onPOITapped(POI poi) {// 回调函数，用于设置点击地图时弹出的图标
						poi.getY();
						poi.getX();
						poi.getFloor();
						poi.getName();
						Bitmap mBitmap = BitmapFactory.decodeResource(
								AnimNavigationActivity.this.getResources(),
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
									null,
									true,
									new RMNavigationUtil.OnNavigationListener() {

										@Override
										public void onFinished(RMRoute route) {
											if (route.getError_code() == 0) {
												mNavigationList.clear();
												mRouteLayer.addRoute("test",
														route.getPointlist());// 我们返回的路线点比较密集，你可以根据自己需求将同一增长率的点去掉（即同一条线），这样动画在播放时会比较流畅
												mRouteLayer
														.startAnimation("test");
												mMapView.refreshMap();
											}
										}
									});
						return mBitmap;
					}
				});

		mNavigationList = new ArrayList<POI>();
		mMapView.initMapConfig("860100010020300001", "F1");// 打开地图（建筑物id，楼层id）
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
		mTapPOILayer = new TapPOILayer(mMapView);// 点击图层

		Bitmap mstartBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.da_marker_red);// 起点图片
		Bitmap mendBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.da_marker_red);// 终点图片

		mRouteLayer = new RouteAnimatorLayer(mMapView, mstartBitmap,
				mendBitmap, null);// 动画路线图层
		mRouteLayer.setNavigationIcon(BitmapFactory.decodeResource(
				getResources(), R.drawable.map_poi_enter_p));// 设置导航动画点图片

		mCompassLayer = new CompassLayer(mMapView);// 指南针图层
		mMapView.addMapLayer(mTapPOILayer);
		mMapView.addMapLayer(mRouteLayer);
		mMapView.addMapLayer(mCompassLayer);

		mMapView.refreshMap();
	}

}