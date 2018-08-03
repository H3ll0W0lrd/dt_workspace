package com.rtm.location.entity;

public class MacRssEntity {
	public String mac;
	public int rss;
	public int count;
	public Type chennal;

	public MacRssEntity(String mac_, int rss_, Type chennal_) {
		count = 1;
		mac = mac_;
		rss = rss_;
		chennal = chennal_;
	}
}
