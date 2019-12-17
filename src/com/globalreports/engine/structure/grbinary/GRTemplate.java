/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.grpdf.GRTemplate
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

import java.util.Vector;

import com.globalreports.engine.err.GRBarcodeException;
import com.globalreports.engine.err.GRValidateException;
import com.globalreports.engine.objects.GRObject;
import com.globalreports.engine.objects.variable.GRText;
import com.globalreports.engine.objects.variable.GRVariableObject;
import com.globalreports.engine.structure.grbinary.data.GRData;
import com.globalreports.engine.structure.grpdf.GRContext;

public class GRTemplate {
	public static final short POSITION_NOTUSED	= 0;
	public static final short POSITION_UNDER	= 1;
	public static final short POSITION_OVER		= 2;
	
	/* Riferimento al document per accedere alle risorse esterne alla Page */
	protected GRDocument grdoc;
	private Vector<GRObject> grobj;
	
	private String stream;
	private double width;
	private double height;
	
	private String name;
	private short position;
	
	public GRTemplate(GRDocument doc) {
		this.grdoc = doc;
		
		init();
		
	}
	
	private void init() {
		// Di default A4 
		width = 595.28;
		height = 841.88;
				
		grobj = null;
		
		stream = "";
		position = POSITION_UNDER;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setPosition(String value) {
		if(value.equals("over"))
			this.position = POSITION_OVER;
		else
			this.position = POSITION_UNDER;
	}
	public void setPosition(short value) {
		this.position = value;
	}
	public short getPosition() {
		return position;
	}
	public String getStream(GRData grdata) throws GRValidateException, GRBarcodeException {
		StringBuffer content = new StringBuffer();
		Vector<String> stream;
		
		GRContext grcontext = GRContext.createContextMaster(width, height);
		if(grobj != null) {
			for(int i = 0;i < grobj.size();i++) {
				GRObject refObj = grobj.get(i);
				
				if(refObj.getType() == GRObject.TYPEOBJ_TEXT) {
					GRText refText = (GRText)refObj;
					refText.setData(grdata);
					
					stream = refText.draw(grcontext);
					content.append(stream.get(0));
				} else if(refObj.getType() == GRObject.TYPEOBJ_IMAGE || refObj.getType() == GRObject.TYPEOBJ_SHAPE) {
					
					stream = refObj.draw(grcontext);
					content.append(stream.get(0));
				}
				
			}
		}
		return content.toString();
	}
	public Vector<GRObject> getObject() {
		return grobj;
	}
	public void addObj(GRObject obj) {
		if(grobj == null)
			grobj = new Vector<GRObject>();
			
		grobj.add(obj);
	}
}
