package com.rtmap.locationdemo.layer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;

import com.rtm.frm.map.BaseMapLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;

public class FrameAnimationLayer implements BaseMapLayer,
		AnimatorUpdateListener {

	private HashMap<Location, ArrayList<Bitmap>> pointMap;
	private int times;
	private ValueAnimator mAnimator;
	private MapView mapview;
	private boolean isRefresh;

	public FrameAnimationLayer(MapView mapview) {
		this.mapview = mapview;
		pointMap = new HashMap<Location, ArrayList<Bitmap>>();
		mAnimator = ValueAnimator.ofInt(1, 20);
		mAnimator.setDuration(1000);
		mAnimator.setRepeatCount(ValueAnimator.INFINITE);
		mAnimator.addUpdateListener(this);
		mAnimator.start();
	}

	@Override
	public void initLayer(MapView view) {

	}

	public void addPointWithAnimator(Location location,
			ArrayList<Bitmap> imageList) {
		if (location != null && imageList != null && imageList.size() > 0) {
			if (pointMap.size() == 0) {
				isRefresh = true;
			}
			pointMap.put(location, imageList);
		}
	}

	public void remove(Location point) {
		if (point != null && pointMap.containsKey(point)) {
			if (pointMap.size() == 1) {
				isRefresh = false;
				times = 0;
			}
			pointMap.remove(point);
		}
	}

	@Override
	public boolean onTap(MotionEvent e) {
		return false;
	}

	@Override
	public void destroyLayer() {
		pointMap.clear();
		times = 0;
		isRefresh = false;
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
				ArrayList<Bitmap> array = pointMap.get(location);
				int index = times % array.size();
				Bitmap bitmap = array.get(index);
				canvas.drawBitmap(bitmap, point.getX() - bitmap.getWidth()
						/ 2.0f, point.getY() - bitmap.getHeight(), null);
			}

		}

	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		if (isRefresh) {
			times++;
			mapview.refreshMap();
		}
	}

	@Override
	public void clearLayer() {
		// TODO Auto-generated method stub

	}

}
