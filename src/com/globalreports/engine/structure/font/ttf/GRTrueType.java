/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.font.ttf.GRTrueType
 * 
 * Begin       : 
 * Last Update : 
 *
 * Author      : Alessandro Baldini - alex.baldini72@gmail.com
 * License     : GNU-GPL v2 (http://www.gnu.org/licenses/)
 * ==========================================================================
 * 
 * GlobalReports
 * Copyright (C) 2015 Alessandro Baldini
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking GlobalReports Engine(C) statically or dynamically with other 
 * modules is making a combined work based on GlobalReports Engine(C). 
 * Thus, the terms and conditions of the GNU General Public License cover 
 * the whole combination.
 *
 * In addition, as a special exception, the copyright holders 
 * of GlobalReports Engine(C) give you permission to combine 
 * GlobalReports Engine(C) program with free software programs or libraries 
 * that are released under the GNU LGPL. 
 * You may copy and distribute such a system following the terms of the GNU GPL 
 * for GlobalReports Engine(C) and the licenses of the other code concerned, 
 * provided that you include the source code of that other code 
 * when and as the GNU GPL requires distribution of source code.
 *
 * Note that people who make modified versions of GlobalReports Engine(C) 
 * are not obligated to grant this special exception for their modified versions; 
 * it is their choice whether to do so. The GNU General Public License 
 * gives permission to release a modified version without this exception; 
 * this exception also makes it possible to release a modified version 
 * which carries forward this exception.
 * 
 */
package com.globalreports.engine.structure.font.ttf;

import java.util.Calendar;
import java.util.Vector;

import com.globalreports.engine.structure.font.GRFontDataStream;
import com.globalreports.engine.structure.font.GRFontType;
import com.globalreports.engine.structure.font.encoding.GRWinAnsi;

public class GRTrueType extends GRFontType {
	
	public static final String TAG_CHARACTERTOGLYPHMAPPING	= "cmap";
	public static final String TAG_GLYPHDATA				= "glyf";
	public static final String TAG_FONTHEADER				= "head";
	public static final String TAG_HORIZONTALHEADER			= "hhea";
	public static final String TAG_HORIZONTALMETRICS		= "hmtx";
	public static final String TAG_INDEXTOLOCATION			= "loca";
	public static final String TAG_MAXIMUMPROFILE			= "maxp";
	public static final String TAG_NAMING					= "name";
	public static final String TAG_POSTSCRIPT				= "post";
	
	public static final String TAG_OS2						= "OS/2";
	
	private float scale;
	private byte[] stream;
	private Vector<GRCMap> cmap;
	private GRCMap cmap4;
	private int numGlyphs;
	int[] advanceWidth;
	
	// Strutture 
	// /FontDescriptor
	private GRWinAnsi ansi;
	private GRBoundingBox fontbbox;
	private String fontName;
	private int ascent;
	private int descent;
	private int winAscent;
	private int winDescent;
	private int italicAngle = 0;
	private int flags = 32;		// Default Adobe
	private long isFixedPitch;	// Usato per determinare il valore di flags
	private int stemV = 0;		// Valore fisso
	private int capHeight = 0;	// Prende il 4 valore del Bounding Box
	private int firstChar = 0;
	private int lastChar = 255;
	
	public GRTrueType(byte[] f) {
		stream = new byte[f.length];
		
		System.arraycopy(f, 0, stream, 0, f.length);
		parseStream();
		
		/*
		System.out.println("scale: "+scale);
		System.out.println("numGlyphs: "+numGlyphs);
		System.out.println("/FontName: "+fontName);
		System.out.println("/FontBBox: "+fontbbox.toString());
		System.out.println("/Flags: "+flags);
		System.out.println("/CapHeight: ");
		System.out.println("/Ascent: "+ascent);
		System.out.println("/Descent: "+descent);
		System.out.println("/ItalicAngle: "+italicAngle);
		System.out.println("/StemV: "+stemV);
		System.out.println("Widths: "+ansi.getMetrics());
		*/
		
	}
	private void init() {
		numGlyphs = 0;
		
		winAscent = -1;
		winDescent = -1;
	}
	private void parseStream() {
		float scalerType;
		int numTables;
		int searchRange;
		int entrySelector;
		int rangeShift;
		
		int numOfLongHorMetrics = 0;	// Numero delle metriche contenute in hmtx
		GRFontDataStream raf = new GRFontDataStream(stream);
		
		scalerType = raf.readDecimal();
		numTables = raf.readUShort();
		searchRange = raf.readUShort();
		entrySelector = raf.readUShort();
		rangeShift = raf.readUShort();
		
		for(int i = 0;i < numTables;i++) {
			String tag = raf.readLine(4);
			long checkSum = raf.readUInt();
			long offset = raf.readUInt();
			long length = raf.readUInt();
			
			if(tag.equals(TAG_CHARACTERTOGLYPHMAPPING)) {
				readCharacterToGlyphMapping(raf, offset);
			} else if(tag.equals(TAG_FONTHEADER)) {
				readFontHeader(raf, offset);
			} else if(tag.equals(TAG_HORIZONTALHEADER)) {
				numOfLongHorMetrics = readHorizontalHeader(raf, offset);
			} else if(tag.equals(TAG_HORIZONTALMETRICS)) {
				readHorizontalMetrics(raf, offset, numOfLongHorMetrics);
			} else if(tag.equals(TAG_MAXIMUMPROFILE)) {
				readMaximumProfile(raf, offset);
			} else if(tag.equals(TAG_NAMING)) {
				readNaming(raf, offset);
			} else if(tag.equals(TAG_POSTSCRIPT)) {
				readPostScript(raf, offset);
			} else if(tag.equals(TAG_OS2)) {
				readOS2(raf, offset);
			}
			
		}
		
		ansi = new GRWinAnsi(advanceWidth, scale);
		ansi.setMetrics(cmap4);
		
		if(winAscent != -1)
			ascent = (int)(winAscent * scale);
		if(winDescent != -1)
			descent = (int)(winDescent * scale);
		
		capHeight = fontbbox.getBottom();
	}
	private void readCharacterToGlyphMapping(GRFontDataStream raf, long offset) {
		int actualAddress = raf.getFilePointer();
		raf.seek((int)offset);
		
		int version; 
        int numberSubtables;
        
        version = raf.readUShort();
        numberSubtables = raf.readUShort();
        
        for(int i = 0;i < numberSubtables;i++) {
        	GRCMap map;
        	
        	int platformID = raf.readUShort();
            int platformSpecificID = raf.readUShort();
            long cmapOffset = raf.readUInt();
            
            map = new GRCMap(platformID,platformSpecificID,cmapOffset);
            map.parse(raf, offset);
            
            if(cmap == null)
            	cmap = new Vector<GRCMap>();
            
            cmap.add(map);
            
            if(map.getFormat() == GRCMap.FORMAT_4)
            	cmap4 = map;
        }
        	
		raf.seek(actualAddress);
	}
	private void readGlyphData(GRFontDataStream raf, long offset) {
		int actualAddress = raf.getFilePointer();
		raf.seek((int)offset);
		
		raf.seek(actualAddress);
	}
	private void readFontHeader(GRFontDataStream raf, long offset) {
		int actualAddress = raf.getFilePointer();
		raf.seek((int)offset);
		
		float version;
	    float fontRevision;
	    long checkSumAdjustment;
	    long magicNumber;
	    int flags;
	    int unitsPerEm;
	    Calendar created;
	    Calendar modified;
	    short xMin;
	    short yMin;
	    short xMax;
	    short yMax;
	    int macStyle;
	    int lowestRecPPEM;
	    short fontDirectionHint;
	    short indexToLocFormat;
	    short glyphDataFormat;
		
	    version = raf.readDecimal();
	    fontRevision = raf.readDecimal();
        checkSumAdjustment = raf.readUInt();
        magicNumber = raf.readUInt();
        flags = raf.readUShort();
        unitsPerEm = raf.readUShort();
        created = raf.readDate();
        modified = raf.readDate();
        xMin = raf.readShort();
        yMin = raf.readShort();
        xMax = raf.readShort();
        yMax = raf.readShort();
        macStyle = raf.readUShort();
        lowestRecPPEM = raf.readUShort();
        fontDirectionHint = raf.readShort();
        indexToLocFormat = raf.readShort();
        glyphDataFormat = raf.readShort();
        
        scale = 1000f / unitsPerEm;
        
        fontbbox = new GRBoundingBox((xMin * scale),(yMin * scale),(xMax * scale),(yMax * scale));
        
		raf.seek(actualAddress);
	}
	private int readHorizontalHeader(GRFontDataStream raf, long offset) {
		int actualAddress = raf.getFilePointer();
		raf.seek((int)offset);
		
		float version;
	    short ascent;
	    short descent;
	    short lineGap;
	    int advanceWidthMax;
	    short minLeftSideBearing;
	    short minRightSideBearing;
	    short xMaxExtent;
	    short caretSlopeRise;
	    short caretSlopeRun;
	    short caretOffset;
	    short reserved1;
	    short reserved2;
	    short reserved3;
	    short reserved4;
	    short metricDataFormat;
	    int numOfLongHorMetrics;
	    
	    version = raf.readDecimal();
        ascent = raf.readShort();
        descent= raf.readShort();
        lineGap = raf.readShort();
        advanceWidthMax = raf.readUShort();
        minLeftSideBearing = raf.readShort();
        minRightSideBearing = raf.readShort();
        xMaxExtent = raf.readShort();
        caretSlopeRise = raf.readShort();
        caretSlopeRun = raf.readShort();
        caretOffset = raf.readShort();
        reserved1 = raf.readShort();
        reserved2 = raf.readShort();
        reserved3 = raf.readShort();
        reserved4 = raf.readShort();
        metricDataFormat = raf.readShort();
        numOfLongHorMetrics = raf.readUShort();
        
        this.ascent = Math.round(ascent * scale);
        this.descent = Math.round(descent * scale);
        
        raf.seek(actualAddress);
        
        return numOfLongHorMetrics;
	}
	private void readHorizontalMetrics(GRFontDataStream raf, long offset, int numOfLongHorMetrics) {
		int actualAddress = raf.getFilePointer();
		raf.seek((int)offset);
		
		advanceWidth = new int[numOfLongHorMetrics];
	    short[] leftSideBearing = new short[numOfLongHorMetrics];
	    
	    for(int i = 0;i < numOfLongHorMetrics;i++) {
	    	advanceWidth[i] = raf.readUShort();
            leftSideBearing[i] = raf.readShort();
            
            //System.out.println(i+" - "+advanceWidth[i] * scale);
	    }
	    
	    raf.seek(actualAddress);
	}
	private void readIndexToLocation(GRFontDataStream raf, long offset) {
		int actualAddress = raf.getFilePointer();
		raf.seek((int)offset);
			    
		raf.seek(actualAddress);
	}
	private void readMaximumProfile(GRFontDataStream raf, long offset) {
		int actualAddress = raf.getFilePointer();
		raf.seek((int)offset);
		
		float version;
	    int numGlyphs;
	    int maxPoints;
	    int maxContours;
	    int maxCompositePoints;
	    int maxCompositeContours;
	    int maxZones;
	    int maxTwilightPoints;
	    int maxStorage;
	    int maxFunctionDefs;
	    int maxInstructionDefs;
	    int maxStackElements;
	    int maxSizeOfInstructions;
	    int maxComponentElements;
	    int maxComponentDepth;
	    
	    version = raf.readDecimal();
	    numGlyphs = raf.readUShort();
	    maxPoints = raf.readUShort();
	    maxContours = raf.readUShort();
	    maxCompositePoints = raf.readUShort();
	    maxCompositeContours = raf.readUShort();
	    maxZones = raf.readUShort();
	    maxTwilightPoints = raf.readUShort();
	    maxStorage = raf.readUShort();
	    maxFunctionDefs = raf.readUShort();
	    maxInstructionDefs = raf.readUShort();
	    maxStackElements = raf.readUShort();
	    maxSizeOfInstructions = raf.readUShort();
	    maxComponentElements = raf.readUShort();
	    maxComponentDepth = raf.readUShort();
	    
	    this.numGlyphs = numGlyphs;
	    
		raf.seek(actualAddress);
	}
	private void readNaming(GRFontDataStream raf, long offset) {
		int actualAddress = raf.getFilePointer();
		raf.seek((int)offset);
		
		int format;
        int count;
        int stringOffset;
        
        format = raf.readUShort();
        count = raf.readUShort();
        stringOffset = raf.readUShort();
        
        // Attualmente questa tabella viene letta solamente per recuperare il nome PostScript
        for(int i = 0;i < count;i++) {
        	int platformID;
        	int platformSpecificID;
        	int languageID;
        	int nameID;
        	int length;
        	int offsetName;
        	
        	platformID = raf.readUShort();
        	platformSpecificID = raf.readUShort();
        	languageID = raf.readUShort();
        	nameID = raf.readUShort();
        	length = raf.readUShort();
        	offsetName = raf.readUShort();
        	
        	if(platformID == 1 && platformSpecificID == 0) {
        		//if(platformSpecificID == 0 || platformSpecificID == 1) {
        			if(nameID == 6) {
        				int currentPosition = raf.getFilePointer();
        				
        				long off = offset + 6 + (count * 2 * 6) + offsetName;
        				
        				raf.seek((int)off);
        				fontName = raf.readLine(length);
        				raf.seek(currentPosition);
        			}
        		//}
        	}
        	
        }
        
		raf.seek(actualAddress);
	}
	private void readPostScript(GRFontDataStream raf, long offset) {
		int actualAddress = raf.getFilePointer();
		raf.seek((int)offset);
		
		float format;
	    float italicAngle;
	    short underlinePosition;
	    short underlineThickness;
	    long isFixedPitch;
	    long minMemType42;
	    long maxMemType42;
	    long mimMemType1;
	    long maxMemType1;
	    
	    format = raf.readDecimal();
        italicAngle = raf.readDecimal();
        underlinePosition = raf.readShort();
        underlineThickness = raf.readShort();
        isFixedPitch = raf.readUInt();
        minMemType42 = raf.readUInt();
        maxMemType42 = raf.readUInt();
        mimMemType1 = raf.readUInt();
        maxMemType1 = raf.readUInt();

        italicAngle = italicAngle / 0x10000;
        if((italicAngle % 0x10000) > 0) {
        	italicAngle = ((italicAngle % 0x10000) * 1000) / 0x10000;
        }

        this.italicAngle = (int)italicAngle;
        this.isFixedPitch = isFixedPitch;
        
		raf.seek(actualAddress);
	}
	private void readOS2(GRFontDataStream raf, long offset) {
		int actualAddress = raf.getFilePointer();
		raf.seek((int)offset);
		
		int version;
	    short xAvgCharWidth;
	    int usWeightClass;
	    int usWidthClass;
	    short fsType;
	    short ySubscriptXSize;
	    short ySubscriptYSize;
	    short ySubscriptXOffset;
	    short ySubscriptYOffset;
	    short ySuperscriptXSize;
	    short ySuperscriptYSize;
	    short ySuperscriptXOffset;
	    short ySuperscriptYOffset;
	    short yStrikeoutSize;
	    short yStrikeoutPosition;
	    int sFamilyClass;
	    byte[] panose = new byte[10];
	    long[] ulCharRange = new long[4];
	    String achVendID = "XXXX";
	    int fsSelection;
	    int fsFirstCharIndex;
	    int fsLastCharIndex;
	    
	    short sTypoAscender;
	    short sTypoDescender;
	    short sTypoLineGap;
	    int usWinAscent;
	    int usWinDescent;
	    long ulCodePageRange1 = -1;
	    long ulCodePageRange2 = -1;
	    short sxHeight;
	    short sCapHeight = 0;
	    int usDefaultChar;
	    int usBreakChar;
	    int usMaxContext;
	    
	    version = raf.readUShort();
	    xAvgCharWidth = raf.readShort();
        usWeightClass = raf.readUShort();
        usWidthClass = raf.readUShort();
        fsType = raf.readShort();
        ySubscriptXSize = raf.readShort();
        ySubscriptYSize = raf.readShort();
        ySubscriptXOffset = raf.readShort();
        ySubscriptYOffset = raf.readShort();
        ySuperscriptXSize = raf.readShort();
        ySuperscriptYSize = raf.readShort();
        ySuperscriptXOffset = raf.readShort();
        ySuperscriptYOffset = raf.readShort();
        yStrikeoutSize = raf.readShort();
        yStrikeoutPosition = raf.readShort();
        sFamilyClass = raf.readShort();
        panose = raf.read(10);
        ulCharRange[0] = raf.readUInt();
        ulCharRange[1] = raf.readUInt();
        ulCharRange[2] = raf.readUInt();
        ulCharRange[3] = raf.readUInt();
        achVendID = raf.readLine(4);
        fsSelection = raf.readUShort();
        fsFirstCharIndex = raf.readUShort();
        fsLastCharIndex = raf.readUShort();
        
        sTypoAscender = raf.readShort();
        sTypoDescender = raf.readShort();
        sTypoLineGap = raf.readShort();
        usWinAscent = raf.readUShort();
        usWinDescent = raf.readUShort();
        
        if (version >= 1) {
        	ulCodePageRange1 = raf.readUInt();
        	ulCodePageRange2 = raf.readUInt();
        }
        if (version >= 1.2) {
            sxHeight = raf.readShort();
            sCapHeight = raf.readShort();
            usDefaultChar = raf.readUShort();
            usBreakChar = raf.readUShort();
            usMaxContext = raf.readUShort();
        }
        
        winAscent = sTypoAscender;
        winDescent = sTypoDescender;
        //System.out.println("FAMILY: "+sFamilyClass);
		raf.seek(actualAddress);
	}
	
	private void setFlags() {
		if(italicAngle != 0) {
			flags |=  64;
	    }
		
	    if(isFixedPitch != 0) {
	    	flags |=  2;
	    }
	    
	 
	}
	@Override
	public String getFontName() {
		return fontName;
	}
	@Override
	public int getFlags() {
		return flags;
	}
	@Override
	public int getCapHeight() {
		return capHeight;
	}
	@Override
	public int getAscent() {
		return ascent;
	}
	@Override
	public int getDescent() {
		return descent;
	}
	@Override
	public int getItalicAngle() {
		return italicAngle;
	}
	@Override
	public int getStemV() {
		return stemV;
	}
	@Override
	public String getFontBBox() {
		return fontbbox.toString();
	}
	@Override
	public int getFirstChar() {
		return firstChar;
	}
	@Override
	public int getLastChar() {
		return lastChar;
	}
	@Override
	public String getWidths() {
		if(ansi == null)
			return null;
		
		return ansi.getMetrics(); 
	}
	@Override
	public int[] getDimensionWidths() {
		if(ansi == null)
			return null;
		
		return ansi.getArrayMetrics();
	}
}
