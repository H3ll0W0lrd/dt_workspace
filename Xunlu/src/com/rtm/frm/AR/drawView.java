package com.rtm.frm.AR;

import com.rtm.frm.utils.XunluUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.view.View;

public class drawView extends View {
	private final Paint mGesturePaint = new Paint();
	private final Path mPath = new Path();
	public static final float lineWidth1 = 40f;
	public static final float linewidth2 = 100f;	
	public Context mContext;
	public drawView(Context context) {
		super(context);
		mContext = context;

		mGesturePaint.setAntiAlias(true);
		// mGesturePaint.setStyle(Style.STROKE);
		mGesturePaint.setStyle(Style.FILL);
		mGesturePaint.setStrokeWidth(5);
		mGesturePaint.setColor(Color.rgb(229, 19, 69));
		mGesturePaint.setAlpha(153);

	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		
		mPath.moveTo(ARTestManager.screenWidth / 2 - linewidth2 / 2, ARTestManager.screenHeight);//
		mPath.lineTo(ARTestManager.screenWidth / 2 + linewidth2 / 2, ARTestManager.screenHeight);//
		mPath.lineTo(ARTestManager.navix + lineWidth1 / 2, ARTestManager.naviy);//
		mPath.lineTo(ARTestManager.navix - lineWidth1 / 2, ARTestManager.naviy);
		mPath.close();
		canvas.drawPath(mPath, mGesturePaint);

	}
	
}
