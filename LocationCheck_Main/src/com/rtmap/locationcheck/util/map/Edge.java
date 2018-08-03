package com.rtmap.locationcheck.util.map;


public class Edge implements Serializable {
    public int mId;
    public int mFromNodeId;
    public int mToNodeId;
    public int mWeight;
    public int[] mPointIds;
    public int nLayerId = 0;// 所在的层
    public int[] mExt = new int[3];
    
    /* (non-Javadoc)
     * @see com.rtmap.util.RTMSerializable#deserialize(byte[], int)
     */
    @Override
    public int deserialize(byte[] b, int from) {
        if(b == null)
            return 0;
        int length = getByteSize();
        if(b.length < from + length)
            return 0;

        int nPos = from;
        mId = SerializeTool.getInt(b, nPos); nPos += 4;
        mFromNodeId = SerializeTool.getInt(b, nPos); nPos += 4;
        mToNodeId = SerializeTool.getInt(b, nPos); nPos += 4;
        mWeight = SerializeTool.getInt(b, nPos); nPos += 4;
        int nSize = SerializeTool.getInt(b, nPos); nPos += 4;
        if(nSize > 0){
            mPointIds = new int[nSize]; 
            for(int i = 0; i < nSize; i++){
                mPointIds[i] = SerializeTool.getInt(b, nPos); nPos += 4;
            }
        }
        nLayerId = SerializeTool.getInt(b, nPos); nPos += 4;
        mExt[0] = SerializeTool.getInt(b, nPos); nPos += 4;
        mExt[1] = SerializeTool.getInt(b, nPos); nPos += 4;
        mExt[2] = SerializeTool.getInt(b, nPos); nPos += 4;
        return nPos - from;
    }

    /* (non-Javadoc)
     * @see com.rtmap.util.RTMSerializable#getByteSize()
     */
    @Override
    public int getByteSize() {
        int nRet = 28;
        if(mPointIds != null)
            nRet += (mPointIds.length * 4);
        return nRet;
    }
    
}