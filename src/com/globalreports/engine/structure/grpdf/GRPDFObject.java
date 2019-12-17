/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.grpdf.GRPDFObject
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
package com.globalreports.engine.structure.grpdf;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalreports.engine.filter.GRFlateDecode;

public class GRPDFObject {
	public static final int TYPE_GENERIC	= 0;
	public static final int TYPE_CATALOG	= 1;
	public static final int TYPE_PAGES		= 2;
	public static final int TYPE_PAGE		= 3;
	public static final int TYPE_INFO		= 4;
	public static final int TYPE_OBJSTM		= 5;
	
	private final String REG_TYPEOBJ = "(\\/Type[ ]{0,}(\\/Catalog))|(\\/Type[ ]{0,}(\\/Pages))|(\\/Type[ ]{0,}(\\/Page[^s]))|(\\/Type[ ]{0,}(\\/ObjStm))";
	
	private int index;
	private int typeObject;
	private String dictionary;
	private byte[] stream;
	private boolean isStream;
	private byte[] streamDecompress;
	
	private Vector<GRPDFObject> objChild;	// Nel caso in cui il padre sia un objstm
	
	public GRPDFObject(int index) {
		this.index = index;
		
		isStream = false;
		typeObject = TYPE_GENERIC;
	}
	public void setDictionary(String value) {
		this.dictionary = value;
		
		setType(value);
	}
	public String getDictionary() {
		return dictionary;
	}
	public void setStream(byte[] value) {
		this.stream = value;
		
		isStream = true;
	}
	public byte[] getStream() {
		return stream;
	}
	public int getLenStream() {
		if(stream == null)
			return -1;
		
		return stream.length;
	}
	public boolean hasStream() {
		return isStream;
	}
	public int getIndex() {
		return index;
	}
	public int getType() {
		return typeObject;
	}
	private void setType(String value) {
		Pattern pattern;
		Matcher matcher;
		
		pattern = Pattern.compile(REG_TYPEOBJ);
		matcher = pattern.matcher(value);
			
		while(matcher.find()) {
			
			if(matcher.group(1) != null)
				typeObject = TYPE_CATALOG;
			else if(matcher.group(3) != null)
				typeObject = TYPE_PAGES;
			else if(matcher.group(5) != null)
				typeObject = TYPE_PAGE;
			else if(matcher.group(7) != null) {
				typeObject = TYPE_OBJSTM;
				
				setChildStream();
			}
		}
	}
	private void setChildStream() {
		String REG_OBJSTM = "((\\/First ([0-9]+))|(\\/Length ([0-9]+))|(\\/N ([0-9]+)))";
		int first = 0;
		int length = 0;
		int n = 0;
		String internalRef;
		
		Pattern pattern;
		Matcher matcher;
		
		pattern = Pattern.compile(REG_OBJSTM);
		matcher = pattern.matcher(dictionary);
			
		while(matcher.find()) {
			if(matcher.group(3) != null)	// 	First
				first = Integer.parseInt(matcher.group(3));
			else if(matcher.group(5) != null)	// 	Length
				length = Integer.parseInt(matcher.group(5));
			else if(matcher.group(7) != null)	//	N
				n = Integer.parseInt(matcher.group(7));
		}
		
		// Decomprime lo stream
		streamDecompress = GRFlateDecode.decode(stream, true);
	
		// Estrae la stringa dei riferimenti
		internalRef = new String(streamDecompress,0, first);
		String[] addrInternalRef = internalRef.split(" ");
		
		// Cicla per tutti gli oggetti contenuti e crea gli oggetti figli
		int start = first;
		int end = 0;
		int prevEnd = 0;
		int numObj = -1;
		
		objChild = new Vector<GRPDFObject>();
		
		for(int i = 0;i < n;i++) {
			end = Integer.parseInt(addrInternalRef[(2 * i)+1]) - prevEnd;
			
			if(end > 0) {
				GRPDFObject obj = new GRPDFObject(numObj);
				
				obj.setDictionary(new String(streamDecompress,start,end));
				objChild.add(obj);
			}
			numObj = Integer.parseInt(addrInternalRef[2 * i]);
			start = first + Integer.parseInt(addrInternalRef[(2 * i)+1]);
			prevEnd = Integer.parseInt(addrInternalRef[(2 * i)+1]);
		}
		end = streamDecompress.length - first - prevEnd;
		
		GRPDFObject obj = new GRPDFObject(numObj);
		
		obj.setDictionary(new String(streamDecompress,start,end));
		objChild.add(obj);
		
	}
	
	public int totalChild() {
		if(objChild == null)
			return -1;
		
		return objChild.size();
	}
	public GRPDFObject getObjStm(int index) {
		if(objChild == null)
			return null;
		
		return objChild.get(index);
	}
}
