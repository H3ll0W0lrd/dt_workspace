package com.rtm.frm.AR;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ARCircleView extends View {
	private Paint paint;
	
	public ARCircleView(Context context) {
		super(context);
	}
	
	public ARCircleView(Context context , AttributeSet attrs) {
		super(context, attrs);   
		paint = new Paint();
		paint.setColor(Color.GRAY);// 设置画笔颜色
		paint.setAlpha(255);// 设置透明度
		paint.setAntiAlias(true);// 设置抗锯齿
		paint.setStrokeWidth(2);// 设置空心线宽
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawCirCle(canvas);
	}
	
	public void drawCirCle(Canvas canvas) {
		paint.setStyle(Paint.Style.STROKE);// 设置空心
		for (int r = 1; r <=3; r++) {
			canvas.drawCircle(ARTestManager.screenWidth / 2, ARTestManager.screenWidth / 2,ARTestManager.screenWidth*r/18, paint);
		}
	}

}
