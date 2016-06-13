/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.grpdf.GRPDFPage
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

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalreports.engine.err.GRAttachmentIndexException;

public class GRPDFPage {
	private final String REG_MEDIABOX = "(\\/MediaBox[ ]{0,}\\[[ ]{0,}([0-9.]+) ([0-9.]+) ([0-9.]+) ([0-9.]+)[ ]{0,}\\])";
	private final String REG_CONTENTS = "(\\/Contents ([0-9]+) 0 R)";
	//private final String REG_RESOURCES = "(\\/Resources ([0-9]+) 0 R)";
	private final String REG_RESOURCES = "(\\/Resources ([0-9]+) 0 R)|(\\/Resources[ ]{0,}<<)";
	
	
	// Riferimenti ai contenuti
	private Vector<Integer> indexContents;
	
	private double width;
	private double height;
	private int indexRes;
	private String resources;
	
	private GRPDFObject obj;
	
	public GRPDFPage(GRPDFObject obj) throws GRAttachmentIndexException {
		this.obj = obj;
		
		indexContents = null;
		resources = null;
		
		this.init();
		
	}
	
	private void init() throws GRAttachmentIndexException {
		// MediaBox
		Pattern pattern;
		Matcher matcher;
		pattern = Pattern.compile(REG_MEDIABOX);
		matcher = pattern.matcher(obj.getDictionary());
		while(matcher.find()) {
			width = Double.parseDouble(matcher.group(4));
			height = Double.parseDouble(matcher.group(5));
			
		}
		
		// /Contents
		indexContents = GRPDFContentLibrary.getRef(obj.getDictionary(), "Contents");
		if(indexContents == null)
			throw new GRAttachmentIndexException("/Contents");
		
		// /Resources
		resources = obtainResource(obj);
		
	}
	public int getIndexResources() {
		return indexRes;
	}
	private String obtainResource(GRPDFObject obj) {
		StringBuffer buff = new StringBuffer();
		int index = -1;
		int pointer = 0;
		String valueTemp = "";
		
		Pattern pattern;
		Matcher matcher;
		String value = obj.getDictionary();
		
		
		pattern = Pattern.compile(REG_RESOURCES);
		matcher = pattern.matcher(value);
		while(matcher.find()) {
			if(matcher.group(1) == null) {
				index = value.indexOf("/Resources");
				value = value.substring(index+10);
				
			} else {
				indexRes = Integer.parseInt(matcher.group(2));
				return null;
			}
			
		}
		
		for(int i = 0;i < value.length();i++) {
			valueTemp = value.substring(i,i+1);
			
			if(valueTemp.equals("<")) {
				pointer++;
				
				if(pointer > 2)
					buff.append(valueTemp);
			} else if(valueTemp.equals(">")) {
				pointer--;
			
				if(pointer >= 2)
					buff.append(valueTemp);
				
				if(pointer <= 0)
					return buff.toString();
			} else {
				if(pointer >= 2)
					buff.append(valueTemp);
			}
			
		}
		return null;
	}
	
	public int getIndex() {
		return obj.getIndex();
	}
	public Vector<Integer> getIndexContents() {
		return indexContents;
	}
	public String getResources() {
		return resources;
	}
	public double getWidth() {
		return width;
	}
	public double getHeight() {
		return height;
	}
	
}
