/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.variable.GRText
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
package com.globalreports.engine.objects.variable;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalreports.engine.err.GRValidateException;
import com.globalreports.engine.objects.GRObject;
import com.globalreports.engine.objects.variable.text.GRParagraph;
import com.globalreports.engine.objects.variable.text.GRRowParagraph;
import com.globalreports.engine.objects.variable.text.GRTextRowParagraph;
import com.globalreports.engine.structure.GRMeasures;
import com.globalreports.engine.structure.grbinary.GRDocument;
import com.globalreports.engine.structure.grbinary.GRFont;
import com.globalreports.engine.structure.grbinary.data.GRData;
import com.globalreports.engine.structure.grpdf.GRContext;

public class GRText extends GRVariableObject {
	protected final String REG_TEXT = "\\[([a-zA-Z0-9]+):([0-9]+):([0-9.]+),([0-9.]+),([0-9.]+)\\]([a-zA-Z0-9 ,!$%&;:\\\\\"\'\\?\\^\\.\\{\\}\\-\\/\\(\\)]+)";
	protected final String REG_ALIGN = "[ ]{0,}[a-zA-Z0-9,!$%&;:\\\\\"\'\\?\\^\\.\\{\\}\\-\\/\\(\\)]+([ ]+|)";
	protected final String REG_VARIABLE = "[{]([a-zA-Z0-9_]+)(:{0,1})([a-zA-Z0-9, =!$%&;\\\\\"\'\\?\\^\\.\\-\\/\\(\\)]{0,})[}]";
	
	private double left;
	private double top;
	private double width;
	private double height;
	private short align;
	private double lineSpacing;
	private String value;
	
	private double maxHeight;	// Altezza massima calcolata a run-time in fase di rendering
	
	private double dim;	// E' la dimensione verticale del testo.
						// Tale valore viene valorizzato al momento del rendering
	
	
	public static final short ALIGN_LEFT		= 1;
	public static final short ALIGN_CENTER		= 2;
	public static final short ALIGN_RIGHT		= 3;
	public static final short ALIGN_JUSTIFY		= 4;
	public static final short ALIGN_SINGLELINE	= 5;
	
	public GRText() {
		this(GRObject.TYPEOBJ_TEXT, 0.0, 0.0, 0.0, 0.0, ALIGN_LEFT, GRObject.HPOSITION_ABSOLUTE, 2.0, null);
		
	}
	public GRText(short type, double left, double top, double width, double height, short align, short hpos, double lineSpacing, String value) {
		super(type);
		
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
		this.align = align;
		this.hposition = hpos;
		this.lineSpacing = lineSpacing;
		this.value = value;
		
		if(width <= 0.0)
			this.width = 80.0;
	}
	public GRText(double left, double top, double width, double height, short align, short hpos, double lineSpacing, String value) {
		this(GRObject.TYPEOBJ_TEXT, left, top, width, height, align, hpos, lineSpacing, value);
	}
	
	public double getLeft() {
		return left;
	}
	public void setLeft(double left) {
		this.left = left;
	}
	public double getTop() {
		return top;
	}
	public void setTop(double top) {
		this.top = top;
	}
	public double getWidth() {
		return width;
	}
	public void setWidth(double width) {
		this.width = width;
	}
	public double getHeight() {
		return height;
	}
	public void setHeight(double height) {
		this.height = height;
	}
	public double getMaxHeight() {
		return maxHeight;
	}
	public short getAlign() {
		return align;
	}
	public void setAlign(short align) {
		this.align = align;
	}
	public void setAlign(String value) {
		if(value.equals("left")) {
			this.align = ALIGN_LEFT;
		} else if(value.equals("center")) {
			this.align = ALIGN_CENTER;
		} else if(value.equals("right")) {
			this.align = ALIGN_RIGHT;
		} else if(value.equals("justify")) {
			this.align = ALIGN_JUSTIFY;
		}
	}
	public void setLineSpacing(double value) {
		this.lineSpacing = value;
	}
	public double getLineSpacing() {
		return lineSpacing;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public Vector<String> draw(GRContext grcontext) throws GRValidateException {
		StringBuffer content = new StringBuffer();
		String value = this.value;
		
		Vector<String> stream = new Vector<String>();
		maxHeight = 0.0;
		
		if(grdata != null) 
			value = grdata.addVariables(this.value);
		
		content.append("BT\n");
		
		content.append(getStreamTEXT(grcontext,value));
		
		content.append("ET\n");

		
		/*
		// Inserisce la marcatura
		double PI = 3.14159;
		double C = PI / 180;
		double sTeta = Math.sin((C * 90));
		double cTeta = Math.cos(C * 90);
		
		sTeta = -1;
		cTeta = 0;
		System.out.println("C: "+cTeta);
		content.append("BT\n");
		content.append("/GRF1 7 Tf\n");
		content.append(cTeta+" "+sTeta+" "+(sTeta * -1)+" "+cTeta+" 28.34 140 Tm\n");
		content.append("-328.34 550 Td\n");
		content.append("(Stampato con tecnologia GlobalReports - www.globalreports.it) Tj\n");
		content.append("ET\n");
		
		sTeta =  Math.sin((C * 45));
		cTeta = Math.cos(C * 45);
		content.append("BT\n");
		content.append("/GRF1 46 Tf\n");
		content.append(cTeta+" "+sTeta+" "+(sTeta * -1)+" "+cTeta+" 28.34 140 Tm\n");
		content.append("300 150 Td\n");
		content.append("1 Tr\n");
		content.append("6 w\n");
		
		content.append("(VADA VIA AL CUL) Tj\n");
		content.append("ET\n");
		*/

		stream.add(content.toString());
		return stream;
		//return content.toString();
	}
	
	private String getStreamTEXT(GRContext grcontext, String value) {
		StringBuffer content = new StringBuffer();
		GRParagraph grpar;
	
		dim = 0.0;
		double heightFirstRow = 0.0;
		
		// Genera il paragrafo
		double left, top;
		
		left = GRMeasures.arrotonda(grcontext.getLeft() + this.getLeft());
		if(this.getHPosition() == GRObject.HPOSITION_ABSOLUTE) {
			top = GRMeasures.arrotonda(grcontext.getTop() - this.getTop());
		} else {
			top = GRMeasures.arrotonda(grcontext.getHPosition() - this.getTop());
		}
		grpar = new GRParagraph(left,top);
	
		grpar.setAlignment(getAlign());
		grpar.setLineSpacing(getLineSpacing());
		grpar.setWidth(this.getWidth() * 1000);
		
		Pattern pattern = Pattern.compile(REG_TEXT);
		Matcher matcher = pattern.matcher(value);
		
		grpar.newRow();
		while(matcher.find()) {
			formatText(grpar,matcher.group(6), matcher.group(1), Double.parseDouble(matcher.group(2)), Double.parseDouble(matcher.group(3)), Double.parseDouble(matcher.group(4)), Double.parseDouble(matcher.group(5)), this.getAlign());
		}
		
		// TEST DEBUG
		for(int i = 0;i < grpar.getTotaleRow();i++) {
			grpar.getLineParagraph(i).normalizza(grpar.getWidth());
		}
		if(grpar.getTotaleRow() > 0) {
			grpar.setTop(top - grpar.getLineParagraph(0).getMaxHeight());
			
			// Inserisce il paragrafo nello stream
			content.append(grpar.getLeft()+" "+grpar.getTop()+" Td\n");
				
			for(int idPar = 0;idPar < grpar.getTotaleRow();idPar++) {
				GRRowParagraph grrow = grpar.getLineParagraph(idPar);
				
				grpar.addHeight(grrow.getMaxHeight());
				
				if(idPar == 0)
					heightFirstRow = grrow.getMaxHeight();
				
				if(idPar > 0) {
					content.append("0 -"+(grrow.getMaxHeight()+grpar.getLineSpacing())+" TD\n");		// INTERLINEA
					grpar.addHeight(grpar.getLineSpacing());
					//grpar.addHeight(grrow.getMaxHeight()+grpar.getLineSpacing());
					
				}
				
				for(int idText = 0;idText < grrow.getTotaleTextRow();idText++) {
					int flagToken = 0;
						
					if(idText == 0) {
						if(idText == (grrow.getTotaleTextRow()-1))
							flagToken = 3;
						else
							flagToken = 1;
					} else {
						if(idText == (grrow.getTotaleTextRow()-1))
							flagToken = 2;
						else
							flagToken = 0;
					}
					GRTextRowParagraph grtokentext = grrow.getTokenTextRow(idText);
							
					content.append("/"+grtokentext.getFontId()+" "+grtokentext.getFontSize()+" Tf\n");
					content.append(grtokentext.getRED()+" "+grtokentext.getGREEN()+" "+grtokentext.getBLUE()+" rg\n");	
					// Un trucchetto relativamente all'allineamento justify
					if(grpar.getAlignment() == ALIGN_JUSTIFY && (idPar+1) == grpar.getTotaleRow())
						content.append(insertText(grrow.getGap(),grrow.getBlank(),ALIGN_LEFT,grtokentext.getFontSize(),grtokentext.getValue(),flagToken,grtokentext.getBlank()));
					else {
						content.append(insertText(grrow.getGap(),grrow.getBlank(),grpar.getAlignment(),grtokentext.getFontSize(),grtokentext.getValue(),flagToken,grtokentext.getBlank()));
					}	
				}
								
			}
			
			// Aggiorna la dimensione verticale della casella di testo
			maxHeight = grpar.getHeight();
			//System.out.println(value+ " - "+heightFirstRow + " - "+maxHeight);
			// Aggiorna hposition
			grcontext.setHPosition(grpar.getTop() + heightFirstRow - maxHeight);
		}
		
		//System.out.println(content.toString());
		return content.toString();
	}
	
	private GRFont getFont(String id) {
		for(int i = 0;i < grfont.size();i++) {
			if(grfont.get(i).getId().equals(id)) {
				return grfont.get(i);
			}
		}
		
		return null;
	}
	private void formatText(GRParagraph grpar,String value, String id, double fontSize, double cRED, double cGREEN, double cBLUE, short align) {
		
		//System.out.println("FT: "+value+" - "+grpar.getTotaleRow());
		int[] widths; 
		int ascent;
		int descent;
		int dimFont;
		double dimToken = 0.0;
		
		byte[] stream;
		int lenStream;
		int startStream;
		int endStream;
		int pointerStream;
		
		String tempStream;
		double tempLenStream;
		int tempBlank;
		
		boolean initWord;	// Questo flag viene inizializzato a false ad ogni nuova riga di testo
							// Successivamente viene impostato a true quando la frase comincia con un qualsiasi
							// carattere diverso dallo spazio (CHAR: 32)
		
		stream = value.getBytes();
		lenStream = stream.length;
		
		GRFont grfont = this.getFont(id);
		widths = grfont.getDimensionWidths();
		ascent = grfont.getAscent();
		descent = Math.abs(grfont.getDescent());
		dimFont = ascent + descent;
		
		startStream = 0;
		endStream = 0;
		pointerStream = 0;
		tempStream = "";
		tempLenStream = 0;
		tempBlank = 0;
		
		GRTextRowParagraph grtextrow = new GRTextRowParagraph(id,fontSize,dimFont,cRED,cGREEN,cBLUE);
		grtextrow.setWidthBlank(widths[32] * fontSize);
		initWord = false;
		//System.out.println("WIDTH TOTALE: "+grpar.getWidth());
		while(pointerStream < lenStream) {
			int c = stream[pointerStream];
			
			if(c == 92) {
				
				// Esegue il richiamo corretto alla Charset
				c = grfont.fromOctalToDecimal(value.substring(pointerStream+1,pointerStream+4));
				pointerStream = pointerStream + 3;
			}
			
			if(c == -1) {
				// Gestione Carriage Return 
				tempStream = value.substring(startStream,pointerStream+1);
				grtextrow.setValue(tempStream);
				grtextrow.setWidth(dimToken);
				
				grpar.addTextRow(grtextrow);
				
				startStream = pointerStream+1;
				
				// Aggiunge una nuova riga per il testo che seguirà
				grpar.newRow();
				
				// Crea il nuovo token
				grtextrow = new GRTextRowParagraph(id,fontSize,dimFont,cRED,cGREEN,cBLUE);
				grtextrow.setWidthBlank(widths[32] * fontSize);
				initWord = false;
				dimToken = 0.0;
			} else {
				dimToken = dimToken + (widths[c] * fontSize);
				//System.out.println("CAR: "+(char)c+" - W: "+widths[c]+" - DIM: "+dimToken+" - ROW: "+grpar.getWidthRowSelected()+" - INITWORD: "+initWord);
				
				if((dimToken + grpar.getWidthRowSelected()) >= grpar.getWidth()) {
					
					if(grtextrow.getBlank() == 0) {
						if(grpar.getTotaleTextRowSelected() == 0) {
							tempStream = value.substring(startStream,pointerStream);
							grtextrow.setValue(tempStream);
							grtextrow.setWidth(dimToken - (widths[c] * fontSize));
							
							grpar.addTextRow(grtextrow);
							
							return;
						}
						
						grpar.newRow();
						
						dimToken = 0.0;
						pointerStream = -1;	// -1 in quanto poi verrà incrementato di 1 prima di procedere nel ciclo
						
					} else {
						tempStream = value.substring(startStream,endStream);
						grtextrow.setValue(tempStream);
						grpar.addTextRow(grtextrow);
						
						pointerStream = endStream-1;
						startStream = endStream;
						
						// Aggiunge una nuova riga per il testo che seguirà
						grpar.newRow();
						
						// Crea il nuovo token
						grtextrow = new GRTextRowParagraph(id,fontSize,dimFont,cRED,cGREEN,cBLUE);
						grtextrow.setWidthBlank(widths[32] * fontSize);
						initWord = false;
						dimToken = 0.0;
						
					}
				} else {
					
					if(c == 32) {	// Se trova uno spazio
						//if(initWord) {
							grtextrow.addBlank();
							grtextrow.setWidth(dimToken);
							
							endStream = pointerStream+1;
						//}
					} else {
						initWord = true;
					}
				}
				
			}
			
			pointerStream++;
		}
		
		if(startStream < lenStream) {
			tempStream = value.substring(startStream);
			
			grtextrow.setWidth(dimToken);
			grtextrow.setValue(tempStream);
			
			grpar.addTextRow(grtextrow);
			
		}
		
	}
	
	private String insertText(double gap, int blank, short align, double fontSize, String value, int flagPosition, int blankToken) {
		/* flagPosition:
		 * 1: Primo token di una riga
		 * 2: Ultimo token di una riga (nel caso in cui ci siano stati altri token)
		 * 3: Unico token di una riga
		 * 0: Token intermedio
		 */
		//System.out.println("IT: "+value+"-FLAG: "+flagPosition+" - GAP: "+gap+" - BLANK :" +blank);
		String ritorno = "";
		double dimToken;
		double lastToken;
		int contatore = 1;
		
		/* Gestione del Carriage Return */
		if(value.endsWith("\\012")) {
			value = value.substring(0,value.length()-4);
			
			if(align == ALIGN_JUSTIFY)
				align = ALIGN_LEFT;
		}
		
		//System.out.println("INSERTTEXT: "+flagPosition+" - "+value);
		ritorno = "[";
		
		if(align == ALIGN_SINGLELINE) {
			ritorno = ritorno + "(" + value + ")";
		} else if(align == ALIGN_LEFT) {
			ritorno = ritorno + "(" + value + ")";
		} else if(align == ALIGN_RIGHT) {
			if(flagPosition == 1 || flagPosition == 3)
				ritorno = ritorno + "-" + GRMeasures.arrotonda(gap / fontSize) + "(" + value + ")";
			else
				ritorno = ritorno + "(" + value + ")";
			
		} else if(align == ALIGN_CENTER) {
			if(flagPosition == 1 || flagPosition == 3) {
				dimToken = GRMeasures.arrotonda((gap / 2) / fontSize);
				ritorno = ritorno + "-" + dimToken + "(" + value + ")";
			} else {
				ritorno = ritorno + "(" + value + ")";
			}
		} else if(align == ALIGN_JUSTIFY) {
			
			//blank--;
			dimToken = GRMeasures.arrotonda((gap / blank) / fontSize);
			lastToken = GRMeasures.arrotonda(((gap / fontSize)  - (dimToken * (blank - 1))));
			
			Pattern pattern = Pattern.compile(REG_ALIGN);
			Matcher matcher = pattern.matcher(value);
			
			while(matcher.find()) { 
				
				if(contatore == 1) {
					if(flagPosition == 0 || flagPosition == 2)
						if(contatore == blankToken) {
							ritorno = ritorno + "-" + lastToken;
						} else
							ritorno = ritorno + "-" + dimToken;
				} else {
					if(contatore == blankToken)
						ritorno = ritorno + "-" + lastToken;
					else
						ritorno = ritorno + "-" + dimToken;
				}
				
				ritorno = ritorno + "("+matcher.group(0)+")";
				
				contatore++;
			}
		}
		
		ritorno = ritorno + "] TJ\n";
		
		/*
		 * UN EVENTUALE SOTTOLINEATURA VA INSERITA QUI
		ritorno = ritorno + "q\n";
		ritorno = ritorno + "0.25 w\n";
		ritorno = ritorno + "113.38 776.55 m\n";
		ritorno = ritorno + "424.62 776.55 l\n";
		ritorno = ritorno + "S\n";
		ritorno = ritorno + "Q\n";
		*/
		
		
		return ritorno;
	}
}
