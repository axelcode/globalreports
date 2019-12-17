/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.variable.barcode.GRQRCode
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
package com.globalreports.engine.objects.variable.barcode;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.globalreports.engine.err.GRBarcodeException;
import com.globalreports.engine.err.GRBarcodeValueIncorrectException;
import com.globalreports.engine.err.GRValidateException;
import com.globalreports.engine.objects.GRObject;
import com.globalreports.engine.objects.variable.GRBarcode;
import com.globalreports.engine.structure.GRMeasures;
import com.globalreports.engine.structure.grpdf.GRContext;

public class GRQRCode extends GRBarcode {
	private QRCode qrcode;
	
	public GRQRCode() {
		super(GRBarcode.TYPEBARCODE_QRCODE);
		
		QRMath.init();
	}
	
	public Vector<String> draw(GRContext grcontext) throws GRValidateException, GRBarcodeException {
		Vector<String> stream = new Vector<String>();
		StringBuffer content = new StringBuffer();
		double left,top,height;
		
		left = grcontext.getLeft();
		if(this.getHPosition() == GRObject.HPOSITION_ABSOLUTE) {
			top = grcontext.getTop();
		} else {
			top = grcontext.getHPosition();
		}
		
		height = top;
		
		content.append("q\n");
		content.append("0.0 w\n");
		content.append("0.0 0.0 0.0 RG\n");
		
		left = GRMeasures.arrotonda(left+this.getLeft());
		top = GRMeasures.arrotonda(top - this.getTop() - this.getHeight());
		
		// GENERA
		if(qrcode == null) {
			qrcode	= new QRCode();
			qrcode.addData(value);
			qrcode.make();
		}
		
		double tileW	= 128  / qrcode.getModuleCount();
		double tileH	= 128 / qrcode.getModuleCount();
		System.out.println("W: "+width);
		for( int row = 0; row < qrcode.getModuleCount(); row++ ){
			for( int col = 0; col < qrcode.getModuleCount(); col++ ){
				boolean fillStyle = qrcode.isDark(row, col);
				if(fillStyle) {
					content.append("0.0 0.0 0.0 RG\n");
					content.append("0.0 0.0 0.0 rg\n");
				} else {
					//content.append("1.0 1.0 1.0 RG\n");
					content.append("1.0 1.0 1.0 rg\n");
				}
				
				//ctx.fillStyle = qrcode.isDark(row, col) ? options.foreground : options.background;
				double w = (Math.ceil((col+1)*tileW) - Math.floor(col*tileW));
				double h = (Math.ceil((row+1)*tileW) - Math.floor(row*tileW));
				
				content.append((Math.round(col*tileW)+left)+" "+(Math.round(row*tileH)-(top - this.getTop()))*-1+" "+w+" "+h+" re\n");
				content.append("B\n");
				
				//g.fillRect((int)Math.round(col*tileW),(int)Math.round(row*tileH), (int)w, (int)h);
				
				//System.out.println("ROW: "+row+" - COL: "+col+" - "+w+" - "+h+" - COLOR: "+fillStyle);
			}	
		}
		
		
		
		grcontext.setHPosition(top);
		grcontext.setMaxHeight(height+this.getHeight());
		
		content.append("Q\n");
		//System.out.println(content.toString());
		stream.add(content.toString());		
		return stream;
	}
	
	public short getTypeBarcode() {
		return GRBarcode.TYPEBARCODE_QRCODE;
	}
}
