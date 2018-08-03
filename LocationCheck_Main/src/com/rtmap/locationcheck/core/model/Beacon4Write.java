package com.rtmap.locationcheck.core.model;

public class Beacon4Write {
	public int oriMajor;
	public int oriMinor;
	public int decodeMajor;
	public int decodeMinor;
	public int rssi;
	public long timeMills;
	
    public Beacon4Write(int oriMajor, int oriMinor, int decodeMajor, int decodeMinor, int rssi, long timeMills) {
        super();
        this.oriMajor = oriMajor;
        this.oriMinor = oriMinor;
        this.decodeMajor = decodeMajor;
        this.decodeMinor = decodeMinor;
        this.rssi = rssi;
        this.timeMills = timeMills;
    }

    public Beacon4Write() {
        super();
    }
	
	
}
