package com.rtmap.locationdemo;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rtm.common.model.POI;
import com.rtm.common.model.RMPois;
import com.rtm.common.utils.Constants;
import com.rtm.common.utils.OnSearchPoiListener;
import com.rtm.common.utils.RMLog;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.POILayer;
import com.rtm.frm.map.RouteLayer;
import com.rtm.frm.map.TapPOILayer;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.utils.Handlerlist;
import com.rtm.frm.utils.RMBuildListUtil;
import com.rtm.frm.utils.RMHotShopTenUtil;
import com.rtm.frm.utils.RMNavigationUtil;
import com.rtm.frm.utils.RMSearchAssoicationUtil;
import com.rtm.frm.utils.RMSearchPoiUtil;
import com.rtmap.locationdemo.beta.R;
import com.rtmap.locationdemo.layer.CircleLayer;

/**
 * 纯地图
 * 
 * @author dingtao
 *
 */
public class MapActivity extends Activity {

	private MapView mMapView;// 地图view

	private TapPOILayer mTapPOILayer;// 点击图层
	private RouteLayer mRouteLayer;// 导航路线图层
	private POILayer mPoiLayer;// 搜索结果图层
	private CompassLayer mCompassLayer;// 指南针图层
	private RMSearchPoiUtil mSearchPoiUtil;// 搜索POI工具

	private ArrayList<POI> mNavigationList;

	Location mLocation = new Location(342f, -100f, "F1");// 虚拟定位结果（x，y，floor），测试用
	private String buildString = "860100010020300001";// 建筑物id，测试用
	private String[] floorArray = new String[] { "F1", "F2", "F3", "F4", "F5",
			"F6", "F7" };

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

					// mMapView.setScale(mMapView.getDefaultscale()/2);
					// mMapView.refreshMap();
				} else if (progress == Constants.MAP_LICENSE) {
					Log.e("rtmap", "license校验结果：" + (String) msg.obj);
				}
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		Handlerlist.getInstance().register(mHandler);
		RMLog.LOG_LEVEL = RMLog.LOG_LEVEL_INFO;
		initColorDialog();
		mNullFill.setText("0x"
				+ Integer.toHexString(MapView.MAPINVALID.getColorfill()));
		mNullBorder.setText("0x"
				+ Integer.toHexString(MapView.MAPINVALID.getColorborder()));
		mVoidFill.setText("0x"
				+ Integer.toHexString(MapView.MAPUNKNOWN.getColorfill()));
		mVoidBorder.setText("0x"
				+ Integer.toHexString(MapView.MAPUNKNOWN.getColorborder()));
		mPoiFill.setText("0x"
				+ Integer.toHexString(MapView.MAPPOI.getColorfill()));
		mPoiBorder.setText("0x"
				+ Integer.toHexString(MapView.MAPPOI.getColorborder()));
		mWcFill.setText("0x"
				+ Integer.toHexString(MapView.MAPWC.getColorfill()));
		mWcBorder.setText("0x"
				+ Integer.toHexString(MapView.MAPWC.getColorborder()));
		mStairFill.setText("0x"
				+ Integer.toHexString(MapView.MAPSTAIRS.getColorfill()));
		mStatirBorder.setText("0x"
				+ Integer.toHexString(MapView.MAPSTAIRS.getColorborder()));
		mGroundFill.setText("0x"
				+ Integer.toHexString(MapView.MAPGROUND.getColorfill()));
		mGroundBorder.setText("0x"
				+ Integer.toHexString(MapView.MAPGROUND.getColorborder()));
		mTextColor.setText("0x"
				+ Integer.toHexString(MapView.MAPTEXT.getTextcolor()));
		mTextSize.setText(MapView.MAPTEXT.getTextsize() + "");
		mNavigationList = new ArrayList<POI>();
		XunluMap.getInstance().init(this);// 初始化
		XunluMap.getInstance().setRootFolder("TestDingtao/public");
		mMapView = (MapView) findViewById(R.id.map_view);
		// MapView.MAPGROUND.setWidthborder(10);
		mMapView.getSurfaceView().setZOrderOnTop(true);
		mMapView.getSurfaceView().getHolder()
				.setFormat(PixelFormat.TRANSLUCENT);
		mMapView.setMapBackgroundColor(0xaaFFFFFF);// 设置背景色
		mMapView.setDrawText(false);
		initLayers();// 初始化图层
//		mMapView.addMapLayer(new CircleLayer(mMapView));
		mSearchPoiUtil = new RMSearchPoiUtil();

		mTapPOILayer
				.setOnPOITappedListener(new TapPOILayer.OnPOITappedListener() {

					@Override
					public Bitmap onPOITapped(POI poi) {// 回调函数，用于设置点击地图时弹出的图标
						poi.getY();
						poi.getX();
						poi.getFloor();
						poi.getName();
						Bitmap mBitmap = BitmapFactory.decodeResource(
								MapActivity.this.getResources(),
								R.drawable.map_poi);

						return mBitmap;
					}
				});

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
						if (mNavigationList.size() >= 2)
							RMNavigationUtil.requestNavigation(
									XunluMap.getInstance().getApiKey(),
									mMapView.getBuildId(),
									mNavigationList.get(0),
									mNavigationList.get(1),
									null,
									false,
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
						return mTapPoiView;

					}
				});
		mPoiLayer.setPoiIcon(BitmapFactory.decodeResource(
				MapActivity.this.getResources(), R.drawable.da_marker_red));

		mMapView.initMapConfig(buildString, floorArray[0]);// 打开地图（建筑物id，楼层id）
		mMapView.setScale(0.1f);
		mMapView.setDrawLogo(false);
	}

	private int count = 0;

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.zoom_up:// 放大
			mMapView.setScale(mMapView.getScale() / 2);
			break;

		case R.id.zoom_down:// 缩小
			mMapView.setScale(mMapView.getScale() * 2);
			break;

		case R.id.initmap:// 打开地图
			if ("860100010020300001".equals(mMapView.getBuildId())) {
				count++;
				mMapView.initMapConfig(buildString, floorArray[count % 7]);
			}
			break;

		case R.id.qiehuan:// 切换建筑物
			if (buildString.equals(mMapView.getBuildId())) {
				mMapView.initMapConfig("860100010040500017", "F10");
			} else {
				mMapView.initMapConfig(buildString, floorArray[count % 7]);
			}
			break;

		case R.id.xuanzhuan:// 设置地图角度
			mMapView.setMapAngle(mMapView.getMapAngle() + 10f);
			break;
		case R.id.clean:// 清除图层
			mMapView.clearLayers();
			break;
		case R.id.color:// 配色
			mColorDialog.show();
			break;
		case R.id.ok:
			mColorDialog.cancel();
			break;
		case R.id.cancel:
			mColorDialog.cancel();
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.submenu, menu);

		return true;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.tuodong:// 禁止拖动
			mMapView.setRoamable(!mMapView.isRoamable());
			if (mMapView.isRoamable()) {
				item.setTitle("禁止拖动");
			} else {
				item.setTitle("恢复拖动");
			}
			return true;
		case R.id.suofang:// 禁止缩放
			mMapView.setZoomable(!mMapView.isZoomable());
			if (mMapView.isZoomable()) {
				item.setTitle("禁止缩放");
			} else {
				item.setTitle("恢复缩放");
			}
			return true;
		case R.id.shuangji:// 禁止双击放大
			mMapView.setDoubleTapable(!mMapView.isDoubleTapable());
			if (mMapView.isDoubleTapable()) {
				item.setTitle("禁止双击放大");
			} else {
				item.setTitle("恢复双击放大");
			}
			return true;
		case R.id.xuanzhuan:// 禁止旋转
			mMapView.setRotateable(!mMapView.isRotateable());
			if (mMapView.isRotateable()) {
				item.setTitle("禁止旋转");
			} else {
				item.setTitle("恢复旋转");
			}
			return true;
		case R.id.key:// 关键字搜索
			String mKeyword = "A";
			mSearchPoiUtil.setKey(XunluMap.getInstance().getApiKey())
					.setBuildid(mMapView.getBuildId()).setKeywords(mKeyword)
					.setFloor("F1")
					.setOnSearchPoiListener(new OnSearchPoiListener() {

						@Override
						public void onSearchPoi(RMPois result) {
							if (result.getError_code() == 0) {
								mPoiLayer.destroyLayer();
								mPoiLayer.addPoiList(result.getPoilist());
								mMapView.refreshMap();
							}
						}
					}).searchPoi();

			return true;
		case R.id.catalog:// 某一楼层分类搜索
			// RMFloorPoiCateUtil.requestFloorPoiCate(XunluMap.getInstance()
			// .getApiKey(), mMapView.getBuildId(), mMapView.getFloor(),
			// new RMFloorPoiCateUtil.onGetFloorPoiCateListener() {
			//
			// @Override
			// public void onFinished(RMCateList result) {
			// }
			// });
			// RMBuildPoiCateUtil.requestBuildPoiCate(XunluMap.getInstance()
			// .getApiKey(), mMapView.getBuildId(),
			// new RMBuildPoiCateUtil.OnGetBuildPoiCateListener() {
			//
			// @Override
			// public void onFinished(RMCateList result) {
			// if (result.getError_code() == 0) {
			// }
			// }
			// });
			// RMCityListUtil.requestCityList("R3ovVkOBQU", new
			// RMCityListUtil.OnGetCityListListener() {
			//
			// @Override
			// public void onFinished(RMCityList result) {
			//
			// }
			// });
			RMBuildListUtil.requestBuildList("R3ovVkOBQU", "北京", null);
			return true;
		case R.id.think:// 联想列表
			RMSearchAssoicationUtil assoication = new RMSearchAssoicationUtil();
			assoication.setKey(XunluMap.getInstance().getApiKey())
					.setBuildid(mMapView.getBuildId()).setKeywords("M")
					.setOnSearchPoiListener(new OnSearchPoiListener() {

						@Override
						public void onSearchPoi(RMPois result) {
							if (result.getError_code() == 0) {
								mPoiLayer.destroyLayer();
								mPoiLayer.addPoiList(result.getPoilist());
								mMapView.refreshMap();
							}
						}
					}).searchPoi();
			return true;
		case R.id.shop_top_ten:// 热门店铺TOP10
			RMHotShopTenUtil shopten = new RMHotShopTenUtil();
			shopten.setKey(XunluMap.getInstance().getApiKey())
					.setOnSearchPoiListener(new OnSearchPoiListener() {

						@Override
						public void onSearchPoi(RMPois result) {
							Gson gson = new Gson();
							Log.i("rtmap", gson.toJson(result));
						}
					}).searchShopTen();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		Handlerlist.getInstance().remove(mHandler);
		mMapView.clearMapLayer(); // 清除图层
		super.onDestroy();

	}

	private Dialog mColorDialog;
	private EditText mNullFill, mNullBorder, mVoidFill, mVoidBorder, mPoiFill,
			mPoiBorder, mWcFill, mWcBorder, mStairFill, mStatirBorder,
			mGroundFill, mGroundBorder, mTextColor, mTextSize;

	/**
	 * 初始化颜色框
	 */
	private void initColorDialog() {
		mColorDialog = new Dialog(MapActivity.this);
		mColorDialog.setTitle("配色");
		mColorDialog.setContentView(R.layout.color_dialog);
		mNullFill = (EditText) mColorDialog.findViewById(R.id.nullfill);
		mNullBorder = (EditText) mColorDialog.findViewById(R.id.nullborder);
		mVoidFill = (EditText) mColorDialog.findViewById(R.id.voidfill);
		mVoidBorder = (EditText) mColorDialog.findViewById(R.id.voidborder);
		mPoiFill = (EditText) mColorDialog.findViewById(R.id.poifill);
		mPoiBorder = (EditText) mColorDialog.findViewById(R.id.poiborder);
		mWcFill = (EditText) mColorDialog.findViewById(R.id.wcfill);
		mWcBorder = (EditText) mColorDialog.findViewById(R.id.wcborder);
		mStairFill = (EditText) mColorDialog.findViewById(R.id.stairfill);
		mStatirBorder = (EditText) mColorDialog.findViewById(R.id.stairborder);
		mGroundFill = (EditText) mColorDialog.findViewById(R.id.groundfill);
		mGroundBorder = (EditText) mColorDialog.findViewById(R.id.groundborder);
		mTextColor = (EditText) mColorDialog.findViewById(R.id.textcolor);
		mTextSize = (EditText) mColorDialog.findViewById(R.id.textsize);
		mColorDialog.findViewById(R.id.ok).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						MapView.MAPINVALID.setColorfill(getColorInt(mNullFill
								.getText().toString()));
						MapView.MAPINVALID
								.setColorborder(getColorInt(mNullBorder
										.getText().toString()));
						MapView.MAPUNKNOWN.setColorfill(getColorInt(mVoidFill
								.getText().toString()));
						MapView.MAPUNKNOWN
								.setColorborder(getColorInt(mVoidBorder
										.getText().toString()));
						MapView.MAPPOI.setColorfill(getColorInt(mPoiFill
								.getText().toString()));
						MapView.MAPPOI.setColorborder(getColorInt(mPoiBorder
								.getText().toString()));
						MapView.MAPSTAIRS.setColorfill(getColorInt(mStairFill
								.getText().toString()));
						MapView.MAPSTAIRS
								.setColorborder(getColorInt(mStatirBorder
										.getText().toString()));
						MapView.MAPGROUND.setColorfill(getColorInt(mGroundFill
								.getText().toString()));
						MapView.MAPGROUND
								.setColorborder(getColorInt(mGroundBorder
										.getText().toString()));
						MapView.MAPWC.setColorfill(getColorInt(mWcFill
								.getText().toString()));
						MapView.MAPWC.setColorborder(getColorInt(mWcBorder
								.getText().toString()));
						MapView.MAPTEXT.setTextcolor(getColorInt(mTextColor
								.getText().toString()));
						MapView.MAPTEXT.setTextsize(Integer.parseInt(mTextSize
								.getText().toString()));
						mColorDialog.cancel();
						mMapView.refreshMap();
					}
				});
		mColorDialog.findViewById(R.id.cancel).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						mColorDialog.cancel();
					}
				});
	}

	/**
	 * 把16进制8位颜色值字符串转化为int型
	 * 
	 * @param str
	 * @return
	 */
	public static int getColorInt(String str) {
		str = str.replace("0x", "");
		char[] array = str.toCharArray();
		int value = 0;
		for (int i = 0; i < array.length; i++) {
			value += (Integer.parseInt(array[i] + "", 16) * (int) Math.pow(16,
					array.length - i - 1));
		}
		return value;
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
		mRouteLayer = new RouteLayer(mMapView, mstartBitmap, mendBitmap, null);
		// mRouteLayer.setShowOtherFloor(false);
		mPoiLayer = new POILayer(mMapView);
		mCompassLayer = new CompassLayer(mMapView);

		mMapView.addMapLayer(mPoiLayer);
		mMapView.addMapLayer(mTapPOILayer);
		mMapView.addMapLayer(mRouteLayer);
		mMapView.addMapLayer(mCompassLayer);
	}

}