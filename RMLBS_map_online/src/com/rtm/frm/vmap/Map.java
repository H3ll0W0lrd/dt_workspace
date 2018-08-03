package com.rtm.frm.vmap;

import java.io.File;
import java.io.IOException;

public class Map {
	private Layer mLayer = null;
	private String mPath = null;
	private boolean mOpended = false;
	@SuppressWarnings("unused")
	private String mbuildid;
	@SuppressWarnings("unused")
	private String mfloorid;
	@SuppressWarnings("unused")
	private int mVersion;

	public Envelope getEnvelope() {
		return mLayer.envelope;
	}

	public int getLayerCount() {
		return 0;
	}

	public int getAngle() {

		return mLayer.angle;
	}

	public Layer getLayer() {
		if (mLayer == null)
			return null;

		return mLayer;
	}

	public boolean isOpened() {
		return mOpended;
	}

	public int open(String path, String id, String floor) throws IOException {
		if (path == null)
			return getLayerCount();
		if (mPath != null && path.compareTo(mPath) == 0) {
			return getLayerCount();
		}
		try {
			mLayer = new Layer();
			if (mLayer.readmap(path)) {
				mOpended = true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			File file = new File(path);
			file.delete();
			e.printStackTrace();
		}

		return getLayerCount();
	}

	public String getPath() {
		return mPath;
	}

	public void close() {
		if (mLayer != null)
			mLayer.clear();
		// Util.fullGC();
	}

	class Location {
		public int mFloor = 0;
		public int mX = 0;
		public int mY = 0;
	}

}

