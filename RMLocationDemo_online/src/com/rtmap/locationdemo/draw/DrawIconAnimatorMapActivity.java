package com.rtmap.locationdemo.draw;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.rtm.common.model.POI;
import com.rtm.common.utils.RMIOUtils;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.POILayer;
import com.rtm.frm.map.RouteAnimatorLayer;
import com.rtm.frm.map.RouteAnimatorLayer.OnRouteAnimatorEndListener;
import com.rtm.frm.map.TapPOILayer;
import com.rtm.frm.map.TapPOILayer.OnTapPOIDrawListener;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.PointInfo;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.utils.RMNavigationUtil;
import com.rtm.frm.utils.RMSearchPoiUtil;
import com.rtmap.locationdemo.beta.R;

public class DrawIconAnimatorMapActivity extends Activity {

	private MapView mMapView;// 地图view
	private ImageView image1;

	private TapPOILayer mTapPOILayer;// 点击图层
	private RouteAnimatorLayer mRouteLayer;// 导航路线图层
	private POILayer mPoiLayer;// 搜索结果图层
	private CompassLayer mCompassLayer;// 指南针图层
	private RMSearchPoiUtil mSearchPoiUtil;// 搜索POI工具
	private TextView start, end, click;// poi信息

	private ArrayList<POI> mNavigationList;

	private String buildString = "860100010020300001";// 建筑物id，测试用
	private String[] floorArray = new String[] { "F1", "F2", "F3", "F4", "F5",
			"F6", "F7" };
	private int count = 0;
	private FrameLayout mLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_navigation);

		start = (TextView) findViewById(R.id.start);
		end = (TextView) findViewById(R.id.end);
		click = (TextView) findViewById(R.id.click);
		mLayout = (FrameLayout) findViewById(R.id.layout);
		findViewById(R.id.change).setOnClickListener(new OnClickListener() {// 切换楼层

					@Override
					public void onClick(View v) {
						count++;
						mMapView.initMapConfig(buildString,
								floorArray[count % 7]);
						// mMapView.initScale();
						// mRouteLayer.startAnimation("test");
					}
				});
		// RMFileUtil.FILEROOT = "TestDingtao";
		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		initLayers();// 初始化图层

		mSearchPoiUtil = new RMSearchPoiUtil();

		final ImageView image = new ImageView(getApplicationContext());
		image.setImageResource(R.drawable.ic_launcher);

		image1 = (ImageView) findViewById(R.id.image);

		mTapPOILayer.setOnTapPOIDrawListener(new OnTapPOIDrawListener() {

			@Override
			public View onTapPOIDraw(POI poi) {
				PointInfo point = mMapView.fromLocation(new Location(poi

				.getX(), poi.getY()));

				image.measure(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT);

				int left = (int) point.getX() - image.getMeasuredWidth() / 2;
				int top = (int) point.getY() - image.getMeasuredHeight();
				int right = (int) left + image.getMeasuredWidth();
				int bottom = (int) point.getY();

				image.layout(left, top, right, bottom);
				image.forceLayout();
				return image;
			}
		});

		mTapPOILayer
				.setOnPOITappedListener(new TapPOILayer.OnPOITappedListener() {

					@Override
					public Bitmap onPOITapped(POI poi) {// 回调函数，用于设置点击地图时弹出的图标
						poi.getY();
						poi.getX();
						poi.getFloor();
						poi.getName();
						Bitmap mBitmap = BitmapFactory.decodeResource(
								DrawIconAnimatorMapActivity.this.getResources(),
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
											if (route.getError_code() == 0)
												mNavigationList.clear();
											ArrayList<NavigatePoint> points = getArrayList(
													route.getPointlist(), 1);
											mRouteLayer
													.addRoute("test", points);
											Drawable blue = getResources()
													.getDrawable(
															R.drawable.sign_purple);
											Bitmap bitmap = RMIOUtils
													.drawableToBitmap(blue);
											mRouteLayer
													.setNavigationIcon(bitmap);
											mRouteLayer.startAnimation("test");
											mMapView.refreshMap();
										}
									});
						return null;
					}
				});

		mNavigationList = new ArrayList<POI>();
		mPoiLayer.setPoiIcon(BitmapFactory.decodeResource(
				DrawIconAnimatorMapActivity.this.getResources(),
				R.drawable.da_marker_red));

		mMapView.initMapConfig(buildString, floorArray[0]);// 打开地图（建筑物id，楼层id）
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

		Drawable black = getResources().getDrawable(R.drawable.icon_loc_light);
		Bitmap blackbitmap = RMIOUtils.drawableToBitmap(black);

		mRouteLayer = new RouteAnimatorLayer(mMapView, null, mendBitmap,
				null);
//		mRouteLayer.setPointIcon(null);
		mRouteLayer
				.setOnRouteAnimatorEndListener(new OnRouteAnimatorEndListener() {
					@Override
					public void onRouteAnimatorEnd() {
						mRouteLayer.removeNavigationIcon();
					}
				});

		mPoiLayer = new POILayer(mMapView);
		mCompassLayer = new CompassLayer(mMapView);
		mMapView.addMapLayer(mPoiLayer);
		mMapView.addMapLayer(mTapPOILayer);
		mMapView.addMapLayer(mRouteLayer);
		mMapView.addMapLayer(mCompassLayer);

		mMapView.refreshMap();
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
			if (RMStringUtils.isEmpty(point.getAroundPoiName())) {
				continue;
			}
			if (!point.getFloor().equals(points.get(i + 1).getFloor())
					|| !point.getFloor().equals(points.get(i - 1).getFloor())) {
				result.add(point);
				continue;
			}
			int isOneLine = RMSpatialUtils.isoneline(point.getX(),
					point.getY(), points.get(i - 1).getX(), points.get(i - 1)
							.getY(), points.get(i + 1).getX(), points
							.get(i + 1).getY());
			if (isOneLine != 0) {// point为拐点
				result.add(point);
				continue;
			}
			NavigatePoint lastResult = result.get(result.size() - 1);
			double distanceWithLastResult = RMSpatialUtils.computedistance(
					point.getX(), point.getY(), lastResult.getX(),
					lastResult.getY());
			if (distanceWithLastResult >= distance) {
				result.add(point);
				continue;
			}
		}
		result.add(points.get(points.size() - 1));
		return result;

	}

	public int getNextIndex(Location location, ArrayList<NavigatePoint> points) {
		if (location == null || points == null || points.size() < 2) {
			return -1;
		}
		int index = -1;
		float distance = -1;
		for (int i = 1; i < points.size(); i++) {
			NavigatePoint point = points.get(i);

			float a = RMSpatialUtils.pointToLineDistance(location.getX(),
					location.getY(), points.get(i - 1).getX(), points
							.get(i - 1).getY(), point.getX(), point.getY(),
					null);
			if (distance < 0) {
				distance = a;
				index = i;

			} else if (a < distance) {
				distance = a;
				index = i;

			}
		}
		if (index < 0) {
			return -1;
		} else {
			float a = RMSpatialUtils.relation(location.getX(), location.getY(),
					points.get(index - 1).getX(), points.get(index - 1).getY(),
					points.get(index).getX(), points.get(index).getY());
			if (a < 0) {
				index--;
			}

		}
		return index;
	}

}