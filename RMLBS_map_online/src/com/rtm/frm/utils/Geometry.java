package com.rtm.frm.utils;

import com.rtm.frm.model.Location;

public class Geometry {
	public static double pointToLine(float f, float g, float h, float i, float j,
            float k) {
         double space = 0;
         double a, b, c;

         a = lineSpace(f, g, h, i);// 线段的长度
         b = lineSpace(f, g, j, k);// (x1,y1)到点的距离
         c = lineSpace(h, i, j, k);// (x2,y2)到点的距离
         if (c <= 0.000001 || b <= 0.000001) {
            space = 0;
            return space;
         }
         if (a <= 0.000001) {
            space = b;
            return space;
         }
         if (c * c >= a * a + b * b) {
            space = b;
            return space;
         }
         if (b * b >= a * a + c * c) {
            space = c;
            return space;
         }
         double p = (a + b + c) / 2;// 半周长
         double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));// 海伦公式求面积
         space = 2 * s / a;// 返回点到线的距离（利用三角形面积公式求高）
         return space;
     }
     // 计算两点之间的距离
	public static double lineSpace(float f, float g, float h, float i) {
         double lineLength = 0;
         lineLength = Math.sqrt((f - h) * (f - h) + (g - i)
                * (g - i));
         return lineLength;
     }

	public static Location projectpoint(float f, float g, float h, float i, float j,
            float k) {
		 double a, b, c;
         Location mlLocation;
         a = lineSpace(f, g, h, i);// 线段的长度
         b = lineSpace(f, g, j, k);// (x1,y1)到点的距离
         c = lineSpace(h, i, j, k);// (x2,y2)到点的距离
         if (c <= 0.000001 || b <= 0.000001) {
            mlLocation=new Location(j, k);
            return mlLocation;
         }
         if (a <= 0.000001) {
        	 mlLocation=new Location(f, g);
             return mlLocation;
         }
         if (c * c >= a * a + b * b) {
        	 mlLocation=new Location(f, g);
             return mlLocation;
         }
         if (b * b >= a * a + c * c) {
        	 mlLocation=new Location(h,i);
             return mlLocation;
         }
		if(f==h){
			 mlLocation=new Location(h,k);
             return mlLocation;
		}
		if(g==i){
			 mlLocation=new Location(j,i);
            return mlLocation;
		}
		double Linek=(i-g)/(h-f);
		float x=(float)((Linek * f + j / Linek + k - g) / (1 / Linek + Linek));
		float y=(float)(-1 / Linek * (x - j) + k);
		 mlLocation=new Location(x,y);
         return mlLocation;
	}

}
