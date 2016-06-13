/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.GRLine
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
package com.globalreports.engine.objects;

import java.util.Vector;

import com.globalreports.engine.structure.GRMeasures;
import com.globalreports.engine.structure.grpdf.GRContext;

public class GRLine extends GRShape {
	private double x1;
	private double y1;
	private double x2;
	private double y2;
	
	public GRLine() {
		super(GRShape.TYPESHAPE_LINE);
		
	}
	
	public void setX1(double value) {
		x1 = value;
	}
	public double getX1() {
		return x1;
	}
	public void setY1(double value) {
		y1 = value;
	}
	public double getY1() {
		return y1;
	}
	public void setX2(double value) {
		x2 = value;
	}
	public double getX2() {
		return x2;
	}
	public void setY2(double value) {
		y2 = value;
	}
	public double getY2() {
		return y2;
	}
	public double getTop() {
		if(y1 < y2)
			return y1;
		
		return y2;
	}
	public double getHeight() {
		return Math.abs(y1 - y2);
	}
	
	public double getMaxHeight() {
		return Math.abs(y1 - y2);
	}
	public Vector<String> draw(GRContext grcontext) {
		StringBuffer content = new StringBuffer();
		double left,top,height;
		
		Vector<String> stream = new Vector<String>();
		
		left = grcontext.getLeft();
		if(this.getHPosition() == GRObject.HPOSITION_ABSOLUTE) {
			top = grcontext.getTop();
		} else {
			top = grcontext.getHPosition();
		}
		
		height = top;
		
		// Disegna lo stream
		content.append(startObject());
		
		content.append(GRMeasures.arrotonda(left+this.getX1())+" "+GRMeasures.arrotonda(top-this.getY1())+" m\n");
		content.append(GRMeasures.arrotonda(left+this.getX2())+" "+GRMeasures.arrotonda(top-this.getY2())+" l\n");
		
		content.append("S\n");
		
		if(getY1() > getY2()) {
			grcontext.setHPosition(top - getY2());
		} else {
			grcontext.setHPosition(top - getY1());
		}
		grcontext.setMaxHeight(height+Math.abs(getY2() -getY1()));
		
		content.append(endObject());
		
		stream.add(content.toString());
		return stream;

	}
}
