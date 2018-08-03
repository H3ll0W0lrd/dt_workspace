package com.rtmap.ambassador.layer;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.view.MotionEvent;

import com.rtm.frm.map.BaseMapLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtmap.ambassador.model.Area;

public class AreaLayer implements BaseMapLayer {

	private MapView mMapView;
	private Paint mPaint;

	private Area mArea;
	private Path mPath;

	public AreaLayer(MapView view) {
		mMapView = view;
		initLayer(view);
	}

	public void setArea(Area area) {
		mArea = area;
	}

	public Area getArea() {
		return mArea;
	}

	@Override
	public void initLayer(MapView view) {
		mPaint = new Paint();
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(3);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeCap(Cap.ROUND);
		mPaint.setStrokeJoin(Join.ROUND);
		mPaint.setColor(Color.RED);

		mPath = new Path();
	}

	public void setRouteColor(int color) {
		mPaint.setColor(color);
	}

	@Override
	public void onDraw(Canvas canvas) {
		mPath.reset();
		if (mArea == null)
			return;
		ArrayList<Location> list = mArea.getCoords();
		for (int i = 0; i < list.size(); i++) {
			Location l = list.get(i);
			PointInfo t = mMapView.fromLocation(l);
			if (i == 0) {
				mPath.moveTo(t.getX(), t.getY());
			} else {
				mPath.lineTo(t.getX(), t.getY());
			}
		}
		mPath.close();
		canvas.drawPath(mPath, mPaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	@Override
	public void destroyLayer() {
	}

	/**
	 * 是否有数据
	 */
	@Override
	public boolean hasData() {
		return mArea == null;
	}

	@Override
	public void clearLayer() {

	}

	@Override
	public boolean onTap(MotionEvent event) {
		return false;
	}
}
