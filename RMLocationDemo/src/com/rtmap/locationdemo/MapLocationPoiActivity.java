package com.rtmap.locationdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.RMLog;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.POILayer;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.RMPoiDetail;
import com.rtm.frm.utils.RMPoiDetailUtil;
import com.rtmap.locationdemo.beta.R;

/**
 * 搜索定位点近的POI
 * 
 * @author dingtao
 *
 */
public class MapLocationPoiActivity extends Activity implements OnClickListener {

	private MapView mMapView;// 地图view
	RMLocation location;
	private CompassLayer mCompassLayer;// 指南针图层
	private POILayer mPoiLayer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_location_poi);
		RMLog.LOG_LEVEL = RMLog.LOG_LEVEL_INFO;

		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		initLayers();// 初始化图层

		mMapView.initMapConfig("860100010020300001", "F3");// 打开地图（建筑物id，楼层id）
		mMapView.initScale();// 初始化比例尺
		location = new RMLocation();
		location.setX(25f);
		location.setY(25f);
		location.setFloorID(20030);
		location.setBuildID("860100010020300001");
		location.setError(0);
		mMapView.setMyCurrentLocation(location);
		findViewById(R.id.wc).setOnClickListener(this);
		findViewById(R.id.stair).setOnClickListener(this);
		findViewById(R.id.poibtn).setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.clearMapLayer();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.wc:// 配色
			RMPoiDetailUtil.getPoiInfo(location, "卫生间",
					new RMPoiDetailUtil.OnGetPoiDetailListener() {

						@Override
						public void onFinished(RMPoiDetail result) {
							if (result.getError_code() == 0) {
								Toast.makeText(
										getApplicationContext(),
										result.getPoi().getName() + "   id:"
												+ result.getPoi().getPoiNo(),
										Toast.LENGTH_LONG).show();
								mPoiLayer.destroyLayer();
								mPoiLayer.addPoi(result.getPoi());
							}
						}
					});
			break;
		case R.id.stair:
			RMPoiDetailUtil.getPoiInfo(location, "扶梯",
					new RMPoiDetailUtil.OnGetPoiDetailListener() {

						@Override
						public void onFinished(RMPoiDetail result) {
							if (result.getError_code() == 0) {
								Toast.makeText(
										getApplicationContext(),
										result.getPoi().getName() + "   id:"
												+ result.getPoi().getPoiNo(),
										Toast.LENGTH_LONG).show();
								mPoiLayer.destroyLayer();
								mPoiLayer.addPoi(result.getPoi());
							}
						}
					});
			break;
		case R.id.poibtn:
			RMPoiDetailUtil.getPoiInfo(location, null,
					new RMPoiDetailUtil.OnGetPoiDetailListener() {

						@Override
						public void onFinished(RMPoiDetail result) {
							if (result.getError_code() == 0) {
								Toast.makeText(
										getApplicationContext(),
										result.getPoi().getName() + "   id:"
												+ result.getPoi().getPoiNo(),
										Toast.LENGTH_LONG).show();
								mPoiLayer.destroyLayer();
								mPoiLayer.addPoi(result.getPoi());
							}
						}
					});

			break;
		}
	}

	/**
	 * 初始化图层
	 */
	private void initLayers() {
		mCompassLayer = new CompassLayer(mMapView);
		mMapView.addMapLayer(mCompassLayer);
		Drawable blue = getResources().getDrawable(R.drawable.sign_purple);
		Bitmap bitmap = drawableToBitmap(blue);
		mPoiLayer = new POILayer(mMapView, bitmap);
		mMapView.addLayer(mPoiLayer);
		mMapView.refreshMap();
	}

	/**
	 * Drawable转化为Bitmap
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;

	}
}