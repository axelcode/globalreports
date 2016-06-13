/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.GRRectangle
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

public class GRCircle extends GRShape {
	private final double GAPC = 0.55;
	
	private double x;
	private double y;
	private double redColorFill;
	private double greenColorFill;
	private double blueColorFill;
	private double radius;
	
	public GRCircle() {
		super(GRShape.TYPESHAPE_CIRCLE);
		
		this.init();
	}
	private void init() {
		redColorFill = -1.0;
		greenColorFill = -1.0;
		blueColorFill = -1.0;
	}

	public void setX(double value) {
		x = value;
	}
	public double getX() {
		return x;
	}
	public void setY(double value) {
		y = value;
	}
	public double getY() {
		return y;
	}
	public void setRadius(double value) {
		this.radius = value;
	}
	public double getRadius() {
		return radius;
	}
	public double getTop() {
		return y-radius;
	}
	public double getHeight() {
		return radius * 2;
	}
	public void setColorFill(double red, double green, double blue) {
		redColorFill = red;
		greenColorFill = green;
		blueColorFill = blue;
	}
	public String getColorFill() {
		return redColorFill+" "+greenColorFill+" "+blueColorFill;
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
	
	public double getMaxHeight() {
		return radius * 2;
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
		
		double xCircle = left + this.getX();
		double yCircle = top - this.getY();
		double rad = this.getRadius();
		
		// Disegna lo stream
		content.append(startObject());
		
		content.append(GRMeasures.arrotonda(xCircle)+" "+GRMeasures.arrotonda(yCircle - rad)+" m\n");
		content.append((xCircle + GAPC * rad)+" "+(yCircle - rad)+" "+(xCircle + rad)+" "+(yCircle - GAPC * rad)+" "+(xCircle + rad)+" "+yCircle+" c\n");
		content.append((xCircle + rad)+" "+(yCircle + GAPC * rad)+" "+(xCircle + GAPC * rad)+" "+(yCircle + rad)+" "+xCircle+" "+(yCircle + rad)+" c\n");
		content.append((xCircle - GAPC * rad)+" "+(yCircle + rad)+" "+(xCircle - rad)+" "+(yCircle + GAPC * rad)+" "+(xCircle - rad)+" "+yCircle+" c\n");
		content.append((xCircle - rad)+" "+(yCircle - GAPC * rad)+" "+(xCircle - GAPC * rad)+" "+(yCircle - rad)+" "+xCircle+" "+(yCircle - rad)+" c\n");
		
		if(isFillTransparent()) {
			content.append("S\n");
		} else {
			content.append(getColorFill()+" rg\n");
			content.append("B\n");
		}
		
		grcontext.setHPosition(top - radius);
		grcontext.setMaxHeight(yCircle+rad);
		
		content.append(endObject());
		
		stream.add(content.toString());
		return stream;
	}
	
}
