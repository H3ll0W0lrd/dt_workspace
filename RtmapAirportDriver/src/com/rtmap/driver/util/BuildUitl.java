package com.rtmap.driver.util;

public class BuildUitl {

	/**
	 * 楼层换算
	 * 
	 * @param floor
	 * @return
	 */
	public static int floorTransform(String floor) {
		int a = 0;
		String str1 = floor.substring(0, 1);
		if (floor.contains(".5")) {
			a += Integer.parseInt(floor.substring(1, floor.indexOf("."))) * 10 + 5;
		} else {
			a += Integer.parseInt(floor.substring(1)) * 10;
		}

		if ("B".equals(str1)) {
			a += 10000;
		} else if ("F".equals(str1)) {
			a += 20000;
		}
		return a;
	}

	/**
	 * 楼层换算:10000是B,20000是F,剩下的数是楼层的10倍
	 * 
	 * @param floor
	 * @return
	 */
	public static String floorTransform(int floor) {
		String a = null;
		if (floor / 10000 == 1)
			a = "B";
		else
			a = "F";
		int f = floor % 10;// 看有没有半层
		if (f != 0)
			a += floor % 10000 / 10f;
		else
			a += floor % 10000 / 10;
		return a;
	}
}
