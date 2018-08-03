package com.example.textdemo.view;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

import com.dingtao.libs.util.DTLog;
import com.example.textdemo.model.StatelliteIitem;

public class DTStatelliteView extends ViewGroup implements
		AnimatorUpdateListener {

	private Paint mPaint2, mPaint;
	private ArrayList<StatelliteIitem> mList;

	public DTStatelliteView(Context context) {
		super(context);
		init(context);
	}

	public DTStatelliteView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DTStatelliteView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		setWillNotDraw(false);
		mList = new ArrayList<StatelliteIitem>();
		mList.add(new StatelliteIitem(0, "哈哈"));
		mList.add(new StatelliteIitem(1, "呜呜"));
		mList.add(new StatelliteIitem(2, "啊啊"));
		mPaint2 = new Paint();
		mPaint2.setColor(getResources().getColor(
				android.R.color.holo_green_dark));
		// mPaint2.setStyle(Paint.Style.STROKE);
		mPaint2.setTextSize(20);
		mPaint2.setAntiAlias(true);
		mPaint = new Paint();
		mPaint.setColor(getResources().getColor(android.R.color.holo_purple));
		// mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(2);
		mPaint.setTextSize(20);

		setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				DTLog.i("长按啊");
				show(downX, downY);
				return false;
			}
		});
	}

	private static final int RADIUS = 150;
	private float itemDegrees = 60;// 每个间隔是45度
	private int startDegrees = -120;// 开始角度
	private float downX, downY;
	private boolean isAnimFinash;

	public void show() {
		show(downX, downY);
	}

	public void show(float x, float y) {
		show(x, y, true);
	}

	public void show(float x, float y, boolean isClearLociton) {
		for (int i = 0; i < mList.size(); i++) {
			StatelliteIitem item = mList.get(i);
			if (isClearLociton) {
				item.setX(x);
				item.setY(y);
			}
			float degrees = i * itemDegrees + startDegrees;
			item.setDegrees((degrees + 720) % 360);
			double cos = Math.cos(Math.toRadians(degrees));
			double sin = Math.sin(Math.toRadians(degrees));
			if (degrees > 0 && degrees < 90) {
			} else if (degrees > 90 && degrees < 180) {
			} else if (degrees > 180 && degrees < 270) {
				sin = -sin;
				cos = -cos;
			} else if (degrees > 270 && degrees < 360) {
				sin = -sin;
				cos = -cos;
			}
			float itemx = (float) (cos * RADIUS + x);
			float itemy = (float) (sin * RADIUS + y);
			DTLog.i("位置:" + itemx + "     " + itemy);
			ObjectAnimator animator = ObjectAnimator.ofFloat(item, "y", y,
					itemy);
			ObjectAnimator animator2 = ObjectAnimator.ofFloat(item, "x", x,
					itemx);
			AnimatorSet set = new AnimatorSet();
			animator.setInterpolator(new AccelerateInterpolator());
			animator2.setInterpolator(new AccelerateInterpolator());
			set.playTogether(animator, animator2);
			set.setDuration(800);
			set.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator animation) {
					isAnimFinash = false;
				}

				@Override
				public void onAnimationRepeat(Animator animation) {

				}

				@Override
				public void onAnimationEnd(Animator animation) {
					isAnimFinash = true;
				}

				@Override
				public void onAnimationCancel(Animator animation) {
					// TODO Auto-generated method stub

				}
			});
			animator.addUpdateListener(this);
			set.start();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Log.i("dt", "onMeasure:" + widthMeasureSpec + "     "
				+ heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.i("dt", "left:" + l + "   top:" + t + "    right: " + r
				+ "   bottom: " + b);
		DTLog.i(getWidth() + "  ***  " + getHeight());
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawCircle(downX - 5, downY - 5, 10, mPaint2);
		for (int i = 0; i < mList.size(); i++) {
			StatelliteIitem item = mList.get(i);
			if (item.isSelect()) {
				canvas.drawCircle(item.getX() - 5, item.getY() - 5, 10, mPaint2);
			} else {
				canvas.drawCircle(item.getX() - 5, item.getY() - 5, 10, mPaint);
			}
			canvas.drawText(item.getId() + "", item.getX(), item.getY(), mPaint);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			DTLog.e("down...");
			downX = event.getX();
			downY = event.getY();
			for (int i = 0; i < mList.size(); i++) {
				mList.get(i).setSelect(false);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (isAnimFinash) {
				float x = event.getX();
				float y = event.getY();
				for (int i = 0; i < mList.size(); i++) {
					StatelliteIitem item = mList.get(i);
					float degree = (float) Math.toDegrees(Math.atan((x - downX)
							/ (y - downY)));
					degree = getDegrees(x, y, downX, downY, degree);
					float dis = ((degree - item.getDegrees()) + 720) % 360;// 如果差值大于360-22.5或者小于22.5
					DTLog.i("触摸点的角度为：" + degree + "      显示点角度为："
							+ item.getDegrees());
					if (dis > 360 - itemDegrees/2 || dis < itemDegrees/2) {
						item.setSelect(true);
					} else {
						item.setSelect(false);
					}
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			break;
		}
		invalidate();
		return super.dispatchTouchEvent(event);
	}

	private float getDegrees(float x, float y, float centerX, float centerY,
			float degree) {
		// 由于POI角度x,y差值都取了正值，所以现在需要根据x,y位置来判断向量的指向，确定实际角度
		if (x > centerX && y > centerY) {
			degree = 90 - degree;
		} else if (x < centerX && y > centerY) {
			degree = 90 - degree;
		} else if (x < centerX && y < centerY) {
			degree = 270-degree;
		} else if (x > centerX && y < centerY) {
			degree = 270-degree;
		}
		return (degree + 720) % 360;
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		invalidate();
	}

}
