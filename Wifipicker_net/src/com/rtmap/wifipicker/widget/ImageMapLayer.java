package com.rtmap.wifipicker.widget;

import java.io.InputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;

import com.rtm.frm.map.BaseMapLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtmap.wifipicker.util.ImgUtil;

public class ImageMapLayer implements BaseMapLayer {
    private int x;
    private int y;
    private boolean mVisiable = false;
    private Bitmap mBitmap;
    private Paint mPaint;
    MapView mapView;
    Location location;

    public ImageMapLayer(MapView v, int resId) {
        Resources res = v.getResources();
        InputStream isBmp = res.openRawResource(resId);
        mBitmap = BitmapFactory.decodeStream(isBmp);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(10);
        initLayer(mapView);
    }
    
    public ImageMapLayer(MapView v, String filePath) {
        mBitmap = ImgUtil.getBitmap(filePath);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(10);
        initLayer(mapView);
    }


    @Override
    public void clearLayer() {
        // TODO Auto-generated method stub

    }

    @Override
    public void destroyLayer() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean hasData() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void initLayer(MapView mapView) {
        this.mapView = mapView;

    }

    @Override
    public void onDraw(Canvas c) {
        if (location != null && mapView.getFloor().equals(location.getFloor())
                && mapView.getBuildId().equals(location.getBuildId())) {// 如果定位点的楼层和建筑物id和当前的地图的楼层和建筑物id一致
            PointInfo point = mapView.fromLocation(location);
            c.drawBitmap(mBitmap, point.getX(), point.getY(), new Paint());
        }

    }

    @Override
    public boolean onTap(MotionEvent arg0) {
        return false;
    }

    public void setLocation(float x, float y, String floor, String buildid) {
        this.location = new Location(x, y, floor);

        location.setBuildId(buildid);
    }

	public float getX() {
		return mapView.fromLocation(location).getX();
	}
	public float getY(){
		return mapView.fromLocation(location).getY();
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		return false;
	}
}
