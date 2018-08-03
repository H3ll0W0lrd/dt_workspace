package com.rtmap.locationdemo;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.rtm.common.model.RMPois;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.POILayer;
import com.rtm.frm.map.TapPOILayer;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.CateInfo;
import com.rtm.frm.model.RMCateList;
import com.rtm.frm.utils.RMPoiCateUtil;
import com.rtm.frm.utils.RMPoiCateUtil.onGetPoiCateListener;
import com.rtm.frm.utils.RMSearchCatePoiUtil;
import com.rtm.frm.utils.RMSearchCatePoiUtil.OnSearchCateListener;
import com.rtmap.mapdemo.R;

/**
 * 分类搜索页面
 * 
 * @author dingtao
 *
 */
public class CateActivity extends Activity implements OnQueryTextListener {

	private MapView mMapView;// 地图view

	private CompassLayer mCompassLayer;// 指南针图层
	private SearchView mSearch;// 搜索
	private RMSearchCatePoiUtil mSearchPoiUtil;// 搜索POI工具
	private POILayer mPoiLayer;// 搜索结果POI图层
	private TapPOILayer mTapLayer;// 点击poi图层

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_cate);
		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		initLayers();// 初始化图层
		mMapView.initMapConfig("860100010020300001", "F1");// 打开地图（建筑物id，楼层id）
		mMapView.initScale();// 初始化比例尺

		mSearchPoiUtil = new RMSearchCatePoiUtil();// 搜索POI工具

		mSearch = (SearchView) findViewById(R.id.search);
		mSearch.setOnQueryTextListener(this);
		findViewById(R.id.get_cate).setOnClickListener(new OnClickListener() {// 获取建筑物分类信息

					@Override
					public void onClick(View v) {
						RMPoiCateUtil.requestPoiCate(XunluMap.getInstance()
								.getApiKey(), mMapView.getBuildId(), mMapView
								.getFloor(), new onGetPoiCateListener() {

							@Override
							public void onFinished(RMCateList arg0) {
								if (arg0.getError_code() == 0) {
									String str = "";
									for (int i = 0; i < arg0.getCatelist()
											.size(); i++) {
										CateInfo info = arg0.getCatelist().get(
												i);
										str += ("cateNo:" + info.getId()
												+ "   name:" + info.getName());
									}
									((TextView) findViewById(R.id.cate_info))
											.setText(str);
								}
							}
						});
					}
				});

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
		if (query != null && !"".equals(query)) {
			String[] str = query.split("-");
			ArrayList<String> list = new ArrayList<String>();
			for (int i = 0; i < str.length; i++) {
				list.add(str[i]);
			}
			// 搜索工具帮助文档可以参照开发者平台地图API
			mSearchPoiUtil.setKey(XunluMap.getInstance().getApiKey())
					.setBuildid(mMapView.getBuildId())
					.setFloor(mMapView.getFloor()).setClassid(list)
					.setOnSearchCateListener(new OnSearchCateListener() {

						@Override
						public void onFinished(RMPois result) {
							if (result.getError_code() == 0) {
								mPoiLayer.destroyLayer();
								mPoiLayer.addPoiList(result.getPoilist());
								mMapView.refreshMap();
							} else {
								Log.i("rtmap", "错误码：" + result.getError_code()
										+ "   错误信息：" + result.getError_msg());
							}
						}
					}).searchPoi();
		}
		return false;
	}

}