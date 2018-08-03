package com.rtmap.wisdom.util.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.rtmap.wisdom.R;
import com.rtmap.wisdom.model.UIBuildInfo;
import com.rtmap.wisdom.util.DTUIUtil;

public class DTSatelliteLayout extends ViewGroup {

	private ArrayList<UIBuildInfo> mBuildList = new ArrayList<UIBuildInfo>();
	public final static int[] BG_COLOR_ARRAY = new int[] {
			R.drawable.like_circle1, R.drawable.like_circle2,
			R.drawable.like_circle3, R.drawable.like_circle4,
			R.drawable.like_circle5, R.drawable.like_circle6,
			R.drawable.like_circle1 };
	public final static int[] BG_ICON_ARRAY = { R.drawable.build_chi,
			R.drawable.build_lei, R.drawable.build_meng, R.drawable.build_niu,
			R.drawable.build_qiong, R.drawable.build_like,
			R.drawable.build_xin, R.drawable.build_zei };

	public boolean update;// 是否为更改状态

	public void addItem(UIBuildInfo info) {
		mBuildList.add(info);
	}

	public void addList(ArrayList<UIBuildInfo> list) {
		mBuildList.addAll(list);
	}

	public UIBuildInfo getItem(int index) {
		return mBuildList.get(index);
	}

	public int getCount() {
		return mBuildList.size();
	}

	public ArrayList<UIBuildInfo> getBuildList() {
		return mBuildList;
	}

	public void notifyDataChanged() {
		for (int i = 0; i < getChildCount(); i++) {
			FrameLayout layout = (FrameLayout) getChildAt(i);
			MarqueeTextView text = (MarqueeTextView) layout.getChildAt(0);
			ImageView image = (ImageView) layout.getChildAt(1);
			if (i < mBuildList.size()) {
				UIBuildInfo info = mBuildList.get(i);
				text.setText(info.getBuild().getBuildName());
				text.setBackgroundResource(BG_COLOR_ARRAY[i]);
				image.setBackgroundResource(R.drawable.dt_trans);
				if (info.getBgIndex() != -1) {// 设置背景
					image.setBackgroundResource(BG_ICON_ARRAY[info.getBgIndex()]);
				}
				layout.setBackgroundResource(R.drawable.build_s_bg_all2);
			} else if (i == mBuildList.size()) {
				text.setBackgroundResource(R.drawable.dt_trans);
				text.setText("");
				image.setBackgroundResource(R.drawable.dt_trans);
				layout.setBackgroundResource(R.drawable.build_add);
			} else {
				text.setBackgroundResource(R.drawable.dt_trans);
				text.setText("");
				image.setBackgroundResource(R.drawable.dt_trans);
				layout.setBackgroundResource(R.drawable.build_s_bg_white1);
			}

			ImageView cha = (ImageView) layout.getChildAt(2);
			if (i < mBuildList.size() && update) {
				cha.setVisibility(View.VISIBLE);
			} else {
				cha.setVisibility(View.GONE);
			}
		}
	}

	public DTSatelliteLayout(Context context) {
		super(context);
		init();
	}

	public DTSatelliteLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DTSatelliteLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private int imageWidth = DTUIUtil.getDimens(R.dimen.build_statellite_image);
	private int boderWidth = 10;

	public void setImageSize(int imageWidth) {
		this.imageWidth = imageWidth;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		for (int i = 0; i < getChildCount(); i++) {

			FrameLayout layout = (FrameLayout) getChildAt(i);
			layout.setTag(i);
			layout.measure(layout.getWidth(), layout.getHeight());
			layout.getChildAt(0).setTag(i);
			layout.getChildAt(0).measure(layout.getChildAt(0).getWidth(),
					layout.getChildAt(0).getHeight());
			layout.getChildAt(1).setTag(i);
			layout.getChildAt(1).measure(layout.getChildAt(1).getWidth(),
					layout.getChildAt(1).getHeight());
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		float radius = (r - l) / 4.0f;

		final int centerX = getWidth() / 2;
		final int centerY = getHeight() / 2;
		// DTLog.e("centerX:" + centerX + "     centerY:" + centerY
		// + "    radius:" + radius);
		//
		// DTLog.i("childCount : " + getChildCount());
		final float perDegrees = 360 / (getChildCount() - 1 == 0 ? 1
				: getChildCount() - 1);
		getChildAt(0).layout(centerX - imageWidth / 2,
				centerY - imageWidth / 2, centerX + imageWidth / 2,
				centerY + imageWidth / 2);
		FrameLayout layout1 = (FrameLayout) getChildAt(0);
		layout1.getChildAt(0).layout(boderWidth, boderWidth,
				imageWidth - boderWidth, imageWidth - boderWidth);
		layout1.getChildAt(1).layout(0, 0, imageWidth, imageWidth);
		layout1.getChildAt(2).layout(imageWidth - 40, 0, imageWidth, 40);
		float degrees = -90;
		for (int i = 1; i < getChildCount(); i++) {

			Rect frame = computeChildFrame(centerX, centerY, radius, degrees,
					imageWidth);
			// DTLog.e("角度:" + degrees + frame.right + "    " + frame.left +
			// "   "
			// + frame.top + "   " + frame.bottom + "    child宽度："
			// + getChildAt(i).getLeft());
			degrees += perDegrees;
			FrameLayout layout = (FrameLayout) getChildAt(i);
			layout.layout(frame.left, frame.top, frame.right, frame.bottom);
			layout.getChildAt(0).layout(boderWidth, boderWidth,
					imageWidth - boderWidth, imageWidth - boderWidth);
			layout.getChildAt(1).layout(0, 0, imageWidth, imageWidth);
			layout.getChildAt(2).layout(imageWidth - 40, 0, imageWidth, 40);
		}

	}

	private static Rect computeChildFrame(final int centerX, final int centerY,
			final float radius, final float degrees, final int size) {

		final double childCenterX = centerX + radius
				* Math.cos(Math.toRadians(degrees));
		final double childCenterY = centerY + radius
				* Math.sin(Math.toRadians(degrees));

		return new Rect((int) (childCenterX - size / 2),
				(int) (childCenterY - size / 2),
				(int) (childCenterX + size / 2),
				(int) (childCenterY + size / 2));
	}

	private Paint mPaint2;
	private Path path = new Path();

	private void init() {
		setWillNotDraw(false);
		mPaint2 = new Paint();
		mPaint2.setColor(0xfff1f1f3);
		mPaint2.setStyle(Paint.Style.STROKE);
		mPaint2.setAntiAlias(true);
		mPaint2.setStrokeWidth(2);
	}
	
	public void setLineColor(int color) {
		mPaint2.setColor(color);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		path.reset();

		float radius = getWidth() / 4.0f;
		float centerX = getWidth() / 2;
		float centerY = getHeight() / 2;
		canvas.drawCircle(centerX, centerY, radius, mPaint2);
		for (int i = 1; i < getChildCount(); i++) {
			View view = getChildAt(i);
			int childCenterX = view.getRight() - view.getWidth() / 2;
			int childCenterY = view.getBottom() - view.getHeight() / 2;
			canvas.drawLine(centerX, centerY, childCenterX, childCenterY,
					mPaint2);
			if (i == 1) {
				path.moveTo(childCenterX, childCenterY);
			} else {
				path.lineTo(childCenterX, childCenterY);
			}
		}
		path.close();
		canvas.drawPath(path, mPaint2);
	}

	public void setChildUpdate(boolean b) {
		update = b;
	}

	public boolean isChildUpdate() {
		return update;
	}
}
