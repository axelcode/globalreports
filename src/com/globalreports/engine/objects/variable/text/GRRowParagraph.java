/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.text.GRParagraph
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
package com.globalreports.engine.objects.variable.text;

import java.util.Vector;

public class GRRowParagraph {
	private double gapFormatted;	// Differenza in punti tra la larghezza totale disponibile e la larghezza della linea da inserire
	private int blank;	// Numero di spazi totali dell'intera riga di testo	
	private double maxHeight;	// E' data dal max fontsize dell'intera riga
	private int maxAscent;		// E' il valore max di estensione verticale di tutti i font della riga
	private double maxFontSize;	// E' il valore max di dimensione del font
	
	private double width;	// E' la dimensione della riga data dalla somma delle 
							// dimensioni di ogni oggetto GRTextRowParagraph in esso contenuti
	
	private Vector<GRTextRowParagraph> grtextrow;
	
	public GRRowParagraph() {
		gapFormatted = 0;
		blank = 0;
		maxHeight = 0;
		maxAscent = 0;
		maxFontSize = 0.0;
		width = 0.0;
		
		grtextrow = new Vector<GRTextRowParagraph>();
	}
	
	private void setMaxHeight(double fSize, int fAscent) {
		double value = (fAscent / 1000.0) * fSize;
		
		if(value > maxHeight)
			maxHeight = value;
	}
	
	public void addTextRow(GRTextRowParagraph value) {
		grtextrow.add(value);
		
		// Aggiorna la dimensione totale della riga
		width += value.getWidth();
		this.blank += value.getBlank();
		setMaxHeight(value.getFontSize(), value.getFontAscent());
		
	}
	public void setMaxAscent(int value) {
		if(maxAscent < value)
			maxAscent = value;
	}
	public int getMaxAscent() {
		return maxAscent;
	}
	public void setGap(double value) {
		this.gapFormatted = value;
	}
	public double getGap() {
		return gapFormatted;
	}
	public int getBlank() {
		return blank;
	}
	public int getTotaleTextRow() {
		return grtextrow.size();
	}
	public GRTextRowParagraph getTokenTextRow(int i) {
		return grtextrow.get(i);
	}
	public double getMaxHeight() {
		return maxHeight;
	}
	
	public double getWidth() {
		return width;
	}
	public String getValue() {
		StringBuffer buffer = new StringBuffer();
		
		for(int i = 0;i < grtextrow.size();i++) {
			buffer.append(grtextrow.get(i).getValue());
		}
		
		return buffer.toString();
	}
	public void normalizza(double maxWidth) {
		// Inizialmente elimina gli spazi alla fine della riga
		// Dopodichè calcola il gap rispetto alla dimensione width totale del paragrafo
		if(grtextrow.size() == 0)
			return;
		
		GRTextRowParagraph refToken = grtextrow.lastElement();
		String value = refToken.getValue();
		int index = value.length()-1;
		
		while(value.codePointAt(index) == 32) {
			value = value.substring(0,index);
			
			refToken.setWidth(refToken.getWidth() - refToken.getWidthBlank());
			this.width = this.width - refToken.getWidthBlank();
			this.blank--;
			
			index--;
			if(index < 0)
				break;
		}
		
		refToken.setValue(value);
		this.gapFormatted = maxWidth - this.width;
		
	}
}
