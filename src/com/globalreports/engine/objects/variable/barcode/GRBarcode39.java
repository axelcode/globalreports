/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.variable.barcode.GRBarcode39
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

public class GRBarcode39 extends GRBarcode {
	private static final Map<Character, String> encoding = new HashMap<Character, String>();
	private static final int THIN	= 2;
	private static final int THICK	= 6;
	
	public GRBarcode39() {
		super(GRBarcode.TYPEBARCODE_CODE_39);
		
		/* Carica la Map */
		encoding.put('*',"bWbwBwBwb");
		encoding.put('0',"bwbWBwBwb");
		encoding.put('1',"BwbWbwbwB");
		encoding.put('2',"bwBWbwbwB");
		encoding.put('3',"BwBWbwbwb");
		encoding.put('4',"bwbWBwbwB");
		encoding.put('5',"BwbWBwbwb");
		encoding.put('6',"bwBWBwbwb");
		encoding.put('7',"bwbWbwBwB");
		encoding.put('8',"BwbWbwBwb");
		encoding.put('9',"bwBWbwBwb");
		
		encoding.put('A',"BwbwbWbwB");
		encoding.put('B',"bwBwbWbwB");
		encoding.put('C',"BwBwbWbwb");
		encoding.put('D',"bwbwBWbwB");
		encoding.put('E',"BwbwBWbwb");
		encoding.put('F',"bwBwBWbwb");
		encoding.put('G',"bwbwbWBwB");
		
		encoding.put('H',"BwbwbWBwb");
		encoding.put('I',"bwBwbWBwb");
		encoding.put('J',"bwbwBWBwb");
		encoding.put('K',"BwbwbwbWB");
		encoding.put('L',"bwBwbwbWB");
		encoding.put('M',"BwBwbwbWb");
		encoding.put('N',"bwbwBwbWB");
		
		encoding.put('O',"BwbwBwbWb");
		encoding.put('P',"bwBwBwbWb");
		encoding.put('Q',"bwbwbwBWB");
		encoding.put('R',"BwbwbwBWb");
		encoding.put('S',"bwBwbwBWb");
		encoding.put('T',"bwbwBwBWb");
		encoding.put('U',"BWbwbwbwB");
		
		encoding.put('V',"bWBwbwbwB");
		encoding.put('W',"BWBwbwbwb");
		encoding.put('X',"bWbwBwbwB");
		encoding.put('Y',"BWbwBwbwb");
		encoding.put('Z',"bWBwBwbwb");
		
		encoding.put(' ',"bWBwbwBwb");
		encoding.put('-',"bWbwbwBwB");
		encoding.put('$',"bWbWbWbwb");
		encoding.put('%',"bwbWbWbWb");
		encoding.put('.',"BWbwbwBwb");
		encoding.put('/',"bWbWbwbWb");
		encoding.put('+',"bWbwbWbWb");
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
		content.append("1.0 1.0 1.0 RG\n");
		
		left = GRMeasures.arrotonda(left+this.getLeft());
		top = GRMeasures.arrotonda(top - this.getTop() - this.getHeight());
		
		String valueBarcode = "*" + value + "*";
		
		double widthChar = width / valueBarcode.length();
		double singleToken = widthChar / 30;
		
		double widthTHIN = THIN * singleToken;
		double widthTHICK = THICK * singleToken;
		
		double widthRect = 0;
		
		if(grdata != null) 
			valueBarcode = grdata.addVariables(valueBarcode);
		
		for(int i = 0;i < valueBarcode.length();i++) {
			String code = encoding.get(valueBarcode.charAt(i));

			if(code == null)
				throw new GRBarcodeValueIncorrectException();
			
			for(int indexCode = 0;indexCode < code.length();indexCode++) {
				
				if(code.charAt(indexCode) == 'b' || code.charAt(indexCode) == 'B')
					content.append("0.0 0.0 0.0 rg\n");
				else
					content.append("1.0 1.0 1.0 rg\n");
				
				if(code.charAt(indexCode) == 'b' || code.charAt(indexCode) == 'w')
					widthRect = widthTHIN;
				else
					widthRect = widthTHICK;
				
				content.append(left+" "+top+" "+widthRect+" "+this.getHeight()+" re\n");
				content.append("B\n");
				
				left += widthRect;
				
			}
			
			if(i < valueBarcode.length() - 1) {
				content.append("1.0 1.0 1.0 rg\n");
				widthRect = widthTHIN;
				content.append(left+" "+top+" "+widthRect+" "+this.getHeight()+" re\n");
				content.append("B\n");
				
				left += widthRect;
				
			}
		}
		//content.append(left+" "+top+" "+this.getWidth()+" "+this.getHeight()+" re\n");
		
		/*
		content.append("0.0 0.0 0.0 rg\n");
		content.append("B\n");
		*/
		//content.append("S\n");
		
		grcontext.setHPosition(top);
		grcontext.setMaxHeight(height+this.getHeight());
		
		content.append("Q\n");
		
		stream.add(content.toString());		
		return stream;
	}
	
	public short getTypeBarcode() {
		return GRBarcode.TYPEBARCODE_CODE_39;
	}
}
