/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.GRObject
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

import com.globalreports.engine.err.GRBarcodeException;
import com.globalreports.engine.err.GRValidateException;
import com.globalreports.engine.structure.grbinary.GRPage;
import com.globalreports.engine.structure.grpdf.GRContext;

public abstract class GRObject {
	public static final short TYPEOBJ_TEXT				= 1;
	public static final short TYPEOBJ_IMAGE				= 2;
	public static final short TYPEOBJ_SHAPE				= 3;
	public static final short TYPEOBJ_LIST				= 4;
	public static final short TYPEOBJ_TABLELIST			= 5;
	public static final short TYPEOBJ_TEXTCONDITION		= 6;
	public static final short TYPEOBJ_OBJCONDITION		= 7;	
	public static final short TYPEOBJ_CHART				= 8;
	public static final short TYPEOBJ_GROUP				= 9;
	public static final short TYPEOBJ_BARCODE			= 10;
	
	public static final short HPOSITION_ABSOLUTE		= 1;
	public static final short HPOSITION_RELATIVE		= 2;
	
	protected short type;
	protected String typography;
	protected short hposition;
	
	protected double maxHeight;
	
	public GRObject(short type) {
		this(type,GRPage.TYPOGRAPHY_POSTSCRIPT);
	}
	public GRObject(short type, String typography) {
		this.type = type;
		this.typography = typography;
		
		this.hposition = HPOSITION_ABSOLUTE;
	}
	
	public short getType() {
		return type;
	}
	public void setHPosition(String value) {
		if(value.equals("relative")) {
			this.setHPosition(HPOSITION_RELATIVE);
		} else {
			// In tutti gli altri casi imposta la posizione assoluta
			this.setHPosition(HPOSITION_ABSOLUTE);
		}
	}
	public void setHPosition(short value) {
		hposition = value;
	}
	public short getHPosition() {
		return hposition;
	}
	
	public abstract double getTop();
	public abstract double getMaxHeight();
	public abstract Vector<String> draw(GRContext grcontext) throws GRValidateException, GRBarcodeException;
	//public abstract String draw(GRContext grcontext) throws GRValidateException;
}