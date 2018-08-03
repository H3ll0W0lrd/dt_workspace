package com.baidu.push.util;


public class MacRssEntity {
    public String mac;
    public int rss;
    public int count;
    public Type chennal;

    public enum Type {
        channel_24, channel_5, ibeacon
    };
    
    public MacRssEntity(String mac_, int rss_, Type chennal_) {
        count = 1;
        mac = mac_;
        rss = rss_;
        chennal = chennal_;
    }
}