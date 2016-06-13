/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.grpdf.GRPage
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
import com.globalreports.engine.objects.variable.dynamic.GRDynamicObject;
import com.globalreports.engine.structure.grbinary.data.GRData;
import com.globalreports.engine.structure.grpdf.GRContext;

public class GRPage {
	public static final String TYPOGRAPHY_MILLIMETERS			= "MM";
	public static final String TYPOGRAPHY_POSTSCRIPT			= "PS";
	
	/* Header */
	private double width;
	private double height;
	private double header;
	private double footer;
	private double marginTop;
	private double marginBottom;
	private double marginLeft;
	private double marginRight;
	
	protected StringBuffer content;
	
	/* Riferimento al document per accedere alle risorse esterne alla Page */
	protected GRDocument grdoc;
	private Vector<GRObject> grHeadObj;
	private Vector<GRObject> grobj;
	private Vector<GRObject> grFootObj;
	
	/*
	protected Vector<GRImage> grimage;
	protected Vector<GRShape> grshape;
	protected Vector<GRText> grtext;
	*/
	/* Variabili di gestione del contenuto nella pagina */
	private double dim;			// Dimensione attuale della linea di testo corrente
	private double hposition;	// Puntatore all'ultima altezza del testo inserito
	private double maxHeight;
	private GRContext grcontext;
	
	/* Stream di ritorno costruito a runtime.
	 * Lo stream potrebbe essere ripartito su pi� pagine fisiche (se si utilizzano oggetti tipo LIST)
	 * Ogni stream rappresenta una pagina fisica e viene racchiuso in un Vector
	 */
	private Vector<String> stream;
	
	public GRPage(GRDocument doc) {
		this.grdoc = doc;
		
		init();
		
	}
	
	private void init() {
		// Di default A4 
		width = 595.28;
		height = 841.88;
		
		header = 0.0;
		footer = 0.0;
		marginTop = 0.0;
		marginBottom = 0.0;
		marginLeft = 0.0;
		marginRight = 0.0;
		
		grHeadObj = null;
		grobj = null;
		grFootObj = null;
		
		content = new StringBuffer();
	}
	
	public Vector<GRObject> getHeaderObject() {
		return grHeadObj;
	}
	public Vector<GRObject> getBodyObject() {
		return grobj;
	}
	public Vector<GRObject> getFooterObject() {
		return grFootObj;
	}
	
	public void setWidth(double value) {
		width = value;
	}
	public double getWidth() {
		return width;
	}
	public void setHeight(double value) {
		height = value;
	}
	public double getHeight() {
		return height;
	}
	public void setHeader(double value) {
		header = value;
		
		marginTop = height - header;
	}
	public double getHeader() {
		return header;
	}
	public void setFooter(double value) {
		footer = value;
		
		marginBottom = footer;
	}
	public double getFooter() {
		return footer;
	}
	public double getMarginTop() {
		return marginTop;
	}
	public double getMarginBottom() {
		return marginBottom;
	}
	public Vector<String> getStreamPage() {
		return stream;
	}
	public void addHeaderObj(GRObject obj) {
		if(grHeadObj == null)
			grHeadObj = new Vector<GRObject>();
		
		grHeadObj.add(obj);
	}
	public void addObj(GRObject obj) {
		if(grobj == null)
			grobj = new Vector<GRObject>();
			
		grobj.add(obj);
	}
	public void addFooterObj(GRObject obj) {
		if(grFootObj == null)
			grFootObj = new Vector<GRObject>();
		
		grFootObj.add(obj);
	}
	
	public Vector<String> getContentStream(GRData grdata) throws GRValidateException, GRBarcodeException {
		StringBuffer content = new StringBuffer();
		
		// Uno stream per ogni sezione
		StringBuffer contentHEAD = new StringBuffer();
		StringBuffer contentBODY = new StringBuffer();
		StringBuffer contentFOOT = new StringBuffer();
		
		stream = new Vector<String>();
		
		// Istanzia il contesto grafico
		// Il master ha le dimensioni della pagina corrente
		GRContext grcontext = GRContext.createContextMaster(this.width, this.height);
		
		// HEADER
		// Cicla per tutti gli oggetti presenti nell'header
		if(grHeadObj != null) {
			GRContext grcontextHeader = grcontext.createContext(0, this.height, this.width, header);
			Vector<String> streamHEAD;
			
			for(int i = 0;i < grHeadObj.size();i++) {
				GRObject refObj = grHeadObj.get(i);
				
				if(refObj.getType() == GRObject.TYPEOBJ_TEXT) {
					GRText refText = (GRText)refObj;
					refText.setData(grdata);
					
					streamHEAD = refText.draw(grcontextHeader);
					contentHEAD.append(streamHEAD.get(0));
				} else if(refObj.getType() == GRObject.TYPEOBJ_IMAGE || refObj.getType() == GRObject.TYPEOBJ_SHAPE) {
					
					streamHEAD = refObj.draw(grcontextHeader);
					contentHEAD.append(streamHEAD.get(0));
				}
			}
		}
		
		// FOOTER
		// Cicla per tutti gli oggetti presenti nel footer
		if(grFootObj != null) {
			GRContext grcontextFooter = grcontext.createContext(0, this.height - (this.height-footer), this.width, footer);
			Vector<String> streamFOOT;
			
			for(int i = 0;i < grFootObj.size();i++) {
				GRObject refObj = grFootObj.get(i);
				
				if(refObj.getType() == GRObject.TYPEOBJ_TEXT) {
					GRText refText = (GRText)refObj;
					refText.setData(grdata);
					
					streamFOOT = refText.draw(grcontextFooter);
					contentFOOT.append(streamFOOT.get(0));
				} else if(refObj.getType() == GRObject.TYPEOBJ_IMAGE || refObj.getType() == GRObject.TYPEOBJ_SHAPE) {
					
					streamFOOT = refObj.draw(grcontextFooter);
					contentFOOT.append(streamFOOT.get(0));
				}
			}
		}
		
		// BODY
		GRContext grcontextBody = grcontext.createContext(0, this.height - header, this.width, this.height - (header + footer));
		if(grobj != null) {
			for(int i = 0;i < grobj.size();i++) {
				
				GRObject refObj = grobj.get(i);
				
				if(refObj instanceof GRVariableObject) {
					GRVariableObject grvariable = (GRVariableObject)refObj;
					grvariable.setData(grdata);
				}
				if(refObj instanceof GRDynamicObject) {
					((GRDynamicObject) refObj).setPage(this);
				}
				
				Vector<String> streamBODY = refObj.draw(grcontextBody);
				
				if(streamBODY.size() > 0) {
					contentBODY.append(streamBODY.get(0));
					
					if(streamBODY.size() > 1) {
						for(int index = 1;index < streamBODY.size();index++) {
							// Page Break
							content.append(contentHEAD.toString());
							content.append(contentBODY.toString());
							content.append(contentFOOT.toString());
							stream.add(content.toString());
							
							content.setLength(0);
							contentBODY.setLength(0);
							contentBODY.append(streamBODY.get(index));
						}
					}
				}
				
				
			}
		}
		
		content.append(contentHEAD.toString());
		content.append(contentBODY.toString());
		content.append(contentFOOT.toString());
		
		stream.add(content.toString());
		
		return stream;
		
	}
}
