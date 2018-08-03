package com.rtm.frm.vmap;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Paint;

import com.rtm.common.style.DrawStyle;
import com.rtm.common.utils.Constants;
import com.rtm.common.utils.RMStringUtils;

public class Shape {
	public int mId;
	public int[] mPoints;
	/**
	 * 1：机场公共区；2：机场控制区；3：无效区、天井；4：未知区域，指可看到，但不可进入的办公区、闲置区等；5：各种店铺、服务场所；6：卫生间、
	 * 母婴室等；7：楼梯、电梯、自动扶梯、水平步道等；8全楼层外框；12：出入口，公用电话、ATM等；
	 */
	public int mStyle;
	public int minx;
	public int miny;
	public int maxx;
	public int maxy;
	public String mName;
	public int mLevel;
	public Coord mCenter;
	public Coord mDisplayCenter;
	public int mLabelDisplayWidth;
	public int mLabelDisplayHeight;
	// public int iconid;
	public String mDrawname;
	public boolean point;
	public double xtr, ytr, wtr, xtl, ytl, wtl;
	public DrawStyle mDrawStyle;
	public int mType;

	public boolean isNull() {
		if (maxx == 0 && maxy == 0 && minx == 0 && miny == 0)
			return true;
		return maxx < minx || maxy < miny;
	}

	// 求任意简单多边形polygon的重心

	void AddPosPart(double x, double y, double w) {
		if (Math.abs(wtr + w) < 1e-10)
			return; // detect zero regions
		xtr = (wtr * xtr + w * x) / (wtr + w);
		ytr = (wtr * ytr + w * y) / (wtr + w);
		wtr = w + wtr;
		return;
	}

	void AddNegPart(double x, double y, double w) {
		if (Math.abs(wtl + w) < 1e-10)
			return;

		xtl = (wtl * xtl + w * x) / (wtl + w);
		ytl = (wtl * ytl + w * y) / (wtl + w);
		wtl = w + wtl;
		return;
	}

	void AddRegion(double x1, double y1, double x2, double y2) {
		if (Math.abs(x1 - x2) < 1e-10)
			return;

		if (x2 > x1) {
			AddPosPart((x2 + x1) / 2, y1 / 2, (x2 - x1) * y1);
			AddPosPart((x1 + x2 + x2) / 3, (y1 + y1 + y2) / 3, (x2 - x1)
					* (y2 - y1) / 2);
		} else {
			AddNegPart((x2 + x1) / 2, y1 / 2, (x2 - x1) * y1);
			AddNegPart((x1 + x2 + x2) / 3, (y1 + y1 + y2) / 3, (x2 - x1)
					* (y2 - y1) / 2);
		}
	}

	Coord cg_simple(Coord[] mCoords) {
		Coord p1, p2, tp = null;
		xtr = ytr = wtr = 0.0;
		xtl = ytl = wtl = 0.0;
		for (int i = 0; i < mPoints.length - 1; i++) {
			p1 = new Coord(mCoords[mPoints[i]].mX, mCoords[mPoints[i]].mY);
			p2 = new Coord(mCoords[mPoints[(i + 1) % (mPoints.length)]].mX,
					mCoords[mPoints[(i + 1) % (mPoints.length)]].mY);
			AddRegion(p1.mX, p1.mY, p2.mX, p2.mY); // 全局变量变化处
		}
		tp = new Coord((int) ((wtr * xtr + wtl * xtl) / (wtr + wtl)),
				(int) ((wtr * ytr + wtl * ytl) / (wtr + wtl)));

		return tp;
	}

	public boolean Intersect(float xmin, float xmax, float ymin, float ymax) {

		float minx = Math.max(this.minx, xmin);
		float miny = Math.max(this.miny, ymin);
		float maxx = Math.min(this.maxx, xmax);
		float maxy = Math.min(this.maxy, ymax);
		return (minx < maxx || miny < maxy);
	}

	public boolean Intersect(Shape mshape) {
		int minx = Math.max(this.minx, mshape.minx);
		int miny = Math.max(this.miny, mshape.miny);
		int maxx = Math.min(this.maxx, mshape.maxx);
		int maxy = Math.min(this.maxy, mshape.maxy);
		return (minx < maxx || miny < maxy);
	}

	public void setbound(Coord[] mCoords) {
		this.minx = mCoords[this.mPoints[0]].mX;
		this.maxx = mCoords[this.mPoints[0]].mX;
		this.miny = mCoords[this.mPoints[0]].mY;
		this.maxy = mCoords[this.mPoints[0]].mY;
		for (int j = 1; j < this.mPoints.length; j++) {
			minx = Math.min(mCoords[this.mPoints[j]].mX, minx);
			miny = Math.min(mCoords[this.mPoints[j]].mY, miny);
			maxx = Math.max(mCoords[this.mPoints[j]].mX, maxx);
			maxy = Math.max(mCoords[this.mPoints[j]].mY, maxy);
		}
	}

	public void setwidth(Paint mPaint) {
		int mLabelWidth = 0,mLabelHeight=0;
		if (!RMStringUtils.isEmpty(mName)) {
			if (mLabelWidth == 0) {
				mLabelWidth = (int) (mPaint.measureText(mName));
				mLabelHeight = (int) mPaint.getTextSize();
			}

//			if (mStyle == 11) {
//				mLabelWidth = (int) (mPaint.measureText(mName.substring(0,
//						mName.length() - 3)));
//			}
			mLabelDisplayWidth = (int) (mLabelWidth * Constants.RATIO);
			mLabelDisplayHeight = (int) (mLabelHeight * Constants.RATIO);
		}
	}

	public boolean Intersectbycenter(Shape mshape, double ratioX,
			double ratioY, double angle) {
		int x_distance;
		int y_distance;
		if (angle == 0) {
			x_distance = Math.abs(mDisplayCenter.mX - mshape.mDisplayCenter.mX);
			y_distance = Math.abs(mDisplayCenter.mY - mshape.mDisplayCenter.mY);
		} else {
			x_distance = (int) Math
					.abs(mDisplayCenter.mX
							* Math.cos(-angle)
							+ (mDisplayCenter.mY * Math.sin(-angle) - (mshape.mDisplayCenter.mX
									* Math.cos(-angle) + (mshape.mDisplayCenter.mY * Math
									.sin(-angle)))));
			y_distance = (int) (Math
					.abs(mDisplayCenter.mY
							* Math.cos(-angle)
							- mDisplayCenter.mX
							* Math.sin(-angle)
							- (mshape.mDisplayCenter.mY * Math.cos(-angle) - mshape.mDisplayCenter.mX
									* Math.sin(-angle))));
		}

		return (x_distance < (mLabelDisplayWidth + mshape.mLabelDisplayWidth)
				* Constants.POI_DENSITY * ratioX / 2 && y_distance < (mLabelDisplayHeight + mshape.mLabelDisplayHeight)
				* Constants.POI_DENSITY * ratioY / 2);
	}

	public boolean LabelOverRange(double ratioX) {
		if (isNull()) {
			return false;
		}
		return (mLabelDisplayWidth * ratioX > (maxx - minx));
	}

}
