package com.rtm.frm.drawmap;

import com.rtm.frm.map.MapView;

import android.os.SystemClock;
import android.view.animation.DecelerateInterpolator;

/**
 * @ClassName: AnimateDraggingMapThread
 * @Description: 动画类
 * @author Comsys-caoyy
 * @date 2013-5-14 上午10:11:26
 *
 */
/**
 * @ClassName: AnimateDraggingMapThread
 * @Description: TODO
 * @author Comsys-caoyy
 * @date 2013-5-14 上午10:11:54
 *
 */
/**
 * @ClassName: AnimateDraggingMapThread
 * @Description: TODO
 * @author Comsys-caoyy
 * @date 2013-5-14 上午10:11:57
 *
 */

// 肖依残留类 视情况抛弃
public class AnimateDraggingMapThread {
	private final static float DRAGGING_ANIMATION_TIME = 600f;
	private final static int DEFAULT_SLEEP_TO_REDRAW = 30;

	private volatile boolean stopped; // NOPMD by caoyy on 13-5-13 ����3:10
	private volatile Thread currentThread = null; // NOPMD by caoyy on 13-5-13
													// ����3:10
	private MapView mapView;

	private double targetLatitude = 0;
	private double targetLongitude = 0;
	private int targetZoom = 0;

	/**
	 * 
	 * 创建一个新的实例 AnimateDraggingMapThread.
	 * <p>
	 * Title:
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param view
	 */
	public AnimateDraggingMapThread(MapView view) {
		mapView = view;
	}

	public void stopAnimating() {
		stopped = true;
	}

	public boolean isAnimating() {
		return currentThread != null && !stopped;
	}

	private void clearTargetValues() {
		targetZoom = 0;
	}

	public int getTargetZoom() {
		return targetZoom;
	}

	public double getTargetLatitude() {
		return targetLatitude;
	}

	public double getTargetLongitude() {
		return targetLongitude;
	}

	public void startMoving(final float endX, final float endY) {
		final float animationTime = DRAGGING_ANIMATION_TIME;
		clearTargetValues();
		startThreadAnimating(new Runnable() {
			@Override
			public void run() {
				float curX = endX;
				float curY = endY;
				DecelerateInterpolator interpolator = new DecelerateInterpolator(
						1);

				long timeMillis = SystemClock.uptimeMillis();
				float normalizedTime = 0f;
				float prevNormalizedTime = 0f;
				while (!stopped) {
					normalizedTime = (SystemClock.uptimeMillis() - timeMillis)
							/ animationTime;
					if (normalizedTime >= 1f) {
						break;
					}
					float interpolation = interpolator
							.getInterpolation(normalizedTime);

					float newX = (1 - interpolation)
							* (normalizedTime - prevNormalizedTime) + curX;
					float newY = (1 - interpolation)
							* (normalizedTime - prevNormalizedTime) + curY;
					mapView.dragToAnimate(curX, curY, newX, newY);
					curX = newX;
					curY = newY;
					prevNormalizedTime = normalizedTime;
					/*
					 * try { Thread.sleep(DEFAULT_SLEEP_TO_REDRAW); } catch
					 * (InterruptedException e) { stopped = true; }
					 */
				}
			}
		}); //$NON-NLS-1$
	}

	public void startDragging(final float velocityX, final float velocityY,
			float startX, float startY, final float endX, final float endY) {
		final float animationTime = DRAGGING_ANIMATION_TIME;
		clearTargetValues();
		startThreadAnimating(new Runnable() {
			@Override
			public void run() {
				float curX = endX;
				float curY = endY;
				DecelerateInterpolator interpolator = new DecelerateInterpolator(
						1);

				long timeMillis = SystemClock.uptimeMillis();
				float normalizedTime = 0f;
				float prevNormalizedTime = 0f;
				while (!stopped) {
					normalizedTime = (SystemClock.uptimeMillis() - timeMillis)
							/ animationTime;
					if (normalizedTime >= 1f) {
						break;
					}
					float interpolation = interpolator
							.getInterpolation(normalizedTime);

					float newX = velocityX * (1 - interpolation)
							* (normalizedTime - prevNormalizedTime) + curX;
					float newY = velocityY * (1 - interpolation)
							* (normalizedTime - prevNormalizedTime) + curY;

					mapView.dragToAnimate(curX, curY, newX, newY);
					curX = newX;
					curY = newY;
					prevNormalizedTime = normalizedTime;
					try {
						Thread.sleep(DEFAULT_SLEEP_TO_REDRAW);
					} catch (InterruptedException e) {
						stopped = true;
					}
				}
				mapView.setGetLabels(true);
				mapView.refreshMap();
			}
		}); //$NON-NLS-1$
	}

	public void startThreadAnimating(final Runnable runnable) {
		stopAnimatingSync();
		stopped = false;
		currentThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					runnable.run();
				} finally {
					currentThread = null;
				}
			}
		}, "Animating Thread");
		currentThread.start();

	}

	public void stopAnimatingSync() {
		// wait until current thread != null
		stopped = true;
		while (currentThread != null) {
			try {
				currentThread.join();
			} catch (InterruptedException e) {
			}
		}
	}

}
