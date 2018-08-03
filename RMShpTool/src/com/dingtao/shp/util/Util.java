package com.dingtao.shp.util;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.dingtao.shp.model.RMLine;
import com.dingtao.shp.model.RMPoi;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class Util {
	public static boolean line2Shp(String mappath, ArrayList<RMLine> lineList) {
		String filepath = mappath + ".shp";
		try {
			File file = new File(filepath);
			java.util.Map<String, Serializable> params = new HashMap<String, Serializable>();
			params.put(ShapefileDataStoreFactory.URLP.key, file.toURI().toURL());
			ShapefileDataStore ds = (ShapefileDataStore) new ShapefileDataStoreFactory()
					.createNewDataStore(params);
			SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
			tb.setName("shapefile");
			tb.add("the_geom", LineString.class);
			tb.add("name_CHN", String.class);
			tb.add("desc", String.class);
			ds.createSchema(tb.buildFeatureType());
			ds.setCharset(Charset.forName("GBK"));
			FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds
					.getFeatureWriter(ds.getTypeNames()[0],
							Transaction.AUTO_COMMIT);
			GeometryFactory gf = new GeometryFactory();
			try {
				HashMap<String, ArrayList<RMPoi>> map = new HashMap<String, ArrayList<RMPoi>>();
				for (RMLine line : lineList) {
					if (line != null) {
						if (line.getPoiList().size() > 1) {
							Coordinate[] coords = new Coordinate[line
									.getPoiList().size()];
							SimpleFeature feature = writer.next();
							feature.setAttribute("name_CHN",
									subSequence(line.getName()));
							feature.setAttribute("desc",
									subSequence(line.getDesc()));
							for (int i = 0; i < line.getPoiList().size(); i++) {
								coords[i] = new Coordinate(line.getPoiList()
										.get(i).getX(), -line.getPoiList()
										.get(i).getY());
								// point
								// lineString
							}
							Geometry g = gf.createLineString(coords);
							feature.setAttribute("the_geom", g);
							writer.write();
						} else if (line.getPoiList().size() == 1) {
							String map_first = mappath.substring(0,
									mappath.lastIndexOf(File.separator))
									+ "_poi" + File.separator;
							String key = map_first
									+ mappath.substring(mappath
											.lastIndexOf(File.separator) + 1);
							System.out.println("key :" + key);
							if (map.containsKey(key)) {
								line.getPoiList().get(0)
										.setDesc(line.getDesc());
								map.get(key).addAll(line.getPoiList());
							} else {
								ArrayList<RMPoi> poiList = new ArrayList<RMPoi>();
								File file1 = new File(map_first);
								System.out.println(file1.mkdirs());
								poiList.addAll(line.getPoiList());
								map.put(key, poiList);
							}
						}
					}
				}
				if (map.size() > 0) {
					Set<String> keys = map.keySet();
					Iterator<String> it = keys.iterator();
					while (it.hasNext()) {
						String k = (String) it.next();
						point2Shp(k, (ArrayList<RMPoi>) map.get(k));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				writer.close();
				ds.dispose();
			}
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean point2Shp(String mappath, ArrayList<RMPoi> poilist) {
		String filepath = mappath + ".shp";
		try {
			File file = new File(filepath);
			java.util.Map<String, Serializable> params = new HashMap<String, Serializable>();
			params.put(ShapefileDataStoreFactory.URLP.key, file.toURI().toURL());
			ShapefileDataStore ds = (ShapefileDataStore) new ShapefileDataStoreFactory()
					.createNewDataStore(params);
			SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
			tb.setName("shapefile");
			tb.add("the_geom", Point.class);
			tb.add("name_CHN", String.class);
			tb.add("desc", String.class);
			ds.createSchema(tb.buildFeatureType());
			ds.setCharset(Charset.forName("GBK"));
			FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds
					.getFeatureWriter(ds.getTypeNames()[0],
							Transaction.AUTO_COMMIT);
			GeometryFactory gf = new GeometryFactory();
			try {
				for (int i = 0; i < poilist.size(); i++) {
					SimpleFeature feature = writer.next();
					feature.setAttribute("name_CHN", subSequence(poilist.get(i)
							.getName()));
					feature.setAttribute("desc", subSequence(poilist.get(i)
							.getDesc()));
					Coordinate coord = new Coordinate(poilist.get(i).getX(),
							-poilist.get(i).getY());
					// point
					// lineString
					System.out.println("X : " + poilist.get(i).getX()
							+ "    Y : " + poilist.get(i).getY());
					Geometry g = gf.createPoint(coord);
					feature.setAttribute("the_geom", g);
					writer.write();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				writer.close();
				ds.dispose();
			}

			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String subSequence(String str) {

		if (str == null || str.length() == 0) {
			return null;
		}

		try {
			return truncate(str, 253);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return str;

	}

	public static Polygon createPolygonByWKT(String polygonMessage)
			throws ParseException {

		GeometryFactory geometryFactory = JTSFactoryFinder
				.getGeometryFactory(null);
		WKTReader reader = new WKTReader(geometryFactory);
		polygonMessage = polygonMessage.replace("MULTIPOLYGON(((", "POLYGON((")
				.replace(")))", "))");
		Polygon polygon = (Polygon) reader.read(polygonMessage);
		return polygon;
	}

	public static String truncate(String s, int n, String encodeName)
			throws UnsupportedEncodingException {
		if (s == null || s.isEmpty()) {
			throw new NullPointerException(
					"StringUtils: truncate(String s, int n), 鍙傛暟s涓嶈兘涓虹┖锛�");
		}
		if (n <= 0) {
			throw new ArrayIndexOutOfBoundsException(
					"StringUtils: truncate(String s, int n), 鍙傛暟n涓嶈兘涓鸿礋鏁�!");
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
