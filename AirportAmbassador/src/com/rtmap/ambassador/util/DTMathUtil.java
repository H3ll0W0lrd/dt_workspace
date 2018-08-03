package com.rtmap.ambassador.util;

import java.util.ArrayList;

import com.rtm.frm.model.Location;

public class DTMathUtil {
	/**
	 * 检查多边形是否包含了某点~
	 * 
	 * @param point
	 * @return
	 */
	public static boolean containsPoint(float x, float y,
			ArrayList<Location> list) {
		int verticesCount = list.size();
		int nCross = 0;
		for (int i = 0; i < verticesCount; ++i) {
			Location p1 = list.get(i);
			Location p2 = list.get((i + 1) % verticesCount);

			// 求解 y=p.y 与 p1 p2 的交点
			if (p1.getY() == p2.getY()) { // p1p2 与 y=p0.y平行
				continue;
			}
			if (y < Math.min(p1.getY(), p2.getY())) { // 交点在p1p2延长线上
				continue;
			}
			if (y >= Math.max(p1.getY(), p2.getY())) { // 交点在p1p2延长线上
				continue;
			}
			// 求交点的 X 坐标
			float x1 = (y - p1.getY()) * (p2.getX() - p1.getX())
					/ (p2.getY() - p1.getY()) + p1.getX();
			if (x1 > x) { // 只统计单边交点
				nCross++;
			}
		}
		// 单边交点为偶数，点在多边形之外
		return (nCross % 2 == 1);
	}
}
