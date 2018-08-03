package com.rtmap.wisdom.util.statellite;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;

import com.rtmap.wisdom.R;
import com.rtmap.wisdom.util.DTLog;

public class DTSatelliteView extends FrameLayout implements
		AnimatorUpdateListener {

	private Paint mPaint;
	private ArrayList<SatelliteIitem> mList;
	private Bitmap pointBitmap;

	public DTSatelliteView(Context context) {
		super(context);
		init(context);
	}

	public DTSatelliteView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DTSatelliteView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		setWillNotDraw(false);
		mList = new ArrayList<SatelliteIitem>();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(0xffffffff);
		mPaint.setStrokeWidth(2);
		mPaint.setTextSize(40);
		pointBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.map_long_touch);
	}

	private static final int RADIUS = 240;
	private float itemDegrees = 60;// 每个间隔是45度
	private int startDegrees = -120;// 开始角度
	private float downX, downY;
	private boolean isAnimFinash;

	public void show() {
		show(downX, downY);
	}

	public void addItem(SatelliteIitem item) {
		mList.add(item);
	}

	public void show(float x, float y) {
		show(x, y, true);
	}

	public void show(float x, float y, boolean isClearLociton) {
		for (int i = 0; i < mList.size(); i++) {
			SatelliteIitem item = mList.get(i);
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

	private boolean isDraw;

	@Override
	protected void onDraw(Canvas canvas) {
		if (!isDraw)
			return;
		canvas.drawColor(0x99000000);
		canvas.drawBitmap(pointBitmap, downX - pointBitmap.getWidth() / 2,
				downY - pointBitmap.getHeight() / 2, null);
		for (int i = 0; i < mList.size(); i++) {
			SatelliteIitem item = mList.get(i);
			Bitmap bitmap;
			if (item.isSelect()) {
				bitmap = item.getSelectedBtimap();
			} else {
				bitmap = item.getNomorlBitmap();
			}
			canvas.drawBitmap(bitmap, item.getX() - bitmap.getWidth() / 2,
					item.getY() - bitmap.getHeight() / 2, null);
			float textWidth = mPaint.measureText(item.getName() + "");

			float textleft = item.getX() - textWidth / 2;
			float textTop = item.getY() - bitmap.getHeight() / 2 - 20;
			float textright = item.getX() + textWidth / 2;
			float textBottom = item.getY() - bitmap.getHeight() / 2
					+ mPaint.getTextSize() - 20;

			mPaint.setColor(0xff000000);
			canvas.drawRect(textleft - 5, textTop - mPaint.getTextSize() - 5,
					textright + 5, textBottom - mPaint.getTextSize() + 5,
					mPaint);
			canvas.drawCircle(textleft - 5, textBottom - mPaint.getTextSize()
					- mPaint.getTextSize() / 2, mPaint.getTextSize() / 2 + 5,
					mPaint);
			canvas.drawCircle(textright + 5, textBottom - mPaint.getTextSize()
					- mPaint.getTextSize() / 2, mPaint.getTextSize() / 2 + 5,
					mPaint);
			mPaint.setColor(0xffffffff);
			canvas.drawText(item.getName() + "", textleft, textTop, mPaint);
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			DTLog.i("长按啦");
			if (Math.abs(moveX - downX) < 30 && Math.abs(moveY - downY) < 30) {
				isDraw = true;
				show(downX, downY);
			}
		};
	};

	private float moveX, moveY;
	private boolean noClick = false;
	public void setNoLong(boolean noClick) {
		this.noClick = noClick;
	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (event.getPointerCount() == 1 && !noClick) {
			invalidate();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				DTLog.e("down...");
				downX = event.getX();
				downY = event.getY();
				for (int i = 0; i < mList.size(); i++) {
					mList.get(i).setSelect(false);
				}
				mHandler.sendEmptyMessageDelayed(1, 1000);
				break;
			case MotionEvent.ACTION_MOVE:
				moveX = event.getX();
				moveY = event.getY();
				if (isAnimFinash) {
					float x = event.getX();
					float y = event.getY();
					for (int i = 0; i < mList.size(); i++) {
						SatelliteIitem item = mList.get(i);
						float degree = (float) Math.toDegrees(Math
								.atan((x - downX) / (y - downY)));
						degree = getDegrees(x, y, downX, downY, degree);
						float dis = ((degree - item.getDegrees()) + 720) % 360;// 如果差值大于360-22.5或者小于22.5
						DTLog.i("触摸点的角度为：" + degree + "      显示点角度为："
								+ item.getDegrees());
						if (dis > 360 - itemDegrees / 2
								|| dis < itemDegrees / 2) {
							item.setSelect(true);
						} else {
							item.setSelect(false);
						}
					}
				}
				if (isDraw) {
					return true;
				}
				break;
			case MotionEvent.ACTION_UP:
				isDraw = false;
				mHandler.removeMessages(1);
				break;
			}
		}else{
			isDraw = false;
			mHandler.removeMessages(1);
		}
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
			degree = 270 - degree;
		} else if (x > centerX && y < centerY) {
			degree = 270 - degree;
		}
		return (degree + 720) % 360;
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		invalidate();
	}

}
