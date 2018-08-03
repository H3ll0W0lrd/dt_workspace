package com.rtmap.locationdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.rtm.common.utils.Constants;
import com.rtm.common.utils.RMLog;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.utils.Handlerlist;
import com.rtmap.locationdemo.beta.R;

public class MapColorActivity extends Activity {

	private MapView mMapView;// 地图view

	private CompassLayer mCompassLayer;// 指南针图层
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
				} else if (progress == Constants.MAP_Down_Fail) {
					Log.e("rtmap", "地图下载失败");
				} else if (progress == Constants.MAP_LOAD_END) {
					Log.e("rtmap", "地图加载完成");
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
		setContentView(R.layout.map_color);
		Handlerlist.getInstance().register(mHandler);
		RMLog.LOG_LEVEL = RMLog.LOG_LEVEL_INFO;

		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		initLayers();// 初始化图层

		mMapView.initMapConfig("860100010020300001", "F1");// 打开地图（建筑物id，楼层id）
		mMapView.initScale();// 初始化比例尺
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Handlerlist.getInstance().remove(mHandler);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.color1:// 配色
			setColor(color1);
			break;
		case R.id.color2:
			setColor(color2);
			break;
		case R.id.color3:
			setColor(color3);
			break;
		case R.id.color4:
			setColor(color4);
			break;
		}
	}

	int[] color1 = new int[] { 0xffe5e5e5, 0xffc9c5c3, 0xffe5e5e5, 0xffc9c5c3,
			0xffefefef, 0xffc9c5c3, 0xfff7f1eb, 0xffc9c5c3, 0xfff9f2eb,
			0xffc9c5c3, 0xfff7f1eb, 0xffc9c5c3, 0xff331004, 10 };
	int[] color2 = new int[] { 0xffe5e5e5, 0xffc9c5c3, 0xffe5e5e5, 0xffc9c5c3,
			0xffefefef, 0xffc9c5c3, 0xffcccbca, 0xffc9c5c3, 0xfff9f2ed,
			0xffc9c5c3, 0xffffffff, 0xffc9c5c3, 0xff332004, 10 };
	int[] color3 = new int[] { 0xfffcf1de, 0xfffcf1de, 0xfff9f3ea, 0xfff9e5cf,
			0xffefeeed, 0xfff9e5cf, 0xfff4ded3, 0xfff9e5cf, 0xfff9e8d4,
			0xfff9e5cf, 0xffffffff, 0xfff9e5cf, 0xff332004, 10 };
	int[] color4 = new int[] { 0xfff1fcfe, -1, 0xfff1eade, 0xffddd5c5,
			0xfffffbf0, 0xffe9bbba, 0xffadd8aa, -1, 0xffcfef81, 0xffbad860,
			0xffcccccb, 0xffdfe7e0, 0xff735A24, 10 };

	private void setColor(int[] type) {
		MapView.MAPINVALID.setColorfill(type[0]);
		MapView.MAPINVALID.setColorborder(type[1]);
		MapView.MAPUNKNOWN.setColorfill(type[2]);
		MapView.MAPUNKNOWN.setColorborder(type[3]);
		MapView.MAPPOI.setColorfill(type[4]);
		MapView.MAPPOI.setColorborder(type[5]);
		MapView.MAPWC.setColorfill(type[6]);
		MapView.MAPWC.setColorborder(type[7]);
		MapView.MAPSTAIRS.setColorfill(type[8]);
		MapView.MAPSTAIRS.setColorborder(type[9]);
		MapView.MAPGROUND.setColorfill(type[10]);
		MapView.MAPGROUND.setColorborder(type[11]);
		MapView.MAPTEXT.setTextcolor(type[12]);
		MapView.MAPTEXT.setTextsize(type[13]);
		mMapView.refreshMap();
	}

	/**
	 * 初始化图层
	 */
	private void initLayers() {
		mCompassLayer = new CompassLayer(mMapView);
		mMapView.addMapLayer(mCompassLayer);
		Drawable blue = getResources().getDrawable(R.drawable.sign_purple);
		Bitmap bitmap = drawableToBitmap(blue);
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