package com.globalreports.engine.objects.variable.barcode;

import java.util.Vector;

public class QRCode {
	public static final int MODE_NUMBER 		= 1 << 0;
	public static final int MODE_ALPHA_NUM 	= 1 << 1;
	public static final int MODE_8BIT_BYTE	= 1 << 2;
	public static final int MODE_KANJI		= 1 << 3;
	
	public static final int ERR_CORRECT_LEVEL_L	= 1;
	public static final int ERR_CORRECT_LEVEL_M	= 0;
	public static final int ERR_CORRECT_LEVEL_Q	= 3;
	public static final int ERR_CORRECT_LEVEL_H	= 2;
	
	private final int MASK_PATTERN000	= 0;
	private final int MASK_PATTERN001	= 1;
	private final int MASK_PATTERN010	= 2;
	private final int MASK_PATTERN011	= 3;
	private final int MASK_PATTERN100	= 4;
	private final int MASK_PATTERN101	= 5;
	private final int MASK_PATTERN110	= 6;
	private final int MASK_PATTERN111	= 7;
	
	private final int[][] PATTERN_POSITION_TABLE = new int[][] {
		{},
		{6, 18},
	    {6, 22},
	    {6, 26},
	    {6, 30},
	    {6, 34},
	    {6, 22, 38},
	    {6, 24, 42},
	    {6, 26, 46},
	    {6, 28, 50},
	    {6, 30, 54},		
	    {6, 32, 58},
	    {6, 34, 62},
	    {6, 26, 46, 66},
	    {6, 26, 48, 70},
	    {6, 26, 50, 74},
	    {6, 30, 54, 78},
	    {6, 30, 56, 82},
	    {6, 30, 58, 86},
	    {6, 34, 62, 90},
	    {6, 28, 50, 72, 94},
	    {6, 26, 50, 74, 98},
	    {6, 30, 54, 78, 102},
	    {6, 28, 54, 80, 106},
	    {6, 32, 58, 84, 110},
	    {6, 30, 58, 86, 114},
	    {6, 34, 62, 90, 118},
	    {6, 26, 50, 74, 98, 122},
	    {6, 30, 54, 78, 102, 126},
	    {6, 26, 52, 78, 104, 130},
	    {6, 30, 56, 82, 108, 134},
	    {6, 34, 60, 86, 112, 138},
	    {6, 30, 58, 86, 114, 142},
	    {6, 34, 62, 90, 118, 146},
	    {6, 30, 54, 78, 102, 126, 150},
	    {6, 24, 50, 76, 102, 128, 154},
	    {6, 28, 54, 80, 106, 132, 158},
	    {6, 32, 58, 84, 110, 136, 162},
	    {6, 26, 54, 82, 110, 138, 166},
	    {6, 30, 58, 86, 114, 142, 170}
																};
	
	private final int G15 = (1 << 10) | (1 << 8) | (1 << 5) | (1 << 4) | (1 << 2) | (1 << 1) | (1 << 0);
	private final int G18 = (1 << 12) | (1 << 11) | (1 << 10) | (1 << 9) | (1 << 8) | (1 << 5) | (1 << 2) | (1 << 0);
	private final int G15_MASK = (1 << 14) | (1 << 12) | (1 << 10)	| (1 << 4) | (1 << 1);
															    
	private int typeNumber;
	private int errorCorrectLevel;
	private QRModules[][] modules = null;
	private int moduleCount = 0;
	private int[] dataCache = null;
	private Vector<QR8bitByte> dataList = new Vector<QR8bitByte>();
	
	public int PAD0 = 0xEC;
	public int PAD1 = 0x11;
	
	public QRCode() {
		typeNumber = -1;
		errorCorrectLevel = ERR_CORRECT_LEVEL_H;
		
	}
	
	public int[] createData(int typeNumber, int errorCorrectLevel, Vector<QR8bitByte>dataList) {
		Vector<QRRSBlock> rsBlocks = QRRSBlock.getRSBlocks(typeNumber, errorCorrectLevel);
		
		QRBitBuffer buffer = new QRBitBuffer();
		
		for (int i = 0; i < dataList.size(); i++) {
			QR8bitByte data = dataList.get(i);
			buffer.put(data.mode, 4);
			buffer.put(data.getLength(), getLengthInBits(data.mode, typeNumber) );
			
			data.write(buffer);
			
		}

		// calc num max data.
		int totalDataCount = 0;
		for (int i = 0; i < rsBlocks.size(); i++) {
			totalDataCount += rsBlocks.get(i).dataCount;
			
		}

		if (buffer.getLengthInBits() > totalDataCount * 8) {
			throw new Error("code length overflow. ("
				+ buffer.getLengthInBits()
				+ ">"
				+  totalDataCount * 8
				+ ")");
		}

		// end code
		if (buffer.getLengthInBits() + 4 <= totalDataCount * 8) {
			buffer.put(0, 4);
		}

		// padding
		while (buffer.getLengthInBits() % 8 != 0) {
			buffer.putBit(false);
		}

		// padding
		while (true) {
			
			if (buffer.getLengthInBits() >= totalDataCount * 8) {
				break;
			}
			buffer.put(PAD0, 8);
			System.out.println(PAD0);
			if (buffer.getLengthInBits() >= totalDataCount * 8) {
				break;
			}
			buffer.put(PAD1, 8);
		}

		return createBytes(buffer, rsBlocks);
	}
	public int[] createBytes(QRBitBuffer buffer, Vector<QRRSBlock> rsBlocks) {
		int offset = 0;
		//for(int i = 0;i < buffer.buffer.size();i++) {
		//	System.out.println(buffer.buffer.get(i));
		//}
		int maxDcCount = 0;
		int maxEcCount = 0;
		
		int[][] dcdata = new int[rsBlocks.size()][];
		int[][] ecdata = new int[rsBlocks.size()][];
		
		for (int r = 0; r < rsBlocks.size(); r++) {

			int dcCount = rsBlocks.get(r).dataCount;
			int ecCount = rsBlocks.get(r).totalCount - dcCount;

			maxDcCount = Math.max(maxDcCount, dcCount);
			maxEcCount = Math.max(maxEcCount, ecCount);
			
			dcdata[r] = new int[dcCount];
			
			for (int i = 0; i < dcdata[r].length; i++) {
				dcdata[r][i] = 0xff & buffer.buffer.get(i + offset);
			}
			offset += dcCount;
			
			QRPolynomial rsPoly = getErrorCorrectPolynomial(ecCount);
			QRPolynomial rawPoly = new QRPolynomial(dcdata[r], rsPoly.getLength() - 1);

			QRPolynomial modPoly = rawPoly.mod(rsPoly);
			ecdata[r] = new int[rsPoly.getLength() - 1];
			for (int i = 0; i < ecdata[r].length; i++) {
	            int modIndex = i + modPoly.getLength() - ecdata[r].length;
				ecdata[r][i] = (modIndex >= 0)? modPoly.get(modIndex) : 0;
			}

		}
		
		int totalCodeCount = 0;
		for (int i = 0; i < rsBlocks.size(); i++) {
			totalCodeCount += rsBlocks.get(i).totalCount;
		}

		int[] data = new int[totalCodeCount];
		int index = 0;

		for (int i = 0; i < maxDcCount; i++) {
			for (int r = 0; r < rsBlocks.size(); r++) {
				if (i < dcdata[r].length) {
					data[index++] = dcdata[r][i];
				}
			}
		}

		for (int i = 0; i < maxEcCount; i++) {
			for (int r = 0; r < rsBlocks.size(); r++) {
				if (i < ecdata[r].length) {
					data[index++] = ecdata[r][i];
				}
			}
		}

		return data;
	}

	public void addData(String data) {
		QR8bitByte newData = new QR8bitByte(data);
		this.dataList.add(newData);
		this.dataCache = null;
	}
	public boolean isDark(int row, int col) {
		if (row < 0 || this.moduleCount <= row || col < 0 || this.moduleCount <= col) {
			throw new Error(row + "," + col);
		}
		
		return this.modules[row][col].value;
	}
	public int getModuleCount() {
		return this.moduleCount;
	}
	
	
	public void make() {
		//QRRSBlock qrrsblock = new QRRSBlock();
		
		if (this.typeNumber < 1 ){
			int typeNumber = 1;
			for (typeNumber = 1; typeNumber < 40; typeNumber++) {
				Vector<QRRSBlock> rsBlocks = QRRSBlock.getRSBlocks(typeNumber, this.errorCorrectLevel);

				QRBitBuffer buffer = new QRBitBuffer();
				int totalDataCount = 0;
				for (int i = 0; i < rsBlocks.size(); i++) {
					totalDataCount += rsBlocks.get(i).dataCount;
				}

				for (int i = 0; i < this.dataList.size(); i++) {
					QR8bitByte data = this.dataList.get(i);
					buffer.put(data.mode, 4);
					buffer.put(data.getLength(), getLengthInBits(data.mode, typeNumber) );
					data.write(buffer);
					
				}
				if (buffer.getLengthInBits() <= totalDataCount * 8)
					break;
			}
			this.typeNumber = typeNumber;
		}
		this.makeImpl(false, this.getBestMaskPattern() );
	}
	
	public void makeImpl(boolean test, int maskPattern) {
		
		this.moduleCount = this.typeNumber * 4 + 17;
		this.modules = new QRModules[this.moduleCount][];
		
		for (int row = 0; row < this.moduleCount; row++) {
			
			this.modules[row] = new QRModules[this.moduleCount];
			
			for (int col = 0; col < this.moduleCount; col++) {
				this.modules[row][col] = null;//(col + row) % 3;
			}
		}
	
		this.setupPositionProbePattern(0, 0);
		this.setupPositionProbePattern(this.moduleCount - 7, 0);
		this.setupPositionProbePattern(0, this.moduleCount - 7);
		this.setupPositionAdjustPattern();
		this.setupTimingPattern();
		this.setupTypeInfo(test, maskPattern);
		
		if (this.typeNumber >= 7) {
			this.setupTypeNumber(test);
		}
	
		if (this.dataCache == null) {
			this.dataCache = createData(this.typeNumber, this.errorCorrectLevel, this.dataList);
		}
		
		this.mapData(this.dataCache, maskPattern);
	}

	public void setupPositionProbePattern(int row, int col)  {
		
		for (int r = -1; r <= 7; r++) {
			
			if (row + r <= -1 || this.moduleCount <= row + r) continue;
			
			for (int c = -1; c <= 7; c++) {
				
				if (col + c <= -1 || this.moduleCount <= col + c) continue;
				
				if ( (0 <= r && r <= 6 && (c == 0 || c == 6) )
						|| (0 <= c && c <= 6 && (r == 0 || r == 6) )
						|| (2 <= r && r <= 4 && 2 <= c && c <= 4) ) {
					//NEW
					modules[row + r][col + c] = new QRModules();
					this.modules[row + r][col + c].value = true;
					
					
				} else {
					//NEW
					modules[row + r][col + c] = new QRModules();
					this.modules[row + r][col + c].value = false;
					
				}
			}		
		}		
	}
	public int getBestMaskPattern() {
		double minLostPoint = 0;
		int pattern = 0;
	
		for (int i = 0; i < 8; i++) {
			
			this.makeImpl(true, i);
	
			double lostPoint = getLostPoint(this);
	
			if (i == 0 || minLostPoint >  lostPoint) {
				minLostPoint = lostPoint;
				pattern = i;
			}
		}
	
		return pattern;
	}
	
	public void setupTimingPattern() {
		for (int r = 8; r < this.moduleCount - 8; r++) {
			
			if (this.modules[r][6] != null) {
				continue;
			}
			//NEW
			modules[r][6] = new QRModules();
			this.modules[r][6].value = (r % 2 == 0);
		}
	
		for (int c = 8; c < this.moduleCount - 8; c++) {
			
			if (this.modules[6][c] != null) {
				continue;
			}
			//NEW
			modules[6][c] = new QRModules();
			this.modules[6][c].value = (c % 2 == 0);
		}
	}
	public void setupPositionAdjustPattern() {
		int[] pos = getPatternPosition(this.typeNumber);
		
		for (int i = 0; i < pos.length; i++) {
		
			for (int j = 0; j < pos.length; j++) {
			
				int row = pos[i];
				int col = pos[j];
				
				if (this.modules[row][col] != null) {
					continue;
				}
				
				for (int r = -2; r <= 2; r++) {
				
					for (int c = -2; c <= 2; c++) {
					
						if (r == -2 || r == 2 || c == -2 || c == 2 
								|| (r == 0 && c == 0) ) {
							//NEW
							modules[row + r][col + c] = new QRModules();
							this.modules[row + r][col + c].value = true;
						} else {
							//NEW
							modules[row + r][col + c] = new QRModules();
							this.modules[row + r][col + c].value = false;
						}
					}
				}
			}
		}
	}
	public void setupTypeNumber(boolean test) {
		int bits = getBCHTypeNumber(this.typeNumber);
		
		for (int i = 0; i < 18; i++) {
			boolean mod = (!test && ( (bits >> i) & 1) == 1);
			this.modules[(int)Math.floor(i / 3)][i % 3 + this.moduleCount - 8 - 3].value = mod;
		
			
		}
	
		for (int i = 0; i < 18; i++) {
			boolean mod = (!test && ( (bits >> i) & 1) == 1);
			this.modules[i % 3 + this.moduleCount - 8 - 3][(int)Math.floor(i / 3)].value = mod;
		}
	}
	public void setupTypeInfo(boolean test, int maskPattern) {
		int data = (this.errorCorrectLevel << 3) | maskPattern;
		int bits = getBCHTypeInfo(data);
	
		// vertical		
		for (int i = 0; i < 15; i++) {
	
			boolean mod = (!test && ( (bits >> i) & 1) == 1);
	
			if (i < 6) {
				//NEW
				modules[i][8] = new QRModules();
				this.modules[i][8].value = mod;
			} else if (i < 8) {
				//NEW
				modules[i+1][8] = new QRModules();
				this.modules[i + 1][8].value = mod;
			} else {
				//NEW
				modules[this.moduleCount - 15 + i][8] = new QRModules();
				this.modules[this.moduleCount - 15 + i][8].value = mod;
			}
		}
	
		// horizontal
		for (int i = 0; i < 15; i++) {
	
			boolean mod = (!test && ( (bits >> i) & 1) == 1);
			
			if (i < 8) {
				//NEW
				modules[8][this.moduleCount - i - 1] = new QRModules();
				this.modules[8][this.moduleCount - i - 1].value = mod;
			} else if (i < 9) {
				//NEW
				modules[8][15 - i - 1 + 1] = new QRModules();
				this.modules[8][15 - i - 1 + 1].value = mod;
			} else {
				//NEW
				modules[8][15 - i - 1] = new QRModules();
				this.modules[8][15 - i - 1].value = mod;
			}
		}
	
		// fixed module
		//NEW
		modules[this.moduleCount - 8][8] = new QRModules();
		this.modules[this.moduleCount - 8][8].value = (!test);
	
	}
	public void mapData(int[] data, int maskPattern) {
		int inc = -1;
		int row = this.moduleCount - 1;
		int bitIndex = 7;
		int byteIndex = 0;
		
		for (int col = this.moduleCount - 1; col > 0; col -= 2) {
	
			if (col == 6) col--;
	
			while (true) {
	
				for (int c = 0; c < 2; c++) {
					
					if (this.modules[row][col - c] == null) {
						
						boolean dark = false;
	
						if (byteIndex < data.length) {
							dark = ( ( (data[byteIndex] >>> bitIndex) & 1) == 1);
						}
						
						boolean mask = getMask(maskPattern, row, col - c);
	
						if (mask) {
							dark = !dark;
						}
						
						//NEW
						modules[row][col - c] = new QRModules();
						this.modules[row][col - c].value = dark;
						bitIndex--;
	
						if (bitIndex == -1) {
							byteIndex++;
							bitIndex = 7;
						}
					}
					
				}
								
				row += inc;
	
				if (row < 0 || this.moduleCount <= row) {
					row -= inc;
					inc = -inc;
					break;
				}
			}
		}
	}
	
	
	/* QRUTIL */
	public double getLostPoint(QRCode qrCode) {
		int moduleCount = qrCode.getModuleCount();
	    
	    double lostPoint = 0;
	    
	    // LEVEL1
	    
	    for (int row = 0; row < moduleCount; row++) {

		    for (int col = 0; col < moduleCount; col++) {

			    int sameCount = 0;
			    boolean dark = qrCode.isDark(row, col);
			    
			    for (int r = -1; r <= 1; r++) {

				    if (row + r < 0 || moduleCount <= row + r) {
					    continue;
				    }

				    for (int c = -1; c <= 1; c++) {

					    if (col + c < 0 || moduleCount <= col + c) {
						    continue;
					    }

					    if (r == 0 && c == 0) {
						    continue;
					    }

					    if (dark == qrCode.isDark(row + r, col + c) ) {
					    	
						    sameCount++;
					    }
				    }
			    }

			    if (sameCount > 5) {
				    lostPoint += (3 + sameCount - 5);
			    }
		    }
	    }

	    // LEVEL2

	    for (int row = 0; row < moduleCount - 1; row++) {
		    for (int col = 0; col < moduleCount - 1; col++) {
			    int count = 0;
			    if (qrCode.isDark(row,     col    ) ) count++;
			    if (qrCode.isDark(row + 1, col    ) ) count++;
			    if (qrCode.isDark(row,     col + 1) ) count++;
			    if (qrCode.isDark(row + 1, col + 1) ) count++;
			    if (count == 0 || count == 4) {
				    lostPoint += 3;
			    }
		    }
	    }

	    // LEVEL3

	    for (int row = 0; row < moduleCount; row++) {
		    for (int col = 0; col < moduleCount - 6; col++) {
			    if (qrCode.isDark(row, col)
					    && !qrCode.isDark(row, col + 1)
					    &&  qrCode.isDark(row, col + 2)
					    &&  qrCode.isDark(row, col + 3)
					    &&  qrCode.isDark(row, col + 4)
					    && !qrCode.isDark(row, col + 5)
					    &&  qrCode.isDark(row, col + 6) ) {
				    lostPoint += 40;
			    }
		    }
	    }

	    for (int col = 0; col < moduleCount; col++) {
		    for (int row = 0; row < moduleCount - 6; row++) {
			    if (qrCode.isDark(row, col)
					    && !qrCode.isDark(row + 1, col)
					    &&  qrCode.isDark(row + 2, col)
					    &&  qrCode.isDark(row + 3, col)
					    &&  qrCode.isDark(row + 4, col)
					    && !qrCode.isDark(row + 5, col)
					    &&  qrCode.isDark(row + 6, col) ) {
				    lostPoint += 40;
			    }
		    }
	    }

	    // LEVEL4
	    
	    int darkCount = 0;

	    for (int col = 0; col < moduleCount; col++) {
		    for (int row = 0; row < moduleCount; row++) {
			    if (qrCode.isDark(row, col) ) {
				    darkCount++;
			    }
		    }
	    }
	    
	    int ratio = Math.abs(100 * darkCount / moduleCount / moduleCount - 50) / 5;
	    lostPoint += ratio * 10;

	    return lostPoint;		
	}	
	private int getBCHTypeInfo(int data) {
		int d = data << 10;
	    while (getBCHDigit(d) - getBCHDigit(G15) >= 0) {
		    d ^= (G15 << (getBCHDigit(d) - getBCHDigit(G15) ) ); 	
	    }
	    return ( (data << 10) | d) ^ G15_MASK;
    }
	private int getBCHTypeNumber(int data) {
	    int d = data << 12;
	    while (getBCHDigit(d) - getBCHDigit(G18) >= 0) {
		    d ^= (G18 << (getBCHDigit(d) - getBCHDigit(G18) ) ); 	
	    }
	    return (data << 12) | d;
    }
	private int getBCHDigit(int data) {
		int digit = 0;

	    while (data != 0) {
		    digit++;
		    data >>>= 1;
	    }

	    return digit;
	}
	private int[] getPatternPosition(int typeNumber) {
		return PATTERN_POSITION_TABLE[typeNumber - 1];
	}
	private boolean getMask(int maskPattern, int i, int j) {
		switch (maskPattern) {
	    
	    case MASK_PATTERN000 : return (i + j) % 2 == 0;
	    case MASK_PATTERN001 : return i % 2 == 0;
	    case MASK_PATTERN010 : return j % 3 == 0;
	    case MASK_PATTERN011 : return (i + j) % 3 == 0;
	    case MASK_PATTERN100 : return (Math.floor(i / 2) + Math.floor(j / 3) ) % 2 == 0;
	    case MASK_PATTERN101 : return (i * j) % 2 + (i * j) % 3 == 0;
	    case MASK_PATTERN110 : return ( (i * j) % 2 + (i * j) % 3) % 2 == 0;
	    case MASK_PATTERN111 : return ( (i * j) % 3 + (i + j) % 2) % 2 == 0;

	    
	    default :
		    return false;
	    }
	}
	private QRPolynomial getErrorCorrectPolynomial(int errorCorrectLength) {
		QRPolynomial a = new QRPolynomial(new int[]{1}, 0);

	    for (int i = 0; i < errorCorrectLength; i++) {
	    	a = a.multiply(new QRPolynomial(new int[]{1, QRMath.gexp(i)}, 0) );
	    }
	    
	    return a;
	}
	public int getLengthInBits(int mode, int type) {
		if (1 <= type && type < 10) {

		    // 1 - 9

		    switch(mode) {
		    case MODE_NUMBER 	: return 10;
		    case MODE_ALPHA_NUM 	: return 9;
		    case MODE_8BIT_BYTE	: return 8;
		    case MODE_KANJI  	: return 8;
		    default :
		    	return -1;
			    //throw new Error("mode:" + mode);
		    }

	    } else if (type < 27) {

		    // 10 - 26

		    switch(mode) {
		    case MODE_NUMBER 	: return 12;
		    case MODE_ALPHA_NUM 	: return 11;
		    case MODE_8BIT_BYTE	: return 16;
		    case MODE_KANJI  	: return 10;
		    default :
		    	return -1;
			    //throw new Error("mode:" + mode);
		    }

	    } else if (type < 41) {

		    // 27 - 40

		    switch(mode) {
		    case MODE_NUMBER 	: return 14;
		    case MODE_ALPHA_NUM	: return 13;
		    case MODE_8BIT_BYTE	: return 16;
		    case MODE_KANJI  	: return 12;
		    default :
		    	return -1;
			    //throw new Error("mode:" + mode);
		    }

	    } else {
		    System.out.println("ERR");
		    return -1;
	    	//throw new Error("type:" + type);
	    }
	}
}
