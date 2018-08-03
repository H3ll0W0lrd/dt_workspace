package com.example.textdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class DTView extends TextView{
	public DTView(Context context) {
		super(context);
	}
	
	public DTView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DTView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		Log.i("dt", "DTView+++dispatchTouchEvent");
		return super.dispatchTouchEvent(event);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.i("dt", "DTView+++onTouchEvent");
		return super.onTouchEvent(event);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		Log.i("dt", "DTView+++onLayout");
		super.onLayout(changed, left, top, right, bottom);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.i("dt", "DTView+++onMeasure");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		Log.i("dt", "DTView+++onDraw");
		super.onDraw(canvas);
	}
}
