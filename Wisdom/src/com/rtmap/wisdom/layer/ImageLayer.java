package com.rtmap.wisdom.layer;

import java.util.HashMap;
import java.util.Iterator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.rtm.common.model.POI;
import com.rtm.frm.map.BaseMapLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.TapPOILayer.OnPOITappedListener;
import com.rtm.frm.model.PointInfo;

public class ImageLayer implements BaseMapLayer {

	private HashMap<POI, Bitmap> pointMap;
	private MapView mapview;
	private OnPOITappedListener onMapTapedListener;

	public ImageLayer(MapView mapview) {
		this.mapview = mapview;
		pointMap = new HashMap<POI, Bitmap>();
	}

	public void setOnMapTapedListener(OnPOITappedListener onMapTapedListener) {
		this.onMapTapedListener = onMapTapedListener;
	}

	@Override
	public void initLayer(MapView view) {

	}

	public void addLocation(POI poi, Bitmap bitmap) {
		destroyLayer();
		pointMap.put(poi, bitmap);
	}

	private float downX, downY;

	@Override
	public boolean onTap(MotionEvent e) {
		if (onMapTapedListener == null) {
			return false;
		}
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			downX = e.getX();
			downY = e.getY();
		} else if (e.getAction() == MotionEvent.ACTION_UP
				|| e.getAction() == MotionEvent.ACTION_CANCEL) {// 当手指抬起时的
			POI clickPoint = null;
			if (Math.abs(e.getX() - downX) < 40
					&& Math.abs(e.getY() - downY) < 40) {// 如果按下与抬起距离在20像素范围内，可视为点击
				Iterator<POI> iterator = pointMap.keySet().iterator();
				while (iterator.hasNext()) {
					POI p = iterator.next();
					if (!p.getBuildId().equals(mapview.getBuildId())
							|| !p.getFloor().equals(mapview.getFloor()))
						continue;
					Bitmap bitmap = pointMap.get(p);
					PointInfo temppoi = mapview
							.fromLocation(p.getX(), p.getY_abs());
					if (temppoi.getX() < 0 || temppoi.getY() < 0)// 屏幕外的不用计算
						continue;
					float left = temppoi.getX() - bitmap.getWidth() / 2f;
					float right = temppoi.getX() + bitmap.getWidth() / 2f;
					float top = temppoi.getY() - bitmap.getHeight()-55;
					float bottom = temppoi.getY();
					if (e.getX() > left && e.getX() < right && e.getY() > top
							&& e.getY() < bottom) {
						clickPoint = p;// 保存距离范围内的点
						break;
					}
				}
				if (clickPoint != null) {// 说明点击在点的范围内
					onMapTapedListener.onPOITapped(clickPoint);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void destroyLayer() {
		pointMap.clear();
	}

	@Override
	public boolean hasData() {
		return pointMap.size()>0;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (pointMap.size() > 0) {
			Iterator<POI> keySet = pointMap.keySet().iterator();
			while (keySet.hasNext()) {
				POI p = keySet.next();
				if (!p.getBuildId().equals(mapview.getBuildId())
						|| !p.getFloor().equals(mapview.getFloor()))
					continue;
				PointInfo point = mapview.fromLocation(p.getX(), p.getY_abs());
				Bitmap bitmap = pointMap.get(p);
				canvas.drawBitmap(bitmap, point.getX() - bitmap.getWidth()
						/ 2.0f, point.getY() - bitmap.getHeight()-55, null);
			}

		}

	}

	@Override
	public void clearLayer() {

	}

}
