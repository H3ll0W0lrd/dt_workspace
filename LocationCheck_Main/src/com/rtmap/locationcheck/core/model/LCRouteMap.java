package com.rtmap.locationcheck.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class LCRouteMap implements Serializable{
	HashMap<String, ArrayList<LCPoint>> routeMap;

	public HashMap<String, ArrayList<LCPoint>> getRouteMap() {
		return routeMap;
	}

	public void setRouteMap(HashMap<String, ArrayList<LCPoint>> routeMap) {
		this.routeMap = routeMap;
	}
}
