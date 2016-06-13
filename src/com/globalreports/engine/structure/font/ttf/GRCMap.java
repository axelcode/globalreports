/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.font.ttf.GRCMap
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

import java.util.HashMap;

import com.globalreports.engine.structure.font.GRFontDataStream;


public class GRCMap {
	public static final int FORMAT_0		= 0;
	public static final int FORMAT_2		= 2;
	public static final int FORMAT_4		= 4;
	public static final int FORMAT_6		= 6;
	public static final int FORMAT_8		= 8;
	public static final int FORMAT_10		= 10;
	public static final int FORMAT_12		= 12;
	public static final int FORMAT_13		= 13;
	public static final int FORMAT_14		= 14;
	
	private int platformID;
    private int platformSpecificID;
    private long offset;
    
    private int format;
    private long length;
    private long language;
    
    private HashMap<Integer,Integer> indexGlyph;
    
    public GRCMap(int platformID, int platformSpecificID, long offset) {
    	this.platformID = platformID;
    	this.platformSpecificID = platformSpecificID;
    	this.offset = offset;
    }
    
    public void parse(GRFontDataStream raf, long offset) {
    	int actualAddress = raf.getFilePointer();
		raf.seek((int)(offset + this.offset));
		
		format = raf.readUShort();
		if(format < 8) {
			length = raf.readUShort();
			language = raf.readUShort();
		} else {
			raf.readUShort();	// a perdere
			length = raf.readUInt();
			language = raf.readUInt();
		}
		
		switch(format) {
		case FORMAT_0:
			break;
			
		case FORMAT_2:
			break;
			
		case FORMAT_4:
			this.format4(raf);
			break;
			
		case FORMAT_6:
			break;
			
		case FORMAT_8:
			break;
			
		case FORMAT_10:
			break;
			
		case FORMAT_12:
			break;
			
		case FORMAT_13:
			break;
			
		case FORMAT_14:
			break;
			
		}
		
		raf.seek(actualAddress);
    }
    
    private void format4(GRFontDataStream raf) {
    	int segCountX2;
        int searchRange;
        int entrySelector;
        int rangeShift;
        int[] endCode;
        int reservedPad;
        int[] startCode;
        int[] idDelta;
        int[] idRangeOffset;
        int[] glyphIndexArray;
        
        indexGlyph = new HashMap<Integer, Integer>();
        
        segCountX2 = raf.readUShort();
        int segCount = segCountX2 / 2;
        
        int counter = 32;
        
        searchRange = raf.readUShort();
        entrySelector = raf.readUShort();
        rangeShift = raf.readUShort();
        endCode = raf.readUShortArray(segCount);
        reservedPad = raf.readUShort();
        startCode = raf.readUShortArray(segCount);
        idDelta = raf.readUShortArray(segCount);
        idRangeOffset = raf.readUShortArray(segCount);
        
        int actualAddress = raf.getFilePointer();
        for(int i = 0;i < segCount;i++) {
        	int start = startCode[i];
            int end = endCode[i];
            int delta = idDelta[i];
            int rangeOffset = idRangeOffset[i];
            if (start != 65535 && end != 65535) {
            	//System.out.println("CICLO DA "+start+" A "+end);
            	for(int j = start;j <= end;j++) {
            		int glyphIndex;
            		
            		if(rangeOffset == 0) {
            			glyphIndex = (j + delta) % 65536;
            			
            			if(start == 402)
            			//System.out.println(counter+" : "+glyphid);
            			counter++;
            		} else {
            			long glyphOffset = actualAddress + ((rangeOffset / 2) +
                                (j - start) + 
                                (i - segCount)) * 2;
                        raf.seek((int)glyphOffset);
                        glyphIndex = raf.readUShort();
                        
                        if (glyphIndex != 0) {
                            glyphIndex += delta;
                            glyphIndex %= 65536;
                            
                        }
            		}
            		
            		indexGlyph.put(j, glyphIndex);
            	}
            	
            	
            }
        }
    }
    
    public int getFormat() {
    	return format;
    }
    public Integer getIndexGlyph(int index) {
    	if(indexGlyph == null)
    		return null;
    	
    	return indexGlyph.get(index);
    }
}
