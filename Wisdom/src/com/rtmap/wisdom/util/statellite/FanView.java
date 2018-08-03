package com.rtmap.wisdom.util.statellite;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.rtmap.wisdom.R;
import com.rtmap.wisdom.util.DTLog;
import com.rtmap.wisdom.util.DTUIUtil;

/**
 * Created by rtmap on 2016/12/1.
 */

public class FanView extends View {

	/**
	 * 默认起始角度225
	 * <p>
	 * * \ * \ ---------\------------ \
	 */
	private static final int START_DEGREES = 225;
	private static final int duration = 300;
	int size = 40;
	int radius = 100;
	private int padding = 20;
	/**
	 * item之间的间隔角度，因为只有4个item因此写死30
	 */
	private float perDegrees = 30;
	private int degrees = START_DEGREES;
	/**
	 * 文本画笔
	 */
	private Paint wordPaint;
	/**
	 * 文本背景画笔
	 */
	private Paint wordBgPaint;
	/**
	 * 圆圈的画笔
	 */
	private Paint handPaint;
	/**
	 * view的宽高
	 */
	private int width, height;
	/**
	 * 圆圈的图
	 */
	private Bitmap hand;
	private List<Integer> degressList = new ArrayList<Integer>();
	private OnItemSelectedListener listener;

	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		this.listener = listener;
	}

	public FanView(Context context) {
		this(context, null);
	}

	public FanView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FanView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private int scaler = 0;

	public void setDraw(boolean isDraw) {
		this.isDraw = isDraw;
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < 5; i++) {
					try {
						scaler = i;
						Thread.sleep(60);
						postInvalidate();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	private void init() {
		handPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		handPaint.setColor(Color.WHITE);
		handPaint.setStyle(Paint.Style.FILL);
		handPaint.setStrokeWidth(40);
		handPaint.setTextSize(40);
		handPaint.setTextAlign(Paint.Align.CENTER);

		wordPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		wordPaint.setColor(Color.WHITE);
		wordPaint.setTextSize(30);
		wordPaint.setTextAlign(Paint.Align.LEFT);

		wordBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		wordBgPaint.setStrokeCap(Paint.Cap.ROUND);
		wordBgPaint.setColor(Color.BLACK);
		size = DTUIUtil.dip2px(35);
		radius = DTUIUtil.dip2px(120);
		padding = DTUIUtil.dip2px(5);
		hand = BitmapFactory.decodeResource(this.getContext().getResources(),
				R.drawable.map_long_touch);
	}

	/**
	 * 计算item大小，位置
	 */
	private static Rect computeChildFrame(final int centerX, final int centerY,
			final int radius, final float degrees, final int size) {

		final double childCenterX = centerX + radius
				* Math.cos(Math.toRadians(degrees));
		final double childCenterY = centerY + radius
				* Math.sin(Math.toRadians(degrees));

		return new Rect((int) (childCenterX - size / 2),
				(int) (childCenterY - size / 2),
				(int) (childCenterX + size / 2),
				(int) (childCenterY + size / 2));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		width = w;
		height = h;
	}

	/*
	 * ------------------------------------------------------------------------->
	 * x \ + 0 + 45 90 + \ \ (x<r && y<r) (y<r && x>r && w-x>r) (y<r && w-x<r) \
	 * \ \ \ \ \ \ \ \ \ （x<r && y>r && h-y>r） (w-x<r && y>r && h-y>r) \ \ + 315
	 * + 225 135 + \ \ (x>r && w-x>r && y>r && h-y>r) \ \ \ \ \ \ \ \ (x<r &&
	 * h-y<r) (h-y<r && x>r && w-x>r) (w-x<r && h-y<r) \ \ + 270 +225 180 + \
	 * \-------------------------------------------------------------------- \y
	 */

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!isDraw) {
			canvas.drawColor(Color.TRANSPARENT);
			return;
		}
		canvas.drawColor(Color.parseColor("#66000000"));
		degrees = START_DEGREES;
		float x = locPoint.x;
		float y = locPoint.y;

		float r = radius + 20;
		if (y < r && x > r && width - x > r)
			degrees = 45;
		if (x > r && width - x > r && y > r && height - y > r)
			degrees = 225;
		if (height - y < r && x > r && width - x > r)
			degrees = 225;
		if (y < r && width - x < r)
			degrees = 90;
		if (width - x < r && y > r && height - y > r)
			degrees = 180;// 135;
		if (width - x < r && height - y < r)
			degrees = 180;
		if (x < r && y < r)
			degrees = 0;
		if (x < r && y > r && height - y > r)
			degrees = 315;
		if (x < r && height - y < r)
			degrees = 270;
		initItems(degrees);
		int itemSize = items.size();
		for (int i = 0; i < itemSize; i++) {
			Item item = items.get(i);
			if (i == index) {
				// 绘制图标
				int angle = item.getSrcAngle();
				// 选中后要偏移一下
				if (angle > 180 && angle < 270) {
					canvas.drawBitmap(item.getFoucusBitmap(),
							item.frame.centerX() - 10 - size / 2,
							item.frame.centerY() - 10 - size / 2, handPaint);
				} else if (angle > 90 && angle < 180) {
					canvas.drawBitmap(item.getFoucusBitmap(),
							item.frame.centerX() - 10 - size / 2,
							item.frame.centerY() + 10 - size / 2, handPaint);
				} else if (angle > 0 && angle < 90 || angle > 360
						&& angle < 450) {
					canvas.drawBitmap(item.getFoucusBitmap(),
							item.frame.centerX() + 10 - size / 2,
							item.frame.centerY() + 10 - size / 2, handPaint);
				} else if (angle > 270 && angle < 360) {
					canvas.drawBitmap(item.getFoucusBitmap(),
							item.frame.centerX() + 10 - size / 2,
							item.frame.centerY() - 10 - size / 2, handPaint);
				} else if (angle == 180) {
					canvas.drawBitmap(item.getFoucusBitmap(),
							item.frame.centerX() - 10 - size / 2,
							item.frame.centerY() - size / 2, handPaint);
				} else if (angle == 270) {
					canvas.drawBitmap(item.getFoucusBitmap(),
							item.frame.centerX() - size / 2,
							item.frame.centerY() - 10 - size / 2, handPaint);
				} else if (angle == 360 || angle == 0) {
					canvas.drawBitmap(item.getFoucusBitmap(),
							item.frame.centerX() + 10 - size / 2,
							item.frame.centerY() - size / 2, handPaint);
				} else if (angle == 90) {
					canvas.drawBitmap(item.getFoucusBitmap(),
							item.frame.centerX() - size / 2,
							item.frame.centerY() + 10 - size / 2, handPaint);
				}

				// canvas.drawBitmap(item.getFoucusBitmap(),
				// item.frame.centerX(), item.frame.centerY(), handPaint);
			} else {
				canvas.drawBitmap(item.getNormalBitmap(), item.frame.centerX()
						- size / 2, item.frame.centerY() - size / 2, handPaint);
			}

		}
		/** 为了防止遮盖，选中后的文字最后画 */
		if (index >= 0 && index < itemSize) {
			Item item = items.get(index);
			Rect textRect = new Rect();
			wordPaint.getTextBounds(item.getName(), 0, item.getName().length(),
					textRect);
			float theight = textRect.height();
			wordBgPaint.setStrokeWidth(theight + 5);
			Paint.FontMetricsInt fontMetrics = wordPaint.getFontMetricsInt();
			Rect targetRect = new Rect(item.frame.left, (int) (item.frame.top
					- theight - padding), item.frame.right, item.frame.top
					- padding);
			int baseline = (targetRect.bottom + targetRect.top
					- fontMetrics.bottom - fontMetrics.top) / 2;
			wordPaint.setTextAlign(Paint.Align.CENTER);
			canvas.drawLine(targetRect.left, targetRect.centerY(),
					targetRect.right, targetRect.centerY(), wordBgPaint);
			canvas.drawText(item.getName(), targetRect.centerX(), baseline,
					wordPaint);
		}
		canvas.drawBitmap(hand, locPoint.x - size / 2, locPoint.y - size / 2,
				handPaint);
	}

	private ArrayList<Item> items = new ArrayList<Item>();
	private ArrayList<FanItem> fanItems = new ArrayList<FanItem>();

	public ArrayList<FanItem> getFanItems() {
		return fanItems;
	}

	private void initItems(int angle) {
		items.clear();
		perDegrees = 90 / (fanItems.size() - 1);
		for (int i = 0; i < fanItems.size(); i++) {
			FanItem it = fanItems.get(i);
			Item item = new Item();
			item.setNormalBitmap(BitmapFactory.decodeResource(this.getContext()
					.getResources(), it.getNormalImgRes()));
			item.setFoucusBitmap(BitmapFactory.decodeResource(this.getContext()
					.getResources(), it.getFoucusImgRes()));
			item.setFrame(computeChildFrame((int) locPoint.x, (int) locPoint.y,
					radius / 5 * scaler, angle + i * perDegrees, size));
			item.setSrcAngle(angle);
			item.setName(it.getName());
			item.setType(it.getType());
			items.add(item);
			degressList.add(angle);
		}
	}

	private PointF locPoint = new PointF();
	private PointF movPoint = new PointF();
	private int index = -1;
	private boolean isDraw = false;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
//			DTLog.i("down....");
			items.clear();
			index = -1;
			locPoint.set(event.getX(), event.getY());
			break;

		case MotionEvent.ACTION_MOVE:
//			DTLog.i("move....");
			movPoint.set(event.getX(), event.getY());
			{
				// 两点在X轴的距离
				float lenX = movPoint.x - locPoint.x;
				// 两点在Y轴距离
				float lenY = movPoint.y - locPoint.y;
				// 两点距离
				float lenXY = (float) Math.sqrt((double) (lenX * lenX + lenY
						* lenY));
				// 计算弧度
				double radian = Math.acos(lenX / lenXY)
						* (movPoint.y < locPoint.y ? -1 : 1);
				// 计算角度
				double angle = radian2Angle(radian);
				if (degrees == 315) {
					if (angle < 90
							&& angle > ((degrees + (fanItems.size() - 1)
									* perDegrees) - 360)) {
						index = -1;
						postInvalidate();
						break;
					} else if (angle > 270 && angle < degrees) {
						index = -1;
						postInvalidate();
						break;
					}
				} else {
					if (angle < degrees - 15
							|| angle > degrees + (fanItems.size() - 1)
									* perDegrees + 15) {
						index = -1;
						postInvalidate();
						break;
					}
				}

			}
			if (items != null && items.size() > 0) {
				float dis[] = new float[items.size()];
				for (int i = 0; i < dis.length; i++) {
					Item item = items.get(i);
					float lenX = movPoint.x - item.getFrame().centerX();
					// 两点在Y轴距离
					float lenY = movPoint.y - item.getFrame().centerY();
					float lenXY = (float) Math
							.sqrt((double) (lenX * lenX + lenY * lenY));
					dis[i] = lenXY;
				}
				float minnum = dis[0];
				index = 0;
				for (int i = 0; i < dis.length; i++) {
					if (dis[i] < minnum) {
						minnum = dis[i];
						index = i;
					}
				}
			}
			postInvalidate();

			break;
		case MotionEvent.ACTION_UP:
			DTLog.i("up....");
			if (index != -1 && listener != null && fanItems != null
					&& fanItems.size() > index) {
				listener.onItemSelected(fanItems.get(index), locPoint);
			}

			items.clear();
			isDraw = false;
			postInvalidate();
			break;
		}
		return false;
	}

	/**
	 * 弧度转成角度
	 */
	public static double radian2Angle(double radian) {
		double tmp = Math.round(radian / Math.PI * 180);
		return tmp >= 0 ? tmp : 360 + tmp;
	}

	public interface OnItemSelectedListener {
		void onItemSelected(FanItem fanItem, PointF pointF);
	}

	private class Item {
		private Bitmap normalBitmap;
		private Bitmap foucusBitmap;
		private int srcAngle;
		private Rect frame;
		private String name;
		private int type;

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Bitmap getNormalBitmap() {
			return normalBitmap;
		}

		public void setNormalBitmap(Bitmap normalBitmap) {
			this.normalBitmap = normalBitmap;
		}

		public Bitmap getFoucusBitmap() {
			return foucusBitmap;
		}

		public void setFoucusBitmap(Bitmap foucusBitmap) {
			this.foucusBitmap = foucusBitmap;
		}

		public int getSrcAngle() {
			return srcAngle;
		}

		public void setSrcAngle(int srcAngle) {
			this.srcAngle = srcAngle;
		}

		public Rect getFrame() {
			return frame;
		}

		public void setFrame(Rect frame) {
			this.frame = frame;
		}
	}

}
