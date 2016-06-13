/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.dynamic.tablelist.GRTableListSection
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
package com.globalreports.engine.objects.variable.dynamic.tablelist;

import java.util.Vector;

public class GRTableListSection {
	public static final int TYPESECTION_HEAD	= 1;
	public static final int TYPESECTION_BODY	= 2;
	public static final int TYPESECTION_FOOT	= 3;
	
	private int type;
	
	private double widthStroke;
	private double redColorStroke;
	private double greenColorStroke;
	private double blueColorStroke;
	private double redColorFill;
	private double greenColorFill;
	private double blueColorFill;
	private double minHeight;
	private short numColumns;
	
	private Vector<GRTableListCell> grcell;
	
	public GRTableListSection(int type) {
		this.type = type;
		
		this.init();
	}
	private void init() {
		widthStroke = 0.5;
		redColorStroke = 0.0;
		greenColorStroke = 0.0;
		blueColorStroke = 0.0;
		
		redColorFill = -1.0;
		greenColorFill = -1.0;
		blueColorFill = -1.0;
		
		minHeight = 10.0;
		
		grcell = new Vector<GRTableListCell>();
	}
	
	public void setColumns(short value) {
		numColumns = value;
	}
	public short getColumns() {
		return numColumns;
	}
	public void setMinHeight(double value) {
		minHeight = value;
	}
	public double getMinHeight() {
		return minHeight;
	}
	public void setWidthStroke(double value) {
		widthStroke = value;
	}
	public double getWidthStroke() {
		return widthStroke;
	}
	public void setColorStroke(double red, double green, double blue) {
		redColorStroke = red;
		greenColorStroke = green;
		blueColorStroke = blue;
	}
	public String getColorStroke() {
		return redColorStroke+" "+greenColorStroke+" "+blueColorStroke+" RG";
	}
	public double getColorStrokeRED() {
		return redColorStroke;
	}
	public double getColorStrokeGREEN() {
		return greenColorStroke;
	}
	public double getColorStrokeBLUE() {
		return blueColorStroke;
	}
	public void setColorFill(double red, double green, double blue) {
		redColorFill = red;
		greenColorFill = green;
		blueColorFill = blue;
	}
	public String getColorFill() {
		return redColorFill+" "+greenColorFill+" "+blueColorFill+" rg";
	}
	public double getColorFillRED() {
		return redColorFill;
	}
	public double getColorFillGREEN() {
		return greenColorFill;
	}
	public double getColorFillBLUE() {
		return blueColorFill;
	}
	public boolean isFillTransparent() {
		if(redColorFill != -1.0 && greenColorFill != -1.0 && blueColorFill != -1.0)
			return false;
			
		return true;
	}
	public void addCell(GRTableListCell refCell) {
		grcell.add(refCell);
	}
	public GRTableListCell getCell(int i) {
		return grcell.get(i);
	}
	public int getTypeSection() {
		return type;
	}
}
