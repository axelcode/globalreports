package com.globalreports.engine.objects.variable.barcode;

import java.util.Vector;

public class QRBitBuffer {
	public int[] buff;
	public Vector<Integer> buffer;
	public int length;
	
	public QRBitBuffer() {
		buffer = new Vector<Integer>();
		length = 0;
	}
	
	public boolean get(int index) {
		int bufIndex = (int)Math.floor(index / 8);
		return ( (buffer.get(bufIndex) >>> (7 - index % 8) ) & 1) == 1;
	}
	public void put(int num, int length) {
		for (int i = 0; i < length; i++) {
			this.putBit( ( (num >>> (length - i - 1) ) & 1) == 1);
		}
	}
	public int getLengthInBits() {
		return length;
	}
	public void putBit(boolean bit) {
		int bufIndex = (int)Math.floor(this.length / 8);
		if (this.buffer.size() <= bufIndex) {
			this.buffer.add(0);
		}
	
		if (bit) {
			int t = this.buffer.get(bufIndex);
			int temp =  (0x80 >>> (this.length % 8) );
			
			//temp += temp;
			this.buffer.remove(bufIndex);
			this.buffer.add(bufIndex,(temp+t));
			//System.out.println("M:"+t);
			
			//VERIFICARE
			//this.buffer[bufIndex] |= (0x80 >>> (this.length % 8) );
			//this.buffer.get(bufIndex) |= (0x80 >>> (this.length % 8) );
		}
	
		this.length++;
	}
}
