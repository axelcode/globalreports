package com.globalreports.engine.structure.grbinary.data;

import java.util.Hashtable;
import java.util.Vector;

public class GRDataVoice extends GRData {
	private Hashtable<String, String> voiceData;
	
	public GRDataVoice() {
		voiceData = new Hashtable<String, String>();
	}
	
	public void addElement(String key, String value) {
		voiceData.put(key,value);
	}
	
	
	public String getValue(String nameVariable) {
		return voiceData.get(nameVariable);
	}
	
	public int getTotaleVariable() {
		return voiceData.size();
	}
	
}
