package com.example.textdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Region.Op;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class DTDrawView extends ViewGroup {

	public DTDrawView(Context context) {
		super(context);
		init(context);
	}

	public DTDrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DTDrawView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private int mX, mY;
	private float mX1,mY1;
	private Paint mPaint2,mPaint;
	float h=2;

	private void init(Context context) {
		mPaint2 = new Paint();
		mPaint2.setColor(getResources().getColor(android.R.color.holo_green_dark));
//		mPaint2.setStyle(Paint.Style.STROKE);
		mPaint2.setAntiAlias(true);
		mPaint2.setStrokeWidth(10);
		mPaint2.setTextSize(20);
		mPaint = new Paint();
		mPaint.setColor(getResources().getColor(android.R.color.holo_purple));
//		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(2);
		mPaint.setTextSize(20);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
		Path path = new Path();
		path.addCircle(50, 50, 20, Direction.CW);
		canvas.clipPath(path,Op.XOR);
		canvas.drawRect(0, 0, 300, 300, mPaint);
		canvas.drawCircle(50, 50, 30, mPaint2);
//		
//		canvas.drawCircle(180, 180, 20, mPaint);
	}

}
