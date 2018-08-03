package com.rtmap.wifipicker.model;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rtmap.wifipicker.data.Airport;
import com.rtmap.wifipicker.data.Terminal;
import com.rtmap.wifipicker.util.XmlHelper;

public class AirportModel {
	
	private ArrayList<Airport> mAirports;
	
	public AirportModel(String data) throws Exception {
		Element element = XmlHelper.getRootElement(data);
		NodeList nodeList = element.getElementsByTagName("city_area");
		
		parse(nodeList);
	}
	
	private void parse(NodeList nodeList) {
		mAirports = new ArrayList<Airport>();
		int length = nodeList.getLength();
		for(int i = 0; i < length; i++) {
			Airport city = new Airport();
			
			Node node = nodeList.item(i);
			String cityName = XmlHelper.getValueByTag((Element)node, "city_name");
			String airportName = XmlHelper.getValueByTag((Element)node, "city_area_name");
			city.setAirport(airportName);
			city.setCity(cityName);
			
			NodeList terminals = ((Element)node).getElementsByTagName("build");
			int sizeOfTerminals = terminals.getLength();
			ArrayList<Terminal> airports = new ArrayList<Terminal>();
			for(int j = 0; j < sizeOfTerminals; j++) {
				Node terminal = terminals.item(j);
				String id = XmlHelper.getValueByTag((Element)terminal, "id");
				String name = XmlHelper.getValueByTag((Element)terminal, "name");
				Terminal airport = new Terminal(id, name);
				airports.add(airport);
			}
			city.setTerminals(airports);
			
			mAirports.add(city);
		}
	}
	
	public ArrayList<Airport> getAirports() {
		return mAirports;
	}
	
	public Terminal getTerminalById(String id) {
		if(mAirports == null) {
			return null;
		}
		
		int length = mAirports.size();
		for(int i = 0; i < length; i++) {
			Airport city = mAirports.get(i);
			if(city.getTerminals() == null) {
				continue;
			}
			int size = city.getTerminals().size();
			for(int j = 0; j < size; j++) {
				if(city.getTerminals().get(j).getId().equals(id)) {
					return city.getTerminals().get(j);
				}
			}
		}
		
		return null;
	}
}
