package com.rtmap.wisdom.layer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.rtm.common.model.POI;
import com.rtm.frm.map.BaseMapLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.model.PointInfo;
import com.rtmap.wisdom.R;
import com.rtmap.wisdom.util.DTUIUtil;

public class CheLayer implements BaseMapLayer {

	private POI mPoi;
	private MapView mapview;
	private Bitmap cheBitmap;

	public CheLayer(MapView mapview) {
		this.mapview = mapview;
		cheBitmap = BitmapFactory.decodeResource(DTUIUtil.getResources(),
				R.drawable.che);
	}

	@Override
	public void initLayer(MapView view) {

	}

	public void setChe(POI mPoi) {
		this.mPoi = mPoi;
	}

	public POI getChe() {
		return mPoi;
	}

	@Override
	public boolean onTap(MotionEvent e) {
		return false;
	}

	@Override
	public void destroyLayer() {
		mPoi = null;
	}

	@Override
	public boolean hasData() {
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	public PointInfo getCenter() {
		int x = mapview.getWidth() / 2 + mapview.getLeft();
		int y = mapview.getHeight() / 2 + mapview.getTop();
		PointInfo point = new PointInfo(x - cheBitmap.getWidth() / 2.0f, y
				- cheBitmap.getHeight() / 2.0f);
		return point;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (mPoi == null) {
			int x = mapview.getWidth() / 2 + mapview.getLeft();
			int y = mapview.getHeight() / 2 + mapview.getTop();
			canvas.drawBitmap(cheBitmap, x - cheBitmap.getWidth() / 2.0f, y
					- cheBitmap.getHeight() / 2.0f, null);
		} else {
			if (mPoi.getBuildId().equals(mapview.getBuildId())
					&& mPoi.getFloor().equals(mapview.getFloor())) {
				PointInfo point = mapview
.fromLocation(mPoi.getX(),
						mPoi.getY_abs());
				canvas.drawBitmap(cheBitmap,
						point.getX() - cheBitmap.getWidth() / 2.0f,
						point.getY() - cheBitmap.getHeight() / 2.0f, null);
			}
		}

	}

	@Override
	public void clearLayer() {

	}

}
