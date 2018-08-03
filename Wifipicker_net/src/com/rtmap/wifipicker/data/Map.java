package com.rtmap.wifipicker.data;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Map {
	private Metadata mMetadata = null;
	private Layer[] mLayers = null;
	private String mPath = null;
	private boolean mOpended = false;
	
	public Map(){
		mMetadata = new Metadata();
	}
	
	public int getVersion(){
		return mMetadata.mVersion;
	}
	
	public Envelope getEnvelope(){
		return mMetadata.mEnvelope;
	}
	
	public int getLayerCount(){
		return mMetadata.mLayerCount;
	}
	
	public int getLongitude(){
		return mMetadata.mLongitude;
	}
	
	public int getLatitude(){
		return mMetadata.mLatitude;
	}
	
	public Layer getLayer(int index){
		if(mLayers == null)
			return null;
		if(index < 0 || index >= mLayers.length)
			return null;
		
		return mLayers[index];
	}
	
	public boolean isOpened(){
		return mOpended;
	}
	
	public int open(String path) throws IOException {
		if(path == null)
			return getLayerCount();
		if(mPath != null && path.compareTo(mPath) == 0){
			return getLayerCount();
		}
		RandomAccessFile indexFile = new RandomAccessFile(path, "rw");
		if(indexFile.length() >= Integer.MAX_VALUE)
			return 0;
		mPath = path;
		byte[] buf = new byte[(int) indexFile.length()];
		indexFile.read(buf);
		int nPos = mMetadata.deserialize(buf, 0);
		if(mMetadata.mLayerCount > 0){
			mLayers = new Layer[mMetadata.mLayerCount];
			for(int i = 0; i < mMetadata.mLayerCount; i++){
				mLayers[i] = new Layer();
				nPos += mLayers[i].deserialize(buf, nPos);
			}
		}
		indexFile.close();
		buf = null;
		mOpended = true;
		return getLayerCount();
	}
	
	public String getName(){
		return String.valueOf(mMetadata.mName).trim();
	}
	
	public String getPath(){
		return mPath;
	}
	
	public void close() {
		for (int i = 0; i < mMetadata.mLayerCount; i++) {
			if (mLayers[i] != null)
				mLayers[i].clear();
		}
		mLayers = null;
//		Util.fullGC();
	}
	
	class Location {
		public int mFloor = 0;
		public int mX = 0;
		public int mY = 0;
	}
	
	class Metadata implements Serializable {
		public final static int length = 116;
		public int mVersion = 1000;
		public Envelope mEnvelope = new Envelope();
		public int mLayerCount = 0;
		public char[] mName = new char[32];
		public int mLongitude = 0;
		public int mLatitude = 0;
		public int[] mExt = new int[3];
		
		public int deserialize(byte[] b, int from) {
			if(b == null)
				return 0;
			if(b.length < length + from)
				return 0;
			
			int nPos = from;
			mVersion = SerializeTool.getInt(b, nPos);
			nPos+=4;
			nPos += mEnvelope.deserialize(b, nPos);
			
			mLayerCount = SerializeTool.getInt(b, nPos);
			nPos += 4;
			
			nPos += SerializeTool.getCharArray(b, nPos, mName);
			
			mLongitude = SerializeTool.getInt(b, nPos);
			nPos += 4;
			
			mLatitude = SerializeTool.getInt(b, nPos);
			nPos += 4;
			
			for(int i = 0; i < 3; i++){
				mExt[i] = SerializeTool.getInt(b, nPos);
				nPos += 4;
			}
			return length;
		}

		public int getByteSize() {
			return length;
		}
	}
}
