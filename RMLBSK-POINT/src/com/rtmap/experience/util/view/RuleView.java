package com.rtmap.experience.util.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.rtmap.experience.R;

public class RuleView extends ImageView {

	public RuleView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initPaint();
	}

	public RuleView(Context context) {
		super(context);
		initPaint();
	}

	public RuleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint();
	}
	private Paint mPaint;
	private void initPaint() {
		mPaint = new Paint();
		mPaint.setColor(getResources().getColor(R.color.black));
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(2);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawLine(0, 5, 100, 5, mPaint);
		canvas.drawLine(0, 0, 0, 5, mPaint);
		canvas.drawLine(100, 0, 100, 5, mPaint);
	}
}
