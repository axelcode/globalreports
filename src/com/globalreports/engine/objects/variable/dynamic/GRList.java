/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.dynamic.GRList
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
import com.globalreports.engine.err.GRValidateException;
import com.globalreports.engine.objects.GRObject;
import com.globalreports.engine.objects.variable.GRText;
import com.globalreports.engine.structure.GRMeasures;
import com.globalreports.engine.structure.grbinary.data.GRDataList;
import com.globalreports.engine.structure.grbinary.data.GRDataRow;
import com.globalreports.engine.structure.grpdf.GRContext;

public class GRList extends GRDynamicObject {
	private double top;
	private double height;
	private String id;
	private Vector<GRObject> listElement;
	
	private double maxHeight;
	
	public GRList() {
		this(0.0, 0.0, null);
	}
	public GRList(double top, double height, String id) {
		super(GRObject.TYPEOBJ_LIST);
		
		this.top = top;
		this.height = height;
		this.id = id;
		
		listElement = new Vector<GRObject>();
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
	public void setId(String value) {
		this.id = value;
	}
	public String getId() {
		return id;
	}
	public double getMaxHeight() {
		return maxHeight;
	}
	public int getTotaleElement() {
		return listElement.size();
	}
	public void addElement(GRObject grelem) {
		listElement.add(grelem);
	}
	public GRObject getElement(int i) {
		return listElement.get(i);
	}
	
	public Vector<String> draw(GRContext grcontext) throws GRValidateException, GRBarcodeException {
		double maxHeightList = 0.0;	// La max height corrisponde al numero di righe stampate moltiplicato per GRList.getHeight();
		
		Vector<String> stream = new Vector<String>();
		
		if(grdata == null) 
			return stream;
		
		GRDataList dataList = grdata.getDataList(this.getId());
		if(dataList == null) {
			return stream;
		}
		
		StringBuffer content = new StringBuffer();
		StringBuffer contentRelative = new StringBuffer();
		
		// Crea un nuovo contesto grafico che userà per il contenuto della lista
		// Tale contesto avrà le seguenti caratteristiche:
		// LEFT: 0 --> grcontextMASTER.LEFT
		// TOP: relativo o assoluto
		// WIDTH: grcontextMASTER.WIDTH
		// HEIGHT: GRLIST.HEIGHT
		double top;
		
		if(this.getHPosition() == GRObject.HPOSITION_ABSOLUTE) {
			top = GRMeasures.arrotonda(grcontext.getTop() - this.getTop());
		} else {
			top = GRMeasures.arrotonda(grcontext.getHPosition() - this.getTop());
		}
		
		GRContext contextList = grcontext.createContext(grcontext.getLeft(),top, grcontext.getWidth(),this.getHeight());
		
		for(int i = 0;i < dataList.getTotaleElement();i++) {
			GRDataRow refRow = dataList.getElement(i);
		
			if(contextList.getTop()-this.getHeight() < grpage.getMarginBottom()) {
				/* Salto pagina */
				if(grfather == null) {
					// Salto pagina
					stream.add(content.toString());
					
					// Azzera lo stream
					content.setLength(0);
					
					// Posiziona il contesto dall'inizio del body
					contextList.setTop(grpage.getMarginTop());
					
				} else {
					// Se la lista � un frame all'interno di un altro oggetto, esce
					// restituendo null. Sar� l'oggetto chiamante a gestire il
					// salto pagina
					
					return null;
				}
			}
			for(int t = 0;t < this.getTotaleElement();t++) {
				GRObject refObj = this.getElement(t);
			
				short type = refObj.getType();
				Vector<String> streamObj;
				
				if(type == GRObject.TYPEOBJ_TEXT) {
					GRText grtext = (GRText)refObj;
					grtext.setData(refRow);
				}
				
				streamObj = refObj.draw(contextList);
				content.append(streamObj.get(0));
				
			}
			
			contextList.setTop(contextList.getTop() - this.getHeight());
			maxHeightList = maxHeightList + this.getHeight();
		}
			
		maxHeight = maxHeightList;
		
		// Aggiorna hposition
		grcontext.setHPosition(top - maxHeightList);
		
		stream.add(content.toString());
		return stream;
		
	}

}
