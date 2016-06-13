/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.grbinary.GRFont
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
package com.globalreports.engine.structure.grbinary;

import com.globalreports.engine.filter.GRFlateDecode;
import com.globalreports.engine.structure.font.GRFontType;
import com.globalreports.engine.structure.font.encoding.GREncoding;
import com.globalreports.engine.structure.font.encoding.GRWinAnsiEncoding;
import com.globalreports.engine.structure.font.ttf.GRTrueType;

public class GRFont {
	public static final int TYPEPROP_ID			= 1;
	public static final int TYPEPROP_NAME		= 2;
	public static final int TYPEPROP_TYPE		= 3;
	
	private final short		SUBTYPE_TYPE1		= 1;
	private final short		SUBTYPE_TYPE2		= 2;
	private final short		SUBTYPE_TYPE3		= 3;
	private final short		SUBTYPE_TRUETYPE	= 5;
	private final short		SUBTYPE_TYPE0		= 10;
	
	private String id;
	private String name;
	private String type;
	
	private GRFontType grfontType;
	private byte[] streamFont;
	private int lenOriginalStream;
	private int lenCompressedStream;
	
	private GREncoding grencoding;
	
	public GRFont() {
		init();
	}
	private void init() {
		type = "Type1";

		grencoding = new GRWinAnsiEncoding();
	}
	public void setId(String value) {
		id = value;
	}
	public String getId() {
		return id;
	}
	public void setName(String value) {
		name = value;
		
	}
	public String getName() {
		return name;
	}
	public String getFontName() {
		return grfontType.getFontName();
	}
	public String getFontBBox() {
		return grfontType.getFontBBox();
	}
	public int getFlags() {
		return grfontType.getFlags();
	}
	public int getCapHeight() {
		return grfontType.getCapHeight();
	}
	public int getAscent() {
		return grfontType.getAscent();
	}
	public int getDescent() {
		return grfontType.getDescent();
	}
	public int getItalicAngle() {
		return grfontType.getItalicAngle();
	}
	public int getStemV() {
		return grfontType.getStemV();
	}
	public int getFirstChar() {
		return grfontType.getFirstChar();
	}
	public int getLastChar() {
		return grfontType.getLastChar();
	}
	public String getWidths() {
		return grfontType.getWidths();
	}
	public int[] getDimensionWidths() {
		return grfontType.getDimensionWidths();
	}
	public void setType(short value) {
		// Tipi riconosciuti:
		// Type1		-> 1
		// TrueType		-> 5
		if(value == SUBTYPE_TYPE1)
			type = "Type1";
		else if(value == SUBTYPE_TRUETYPE)
			type = "TrueType";
	}
	public String getType() {
		return type;
	}
	public short getCodeType() {
		if(type.equals("TrueType"))
			return SUBTYPE_TRUETYPE;
		
		return -1;
	}
	public String getEncoding() {
		return grencoding.getEncoding();
	}
	public void setStream(byte[] value) {
		streamFont = new byte[(int)value.length];
		
		for(int i = 0;i < value.length;i++)
			streamFont[i] = value[i];
		
		// Al momento riconosce solamente font TTF
		grfontType = new GRTrueType(GRFlateDecode.decode(streamFont,true));
	}
	public byte[] getStream() {
		return streamFont;
	}
	public void setLenOriginalStream(int value) {
		lenOriginalStream = value;
	}
	public int getLenOriginalStream() {
		return lenOriginalStream;
	}
	public void setLenCompressedStream(int value) {
		lenCompressedStream = value;
	}
	public int getLenCompressedStream() {
		return lenCompressedStream;
	}
	
	public int fromOctalToDecimal(String charcode) {
		return grencoding.fromOctalToDecimal(charcode);
	}
}