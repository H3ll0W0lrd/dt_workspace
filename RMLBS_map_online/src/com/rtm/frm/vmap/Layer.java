package com.rtm.frm.vmap;

import java.util.ArrayList;

import android.util.Log;

import com.rtm.frm.map.MapView;
import com.rtm.frm.map.XunluMap;

public class Layer {
	public Node[] nodes;// 节点
	public Edge[] edges;// 边缘
	public Coord[] coords;// 线
	public Shape[] shapes;// 面,矩形
	public Envelope envelope = new Envelope();
	public int angle = 0;

	public Layer() {

	}

	public void clear() {
		nodes = null;
		coords = null;
		shapes = null;
	}

	public synchronized String findShapePoint(int id) {// for wangxin
		String pointString = null;
		for (int i = 0; i < shapes.length; i++) {
			if (shapes[i].mId == id) {
				pointString = String.valueOf(shapes[i].mPoints.length - 1);
				for (int j = 0; j < shapes[i].mPoints.length - 1; j++) {
					pointString += "_" + coords[shapes[i].mPoints[j]].mX + "|"
							+ coords[shapes[i].mPoints[j]].mY;
				}
				break;
			}
		}
		return pointString;
	}

	public synchronized boolean inPolygon(Shape mShape, float x, float y) {

		int nCross = 0;
		int nCount = mShape.mPoints.length;
		for (int j = 0; j < nCount; j++) {
			Coord p1 = coords[mShape.mPoints[j]];
			Coord p2 = coords[mShape.mPoints[(j + 1) % nCount]];
			// 求解 y=p.y 与 p1p2 的交点
			if (p1.mY == p2.mY) // p1p2 与 y=p0.y平行
				continue;
			if (y < Math.min(p1.mY, p2.mY)) // 交点在p1p2延长线上
				continue;
			if (y >= Math.max(p1.mY, p2.mY)) // 交点在p1p2延长线上
				continue;
			// 求交点的 X 坐标
			// --------------------------------------------------------------
			double cx = (double) (y - p1.mY) * (double) (p2.mX - p1.mX)
					/ (double) (p2.mY - p1.mY) + p1.mX;
			if (cx > x)
				nCross++; // 只统计单边交点
		}
		// 单边交点为偶数，点在多边形之外
		return (nCross % 2 == 1);

	}

	public synchronized Coord calculatecenter(Shape mShape) {
		int y = (mShape.maxy + mShape.miny) / 2;
		int nCount = mShape.mPoints.length;
		ArrayList<Double> XList = new ArrayList<Double>();
		for (int j = 0; j < nCount; j++) {
			Coord p1 = coords[mShape.mPoints[j]];
			Coord p2 = coords[mShape.mPoints[(j + 1) % nCount]];
			// 求解 y=p.y 与 p1p2 的交点
			if (p1.mY == p2.mY) // p1p2 与 y=p0.y平行
				continue;
			if (y < Math.min(p1.mY, p2.mY)) // 交点在p1p2延长线上
				continue;
			if (y >= Math.max(p1.mY, p2.mY)) // 交点在p1p2延长线上
				continue;
			// 求交点的 X 坐标
			// --------------------------------------------------------------
			double cx = (double) (y - p1.mY) * (double) (p2.mX - p1.mX)
					/ (double) (p2.mY - p1.mY) + p1.mX;
			XList.add(cx);
		}
		if (!XList.isEmpty()) {
			double distance = 0;
			int index = 0;
			for (int i = 0; i < XList.size(); i += 2) {
				double temp = Math.abs(XList.get(i) - XList.get(i + 1));
				if (temp > distance) {
					distance = temp;
					index = i;
				}
			}
			int x = (int) (XList.get(index) + XList.get(index + 1)) / 2;
			return new Coord(x, y);
		}

		return null;

	}

	public Node[] getNodes() {
		return nodes;
	}

	public void setNodes(Node[] nodes) {
		this.nodes = nodes;
	}

	public Edge[] getEdges() {
		return edges;
	}

	public void setEdges(Edge[] edges) {
		this.edges = edges;
	}

	public Coord[] getCoords() {
		return coords;
	}

	public void setCoords(Coord[] coords) {
		this.coords = coords;
	}

	public Shape[] getShapes() {
		return shapes;
	}

	public void setShapes(Shape[] shapes) {
		this.shapes = shapes;
	}

	public Envelope getEnvelope() {
		return envelope;
	}

	public void setEnvelope(Envelope envelope) {
		this.envelope = envelope;
	}

	public int getAngle() {
		return angle;
	}

	public void setAngle(int angle) {
		this.angle = angle;
	}

	public boolean readmap(String path) {
		XunluMap.getInstance().getLock().lock();
		readmap.init(path);
		angle = readmap.getMapAngle();
		envelope._minx = readmap.readEnve(1);
		envelope._miny = readmap.readEnve(2);
		envelope._maxx = readmap.readEnve(3);
		envelope._maxy = readmap.readEnve(4);

		int nCoordCount = readmap.readmap2Int(1);
		if (nCoordCount > 0) {
			coords = new Coord[nCoordCount];
			for (int i = 0; i < nCoordCount; i++) {
				coords[i] = new Coord();
				coords[i].mX = readmap.readmap2Int(2);
				coords[i].mY = readmap.readmap2Int(3);
			}
		}

		int nShapeCount = readmap.readmap2Int(4);
		if (nShapeCount > 0) {
			shapes = new Shape[nShapeCount];
			for (int i = 0; i < nShapeCount; i++) {
				shapes[i] = new Shape();
				shapes[i].mId = readmap.readmap2Int(5);
				// cout<<"shapeid:"<<id<<endl;
				int size = readmap.readmap2Int(6);
				// cout<<size<<endl;
				if (size > 0) {
					shapes[i].mPoints = new int[size];
					for (int j = 0; j < size; j++) {
						shapes[i].mPoints[j] = readmap.readmap2Int(7);
						// cout<<point<<endl;
					}
				}
				shapes[i].mStyle = readmap.readmap2Int(8);

				// cout<<"style:"<<style<<endl;
				shapes[i].mLevel = readmap.readmap2Int(9);
				shapes[i].mType = shapes[i].mLevel;
				shapes[i].mName = readmap.readmap2Char();
				// mShapes[i].mName=new String(readmap.readmap2Char(),"ASCII");

				shapes[i].setbound(coords);
				shapes[i].mCenter = shapes[i].cg_simple(coords);
				if (!shapes[i].mName.equalsIgnoreCase(null)
						&& !inPolygon(shapes[i], shapes[i].mCenter.mX,
								shapes[i].mCenter.mY)) {
					shapes[i].mCenter = calculatecenter(shapes[i]);
				}
				shapes[i].mDrawStyle = MapView.STYLES.get(shapes[i].mStyle);
				/* mShapes[i].setwidth(null); */
			}
		}
		readmap.closemap();
		XunluMap.getInstance().getLock().unlock();
		return true;
	}
}
