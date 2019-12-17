package com.globalreports.engine.objects.variable.barcode;

public class QRMath {
	public static int[] EXP_TABLE = new int[256];
	public static int[] LOG_TABLE = new int[256];
	
	public static void init() {
		for (int i = 0; i < 8; i++) {
			EXP_TABLE[i] = 1 << i;
		}
		for (int i = 8; i < 256; i++) {
			EXP_TABLE[i] = EXP_TABLE[i - 4]
				^ EXP_TABLE[i - 5]
				^ EXP_TABLE[i - 6]
				^ EXP_TABLE[i - 8];
		}
		for (int i = 0; i < 255; i++) {
			LOG_TABLE[EXP_TABLE[i] ] = i;
		}

	}
	public static int glog(int n) {
		if (n < 1) {
			throw new Error("glog(" + n + ")");
		}
		
		return LOG_TABLE[n];
	}
	public static int gexp(int n) {
		while (n < 0) {
			n += 255;
		}
	
		while (n >= 256) {
			n -= 255;
		}
	
		return EXP_TABLE[n];
	}
}
