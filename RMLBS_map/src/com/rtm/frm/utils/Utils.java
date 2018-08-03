package com.rtm.frm.utils;


public class Utils {

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
