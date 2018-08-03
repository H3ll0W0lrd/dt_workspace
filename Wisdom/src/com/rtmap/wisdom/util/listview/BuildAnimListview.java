package com.rtmap.wisdom.util.listview;

import com.rtmap.wisdom.R;
import com.rtmap.wisdom.model.Point;
import com.rtmap.wisdom.util.DTLog;
import com.rtmap.wisdom.util.view.DTSatelliteLayout;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;
import android.widget.AbsListView.LayoutParams;

public class BuildAnimListview extends ListView {

	private DTSatelliteLayout mStatelliteLayout;

	public BuildAnimListview(Context context) {
		super(context);
	}

	public BuildAnimListview(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BuildAnimListview(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	private float y;
	private boolean isRunAnim, isMove;
	private OnAnimEndListener listener;

	public void reset() {
		isRunAnim = false;
		isMove = false;
		open = true;
	}

	private int mPosition;

	public void setOnAnimEndListener(OnAnimEndListener listener) {
		this.listener = listener;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		final int actionMasked = ev.getActionMasked() & MotionEvent.ACTION_MASK;

		if (actionMasked == MotionEvent.ACTION_DOWN) {
			// 记录手指按下时的位置
			mPosition = pointToPosition((int) ev.getX(), (int) ev.getY());
			y = ev.getY();
			return super.dispatchTouchEvent(ev);
		}

		if (actionMasked == MotionEvent.ACTION_MOVE) {
			// 最关键的地方，忽略MOVE 事件
			// ListView onTouch获取不到MOVE事件所以不会发生滚动处理
			if (ev.getY() - y < -10 && !isRunAnim) {
				startAnim();
			}
			if (!isMove) {
				setPressed(false);
				return true;
			}
		}

		// 手指抬起时
		if ((actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL)
				&& !isMove) {
			// 手指按下与抬起都在同一个视图内，交给父控件处理，这是一个点击事件
			if (ev.getY() - y >= -10 && ev.getY() - y < 10) {// 由于不能滚动，所以一旦抬起手指就会识别为点击，要做处理
				return super.dispatchTouchEvent(ev);
			} else {
				// 如果手指已经移出按下时的Item，说明是滚动行为，清理Item pressed状态
				setPressed(false);
				invalidate();
				return true;
				// super.dispatchTouchEvent(ev);
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * 是否运行过动画
	 * 
	 * @param isRunAnim
	 */
	public void setRunAnim(boolean isRunAnim) {
		this.isRunAnim = isRunAnim;
	}

	public void startAnim() {
		if (!open) {// 关闭状态
			return;
		}
		isRunAnim = true;
		final Point keypoint = new Point();
		keypoint.setY(mStatelliteLayout.getHeight() + 1);
		ValueAnimator anim = ObjectAnimator.ofInt(keypoint, "y",
				keypoint.getY(), 0);
		anim.setDuration(1000);
		anim.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				DTLog.i("  " + keypoint.getY());
				LayoutParams p = (LayoutParams) mStatelliteLayout
						.getLayoutParams();
				p.height = keypoint.getY();
				mStatelliteLayout.setLayoutParams(p);
			}
		});
		anim.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				isMove = true;
				LayoutParams p = (LayoutParams) mStatelliteLayout
						.getLayoutParams();
				p.height = 0;
				mStatelliteLayout.setLayoutParams(p);
				open = false;
				if (listener != null) {
					listener.onAnimEnd();
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}
		});
		anim.start();
	}

	public void endAnim(final AnimatorUpdateListener updateistener,
			final AnimatorListener animListener, boolean isReset) {
		if (isReset) {
			reset();
		}
		final Point keypoint = new Point();
		ValueAnimator anim = ObjectAnimator.ofInt(
				keypoint,
				"y",
				keypoint.getY(),
				getResources().getDimensionPixelSize(
						R.dimen.build_statellite_height));
		anim.setDuration(1000);
		anim.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				LayoutParams p = (LayoutParams) mStatelliteLayout
						.getLayoutParams();
				p.height = keypoint.getY();
				mStatelliteLayout.setLayoutParams(p);
				if (updateistener != null) {
					updateistener.onAnimationUpdate(animation);
				}

			}
		});
		anim.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				if (animListener != null) {
					animListener.onAnimationStart(animation);
				}
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				if (animListener != null) {
					animListener.onAnimationRepeat(animation);
				}
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (animListener != null) {
					animListener.onAnimationEnd(animation);
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				if (animListener != null) {
					animListener.onAnimationCancel(animation);
				}
			}
		});
		anim.start();
	}

	public void setStatelliteView(DTSatelliteLayout statelliteLayout) {
		mStatelliteLayout = statelliteLayout;
	}

	public interface OnAnimEndListener {
		void onAnimEnd();
	}

	private boolean open = true;

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}
}
