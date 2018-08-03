package com.rtm.frm.map;

import java.lang.reflect.Method;

import android.content.Context;
import android.graphics.PointF;
import android.view.MotionEvent;

import com.rtm.common.utils.Constants;
import com.rtm.frm.utils.RMathUtils;

public class MultiTouchSupport {

	private float oldRotation = 0;

	public interface MultiTouchZoomListener {

		public void onZoomStarted(float distance, PointF centerPoint);

		public void onZooming(float distance, float relativeToStart,
				double radians);

		public void onZoomEnded(float distance, float relativeToStart);

		public void onGestureInit(float x1, float y1, float x2, float y2);

	}

	private boolean multiTouchAPISupported = false;
	private final MultiTouchZoomListener listener;
	protected final Context ctx;

	protected Method getPointerCount;
	protected Method getX;
	protected Method getY;
	protected Method getPointerId;

	public MultiTouchSupport(Context ctx, MultiTouchZoomListener listener) {
		this.ctx = ctx;
		this.listener = listener;
		initMethods();
	}

	public boolean isMultiTouchSupported() {
		return multiTouchAPISupported;
	}

	public boolean isInZoomMode() {
		return inZoomMode;
	}

	private float rotation(MotionEvent event) {
		double delta_x = (event.getX(0) - event.getX(1));
		double delta_y = (event.getY(0) - event.getY(1));
		double radians = Math.atan2(delta_y, delta_x);
		return (float) radians;
	}

	private void initMethods() {
		try {
			getPointerCount = MotionEvent.class.getMethod("getPointerCount"); //$NON-NLS-1$
			getPointerId = MotionEvent.class.getMethod(
					"getPointerId", Integer.TYPE); //$NON-NLS-1$
			getX = MotionEvent.class.getMethod("getX", Integer.TYPE); //$NON-NLS-1$
			getY = MotionEvent.class.getMethod("getY", Integer.TYPE); //$NON-NLS-1$
			multiTouchAPISupported = true;
		} catch (Exception e) {
			multiTouchAPISupported = false;
		}
	}

	private int inRotateMode = 0;
	private boolean inZoomMode = false;
	private float zoomStartedDistance = 100;
	private float previousZoom = 1;
	private PointF centerPoint = new PointF();
	PointF pA = new PointF();
	PointF pB = new PointF();

	public boolean onTouchEvent(MotionEvent event) {
		if (!isMultiTouchSupported()) {
			return false;
		}
		int actionCode = event.getAction() & Constants.ACTION_MASK;
		try {
			Integer pointCount = (Integer) getPointerCount.invoke(event);
			if (actionCode == Constants.ACTION_DOWN) {
				inZoomMode = false;

			}
			if (pointCount < 2) {
				if (inZoomMode) {
					listener.onZoomEnded(zoomStartedDistance * previousZoom,
							previousZoom);
					return true;
				}
				return false;
			}
			Float x1 = (Float) getX.invoke(event, 0);
			Float x2 = (Float) getX.invoke(event, 1);
			Float y1 = (Float) getY.invoke(event, 0);
			Float y2 = (Float) getY.invoke(event, 1);
			float distance = RMathUtils.distance(x1, y1, x2, y2);
			previousZoom = distance / zoomStartedDistance;
			if (actionCode == Constants.ACTION_POINTER_DOWN) {

				pA.set(event.getX(0), event.getY(0));
				pB.set(event.getX(1), event.getY(1));
				centerPoint = new PointF((x1 + x2) / 2, (y1 + y2) / 2);
				listener.onGestureInit(x1, y1, x2, y2);
				listener.onZoomStarted(distance, centerPoint);
				zoomStartedDistance = distance;
				oldRotation = rotation(event);
				inZoomMode = true;
				inRotateMode = 0;
				return true;
			} else if (inZoomMode && actionCode == MotionEvent.ACTION_MOVE) {

				float rotation = rotation(event) - oldRotation;
				if (Constants.ROTATE) {
					if (inRotateMode == 0) {
						PointF pC = new PointF(event.getX(1) - event.getX(0)
								+ pA.x, event.getY(1) - event.getY(0) + pA.y);
						double a = RMathUtils.distance(pB.x, pB.y, pC.x, pC.y);
						double b = RMathUtils.distance(pA.x, pA.y, pC.x, pC.y);
						double c = RMathUtils.distance(pA.x, pA.y, pB.x, pB.y);
						if (a >= 10) {
							double cosB = (a * a + c * c - b * b) / (2 * a * c);
							double angleB = Math.acos(cosB);
							double PID4 = Math.PI / 4;
							if (angleB > PID4 && angleB < 3 * PID4) {
								inRotateMode = 2;// 旋转

							} else {
								inRotateMode = 1;// 缩放
							}
						}
					}
				} else {
					inRotateMode = 1;// 缩放
				}

				/*
				 * if (Math.abs(previousZoom-1f)>0.01f||rotation>0.01f) { if
				 * (inRotateMode==0){ if(Math.abs(previousZoom-1f)<0.018f) {
				 * inRotateMode=2; } else { inRotateMode=1; } } }
				 */

				if (inRotateMode == 1) {
					listener.onZooming(distance, previousZoom, 0);
				}
				if (inRotateMode == 2) {
					listener.onZooming(distance, 1, rotation);
				}

				return true;
			}
		} catch (Exception e) {
			// Log.log(e);
		}
		return false;
	}
}
