package com.rtmap.experience.util;


public class DTMathUtils {
	
	/**
	 * 计算两点之间的距离
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static float distance(double x1,double y1,double x2,double y2) {
		double x = x1-x2;
		double y = y1-y2;
		DTLog.e("x1 : "+x1+"   y1: "+y1+"   x2:"+x2+"   y2 : "+y2);
		return (float)Math.sqrt(x * x + y * y);
	}
}
