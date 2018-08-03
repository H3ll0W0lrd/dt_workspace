package com.rtmap.locationdemo;

import java.util.ArrayList;

import com.rtm.common.model.POI;
import com.rtm.common.utils.Constants;
import com.rtm.common.utils.RMLog;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.RouteLayer;
import com.rtm.frm.map.TapPOILayer;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.utils.Handlerlist;
import com.rtm.frm.utils.RMNavigationUtil;
import com.rtm.frm.utils.RMNavigationUtil.OnNavigationListener;
import com.rtmap.locationdemo.beta.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class TextNavigationActivity extends Activity{
	
	
	private POI poi1 = new POI(-1, "起点", "860100010030100002", "F3", 336.62f,
			255.329f);
	private POI poi2 = new POI(-1, "南航VIP值机", "860100010030100002", "F2",
			297.028f, 265.578f);
	private POI poi3 = new POI(-1, "国内出发安全检查", "860100010030100002", "F2",
			216.059f, 217.7f);
	private POI poi4 = new POI(-1, "32登机口", "860100010030100002", "F2",
			40.056f, 127.067f);
	private int i;
	private ArrayList<POI> mNavigationList;
	
	private boolean isFirst = true;
	private MapView mMapView;// 地图view

	private RouteLayer mRouteLayer;// 导航路线图层

	private Handler mHandler = new Handler() {// 下载地图过程中下载进度消息
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.RTMAP_MAP:
				int progress = msg.arg1;
				if (progress == Constants.MAP_LOAD_START) {// 开始加载
					Log.e("rtmap", "开始加载");
				} else if (progress == Constants.MAP_FailNetResult) {// 校验结果失败
					Log.e("rtmap", "地图校验结果：" + (String) msg.obj);
				} else if (progress == Constants.MAP_FailCheckNet) {// 联网检测失败
					Log.e("rtmap", "校验联网失败");
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
				} else if (progress == Constants.MAP_LOAD_END) {
					Log.e("rtmap", "地图加载完成");

					if (isFirst) {
						isFirst = false;
						sendEmptyMessage(1);
					}

					// mMapView.setScale(mMapView.getDefaultscale()/2);
					// mMapView.refreshMap();
				} else if (progress == Constants.MAP_LICENSE) {
					Log.e("rtmap", "license校验结果：" + (String) msg.obj);
				}
				break;
			case 1:

				RMNavigationUtil.requestNavigation(XunluMap.getInstance()
						.getApiKey(), "860100010030100002", mNavigationList
						.get(i), mNavigationList.get(i + 1), null, false,
						new OnNavigationListener() {

							@Override
							public void onFinished(RMRoute route) {
								if(route.getError_code()==0){
									mRouteLayer.setNavigatePoints(route.getPointlist());
									mMapView.refreshMap();
									if(i==1){
										mMapView.initMapConfig("860100010030100002", "F2");
									}
//									if(i==0){
//										mMapView.initMapConfig("860100010030100002", "F3");
//									}
									i++;
									if(i==3){
										i=0;
									}
									sendEmptyMessageDelayed(1, 4000);
								}else{
									Toast.makeText(getApplicationContext(), route.getError_msg(), 3000).show();
								}
							}
						});
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loc_map);
		Handlerlist.getInstance().register(mHandler);
		RMLog.LOG_LEVEL = RMLog.LOG_LEVEL_INFO;
		XunluMap.getInstance().init(this);// 初始化
		XunluMap.getInstance().setRootFolder("TestDingtao/public");
		mMapView = (MapView) findViewById(R.id.map_view);
		mNavigationList = new ArrayList<POI>();
		mNavigationList.add(poi1);
		mNavigationList.add(poi2);
		mNavigationList.add(poi3);
		mNavigationList.add(poi4);
		Bitmap mstartBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.da_marker_red);// 起点图片
		Bitmap mendBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.da_marker_red);// 终点图片
		mRouteLayer = new RouteLayer(mMapView, mstartBitmap, mendBitmap, null);
		mMapView.addMapLayer(mRouteLayer);
		mMapView.initMapConfig("860100010030100002", "F2");// 打开地图（建筑物id，楼层id）
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Handlerlist.getInstance().remove(mHandler);
		mMapView.clearMapLayer(); // 清除图层
	}
}
