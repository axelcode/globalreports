/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.variable.GRTextCondition
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
package com.globalreports.engine.objects.variable;

import java.util.Vector;

import com.globalreports.engine.err.GRTextConditionException;
import com.globalreports.engine.err.GRValidateException;
import com.globalreports.engine.objects.GRObject;
import com.globalreports.engine.structure.grbinary.data.GRData;
import com.globalreports.engine.structure.grbinary.data.GRParser;
import com.globalreports.engine.structure.grpdf.GRContext;

public class GRTextCondition extends GRVariableObject {
	private Vector<GRMapCondition> valueCondition;
	
	private double left;
	private double top;
	private double width;
	private double height;
	private short align;
	private double lineSpacing;
	
	public GRTextCondition() {
		super(GRObject.TYPEOBJ_TEXTCONDITION);
		
	}
	
	public GRTextCondition(double left, double top, double width, double height, short align, short hpos, double lineSpacing) {
		super(GRObject.TYPEOBJ_TEXTCONDITION);
		
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
		this.align = align;
		this.hposition = hpos;
		this.lineSpacing = lineSpacing;
		
		if(width <= 0.0)
			this.width = 80.0;
		
	}

	public double getLeft() {
		return left;
	}
	public void setLeft(double left) {
		this.left = left;
	}
	public double getTop() {
		/* Nella vecchia version ritorna 0.0 PERCHE'???? */
		return top;
	}
	public void setTop(double top) {
		this.top = top;
	}
	public double getHeight() {
		return height;
	}
	public double getWidth() {
		return width;
	}
	public void setWidth(double width) {
		this.width = width;
	}
	public short getAlign() {
		return align;
	}
	public void setAlign(short align) {
		this.align = align;
	}
	public void setAlign(String value) {
		if(value.equals("left")) {
			this.align = GRText.ALIGN_LEFT;
		} else if(value.equals("center")) {
			this.align = GRText.ALIGN_CENTER;
		} else if(value.equals("right")) {
			this.align = GRText.ALIGN_RIGHT;
		} else if(value.equals("justify")) {
			this.align = GRText.ALIGN_JUSTIFY;
		}
	}
	public void setLineSpacing(double value) {
		this.lineSpacing = value;
	}
	public double getLineSpacing() {
		return lineSpacing;
	}
	public int getTotalCondition() {
		return valueCondition.size();
	}
	public String getCondition(int i) {
		return valueCondition.get(i).getCondition();
	}
	public String getValueCondition(int i) {
		return valueCondition.get(i).getValueCondition();
	}
	public GRMapCondition getSelectCase(int i) {
		return valueCondition.get(i);
	}

	public void addElement(String condition, String value) {
		if(valueCondition == null)
			valueCondition = new Vector<GRMapCondition>();
		
		GRText grtext = new GRText(left, top, width, height, align, hposition, lineSpacing, value);
		grtext.setFontResources(grfont);
		valueCondition.add(new GRMapCondition(condition, grtext));
	}

	public double getMaxHeight() {
		return maxHeight;
	}
	public Vector<String> draw(GRContext grcontext) throws GRValidateException {
		maxHeight = 0.0;
		Vector<String> stream = new Vector<String>();
		
		if(valueCondition == null)
			return stream;
		
		GRParser grparser = new GRParser(grdata);
		
		for(int index = 0;index < this.getTotalCondition();index++) {
			GRMapCondition map = valueCondition.get(index);
			
			grparser.setCondition(map.getCondition());
			
			try {
				boolean v = grparser.verify();
				
				if(v) {
					map.getValue().setData(grdata);
					stream = map.getValue().draw(grcontext);
					maxHeight = map.getValue().getMaxHeight();
					
					break;
				}
			} catch(GRTextConditionException e) {
				
				break;
			}
		
		}
		
		return stream;
	}
}
