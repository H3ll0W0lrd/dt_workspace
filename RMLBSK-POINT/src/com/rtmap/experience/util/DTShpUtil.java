package com.rtmap.experience.util;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;

import android.net.ParseException;

public class DTShpUtil {
//	public static boolean ResultSet2Shp(String datasetName, String userName,
//			String passWord, String buildIdString, String floorIdString,
//			String filepath, String shpName) {
//		filepath = filepath + shpName + ".shp";
//		try {
//			// 创建shape文件对象
//			File file = new File(filepath);
//			java.util.Map<String, Serializable> params = new HashMap<String, Serializable>();
//			params.put(ShapefileDataStoreFactory.URLP.key, file.toURI().toURL());
//			ShapefileDataStore ds = (ShapefileDataStore) new ShapefileDataStoreFactory()
//					.createNewDataStore(params);
//			// 定义图形信息和属性信息
//			SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
//			// CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326",
//			// true) ;
//			tb.setCRS(DefaultGeographicCRS.WGS84);
//			tb.setName("shapefile");
//			tb.add("style", Long.class);
//			tb.add("the_geom", Polygon.class);
//			tb.add("poi_no", Long.class);
//			tb.add("name_CHN", String.class);
//			tb.add("two_class", String.class);
//			tb.add("level", Long.class);
//			tb.add("type", Long.class);
//			tb.add("search_out", String.class);
//			tb.add("uses", String.class);
//			tb.add("address", String.class);
//			tb.add("phone", String.class);
//			tb.add("introd", String.class);
//			tb.add("time_open", String.class);
//			tb.add("currency", String.class);
//			tb.add("rjxf", String.class);
//			tb.add("dianti_ids", String.class);
//			tb.add("road_ids", String.class);
//			tb.add("zone_one", Long.class);
//			tb.add("zone_two", Long.class);
//			tb.add("zone_three", Long.class);
//			ds.createSchema(tb.buildFeatureType());
//			ds.setCharset(Charset.forName("GBK"));
//			// 设置Writer
//			FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds
//					.getFeatureWriter(ds.getTypeNames()[0],
//							Transaction.AUTO_COMMIT);
//			// 写下一条
//
//			try {
//				while (rSet.next()) {
//					SimpleFeature feature = writer.next();
//					feature.setAttribute("poi_no", rSet.getInt("poi_no"));
//					feature.setAttribute("two_class",
//							rSet.getString("two_class"));
//					feature.setAttribute("level", rSet.getString("two_class"));
//					// System.out.println(rSet.getInt("poi_no"));
//					feature.setAttribute("style", rSet.getInt("style"));
//					feature.setAttribute("name_CHN",
//							subSequence(rSet.getString("name_chinese")));
//					feature.setAttribute("search_out",
//							subSequence(rSet.getString("search_out")));
//					feature.setAttribute("uses",
//							subSequence(rSet.getString("uses")));
//					feature.setAttribute("address",
//							subSequence(rSet.getString("address")));
//					feature.setAttribute("phone",
//							subSequence(rSet.getString("phone")));
//					feature.setAttribute("introd",
//							subSequence(rSet.getString("introd")));
//					feature.setAttribute("time_open",
//							subSequence(rSet.getString("time_open")));
//					feature.setAttribute("currency",
//							subSequence(rSet.getString("currency")));
//					feature.setAttribute("rjxf",
//							subSequence(rSet.getString("rjxf")));
//					feature.setAttribute("dianti_ids",
//							subSequence(rSet.getString("dianti_ids")));
//					feature.setAttribute("road_ids",
//							subSequence(rSet.getString("road_ids")));
//					feature.setAttribute("zone_one", rSet.getInt("zone_one"));
//					feature.setAttribute("zone_two", rSet.getInt("zone_two"));
//					feature.setAttribute("zone_three",
//							rSet.getInt("zone_three"));
//					feature.setAttribute("the_geom", createPolygonByWKT(rSet
//							.getString("AsText(the_geom)")));
//
//					writer.write();
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				writer.close();
//				ds.dispose();
//			}
//
//			return true;
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return false;
//	}

	public static String subSequence(String str) {

		if (str == null || str.length() == 0) {
			return null;
		}

		try {
			return truncate(str, 253);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;

	}

//	public static Polygon createPolygonByWKT(String polygonMessage)
//			throws ParseException {
		// LineString 线
		// Point 点
		// Polygon 面
//		GeometryFactory geometryFactory = JTSFactoryFinder
//				.getGeometryFactory(null);
//		WKTReader reader = new WKTReader(geometryFactory);
//		polygonMessage = polygonMessage.replace("MULTIPOLYGON(((", "POLYGON((")
//				.replace(")))", "))");
//		Polygon polygon = null;
//		try {
//			polygon = (Polygon) reader.read(polygonMessage);
//		} catch (com.vividsolutions.jts.io.ParseException e) {
//			e.printStackTrace();
//		}
//		return polygon;
//	}

	public static boolean shp2Zip(String shpPath, String zipPath, String shpName) {
		File file = new File(zipPath);
		if (file.exists()) {
			file.delete();
		}
		try {
//			CompressUtil.zip(shpPath + shpName + ".shp", zipPath, null);
//			CompressUtil.zip(shpPath + shpName + ".shx", zipPath, null);
//			CompressUtil.zip(shpPath + shpName + ".dbf", zipPath, null);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	public static String truncate(String s, int n, String encodeName)
			throws UnsupportedEncodingException {
		if (DTStringUtils.isEmpty(s)) {
			throw new NullPointerException(
					"StringUtils: truncate(String s, int n), 参数s不能为空！");
		}
		if (n <= 0) {
			throw new ArrayIndexOutOfBoundsException(
					"StringUtils: truncate(String s, int n), 参数n不能为负数!");
		}
		if (n > s.getBytes(encodeName).length) {
			n = s.getBytes(encodeName).length;
		}
		byte[] resultBytes = new byte[n];
		int j = 0;
		for (int i = 0; i < s.length(); i++) {
			byte[] bytes = String.valueOf(s.charAt(i)).getBytes(encodeName);
			if (bytes.length <= n - j) {
				for (int k = 0; k < bytes.length; k++) {
					resultBytes[j] = bytes[k];
					j++;
				}
			} else {
				break;
			}
		}
		return new String(resultBytes, 0, j, encodeName);
	}

	public static String truncate(String s, int n)
			throws UnsupportedEncodingException {
		return truncate(s, n, Charset.defaultCharset().toString());
	}
}
