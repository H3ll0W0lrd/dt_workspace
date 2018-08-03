package com.rtmap.locationdemo.layer;

import java.util.HashMap;
import java.util.Iterator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.rtm.frm.map.BaseMapLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtm.frm.utils.OnMapTapedListener;

public class ImageLayer implements BaseMapLayer {

	private HashMap<Location, Bitmap> pointMap;
	private MapView mapview;
	private OnMapTapedListener onMapTapedListener;

	public ImageLayer(MapView mapview) {
		this.mapview = mapview;
		pointMap = new HashMap<Location, Bitmap>();
	}

	public void setOnMapTapedListener(OnMapTapedListener onMapTapedListener) {
		this.onMapTapedListener = onMapTapedListener;
	}

	@Override
	public void initLayer(MapView view) {

	}

	public void addLocation(Location location, Bitmap bitmap) {
		if (location != null && bitmap != null) {
			pointMap.put(location, bitmap);
		}
	}

	public void remove(Location location) {
		if (location != null && pointMap.containsKey(location)) {
			pointMap.remove(location);
		}
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
			Location clickPoint = null;
			if (Math.abs(e.getX() - downX) < 40
					&& Math.abs(e.getY() - downY) < 40) {// 如果按下与抬起距离在20像素范围内，可视为点击
				Iterator<Location> iterator = pointMap.keySet().iterator();
				while (iterator.hasNext()) {
					Location p = iterator.next();
					Bitmap bitmap = pointMap.get(p);
					PointInfo temppoi = mapview.fromLocation(p);
					if (temppoi.getX() < 0 || temppoi.getY() < 0)// 屏幕外的不用计算
						continue;
					float left = temppoi.getX() - bitmap.getWidth() / 2f;
					float right = temppoi.getX() + bitmap.getWidth() / 2f;
					float top = temppoi.getY() - bitmap.getHeight();
					float bottom = temppoi.getY();
					if (e.getX() > left && e.getX() < right && e.getY() > top
							&& e.getY() < bottom) {
						clickPoint = p;// 保存距离范围内的点
						break;
					}
				}
				if (clickPoint != null) {// 说明点击在点的范围内
					onMapTapedListener.onMapTaped(null,clickPoint);
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
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (pointMap.size() > 0) {
			Iterator<Location> keySet = pointMap.keySet().iterator();
			while (keySet.hasNext()) {
				Location location = keySet.next();
				PointInfo point = mapview.fromLocation(location);
				Bitmap bitmap = pointMap.get(location);
				canvas.drawBitmap(bitmap, point.getX() - bitmap.getWidth()
						/ 2.0f, point.getY() - bitmap.getHeight(), null);
			}

		}

	}

	@Override
	public void clearLayer() {
		// TODO Auto-generated method stub
		
	}

}
