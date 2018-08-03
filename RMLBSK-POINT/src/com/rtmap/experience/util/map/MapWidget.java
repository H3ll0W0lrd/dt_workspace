package com.rtmap.experience.util.map;

import java.util.ArrayList;

import com.rtmap.experience.util.DTImageUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MapWidget extends SurfaceView implements SurfaceHolder.Callback,
		OnGestureListener {
	public static boolean isRunning = true;

	private final static int EVENT_MAP_LOADED = 1;

	private final static int EVENT_BITMAP_DECODED = 2;

	private final static int EVENT_LAYER_CHANGED = 3;

	private final static int EVENT_PATH_PLANED = 4;

	private final static int EVENT_LOCATION_UPDATED = 5;

	private final static int ENVENT_VIRTUAL_GUILD_FINISHED = 6;

	private static final int MOVE_DISTANCE = 10;

	private final static int ACTION_NONE = -1;

	private final static int ACTION_ZOOM = 0;

	@SuppressWarnings("unused")
	private final static int ACTION_SCROLL = 1;

	private final static int ACTION_PICK = 2;

	private int mAction = ACTION_NONE;

	private DrawThread mDrawThread = null;

	private GestureDetector mGestureDetector = null;

	private LCPointTransform mLCPointTransform = null;

	private Context mContext;

	private Map mMap = null;

	private Handler mHandler = null;

	// private ProgressDialog mProgressDialog;
	private int mCurLevel = 0;

	private int mCurLayer = 0;

	// for map zoom parameters
	private float mOldDistance = 0;

	private Matrix mSavedMatrix = new Matrix();

	private SurfaceHolder mSurfaceHolder;

	// 附加显示信息
	private ArrayList<Marker> mMarks;

	private Marker mOneMarker;

	// 是否只显示一个指纹点
	private boolean drawOnePoint;

	/** 同步锁，用于p数据的一致性 **/
	private static Object lockObject = new Object();

	private float mLastMoveX;

	private float mLastMoveY;

	private ArrayList<OnMouseListener> mMouseListeners;

	private ArrayList<WidgetStateListener> mWidgetStateListeners;

	private ArrayList<MapStateListener> mMapStateListeners;

	public void onResume() {
		synchronized (lockObject) {
			isRunning = true;
		}
	}

	public void onPause() {
		synchronized (lockObject) {
			isRunning = false;
		}
	}

	public MapWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		// mProgressDialog = new ProgressDialog(mContext);
		mMarks = new ArrayList<Marker>();
		mMouseListeners = new ArrayList<OnMouseListener>();
		mWidgetStateListeners = new ArrayList<WidgetStateListener>();
		mMapStateListeners = new ArrayList<MapStateListener>();
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);

		mGestureDetector = new GestureDetector(context, this);
		mGestureDetector.setIsLongpressEnabled(true);
		mLCPointTransform = new LCPointTransform();
		// mMap = new Map();
		setFocusable(true); // make sure we get key events

		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				// mProgressDialog.dismiss();
				switch (msg.what) {
				case EVENT_MAP_LOADED:
					onMapOpened();
					startDrawMap();
					break;
				case EVENT_BITMAP_DECODED:
					startDrawMap();
					break;
				case EVENT_LAYER_CHANGED:
					break;
				case EVENT_PATH_PLANED:
					break;
				case EVENT_LOCATION_UPDATED:
					break;
				case ENVENT_VIRTUAL_GUILD_FINISHED:
					break;
				}
			}
		};
	}

//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		/** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
//		switch (event.getAction() & MotionEvent.ACTION_MASK) {
//		// 手指压下屏幕
//		case MotionEvent.ACTION_DOWN:
//			mode = MODE_DRAG;
//			// 记录ImageView当前的移动位置
//			currentMatrix.set(mLCPointTransform.getMatrix());
//			startPoint.set(event.getX(), event.getY());
//			break;
//		// 手指在屏幕上移动，改事件会被不断触发
//		case MotionEvent.ACTION_MOVE:
//			// 拖拉图片
//			if (mode == MODE_DRAG) {
//				float dx = event.getX() - startPoint.x; // 得到x轴的移动距离
//				float dy = event.getY() - startPoint.y; // 得到x轴的移动距离
//				// 在没有移动之前的位置上进行移动
//				matrix.set(currentMatrix);
//				matrix.postTranslate(dx, dy);
//			}
//			// 放大缩小图片
//			else if (mode == MODE_ZOOM) {
//				float endDis = distance(event);// 结束距离
//				if (endDis > 10f) { // 两个手指并拢在一起的时候像素大于10
//					float scale = endDis / startDis;// 得到缩放倍数
//					matrix.set(currentMatrix);
//					matrix.postScale(scale, scale, midPoint.x, midPoint.y);
//				}
//			}
//			break;
//		// 手指离开屏幕
//		case MotionEvent.ACTION_UP:
//			// 当触点离开屏幕，但是屏幕上还有触点(手指)
//		case MotionEvent.ACTION_POINTER_UP:
//			mode = 0;
//			break;
//		// 当屏幕上已经有触点(手指)，再有一个触点压下屏幕
//		case MotionEvent.ACTION_POINTER_DOWN:
//			mode = MODE_ZOOM;
//			/** 计算两个手指间的距离 */
//			startDis = distance(event);
//			/** 计算两个手指间的中间点 */
//			if (startDis > 10f) { // 两个手指并拢在一起的时候像素大于10
//				midPoint = mid(event);
//				// 记录当前ImageView的缩放倍数
//				currentMatrix.set(mLCPointTransform.getMatrix());
//			}
//			break;
//		}
//		mLCPointTransform.setMatrix(matrix);
//		return true;
//	}
//
//	/** 记录是拖拉照片模式还是放大缩小照片模式 */
//	private int mode = 0;// 初始状态
//	/** 拖拉照片模式 */
//	private static final int MODE_DRAG = 1;
//	/** 放大缩小照片模式 */
//	private static final int MODE_ZOOM = 2;
//
//	/** 用于记录开始时候的坐标位置 */
//	private PointF startPoint = new PointF();
//	/** 用于记录拖拉图片移动的坐标位置 */
//	private Matrix matrix = new Matrix();
//	/** 用于记录图片要进行拖拉时候的坐标位置 */
//	private Matrix currentMatrix = new Matrix();
//
//	/** 两个手指的开始距离 */
//	private float startDis;
//	/** 两个手指的中间点 */
//	private PointF midPoint;
//
//	/** 计算两个手指间的距离 */
//	private float distance(MotionEvent event) {
//		float dx = event.getX(1) - event.getX(0);
//		float dy = event.getY(1) - event.getY(0);
//		/** 使用勾股定理返回两点之间的距离 */
//		return FloatMath.sqrt(dx * dx + dy * dy);
//	}
//
//	/** 计算两个手指间的中间点 */
//	private PointF mid(MotionEvent event) {
//		float midX = (event.getX(1) + event.getX(0)) / 2;
//		float midY = (event.getY(1) + event.getY(0)) / 2;
//		return new PointF(midX, midY);
//	}

	@SuppressWarnings("unused")
	public boolean onTouchEvent(MotionEvent event) {
		if (!isRunning)
			return true;
		mGestureDetector.onTouchEvent(event);
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mAction = ACTION_PICK;
			mLastMoveX = event.getX();
			mLastMoveY = event.getY();

			Log.d("MapWidget",
					String.format("down %f %f", mLastMoveX, mLastMoveY));
			break;
		case MotionEvent.ACTION_UP: {
			Log.d("MapWidget", "up");
			if (mAction == ACTION_PICK) {
				float x = event.getX();
				float y = event.getY();
				int size = mMouseListeners.size();
				for (int i = 0; i < size; i++) {
					mMouseListeners.get(i).onSingleTap(this, x, y);
				}
			}
			if(mAction == ACTION_SCROLL){//移动
				if(onActionUpListener!=null)
					onActionUpListener.onActionUp();
			}
		}
			break;
		case MotionEvent.ACTION_POINTER_DOWN: {
			int counts = event.getPointerCount();
			mAction = ACTION_ZOOM;
			if (counts > 1) {
				float x = event.getX(0) - event.getX(counts - 1);
				float y = event.getY(0) - event.getY(counts - 1);
				mOldDistance = (float) Math.sqrt(x * x + y * y);
				Matrix m = mLCPointTransform.getMatrix();
				mSavedMatrix.set(m);
			}
		}
			break;
		case MotionEvent.ACTION_MOVE: {
			int counts = event.getPointerCount();
			Log.d("MapWidget",
					String.format("move %d level %d", counts, mCurLevel));
			if (counts > 1) {
				mAction = ACTION_NONE;
				// 禁止缩放
				float x = event.getX(0) - event.getX(counts - 1);
				float y = event.getY(0) - event.getY(counts - 1);
				float newDistance = (float) Math.sqrt(x * x + y * y);

				float dd = Math.abs(newDistance - mOldDistance);
				float ratio = 1;
				if (dd > 10) {
					if (newDistance < mOldDistance) {
						// 如果已经最小，则不允许再缩小
						if (mCurLevel <= 0)
							break;
						ratio -= ((dd / mOldDistance)) * 0.5;
					} else {
						if (mCurLevel >= 2)
							break;
						ratio += ((dd / newDistance)) * 0.6;
					}
				}
				mLCPointTransform.setMatrix(mSavedMatrix);
				// mLCPointTransform.zoom(ratio);
			} else {
				float deltaX = event.getX() - mLastMoveX;
				float deltaY = event.getY() - mLastMoveY;
				Log.d("MapWidget", String.format("move %f %f", deltaX, deltaY));
				if (Math.abs(deltaX) > MOVE_DISTANCE
						|| Math.abs(deltaY) > MOVE_DISTANCE) {
					mAction = ACTION_SCROLL;
				}
			}
		}
			break;
		case MotionEvent.ACTION_POINTER_UP: {
			int counts = event.getPointerCount();
			mAction = ACTION_ZOOM;
			if (counts > 1) {
				float x = event.getX(0) - event.getX(counts - 1);
				float y = event.getY(0) - event.getY(counts - 1);
				float newDistance = (float) Math.sqrt(x * x + y * y);
				float dd = newDistance - mOldDistance;
				// 禁止缩放
				if (Math.abs(dd) > 10) {
					if (newDistance < mOldDistance) {
						zoomout();
					} else {
						zoomin();
					}
					// mLCPointTransform.zoom(1);
				}
			}
		}
			break;
		}

		return true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		mLCPointTransform.setScreenDimension(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// loadMap();// 延迟到窗口完成之后进行
		mDrawThread = new DrawThread(mSurfaceHolder, mContext);
		int size = mWidgetStateListeners.size();
		for (int i = 0; i < size; i++) {
			mWidgetStateListeners.get(i).onMapWidgetCreated(this);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		mDrawThread.setRunning(false);
		while (retry) {
			try {
				mDrawThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
		mDrawThread = null;
	}

	@Override
	public boolean onDown(MotionEvent e) {

		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		Log.d("MapWidget",
				String.format("on scroll %f %f", distanceX, distanceY));
		mLCPointTransform.bitmapTranslate(-distanceX, -distanceY);
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {

		return false;
	}

	public void addMark(Marker marker) {
		if (marker == null) {
			return;
		}
		drawOnePoint = false;
		mMarks.add(marker);
	}

	@SuppressWarnings("unused")
	private void addOneMark(Marker marker) {
		if (marker == null) {
			return;
		}
		drawOnePoint = true;
		mOneMarker = marker;
	}

	public void registerMouseListener(OnMouseListener lsn) {
		if (lsn != null)
			mMouseListeners.add(lsn);
	}

	public void removeMouseListener(OnMouseListener lsn) {
		if (lsn != null)
			mMouseListeners.remove(lsn);
	}

	public void removeAllMouseListener() {
		mMouseListeners.clear();
	}

	public void registerWidgetStateListener(WidgetStateListener lsn) {
		if (lsn != null)
			mWidgetStateListeners.add(lsn);
	}

	public void removeWidgetStateListener(WidgetStateListener lsn) {
		if (lsn != null)
			mWidgetStateListeners.remove(lsn);
	}

	public void removeAllWidgetStateListener() {
		mWidgetStateListeners.clear();
	}

	public void registerMapStateListener(MapStateListener lsn) {
		if (lsn != null)
			mMapStateListeners.add(lsn);
	}

	public void removeMapStateListener(MapStateListener lsn) {
		if (lsn != null)
			mMapStateListeners.remove(lsn);
	}

	public void removeAllMapStateListener() {
		mMapStateListeners.clear();
	}

	@SuppressWarnings("unused")
	private String getMapName() {
		if (mMap == null)
			return "";
		return mMap.getName();
	}

	private void setBitmap(Bitmap bmp) {
		if (bmp != null) {
			mLCPointTransform.setBitmapDimension(bmp.getWidth(),
					bmp.getHeight());
		}
		if (mDrawThread != null)
			mDrawThread.setBitmap(bmp);
	}

	private void setEnvelope(Envelope box) {
		mLCPointTransform.setEnvelope(box);
	}

	private void zoomin() {
		mCurLevel++;
		if (mCurLevel >= 3) {
			mCurLevel = 2;
			return;
		}
		startLoadBitmap();
	}

	private void zoomout() {
		mCurLevel--;
		if (mCurLevel < 0) {
			mCurLevel = 0;
			return;
		}
		startLoadBitmap();
	}

	@SuppressWarnings("unused")
	private void gotoLayer(int toLayer) {
		if (mCurLayer == toLayer)
			return;
		mCurLayer = toLayer;
		startLoadBitmap();
	}

	@SuppressWarnings("unused")
	private Map getMap() {
		return mMap;
	}

	@SuppressWarnings("unused")
	private Layer getCurLayer() {
		return mMap.getLayer(mCurLayer);
	}

	@SuppressWarnings("unused")
	private synchronized void openMap(final String strMap) {
		if (strMap == null)
			return;
		mMap = new Map();
		// mProgressDialog.setMessage("正在加载地图，请稍侯...");
		// mProgressDialog.show();
		new Thread() {
			public void run() {
				try {
					int layerCount = mMap.open(strMap);
					if (layerCount > 0) {
						mCurLayer = 0;
						loadBitmap();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					mHandler.sendEmptyMessage(EVENT_MAP_LOADED);
				}
			}
		}.start();
	}

	public synchronized void openMapFile(final String strMap) {
		if (strMap == null)
			return;

		mMap = new Map();
		new Thread() {
			public void run() {
				try {
					loadBitmap(strMap);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					mHandler.sendEmptyMessage(EVENT_MAP_LOADED);
				}
			}
		}.start();
	}

	public LCPointTransform getLCPointTransformer() {
		return mLCPointTransform;
	}

	private void startLoadBitmap() {
		// mProgressDialog.show();
		new DecodeBitmapThread().start();
	}

	private void loadBitmap() {
		if (mMap == null)
			return;
		Layer layer = mMap.getLayer(mCurLayer);
		if (layer == null)
			return;
		setEnvelope(layer.getEnvelope());
		byte[] buffer = layer.getBitmap(mCurLevel);
		if (buffer == null)
			return;
		Bitmap bmp = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
		setBitmap(bmp);
	}

	private void loadBitmap(String filePath) {
		Bitmap bmp = DTImageUtil.getBitmap(filePath);
		setEnvelope(new Envelope(0, 0, bmp.getWidth(), bmp.getHeight()));
		setBitmap(bmp);
	}

	private void startDrawMap() {
		if (mDrawThread == null) {
			return;
		}
		if (!mDrawThread.isRunning()) {
			mDrawThread.setRunning(true);
			mDrawThread.start();
		}
	}

	public interface OnMouseListener {
		public void onSingleTap(MapWidget mw, float x, float y);
	}
	
	private OnActionUpListener onActionUpListener;
	
	public OnActionUpListener getOnActionUpListener() {
		return onActionUpListener;
	}

	public void setOnActionUpListener(OnActionUpListener onActionUpListener) {
		this.onActionUpListener = onActionUpListener;
	}

	public interface OnActionUpListener{
		void onActionUp();
	}

	public interface WidgetStateListener {
		public void onMapWidgetCreated(MapWidget map);
	}

	public interface MapStateListener {
		public void onMapOpened(Map map);
	}

	private void onMapOpened() {
		int size = mMapStateListeners.size();
		for (int i = 0; i < size; i++) {
			mMapStateListeners.get(i).onMapOpened(mMap);
		}
	}

	class DecodeBitmapThread extends Thread {
		public void run() {
			loadBitmap();
			mHandler.sendEmptyMessage(EVENT_BITMAP_DECODED);
		}
	}

	class DrawThread extends Thread {
		private SurfaceHolder mSurfaceHolder;

		private boolean mRun = false;

		private Bitmap mBkDrawable = null;

		private Paint mPaint = null;

		private Paint mStrokePaint = new Paint();

		public DrawThread(SurfaceHolder holder, Context context) {
			mSurfaceHolder = holder;
			mPaint = new Paint();
			mStrokePaint.setAntiAlias(true);
			mStrokePaint.setStrokeJoin(Paint.Join.ROUND);
			mStrokePaint.setStrokeCap(Paint.Cap.ROUND);
			mStrokePaint.setColor(Color.BLUE);
			mStrokePaint.setAlpha(192);
			mStrokePaint.setStrokeWidth(10);
			mStrokePaint.setStyle(Paint.Style.STROKE);
		}

		public void run() {
			while (mRun) {
				if (!isRunning) {
					continue;
				}

				Canvas c = null;
				try {
					c = mSurfaceHolder.lockCanvas(null);
					synchronized (mSurfaceHolder) {
						doDraw(c);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						mSurfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}

		public void setNextPosition(int nPos) {
			// mStartIndexPosition = nPos;
		}

		public void setBitmap(Bitmap bmp) {
			mBkDrawable = bmp;
		}

		public void setRunning(boolean flag) {
			mRun = flag;
		}

		public boolean isRunning() {
			return mRun;
		}

		protected Bitmap getBitmap() {
			return mBkDrawable;
		}

		private void doDraw(Canvas canvas) {
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
					Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
			canvas.drawColor(Color.WHITE);
			if (mBkDrawable != null) {
				canvas.drawBitmap(mBkDrawable, mLCPointTransform.getMatrix(),
						mPaint);
			}

			if (drawOnePoint) {
				Marker m = mOneMarker;
				if (m != null)
					m.draw(canvas, mLCPointTransform);
			} else {
				int size = mMarks.size();
				for (int i = 0; i < size; i++) {
					Marker m = mMarks.get(i);
					if (m == null)
						continue;
					m.draw(canvas, mLCPointTransform);
				}
			}
		}
	}
}