package com.airport.test.util.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.airport.test.R;
import com.dingtao.libs.util.DTIOUtil;
import com.dingtao.libs.util.DTUIUtil;

public class DTCircleImage extends ImageView {

	public DTCircleImage(Context context) {
		super(context);
	}

	public DTCircleImage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DTCircleImage(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	private Bitmap mBitmap;

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mBitmap != null) {
			LayoutParams parmas = (LayoutParams) getLayoutParams();
			parmas.height = mBitmap.getHeight();
			parmas.width = mBitmap.getWidth();
			setLayoutParams(parmas);
		}
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		mBitmap = bm;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mBitmap != null) {
			int width = mBitmap.getWidth() / 2;
			int height = mBitmap.getHeight() / 2;
			int radius = width > height ? height : width;
			Path path = new Path();
			path.addCircle(radius, radius, radius, Path.Direction.CW);
			canvas.clipPath(path);
			canvas.drawBitmap(mBitmap, 0, 0, null);
		}
	}

}
