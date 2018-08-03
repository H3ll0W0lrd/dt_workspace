package com.rtmap.locationdemo;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.rtm.common.model.RMPois;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.POILayer;
import com.rtm.frm.map.TapPOILayer;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.utils.RMSearchPoiUtil;
import com.rtmap.mapdemo.R;

/**
 * 关键字搜索页面
 * @author dingtao
 *
 */
public class SearchActivity extends Activity implements OnQueryTextListener {

	private MapView mMapView;// 地图view

	private CompassLayer mCompassLayer;// 指南针图层
	private SearchView mSearch;// 搜索
	private RMSearchPoiUtil mSearchPoiUtil;// 搜索POI工具
	private POILayer mPoiLayer;// 搜索结果POI图层
	private TapPOILayer mTapLayer;// 点击poi图层

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_search);
		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		initLayers();// 初始化图层
		mMapView.initMapConfig("860100010020300001", "F1");// 打开地图（建筑物id，楼层id）

		mSearchPoiUtil = new RMSearchPoiUtil();// 搜索POI工具

		mSearch = (SearchView) findViewById(R.id.search);
		mSearch.setOnQueryTextListener(this);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.clearMapLayer();
	}

	/**
	 * 初始化图层
	 */
	private void initLayers() {
		mCompassLayer = new CompassLayer(mMapView);
		mMapView.addMapLayer(mCompassLayer);
		mPoiLayer = new POILayer(mMapView);
//		mPoiLayer.setOnPOIDrawListener(new POILayer.OnPOIDrawListener() {
//
//			@Override
//			public Bitmap onPOIDraw(POI poi) {// 回调函数，设置poi搜索时显示的气泡
//
//				return BitmapFactory.decodeResource(getResources(),
//						R.drawable.da_marker_red);
//
//			}
//		});
		mPoiLayer.setPoiIcon(BitmapFactory.decodeResource(getResources(),
						R.drawable.da_marker_red));
		mMapView.addMapLayer(mPoiLayer);
		mTapLayer = new TapPOILayer(mMapView);
		mMapView.addMapLayer(mTapLayer);
		mMapView.refreshMap();
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		//搜索工具设置方法API可以参照开发者平台地图API使用
		mSearchPoiUtil
				.setKey(XunluMap.getInstance().getApiKey())
				.setBuildid(mMapView.getBuildId())
				.setFloor(mMapView.getFloor())
				.setKeywords(query)
				.setOnSearchPoiListener(
						new RMSearchPoiUtil.OnSearchPoiListener() {

							@Override
							public void onFinished(RMPois result) {
								if (result.getError_code() == 0) {
									mPoiLayer.destroyLayer();
									mPoiLayer.addPoiList(result.getPoilist());
									mMapView.refreshMap();
								}else {
									Log.i("rtmap",
											"错误码："
													+ result.getError_code()
													+ "   错误信息："
													+ result.getError_msg());
								}
							}
						}).searchPoi();
		return false;
	}

}