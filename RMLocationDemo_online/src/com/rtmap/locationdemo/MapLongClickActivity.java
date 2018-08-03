package com.rtmap.locationdemo;

import java.util.ArrayList;

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
import com.rtm.frm.map.TapPOILayer.OnMapLongClickListener;
import com.rtm.frm.map.TapPOILayer.OnMapTapedListener;
import com.rtm.frm.map.TapPOILayer.OnPOITappedListener;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtmap.locationdemo.beta.R;
import com.rtmap.locationdemo.layer.FrameAnimationLayer;
import com.rtmap.locationdemo.layer.ImageLayer;

/**
 * POI居中
 * 
 * @author dingtao
 *
 */
public class MapLongClickActivity extends Activity implements OnClickListener,
		OnMapLongClickListener {

	private MapView mMapView;// 地图view
	private CompassLayer mCompassLayer;// 指南针图层
	private TapPOILayer mPoiLayer;
	private FrameAnimationLayer mAnimLayer;
	ArrayList<Bitmap> mBitmapList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_center_poi);
		RMLog.LOG_LEVEL = RMLog.LOG_LEVEL_INFO;

		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		initLayers();// 初始化图层

		mBitmapList = new ArrayList<Bitmap>();
		mBitmapList.add(getBitmap(R.drawable.grass1));
		mBitmapList.add(getBitmap(R.drawable.grass2));
		mBitmapList.add(getBitmap(R.drawable.grass3));
		mBitmapList.add(getBitmap(R.drawable.grass4));
		mBitmapList.add(getBitmap(R.drawable.grass5));
		mBitmapList.add(getBitmap(R.drawable.grass6));
		mBitmapList.add(getBitmap(R.drawable.grass7));
		mBitmapList.add(getBitmap(R.drawable.grass8));
		mBitmapList.add(getBitmap(R.drawable.grass9));
		mBitmapList.add(getBitmap(R.drawable.grass10));
		mBitmapList.add(getBitmap(R.drawable.grass11));
		mBitmapList.add(getBitmap(R.drawable.grass12));
		mBitmapList.add(getBitmap(R.drawable.grass13));

		mMapView.initMapConfig("860100010020300001", "F3");// 打开地图（建筑物id，楼层id）
		mMapView.initScale();// 初始化比例尺
	}

	private Bitmap getBitmap(int id) {
		Drawable blue = getResources().getDrawable(id);
		return drawableToBitmap(blue);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.clearMapLayer();
	}

	public void onClick(View v) {
	}

	Bitmap mBitmap, mBitmap2;
	ImageLayer mImageLayer;// 气泡

	/**
	 * 初始化图层
	 */
	private void initLayers() {
		mCompassLayer = new CompassLayer(mMapView);
		mMapView.addMapLayer(mCompassLayer);
		
		mBitmap = getBitmap(R.drawable.sign_purple);
		mBitmap2 = getBitmap(R.drawable.navflag);
		mPoiLayer = new TapPOILayer(mMapView);
		mImageLayer = new ImageLayer(mMapView);
		mImageLayer.setOnMapTapedListener(new OnMapTapedListener() {
			
			@Override
			public void OnMapTaped(Location location) {
				mImageLayer.remove(location);
				mMapView.refreshMap();
			}
		});
		mPoiLayer.setOnMapLongClickListener(this);
		mPoiLayer.setOnPOITappedListener(new OnPOITappedListener() {

			@Override
			public Bitmap onPOITapped(POI poi) {
				mMapView.setCenter(poi.getX(), poi.getY(),true);
				mImageLayer.addLocation(new Location(poi.getX(), poi.getY()),
						mBitmap2);
				mAnimLayer.destroyLayer();
				return mBitmap;
			}
		});
		mAnimLayer = new FrameAnimationLayer(mMapView);
		mMapView.addLayer(mAnimLayer);
		mMapView.addLayer(mPoiLayer);
		mMapView.addLayer(mImageLayer);
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

	@Override
	public void onMapLongClick(PointInfo point, Location location) {
		RMLog.i("rtmap",
				point.getX() + "坐标" + point.getY() + "   " + location.getX()
						+ "   " + location.getY());
		mAnimLayer.destroyLayer();
		mAnimLayer.addPointWithAnimator(location, mBitmapList);
	}
}