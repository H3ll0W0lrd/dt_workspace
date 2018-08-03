package com.rtmap.wisdom.util.view;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class DTCircleView extends View implements AnimatorUpdateListener {

	private ValueAnimator animator;

	public DTCircleView(Context context) {
		super(context);
		init();
	}

	public DTCircleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DTCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public void start() {
		animator = ValueAnimator.ofInt(100, getWidth() / 2 - 20);
		animator.setDuration(2000);
		animator.addUpdateListener(this);
		animator.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				isDraw = false;
				invalidate();
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				
			}
		});
		animator.start();
	}

	private Paint mRoutePaint;

	private void init() {

		mRoutePaint = new Paint();
		mRoutePaint.setStyle(Style.STROKE);
		mRoutePaint.setStrokeWidth(2);
		mRoutePaint.setAntiAlias(true);
		mRoutePaint.setColor(0xff373745);
	}
	
	private boolean isDraw = true;

	@Override
	protected void onDraw(Canvas canvas) {
		if (animator != null&&isDraw) {
			canvas.drawCircle(getWidth() / 2, getHeight() / 2,
					(Integer) animator.getAnimatedValue(), mRoutePaint);
			if ((Integer) animator.getAnimatedValue() > 180) {
				canvas.drawCircle(getWidth() / 2, getHeight() / 2,
						(Integer) animator.getAnimatedValue() - 80, mRoutePaint);
			}
		}
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		invalidate();
	}
}
