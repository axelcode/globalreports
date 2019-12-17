/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.dynamic.GRTableList
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
import com.globalreports.engine.objects.GRShape;
import com.globalreports.engine.objects.variable.GRText;
import com.globalreports.engine.objects.variable.GRTextCondition;
import com.globalreports.engine.objects.variable.dynamic.tablelist.GRTableListCell;
import com.globalreports.engine.objects.variable.dynamic.tablelist.GRTableListColumn;
import com.globalreports.engine.objects.variable.dynamic.tablelist.GRTableListRecord;
import com.globalreports.engine.objects.variable.dynamic.tablelist.GRTableListSection;
import com.globalreports.engine.structure.GRMeasures;
import com.globalreports.engine.structure.grbinary.data.GRDataList;
import com.globalreports.engine.structure.grbinary.data.GRDataRow;
import com.globalreports.engine.structure.grpdf.GRContext;

public class GRTableList extends GRDynamicObject {
	public static final short TYPEVISIBLE_ALWAYS	= 1;
	public static final short TYPEVISIBLE_ONLYDATA	= 2;
	
	private double left;
	private double top;
	private String id;
	private short visible;
		
	private Vector<GRTableListColumn> grtablelistColumn;
	private GRTableListSection grtablelistHead;
	private GRTableListSection grtablelistBody;
	private GRTableListSection grtablelistFooter;
	
	private GRTableListColumn refColumn;
	
	public GRTableList() {
		this(0.0, 0.0, null);
	}
	public GRTableList(double left, double top, String id) {
		super(GRObject.TYPEOBJ_TABLELIST);
		
		this.left = left;
		this.top = top;
		this.id = id;
		
		visible = TYPEVISIBLE_ALWAYS;
		
		grtablelistColumn = new Vector<GRTableListColumn>();
		grtablelistHead = null;
		grtablelistFooter = null;
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
	public void setId(String value) {
		this.id = value;
	}
	public String getId() {
		return id;
	}
	public void setVisible(String value) {
		if(value.equals("always"))
			this.visible = TYPEVISIBLE_ALWAYS;
		else if(value.equals("onlydata"))
			this.visible = TYPEVISIBLE_ONLYDATA;
	}
	public void setVisible(short visible) {
		this.visible = visible;
	}
	public short getVisible() {
		return visible;
	}
	public void addColumn() {
		refColumn = new GRTableListColumn();
		
		grtablelistColumn.add(refColumn);
	}
	public void setColumnWidth(double value) {
		refColumn.setWidth(value);
	}
	public double getColumnWidth(int i) {
		return grtablelistColumn.get(i).getWidth();
	}
	public int getTotaleColumn() {
		return grtablelistColumn.size();
	}
	
	// SECTION GENERIC
	public void setSectionColumn(short value, int section) {
		switch(section) {
			case GRTableListSection.TYPESECTION_HEAD:
				this.setHeadColumn(value);
				break;
				
			case GRTableListSection.TYPESECTION_BODY:
				this.setBodyColumn(value);
				break;
				
			case GRTableListSection.TYPESECTION_FOOT:
				this.setFooterColumn(value);
				break;
				
		}
	}
	public void setSectionMinHeight(double value, int section) {
		switch(section) {
			case GRTableListSection.TYPESECTION_HEAD:
				this.setHeadMinHeight(value);
				break;
				
			case GRTableListSection.TYPESECTION_BODY:
				this.setBodyMinHeight(value);
				break;
				
			case GRTableListSection.TYPESECTION_FOOT:
				this.setFooterMinHeight(value);
				break;
				
		}
	}
	public void setSectionWidthStroke(double value, int section) {
		switch(section) {
			case GRTableListSection.TYPESECTION_HEAD:
				this.setHeadWidthStroke(value);
				break;
				
			case GRTableListSection.TYPESECTION_BODY:
				this.setBodyWidthStroke(value);
				break;
				
			case GRTableListSection.TYPESECTION_FOOT:
				this.setFooterWidthStroke(value);
				break;
				
		}
	}
	public void setSectionColorStroke(double red, double green, double blue, int section) {
		switch(section) {
			case GRTableListSection.TYPESECTION_HEAD:
				this.setHeadColorStroke(red, green, blue);
				break;
				
			case GRTableListSection.TYPESECTION_BODY:
				this.setBodyColorStroke(red, green, blue);
				break;
				
			case GRTableListSection.TYPESECTION_FOOT:
				this.setFooterColorStroke(red, green, blue);
				break;
				
		}
	}
	public void setSectionColorFill(double red, double green, double blue, int section) {
		switch(section) {
			case GRTableListSection.TYPESECTION_HEAD:
				this.setHeadColorFill(red, green, blue);
				break;
				
			case GRTableListSection.TYPESECTION_BODY:
				this.setBodyColorFill(red, green, blue);
				break;
				
			case GRTableListSection.TYPESECTION_FOOT:
				this.setFooterColorFill(red, green, blue);
				break;
				
		}
	}
	public void addCellSection(GRTableListCell grcell, int section) {
		switch(section) {
			case GRTableListSection.TYPESECTION_HEAD:
				this.addCellHead(grcell);
				break;
				
			case GRTableListSection.TYPESECTION_BODY:
				this.addCellBody(grcell);
				break;
				
			case GRTableListSection.TYPESECTION_FOOT:
				this.addCellFooter(grcell);
				break;
				
		}
	}
	
	// HEAD
	public void setHead() {
		grtablelistHead = new GRTableListSection(GRTableListSection.TYPESECTION_HEAD);
	}
	public void setHeadColumn(short value) {
		grtablelistHead.setColumns(value);
	}
	public void setHeadMinHeight(double value) {
		grtablelistHead.setMinHeight(value);
	}
	public void setHeadWidthStroke(double value) {
		grtablelistHead.setWidthStroke(value);
	}
	public void setHeadColorStroke(double red, double green, double blue) {
		grtablelistHead.setColorStroke(red, green, blue);
	}
	public void setHeadColorFill(double red, double green, double blue) {
		grtablelistHead.setColorFill(red, green, blue);
	}
	public short hasHead() {
		if(grtablelistHead == null)
			return 0;
		
		return 1;
	}
	public GRTableListSection getHead() {
		return grtablelistHead;
	}
	public void addCellHead(GRTableListCell grcell) {
		grtablelistHead.addCell(grcell);
	}
	
	// BODY
	public void setBody() {
		grtablelistBody = new GRTableListSection(GRTableListSection.TYPESECTION_BODY);
	}
	public void setBodyColumn(short value) {
		grtablelistBody.setColumns(value);
	}
	public void setBodyMinHeight(double value) {
		grtablelistBody.setMinHeight(value);
	}
	public void setBodyWidthStroke(double value) {
		grtablelistBody.setWidthStroke(value);
	}
	public void setBodyColorStroke(double red, double green, double blue) {
		grtablelistBody.setColorStroke(red, green, blue);
	}
	public void setBodyColorFill(double red, double green, double blue) {
		grtablelistBody.setColorFill(red, green, blue);
	}
	public GRTableListSection getBody() {
		return grtablelistBody;
	}
	public void addCellBody(GRTableListCell grcell) {
		grtablelistBody.addCell(grcell);
	}
	
	// FOOTER
	public void setFooter() {
		grtablelistFooter = new GRTableListSection(GRTableListSection.TYPESECTION_FOOT);
	}
	public void setFooterColumn(short value) {
		grtablelistFooter.setColumns(value);
	}
	public void setFooterMinHeight(double value) {
		grtablelistFooter.setMinHeight(value);
	}
	public void setFooterWidthStroke(double value) {
		grtablelistFooter.setWidthStroke(value);
	}
	public void setFooterColorStroke(double red, double green, double blue) {
		grtablelistFooter.setColorStroke(red, green, blue);
	}
	public void setFooterColorFill(double red, double green, double blue) {
		grtablelistFooter.setColorFill(red, green, blue);
	}
	public short hasFooter() {
		if(grtablelistFooter == null)
			return 0;
		
		return 1;
	}
	public GRTableListSection getFooter() {
		return grtablelistFooter;
	}
	public void addCellFooter(GRTableListCell grcell) {
		grtablelistFooter.addCell(grcell);
	}

	public double getMaxHeight() {
		// TO DO
		return 0.0;
	}
	public Vector<String> draw(GRContext grcontext) throws GRValidateException, GRBarcodeException {
		GRDataList dataList = null;

		double top;
		
		Vector<String> stream = new Vector<String>();
		
		GRTableListRecord recordHEAD;
		GRTableListRecord recordFOOT;
		
		if(grdata != null) {
			dataList = grdata.getDataList(this.getId());
			
			if(dataList == null && this.getVisible() == GRTableList.TYPEVISIBLE_ONLYDATA)
				return stream;
		} else {
			if(this.getVisible() == GRTableList.TYPEVISIBLE_ONLYDATA)
				return stream;
		}
		
		StringBuffer THEAD = new StringBuffer();
		StringBuffer TBODY = new StringBuffer();
		
		if(this.getHPosition() == GRObject.HPOSITION_ABSOLUTE) {
			top = GRMeasures.arrotonda(grcontext.getTop() - this.getTop());
		} else {
			top = GRMeasures.arrotonda(grcontext.getHPosition() - this.getTop());
		}
		
		// HEAD
		recordHEAD = getStreamSection(grcontext, top, grtablelistHead);
		//System.out.println("H: "+recordFOOT.getHeight()+" - TOP: "+top+" - CONTEXT TOP: "+grcontext.getTop() + " - CONTEXT HEIGHT: "+grcontext.getHeight());
		if((top-recordHEAD.getHeight()) < (grcontext.getTop() - grcontext.getHeight())) {
			// Salto pagina
			stream.add(TBODY.toString());
			
			grcontext.setTop(grpage.getMarginTop());
			top = grcontext.getTop();
			
			// Rigenera l'Header
			recordHEAD = getStreamSection(grcontext, top, grtablelistHead);
		}
		top = top - recordHEAD.getHeight();
		
		// BODY
		if(dataList != null) {
			//for(int nRow = 0;nRow < dataList.getTotaleElement();nRow++) {
			int nRow = 0; 
			while(nRow < dataList.getTotaleElement()) {
				GRTableListRecord record = getStreamBody(grcontext, top, dataList.getElement(nRow));
				
				if(top - record.getHeight() < grpage.getMarginBottom()) {
					// Salto pagina
					stream.add(recordHEAD.getContentStream() + TBODY.toString());
					
					// Azzera gli stream
					THEAD.setLength(0);
					TBODY.setLength(0);
					
					// Posiziona il contesto dall'inizio del body
					grcontext.setTop(grpage.getMarginTop());
					top = grcontext.getTop();
						
					// Rigenera l'Header
					recordHEAD = getStreamSection(grcontext, top, grtablelistHead);
					top = top - recordHEAD.getHeight();
					
				} else {
					TBODY.append(record.getContentStream());
					
					top = GRMeasures.arrotonda(top - record.getHeight());
					
					nRow++;
				}
			}
		}

		// FOOT
		recordFOOT = getStreamSection(grcontext, top, grtablelistFooter);
		if((top-recordFOOT.getHeight()) < (grcontext.getTop() - grcontext.getHeight())) {
			
			// Salto pagina
			stream.add(recordHEAD.getContentStream() + TBODY.toString());
			
			grcontext.setTop(grpage.getMarginTop());
			top = grcontext.getTop();
			
			// Rigenera l'Header
			recordFOOT = getStreamSection(grcontext, top, grtablelistFooter);
			stream.add(recordFOOT.getContentStream());
		}
		top = top - recordFOOT.getHeight();
		
		stream.add(recordHEAD.getContentStream() + TBODY.toString() + recordFOOT.getContentStream());
		//stream.add(recordFOOT.getContentStream());
		
		grcontext.setHPosition(top);	// Aggiorna il contesto grafico

		//stream.add(recordHEAD.getContentStream() + TBODY.toString() + recordFOOT.getContentStream());
		return stream;
		
	}
	
	private GRTableListRecord getStreamSection(GRContext grcontext, double top, GRTableListSection grtablelistSection) throws GRValidateException, GRBarcodeException {
		double gapLeft;
		
		double left;
		double widthCell;
		double heightSection = 0.0;
				
		int indexColumn;
		GRTableListRecord record = new GRTableListRecord();
		
		if(grtablelistSection == null) 
			return record;
			
		indexColumn = 0;
		heightSection = grtablelistSection.getMinHeight();
		gapLeft = 0.0;
		
		int i = 0;
		
		while(i < grtablelistSection.getColumns()) {
			GRTableListCell refCell = grtablelistSection.getCell(i);

			//double dimCell = this.getColumnWidth(i) - refCell.getMarginLeft() - refCell.getMarginRight();
			/* Questa gestione sostituisce quella della riga sopra
			 * Questo perchè la cella potrebbe espandersi su più colonne
			 * quindi la sua dimensione è la somma dei valori delle colonne coinvolte
			 */
			double dimCell = 0.0;
			
			int index = i;
			for(int wCell = 0;wCell < refCell.getColumns();wCell++) {
				dimCell = dimCell + this.getColumnWidth(index);
				
				index++;
				
			}
			dimCell = dimCell - refCell.getMarginLeft() - refCell.getMarginRight();
			
			
			double cLeft = grcontext.getLeft() + this.getLeft() + refCell.getMarginLeft() + gapLeft;
			double cTop = top-refCell.getMarginTop();
			double cWidth = this.getColumnWidth(i);
			double cHeight = -1.0;
			
			GRContext contextCell = grcontext.createContext(cLeft, cTop, cWidth, cHeight);
			
			for(int nObj = 0;nObj < refCell.getTotaleElement();nObj++) {
				GRObject refObj = refCell.getElement(nObj);
				
				short type = refObj.getType();
				Vector<String> streamObj;
			
				if(type == GRObject.TYPEOBJ_TEXT) {
					
					GRText refText = (GRText)refObj;
					refText.setData(grdata);
					
					if(refText.getWidth() > dimCell)
						refText.setWidth(dimCell); 	// Se la larghezza del testo eccede quella della cella
													// la adatta automaticamente
					
					streamObj = refText.draw(contextCell);
					record.addData(streamObj.get(0));
					
					if(refText.getTop()+refText.getMaxHeight()+refCell.getMarginTop()+refCell.getMarginBottom() > heightSection) {
						heightSection = refText.getTop()+refText.getMaxHeight()+refCell.getMarginTop()+refCell.getMarginBottom();
					}
				} else if(type == GRObject.TYPEOBJ_TEXTCONDITION) {
					
					GRTextCondition refTextCondition = (GRTextCondition)refObj;
					refTextCondition.setData(grdata);
					
					if(refTextCondition.getWidth() > dimCell)
						refTextCondition.setWidth(dimCell); 	// Se la larghezza del testo eccede quella della cella
													// la adatta automaticamente
					
					streamObj = refTextCondition.draw(contextCell);
					if(streamObj.size() > 0) {
						record.addData(streamObj.get(0));
					
						if(refTextCondition.getTop()+refTextCondition.getMaxHeight()+refCell.getMarginTop()+refCell.getMarginBottom() > heightSection) {
							heightSection = refTextCondition.getTop()+refTextCondition.getMaxHeight()+refCell.getMarginTop()+refCell.getMarginBottom();
						}
					}
				} else if(type == GRObject.TYPEOBJ_SHAPE) {
					GRShape refShape = (GRShape)refObj;
					
					streamObj = refShape.draw(contextCell);
					record.addData(streamObj.get(0));
					
					if(refShape.getTop()+refShape.getHeight()+refCell.getMarginTop()+refCell.getMarginBottom() > heightSection) {
						heightSection = refShape.getTop()+refShape.getHeight()+refCell.getMarginTop();
					}
				}
				
			}
			
			for(int wCell = 0;wCell < refCell.getColumns();wCell++) {
				
				gapLeft = gapLeft + this.getColumnWidth(indexColumn);
				indexColumn++;
			}
			
			i++;
		}
		
		indexColumn = 0;
		
		// Inserisce lo sfondo
		left = grcontext.getLeft()+this.getLeft();
		top = GRMeasures.arrotonda(top-heightSection);
					
		for(int iCol = 0;iCol < grtablelistSection.getColumns();iCol++) {
									
			GRTableListCell refCell = grtablelistSection.getCell(iCol);
			
			widthCell = 0.0;
			for(int wCell = 0;wCell < refCell.getColumns();wCell++) {
				widthCell = widthCell + this.getColumnWidth(indexColumn);
				indexColumn++;
			}
			
			record.addBackground("q\n");
			record.addBackground(grtablelistSection.getWidthStroke()+" w\n");
			record.addBackground(grtablelistSection.getColorStroke()+"\n");
				
			record.addBackground(left+" "+top+" "+widthCell+" "+heightSection+" re\n");
			
			if(grtablelistSection.isFillTransparent()) {
				record.addBackground("S\n");
			} else {
				record.addBackground(grtablelistSection.getColorFill()+"\n");
				record.addBackground("B\n");
			}
			
			record.addBackground("Q\n");
			
			left = left + widthCell;
			
		}
		
		record.setHeight(heightSection);
		return record;
	}
	
	private GRTableListRecord getStreamBody(GRContext grcontext, double top, GRDataRow dataRow) throws GRValidateException, GRBarcodeException {
		double gapLeft;
		
		double left;
		double widthCell;
		double heightHead = 0.0;
		double heightBody = 0.0;
		double heightFooter = 0.0;
		boolean saltoPagina = false;
		
		int indexColumn;
		
		GRTableListRecord record = new GRTableListRecord();
		
		if(dataRow != null) {
			gapLeft = 0.0;
			heightBody = grtablelistBody.getMinHeight();
			
			// Cicla per tutte le colonne della tabella
			int i = 0;
			while(i < this.getTotaleColumn()) {
				GRTableListCell refCell = grtablelistBody.getCell(i);
				
				double cLeft = grcontext.getLeft() + this.getLeft() + refCell.getMarginLeft() + gapLeft;
				double cTop = top-refCell.getMarginTop();
				double cWidth = this.getColumnWidth(i);
				double cHeight = -1.0;
				
				GRContext contextCell = grcontext.createContext(cLeft, cTop, cWidth, cHeight);
				
				int nObj = 0;
				// Cicla per tutti gli elementi presenti in ogni singola cella
				while(nObj < refCell.getTotaleElement() && !saltoPagina) {
					GRObject refObj = refCell.getElement(nObj);
					
					short type = refObj.getType();
					Vector<String> streamObj;
					
					if(type == GRObject.TYPEOBJ_TEXT) {
						GRText refText = (GRText)refObj;
						refText.setData(dataRow);

						streamObj = refText.draw(contextCell);	
						record.addData(streamObj.get(0));
						
						if((refText.getTop()+refText.getMaxHeight()+refCell.getMarginTop()+refCell.getMarginBottom()) > heightBody)
							heightBody = refText.getTop()+refText.getMaxHeight()+refCell.getMarginTop()+refCell.getMarginBottom();
					
					} else if(type == GRObject.TYPEOBJ_SHAPE) {
						GRShape refShape = (GRShape)refObj;
						
						streamObj = refShape.draw(contextCell);	
						record.addData(streamObj.get(0));
						
						if((refShape.getTop()+refShape.getHeight()+refCell.getMarginTop()) > heightBody)
							heightBody = refShape.getTop()+refShape.getHeight()+refCell.getMarginTop();
					} else if(type == GRObject.TYPEOBJ_LIST) {
						
						GRList refList = (GRList)refObj;
						refList.setData(dataRow);
						refList.setPage(grpage);
						
						streamObj = refList.draw(contextCell);
						if(streamObj.size() > 0) {
							record.addData(streamObj.get(0));
							
							if((refList.getTop()+refList.getMaxHeight()+refCell.getMarginTop()) > heightBody)
								heightBody = refList.getTop()+refList.getMaxHeight()+refCell.getMarginTop();
						}
					} else if(type == GRObject.TYPEOBJ_TABLELIST) {
						
					} else if(type == GRObject.TYPEOBJ_GROUP) {
						
					}
					
					nObj++;
				}
				
				gapLeft = gapLeft + this.getColumnWidth(i);
				i++;
			}	
			
			left = grcontext.getLeft()+this.getLeft();
			
			for(int iColumn = 0;iColumn < this.getTotaleColumn();iColumn++) {
				record.addBackground("q\n");
				record.addBackground(grtablelistBody.getWidthStroke()+" w\n");
				record.addBackground(grtablelistBody.getColorStroke()+"\n");
					
				record.addBackground(left+" "+(top-heightBody)+" "+this.getColumnWidth(iColumn)+" "+heightBody+" re\n");
				
				if(grtablelistBody.isFillTransparent()) {
					record.addBackground("S\n");
				} else {
					record.addBackground(grtablelistBody.getColorFill()+"\n");
					record.addBackground("B\n");
				}
				
				record.addBackground("Q\n");
				
				left = left + this.getColumnWidth(iColumn);
				
			}
			
		}	
			
		record.setHeight(heightBody);
		return record;
	}
	
}
