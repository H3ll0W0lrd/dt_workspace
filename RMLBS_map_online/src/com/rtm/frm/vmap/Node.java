package com.rtm.frm.vmap;

public class Node {
	public final static int NODE_TYPE_NORMAL = 0;
	public final static int NODE_TYPE_ENTRANCE = 1;
	public final static int NODE_TYPE_PARKPLACE = 2;
	public final static int NODE_TYPE_ELEVATOR = 3;

	public int mId;
	public int mPointId;
	public int[] mEdges = null;
	public int mType = 0;
	public int mLayerId = 0;
	public char[] mName = new char[32];
	public char[] mNaviName = new char[32];
	public int mZone;
	public int mLevel;
	public int[] mExt = new int[3];

	public String toString() {
		return String.valueOf(mName).trim();
	}
}
