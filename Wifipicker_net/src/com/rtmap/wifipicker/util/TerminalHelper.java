package com.rtmap.wifipicker.util;

import java.util.ArrayList;

import com.rtmap.wifipicker.data.Airport;
import com.rtmap.wifipicker.data.Terminal;
import com.rtmap.wifipicker.model.AirportModel;

public class TerminalHelper {
	private static AirportModel getAirportModel() {
		String data = loadTerminalData();
		
		try {
			return new AirportModel(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Terminal getTerminalById(String id) {
		AirportModel model = getAirportModel();
		if (model != null) {
		    return model.getTerminalById(id);
        }
		return null;
	}
	
	public static ArrayList<Airport> getAirports() {
		AirportModel model = getAirportModel();
		return model.getAirports();
	}
	
	public static String loadTerminalData() {
		String data = FileHelper.getTextFromAssets("api_buildlist_all_3.xml");;
		return data;
	}
}
