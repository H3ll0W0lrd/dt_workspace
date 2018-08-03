package com.rtmap.experience.util.map;

public class Node implements Serializable {
    public final static int NODE_TYPE_NORMAL = 0;
    public final static int NODE_TYPE_ENTRANCE = 1;
    public final static int NODE_TYPE_PARKPLACE = 2;
    public final static int NODE_TYPE_ELEVATOR = 3;

    public int mId;
    public int mPointId;
    public int[] mEdges = null;
    public int mType = 0;
    public int mLayerId = 0;// 所在的层
    public char[] mName = new char[32];
    public int[] mExt = new int[3];

    /* (non-Javadoc)
     * 
     * @see com.rtmap.util.RTMSerializable#deserialize(byte[], int) */
    @Override
    public int deserialize(byte[] b, int from) {
        if (b == null)
            return 0;
        int length = getByteSize();
        if (b.length < from + length)
            return 0;

        int nPos = from;
        mId = SerializeTool.getInt(b, nPos);
        nPos += 4;
        mPointId = SerializeTool.getInt(b, nPos);
        nPos += 4;
        int nSize = SerializeTool.getInt(b, nPos);
        nPos += 4;
        if (nSize > 0) {
            mEdges = new int[nSize];
            for (int i = 0; i < nSize; i++) {
                mEdges[i] = SerializeTool.getInt(b, nPos);
                nPos += 4;
            }
        }
        mType = SerializeTool.getInt(b, nPos);
        nPos += 4;
        mLayerId = SerializeTool.getInt(b, nPos);
        nPos += 4;
        nPos += SerializeTool.getCharArray(b, nPos, mName);
        mExt[0] = SerializeTool.getInt(b, nPos);
        nPos += 4;
        mExt[1] = SerializeTool.getInt(b, nPos);
        nPos += 4;
        mExt[2] = SerializeTool.getInt(b, nPos);
        nPos += 4;
        return nPos - from;
    }

    /* (non-Javadoc)
     * 
     * @see com.rtmap.util.RTMSerializable#getByteSize() */
    @Override
    public int getByteSize() {
        int nRet = 28;
        if (mEdges != null) {
            nRet += (mEdges.length * 4);
        }
        return nRet;
    }
}