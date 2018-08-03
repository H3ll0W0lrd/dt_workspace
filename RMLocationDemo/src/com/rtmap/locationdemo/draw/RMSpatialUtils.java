package com.rtmap.locationdemo.draw;

import android.graphics.PointF;

public class RMSpatialUtils {
	public static float dotMultiply(float x1, float y1, float x2, float y2,// 点积
			float x0, float y0) {
		return ((x1 - x0) * (x2 - x0) + (y1 - y0) * (y2 - y0));
	}

	public static PointF pointToLinePerpendicularFoot(float x, float y,// 点到线的垂足
			float x1, float y1, float x2, float y2) {
		float r = relation(x, y, x1, y1, x2, y2);
		PointF ret = new PointF();
		ret.set(x1 + r * (x2 - x1), y1 + r * (y2 - y1));
		return ret;
	}

	public static float pointToPointDistance(float x1, float y1, float x2,// 点到点的距离

			float y2) {
		return (float) (Math
				.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)));
	}

	/**
	 * 
	 * 方法描述 : 判断点和线段的关系 创建者：玉尧 版本： v1.0 创建时间： 2015年8月18日 下午11:37:08
	 * 
	 * @param x
	 *            点坐标x
	 * @param y
	 *            点坐标y
	 * @param x1
	 *            线段A点坐标x
	 * @param y1
	 *            线段A点坐标y
	 * @param x2
	 *            线段B点坐标x
	 * @param y2
	 *            线段B点坐标y
	 * @return float 返回值为垂足和线段AB的位置 返回值=0 垂足为A 返回值=1 垂足为B 返回值<0 垂足为线段A侧 返回值>1
	 *         垂足为线段B侧 返回值在0到1中间 垂足在线段上
	 * 
	 * 
	 */
	public static float relation(float x, float y, float x1, float y1,// 判断点与线段的关系
																		// return<0
			float x2, float y2) {
		return dotMultiply(x, y, x2, y2, x1, y1)
				/ (pointToPointDistance(x1, y1, x2, y2) * pointToPointDistance(
						x1, y1, x2, y2));
	}

	public static float pointToLineDistance(float x, float y, float x1,// 点到线的距离
			float y1, float x2, float y2, PointF out) {
		float r = relation(x, y, x1, y1, x2, y2);
		if (r < 0) {
			if (out != null) {
				out.set(x1, y1);
			}
			return pointToPointDistance(x, y, x1, y1);
		} else if (r > 1) {
			if (out != null) {
				out.set(x2, y2);
			}
			return pointToPointDistance(x, y, x2, y2);
		}
		PointF foot = pointToLinePerpendicularFoot(x, y, x1, y1, x2, y2);
		if (out != null) {
			out.set(foot);
		}
		return pointToPointDistance(x, y, foot.x, foot.y);
	}

	public static double computeAngle(double a1, double b1, double a2,
			double b2, double a3, double b3) {
		double x1 = a2 - a1;
		double y1 = b2 - b1;
		double x2 = a3 - a1;
		double y2 = b3 - b1;
		double v1 = (x1 * x2) + (y1 * y2);
		double ma_val = Math.sqrt(x1 * x1 + y1 * y1);
		double mb_val = Math.sqrt(x2 * x2 + y2 * y2);
		double cosM = v1 / (ma_val * mb_val);
		double angle = Math.acos(cosM);

		return angle;
	}

	public static int isoneline(double a1, double b1, double a2, double b2,
			double a3, double b3) {
		double x1 = a2 - a1;
		double y1 = b2 - b1;
		double x2 = a3 - a1;
		double y2 = b3 - b1;

		double angle = computeAngle(a1, b1, a2, b2, a3, b3);

		double result = (x2) * (y1) - (y2) * (x1);
		if (Math.abs(angle) < Math.PI + Math.PI / 4
				&& Math.abs(angle) > Math.PI - Math.PI / 4) {

			return 0;
		} else if (Math.abs(angle) < Math.PI / 4) {
			return 2;
		}
		if (result > 0) {
			return 1;
		} else {
			return -1;
		}

	}

	public static double computedistance(double a1, double b1, double a2,
			double b2) {
		double temp_A, temp_B;
		double C; // 用来储存算出来的斜边距离
		temp_A = a1 > a2 ? (a1 - a2) : (a2 - a1); // 横向距离 (取正数，因为边长不能是负数)
		temp_B = b1 > b2 ? (b1 - b2) : (b2 - b1); // 竖向距离 (取正数，因为边长不能是负数)
		C = Math.sqrt(temp_A * temp_A + temp_B * temp_B);
		return C;

	}
}
