package com.rtmap.wisdom.layer;

import java.util.ArrayList;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.rtm.common.model.POI;
import com.rtm.frm.map.BaseMapLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;

public class FrameAnimationLayer implements BaseMapLayer,
		AnimatorUpdateListener {

	private POI mLoc;
	private ArrayList<Bitmap> mBitmapList;
	private int times;
	private ValueAnimator mAnimator;
	private MapView mapview;
	private boolean isRefresh;
	private OnFreshListener listener;
	
	public void setListener(OnFreshListener listener) {
		this.listener = listener;
	}

	public FrameAnimationLayer(MapView mapview) {
		this.mapview = mapview;
		mBitmapList = new ArrayList<Bitmap>();
		mAnimator = ValueAnimator.ofInt(1, 20);
		mAnimator.setDuration(1000);
		mAnimator.setRepeatCount(ValueAnimator.INFINITE);
		mAnimator.addUpdateListener(this);
		mAnimator.start();
	}

	@Override
	public void initLayer(MapView view) {

	}
	public void addBitmapList(POI location, ArrayList<Bitmap> imageList) {
		destroyLayer();
		if (location != null && imageList != null && imageList.size() > 0) {
			if (imageList.size() > 1) {
				isRefresh = true;
			}
			mLoc = location;
			mBitmapList.addAll(imageList);
		}
	}

	public void addBitmap(POI location, Bitmap bitmap) {
		destroyLayer();
		if (location != null && bitmap != null) {
			isRefresh = false;
			mLoc = location;
			mBitmapList.add(bitmap);
		}
	}

	@Override
	public boolean onTap(MotionEvent e) {
		return false;
	}
	
	public POI getLocation() {
		return mLoc;
	}

	@Override
	public void destroyLayer() {
		if(listener!=null){
			listener.onFresh();
		}
		mLoc = null;
		mBitmapList.clear();
		times = 0;
		isRefresh = false;
	}

	@Override
	public boolean hasData() {
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (mLoc != null && mLoc.getBuildId().equals(mapview.getBuildId())
				&& mLoc.getFloor().equals(mapview.getFloor())) {
			PointInfo point = mapview
					.fromLocation(mLoc.getX(), mLoc.getY_abs());
			int index = times % mBitmapList.size();
			Bitmap bitmap = mBitmapList.get(index);
			if (mBitmapList.size() == 1) {
				canvas.drawBitmap(bitmap, point.getX() - bitmap.getWidth()
						/ 2.0f, point.getY() - bitmap.getHeight()/2, null);
			} else {
				canvas.drawBitmap(bitmap, point.getX() - bitmap.getWidth()
						/ 2.0f, point.getY() - bitmap.getHeight(), null);
			}
		}
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		if (isRefresh) {
			times++;
			mapview.refreshMap();
		}
	}

	@Override
	public void clearLayer() {

	}

	public interface OnFreshListener{
		void onFresh();
	}
	
}
