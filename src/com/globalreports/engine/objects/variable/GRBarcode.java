/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.variable.GRBarCode
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

import java.awt.geom.Point2D;
import java.util.Vector;

import com.globalreports.engine.err.GRBarcodeException;
import com.globalreports.engine.err.GRValidateException;
import com.globalreports.engine.objects.GRObject;
import com.globalreports.engine.objects.variable.barcode.GRBarcode128;
import com.globalreports.engine.objects.variable.barcode.GRBarcode39;
import com.globalreports.engine.objects.variable.barcode.GRQRCode;
import com.globalreports.engine.structure.GRColor;
import com.globalreports.engine.structure.GRMeasures;
import com.globalreports.engine.structure.grpdf.GRContext;

public abstract class GRBarcode extends GRVariableObject {
	public static final short TYPEBARCODE_CODE_39				= 1;
	public static final short TYPEBARCODE_QRCODE				= 2;
	public static final short TYPEBARCODE_CODE_128				= 3;
	
	protected double widthStroke;
	protected double left;
	protected double top;
	protected double width;
	protected double height;
	protected String value;
	
	protected short typeBarcode;
	protected int gap;
	protected String id;
	
	protected GRBarcode(short typeBarcode) {
		super(GRObject.TYPEOBJ_BARCODE);
		
		this.typeBarcode = typeBarcode;
	}
	
	public static GRBarcode createBarcode(String typeBarcode) {
		if(typeBarcode.equals("code39"))
			return GRBarcode.createBarcode(GRBarcode.TYPEBARCODE_CODE_39);
		else if(typeBarcode.equals("code128"))
			return GRBarcode.createBarcode(GRBarcode.TYPEBARCODE_CODE_128);
		else if(typeBarcode.equals("qrcode"))
			return GRBarcode.createBarcode(GRBarcode.TYPEBARCODE_QRCODE);
		return null;
	}
	public static GRBarcode createBarcode(short typeBarcode) {
		GRBarcode grbarcode = null;
		
		switch(typeBarcode) {
		case TYPEBARCODE_CODE_39:
			grbarcode = new GRBarcode39();
			break;
			
		case TYPEBARCODE_CODE_128:
			grbarcode = new GRBarcode128();
			break;
			
		case TYPEBARCODE_QRCODE:
			grbarcode = new GRQRCode();
			break;
			
		}
		
		return grbarcode;
	}
	public double getLeft() {
		return left;
	}
	public void setLeft(double left) {
		this.left = left;
	}
	public double getTop() {
		return top;
	}
	public void setTop(double top) {
		this.top = top;
	}
	public double getWidth() {
		return width;
	}
	public void setWidth(double width) {
		this.width = width;
	}
	public double getHeight() {
		return height;
	}
	public void setHeight(double height) {
		this.height = height;
	}
	public void setNameXml(String value) {
		this.id = value;
	}
	public void setWidthStroke(double value) {
		this.widthStroke = value;
	}
	public double getWidthStroke() {
		return widthStroke;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
	
	@Override
	public double getMaxHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Vector<String> draw(GRContext grcontext) throws GRValidateException, GRBarcodeException {
		Vector<String> stream = new Vector<String>();

		return stream;
	}
	
	/* Metodi astratti */
	public abstract short getTypeBarcode();
}
