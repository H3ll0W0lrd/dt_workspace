package com.example.textdemo.view;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewGroup;

import com.dingtao.libs.util.DTLog;
import com.dingtao.libs.util.DTUIUtil;

public class DTBrickView extends ViewGroup implements AnimatorUpdateListener {
	private double mBoardX;// 底板的x和高度
	private double mBallX, mBallY; // 球的x,y轴
	private static final int BOARD_WIDTH = 100;// 底板宽度为120px
	private static final int BOADR_HEIGHT = 200;// 底板高度为150px
	private static final int BOARD_LINE_HEIGHT = 20;// 底板线的高度为10px
	private static final int BALL_RADIUS = 16;// 球的半径
	private static final double RUN_DISTANCE = 10;// 球每次运行距离
	private static final int BALL_BOARD_STEP_TIME = 30;// 小球与板停留时间为70毫秒
	private static final int VELOCITY_MAX = 1000;// 最大速度为300像素每30毫秒
	private double mRunX, mRunY;// 球每次横向与纵向运行距离
	private Paint mPaint2, mPaint, mPaint3;
	private boolean isRunBall;// 是否发球
	private ValueAnimator mAnimator;
	private boolean isRefresh;
	private double mBallDegree;// 球运行角度
	private boolean isSendMsg;// 是否发送信息中
	private double mSaveBoardX, mSaveBallX;// 保存底板x
	private static final int[] COLOR_ARRAY = new int[] { 0xFF194672,
			0xFF234763, 0xFFD564FA, 0xFFCCC123, 0xFF445566, 0xFF3322FF,
			0xFF883FFF, 0xFF69AA78, 0xFF33DD88, 0xFF6611AF };

	private static final int BRICK_HEIGHT = 40;
	private double mBrickWidth;// 砖块宽度

	private VelocityTracker mTracker;

	private int[][] mBrick = new int[10][10];// 砖块集合
	private int[][] mBrickColor = new int[10][10];//   砖块颜色集合

	private double mBoundLeft, mBoundTop, mBoundBottom, mBoundRight;// 小球运动边界

	private OnCrashBrickListener onCrashBrickListener;

	public DTBrickView(Context context) {
		super(context);
		init(context);
	}

	public DTBrickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DTBrickView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		setWillNotDraw(false);
		mPaint2 = new Paint();
		mPaint2.setColor(getResources().getColor(
				android.R.color.holo_green_dark));
		// mPaint2.setStyle(Paint.Style.STROKE);
		mPaint2.setAntiAlias(true);
		mPaint2.setStrokeWidth(BOARD_LINE_HEIGHT);
		mPaint = new Paint();
		mPaint.setColor(getResources().getColor(android.R.color.holo_purple));
		// mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(2);

		mPaint3 = new Paint();
		// mPaint.setStyle(Paint.Style.STROKE);
		mPaint3.setAntiAlias(true);
		mPaint3.setStrokeWidth(2);

		mTracker = VelocityTracker.obtain();

		initValue();
		initAnimator();
		for (int i = 0; i < mBrick.length; i++) {
			for (int j = 0; j < mBrick[i].length; j++) {
				if (Math.random() > 0.5) {
					mBrick[i][j] = 1;
					mBrickColor[i][j] = COLOR_ARRAY[(int) (Math.random() * COLOR_ARRAY.length)];
				}
			}
		}
	}

	private void initValue() {
		isRunBall = false;
		isRefresh = false;
		isSendMsg = false;
		mBallDegree = 45;
		mSaveBoardX = 0;
		mSaveBallX = 0;
	}

	private void initAnimator() {
		mAnimator = ValueAnimator.ofInt(1, 20);
		mAnimator.setDuration(1000);
		mAnimator.setRepeatCount(ValueAnimator.INFINITE);
		mAnimator.addUpdateListener(this);
		mAnimator.start();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Log.i("dt", "onMeasure:"+widthMeasureSpec+"     "+heightMeasureSpec );
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.i("dt", "left:" + l + "   top:" + t + "    right: " + r
				+ "   bottom: " + b);
		DTLog.i(getWidth() + "  ***  " + getHeight());
		initBallBound();
		mBallY = mBoundBottom - BALL_RADIUS;
		mBoardX = getWidth() / 2;
		mBallX = getWidth() / 2;
		mBrickWidth = getWidth() / 10f;
	}

	private void initBallBound() {
		mBoundLeft = 0;
		mBoundTop = 0;
		mBoundRight = getWidth();
		mBoundBottom = getHeight() - BOADR_HEIGHT - BOARD_LINE_HEIGHT / 2;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (getWidth() == 0 || getHeight() == 0)
			return;
		drawBoard(canvas);
		drawBrick(canvas);
		drawBall(canvas);
	}

	public void setOnCrashBrickListener(
			OnCrashBrickListener onCrashBrickListener) {
		this.onCrashBrickListener = onCrashBrickListener;
	}

	/**
	 * 画出运动的小球球哦
	 * 
	 * @param canvas
	 */
	private void drawBall(Canvas canvas) {
		if (isRefresh) {
			mRunX = RUN_DISTANCE * Math.cos(Math.toRadians(mBallDegree));// 小球横向运动距离
			mRunY = RUN_DISTANCE * Math.sin(Math.toRadians(mBallDegree));// 小球纵向运动距离
			mBallX += mRunX;// 小球x坐标
			mBallY -= mRunY;// 小球y坐标
			for (int i = 0; i < mBrick.length; i++) {// 判断小球是否碰撞砖块
				boolean isCrash = false;
				for (int j = 0; j < mBrick[i].length; j++) {
					if (mBrick[i][j] == 1) {
						float x1 = i * (float) mBrickWidth;// 砖块左边坐标
						float x2 = (i + 1) * (float) mBrickWidth;// 砖块右边坐标
						float y1 = j * BRICK_HEIGHT;// 砖块上边坐标
						float y2 = (j + 1) * BRICK_HEIGHT;// 砖块下边坐标
						double ballTopX = mBallX, ballTopY = mBallY// 小球上边坐标
								- BALL_RADIUS;
						if (ballTopX > x1 && ballTopX < x2 && ballTopY > y1
								&& ballTopY < y2) {// 小球碰到砖块下边
							mBoundTop = y2;// 上边界变为砖块下坐标
							isCrash = true;// 撞上了
							mBrick[i][j] = 0;// 砖块消失
							break;
						}
						double ballBottomX = mBallX, ballBottomY = mBallY
								+ BALL_RADIUS;// 小球下边坐标
						if (ballBottomX > x1 && ballBottomX < x2
								&& ballBottomY > y1 && ballBottomY < y2) {// 小球碰到砖块上边
							mBoundBottom = y1;
							isCrash = true;
							mBrick[i][j] = 0;
							break;
						}
						double ballLeftX = mBallX - BALL_RADIUS, ballLeftY = mBallY;// 小球坐边坐标
						if (ballLeftX > x1 && ballLeftX < x2 && ballLeftY > y1
								&& ballLeftY < y2) {// 小球碰到砖块右边
							mBoundLeft = x2;
							isCrash = true;
							mBrick[i][j] = 0;
							break;
						}
						double ballRightX = mBallX + BALL_RADIUS, ballRightY = mBallY;
						if (ballRightX > x1 && ballRightX < x2
								&& ballRightY > y1 && ballRightY < y2) {// 小球碰到砖块左边
							mBoundRight = x1;
							isCrash = true;
							mBrick[i][j] = 0;
							break;
						}
					}
				}
				if (isCrash) {// 如果碰上了，则通知用户
					if (onCrashBrickListener != null) {
						onCrashBrickListener.onCrashBrick();
					}
					break;
				}
			}
			if (!isSendMsg) {
				if (mRunX > 0 && mRunY > 0) {// 小球运行方向为右上方
					if (mBallX + BALL_RADIUS >= mBoundRight) {// 当小球运动接触到右边，则角度为180度翻转，并且需要将mBallX设置为宽度
						mBallX = mBoundRight - BALL_RADIUS;
						mBallDegree = 180 - mBallDegree;
					}
					if (mBallY - BALL_RADIUS <= mBoundTop) {// 当小球运动接触到上边，则角度镜像增加mBallDegree，并且需要mBallY设置为BALL_WIDTH
						mBallY = mBoundTop + BALL_RADIUS;
						mBallDegree = -mBallDegree;
					}
				}
				if (mRunX < 0 && mRunY > 0) {// 运动方向为左上方
					if (mBallY - BALL_RADIUS <= mBoundTop) {// 当小球已经接触到上边，则角度
						mBallY = mBoundTop + BALL_RADIUS;
						mBallDegree = -mBallDegree;
					}
					if (mBallX - BALL_RADIUS <= mBoundLeft) {// 当小球接触到左边
						mBallX = mBoundLeft + BALL_RADIUS;
						mBallDegree = 180 - mBallDegree;
					}
				}
				if (mRunX > 0 && mRunY < 0) {// 小球运动方向为右下方
					if (mBallX + BALL_RADIUS >= mBoundRight) {// 当小球接触到右边
						mBallX = mBoundRight - BALL_RADIUS;
						mBallDegree = 180 - mBallDegree;
					}
					if (mBallY + BALL_RADIUS >= mBoundBottom) {// 当小球接触到下边
						mBallY = mBoundBottom - BALL_RADIUS;
						mBallDegree = -mBallDegree;
					}
				}

				if (mRunX < 0 && mRunY < 0) {// 运动方向为左下方
					if (mBallX - BALL_RADIUS <= mBoundLeft) {// 当小球接触到左边
						mBallX = mBoundLeft + BALL_RADIUS;
						mBallDegree = -mBallDegree - 180;
					}
					if (mBallY + BALL_RADIUS >= mBoundBottom) {// 当小球接触到下边
						mBallY = mBoundBottom - BALL_RADIUS;
						mBallDegree = -mBallDegree;
					}
				}

				mBallDegree = (mBallDegree + 360) % 360;// 将角度变为正值
			}

			if (mBallY == getHeight() - BOADR_HEIGHT - BOARD_LINE_HEIGHT / 2
					- BALL_RADIUS) {// 如果小球在底边时候
				if (mBallX > mBoardX - BOARD_WIDTH
						&& mBallX < mBoardX + BOARD_WIDTH) {// 当小球碰搬，则接住了
					isRefresh = false;// 此刻不要刷新
					mSaveBoardX = mBoardX;
					mSaveBallX = mBallX;
					if (!isSendMsg) {
						isSendMsg = true;
						postDelayed(new Runnable() {// 延迟30毫秒，产生小球在底边接触时的移动效果

									@Override
									public void run() {
										isRefresh = true;
										isSendMsg = false;
										mTracker.computeCurrentVelocity(30);
										DTLog.i("横向速度："
												+ mTracker.getXVelocity()
												+ "    角度: " + mBallDegree);
										mBallDegree += mBallDegree
												* mTracker.getXVelocity()
												/ VELOCITY_MAX;// 根据速率计算小球改变的角度
										if (mBallDegree >= 0
												&& mBallDegree <= 10)
											mBallDegree = 10;
										if (mBallDegree >= 170
												&& mBallDegree <= 180) {
											mBallDegree = 170;
										}
										invalidate();
									}
								}, BALL_BOARD_STEP_TIME);
					}
				} else {
					DTUIUtil.showToastSafe("没接住");
					initValue();
				}
			}
		}
		initBallBound();//重新定义边界
		canvas.drawCircle((float) mBallX, (float) mBallY, BALL_RADIUS, mPaint);//绘制小球
	}

	public interface OnCrashBrickListener {
		void onCrashBrick();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mBoardX = event.getX();
		double leftX = mBoardX - BOARD_WIDTH;
		double rightX = mBoardX + BOARD_WIDTH;
		mTracker.addMovement(event);
		if (leftX < 0) {
			mBoardX = BOARD_WIDTH;
		}
		if (rightX > getWidth()) {
			mBoardX = getWidth() - BOARD_WIDTH;
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			if (!isRunBall) {
				mBallX = mBoardX;
			} else {
				if (isSendMsg) {
					mBallX = mSaveBallX + (mBoardX - mSaveBoardX);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if (!isRunBall) {
				isRunBall = true;
				isRefresh = true;
			}
			break;
		}
		invalidate();
		return true;
	}

	private void drawBoard(Canvas canvas) {
		int boardHeight = getHeight() - BOADR_HEIGHT;
		canvas.drawLine((float) mBoardX - BOARD_WIDTH, boardHeight,
				(float) mBoardX + BOARD_WIDTH, boardHeight, mPaint2);
	}

	private void drawBrick(Canvas canvas) {
		int brickCount = 0;// 剩余砖块数量
		for (int i = 0; i < mBrick.length; i++) {
			for (int j = 0; j < mBrick[i].length; j++) {
				if (mBrick[i][j] == 1) {
					brickCount++;
					float x1 = i * (float) mBrickWidth;
					float x2 = (i + 1) * (float) mBrickWidth;
					float y1 = j * BRICK_HEIGHT;
					float y2 = (j + 1) * BRICK_HEIGHT;
					mPaint3.setColor(mBrickColor[i][j]);
					canvas.drawRect(x1, y1, x2, y2, mPaint3);
				}
			}
		}
		if (brickCount == 0) {
			initValue();
		}
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		if (isRefresh) {
			invalidate();
		}
	}

	public void destory() {
		initValue();
	}

}
