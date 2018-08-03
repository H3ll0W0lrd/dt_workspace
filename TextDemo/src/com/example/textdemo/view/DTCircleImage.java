package com.example.textdemo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.dingtao.libs.util.DTIOUtil;
import com.example.textdemo.R;

public class DTCircleImage extends ImageView {

	public DTCircleImage(Context context) {
		super(context);
		init(context);
	}

	public DTCircleImage(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DTCircleImage(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private int mX, mY;
	private float mX1, mY1;
	private Paint mPaint2, mPaint;
	float h = 2;
	private Bitmap mBitmap;

	private void init(Context context) {
		mPaint2 = new Paint();
		mPaint2.setColor(getResources().getColor(
				android.R.color.holo_green_dark));
		mPaint2.setStyle(Paint.Style.STROKE);
		mPaint2.setAntiAlias(true);
		mPaint2.setStrokeWidth(10);
		mPaint2.setTextSize(20);
		mPaint = new Paint();
		mPaint.setColor(getResources().getColor(android.R.color.holo_purple));
		// mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(2);
		mPaint.setTextSize(20);
		Drawable red = getResources().getDrawable(R.drawable.i);
		mBitmap = DTIOUtil.drawableToBitmap(red);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		LayoutParams parmas = (LayoutParams) getLayoutParams();
		parmas.height = mBitmap.getHeight();
		parmas.width = mBitmap.getWidth();
		setLayoutParams(parmas);
	}
	
	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		mBitmap = bm;
		LayoutParams parmas = (LayoutParams) getLayoutParams();
		parmas.height = mBitmap.getHeight();
		parmas.width = mBitmap.getWidth();
		setLayoutParams(parmas);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mBitmap != null) {
			Path path = new Path();
			int width = mBitmap.getWidth() / 2;
			int height = mBitmap.getHeight() / 2;
			int radius = width > height ? height : width;
			path.addCircle(width, height, radius - 12, Direction.CW);
			canvas.clipPath(path, Op.XOR);
			canvas.drawCircle(width, height, radius - 7, mPaint2);
			canvas.clipPath(path, Op.REPLACE);
			canvas.rotate(90, width, height);
			canvas.drawBitmap(mBitmap, 0, 0, mPaint);
			canvas.drawColor(0xaaffffff);
		}
		// canvas.drawCircle(180, 180, 20, mPaint);
	}

}
