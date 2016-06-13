/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.text.GRParagraph
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
package com.globalreports.engine.objects.variable.text;

import java.util.Vector;

import com.globalreports.engine.structure.grbinary.GRDocument;

public class GRParagraph {
	private double left;
	private double top;
	private double height;
	private double width;
	private short alignment;
	private double lineSpacing;
	
	private Vector<GRRowParagraph> grrow;
	private GRRowParagraph refRow;
	
	public GRParagraph() {
		this(0, 0);
	}
	
	public GRParagraph(double left, double top) {
		this.left = left;
		this.top = top;
		
		height = 0;
		grrow = new Vector<GRRowParagraph>();
	}

	public void setLeft(double value) {
		left = value;
	}
	public double getLeft() {
		return left;
	}
	public void setTop(double value) {
		top = value;
	}
	public double getTop() {
		return top;
	}
	public short getAlignment() {
		return alignment;
	}
	public void setAlignment(short align) {
		this.alignment = align;
	}
	public double getLineSpacing() {
		return lineSpacing;
	}
	public void setLineSpacing(double value) {
		lineSpacing = value;
	}
	
	public void newRow() {
		// Aggiunge una riga di testo al paragrafo
		refRow = new GRRowParagraph();
		
		grrow.add(refRow);
	}
	public void addTextRow(GRTextRowParagraph value) {
		refRow.addTextRow(value);
	}
	public int getTotaleRow() {
		return grrow.size();
	}
	public void setAscentRow(int value) {
		refRow.setMaxAscent(value);
	}
	public int getAscentRow(int i) {
		return grrow.get(i).getMaxAscent();
	}
	public void setGapRow(double value) {
		refRow.setGap(value);
	}
	public double getGapRow(int i) {
		return grrow.get(i).getGap();
	}
	public double getGapRowSelected() {
		if(refRow == null)
			return -1.0;
		
		return refRow.getGap();
	}
	public int getTotaleTextRowSelected() {
		if(refRow == null)
			return 0;
		
		return refRow.getTotaleTextRow();
	}
	public double getWidthRowSelected() {
		if(refRow == null)
			return 0;
		
		return refRow.getWidth();
	}
	public GRRowParagraph getLineParagraph(int i) {
		return grrow.get(i);
	}
	public void addHeight(double value) {
		height += value;
	}
	public double getHeight() {
		return height;
	}
	public void setWidth(double value) {
		this.width = value;
	}
	public double getWidth() {
		return width;
	}
	
}
