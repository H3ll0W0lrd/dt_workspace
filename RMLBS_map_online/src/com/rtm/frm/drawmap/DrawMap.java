package com.rtm.frm.drawmap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;

import com.rtm.common.model.POI;
import com.rtm.common.style.DrawStyle;
import com.rtm.common.utils.Constants;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtm.frm.vmap.Coord;
import com.rtm.frm.vmap.Layer;
import com.rtm.frm.vmap.Shape;

/**
 * @ClassName: DrawMap
 * @Description: 绘制矢量地图
 * @author caoyy
 * @date 2013-5-14 上午10:16:20
 *
 */
public class DrawMap {
	// 绘制地图用画笔
	private Paint mPaint = null;
	// 绘制注记用画笔
	private Paint mLabelPaint = null;
	// 矢量数据
	private Layer mLayer;// 图层
	private float angle;

	// 填色方案
	private DrawStyle mStyle;
	// 绘制注记id集合
	private HashSet<Integer> drawLabelsId;

	private float mWidth;
	private float mHeight;

	private int xmin = 0;
	private int ymin = 0;
	private int xmax = 0;
	private int ymax = 0;

	private float mcenterX;
	private float mcenterY;
	private float mscale = 0;
	private float buildwidth;
	private float buildheight;
	int radius = 0;
	// private int alpha = 255;
	private POI selectPoi;
	private MapView mMapView;

	public DrawMap(MapView mapview) {
		mMapView = mapview;
		mPaint = new Paint();
		mPaint.setTextSize(MapView.MAPTEXT.getTextsize());
		mLabelPaint = new Paint();
		mLabelPaint.setAntiAlias(true);
		mLabelPaint.setFilterBitmap(true);
		mLabelPaint.setDither(true);
		mLabelPaint.setStrokeWidth(0);
		mLabelPaint.setAntiAlias(true);
		mLabelPaint.setTextAlign(Align.CENTER);
	}

	public void init(float centerX, float centerY, float scale, int width,
			int height, POI SelectPoi) {
		setSelectPoi(SelectPoi);
		if (mcenterX == (int) (centerX * 1000f)
				&& mcenterY == (int) (centerY * 1000f)
				&& mscale == scale * 1000f) {
			return;
		}
		mcenterX = (int) (centerX * 1000f);
		mcenterY = (int) (centerY * 1000f);

		mWidth = width * Constants.buffer;
		mHeight = height * Constants.buffer;// 1127
		xmin = (int) (mcenterX - mWidth * scale * 500f);
		ymin = (int) (mcenterY - mHeight * scale * 500f);
		xmax = (int) (mcenterX + mWidth * scale * 500f);
		ymax = (int) (mcenterY + mHeight * scale * 500f);

		if (mscale == 0 || mscale != scale * 1000f) {
			mscale = scale * 1000f;
		}

	}

	public void drawShape(Canvas canvas) {
		Rect mRect = new Rect((int) xmin, (int) ymax, (int) xmax, (int) ymin);
		drawShape(canvas, mRect);
	}

	public int getX(int i, int j) {
		return mLayer.coords[mLayer.shapes[i].mPoints[j]].mX;
	}

	public int getY(int i, int j) {
		return mLayer.coords[mLayer.shapes[i].mPoints[j]].mY;
	}

	private float getScrX(float CoordX) {
		return (((CoordX - xmin) / mscale));
	}

	private float getScrY(float CoordY) {
		return ((CoordY - ymin) / mscale);
	}

	private PointInfo fromLocation(Location mLocation) {
		float x1 = getScrX(mLocation.getX());
		float y1 = getScrY(mLocation.getY());
		float x;
		float y;
		if (mMapView.mapangle != 0) {
			x = (float) ((x1 - mWidth / 2) * Math.cos(mMapView.mapangle) - (y1 - mHeight / 2)
					* Math.sin(mMapView.mapangle))
					+ mWidth / 2;
			y = (float) ((x1 - mWidth / 2) * Math.sin(mMapView.mapangle) + (y1 - mHeight / 2)
					* Math.cos(mMapView.mapangle))
					+ mHeight / 2;
		} else {
			x = x1;
			y = y1;
		}

		return new PointInfo(x, y);

	}

	public synchronized void openMap(String path, String id, String floor) {
		if (path == null)
			return;
		if (mPath != null && path.compareTo(mPath) == 0) {
			return;
		}
		try {
			mLayer = new Layer();
			if (mLayer.readmap(path)) {
				mOpended = true;
			}

			buildwidth = mLayer.envelope._maxx + mLayer.envelope._minx;
			buildheight = -mLayer.envelope._maxy - mLayer.envelope._miny;
			for (int i = 0; i < mLayer.shapes.length; i++) {
				mLabelPaint.setTextSize(MapView.MAPTEXT.getTextsize());
				mLayer.shapes[i].setwidth(mLabelPaint);
			}
			sortby(2);
			setAngle((float) Math.toRadians(mLayer.angle));
		} catch (Exception e) {
			File file = new File(path);
			file.delete();
			e.printStackTrace();
		}
	}

	public void sortby(int attr) {
		Shape temp = null;
		for (int i = 0; i < mLayer.shapes.length - 1; i++) {
			for (int j = 0; j < mLayer.shapes.length - 1 - i; j++) {
				if (attr == 1) {// 按style排序
					if (mLayer.shapes[j].mStyle > mLayer.shapes[j + 1].mStyle) {
						temp = mLayer.shapes[j];
						mLayer.shapes[j] = mLayer.shapes[j + 1];
						mLayer.shapes[j + 1] = temp;
					}
				} else if (attr == 2) {// 按level排序
					if ((mLayer.shapes[j].mLevel > mLayer.shapes[j + 1].mLevel)) {
						temp = mLayer.shapes[j];
						mLayer.shapes[j] = mLayer.shapes[j + 1];
						mLayer.shapes[j + 1] = temp;
					}
				}
			}
		}
		int j = 0;
		for (int i = 0; i < mLayer.shapes.length; i++) {
			if (mLayer.shapes[i].mStyle == 8) {// 外边框是8
				for (j = mLayer.shapes.length - 1; j >= 0; j--) {
					Shape shape = mLayer.shapes[j];
					if (shape.mStyle != 8) {
						break;
					}
				}
				if (i >= j) {
					break;
				}
				Shape s = mLayer.shapes[i];
				mLayer.shapes[i] = mLayer.shapes[j];
				mLayer.shapes[j] = s;
			}
		}
		// for (int i = 0; i < mLayer.shapes.length; i++)
		// Log.e("rtmap", "name : " + mLayer.shapes[i].mName + "    style"
		// + mLayer.shapes[i].mStyle + "    poi_no  : "
		// + mLayer.shapes[i].mId + "    " + mLayer.shapes[i].mLevel);
	}

	public HashSet<Integer> calculateDrawLabelsId() {// 计算需要绘图的注记
		HashSet<Integer> DrawLabelsId = new HashSet<Integer>();
		for (int i = 0; i < mLayer.shapes.length; i++) {
			if (mMapView.Isfling()) {
				break;
			}
			if (mLayer.shapes[i].mCenter == null
			// || mLayer.shapes[i].mStyle != 13
					&& (mLayer.shapes[i].mName == null || mLayer.shapes[i].mName
							.length() == 0)) {
				continue;
			}

			mPaint.setTextSize(MapView.MAPTEXT.getTextsize());

			if (RMStringUtils.isEmpty(mLayer.shapes[i].mDrawname)
					&& mLayer.shapes[i].LabelOverRange(mscale)
					// && mLayer.shapes[i].mStyle != 11 &&
					// mLayer.shapes[i].mStyle !=
					// 15
					&& mLayer.shapes[i].mStyle < 20) {// style=11时不出现点
				mPaint.setTextSize(MapView.MAPTEXT.getTextsize());
				int mx = (int) (Constants.POINTPT * mscale + mPaint
						.measureText(mLayer.shapes[i].mName) / 2 * mscale);
				mLayer.shapes[i].mDisplayCenter = new Coord(
						(int) (mLayer.shapes[i].mCenter.mX + Math.cos(-mMapView.mapangle)
								* mx),
						(int) (mLayer.shapes[i].mCenter.mY + Math
								.sin(-mMapView.mapangle) * mx));
				mLayer.shapes[i].point = true;
			} else {
				mLayer.shapes[i].mDisplayCenter = new Coord(
						mLayer.shapes[i].mCenter);
				mLayer.shapes[i].point = false;
			}
			if (DrawLabelsId.size() == 0) {
				DrawLabelsId.add(i);
			} else {
				boolean needtoadd = true;
				for (Iterator<Integer> j = DrawLabelsId.iterator(); j.hasNext();) {
					int Iteratorid = j.next();
					if (Iteratorid < mLayer.shapes.length) {// 这里出现了数据越界异常，所以添加长度判断
						if (mLayer.shapes[Iteratorid].Intersectbycenter(
								mLayer.shapes[i], mscale, mscale,
								mMapView.mapangle)) {
							needtoadd = false;
							break;
						}
					}
				}
				if (needtoadd) {
					DrawLabelsId.add(i);
				}
			}
		}
		return DrawLabelsId;
	}

	/**
	 * 绘制地图
	 * 
	 * @param canvas
	 * @param context
	 * @param getlabelid
	 * @param mMapView
	 */
	public void drawLabels(Canvas canvas, Context context, boolean getlabelid) {

		if (mLayer.shapes == null) {
			return;
		}
		if (getlabelid || drawLabelsId == null) {
			drawLabelsId = calculateDrawLabelsId();
		}

		drawLabel(canvas, context);

	}

	private void drawLabel(Canvas canvas, Context context) {
		Rect dirty = new Rect(xmin, ymin, xmax, ymax);
		drawLabel(canvas, context, dirty);
	}

	private void drawLabel(Canvas canvas, Context context, Rect dirty) {
		mLabelPaint.setTextSize(MapView.MAPTEXT.getTextsize());
		mLabelPaint.setColor(MapView.MAPTEXT.getTextcolor());
		Iterator<Integer> i = drawLabelsId.iterator();
		while (i.hasNext()) {
			int Iteratorid = i.next();
			if (Iteratorid > mLayer.shapes.length - 1)
				continue;
			Shape shape = mLayer.shapes[Iteratorid];
			if (Iteratorid >= mLayer.shapes.length
					|| RMStringUtils.isEmpty(shape.mName))
				continue;
			Coord coord = shape.mDisplayCenter;
			if (coord != null && coord.mX < dirty.right
					&& coord.mX > dirty.left && coord.mY > dirty.top
					&& coord.mY < dirty.bottom) {
				if (mMapView.getPoiIconMap().containsKey(shape.mName)) {
					shape.mDrawname = mMapView.getPoiIconMap().get(shape.mName);
				}
				if (!RMStringUtils.isEmpty(shape.mDrawname)) {
					try {
						Bitmap bmp = null;
						bmp = BitmapFactory.decodeStream(mMapView.getContext()
								.getAssets().open(shape.mDrawname));
						PointInfo mPoint = fromLocation(new Location(coord.mX,
								coord.mY));
						canvas.drawBitmap(bmp, mPoint.getX() - bmp.getWidth()
								/ 2, mPoint.getY() - bmp.getHeight() / 2,
								mLabelPaint);
					} catch (Exception e) {
						e.printStackTrace();
					}

				} else {
					if (getSelectPoi() == null
							|| shape.mId != getSelectPoi().getPoiNo()) {

						if (shape.point) {
							int mx = (int) (Constants.POINTPT * mscale + mLabelPaint
									.measureText(shape.mName) / 2 * mscale);
							shape.mDisplayCenter = new Coord(
									(int) (shape.mCenter.mX + Math.cos(-mMapView.mapangle)
											* mx),
									(int) (shape.mCenter.mY + Math
											.sin(-mMapView.mapangle) * mx));
							PointInfo mPoint = fromLocation(shape.mCenter);
							canvas.drawCircle(mPoint.getX(), mPoint.getY(),
									Constants.POINTPT / 2, mLabelPaint);
						}
						PointInfo mPoint = fromLocation(new Location(coord.mX,
								coord.mY));
						int x = (int) mPoint.getX();
						int y = (int) (mPoint.getY() + MapView.MAPTEXT
								.getTextsize() / 2);

						drawText(canvas, shape.mName, x, y, mLabelPaint,
								MapView.MAPTEXT.getTextcolor());
					}
				}
			}
		}
	}

	private PointInfo fromLocation(Coord mCenter) {
		return fromLocation(new Location(mCenter.mX, mCenter.mY));
	}

	public void drawText(Canvas canvas, String name, int x, int y,
			Paint mPaint, int color) {
		mPaint.setColor(MapView.MAPTEXT.getTextcolor());
		// mPaint.setAlpha(alpha);
		// canvas.drawText(name, x - 1, y, mPaint);
		// canvas.drawText(name, x + 1, y, mPaint);
		// canvas.drawText(name, x - 1, y - 1, mPaint);
		// canvas.drawText(name, x + 1, y + 1, mPaint);
		// canvas.drawText(name, x, y + 1, mPaint);
		// canvas.drawText(name, x + 1, y - 1, mPaint);
		// canvas.drawText(name, x, y - 1, mPaint);
		// canvas.drawText(name, x - 1, y + 1, mPaint);
		// mPaint.setColor(color);
		// mPaint.setAlpha(alpha);
		canvas.drawText(name, x, y, mPaint);
	}

	public void drawShape(Canvas canvas, Rect dirty) {
		drawShape(canvas, dirty, false);
	}

	public void drawShape(Canvas canvas, Rect dirty, boolean isbackguard) {

		if (canvas == null || mLayer.shapes == null) {
			return;
		}
		PointInfo theMaxPoint = mMapView.skewCoord(new PointInfo(
				getBuildwidth(), getBuildheight()));
		float toJpgScale = Math.max(
				theMaxPoint.getY()
						/ Math.max(mMapView.getHeight(), mMapView.getWidth()),
				theMaxPoint.getX()
						/ Math.min(mMapView.getHeight(), mMapView.getWidth())) * 1000;

		mPaint.setAntiAlias(true);
		Shape[] shapes = mLayer.shapes;
		for (int i = 0; i < shapes.length; i++) {
			Shape s = shapes[shapes.length - i - 1];// 倒序执行，画地面颜色
			mStyle = s.mDrawStyle;

			/**
			 * 画完地面之后基本上此判断不会再进入
			 */
			if (s.mStyle == 8
					&& s.Intersect(dirty.left, dirty.right, dirty.bottom,
							dirty.top)) {
				Path path1 = calculatePath(s, isbackguard, toJpgScale);
				if (path1 != null) {
					if (mStyle.getColorfill() != -1) {
						mPaint.setStyle(Paint.Style.FILL);
						mPaint.setColor(mStyle.getColorfill());
						if (getSelectPoi() != null
								&& s.mId == getSelectPoi().getPoiNo()) {
							mPaint.setColor(0xffFFB185);
						}
						canvas.drawPath(path1, mPaint);
					}
				}
			}

			Shape mshapeHead = shapes[i];// 顺序执行，最后划外边框
			// Log.e("rtmap", "name : " + shapes[i].mName + "    style"
			// + shapes[i].mStyle + "    poi_no  : " + shapes[i].mId);
			mStyle = mshapeHead.mDrawStyle;
			for (POI poi : mMapView.getCustomPoiList()) {
				if (poi.getDrawStyle() != null
						&& poi.getPoiNo() == mshapeHead.mId
						&& mMapView.getBuildId().equals(poi.getBuildId())
						&& mMapView.getFloor().equals(poi.getFloor())) {
					mStyle = poi.getDrawStyle();
				}
			}
			if (mStyle != null
					&& mshapeHead.Intersect(dirty.left, dirty.right,
							dirty.bottom, dirty.top)) {
				Path path1 = calculatePath(mshapeHead, isbackguard, toJpgScale);
				if (path1 == null) {
					continue;
				}
				if (mStyle.getColorfill() != -1 && mshapeHead.mStyle != 8) {
					mPaint.setStyle(Paint.Style.FILL);
					mPaint.setColor(mStyle.getColorfill());
					if (getSelectPoi() != null
							&& mshapeHead.mId == getSelectPoi().getPoiNo()) {
						mPaint.setColor(0xffFFB185);
					}
					canvas.drawPath(path1, mPaint);
				}
				if (mStyle.getColorborder() != -1) {
					mPaint.setStyle(Paint.Style.STROKE);
					mPaint.setColor(mStyle.getColorborder());
					mPaint.setStrokeWidth(mStyle.getWidthborder());
					canvas.drawPath(path1, mPaint);
				}
			}
		}

	}

	private Path calculatePath(Shape mshape, boolean isbackguard,
			float toJpgScale) {
		if (mshape.mName != null) {
			if (!isbackguard) {
				if ((mshape.mStyle == 5 || mshape.mStyle == 6 || mshape.mStyle == 7)
						&& (mshape.maxx - mshape.minx) / mscale < Constants.MIN_PIXEL
						&& (mshape.maxy - mshape.miny) / mscale < Constants.MIN_PIXEL) {
					return null;
				}
			}
		}

		Path path1 = new Path();
		PointInfo mPoint = null;
		if (isbackguard) {

			mPoint = new PointInfo(mLayer.coords[mshape.mPoints[0]].mX
					/ toJpgScale, mLayer.coords[mshape.mPoints[0]].mY
					/ toJpgScale);
			mPoint = mMapView.skewCoord(mPoint);
		} else {
			mPoint = fromLocation(mLayer.coords[mshape.mPoints[0]]);
		}

		path1.moveTo(mPoint.getX(), mPoint.getY());
		for (int j : mshape.mPoints) {
			PointInfo mPoint1;
			if (isbackguard) {

				mPoint1 = new PointInfo(mLayer.coords[j].mX / toJpgScale,
						mLayer.coords[j].mY / toJpgScale);
				mPoint1 = mMapView.skewCoord(mPoint1);
			} else {
				mPoint1 = fromLocation(mLayer.coords[j]);
			}
			path1.lineTo(mPoint1.getX(), mPoint1.getY());
		}
		path1.close();
		return path1;
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public Layer getLayer() {
		return mLayer;
	}

	public void setLayer(Layer layer) {
		this.mLayer = layer;
	}

	public float getBuildwidth() {
		return buildwidth;
	}

	public void setBuildwidth(float buildwidth) {
		this.buildwidth = buildwidth;
	}

	public float getBuildheight() {
		return buildheight;
	}

	public void setBuildheight(float buildheight) {
		this.buildheight = buildheight;
	}

	public POI getSelectPoi() {
		return selectPoi;
	}

	public void setSelectPoi(POI selectPoi) {
		this.selectPoi = selectPoi;
	}

	private String mPath = null;
	private boolean mOpended = false;

	public int getLayerCount() {
		return 0;
	}

	public boolean isOpened() {
		return mOpended;
	}

	public String getPath() {
		return mPath;
	}

	public void close() {
		if (mLayer != null) {
			mLayer.clear();
		}
	}

}
