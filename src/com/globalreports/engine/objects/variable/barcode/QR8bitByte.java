package com.globalreports.engine.objects.variable.barcode;

public class QR8bitByte {
	public int mode;
	public String data;
	
	public QR8bitByte(String data) {
		this.mode = QRCode.MODE_8BIT_BYTE;
		this.data = data;
	}
	
	public int getLength() {
		return this.data.length();
	}
	
	public void write(QRBitBuffer buffer) {
		
		for (int i = 0; i < this.data.length(); i++) {
			buffer.put(this.data.codePointAt(i), 8);
		}
	}
}
