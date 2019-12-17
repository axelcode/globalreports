/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.grbinary.GRDocument
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

import com.globalreports.engine.structure.font.encoding.GREncoding;
import com.globalreports.engine.structure.font.encoding.GRWinAnsiEncoding;
import com.globalreports.engine.structure.grbinary.data.GRData;
import com.globalreports.engine.err.*;
import com.globalreports.engine.structure.grbinary.GRFont;
import com.globalreports.engine.structure.grbinary.GRImageProperty;
import com.globalreports.engine.objects.GRObject;
import com.globalreports.engine.objects.GRSystemObject;
import com.globalreports.engine.objects.variable.GRText;
import com.globalreports.engine.structure.grbinary.GRPage;

public class GRDocument {
	private Vector<GRTemplate> grtemplate;
	private Vector<GRPage> grpage;
	private Vector<GRImageProperty> grimageproperty;
	private Vector<GRFont> grfont;
	
	/* Riferimenti attuali */
	GRTemplate refTemplate;
	GRPage refPage;
	GRImageProperty refImageProperty;
	GRFont refFont;
	
	private int numberTemplates;
	private int numberPages;
	
	public GRDocument() {
		init();
	}
	
	private void init() {
		grtemplate = new Vector<GRTemplate>();
		grpage = new Vector<GRPage>();
		grimageproperty = new Vector<GRImageProperty>();
		grfont = new Vector<GRFont>();
		
		refTemplate = null;
		refPage = null;
		refImageProperty = null;
		refFont = null;
		
	}
	
	// TEMPLATE
	public void addTemplate() {
		refTemplate = new GRTemplate(this);
		grtemplate.add(refTemplate);
		
		numberTemplates++;
	}
	public void addTemplateObj(GRObject grobj) {
		refTemplate.addObj(grobj);
	}
	public void setTemplateName(String value) {
		refTemplate.setName(value);
	}
	public String getTemplateName(int i) {
		return grtemplate.get(i).getName();
	}
	public void setTemplatePosition(String value) {
		refTemplate.setPosition(value);
	}
	public void setTemplatePosition(short value) {
		refTemplate.setPosition(value);
	}
	public short getTemplatePosition(int i) {
		return grtemplate.get(i).getPosition();
	}
	public short getTemplatePosition(String name) {
		if(name == null)
			return GRTemplate.POSITION_NOTUSED;
		
		for(int i = 0;i < grtemplate.size();i++) {
			GRTemplate template = grtemplate.get(i);
			
			if(template.getName().equals(name)) {
				return template.getPosition();
			}
		}

		return GRTemplate.POSITION_NOTUSED;
	}
	public Vector<GRObject> getTemplateObject(int i) {
		return grtemplate.get(i).getObject();
	}
	public String getTemplateStream(String name,GRData grdata) throws GRValidateException, GRBarcodeException {
		/* Cicla finchè non trova il template.
		 * Se lo trova restituisce lo stream, altrimenti restituisce stringa vuota 
		 */
		if(name == null)
			return "";
		
		for(int i = 0;i < grtemplate.size();i++) {
			GRTemplate template = grtemplate.get(i);
			
			if(template.getName().equals(name)) {
				return template.getStream(grdata);
			}
		}

		return "";
	}
	public int getNumberTemplates() {
		return numberTemplates;
	}
	
	// PAGE
	public void addPage() {
		refPage = new GRPage(this);
		grpage.add(refPage);
		
		numberPages++;
	}
	public Vector<GRObject> getHeaderObject(int i) {
		return grpage.get(i).getHeaderObject();
	}
	public Vector<GRObject> getBodyObject(int i) {
		return grpage.get(i).getBodyObject();
	}
	public Vector<GRObject> getFooterObject(int i) {
		return grpage.get(i).getFooterObject();
	}
	public Vector<GRSystemObject> getSystemObject(int i) {
		return grpage.get(i).getSystemObject();
	}
	
	public void addPageTemplate(String name) {
		refPage.setTemplate(name);
	}
	public Vector<String> getPageTemplate(int i) {
		return grpage.get(i).getTemplate();
	}
	public void setPageWidth(double value) {
		refPage.setWidth(value);
	}
	public double getPageWidth(int i) {
		return grpage.get(i).getWidth();
	}
	public void setPageHeight(double value) {
		refPage.setHeight(value);
	}
	public double getPageHeight(int i) {
		return grpage.get(i).getHeight();
	}
	public void setPageHeader(double value) {
		refPage.setHeader(value);
	}
	public double getPageHeader(int i) {
		return grpage.get(i).getHeader();
	}
	public void setPageFooter(double value) {
		refPage.setFooter(value);
	}
	public double getPageFooter(int i) {
		return grpage.get(i).getFooter();
	}
	public void addPageHeaderObj(GRObject grobj) {
		refPage.addHeaderObj(grobj);
	}
	public void addPageObj(GRObject grobj) {
		refPage.addObj(grobj);
	}
	public void addPageFooterObj(GRObject grobj) {
		refPage.addFooterObj(grobj);
	}
	public void addPageSysObj(GRSystemObject grobj, int section) {
		refPage.addSysObj(grobj, section);
	}
	public Vector<String> getPageContent(int i, GRData grdata) throws GRValidateException, GRBarcodeException {
		return grpage.get(i).getContentStream(grdata);
		//return grpage.get(i).getContentText(grdata);
	}
	public void setNumberPages(int value) {
		numberPages = value;
	}
	public int getNumberPages() {
		return numberPages;
	}
	
	// IMAGE
	public void addImageProperty() {
		refImageProperty = new GRImageProperty();
		grimageproperty.add(refImageProperty);
	}
	public void setImageId(String value) {
		refImageProperty.setId(value);
	}
	public String getImageId(int i) {
		return grimageproperty.get(i).getId();
	}
	public void setImageType(short value) {
		refImageProperty.setType(value);
	}
	public short getImageType(int i) {
		return grimageproperty.get(i).getType();
	}
	public void setImageOriginalWidth(int value) {
		refImageProperty.setOriginalWidth(value);
	}
	public int getImageOriginalWidth(int i) {
		return grimageproperty.get(i).getOriginalWidth();
	}
	public void setImageOriginalHeight(int value) {
		refImageProperty.setOriginalHeight(value);
	}
	public int getImageOriginalHeight(int i) {
		return grimageproperty.get(i).getOriginalHeight();
	}
	public void setImageStream(byte[] value) {
		refImageProperty.setStream(value);
	}
	public byte[] getImageStream(int i) {
		return grimageproperty.get(i).getStream();
	}
	public byte[] getImageMask(int i) {
		return grimageproperty.get(i).getMask();
	}
	public int getImageSizeStream(int i) {
		return grimageproperty.get(i).getSizeImage();
	}
	public int getImageSizeMask(int i) {
		return grimageproperty.get(i).getSizeMask();
	}
	public int getTotaleImage() {
		return grimageproperty.size();
	}
	
	// FONT
	public void addFont() {
		refFont = new GRFont();
		grfont.add(refFont);
	}
	public void setFontId(String value) {
		refFont.setId(value);
	}
	public void setFontName(String value) {
		refFont.setName(value);
	}
	public void setFontType(short value) {
		refFont.setType(value);
	}
	public int getTotaleFont() {
		return grfont.size();
	}
	public String getFontId(int i) {
		return grfont.get(i).getId();
	}
	public String getBaseFont(int i) {
		return grfont.get(i).getName();
	}
	public String getFontName(int i) {
		return grfont.get(i).getFontName();
	}
	public String getFontType(int i) {
		return grfont.get(i).getType();
	}
	public short getFontCodeType(int i) {
		return grfont.get(i).getCodeType();
	}
	public String getFontEncoding(int i) {
		return grfont.get(i).getEncoding();
	}
	public String getFontBBox(int i) {
		return grfont.get(i).getFontBBox();
	}
	public int getFontFlags(int i) {
		return grfont.get(i).getFlags();
	}
	public int getFontCapHeight(int i) {
		return grfont.get(i).getCapHeight();
	}
	public int getFontAscent(int i) {
		return grfont.get(i).getAscent();
	}
	public int getFontDescent(int i) {
		return grfont.get(i).getDescent();
	}
	public int getFontItalicAngle(int i) {
		return grfont.get(i).getItalicAngle();
	}
	public int getFontStemV(int i) {
		return grfont.get(i).getStemV();
	}
	public int getFontFirstChar(int i) {
		return grfont.get(i).getFirstChar();
	}
	public int getFontLastChar(int i) {
		return grfont.get(i).getLastChar();
	}
	public String getFontWidths(int i) {
		return grfont.get(i).getWidths();
	}
	public int[] getFontDimensionWidths(String id) {
		for(int i = 0;i < grfont.size();i++) {
			if((grfont.get(i).getId().equals(id))) {
				return grfont.get(i).getDimensionWidths();
			}
					
		}
		
		return null;
	}
	public void setFontStream(byte[] value) {
		refFont.setStream(value);
	}
	public byte[] getFontStream(int i) {
		return grfont.get(i).getStream();
	}
	public void setFontLenOriginalStream(int value) {
		refFont.setLenOriginalStream(value);
	}
	public int getFontLenOriginalStream(int i) {
		return grfont.get(i).getLenOriginalStream();
	}
	public void setFontLenCompressedStream(int value) {
		refFont.setLenCompressedStream(value);
	}
	public int getFontLenCompressedStream(int i) {
		return grfont.get(i).getLenCompressedStream();
	}
	public Vector<GRFont> getFontResources() {
		return grfont;
	}
	
}
