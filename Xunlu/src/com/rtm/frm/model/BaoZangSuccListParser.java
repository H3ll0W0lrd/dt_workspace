package com.rtm.frm.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BaoZangSuccListParser {
	String error = "";
	String msg = "";
	ArrayList<BaoZangSuccData> dataArray = new ArrayList<BaoZangSuccData>();
	public BaoZangSuccListParser(String result) {
		try {
			JSONObject mDataObject = new JSONObject(result);
			error = mDataObject.getString("error");
			msg = mDataObject.getString("msg");
			JSONArray jsonArray = mDataObject.getJSONArray("data");
			parseData(jsonArray);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void parseData(JSONArray jsonArray) {
		try {
			
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jso = jsonArray.getJSONObject(i);
				BaoZangSuccData b = new BaoZangSuccData();
				
				b.setPoiName(jso.getString("name_poi"));
				b.setPoiId(jso.getString("id"));
				b.setStatus(jso.getString("status"));
				
				dataArray.add(b);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}		
	}
	
	public ArrayList<BaoZangSuccData> getBZList(){
		return dataArray;
	}
}
