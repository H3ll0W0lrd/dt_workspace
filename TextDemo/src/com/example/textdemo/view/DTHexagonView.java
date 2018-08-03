package com.example.textdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.Path.Direction;
import android.graphics.Region.Op;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

public class DTHexagonView extends ImageView {

	public DTHexagonView(Context context) {
		super(context);
		init(context);
	}

	public DTHexagonView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DTHexagonView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private Paint mPaint2,mPaint;
	private Path mPath;
	int mPaintWidth = 15;

	private void init(Context context) {
		mPath = new Path();
		mPaint2 = new Paint();
		mPaint2.setColor(getResources().getColor(android.R.color.holo_green_dark));
		mPaint2.setStyle(Paint.Style.STROKE);
		mPaint2.setAntiAlias(true);
		mPaint2.setStrokeWidth(mPaintWidth);
		mPaint2.setTextSize(20);
		
		PathEffect peArray = new PathDashPathEffect(makePathDash(), // 形状
				mPaintWidth, // 间距
				0,// 首绘制偏移量
				PathDashPathEffect.Style.ROTATE);
		mPaint2.setPathEffect(peArray);
		
		mPaint = new Paint();
		mPaint.setColor(getResources().getColor(android.R.color.holo_purple));
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(mPaintWidth);
		
		mPaint.setTextSize(20);
	}
	
	private Path makePathDash() {
		Path p = new Path();
		float y = (float)(mPaintWidth*Math.tan(Math.toRadians(30)));
		p.moveTo(0, 0);
		p.lineTo(0, mPaintWidth);
		p.lineTo(mPaintWidth, y);
		p.lineTo(mPaintWidth, mPaintWidth-y);
		
		p.close();
		return p;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

	}
	
	private int[] center = new int[]{100,100};
	private int side = 30;

	@Override
	protected void onDraw(Canvas canvas) {
		
		float height = (float)(side*Math.tan(Math.toRadians(60)));
		
		mPath.moveTo(center[0]-height, center[1]-side);
		mPath.lineTo(center[0], center[1]-side*2);
		mPath.lineTo(center[0]+height, center[1]-side);
		mPath.lineTo(center[0]+height, center[1]+side);
		mPath.lineTo(center[0], center[1]+side*2);
		mPath.lineTo(center[0]-height, center[1]+side);
//		mPath.close();
		canvas.drawPath(mPath, mPaint);
		mPath.reset();
		mPath.moveTo(center[0]-height, center[1]-side);
		mPath.lineTo(center[0]-height, center[1]+side);
		canvas.drawPath(mPath, mPaint2);
	}

}
