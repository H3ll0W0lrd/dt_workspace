package com.rtmap.wifipicker.widget;

import com.rtmap.wifipicker.data.Coord;
import com.rtmap.wifipicker.data.Envelope;

import android.graphics.Matrix;
import android.graphics.Point;

/**
 * 进行坐标转换
 * @author hotstar
 * 
 */
public class CoordTransform {
    private Envelope mEnvelope = null;
    private Envelope mBitmapEnvelope = new Envelope();
    private float mRatioX = 0;
    private float mRatioY = 0;
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    private Matrix mMatrix;
    private boolean mDirty = false;

    public CoordTransform() {
        mMatrix = new Matrix();
    }

    /**
     * 测试代码
     * @param dx x方向上的坐标
     * @return
     */
    public float xBumpTest(float dx) {
        if (mBitmapEnvelope._minx == 0 && mBitmapEnvelope._maxx < mScreenWidth && dx < 0)
            return 0;
        if (mBitmapEnvelope._maxx == mScreenWidth && mBitmapEnvelope._minx > 0 && dx > 0)
            return 0;
        if (mBitmapEnvelope._maxx + dx > mScreenWidth && mBitmapEnvelope._minx + dx > 0) {
            if (mBitmapEnvelope.getWidth() > mScreenWidth)
                return -mBitmapEnvelope._minx;
            else
                return mScreenWidth - mBitmapEnvelope._maxx;
        }

        if (mBitmapEnvelope._minx + dx < 0 && mBitmapEnvelope._maxx + dx < mScreenWidth) {
            if (mBitmapEnvelope.getWidth() > mScreenWidth)
                return mScreenWidth - mBitmapEnvelope._maxx;
            else
                return -mBitmapEnvelope._minx;
        }

        return dx;
    }

    /**
     * 测试代码
     * @param dy y方向上的坐标
     * @return
     */
    public float yBumpTest(float dy) {
        if (mBitmapEnvelope._miny == 0 && mBitmapEnvelope._maxy < mScreenHeight && dy < 0)
            return 0;
        if (mBitmapEnvelope._maxy == mScreenHeight && mBitmapEnvelope._miny > 0 && dy > 0)
            return 0;
        if (mBitmapEnvelope._maxy + dy > mScreenHeight && mBitmapEnvelope._miny + dy > 0) {
            if (mBitmapEnvelope.getHeight() > mScreenHeight)
                return -mBitmapEnvelope._miny;
            else
                return mScreenHeight - mBitmapEnvelope._maxy;
        }
        if (mBitmapEnvelope._miny + dy < 0 && mBitmapEnvelope._maxy + dy < mScreenHeight) {
            if (mBitmapEnvelope.getHeight() > mScreenHeight)
                return mScreenHeight - mBitmapEnvelope._maxy;
            else
                return -mBitmapEnvelope._miny;
        }
        return dy;
    }

    /**
     * 坐标系转换得到图片坐标
     * @param dx
     * @param dy
     */
    public void bitmapTranslate(float dx, float dy) {
        if (dx == 0 && dy == 0)
            return;
        mBitmapEnvelope.translate((int) dx, (int) dy);
        //
        mMatrix.setTranslate(mBitmapEnvelope._minx, mBitmapEnvelope._miny);

        reset();
        mDirty = true;
    }

    public void bitmapTranslateToCenter(float x, float y) {
        int cx = mScreenWidth / 2;
        int cy = mScreenHeight / 2;
        bitmapTranslate(cx - x, cy - y);
    }

    /**
     * 设置屏幕的范围
     * @param width 宽
     * @param height 高
     */
    public void setScreenDimension(int width, int height) {
        mScreenWidth = width;
        mScreenHeight = height;
    }

    public int getClipX() {
        return mScreenWidth;
    }

    public int getClipY() {
        return mScreenHeight;
    }

    /**
     * 设置图像的范围
     * @param width 宽
     * @param height 高
     */
    public void setBitmapDimension(int width, int height) {
        if (mBitmapEnvelope.getWidth() == width && mBitmapEnvelope.getHeight() == height)
            return;
        if (width == 0 || height == 0)
            return;
        if (mBitmapEnvelope.isNull()) {
            mBitmapEnvelope.init(0, 0, width, height);
            moveCenter();
        } else {
            Coord c = new Coord();
            screenToBitmap(mScreenWidth / 2, mScreenHeight / 2, c);
            float rx = (float) c.mX / (float) mBitmapEnvelope.getWidth();
            float ry = (float) c.mY / (float) mBitmapEnvelope.getHeight();
            int nx = (int) (width * rx);
            int ny = (int) (height * ry);
            int offsetx = nx - c.mX;
            int offsety = ny - c.mY;
            int oldWidth = mBitmapEnvelope.getWidth();
            int oldHeight = mBitmapEnvelope.getHeight();

            mBitmapEnvelope.translate(-offsetx, -offsety);
            mBitmapEnvelope.inflate(0, 0, width - oldWidth, height - oldHeight);

            // test
            mMatrix.setTranslate(mBitmapEnvelope._minx, mBitmapEnvelope._miny);
        }
        reset();
    }

    /**
     * 设置Envelope（封装？）
     * @param env
     */
    public void setEnvelope(Envelope env) {
        if (env == mEnvelope)
            return;
        mEnvelope = env;
        reset();
    }

    /**
     * 从图上得到的坐标（实际坐标信息）
     * @param clix
     * @param cliy
     * @param c
     * @return
     */
    public boolean clientToWorld(float clix, float cliy, Coord c) {
        if (mEnvelope == null)
            return false;
        if (c == null)
            return false;
        if (!screenToBitmap(clix, cliy, c))
            return false;
        c.mX = mEnvelope._minx + (int) (c.mX / mRatioX);
        c.mY = mEnvelope._miny + (int) (c.mY / mRatioY);

        return true;
    }

    /**
     * 屏幕点击后的在图片上显示的坐标
     * @param worx
     * @param wory
     * @param p
     * @return
     */
    public boolean worldToClient(float worx, float wory, Point p) {
        if (mEnvelope == null)
            return false;
        if (p == null)
            return false;
        p.x = (int) ((worx - mEnvelope._minx) * mRatioX);
        p.y = (int) ((wory - mEnvelope._miny) * mRatioY);

        return bitmapToScreen(p.x, p.y, p);
    }

    /**
     * 得到当前显示的Envelope
     * @return
     */
    public Envelope getBitmapEnvelope() {
        return mBitmapEnvelope;
    }

    /**
     * 有效性判断
     * @return
     */
    public boolean checkValid() {
        if (mEnvelope == null)
            return false;
        if (mEnvelope.isNull())
            return false;
        if (mBitmapEnvelope.isNull())
            return false;
        if (mScreenWidth == 0 || mScreenHeight == 0)
            return false;
        return true;
    }

    /**
     * 进行缩放
     * @param ratio
     */
    public void zoom(float ratio) {
        mMatrix.postScale(ratio, ratio, mScreenWidth / 2, mScreenHeight / 2);
    }

    public Matrix getMatrix() {
        return mMatrix;
    }

    public void setMatrix(Matrix m) {
        mMatrix.set(m);
    }

    /**
     * 坐标转换，屏幕坐标 -> 地图坐标
     * @param sx
     * @param sy
     * @param c
     * @return
     */
    private boolean screenToBitmap(float sx, float sy, Coord c) {
        c.mX = (int) (sx - mBitmapEnvelope._minx);// + (int)(sx / mSrcRatioX);
        c.mY = (int) (sy - mBitmapEnvelope._miny);// + (int)(sy / mSrcRatioY);
        return true;
    }

    /**
     * 坐标转换，地图坐标 -> 屏幕坐标
     * @param bx
     * @param by
     * @param p
     * @return
     */
    private boolean bitmapToScreen(float bx, float by, Point p) {
        p.x = (int) (bx + mBitmapEnvelope._minx);
        p.y = (int) (by + mBitmapEnvelope._miny);
        return true;
    }

    /**
     * 重置
     */
    private void reset() {
        if (mEnvelope == null)
            return;
        if (mEnvelope.getWidth() == 0 || mEnvelope.getHeight() == 0)
            return;
        mRatioX = ((float) mBitmapEnvelope.getWidth()) / (float) (mEnvelope.getWidth());
        mRatioY = ((float) mBitmapEnvelope.getHeight()) / (float) (mEnvelope.getHeight());
    }

    /**
     * 移动到屏幕中心
     */
    private void moveCenter() {
        if (mScreenWidth == 0 || mScreenHeight == 0)
            return;
        if (mDirty)
            return;
        mBitmapEnvelope.translate(-mBitmapEnvelope._minx, -mBitmapEnvelope._miny);
        int ox = (mScreenWidth - mBitmapEnvelope._maxx) / 2;
        int oy = (mScreenHeight - mBitmapEnvelope._maxy) / 2;

        mBitmapEnvelope.translate(ox, oy);
        // test
        mMatrix.setTranslate(mBitmapEnvelope._minx, mBitmapEnvelope._miny);
    }
}
