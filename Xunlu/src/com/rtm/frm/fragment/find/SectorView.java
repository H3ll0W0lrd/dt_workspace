package com.rtm.frm.fragment.find;

import com.rtm.frm.fragment.controller.FindManager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class SectorView extends View {
	private Paint paint;
	int startAngle, sweetAngle;// 起始角度，扫描角度
	int currentAngle = 0;// 当前真实角度
	boolean flag = true;
	
	public SectorView(Context context) {
		super(context);
	}
	
	public SectorView(Context context , AttributeSet attrs) {
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
			canvas.drawCircle(FindManager.width / 2, FindManager.height / 2,FindManager.height*r/6, paint);
		}
	}

}
