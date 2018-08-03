package com.rtmap.experience.core.model;

import java.io.Serializable;
import java.util.ArrayList;

public class BeaconList implements Serializable{
	
	ArrayList<BeaconInfo> beacons ;
	ArrayList<BeaconInfo> broadcasts;
	public ArrayList<BeaconInfo> getBeacons() {
		return beacons;
	}
	public void setBeacons(ArrayList<BeaconInfo> beacons) {
		this.beacons = beacons;
	}
	public ArrayList<BeaconInfo> getBroadcasts() {
		return broadcasts;
	}
	public void setBroadcasts(ArrayList<BeaconInfo> broadcasts) {
		this.broadcasts = broadcasts;
	}
}
