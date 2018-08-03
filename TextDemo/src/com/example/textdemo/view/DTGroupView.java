package com.example.textdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class DTGroupView extends FrameLayout{
	
	public DTGroupView(Context context) {
		super(context);
	}
	
	public DTGroupView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DTGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		Log.i("dt", "group+++dispatchTouchEvent");
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		Log.i("dt", "group+++onInterceptTouchEvent");
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.i("dt", "group+++onTouchEvent");
		return super.onTouchEvent(event);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.i("dt", "group+++onLayout");
		getChildAt(0).layout(l, t, r, b);
		getChildAt(1).layout(l, t, r, b);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Log.i("dt", "group+++onMeasure");
		getChildAt(0).measure(widthMeasureSpec, heightMeasureSpec);
		getChildAt(1).measure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.i("dt", "group+++onDraw");
	}

}
