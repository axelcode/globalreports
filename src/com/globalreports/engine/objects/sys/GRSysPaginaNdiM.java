/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.sys.GRSysPaginaNdiM
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
package com.globalreports.engine.objects.sys;

import java.util.Vector;

import com.globalreports.engine.objects.GRSystemObject;
import com.globalreports.engine.structure.grbinary.GRGlobalPDF;

public class GRSysPaginaNdiM extends GRSystemObject {
	public static final short LANGUAGE_IT		= 1;
	public static final short LANGUAGE_EN		= 2;
	public static final short LANGUAGE_FR		= 3;
	public static final short LANGUAGE_DE		= 4;
	
	private String fontStyle;
	private String fontName;
	private double fontSize;
	private short language;
	
	private double redColor;
	private double greenColor;
	private double blueColor;
	
	public GRSysPaginaNdiM() {
		this("");
	}
	public GRSysPaginaNdiM(String fontStyle) {
		this(fontStyle,"it");
	}
	public GRSysPaginaNdiM(String fontStyle, String language) {
		super(GRSystemObject.TYPESYSOBJECT_PAGINANDIM);
		
		if(language.toLowerCase().equals("it"))
			this.language = LANGUAGE_IT;
		else if(language.toLowerCase().equals("en"))
			this.language = LANGUAGE_EN;
		else if(language.toLowerCase().equals("fr"))
			this.language = LANGUAGE_FR;
		else if(language.toLowerCase().equals("de"))
			this.language = LANGUAGE_DE;
		
		this.fontStyle = "";
		if(fontStyle.length() > 0) {
			this.fontStyle = fontStyle;
			fontStyle = fontStyle.substring(1,fontStyle.length()-1);

			String[] formatStyle = fontStyle.split(":");
			fontName = formatStyle[0];
			fontSize = Double.parseDouble(formatStyle[1]);
			
			String[] fontColor = formatStyle[2].split(",");
			redColor = Double.parseDouble(fontColor[0]);
			greenColor = Double.parseDouble(fontColor[1]);
			blueColor = Double.parseDouble(fontColor[2]);
			
		}
		
	}
	
	private void init() {
		this.language = LANGUAGE_IT;
		
		this.fontStyle = "";
		
		this.fontName = "verdana";
		this.fontSize = 8.0;
		
		this.redColor = 0.0;
		this.greenColor = 0.0;
		this.blueColor = 0.0;
	}
	
	public void setLanguage(short value) {
		this.language = value;
	}
	public void setLanguage(String value) {
		if(value.toLowerCase().equals("it"))
			this.language = LANGUAGE_IT;
		else if(value.toLowerCase().equals("en"))
			this.language = LANGUAGE_EN;
		else if(value.toLowerCase().equals("fr"))
			this.language = LANGUAGE_FR;
		else if(value.toLowerCase().equals("de"))
			this.language = LANGUAGE_DE;
	}
	public short getLanguage() {
		return language;
	}
	public void setFontStyle(String value) {
		System.out.println("IMPOSTO FONTSTYLE: "+value);
		this.fontStyle = value;
	}
	public String getFontStyle() {
		return fontStyle;
	}
	public void setFontColor(double redColor, double greenColor, double blueColor) {
		this.redColor = redColor;
		this.greenColor = greenColor;
		this.blueColor = blueColor;
	}
	public String getFontColor() {
		return redColor+" "+greenColor+" "+blueColor;
	}
	public double getFontColorRED() {
		return redColor;
	}
	public double getFontColorGREEN() {
		return greenColor;
	}
	public double getFontColorBLUE() {
		return blueColor;
	}
	public void setFontSize(double value) {
		this.fontSize = value;
	}
	public double getFontSize() {
		return fontSize;
	}
	public String getFontName() {
		return fontName;
	}
	public String draw(GRGlobalPDF grglobal) {
		StringBuffer content = new StringBuffer();
		String label = null;
		
		int N = grglobal.getPaginaCorrente();
		int M = grglobal.getPagineTotale();
		
		double top;
		
		//top = grcontext.getTop() - this.getTop();
		top = heightPage - this.getTop();
		
		// Esegue una verifica sul language
		switch(language) {
		case LANGUAGE_IT:
			label = "(Pagina "+N+" di "+M+")";
			break;
			
		case LANGUAGE_EN:
			label = "(Page "+N+" of "+M+")";
			break;
		
		case LANGUAGE_FR:
			label = "(Page "+N+" de "+M+")";
			break;
			
		case LANGUAGE_DE:
			label = "(Seite "+N+" von "+M+")";
			break;
			
		default:
			label = "";
			
		}
		content.append("BT\n");
		
		//content.append("510.0 75.0 Td\n");	// Position
		content.append(left+" "+top+" Td\n");
		content.append("/"+fontName+" "+fontSize+" Tf\n"); // Font name e size
		content.append(redColor+" "+greenColor+" "+blueColor+" rg\n");	// Color
		content.append("["+label+"] TJ\n");	// Value
		content.append("ET\n");
		
		return content.toString();
	
	}
}
