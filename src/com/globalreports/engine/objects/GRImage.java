/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.GRImage
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

public class GRImage extends GRObject {
	private double left;
	private double top;
	private double width;
	private double height;
	private String id;
	
	public GRImage() {
		super(GRObject.TYPEOBJ_IMAGE);
		
		this.id = "";
	}
	public GRImage(double left, double top, double width, double height, short hpos, String id) {
		super(GRObject.TYPEOBJ_IMAGE);
		
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
		this.hposition = hpos;
		this.id = id;
	}
	
	public String getReferenceId() {
		return id;
	}
	public void setLeft(double value) {
		this.left = value;
	}
	public double getLeft() {
		return left;
	}
	public void setTop(double value) {
		this.top = value;
	}
	public double getTop() {
		return top;
	}
	public void setWidth(double value) {
		this.width = value;
	}
	public double getWidth() {
		return width;
	}
	public void setHeight(double value) {
		this.height = value;
	}
	public double getHeight() {
		return height;
	}
	public void setId(String value) {
		this.id = value;
	}
	public String getId() {
		return id;
	}
	
	public double getMaxHeight() {
		return height;
	}
	public Vector<String> draw(GRContext grcontext) {
		StringBuffer content = new StringBuffer();
		double left,top,height;
		
		Vector<String> stream = new Vector<String>();
		
		left = GRMeasures.arrotonda(grcontext.getLeft() + this.getLeft());
		if(getHPosition() == GRObject.HPOSITION_ABSOLUTE) {
			top = GRMeasures.arrotonda(grcontext.getTop() - this.getTop() - this.getHeight());
		} else {
			top = GRMeasures.arrotonda(grcontext.getHPosition() - this.getTop() - this.getHeight());
		}
		
		// Disegna lo stream
		content.append("q\n");
		content.append(this.getWidth()+" 0 0 "+this.getHeight()+" "+left+" "+top+" cm\n");
		content.append("/"+this.getId()+" Do\n");
		content.append("Q\n");
		
		grcontext.setHPosition(top);
		
		stream.add(content.toString());
		return stream;

	}

}
