package com.globalreports.engine.objects.variable.barcode.code128;

import java.util.Vector;

public class GRMap {
	private Vector<Character> character;
	private Vector<Long> position;
	private Vector<String> weights;
	
	public GRMap() {
		character = new Vector<Character>();
		position = new Vector<Long>();
		weights = new Vector<String>();
	}

	public void put(char c, long pos, String w) {
		character.add(c);
		position.add(pos);
		weights.add(w);
	}
	public String getWeightsFromCharacter(char value) {
		for(int i = 0;i < character.size();i++) {
			if(character.get(i) == value)
				return weights.get(i);
		}
		
		return null;
	}
	
	public long getPositionFromCharacter(char value) {
		for(int i = 0;i < character.size();i++) {
			if(character.get(i) == value)
				return position.get(i);
		}
		
		return -1;
	}
	public String getWeightsFromPosition(long value) {
		for(int i = 0;i < position.size();i++) {
			if(position.get(i) == value)
				return weights.get(i);
		}
		
		return null;
	}
}
