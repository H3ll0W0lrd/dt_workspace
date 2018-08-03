package com.rtm.frm.vmap;


public class readmap {
    public synchronized static native int readmap2Int(int classid);
    public synchronized static native void init(String filename);
    public synchronized static native String readmap2Char();
    public synchronized static native void closemap();
    public synchronized static native int readEnve(int id);
    public synchronized static native int getFileVersion(String filename);
    public synchronized static native String soVersion();
    public synchronized static native int getMapAngle();
    static{
        System.loadLibrary("MapInterfaceSo");
    }
}
