package com.example.textdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class DTView1 extends TextView{
	public DTView1(Context context) {
		super(context);
	}
	
	public DTView1(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DTView1(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		Log.i("dt", "DTView1+++dispatchTouchEvent");
		return super.dispatchTouchEvent(event);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.i("dt", "DTView1+++onTouchEvent");
		return super.onTouchEvent(event);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		Log.i("dt", "DTView1+++onLayout");
		super.onLayout(changed, left, top, right, bottom);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.i("dt", "DTView1+++onMeasure");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		Log.i("dt", "DTView1+++onDraw");
		super.onDraw(canvas);
	}
}
