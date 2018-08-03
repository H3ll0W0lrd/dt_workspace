package com.rtmap.locationdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.rtm.common.model.POI;
import com.rtm.common.utils.RMLog;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.TapPOILayer;
import com.rtm.frm.map.TapPOILayer.OnPOITappedListener;
import com.rtm.frm.map.XunluMap;
import com.rtmap.locationdemo.beta.R;

/**
 * POI居中
 * 
 * @author dingtao
 *
 */
public class MapPoiCenterActivity extends Activity implements OnClickListener {

	private MapView mMapView;// 地图view
	private CompassLayer mCompassLayer;// 指南针图层
	private TapPOILayer mPoiLayer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_center_poi);
		RMLog.LOG_LEVEL = RMLog.LOG_LEVEL_INFO;

		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		initLayers();// 初始化图层

		mMapView.initMapConfig("860100010040500017", "F10");// 打开地图（建筑物id，楼层id）
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.clearMapLayer();
	}

	public void onClick(View v) {
	}

	Bitmap mBitmap;

	/**
	 * 初始化图层
	 */
	private void initLayers() {
		mCompassLayer = new CompassLayer(mMapView);
		mMapView.addMapLayer(mCompassLayer);
		Drawable blue = getResources().getDrawable(R.drawable.sign_purple);
		mBitmap = drawableToBitmap(blue);
		mPoiLayer = new TapPOILayer(mMapView);
		mPoiLayer.setOnPOITappedListener(new OnPOITappedListener() {

			@Override
			public Bitmap onPOITapped(POI poi) {
				mMapView.setCenter(poi.getX(), poi.getY(),true);
				return mBitmap;
			}
		});
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