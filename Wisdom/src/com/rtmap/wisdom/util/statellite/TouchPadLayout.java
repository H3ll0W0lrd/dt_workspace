package com.rtmap.wisdom.util.statellite;

import com.rtm.frm.map.MapView;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtmap.wisdom.util.DTLog;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by rtmap on 2016/12/2.
 */

public class TouchPadLayout extends FrameLayout {

	private MapView mMapView;

	public TouchPadLayout(Context context) {
		this(context, null);
	}

	public TouchPadLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TouchPadLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	private PointF pointF = new PointF();
	private boolean isLongClick = false;
	private boolean noClick = false;
	public void setNoLong(boolean noClick) {
		this.noClick = noClick;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
//		DTLog.i("触点个数："+event.getPointerCount());
		if (event.getPointerCount() == 1&&!noClick) {
			if (isLongClick) {
				getChildAt(1).dispatchTouchEvent(event);
				getChildAt(2).dispatchTouchEvent(event);
			}

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				pointF.set(event.getX(), event.getY());
				postDelayed(mLongClickRunnable, 800);// 如果2s后能执行，长按线程，则标识为长按
				break;
			case MotionEvent.ACTION_MOVE:
				// 处理长按抖动
				if (Math.abs(event.getY() - pointF.y) > 50
						|| Math.abs(event.getX() - pointF.x) > 50||event.getPointerCount()>1)
					removeCallbacks(mLongClickRunnable);
				break;
			case MotionEvent.ACTION_UP:
				removeCallbacks(mLongClickRunnable);
				((FanView) getChildAt(1)).setDraw(false);
				((FanView) getChildAt(2)).setDraw(false);
				isLongClick = false;
				break;
			}
		}else{
			removeCallbacks(mLongClickRunnable);
			((FanView) getChildAt(1)).setDraw(false);
			((FanView) getChildAt(2)).setDraw(false);
			isLongClick = false;
		}
		if(isLongClick){
			return true;
		}
		return super.dispatchTouchEvent(event);
	}

	private LongClickRunnable mLongClickRunnable = new LongClickRunnable();

	private class LongClickRunnable implements Runnable {

		public void run() {
			isLongClick = true;
			Location myLoc = mMapView.getMyCurrentLocation();
			if (myLoc != null
					&& myLoc.getBuildId().equals(mMapView.getBuildId())
					&& myLoc.getFloor().equals(mMapView.getFloor())) {
				PointInfo mypoint = mMapView.fromLocation(myLoc);// 将定位点屏幕坐标转化出来
				float disx = Math.abs(pointF.x - mypoint.getX());// x轴差距
				float disy = Math.abs(pointF.y - mypoint.getY());// y轴差距
				if (disx < 40 && disy < 40) {// 点击点和位置点间距小于40像素
					((FanView) getChildAt(1)).setDraw(true);
				}
			} else {
				((FanView) getChildAt(2)).setDraw(true);
			}

		}
	}

	public void setMapView(MapView mMapView) {
		this.mMapView = mMapView;
	}
}
