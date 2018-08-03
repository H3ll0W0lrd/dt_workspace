package com.rtmap.locationdemo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

import com.rtm.frm.map.BaseMapLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;

public class ExampleLayer implements BaseMapLayer {

    MapView mapView;
    Location location;

    public ExampleLayer(MapView mapView) {
        initLayer(mapView);
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
                && mapView.getBuildId().equals(location.getBuildId())) {// 
            PointInfo point = mapView.fromLocation(location);
            Bitmap bitmap = null;// 
            c.drawBitmap(bitmap, point.getX(), point.getY(), new Paint());
        }

    }

    @Override
    public boolean onTap(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public void setLocation(float x, float y, String floor, String buildid) {
        this.location = new Location(x, y, floor);

        location.setBuildId(buildid);
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clearLayer() {
		// TODO Auto-generated method stub
		
	}

}
