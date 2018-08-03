package com.rtmap.locationdemo.layer;

import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Region.Op;
import android.view.MotionEvent;

import com.rtm.frm.map.BaseMapLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.model.Location;

public class CircleLayer implements BaseMapLayer {

	private Paint mPaint;
	private MapView mapview;

	public CircleLayer(MapView mapview) {
		this.mapview = mapview;
		mPaint = new Paint();
		mPaint.setColor(0xFFAAee22);
	}

	@Override
	public void initLayer(MapView view) {
	}

	@Override
	public boolean onTap(MotionEvent event) {
		return false;
	}

	@Override
	public void destroyLayer() {

	}

	@Override
	public void clearLayer() {

	}

	@Override
	public boolean hasData() {
		return false;
	}

	Path path = new Path();

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	@Override
	public void onDraw(Canvas canvas) {
		path.addCircle(0, 0, 100, Direction.CW);
		canvas.clipPath(path, Op.INTERSECT);
		canvas.drawRect(mapview.getLeft(), mapview.getTop(),
				mapview.getRight(), mapview.getBottom(), mPaint);
	}

}
