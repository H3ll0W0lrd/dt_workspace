package com.rtm.frm.utils;

import android.content.Context;

/**
 * 用于运算的数学工具类
 * @author dingtao
 *
 */
public class RMathUtils {

	/**
	 * 计算两点之间的距离
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static float distance(double x1, double y1, double x2, double y2) {
		double x = Math.abs(x1) - Math.abs(x2);
		double y = Math.abs(y1) - Math.abs(y2);
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 * 
	 * @param context
	 * @param spValue
	 * @return
	 */
	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}
}
