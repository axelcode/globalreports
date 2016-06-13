/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.grbinary.GRLayout
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

import com.globalreports.compiler.GRCompiler;
import com.globalreports.engine.err.*;
import com.globalreports.engine.structure.grpdf.GRAddressTable;
import com.globalreports.engine.structure.grbinary.GRDocument;
import com.globalreports.engine.structure.grbinary.data.GRData;
import com.globalreports.engine.objects.GRCircle;
import com.globalreports.engine.objects.GRImage;
import com.globalreports.engine.objects.GRLine;
import com.globalreports.engine.objects.GRObject;
import com.globalreports.engine.objects.GRRectangle;
import com.globalreports.engine.objects.GRShape;
import com.globalreports.engine.objects.variable.GRBarcode;
import com.globalreports.engine.objects.variable.GRChart;
import com.globalreports.engine.objects.variable.GRMapCondition;
import com.globalreports.engine.objects.variable.GRText;
import com.globalreports.engine.objects.variable.GRTextCondition;
import com.globalreports.engine.objects.variable.dynamic.GRGroup;
import com.globalreports.engine.objects.variable.dynamic.GRList;
import com.globalreports.engine.objects.variable.dynamic.GRTableList;
import com.globalreports.engine.objects.variable.dynamic.tablelist.GRTableListCell;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Vector;

public class GRLayout {
	private String pathLayout;
	private GRDocument grdocument;
	private GRData grdata;
		
	public GRLayout(String layout) throws GRLayoutException {
		this.pathLayout = layout;
		
		grdata = null;	// All'inizio imposta a null il modello di variabili
						// Potrebbe anche non essere presente
		
		/* Verifica il file passato come layout */
		if(layout.endsWith(".grx")) {
			/* E' un GRX. Passa dal compilatore */
			GRCompiler grcompiler = new GRCompiler(layout);
			
			try {
				grcompiler.compile();
				
				grdocument = grcompiler.getDocument();
			} catch(GRCompileException e) {
				
			}
		} else {
			this.read();
		}
	}
	public GRDocument getDocument() {
		return grdocument;
	}
	
	private void read() throws GRLayoutException {
		RandomAccessFile rLayout;
		GRAddressTable addressTable;
		byte[] grHeader = new byte[64];
		long fPointerAddressTable;
		int numberPages;
		int totaleImage;
		int totaleFont;
		int totaleObj;
		
		int lengthField;
		byte[] bufferImage;
		byte[] bufferFont;
		byte[] bufferText;
		byte[] bufferXml;
		byte[] bufferImg;
		byte[] bufferList;
		
		try {
			rLayout = new RandomAccessFile(pathLayout,"r");
			
			// Inizializza le strutture
			grdocument = new GRDocument();
			bufferFont = new byte[1024];
			bufferText = new byte[4096];
			bufferXml = new byte[256];
			bufferImg = new byte[256];
			bufferList = new byte[256];
			
			// HEADER
			// GR
			rLayout.read(grHeader);
			//System.out.println(new String(grHeader));
			
			// ADDRESS TABLE
			fPointerAddressTable = rLayout.readLong();
			
			addressTable = new GRAddressTable(fPointerAddressTable);
			rLayout.seek(fPointerAddressTable);	// Sposta il puntatore all'inizio dell'addressTable
			
			addressTable.setAddressImage(rLayout.readLong());
			addressTable.setAddressFont(rLayout.readLong());
			
			numberPages = rLayout.readInt();
		
			// DEPRECATED: Il numero di pagine viene aggiornato ogni volta
			// che viene chiamata GRDocument.addPage();
			//grdocument.setNumberPages(numberPages);
			
			for(int i = 0;i < numberPages;i++) {
				addressTable.addAddressPage(rLayout.readLong());
			}
			
			// Comincia ad acquisire le risorse
			// Images
			rLayout.seek(addressTable.getAddressImage());
			totaleImage = rLayout.readInt();
			
			for(int i = 0;i < totaleImage;i++) {
				grdocument.addImageProperty();
				
				// ID
				lengthField = rLayout.readInt();
				bufferImage = new byte[(int)lengthField];
				rLayout.read(bufferImage,0,lengthField);
				grdocument.setImageId(new String(bufferImage,0,lengthField));
				
				// TYPE
				grdocument.setImageType(rLayout.readShort());
				
				// ORIGINALWIDTH
				grdocument.setImageOriginalWidth(rLayout.readInt());
				
				// ORIGINALHEIGHT
				grdocument.setImageOriginalHeight(rLayout.readInt());
				
				// STREAM
				lengthField = rLayout.readInt();
				bufferImage = new byte[(int)lengthField];
				rLayout.read(bufferImage,0,lengthField);
				grdocument.setImageStream(bufferImage);
				
			}
			
			// Fonts
			rLayout.seek(addressTable.getAddressFont());
			totaleFont = rLayout.readInt();
				
			for(int i = 0;i < totaleFont;i++) {
				byte[] bufferStreamFont;
				grdocument.addFont();
				
				// ID
				lengthField = rLayout.readInt();
				rLayout.read(bufferFont,0,lengthField);
				grdocument.setFontId(new String(bufferFont,0,lengthField));
				
				// NAME
				lengthField = rLayout.readInt();
				rLayout.read(bufferFont,0,lengthField);
				grdocument.setFontName(new String(bufferFont,0,lengthField));
				
				// TYPE
				grdocument.setFontType(rLayout.readShort());
				
				// STREAM 
				grdocument.setFontLenOriginalStream(rLayout.readInt());
				lengthField = rLayout.readInt();
				grdocument.setFontLenCompressedStream(lengthField);
				bufferStreamFont = new byte[(int)lengthField];
				rLayout.read(bufferStreamFont,0,lengthField);
				grdocument.setFontStream(bufferStreamFont);
				
			}
	
			// Adesso acquisisce tutte le pagine ed i loro contenuti
			for(int i = 0;i < numberPages;i++) {
				rLayout.seek(addressTable.getAddressPage(i));
					
				grdocument.addPage();
					
				// WIDTH
				grdocument.setPageWidth(rLayout.readDouble());
				// HEIGHT
				grdocument.setPageHeight(rLayout.readDouble());
				// HEADER
				grdocument.setPageHeader(rLayout.readDouble());
				// FOOTER
				grdocument.setPageFooter(rLayout.readDouble());
			
				// Cicla per tutti gli oggetti contenuti nell'header della pagina
				totaleObj = rLayout.readInt();
				for(int t = 0;t < totaleObj;t++) {
					short type = rLayout.readShort();
					
					
					if(type == GRObject.TYPEOBJ_TEXT) {
						grdocument.addPageHeaderObj(this.getGRText(rLayout));
						
					} else if(type == GRObject.TYPEOBJ_IMAGE) {
						grdocument.addPageHeaderObj(this.getGRImage(rLayout));
						
					} else if(type == GRObject.TYPEOBJ_SHAPE) {
						grdocument.addPageHeaderObj(this.getGRShape(rLayout));
						
					} else {
						throw new GRLayoutObjectNotDefinedException(pathLayout,type);
					}
					
				}
				
				// Cicla per tutti gli oggetti contenuti nella pagina
				totaleObj = rLayout.readInt();
				for(int t = 0;t < totaleObj;t++) {
					short type = rLayout.readShort();
					
					if(type == GRObject.TYPEOBJ_TEXT) {
						grdocument.addPageObj(this.getGRText(rLayout));
						
					} else if(type == GRObject.TYPEOBJ_IMAGE) {
						grdocument.addPageObj(this.getGRImage(rLayout));
						
					} else if(type == GRObject.TYPEOBJ_SHAPE) {
						grdocument.addPageObj(this.getGRShape(rLayout));
						
					} else if(type == GRObject.TYPEOBJ_LIST) {
						grdocument.addPageObj(this.getGRList(rLayout));
						
					} else if(type == GRObject.TYPEOBJ_TABLELIST) {
						grdocument.addPageObj(this.getGRTableList(rLayout));
					
					} else if(type == GRObject.TYPEOBJ_TEXTCONDITION) {
						grdocument.addPageObj(this.getGRTextCondition(rLayout));
					
					} else if(type == GRObject.TYPEOBJ_CHART) {
						grdocument.addPageObj(this.getGRChart(rLayout));
					
					} else if(type == GRObject.TYPEOBJ_GROUP) {
						grdocument.addPageObj(this.getGRGroup(rLayout));
					
					} else if(type == GRObject.TYPEOBJ_BARCODE) {
						grdocument.addPageObj(this.getGRBarcode(rLayout));
						
					} else {
						throw new GRLayoutObjectNotDefinedException(pathLayout,type);
					}
					
				}
				
				// Cicla per tutti gli oggetti contenuti nel footer della pagina
				totaleObj = rLayout.readInt();
				for(int t = 0;t < totaleObj;t++) {
					short type = rLayout.readShort();
					
					if(type == GRObject.TYPEOBJ_TEXT) {
						grdocument.addPageFooterObj(this.getGRText(rLayout));
						
					} else if(type == GRObject.TYPEOBJ_IMAGE) {
						grdocument.addPageFooterObj(this.getGRImage(rLayout));
						
					} else if(type == GRObject.TYPEOBJ_SHAPE) {
						grdocument.addPageFooterObj(this.getGRShape(rLayout));
						
					} else {
						throw new GRLayoutObjectNotDefinedException(pathLayout,type);
					}
					
				}
				
			}
			
		} catch(FileNotFoundException fnfe) {
			throw new GRLayoutFileNotFoundException(pathLayout);
			
		} catch(IOException ioe) {
			throw new GRLayoutIOReadException(pathLayout);
			
		} 
		
	}
	
	private GRTableList getGRTableList(RandomAccessFile rLayout) throws IOException {
		short numColumns = 0;
		double minHeight;
		double widthStroke;
		double red;
		double green;
		double blue;
		
		long actualAddress;
		GRTableList grtablelist;
		
		double left = rLayout.readDouble();
		double top = rLayout.readDouble();
		short visible = rLayout.readShort();
		short hpos = rLayout.readShort();
		
		int lengthField = rLayout.readInt();	// Len dell'Id
		byte[] buffer = new byte[lengthField];
		rLayout.read(buffer,0,lengthField);
		
		short hasHead = rLayout.readShort();	
		short hasFooter = rLayout.readShort();	
		
		long addressExtend = rLayout.readLong();
		
		grtablelist = new GRTableList(left,top,new String(buffer,0,lengthField));
		grtablelist.setHPosition(hpos);
		grtablelist.setVisible(visible);
		
		short totColumn = rLayout.readShort();
		// Acquisisce tutte le colonne
		for(int i = 0;i < totColumn;i++) {
			grtablelist.addColumn();
			grtablelist.setColumnWidth(rLayout.readDouble());
		}
		
		if(hasHead == 1) {
			// HEAD
			numColumns = rLayout.readShort();
			minHeight = rLayout.readDouble();
			widthStroke = rLayout.readDouble();
			
			grtablelist.setHead();
			grtablelist.setHeadColumn(numColumns);
			grtablelist.setHeadMinHeight(minHeight);
			grtablelist.setHeadWidthStroke(widthStroke);
			
			red = rLayout.readDouble();
			green = rLayout.readDouble();
			blue = rLayout.readDouble();
			grtablelist.setHeadColorStroke(red, green, blue);
			
			red = rLayout.readDouble();
			green = rLayout.readDouble();
			blue = rLayout.readDouble();
			grtablelist.setHeadColorFill(red, green, blue);
			
			for(int i = 0;i < grtablelist.getHead().getColumns();i++) {
				grtablelist.addCellHead(readTableListCell(rLayout));
			}
		}
		
		
		// BODY
		numColumns = rLayout.readShort();
		minHeight = rLayout.readDouble();
		widthStroke = rLayout.readDouble();
		
		grtablelist.setBody();
		grtablelist.setBodyColumn(numColumns);
		grtablelist.setBodyMinHeight(minHeight);
		grtablelist.setBodyWidthStroke(widthStroke);
		
		red = rLayout.readDouble();
		green = rLayout.readDouble();
		blue = rLayout.readDouble();
		grtablelist.setBodyColorStroke(red, green, blue);
		
		red = rLayout.readDouble();
		green = rLayout.readDouble();
		blue = rLayout.readDouble();
		grtablelist.setBodyColorFill(red, green, blue);
		
		for(int i = 0;i < totColumn;i++) {
			grtablelist.addCellBody(readTableListCell(rLayout));
		}
		
		if(hasFooter == 1) {
			// FOOTER
			numColumns = rLayout.readShort();
			minHeight = rLayout.readDouble();
			widthStroke = rLayout.readDouble();
		
			grtablelist.setFooter();
			grtablelist.setFooterColumn(numColumns);
			grtablelist.setFooterMinHeight(minHeight);
			grtablelist.setFooterWidthStroke(widthStroke);
			
			red = rLayout.readDouble();
			green = rLayout.readDouble();
			blue = rLayout.readDouble();
			grtablelist.setFooterColorStroke(red, green, blue);
			
			red = rLayout.readDouble();
			green = rLayout.readDouble();
			blue = rLayout.readDouble();
			grtablelist.setFooterColorFill(red, green, blue);
			
			for(int i = 0;i < grtablelist.getFooter().getColumns();i++) {
				grtablelist.addCellFooter(readTableListCell(rLayout));
				
			}
		}
		
		
		return grtablelist;
		
	}
	private GRTableListCell readTableListCell(RandomAccessFile rLayout) throws IOException {
		GRTableListCell refCell = new GRTableListCell();
		
		// Totale colonne occupate dalla cella
		refCell.setColumns(rLayout.readShort());
		
		// Margini
		refCell.getMarginLeft(rLayout.readDouble());
		refCell.getMarginTop(rLayout.readDouble());
		refCell.getMarginRight(rLayout.readDouble());
		refCell.getMarginBottom(rLayout.readDouble());
		
		int totElement = rLayout.readInt();
		
		for(int i = 0;i < totElement;i++) {
			short type = rLayout.readShort();
			
			if(type == GRObject.TYPEOBJ_TEXT) {
				refCell.addObj(this.getGRText(rLayout));			
			} else if(type == GRObject.TYPEOBJ_SHAPE) {
				refCell.addObj(this.getGRShape(rLayout));
			} else if(type == GRObject.TYPEOBJ_IMAGE) {
				refCell.addObj(this.getGRImage(rLayout));
			} else if(type == GRObject.TYPEOBJ_LIST) {
				refCell.addObj(this.getGRList(rLayout));
			} else if(type == GRObject.TYPEOBJ_TABLELIST) {
				refCell.addObj(this.getGRTableList(rLayout));
			} else if(type == GRObject.TYPEOBJ_GROUP) {
				//refCell.addObj(this.getGRGroup(rLayout));
			}
		}
		
		return refCell;
	}
	private GRList getGRList(RandomAccessFile rLayout) throws IOException {
		GRList grlist;
		
		double topList = rLayout.readDouble();
		double heightList = rLayout.readDouble();
		short hpos = rLayout.readShort();
		
		int lengthField = rLayout.readInt();	// Len dell'Id
		byte[] buffer = new byte[lengthField];
		rLayout.read(buffer,0,lengthField);
		
		long addressExtend = rLayout.readLong();
		
		grlist = new GRList(topList,heightList,new String(buffer,0,lengthField));
		grlist.setHPosition(hpos);
		
		readListElement(grlist, rLayout);
		
		return grlist;
		
	}
	private void readListElement(GRList grlist, RandomAccessFile rLayout) throws IOException {
		int totElement = rLayout.readInt();
		
		for(int i = 0;i < totElement;i++) {
			short type = rLayout.readShort();
						
			if(type == GRObject.TYPEOBJ_TEXT) {
				grlist.addElement(this.getGRText(rLayout));			
			} else if(type == GRObject.TYPEOBJ_SHAPE) {
				grlist.addElement(this.getGRShape(rLayout));
			} else if(type == GRObject.TYPEOBJ_IMAGE) {
				grlist.addElement(this.getGRImage(rLayout));
			}
		}
	}
	private GRImage getGRImage(RandomAccessFile rLayout) throws IOException {
		double left = rLayout.readDouble();
		double top = rLayout.readDouble();
		double width = rLayout.readDouble();
		double height = rLayout.readDouble();
		short hpos = rLayout.readShort();
		
		int lengthField = rLayout.readInt();	// Len dell'Id
		byte[] buffer = new byte[lengthField];
		rLayout.read(buffer,0,lengthField);
		
		long addressExtend = rLayout.readLong();
		
		return new GRImage(left,top,width,height,hpos,new String(buffer,0,lengthField));
	}
	private GRTextCondition getGRTextCondition(RandomAccessFile rLayout) throws IOException {
		Vector<GRMapCondition> valueCondition = new Vector<GRMapCondition>();
		
		double leftText = rLayout.readDouble();
		double topText = rLayout.readDouble();
		double widthText = rLayout.readDouble();
		double heightText = rLayout.readDouble();
		short alignText = rLayout.readShort();
		short hposText = rLayout.readShort();
		double lineSpacingText = rLayout.readDouble();
		
		long addressExtend = rLayout.readLong();
		
		GRTextCondition grtextCondition = new GRTextCondition(leftText,topText,widthText,heightText,alignText,hposText,lineSpacingText);
		grtextCondition.setFontResources(grdocument.getFontResources());
		
		int totCondition = rLayout.readInt();	// Numero totale di condizioni presenti nell'oggetto
		for(int i = 0;i < totCondition;i++) {
			int lengthCondition = rLayout.readInt();
			byte[] bufferCondition = new byte[lengthCondition];
			rLayout.read(bufferCondition,0,lengthCondition);
			
			int lengthValue = rLayout.readInt();
			byte[] bufferValue = new byte[lengthValue];
			rLayout.read(bufferValue,0,lengthValue);
			
			grtextCondition.addElement(new String(bufferCondition,0,lengthCondition), new String(bufferValue,0,lengthValue));
			
		}
		
		return grtextCondition;
	}
	private GRText getGRText(RandomAccessFile rLayout) throws IOException {
		double leftText = rLayout.readDouble();
		double topText = rLayout.readDouble();
		double widthText = rLayout.readDouble();
		double heightText = rLayout.readDouble();
		short alignText = rLayout.readShort();
		short hposText = rLayout.readShort();
		double lineSpacingText = rLayout.readDouble();
		
		int lengthField = rLayout.readInt();	// Len della stringa di testo
		byte[] bufferText = new byte[lengthField];
		rLayout.read(bufferText,0,lengthField);
		
		long addressExtend = rLayout.readLong();
		
		GRText grtext = new GRText(leftText,topText,widthText,heightText,alignText,hposText,lineSpacingText,new String(bufferText,0,lengthField));
		
		grtext.setFontResources(grdocument.getFontResources());
		return grtext;
	}
	private GRShape getGRShape(RandomAccessFile rLayout) throws IOException {
		GRShape shape = null;
		short typeShape = rLayout.readShort();
		double widthStroke = rLayout.readDouble();
		double redColorStroke = rLayout.readDouble();
		double greenColorStroke = rLayout.readDouble();
		double blueColorStroke = rLayout.readDouble();
		double left = rLayout.readDouble();
		double top = rLayout.readDouble();
		short hpos = rLayout.readShort();
		
		long addressExtend = rLayout.readLong();
		
		if(typeShape == GRShape.TYPESHAPE_RECT) {
			double width = rLayout.readDouble();
			double height = rLayout.readDouble();
			double redColorFill = rLayout.readDouble();
			double greenColorFill = rLayout.readDouble();
			double blueColorFill = rLayout.readDouble();
			
			GRRectangle grrect = new GRRectangle();
			
			grrect.setWidthStroke(widthStroke);
			grrect.setPosition(left, top);
			grrect.setDimension(width, height);
			grrect.setColorStroke(redColorStroke, greenColorStroke, blueColorStroke);
			grrect.setColorFill(redColorFill, greenColorFill, blueColorFill);
			
			shape = grrect;
		} else if(typeShape == GRShape.TYPESHAPE_LINE) {
			double x2 = rLayout.readDouble();
			double y2 = rLayout.readDouble();
			
			GRLine grline = new GRLine();
			
			grline.setWidthStroke(widthStroke);
			grline.setX1(left);
			grline.setY1(top);
			grline.setX2(x2);
			grline.setY2(y2);
			grline.setColorStroke(redColorStroke, greenColorStroke, blueColorStroke);
			
			shape = grline;
		} else if(typeShape == GRShape.TYPESHAPE_CIRCLE) {
			double radius = rLayout.readDouble();
			
			double redColorFill = rLayout.readDouble();
			double greenColorFill = rLayout.readDouble();
			double blueColorFill = rLayout.readDouble();
			
			GRCircle grcircle = new GRCircle();
			
			grcircle.setWidthStroke(widthStroke);
			grcircle.setX(left);
			grcircle.setY(top);
			grcircle.setRadius(radius);
			grcircle.setColorStroke(redColorStroke, greenColorStroke, blueColorStroke);
			grcircle.setColorFill(redColorFill, greenColorFill, blueColorFill);
			
			shape = grcircle;
		}
		
		shape.setHPosition(hpos);
		
		return shape;
	}
	private GRBarcode getGRBarcode(RandomAccessFile rLayout) throws IOException {
		GRBarcode barcode = null;
		short typeBarcode = rLayout.readShort();
		
		double left = rLayout.readDouble();
		double top= rLayout.readDouble();
		double width= rLayout.readDouble();
		double height= rLayout.readDouble();
		short hposition = rLayout.readShort();

		int lengthField = rLayout.readInt();	// Len della stringa di testo
		byte[] bufferText = new byte[lengthField];
		rLayout.read(bufferText,0,lengthField);

		long addressExtend = rLayout.readLong();
		
		barcode = GRBarcode.createBarcode(typeBarcode);
		
		barcode.setLeft(left);
		barcode.setTop(top);
		barcode.setWidth(width);
		barcode.setHeight(height);
		barcode.setHPosition(hposition);
		barcode.setValue(new String(bufferText,0,lengthField));
		
		return barcode;
	}
	private GRChart getGRChart(RandomAccessFile rLayout) throws IOException {
		GRChart chart = null;
		short typeChart = rLayout.readShort();
		double widthStroke = rLayout.readDouble();
		double left = rLayout.readDouble();
		double top= rLayout.readDouble();
		double width= rLayout.readDouble();
		double height= rLayout.readDouble();
		short hposition = rLayout.readShort();
		short view = rLayout.readShort();
		int gap = rLayout.readInt();
		short legend = rLayout.readShort();
		
		// Campi opzionali
		short position = 0;
		int lengthTitle = 0;
		byte[] bufferTitle = null;
		int lengthIdTitle = 0;
		byte[] bufferIdTitle = null;
		int lengthIdVoice = 0;
		byte[] bufferIdVoice = null;
		byte[] bufferNameXml = null;
		
		if(legend == GRChart.LEGEND_VISIBLE) {
			position = rLayout.readShort();
			lengthTitle = rLayout.readInt();	// Len del titolo della legenda
			bufferTitle = new byte[lengthTitle];
			rLayout.read(bufferTitle,0,lengthTitle);
			
			/* Legge i font da utilizzare per i testi */
			lengthIdTitle = rLayout.readInt();
			bufferIdTitle = new byte[lengthIdTitle];
			rLayout.read(bufferIdTitle,0,lengthIdTitle);
			
			lengthIdVoice = rLayout.readInt();
			bufferIdVoice = new byte[lengthIdVoice];
			rLayout.read(bufferIdVoice,0,lengthIdVoice);
		}
		
		int lengthNameXml = rLayout.readInt();	// Len del nome del file xml
		
		if(lengthNameXml > 0) {
			bufferNameXml = new byte[lengthNameXml];
			rLayout.read(bufferNameXml,0,lengthNameXml);
		}
		
		/* Prima di leggere eventuali dati statici provvede a generare l'oggetto.
		 * Se sono presenti dati statici li aggiungerà una voce alla volta
		 */	
		chart = GRChart.createChart(typeChart, view);
		
		chart.setWidthStroke(widthStroke);
		chart.setLeft(left);
		chart.setTop(top);
		chart.setWidth(width);
		chart.setHeight(height);
		chart.setHPosition(hposition);
		chart.setGap(gap);
		/* LEGEND
		if(legend == GR) {
			chart.setLegend(new String(bufferTitle,0,lengthTitle), position);
			chart.setFontLegend(new String(bufferIdTitle,0,lengthIdTitle), new String(bufferIdVoice,0,lengthIdVoice));
		}
		*/
		
		if(lengthNameXml > 0)
			chart.setNameXml(new String(bufferNameXml,0,lengthNameXml));
		

		int totVoice = rLayout.readInt();
		if(totVoice > 0) {
			
			for(int i = 0;i < totVoice;i++) {
				int lengthLabel = rLayout.readInt();	// Len della label
				byte[] bufferLabel = new byte[lengthLabel];
				rLayout.read(bufferLabel,0,lengthLabel);
				
				double value = rLayout.readDouble();
				double cRED = rLayout.readDouble();
				double cGREEN = rLayout.readDouble();
				double cBLUE = rLayout.readDouble();
				
				chart.addVoice(new String(bufferLabel,0,lengthLabel), value, cRED, cGREEN, cBLUE);
			}	
		}
		
		long addressExtend = rLayout.readLong();
		
		return chart;
	}
	private GRGroup getGRGroup(RandomAccessFile rLayout) throws IOException {
		GRGroup grgroup = null;
		
		double left = rLayout.readDouble();
		double top = rLayout.readDouble();
		double height = rLayout.readDouble();
		short hpos = rLayout.readShort();
		int lenCondition = rLayout.readInt();
		byte[] buffer = null;
		
		if(lenCondition > 0) {
			buffer = new byte[lenCondition];
			rLayout.read(buffer,0,lenCondition);
			
		}
		long addressExtend = rLayout.readLong();
		
		int totObj = rLayout.readInt();
		
		grgroup = new GRGroup(left, top, height);
		grgroup.setHPosition(hpos);
		
		if(lenCondition > 0) {
			// C'è una condizione: la censisce
			grgroup.setCondition(new String(buffer,0,lenCondition));
		}
		// Cicla per tutti gli oggetti
		for(int i = 0;i < totObj;i++) {
			short type = rLayout.readShort();
			
			switch(type) {
				case GRObject.TYPEOBJ_SHAPE:
					grgroup.addElement(getGRShape(rLayout));
					
					break;
					
				case GRObject.TYPEOBJ_TEXT:
					grgroup.addElement(getGRText(rLayout));
					
					break;
					
				case GRObject.TYPEOBJ_LIST:
					grgroup.addElement(getGRList(rLayout));
					
					break;
			}
		}
		
		
		return grgroup;
	}

}
