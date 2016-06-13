/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.variable.dynamic.GRGroup
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
package com.globalreports.engine.objects.variable.dynamic;

import java.util.Vector;

import com.globalreports.engine.err.GRBarcodeException;
import com.globalreports.engine.err.GRTextConditionException;
import com.globalreports.engine.err.GRValidateException;
import com.globalreports.engine.objects.GRObject;
import com.globalreports.engine.objects.variable.GRText;
import com.globalreports.engine.structure.GRMeasures;
import com.globalreports.engine.structure.grbinary.data.GRParser;
import com.globalreports.engine.structure.grpdf.GRContext;

public class GRGroup extends GRDynamicObject {
	private double left;
	private double top;
	private double height;	// Deprecated
	private String condition;
	
	private Vector<GRObject> grelement;
	
	public GRGroup() {
		this(0.0,0.0,0.0);
	}
	public GRGroup(double left, double top, double height) {
		super(GRObject.TYPEOBJ_GROUP);
		
		this.left = left;
		this.top = top;
		this.height = height;
		
		this.condition = null;
		grelement = new Vector<GRObject>();
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
	public void setHeight(double value) {
		this.height = value;
	}
	public double getHeight() {
		return height;
	}
	public void setCondition(String value) {
		this.condition = value;
	}
	public String getCondition() {
		return condition;
	}
	public int getTotaleElement() {
		return grelement.size();
	}
	public void addElement(GRObject grelem) {
		grelement.add(grelem);
	}
	public GRObject getElement(int i) {
		return grelement.get(i);
	}
	
	public double getMaxHeight() {
		// TO DO
		return 0.0;
	}
	public Vector<String> draw(GRContext grcontext) throws GRValidateException, GRBarcodeException {
		maxHeight = 0.0;
		boolean okStamp = false;
		
		StringBuffer content = new StringBuffer();
		
		Vector<String> stream = new Vector<String>();
		
		double top;
		
		if(this.getHPosition() == GRObject.HPOSITION_ABSOLUTE) {
			top = GRMeasures.arrotonda(grcontext.getTop() - this.getTop());
		} else {
			top = GRMeasures.arrotonda(grcontext.getHPosition() - this.getTop());
		}
		
		GRContext contextGroup = grcontext.createContext(grcontext.getLeft(),top, grcontext.getWidth(),top);
		
		if(grdata == null) {
			okStamp = true;
		} else {
			if(this.getCondition() == null) {
				okStamp = true;
			} else {
				GRParser grparser = new GRParser(grdata);
				grparser.setCondition(this.getCondition());
				
				try {
					okStamp = grparser.verify();
				} catch(GRTextConditionException e) {
					okStamp = false;
				}
			}
				
		}
		
		if(okStamp) {
			// Cicla per tutti gli oggetti contenuti nel contesto grafico
			int indexElement = 0;
			
			while(indexElement < this.getTotaleElement()) {
				
				GRObject refObj = this.getElement(indexElement);
				
				short type = refObj.getType();
				Vector<String> streamObj;
				
				if(type == GRObject.TYPEOBJ_TEXT) {
					GRText grtext = (GRText)refObj;
					grtext.setData(grdata);
					
				} 
				
				streamObj = refObj.draw(contextGroup);
				content.append(streamObj.get(0));
				
				if(maxHeight < (refObj.getTop() + refObj.getMaxHeight()))
					maxHeight = refObj.getTop() + refObj.getMaxHeight();
				
				// Verifica se ha oltrepassato il proprio contesto grafico
				if(top - maxHeight - grpage.getFooter() < 0) {
					stream.add("");	// Salto pagina
					
					// Azzera le strutture
					indexElement = 0;
					maxHeight = 0.0;
					content.setLength(0);
					
					top = GRMeasures.arrotonda(grcontext.getTop());
					contextGroup = grcontext.createContext(grcontext.getLeft(),top, grcontext.getWidth(),top);
					
				} else {
				
					indexElement++;
				}
			}
						
			// Il contesto lo aggiorna solamente se il contenuto grafico è stato renderizzato
			grcontext.setHPosition(top - maxHeight);
		} 
		
		stream.add(content.toString());
		return stream;
	}

}
