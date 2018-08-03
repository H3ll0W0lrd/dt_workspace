package com.rtmap.locationcheck.util.map;


public class Layer implements Serializable {
	private int mLength = 0;
	private byte[][] mBitmaps;
	private Metadata mMetadata;
	
	public Node[] mNodes;
	public Edge[] mEdges;
	public Coord[] mCoords;
	
	public Layer(){
		mMetadata = new Metadata();
	}
	
	public String toString(){
		return String.valueOf(mMetadata.mName).trim();
	}
	
	public void clear(){
		mBitmaps = null;
		mNodes = null;
		mCoords = null;
		mLength = 0;
	}
	
	@Override
	public int deserialize(byte[] b, int from) {
		if(b == null)
			return 0;
		int nPos = from;
		// 1.����Ԫ���
		nPos += mMetadata.deserialize(b, nPos);
		// 2.����դ���ͼ
		if(mMetadata.mBitmapCount > 0/* && mMetadata.mBitmapCount <= 3*/){
			mBitmaps = new byte[mMetadata.mBitmapCount][];
			for(int i = 0; i < mMetadata.mBitmapCount; i++){
				if(mMetadata.mBitmapSize[i] > 0){
					mBitmaps[i] = new byte[mMetadata.mBitmapSize[i]];
					nPos += SerializeTool.getBytes(b, nPos, mBitmaps[i]);
				}
			}
		}
		// 3.�����������
		int nNodeCount = SerializeTool.getInt(b, nPos); nPos += 4;
		if(nNodeCount > 0){
			mNodes = new Node[nNodeCount];
			for(int i = 0; i < nNodeCount; i++){
				mNodes[i] = new Node();
				nPos += mNodes[i].deserialize(b, nPos);
			}
		}
		int nEdgeCount = SerializeTool.getInt(b, nPos); nPos += 4;
		if(nEdgeCount > 0){
			mEdges = new Edge[nEdgeCount];
			for(int i = 0; i < nEdgeCount; i++){
				mEdges[i] = new Edge();
				nPos += mEdges[i].deserialize(b, nPos);
			}
		}
		int nCoordCount = SerializeTool.getInt(b, nPos); nPos += 4;
		if(nCoordCount > 0){
			mCoords = new Coord[nCoordCount];
			for(int i = 0; i < nCoordCount; i++){
				mCoords[i] = new Coord();
				nPos += mCoords[i].deserialize(b, nPos);
			}
		}
		return (mLength = nPos - from);
	}

	@Override
	public int getByteSize() {
		return mLength;
	}
	
	public int getBitmapCount(){
		return mMetadata.mBitmapCount;
	}
	
	public byte[] getBitmap(int index){
		if(mBitmaps == null)
			return null;
		if(index < 0 || index >= mBitmaps.length)
			return null;
		return mBitmaps[index];
	}
	
	public Envelope getEnvelope(){
		return mMetadata.mBox;
	}
	
	public String getName(){
		return String.valueOf(mMetadata.mName).trim();
	}
	
	public int findNode(int x, int y){
		if(mCoords == null)
			return -1;
		double fMinDistance = 0.0;
		boolean first = true;
		int nNode = -1;
		for(int i = 0; i < mNodes.length; i++){
			if(mNodes[i] == null)
				continue;
			if(mNodes[i].mPointId < 0 || mNodes[i].mPointId >= mCoords.length)
				continue;
			if(mCoords[mNodes[i].mPointId] == null)
				continue;
			// ��������Node
			// �����޳�̫Զ��NODE
			int dx = mCoords[mNodes[i].mPointId].mX - x;
			int dy = mCoords[mNodes[i].mPointId].mY - y;
			if(Math.abs(dx) > 10000 || Math.abs(dy) > 10000)
				continue;
			double d = dx*dx+dy*dy;
			if(first){
				fMinDistance = d;
				first = false;
				nNode = i;
			}
			else{
				if(d < fMinDistance){
					fMinDistance = d;
					nNode = i;
				}
			}
		}
		return nNode;
	}
	
	class Metadata implements Serializable {
		public final static int length = 120;
		public char[] mName = new char[32];
		public Envelope mBox = new Envelope();
		public int mFilePos = 0;
		public int mBitmapCount = 0;
		public int[] mBitmapSize = new int[4];
		public int[] mExt = new int[3];
		@Override
		public int deserialize(byte[] b, int from) {
			if(b == null)
				return 0;
			if(b.length < length + from)
				return 0;
			
			int nPos = from;
			nPos += SerializeTool.getCharArray(b, nPos, mName);
			
			nPos += mBox.deserialize(b, nPos);
			
			mFilePos = SerializeTool.getInt(b, nPos);
			nPos += 4;
			
			mBitmapCount = SerializeTool.getInt(b, nPos);
			nPos += 4;
			
			for(int i = 0; i < 3; i++){
				mBitmapSize[i] = SerializeTool.getInt(b, nPos);
				nPos += 4;
			}
			for(int i = 0; i < 3; i++){
				mExt[i] = SerializeTool.getInt(b, nPos);
				nPos += 4;
			}
			return length;
		}

		@Override
		public int getByteSize() {
			return length;
		}	
	}

}
