/*
 * ==========================================================================
 * class name  : com.globalreports.compiler.GRCompiler
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
package com.globalreports.compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.globalreports.compiler.measures.GRDimension;
import com.globalreports.compiler.resources.GRFontResource;
import com.globalreports.compiler.resources.GRImageResource;
import com.globalreports.engine.GRPDF;
import com.globalreports.engine.err.GRCompileException;
import com.globalreports.engine.err.GRCompileIOException;
import com.globalreports.engine.err.GRCompileNameDocumentMissingException;
import com.globalreports.engine.err.GRCompileTypeNotDefined;
import com.globalreports.engine.err.GRCompileWriteGrbException;
import com.globalreports.engine.err.GRCompileXmlException;
import com.globalreports.engine.objects.GRCircle;
import com.globalreports.engine.objects.GRImage;
import com.globalreports.engine.objects.GRLine;
import com.globalreports.engine.objects.GRObject;
import com.globalreports.engine.objects.GRRectangle;
import com.globalreports.engine.objects.GRShape;
import com.globalreports.engine.objects.GRSystemObject;
import com.globalreports.engine.objects.sys.GRSysPaginaNdiM;
import com.globalreports.engine.objects.variable.GRBarcode;
import com.globalreports.engine.objects.variable.GRChart;
import com.globalreports.engine.objects.variable.GRText;
import com.globalreports.engine.objects.variable.GRTextCondition;
import com.globalreports.engine.objects.variable.chart.GRChartVoice;
import com.globalreports.engine.objects.variable.dynamic.GRGroup;
import com.globalreports.engine.objects.variable.dynamic.GRList;
import com.globalreports.engine.objects.variable.dynamic.GRTableList;
import com.globalreports.engine.objects.variable.dynamic.tablelist.GRTableListCell;
import com.globalreports.engine.objects.variable.dynamic.tablelist.GRTableListSection;
import com.globalreports.engine.structure.font.encoding.GREncode_ISO8859;
import com.globalreports.engine.structure.grbinary.GRDocument;
import com.globalreports.engine.structure.grbinary.GRPage;


public class GRCompiler {
	public final static short EOS			= 0;	// End Of Session
	private final String HEADER_VERSION		= "GRAB";
	private final String BLANK_16B			= "                ";
	private final String BLANK_32B			= "                                ";
	private final String BLANK_64B			= "                                                                ";
	private final String DEFAULTPROGRAM		= "GlobalReports Compiler ";
	
	private String grsource;
	private String nameDocument;
	private Vector<String> pathFont;
	private String typography;
	
	private Vector<GRImageResource> grimageResource;
	private Vector<GRFontResource> grfontResource;
	
	private GRDocument grdocument;
	
	public GRCompiler(String source) {
		this.grsource = source;
		
		nameDocument = null;
		pathFont = null;
		typography = "MM";	// L'unit�� di misura predefinita sono i millimetri
	}
	
	public void compile(String fileOutput) throws GRCompileException {
		grdocument = new GRDocument();
		nameDocument = fileOutput;
		
		/* Legge il file .main */
		this.readSource(grsource);
		
	}
	public void compile() throws GRCompileException {
		this.compile(null);
		
	}
	public String writeGRB() throws GRCompileWriteGrbException {
		RandomAccessFile rOut;
		String dirOut = (new File(grsource)).getParent();

		if(dirOut == null || dirOut.equals(""))
			dirOut = "";
		else
			dirOut = dirOut + "//";
				
		String pathOut = dirOut + nameDocument + ".grb";
		long fPointImage;
		long fPointFont;
		Vector<Long> fPointPage;		// Contiene gli indirizzi delle pagine
		Vector<Long> fPointTemplate;	// Contiene gli indirizzi dei template
		long fPointAddressTable;
		
		int totPagine = 0;
		
		// Gli oggetti che hanno un riferimento a fine documento
		try {
			// Se il file esiste lo cancella
			File f = new File(pathOut);
			
			f.delete();
					
			rOut = new RandomAccessFile(pathOut,"rw");
			
			// Init 
			fPointAddressTable = 0;
			fPointFont = 0;
			fPointPage = new Vector<Long>();	
			fPointTemplate = new Vector<Long>();
					
			// HEADER
			/*
			 * L'header è composto da 64 byte così suddivisi
			 * I primi 4 byte hanno il magic code: GRAB:
			 * -->Global Reports author Alessandro Baldini
			 * 12 byte suddivisi su 3 int da 4 byte per indicare la versione
			 * (Major.Minor.Minus)
			 * 32 byte di testo per info sull'applicativo utilizzato per la compilazione
			 * 16 byte non usati per sviluppi futuri (BLANK)
			 */
			rOut.writeBytes("GR");
			rOut.writeBytes("AB");
			rOut.writeInt(GRPDF.GR_VERSION_MAJOR);
			rOut.writeInt(GRPDF.GR_VERSION_MINOR);
			rOut.writeInt(GRPDF.GR_VERSION_BETAVERSION);
					
			rOut.writeBytes(DEFAULTPROGRAM);
			rOut.writeBytes(BLANK_32B.substring(0,32-(DEFAULTPROGRAM.length())));
			rOut.writeBytes(BLANK_16B);
					
			// ADDRESS POINTERS-TABLE
			rOut.writeLong(0);
					
			// RESOURCES
			// GESTITE LE SEGUENTI RISORSE:
			// IMAGE
			// FONT
					
			// IMAGES
			fPointImage = rOut.getFilePointer();
					
			rOut.writeInt(grdocument.getTotaleImage());
			for(int i = 0;i < grdocument.getTotaleImage();i++) {
				
				// ID IMAGE
				rOut.writeInt((grdocument.getImageId(i)).length());
				rOut.writeBytes(grdocument.getImageId(i));
						
				// TYPE IMAGE
				rOut.writeShort(grdocument.getImageType(i)	);
					
				// ORIGINALWIDTH IMAGE
				rOut.writeInt(grdocument.getImageOriginalWidth(i));
						
				//ORIGINALHEIGHT IMAGE
				rOut.writeInt(grdocument.getImageOriginalHeight(i));
				
				// PATH IMAGE
				rOut.writeInt(grdocument.getImageSizeStream(i));
				rOut.write(grdocument.getImageStream(i));
						
			}
					
			// FONTS
			fPointFont = rOut.getFilePointer();
					
			rOut.writeInt(grdocument.getTotaleFont());
			for(int i = 0;i < grdocument.getTotaleFont();i++) {
				
				// ID FONT
				rOut.writeInt((grdocument.getFontId(i)).length());
				rOut.writeBytes(grdocument.getFontId(i));
						
				// NAME FONT
				rOut.writeInt((grdocument.getFontName(i)).length());
				rOut.writeBytes(grdocument.getFontName(i));
						
				// TYPE FONT
				rOut.writeShort(grdocument.getFontCodeType(i));
						
				// STREAM FONT
				rOut.writeInt(grdocument.getFontLenOriginalStream(i));
				rOut.writeInt(grdocument.getFontLenCompressedStream(i));
				rOut.write(grdocument.getFontStream(i));
						
						
			}
			
			// TEMPLATES
			// Inserisce il contenuto di ogni template
			
			for(int i = 0;i < grdocument.getNumberTemplates();i++) {
				fPointTemplate.add(new Long(rOut.getFilePointer()));
				
				// Nome della pagina
				rOut.writeInt((grdocument.getTemplateName(i)).length());
				rOut.writeBytes(grdocument.getTemplateName(i));
				
				// Posizione
				rOut.writeShort(grdocument.getTemplatePosition(i));
				
				Vector<GRObject> grtemplateobj = grdocument.getTemplateObject(i);
				if(grtemplateobj != null) {
					rOut.writeInt(grtemplateobj.size());
					// Cicla per tutti gli oggetti trovati
					for(int j = 0;j < grtemplateobj.size();j++) {
						GRObject grobj = grtemplateobj.get(j);
								
						short type = grobj.getType();
								
						rOut.writeShort(type);
						if(type == GRObject.TYPEOBJ_TEXT) {
							this.insertTEXT(grobj, rOut);	
						} else if(type == GRObject.TYPEOBJ_IMAGE) {
							this.insertIMAGE(grobj, rOut);
						} else if(type == GRObject.TYPEOBJ_SHAPE) {
							this.insertSHAPE(grobj, rOut);
						}
					}
				} else {
					rOut.writeInt(0);
				}
			}
			// PAGES
			// Inserisce il contenuto di ogni pagina
			
			for(int i = 0;i < grdocument.getNumberPages();i++) {
				fPointPage.add(new Long(rOut.getFilePointer()));
						
				// Intestazione della pagina
				rOut.writeDouble(grdocument.getPageWidth(i));
				rOut.writeDouble(grdocument.getPageHeight(i));
				rOut.writeDouble(grdocument.getPageHeader(i));
				rOut.writeDouble(grdocument.getPageFooter(i));
						
				// HEAD
				Vector<GRObject> grheaderobj = grdocument.getHeaderObject(i);

				if(grheaderobj != null) {
					rOut.writeInt(grheaderobj.size());
					// Cicla per tutti gli oggetti trovati
					for(int j = 0;j < grheaderobj.size();j++) {
						GRObject grobj = grheaderobj.get(j);
								
						short type = grobj.getType();
								
						rOut.writeShort(type);
						if(type == GRObject.TYPEOBJ_TEXT) {
							this.insertTEXT(grobj, rOut);	
						} else if(type == GRObject.TYPEOBJ_IMAGE) {
							this.insertIMAGE(grobj, rOut);
						} else if(type == GRObject.TYPEOBJ_SHAPE) {
							this.insertSHAPE(grobj, rOut);
						}
					}
				} else {
					rOut.writeInt(0);
				}
						
				// BODY
				Vector<GRObject> grbodyobj = grdocument.getBodyObject(i);
				
					rOut.writeInt(grbodyobj.size());
						
					// Cicla per tutti gli oggetti trovati
					for(int j = 0;j < grbodyobj.size();j++) {
						GRObject grobj = grbodyobj.get(j);
								
						short type = grobj.getType();
								
						rOut.writeShort(type);
						if(type == GRObject.TYPEOBJ_TEXT) {
							this.insertTEXT(grobj, rOut);	
						} else if(type == GRObject.TYPEOBJ_IMAGE) {
							this.insertIMAGE(grobj, rOut);
						} else if(type == GRObject.TYPEOBJ_SHAPE) {
							this.insertSHAPE(grobj, rOut);
						} else if(type == GRObject.TYPEOBJ_LIST) {
							this.insertLIST(grobj, rOut);
						} else if(type == GRObject.TYPEOBJ_TABLELIST) {
							this.insertTABLELIST(grobj, rOut);
						} else if(type == GRObject.TYPEOBJ_TEXTCONDITION) {
							this.insertTEXTCONDITION(grobj, rOut);
						} else if(type == GRObject.TYPEOBJ_CHART) {
							this.insertCHART(grobj, rOut);
						} else if(type == GRObject.TYPEOBJ_GROUP) {
							this.insertGROUP(grobj, rOut);
						} else if(type == GRObject.TYPEOBJ_BARCODE) {
							this.insertBARCODE(grobj,  rOut);
						}
								
								
					}
				
				
				// FOOT
				Vector<GRObject> grfooterobj = grdocument.getFooterObject(i);
				
				if(grfooterobj != null) {
					rOut.writeInt(grfooterobj.size());
					// Cicla per tutti gli oggetti trovati
					for(int j = 0;j < grfooterobj.size();j++) {
						GRObject grobj = grfooterobj.get(j);
								
						short type = grobj.getType();
								
						rOut.writeShort(type);
						if(type == GRObject.TYPEOBJ_TEXT) {
							this.insertTEXT(grobj, rOut);	
						} else if(type == GRObject.TYPEOBJ_IMAGE) {
							this.insertIMAGE(grobj, rOut);
						} else if(type == GRObject.TYPEOBJ_SHAPE) {
							this.insertSHAPE(grobj, rOut);
						}
					}
				} else {
					rOut.writeInt(0);
				}
				
				// Accoda eventuali oggetti di sistema
				Vector<GRSystemObject> grsys = grdocument.getSystemObject(i);
				
				if(grsys != null) {
					rOut.writeShort(grsys.size());
					// Cicla per tutti gli oggetti trovati
					for(int j = 0;j < grsys.size();j++) {
						GRSystemObject grobj = grsys.get(j);
								
						this.insertSYSOBJECT(grobj, rOut);	
						
					}
				} else {
					rOut.writeShort(0);
				}
				
				// Se presente inserisce il template
				if(grdocument.getPageTemplate(i) != null) {
					Vector<String> pageTemplate = grdocument.getPageTemplate(i);
					
					rOut.writeInt(pageTemplate.size());	// Scrive il numero totale di template presenti nella pagina
					
					for(int j = 0;j < pageTemplate.size();j++) {
						rOut.writeInt(pageTemplate.get(j).length());
						rOut.writeBytes(pageTemplate.get(j));
						
					}
					
				} else {
					rOut.writeInt(-1);
				}
				// Ogni altra sezione viene definita da un flag di 2 byte
				// Se zero terminata la pagina
						
				// PAGEOBJECT
				/* DA IMPLEMENTARE
				GRPageObject grpageobject = grdoc.getContentPageObject(i);
				if(grpageobject.getTotaleObj() > 0) {
					
					rOut.writeInt(grpageobject.getTotaleObj());
					for(int j = 0;j < grpageobject.getTotaleObj();j++) {
						GRObject grobj = grpageobject.getObj(j);
						
						short type = grobj.getType();
						
						rOut.writeShort(type);
						if(type == GRObject.TYPEOBJ_PAGEOBJECT) {
							this.insertPAGEOBJECTFIELD(grobj, rOut);	
						} 
					}
				}
				*/
				
				// Fine delle sezioni
				rOut.writeShort(EOS);
						
				totPagine++;
						
			}
					
			// Prima di chiudere il file inserisce i puntatori alle sezioni
			// ADDRESS POINTERSTABLE
			// LONG(8): Indirizzo di inizio delle IMAGEPROPERTIES
			// LONG(8): Indirizzo di inizio dei FONT
			// INT(4): Totale pagine presenti nel documento
			// FOR-EACH LONG(8): Indirizzo di inizio della singola pagina
			
			// DALLA 1.31 IN SU - INT(4): Totale template presenti nel documento
			// FOR-EACH LONG(8): Indirizzo di inizio del singolo template
						
			fPointAddressTable = rOut.getFilePointer();
			
			rOut.writeLong(fPointImage);				// Address Images
			rOut.writeLong(fPointFont);					// Address Fonts
			
			// PAGES
			rOut.writeInt(grdocument.getNumberPages());	// Totale pagine 
			for(int i = 0;i < grdocument.getNumberPages();i++) {
				rOut.writeLong(fPointPage.get(i));
			}
			// PAGES
			rOut.writeInt(grdocument.getNumberTemplates());	// Totale template 
			for(int i = 0;i < grdocument.getNumberTemplates();i++) {
				rOut.writeLong(fPointTemplate.get(i));
			}
					
			// Per finire aggiorna l'address init table
			rOut.seek(64);
			rOut.writeLong(fPointAddressTable);
			
			return nameDocument + ".grb";
		
		} catch(Exception e) {
			throw new GRCompileWriteGrbException(e.getMessage());
		} 
	}
	
	
	public GRDocument getDocument() {
		return grdocument;
	}
	
	private void readSource(String fileXml) throws GRCompileException {
		try {
			
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(new File(fileXml));
			
			Element rootElement = document.getRootElement();
			List children = rootElement.getChildren();
				
			Iterator iterator = children.iterator();
			while (iterator.hasNext()){	
				readChild((Element)iterator.next());
			} 
		} catch(JDOMException jde) {
			System.out.println("GRManage::readSource::JDOMException: "+jde.getMessage());
		
			throw new GRCompileXmlException(jde.getMessage());
		} catch(IOException ioe) {
			ioe.printStackTrace();
			
			throw new GRCompileIOException(grsource);
		} 
		
	}
	private void readChild(Element element) throws GRCompileException {
		
		if(element.getName().equals("grinfo")) {
			readMainInfo(element);
		} else if(element.getName().equals("page")) {
			readPage(element);
		} else if(element.getName().equals("template")) {
			readTemplate(element);
		}
			
	}
	private void readMainInfo(Element el) throws GRCompileNameDocumentMissingException {
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("namedocument")) {
				if(((Element)element.getParent()).getName().equals("grinfo")) {
					if(nameDocument == null)
						nameDocument = element.getValue();
				}
			} else if(element.getName().equals("pathfont")) {
				if(((Element)element.getParent()).getName().equals("grinfo")) {
					if(pathFont == null)
						pathFont = new Vector<String>();
					
					pathFont.add(element.getValue());
				}
			}
		}
		
		if(nameDocument == null) {
			throw new GRCompileNameDocumentMissingException(grsource);
		}
	}
	
	private void readTemplate(Element el) throws GRCompileException {
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		grdocument.addTemplate();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			if(element.getName().equals("name")) {
				grdocument.setTemplateName(element.getValue());
			
			} else if(element.getName().equals("position")) {
				grdocument.setTemplatePosition(element.getValue());
				
			} else if(element.getName().equals("shape") || element.getName().equals("grshape")) {
				GRShape refShape = readShape(element);
					
				grdocument.addTemplateObj(refShape);
				 
			} else if(element.getName().equals("text") || element.getName().equals("grtext")) {
				GRText refText = readText(element);
				
				grdocument.addTemplateObj(refText);
			} else if(element.getName().equals("image") || element.getName().equals("grimage")) {
				GRImage refImage = readImage(element);
				
				grdocument.addTemplateObj(refImage);
			} 
			
		}
		
		
	}
	private void readPage(Element el) throws GRCompileException {
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		grdocument.addPage();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("typography")) {
				if(((Element)element.getParent()).getName().equals("page")) {
					typography = element.getValue();
				}
			} else if(element.getName().equals("pagewidth")) {
				if(((Element)element.getParent()).getName().equals("page")) {
					grdocument.setPageWidth(GRDimension.getDocumentDimension(typography,element.getValue()));
				}
			} else if(element.getName().equals("pageheight")) {
				if(((Element)element.getParent()).getName().equals("page")) {
					grdocument.setPageHeight(GRDimension.getDocumentDimension(typography,element.getValue()));
				}	
			} else if(element.getName().equals("pageheader")) {
				if(((Element)element.getParent()).getName().equals("page")) {
					grdocument.setPageHeader(GRDimension.getDocumentDimension(typography,element.getValue()));
				}	
			} else if(element.getName().equals("pagefooter")) {
				if(((Element)element.getParent()).getName().equals("page")) {
					grdocument.setPageFooter(GRDimension.getDocumentDimension(typography,element.getValue()));
				}	
			} else if(element.getName().equals("grtemplate")) {
				readPageTemplate(element);
			} else if(element.getName().equals("grheader")) {
				readHeader(element);
			} else if(element.getName().equals("grbody")) {
				readBody(element);
			} else if(element.getName().equals("grfooter")) {
				readFooter(element);
			} 
		}
		 
	}
	private void readPageTemplate(Element el) {
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			if(element.getName().equals("template")) {
				grdocument.addPageTemplate(element.getValue());
			}
		}
	}
	private void readHeader(Element el) {
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			if(element.getName().equals("shape") || element.getName().equals("grshape")) {
				GRShape refShape = readShape(element);
					
				grdocument.addPageHeaderObj(refShape);
				 
			} else if(element.getName().equals("text") || element.getName().equals("grtext")) {
				GRText refText = readText(element);
				
				grdocument.addPageHeaderObj(refText);
			} else if(element.getName().equals("image") || element.getName().equals("grimage")) {
				GRImage refImage = readImage(element);
				
				grdocument.addPageHeaderObj(refImage);
			} 
			
		}
	}
	private void readBody(Element el) throws GRCompileException {
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			if(element.getName().equals("shape") || element.getName().equals("grshape")) {
				GRShape refShape = readShape(element);
					
				grdocument.addPageObj(refShape);
				 
			} else if(element.getName().equals("text") || element.getName().equals("grtext")) {
				GRText refText = readText(element);
				
				grdocument.addPageObj(refText);
			} else if(element.getName().equals("image") || element.getName().equals("grimage")) {
				GRImage refImage = readImage(element);
				
				if(refImage != null)
				grdocument.addPageObj(refImage);
			} else if(element.getName().equals("textcondition") || element.getName().equals("grtextcondition")) {
				GRTextCondition refTextCondition = readTextCondition(element);
				
				grdocument.addPageObj(refTextCondition);
			} else if(element.getName().equals("chart") || element.getName().equals("grchart")) {
				GRChart grchart = readChart(element);
				
				grdocument.addPageObj(grchart);
			} else if(element.getName().equals("group") || element.getName().equals("grgroup")) {
				GRGroup grgroup = readGroup(element);
				
				grdocument.addPageObj(grgroup);
			} else if(element.getName().equals("list") || element.getName().equals("grlist")) {
				GRList grlist = readList(element);
				
				grdocument.addPageObj(grlist);
			} else if(element.getName().equals("tablelist") || element.getName().equals("grtablelist")) {
				GRTableList grtablelist = readTableList(element);
				
				grdocument.addPageObj(grtablelist);
			} else if(element.getName().equals("barcode") || element.getName().equals("grbarcode")) {
				GRBarcode grbarcode = readBarcode(element);
				
				grdocument.addPageObj(grbarcode);
			} else if(element.getName().equals("sysobject") || element.getName().equals("grsysobject")) {	// Oggetti di sistema
				GRSystemObject sysObject = readSystemObject(element);
				
				grdocument.addPageSysObj(sysObject, GRPage.SECTION_PAGE_BODY);
			}
			
		}
	}
	private void readFooter(Element el) {
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			if(element.getName().equals("shape") || element.getName().equals("grshape")) {
				GRShape refShape = readShape(element);
					
				grdocument.addPageFooterObj(refShape);
				 
			} else if(element.getName().equals("text") || element.getName().equals("grtext")) {
				GRText refText = readText(element);
				
				grdocument.addPageFooterObj(refText);
			} else if(element.getName().equals("image") || element.getName().equals("grimage")) {
				GRImage refImage = readImage(element);
				
				grdocument.addPageFooterObj(refImage);
			} 
			
		}
	}
	private GRShape readShape(Element el) {
		
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		GRShape refShape = null;
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("type")) {
				if(((Element)element.getParent()).getName().equals("shape")) {
					if(element.getValue().equals("rectangle")) {
						refShape = getGRRectangle(iterator);
					} else if(element.getValue().equals("line")) {
						refShape = getGRLine(iterator);
					} else if(element.getValue().equals("circle")) {
						refShape = getGRCircle(iterator);
					}
						
				}
			}
		}
		return refShape;
	}
	private GRRectangle getGRRectangle(Iterator iterator) {
		double left = 0.0;
		double top = 0.0;
		double width = 0.0;
		double height = 0.0;
		
		double widthStroke = 0.5;
		double[] colorStroke = null;
		double[] colorFill = null;
		String hposition = "absolute";
		
		GRRectangle refRect = new GRRectangle();
		
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("left")) {
				if(((Element)element.getParent()).getName().equals("shape")) {
					left = GRDimension.getDocumentDimension(typography,element.getValue());
				} 
			} else if(element.getName().equals("top")) {
				if(((Element)element.getParent()).getName().equals("shape")) {
					top = GRDimension.getDocumentDimension(typography,element.getValue());
				}
			} else if(element.getName().equals("width")) {
				if(((Element)element.getParent()).getName().equals("shape")) {
					width = GRDimension.getDocumentDimension(typography,element.getValue());
				}
			} else if(element.getName().equals("height")) {
				if(((Element)element.getParent()).getName().equals("shape")) {
					height = GRDimension.getDocumentDimension(typography,element.getValue());
				}
			} else if(element.getName().equals("hposition")) {
				if(((Element)element.getParent()).getName().equals("shape")) {
					hposition = element.getValue();
				}
			} else if(element.getName().equals("widthstroke")) {
				if(((Element)element.getParent()).getName().equals("shape")) {
					widthStroke = Double.parseDouble(element.getValue());
				}
			} else if(element.getName().equals("colorstroke")) {
				if(((Element)element.getParent()).getName().equals("shape")) {
					colorStroke = getColorForPDF(element.getValue());
				}
			} else if(element.getName().equals("colorfill")) {
				if(((Element)element.getParent()).getName().equals("shape")) {
					colorFill = getColorForPDF(element.getValue());
				}
			} 
			
		}
		
		refRect.setPosition(left,  top);
		refRect.setDimension(width, height);
		
		refRect.setWidthStroke(widthStroke);
		refRect.setHPosition(hposition);
		
		if(colorStroke != null) {
			refRect.setColorStroke(colorStroke[0], colorStroke[1], colorStroke[2]);
		}
		if(colorFill != null) {
			refRect.setColorFill(colorFill[0], colorFill[1], colorFill[2]);
		}
		
		return refRect;
	}
	private GRLine getGRLine(Iterator iterator) {
		double x1 = 0.0;
		double y1 = 0.0;
		double x2 = 0.0;
		double y2 = 0.0;
		
		double widthStroke = 0.5;
		double[] colorStroke = null;
		String hposition = "absolute";
		
		GRLine refLine = new GRLine();
		
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("x1")) {
				x1 = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("y1")) {
				y1 = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("x2")) {
				x2 = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("y2")) {
				y2 = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("hposition")) {
				hposition = element.getValue();
			} else if(element.getName().equals("widthstroke")) {
				widthStroke = Double.parseDouble(element.getValue());
			} else if(element.getName().equals("colorstroke")) {
				colorStroke = getColorForPDF(element.getValue());
			} 
			
		}
		
		refLine.setX1(x1);
		refLine.setY1(y1);
		refLine.setX2(x2);
		refLine.setY2(y2);
		
		refLine.setWidthStroke(widthStroke);
		refLine.setHPosition(hposition);
		
		if(colorStroke != null) {
			refLine.setColorStroke(colorStroke[0], colorStroke[1], colorStroke[2]);
		}
		
		return refLine;
	}
	private GRCircle getGRCircle(Iterator iterator) {
		double x = 0.0;
		double y = 0.0;
		double radius = 0.0;
		
		double widthStroke = 0.5;
		double[] colorStroke = null;
		double[] colorFill = null;
		String hposition = "absolute";
		
		GRCircle refCircle = new GRCircle();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("x")) {
				x = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("y")) {
				y = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("radius")) {
				radius = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("hposition")) {
				hposition = element.getValue();
			} else if(element.getName().equals("widthstroke")) {
				widthStroke = Double.parseDouble(element.getValue());
			} else if(element.getName().equals("colorstroke")) {
				colorStroke = getColorForPDF(element.getValue());
			} else if(element.getName().equals("colorfill")) {
				colorFill = getColorForPDF(element.getValue());
			}
			
		}
		
		refCircle.setX(x);
		refCircle.setY(y);
		refCircle.setRadius(radius);
		
		refCircle.setWidthStroke(widthStroke);
		refCircle.setHPosition(hposition);
		
		if(colorStroke != null) {
			refCircle.setColorStroke(colorStroke[0], colorStroke[1], colorStroke[2]);
		}
		if(colorFill != null) {
			refCircle.setColorFill(colorFill[0], colorFill[1], colorFill[2]);
		}
		
		return refCircle;
	}
	private GRText readText(Element el) {
		double left = 0.0;
		double top = 0.0;
		double width = 0.0;
		double height = 0.0;
		String value = "";
		
		// Proprietà aggiunta il 20/07/2017
		// false <default>: Il testo inserito viene filtrato dei caratteri 0A e 0D
		// true: Il testo mantiene gli acapo per come si presentano nel grx
		boolean pre = false;
		
		double lineSpacing = 2.0;
		
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		GRText refText = new GRText();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("left")) {
				left = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("top")) {
				top = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("width")) {
				width = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("alignment")) {
				refText.setAlign(element.getValue());
			} else if(element.getName().equals("hposition")) {
				refText.setHPosition(element.getValue());
			} else if(element.getName().equals("linespacing")) {
				lineSpacing = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("value")) {
				value = GREncode_ISO8859.lineASCIIToOct(element.getValue(), pre);
				value = getTextValue(value);
				//value = getTextValue(element.getValue());
			} else if(element.getName().equals("pre")) {
				pre = Boolean.parseBoolean(element.getValue());
			}
		}
		
		refText.setLeft(left);
		refText.setTop(top);
		refText.setWidth(width);
		refText.setLineSpacing(lineSpacing);
		refText.setValue(value);
		
		refText.setFontResources(grdocument.getFontResources());

		return refText;
	}
	private String getTextFormat(String value) {
		/* Il format style dei testi è il seguente:
		 * [FontName:FontSize:Color R G B:FontUnderline (optional)]
		 */
		
		String REG_TEXT = "\\[([a-zA-Z0-9\\-_]+):([0-9]+)(:([0-9]+),([0-9]+),([0-9]+)){0,}(:underline|:none){0,1}\\]";
		String newValue = "";
		
		double cRED;
		double cGREEN;
		double cBLUE;
		
		Pattern pattern = Pattern.compile(REG_TEXT);
		Matcher matcher = pattern.matcher(value);
		
		String fontName = "";
		String underline = "";
				
		while(matcher.find()) {
			cRED = 0.0;
			cGREEN = 0.0;
			cBLUE = 0.0;
			
			fontName = matcher.group(1);
			String id = this.addFontResource(fontName);
			
			/* Gestione dei colori */
			if(matcher.group(3) != null) {
				cRED = GRDimension.fromRGBToPDF(Integer.parseInt(matcher.group(4)));
				cGREEN = GRDimension.fromRGBToPDF(Integer.parseInt(matcher.group(5)));
				cBLUE = GRDimension.fromRGBToPDF(Integer.parseInt(matcher.group(6)));
				
			}
				
			/* Gestione del sottolineato */
			if(matcher.group(7) != null) {
				underline = matcher.group(7).substring(1);
			} else {
				underline = "none";
			}
			newValue = newValue + "["+id+":"+matcher.group(2)+":"+cRED+","+cGREEN+","+cBLUE+":"+underline+"]";
			
		}
		
		return newValue;
	}
	private String getTextValue(String value) {
		String REG_TEXT = "\\[([a-zA-Z0-9\\-_]+):([0-9]+)(:([0-9]+),([0-9]+),([0-9]+)){0,}(:underline|:none){0,1}\\]([a-zA-Z0-9 ,!$%&;:\\\\\"\'\\?\\^\\.\\{\\}\\-\\/\\(\\)]+)";
		String newValue = "";
		
		double cRED;
		double cGREEN;
		double cBLUE;
		
		Pattern pattern = Pattern.compile(REG_TEXT);
		Matcher matcher = pattern.matcher(value);
		
		String fontName = "";
		String underline = "";
				
		while(matcher.find()) {
			cRED = 0.0;
			cGREEN = 0.0;
			cBLUE = 0.0;
			
			fontName = matcher.group(1);
			String id = this.addFontResource(fontName);
			
			/* Gestione dei colori */
			if(matcher.group(3) != null) {
				cRED = GRDimension.fromRGBToPDF(Integer.parseInt(matcher.group(4)));
				cGREEN = GRDimension.fromRGBToPDF(Integer.parseInt(matcher.group(5)));
				cBLUE = GRDimension.fromRGBToPDF(Integer.parseInt(matcher.group(6)));
				
			}
				
			/* Gestione del sottolineato */
			if(matcher.group(7) != null) {
				underline = matcher.group(7).substring(1);
			} else {
				underline = "none";
			}
			newValue = newValue + "["+id+":"+matcher.group(2)+":"+cRED+","+cGREEN+","+cBLUE+":"+underline+"]"+matcher.group(8);
			
		}
		
		return newValue;
	}
	private GRImage readImage(Element el) {
		double left = 0.0;
		double top = 0.0;
		double width = 0.0;
		double height = 0.0;
		
		String id = "";
		String hposition = "absolute";
		
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		GRImage refImage = new GRImage();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("left")) {
				left = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("top")) {
				top = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("width")) {
				width = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("height")) {
				height = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("hposition")) {
				hposition = element.getValue();
			} else if(element.getName().equals("path")) {
				File f = new File(element.getValue());
				if(!f.exists())
					return null;
				
				id = this.setImage(element.getValue());
			} 
			
		}
		
		refImage.setLeft(left);
		refImage.setTop(top);
		refImage.setWidth(width);
		refImage.setHeight(height);
		refImage.setId(id);
		refImage.setHPosition(hposition);
		
		return refImage;
	}
	private String setImage(String pathImage) {
		String id = this.addImageResource(pathImage);
		
		return id;
	}
	private GRTextCondition readTextCondition(Element el) {
		double left = 0.0;
		double top = 0.0;
		double width = 0.0;
		
		double lineSpacing = 2.0;
		
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		GRTextCondition refTextCondition = new GRTextCondition();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("left")) {
				left = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("top")) {
				top =  GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("width")) {
				width =  GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("alignment")) {
				refTextCondition.setAlign(element.getValue());
			} else if(element.getName().equals("hposition")) {
				refTextCondition.setHPosition(element.getValue());
			} else if(element.getName().equals("linespacing")) {
				lineSpacing = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("selectcase")) {
				/* Per prima cosa istanzia l'oggetto. Questo perch�� i vari
				 * condition genereranno ognuno un oggetto GRText con le 
				 * caratteristiche dell'oggetto GRTextCondition
				 */
				refTextCondition.setLeft(left);
				refTextCondition.setTop(top);
				refTextCondition.setWidth(width);
				refTextCondition.setLineSpacing(lineSpacing);
				
				refTextCondition.setFontResources(grdocument.getFontResources());

				/* A questo punto inserisce le condition */
				readSelectCaseTextCondition(element, refTextCondition);
			}
		}
		
		
		return refTextCondition;
		
	}
	private void readSelectCaseTextCondition(Element el, GRTextCondition refTextCondition) {
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("condition")) {
				readConditionTextCondition(element, refTextCondition);
			}
		}
	}
	private void readConditionTextCondition(Element el, GRTextCondition refTextCondition) {
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		String condition = null;
		String value = null;
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("case")) {
				condition = element.getValue();
			} else if(element.getName().equals("value")) {
				value = getTextValue(element.getValue());
			}
		}
		
		if(condition == null || value == null) {
			System.out.println("ERRORE::readConditionTextCondition: uno o pi? elementi sono mancanti!");
		}
		
		refTextCondition.addElement(condition, value);
		
	}
	private GRChart readChart(Element el) {
		short typeChart = -1;
		short view = -1;
		double left = 0.0;
		double top = 0.0;
		double width = 0.0;
		double height = 0.0;
		int gap = 0;
		double widthStroke = 0.5;
		String hposition = "absolute";
		
		double borderStroke = 0.0;
		String valueLabel = "nothing";
		String labelx = "hide";
		String labely = "hide";
		String barratio = "0.4";
		String name = "";
		
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		GRChart refChart = null;
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("type")) {
				typeChart = GRChart.typeChartFromStringToShort(element.getValue());
			} else if(element.getName().equals("left")) {
				left = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("top")) {
				top = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("width")) {
				width = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("height")) {
				height = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("view")) {
				view = GRChart.viewChartFromStringToShort(element.getValue());
			} else if(element.getName().equals("gap")) {
				gap = Integer.parseInt(element.getValue());
			} else if(element.getName().equals("hposition")) {
				hposition = element.getValue();
			} else if(element.getName().equals("widthstroke")) {
				widthStroke = Double.parseDouble(element.getValue());
			} else if(element.getName().equals("borderstroke")) {
				borderStroke = Double.parseDouble(element.getValue());
			} else if(element.getName().equals("valuelabel")) {
				valueLabel = element.getValue();
			} else if(element.getName().equals("labelx")) {
				labelx = element.getValue();
			} else if(element.getName().equals("labely")) {
				labely = element.getValue();
			} else if(element.getName().equals("barratio")) {
				barratio = element.getValue();
			} else if(element.getName().equals("name")) {
				name = element.getValue();
			} else if(element.getName().equals("data")) {
				/* Prima di procedere all'inserimento dei dati istanzia l'oggetto */
				refChart = GRChart.createChart(typeChart, view);
				
				refChart.setLeft(left);
				refChart.setTop(top);
				refChart.setWidth(width);
				refChart.setHeight(height);
				refChart.setGap(gap);
				refChart.setHPosition(hposition);
				
				refChart.setBorderStroke(borderStroke);
				refChart.setValueLabel(valueLabel);
				refChart.setLabelX(labelx);
				refChart.setLabelY(labely);
				refChart.setBarRatio(barratio);
				refChart.setName(name);
				
				readDataChart(element, refChart);
			}
		}
		
		return refChart;
	}
	private void readDataChart(Element el, GRChart refChart) {
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("voice")) {
				readVoiceDataChart(element, refChart);
				//refChart.addDataVoice(grvoice);
			} 
		}
	}
	private void readVoiceDataChart(Element el, GRChart refChart) {
		String label = "";
		double value = 0.0;
		double colorstroke[] = null;
		double color[] = null;
		
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		//refChart.addData();
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("label")) {
				label = element.getValue();
			} else if(element.getName().equals("value")) {
				value = Double.parseDouble(element.getValue());
			} else if(element.getName().equals("colorstroke")) {
				colorstroke = this.getColorForPDF(element.getValue());
			} else if(element.getName().equals("colorfill")) {
				color = this.getColorForPDF(element.getValue());
			}
		}
		
		if(colorstroke == null)
			refChart.addVoice(label, value, color[0], color[1], color[2]);
		else
			refChart.addVoice(label, value, colorstroke[0],colorstroke[1],colorstroke[2],color[0], color[1], color[2]);
		//refChart.addVoice(label, value, color[0], color[1], color[2]);
	}
	private GRGroup readGroup(Element el) {
		double top = 0.0;
		
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		GRGroup refGroup = new GRGroup();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("top")) {
				top = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("hposition")) {
				refGroup.setHPosition(element.getValue());
			} else if(element.getName().equals("condition")) {
				refGroup.setCondition(element.getValue());
			} else if(element.getName().equals("content")) {
				readContentGroup(element, refGroup);
			} 
		}
		
		refGroup.setTop(top);
		
		return refGroup;
	}
	private void readContentGroup(Element el, GRGroup refGroup) {
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("shape") || element.getName().equals("grshape")) {
				GRShape refShape = readShape(element);
				
				refGroup.addElement(refShape);
			} else if(element.getName().equals("image") || element.getName().equals("grimage")) {
				GRImage refImage = readImage(element);
				
				refGroup.addElement(refImage);
	
			} else if(element.getName().equals("text") || element.getName().equals("grtext")) {
				GRText refText = readText(element);
				
				refGroup.addElement(refText);
			} else if(element.getName().equals("textcondition")) {
				GRTextCondition refTextCondition = readTextCondition(element);
				
				refGroup.addElement(refTextCondition);
			} else if(element.getName().equals("list")) {
				GRList refList = readList(element);
				
				refGroup.addElement(refList);
				/*refList = new GRList(typography);
				readList(element);
				
				refGroup.addObj(refList);*/
			}
		}
	}
	private GRList readList(Element el) {
		double top = 0.0;
		double height = 0.0;
		String id = null;
		
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		GRList refList = new GRList();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("top")) {
				top = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("height")) {
				height = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("id")) {
				id = element.getValue();
			} else if(element.getName().equals("hposition")) {
				refList.setHPosition(element.getValue());
			} else if(element.getName().equals("row")) {
				readRowList(element, refList);
			}
		}
		
		refList.setTop(top);
		refList.setHeight(height);
		refList.setId(id);
		
		return refList;
		
	}
	private void readRowList(Element el, GRList refList) {
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("text")) {
				GRText refText = readText(element);
				
				refList.addElement(refText);
			} else if(element.getName().equals("image")) {
				GRImage refImage = readImage(element);
				
				refList.addElement(refImage);
			} else if(element.getName().equals("shape")) {
				GRShape refShape = readShape(element);
				
				refList.addElement(refShape);
			} 
		}
		 
	}
	private GRTableList readTableList(Element el) {
		double left = 0.0;
		double top = 0.0;
		String id = null;
		
		String visible = "always";
		
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		GRTableList refTableList = new GRTableList();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("left")) {
				left = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("top")) {
				top = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("id")) {
				id = element.getValue();
			} else if(element.getName().equals("cols")) {
				readColsTableList(element, refTableList);
			} else if(element.getName().equals("hposition")) {
				refTableList.setHPosition(element.getValue());
			} else if(element.getName().equals("visible")) {
				visible = element.getValue();
			} else if(element.getName().equals("thead")) {
				refTableList.setHead();
				readContentTableList(element, refTableList, GRTableListSection.TYPESECTION_HEAD);
			} else if(element.getName().equals("tbody")) {
				refTableList.setBody();
				readContentTableList(element, refTableList, GRTableListSection.TYPESECTION_BODY);
			} else if(element.getName().equals("tfooter")) {
				refTableList.setFooter();
				readContentTableList(element, refTableList, GRTableListSection.TYPESECTION_FOOT);
			}
		}
		
		refTableList.setLeft(left);
		refTableList.setTop(top);
		refTableList.setId(id);
		refTableList.setVisible(visible);
		
		return refTableList;
		
	}
	private void readColsTableList(Element el, GRTableList refTableList) {
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("cell")) {
				readTableListColumn(element, refTableList);
			} 
		}
		
	}
	private void readTableListColumn(Element el, GRTableList refTableList) {
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		refTableList.addColumn();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("width")) {
				refTableList.setColumnWidth(GRDimension.getDocumentDimension(typography,element.getValue()));
				
			} 
		}
		
	}
	private void readContentTableList(Element el, GRTableList refTableList, int typeCell) {
		short numColumns = 0;
		short totColumns = -1;
		
		double widthStroke = 0.5;
		double minHeight = GRDimension.getDocumentDimension(typography,"10.0");
		double[] colorStroke = null;
		double[] colorFill = null;
		GRTableListCell refTableListCell = null;
		
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("widthstroke")) {
				widthStroke = Double.parseDouble(element.getValue());
			} else if(element.getName().equals("colorstroke")) {
				colorStroke = getColorForPDF(element.getValue());
			} else if(element.getName().equals("colorfill")) {
				colorFill = getColorForPDF(element.getValue());
			} else if(element.getName().equals("minheight")) {
				minHeight = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("column")) {
				/* DEPRECATED. Mantiene compatibilit�� con vecchie versioni */
				totColumns = Short.parseShort(element.getValue());
			} else if(element.getName().equals("cell")) {
				refTableListCell = readCellTableList(element);
				
				refTableList.addCellSection(refTableListCell, typeCell);
				numColumns++;
			}
		}
		
		if(totColumns != -1)
			numColumns = totColumns;	// Per compatibilit�� con vecchie versioni
		
		refTableList.setSectionColumn(numColumns, typeCell);
		refTableList.setSectionMinHeight(minHeight, typeCell);
		refTableList.setSectionWidthStroke(widthStroke, typeCell);
		refTableList.setSectionColorStroke(colorStroke[0], colorStroke[1], colorStroke[2], typeCell);
		refTableList.setSectionColorFill(colorFill[0], colorFill[1], colorFill[2], typeCell);
		
	}
	private GRTableListCell readCellTableList(Element el) {
		double marginCell[] = new double[4];
		short column = 1;
		
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		GRTableListCell refTableListCell = new GRTableListCell();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("margincell")) {
				String margin[] = (element.getValue()).split(" ");
				marginCell[0] = GRDimension.getDocumentDimension(typography,margin[0]);
				marginCell[1] = GRDimension.getDocumentDimension(typography,margin[1]);
				marginCell[2] = GRDimension.getDocumentDimension(typography,margin[2]);
				marginCell[3] = GRDimension.getDocumentDimension(typography,margin[3]);
				
			} else if(element.getName().equals("column")) {
				column = Short.parseShort(element.getValue());
			} else if(element.getName().equals("text")) {
				GRText refText = readText(element);
				
				refTableListCell.addObj(refText);
			} else if(element.getName().equals("textcondition")) {
				GRTextCondition refTextCondition = readTextCondition(element);
				
				refTableListCell.addObj(refTextCondition); 
			} else if(element.getName().equals("image")) {
				GRImage refImage = readImage(element);
				
				refTableListCell.addObj(refImage);
			} else if(element.getName().equals("shape")) {
				GRShape refShape = readShape(element);
				
				refTableListCell.addObj(refShape);
			} else if(element.getName().equals("list")) {
				GRList grlist = readList(element);
				
				refTableListCell.addObj(grlist);
			} else if(element.getName().equals("tablelist")) {
				GRTableList grtablelist = readTableList(element);
				
				refTableListCell.addObj(grtablelist);
			} else if(element.getName().equals("group")) {
				GRGroup grgroup = readGroup(element);
				
				refTableListCell.addObj(grgroup);
			}
		}
		
		// Totale colonne occupate dalla cella
		refTableListCell.setColumns(column);
				
		// Margini
		refTableListCell.getMarginLeft(marginCell[0]);
		refTableListCell.getMarginTop(marginCell[0]);
		refTableListCell.getMarginRight(marginCell[0]);
		refTableListCell.getMarginBottom(marginCell[0]);
				
		return refTableListCell;
	}
	private GRBarcode readBarcode(Element el) throws GRCompileTypeNotDefined {
		double left = 0.0;
		double top = 0.0;
		double width = 0.0;
		double height = 0.0;
		String value = null;
		String type = null;
		String hposition = "absolute";
		
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		GRBarcode refBarcode = null;
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("type")) {
				type = element.getValue();
			} else if(element.getName().equals("left")) {
				left = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("top")) {
				top = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("width")) {
				width = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("height")) {
				height = GRDimension.getDocumentDimension(typography,element.getValue());
			} else if(element.getName().equals("hposition")) {
				hposition = element.getValue();
			} else if(element.getName().equals("value")) {
				value = element.getValue();
			}
		}
		
		refBarcode = GRBarcode.createBarcode(type);
		
		if(refBarcode == null)
			throw new GRCompileTypeNotDefined(type);
		
		refBarcode.setLeft(left);
		refBarcode.setTop(top);
		refBarcode.setWidth(width);
		refBarcode.setHeight(height);
		refBarcode.setHPosition(hposition);
		refBarcode.setValue(value);
		
		return refBarcode;
	}
	private GRSystemObject readSystemObject(Element el) throws GRCompileTypeNotDefined {
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		GRSystemObject refSysObj = null;
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("type")) {
				if(((Element)element.getParent()).getName().equals("sysobject")) {
					if(element.getValue().equals("paginaNdiM")) {
						refSysObj = getGRSysPaginaNdiM(iterator);
					} 
						
				}
				
				if(refSysObj == null)
					throw new GRCompileTypeNotDefined(element.getValue());
			}
		}
		
		
		return refSysObj;
	}
	private GRSystemObject getGRSysPaginaNdiM(Iterator iterator) {
		double left = 0.0;
		double top = 0.0;
		
		double fontSize = 8.0;
		double[] fontColor = null;
		
		String hposition = "absolute";
		String language = "it";
		String fontStyle = "";
				
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(element.getName().equals("left")) {
				if(((Element)element.getParent()).getName().equals("sysobject")) {
					left = GRDimension.getDocumentDimension(typography,element.getValue());
				} 
			} else if(element.getName().equals("top")) {
				if(((Element)element.getParent()).getName().equals("sysobject")) {
					top = GRDimension.getDocumentDimension(typography,element.getValue());
				}
			} else if(element.getName().equals("fontsize")) {
				if(((Element)element.getParent()).getName().equals("sysobject")) {
					fontSize = Double.parseDouble(element.getValue());
				}
			} else if(element.getName().equals("fontcolor")) {
				if(((Element)element.getParent()).getName().equals("sysobject")) {
					fontColor = getColorForPDF(element.getValue());
				}
			} else if(element.getName().equals("language")) {
				if(((Element)element.getParent()).getName().equals("sysobject")) {
					language = element.getValue();
				}
			} else if(element.getName().equals("fontstyle")) {
				if(((Element)element.getParent()).getName().equals("sysobject")) {
					fontStyle = this.getTextFormat(element.getValue());
				}
			} 
			
		}
		
		GRSysPaginaNdiM refSys = new GRSysPaginaNdiM(fontStyle,language);
		
		refSys.setPosition(left,  top);
		
		
		return refSys;
	}
	private double[] getColorForPDF(String value) {
		double[] color = new double[3];
		
		if(value.equals("transparent")) {
			color[0] = -1;
			color[1] = -1;
			color[2] = -1;
		} else {
			String[] buffer = value.split(" ");
			color[0] = GRDimension.fromRGBToPDF(Integer.parseInt(buffer[0]));
			color[1] = GRDimension.fromRGBToPDF(Integer.parseInt(buffer[1]));
			color[2] = GRDimension.fromRGBToPDF(Integer.parseInt(buffer[2]));
		}
		
		return color;
	}
	private String addFontResource(String value) {
		/* Verifica che il nome del font sia gi�� stato censito
		 * Se ok ritorna il valore relativo all'ID
		 * Se ko censisce il font e ritorna il nuovo id
		 * 
		 */
		String id = null;
		short fontType = 5; // Per ora gestisce solamente True Type
		
		if(grfontResource == null)
			grfontResource = new Vector<GRFontResource>();
		
		for(int i = 0;i < grfontResource.size();i++) {
			if(grfontResource.get(i).getName().equals(value)) {
				id = grfontResource.get(i).getId();
				
				break;
			}
		}
		
		if(id == null) {
			GRFontResource refFont = new GRFontResource(pathFont, value, grfontResource.size());
			id = refFont.getId();
			grfontResource.add(refFont);
			
			/* Aggiunge il font al document */
			grdocument.addFont();
			
			// ID
			grdocument.setFontId(id);
			
			// NAME
			grdocument.setFontName(refFont.getName());
			
			// TYPE
			grdocument.setFontType(fontType);
			
			// STREAM
			grdocument.setFontLenOriginalStream(refFont.getLenOriginalStream());
			grdocument.setFontLenCompressedStream(refFont.getLenCompressedStream());
			grdocument.setFontStream(refFont.getStream());
		
		}
		
		return id;
	}
	
	private String addImageResource(String pathImage) {
		/* Verifica che l'immagine sia gi�� stata censita
		 * Se ok ritorna il valore relativo all'ID
		 * Se ko censisce l'immagine e ritorna il nuovo id
		 * 
		 */
		String id = null;
		
		if(grimageResource == null)
			grimageResource = new Vector<GRImageResource>();
		
		for(int i = 0;i < grimageResource.size();i++) {
			if(grimageResource.get(i).getPath().equals(pathImage)) {
				id = grimageResource.get(i).getId();
				
				break;
			}
		}
		
		if(id == null) {
			GRImageResource refImage = new GRImageResource(pathImage, grimageResource.size());
			id = refImage.getId();
			grimageResource.add(refImage);
			
			/* Aggiunge il font al document */
			grdocument.addImageProperty();
			
			// ID
			grdocument.setImageId(id);
			
			// TYPE
			grdocument.setImageType(refImage.getTypeImage());
			
			// ORIGINALWIDTH
			grdocument.setImageOriginalWidth(refImage.getDimensionWidth());
			
			// ORIGINALHEIGHT
			grdocument.setImageOriginalHeight(refImage.getDimensionHeight());
			
			// STREAM
			grdocument.setImageStream(refImage.getImage());
			
		}
		
		return id;
	}
	
	private void insertSYSOBJECT(GRSystemObject grobj, RandomAccessFile rOut) throws IOException {
		short typeObj = grobj.getType();
		
		rOut.writeShort(typeObj);
		rOut.writeDouble(grobj.getLeft());
		rOut.writeDouble(grobj.getTop());
		rOut.writeShort(grobj.getHPosition());
		
		if(typeObj == GRSystemObject.TYPESYSOBJECT_PAGINANDIM) {
			GRSysPaginaNdiM grsys = (GRSysPaginaNdiM)grobj;
			
			rOut.writeShort(grsys.getLanguage());
			
			rOut.writeInt(grsys.getFontStyle().length());
			rOut.writeBytes(grsys.getFontStyle());
			
		}
		rOut.writeLong(0);	// Address extended property - not used
	}
	private void insertSHAPE(GRObject grobj, RandomAccessFile rOut) throws IOException {
		GRShape grshape = (GRShape)grobj;
		short typeShape = grshape.getTypeShape();
		
		// A seconda della tipologia i bytes occupati possono essere diversi
		// Prima entrano i campi uguali per tutti
		rOut.writeShort(typeShape);
		rOut.writeDouble(grshape.getWidthStroke());
		rOut.writeDouble(grshape.getColorStrokeRED());
		rOut.writeDouble(grshape.getColorStrokeGREEN());
		rOut.writeDouble(grshape.getColorStrokeBLUE());
		
		if(typeShape == GRShape.TYPESHAPE_RECT) {
			GRRectangle grrect = (GRRectangle)grshape;
			
			rOut.writeDouble(grrect.getLeft());
			rOut.writeDouble(grrect.getTop());
			rOut.writeShort(grrect.getHPosition());
		
			rOut.writeLong(0);	// Address extended property - not used
			
			rOut.writeDouble(grrect.getWidth());
			rOut.writeDouble(grrect.getHeight());
			
			// Colore di riempimento. Se -1 sar�� trasparente
			rOut.writeDouble(grrect.getColorFillRED());
			rOut.writeDouble(grrect.getColorFillGREEN());
			rOut.writeDouble(grrect.getColorFillBLUE());
		} else if(typeShape == GRShape.TYPESHAPE_LINE) {
			GRLine grline = (GRLine)grshape;
		
			rOut.writeDouble(grline.getX1());
			rOut.writeDouble(grline.getY1());
			rOut.writeShort(grline.getHPosition());
			
			rOut.writeLong(0);	// Address extended property - not used
			
			rOut.writeDouble(grline.getX2());
			rOut.writeDouble(grline.getY2());
		} else if(typeShape == GRShape.TYPESHAPE_CIRCLE) {
			GRCircle grcircle = (GRCircle)grshape;
			
			rOut.writeDouble(grcircle.getX());
			rOut.writeDouble(grcircle.getY());
			rOut.writeShort(grcircle.getHPosition());
			
			rOut.writeLong(0);	// Address extended property - not used
			
			rOut.writeDouble(grcircle.getRadius());
			
			// Colore di riempimento. Se -1 sar�� trasparente
			rOut.writeDouble(grcircle.getColorFillRED());
			rOut.writeDouble(grcircle.getColorFillGREEN());
			rOut.writeDouble(grcircle.getColorFillBLUE());
		}
	}
	private void insertCHART(GRObject grobj, RandomAccessFile rOut) throws IOException {
		GRChart grchart = (GRChart)grobj;
		short typeChart = grchart.getTypeChart();
		
		rOut.writeShort(typeChart);
		rOut.writeDouble(grchart.getWidthStroke());
		rOut.writeDouble(grchart.getLeft());
		rOut.writeDouble(grchart.getTop());
		rOut.writeDouble(grchart.getWidth());
		rOut.writeDouble(grchart.getHeight());
		rOut.writeShort(grchart.getHPosition());
		rOut.writeShort(grchart.getView());
		rOut.writeInt(grchart.getGap());
		
		rOut.writeShort(0);	// Al momento non �� gestita la legenda
		
		String name = grchart.getName();
		rOut.writeInt(name.length());
		if(name.length() > 0)
			rOut.writeBytes(name);
		//rOut.writeInt(0);	// Al momento non �� gestita la possibilit�� di dati dinamici
		
		rOut.writeInt(grchart.getTotaleDataVoice());
		if(grchart.getTotaleDataVoice() > 0) {
			Vector<GRChartVoice> grdata = grchart.getVoice();
		
			for(int i = 0;i < grdata.size();i++) {
				GRChartVoice grvoice = grdata.get(i);
				
				rOut.writeInt(grvoice.getLabel().length());
				rOut.writeBytes(grvoice.getLabel());
				rOut.writeDouble(grvoice.getValue());
				
				rOut.writeDouble(grvoice.getColorStrokeRED());
				rOut.writeDouble(grvoice.getColorStrokeGREEN());
				rOut.writeDouble(grvoice.getColorStrokeBLUE());
				
				rOut.writeDouble(grvoice.getColorFillRED());
				rOut.writeDouble(grvoice.getColorFillGREEN());
				rOut.writeDouble(grvoice.getColorFillBLUE());
			}
		}
		
		rOut.writeLong(1);  /*
		 					 * Fino alla 1.31 valeva 0. Nuove proprietà aggiunte
							 *
							 * Nuove proprietà:
							 * -borderstroke
							 * -valueLabel
							 * 
							 */
		rOut.writeDouble(grchart.getBorderStroke());
		rOut.writeShort(grchart.getValueLabel());
		rOut.writeShort(grchart.getLabelX());
		rOut.writeShort(grchart.getLabelY());
		rOut.writeDouble(grchart.getBarRatio());
		
		// Chiusura
		rOut.writeLong(0);	// Address extended property - not used
		
	}
	private void insertIMAGE(GRObject grobj, RandomAccessFile rOut) throws IOException {
		GRImage grimage = (GRImage)grobj;
		
		rOut.writeDouble(grimage.getLeft());
		rOut.writeDouble(grimage.getTop());
		rOut.writeDouble(grimage.getWidth());
		rOut.writeDouble(grimage.getHeight());
		rOut.writeShort(grimage.getHPosition());
		
		rOut.writeInt((grimage.getReferenceId()).length());
		rOut.writeBytes(grimage.getReferenceId());
		
		rOut.writeLong(0);	// Address extended property - not used
	}
	private void insertTEXT(GRObject grobj, RandomAccessFile rOut) throws IOException {
		GRText grtext = (GRText)grobj;
		
		rOut.writeDouble(grtext.getLeft());
		rOut.writeDouble(grtext.getTop());
		rOut.writeDouble(grtext.getWidth());
		rOut.writeDouble(grtext.getHeight());
		rOut.writeShort(grtext.getAlign());
		rOut.writeShort(grtext.getHPosition());
		rOut.writeDouble(grtext.getLineSpacing());
		rOut.writeInt((grtext.getValue()).length());
		rOut.writeBytes(grtext.getValue());
		
		rOut.writeLong(0);	// Address extended property - not used
	}
	private void insertTEXTCONDITION(GRObject grobj, RandomAccessFile rOut) throws IOException {
		GRTextCondition grtextCondition = (GRTextCondition)grobj;
		
		rOut.writeDouble(grtextCondition.getLeft());
		rOut.writeDouble(grtextCondition.getTop());
		rOut.writeDouble(grtextCondition.getWidth());
		rOut.writeDouble(grtextCondition.getHeight());
		rOut.writeShort(grtextCondition.getAlign());
		rOut.writeShort(grtextCondition.getHPosition());
		rOut.writeDouble(grtextCondition.getLineSpacing());
		
		rOut.writeLong(0);	// Address extended property - not used
		
		/* Cicla per tutte le condizioni */
		rOut.writeInt(grtextCondition.getTotalCondition());
		for(int i = 0;i < grtextCondition.getTotalCondition();i++) {
			rOut.writeInt(grtextCondition.getCondition(i).length());
			rOut.writeBytes(grtextCondition.getCondition(i));
			
			rOut.writeInt(grtextCondition.getValueCondition(i).length());
			rOut.writeBytes(grtextCondition.getValueCondition(i));
		}
	}
	private void insertGROUP(GRObject grobj, RandomAccessFile rOut) throws IOException {
		GRGroup grcontext = (GRGroup)grobj;
		
		rOut.writeDouble(grcontext.getLeft());
		rOut.writeDouble(grcontext.getTop());
		rOut.writeDouble(grcontext.getHeight());
		rOut.writeShort(grcontext.getHPosition());
		
		if(grcontext.getCondition() == null) 
			rOut.writeInt(0);	// Nessuna condizione. Viene sempre visualizzato
		else {
			rOut.writeInt((grcontext.getCondition()).length());
			rOut.writeBytes(grcontext.getCondition());
		}
		rOut.writeLong(0);	// Address extended property - not used
		
		rOut.writeInt(grcontext.getTotaleElement());
		
		// Cicla per tutti gli oggetti e li inserisce
		for(int i = 0;i < grcontext.getTotaleElement();i++) {
			short type = grcontext.getElement(i).getType();
			rOut.writeShort(type);
			
			if(type == GRObject.TYPEOBJ_TEXT) {
				this.insertTEXT(grcontext.getElement(i),rOut);
			} else if(type == GRObject.TYPEOBJ_IMAGE) {
				this.insertIMAGE(grcontext.getElement(i),rOut);
			} else if(type == GRObject.TYPEOBJ_SHAPE) {
				this.insertSHAPE(grcontext.getElement(i),rOut);
			} else if(type == GRObject.TYPEOBJ_LIST) {
				this.insertLIST(grcontext.getElement(i), rOut);
			} else if(type == GRObject.TYPEOBJ_TEXTCONDITION) {
				this.insertTEXTCONDITION(grcontext.getElement(i),rOut);
			}
		}
	}
	private GRList insertLIST(GRObject grobj, RandomAccessFile rOut) throws IOException {
		GRList grlist = (GRList)grobj;
		
		rOut.writeDouble(grlist.getTop());
		rOut.writeDouble(grlist.getHeight());
		rOut.writeShort(grlist.getHPosition());
		rOut.writeInt((grlist.getId()).length());
		rOut.writeBytes(grlist.getId());
		
		rOut.writeLong(0);	// Address extended property - not used
		
		rOut.writeInt(grlist.getTotaleElement());
		for(int i = 0;i < grlist.getTotaleElement();i++) {
			short type = grlist.getElement(i).getType();
			rOut.writeShort(type);
			
			if(type == GRObject.TYPEOBJ_TEXT) {
				this.insertTEXT(grlist.getElement(i),rOut);
			} else if(type == GRObject.TYPEOBJ_IMAGE) {
				this.insertIMAGE(grlist.getElement(i),rOut);
			} else if(type == GRObject.TYPEOBJ_SHAPE) {
				this.insertSHAPE(grlist.getElement(i),rOut);
			}
		}
		
		return grlist;
	}
	private GRTableList insertTABLELIST(GRObject grobj, RandomAccessFile rOut) throws IOException {
		GRTableList grtablelist = (GRTableList)grobj;
		int nCol;
		int wCell;
		
		rOut.writeDouble(grtablelist.getLeft());
		rOut.writeDouble(grtablelist.getTop());
		rOut.writeShort(grtablelist.getVisible());
		rOut.writeShort(grtablelist.getHPosition());
		rOut.writeInt((grtablelist.getId()).length());
		rOut.writeBytes(grtablelist.getId());
		
		rOut.writeShort(grtablelist.hasHead());
		rOut.writeShort(grtablelist.hasFooter());
		
		rOut.writeLong(0);	// Address extended property - not used
		
		rOut.writeShort(grtablelist.getTotaleColumn());
		for(int i = 0;i < grtablelist.getTotaleColumn();i++) {
			rOut.writeDouble(grtablelist.getColumnWidth(i));
		}
		
		// HEAD
		GRTableListSection grtablelistHead = grtablelist.getHead();
		if(grtablelistHead != null) {
			rOut.writeShort(grtablelistHead.getColumns());
			rOut.writeDouble(grtablelistHead.getMinHeight());
			rOut.writeDouble(grtablelistHead.getWidthStroke());
			rOut.writeDouble(grtablelistHead.getColorStrokeRED());
			rOut.writeDouble(grtablelistHead.getColorStrokeGREEN());
			rOut.writeDouble(grtablelistHead.getColorStrokeBLUE());
			rOut.writeDouble(grtablelistHead.getColorFillRED());
			rOut.writeDouble(grtablelistHead.getColorFillGREEN());
			rOut.writeDouble(grtablelistHead.getColorFillBLUE());
			
			nCol = 0;
			wCell = 0;
			
			while(wCell < grtablelist.getTotaleColumn()) {
				GRTableListCell refCell = grtablelistHead.getCell(nCol);
				
				wCell = wCell + refCell.getColumns();
				// Totale colonne occupate dalla cella
				rOut.writeShort(refCell.getColumns());
				
				// Margini della cella
				rOut.writeDouble(refCell.getMarginLeft());
				rOut.writeDouble(refCell.getMarginTop());
				rOut.writeDouble(refCell.getMarginRight());
				rOut.writeDouble(refCell.getMarginBottom());
						
				rOut.writeInt(refCell.getTotaleElement());
				for(int nCell = 0;nCell < refCell.getTotaleElement();nCell++) {
					short type = refCell.getElement(nCell).getType();
					rOut.writeShort(type);
							
					if(type == GRObject.TYPEOBJ_TEXT) {
						this.insertTEXT(refCell.getElement(nCell),rOut);
					} else if(type == GRObject.TYPEOBJ_IMAGE) {
						this.insertIMAGE(refCell.getElement(nCell),rOut);
					} else if(type == GRObject.TYPEOBJ_SHAPE) {
						this.insertSHAPE(refCell.getElement(nCell),rOut);
					}
				}
				
				nCol++;
			}
		}
		
		// BODY
		GRTableListSection grtablelistBody = grtablelist.getBody();
		
		rOut.writeShort(grtablelistBody.getColumns());
		rOut.writeDouble(grtablelistBody.getMinHeight());
		rOut.writeDouble(grtablelistBody.getWidthStroke());
		rOut.writeDouble(grtablelistBody.getColorStrokeRED());
		rOut.writeDouble(grtablelistBody.getColorStrokeGREEN());
		rOut.writeDouble(grtablelistBody.getColorStrokeBLUE());
		rOut.writeDouble(grtablelistBody.getColorFillRED());
		rOut.writeDouble(grtablelistBody.getColorFillGREEN());
		rOut.writeDouble(grtablelistBody.getColorFillBLUE());
		
		nCol = 0;
		wCell = 0;
		while(wCell < grtablelist.getTotaleColumn()) {
			GRTableListCell refCell = grtablelistBody.getCell(nCol);
			
			wCell = wCell + refCell.getColumns();
			// Totale colonne occupate dalla cella
			rOut.writeShort(refCell.getColumns());
			
			// Margini della cella
			rOut.writeDouble(refCell.getMarginLeft());
			rOut.writeDouble(refCell.getMarginTop());
			rOut.writeDouble(refCell.getMarginRight());
			rOut.writeDouble(refCell.getMarginBottom());
			
			rOut.writeInt(refCell.getTotaleElement());
			
			for(int nCell = 0;nCell < refCell.getTotaleElement();nCell++) {
				short type = refCell.getElement(nCell).getType();
				rOut.writeShort(type);
				
				if(type == GRObject.TYPEOBJ_TEXT) {
					this.insertTEXT(refCell.getElement(nCell),rOut);
				} else if(type == GRObject.TYPEOBJ_IMAGE) {
					this.insertIMAGE(refCell.getElement(nCell),rOut);
				} else if(type == GRObject.TYPEOBJ_SHAPE) {
					this.insertSHAPE(refCell.getElement(nCell),rOut);
				} else if(type == GRObject.TYPEOBJ_LIST) {
					this.insertLIST(refCell.getElement(nCell), rOut);
				} else if(type == GRObject.TYPEOBJ_TABLELIST) {
					this.insertTABLELIST(refCell.getElement(nCell), rOut);
				} else if(type == GRObject.TYPEOBJ_GROUP) {
					this.insertGROUP(refCell.getElement(nCell), rOut);
				}
			}
			
			nCol++;
		}
		
		// FOOTER
		GRTableListSection grtablelistFooter = grtablelist.getFooter();
		if(grtablelistFooter != null) {
			rOut.writeShort(grtablelistFooter.getColumns());
			rOut.writeDouble(grtablelistFooter.getMinHeight());
			rOut.writeDouble(grtablelistFooter.getWidthStroke());
			rOut.writeDouble(grtablelistFooter.getColorStrokeRED());
			rOut.writeDouble(grtablelistFooter.getColorStrokeGREEN());
			rOut.writeDouble(grtablelistFooter.getColorStrokeBLUE());
			rOut.writeDouble(grtablelistFooter.getColorFillRED());
			rOut.writeDouble(grtablelistFooter.getColorFillGREEN());
			rOut.writeDouble(grtablelistFooter.getColorFillBLUE());
			
			nCol = 0;
			wCell = 0;
			
			while(wCell < grtablelist.getTotaleColumn()) {
				GRTableListCell refCell = grtablelistFooter.getCell(nCol);
				
				wCell = wCell + refCell.getColumns();
				// Totale colonne occupate dalla cella
				rOut.writeShort(refCell.getColumns());
				
				// Margini della cella
				rOut.writeDouble(refCell.getMarginLeft());
				rOut.writeDouble(refCell.getMarginTop());
				rOut.writeDouble(refCell.getMarginRight());
				rOut.writeDouble(refCell.getMarginBottom());
						
				rOut.writeInt(refCell.getTotaleElement());
				for(int nCell = 0;nCell < refCell.getTotaleElement();nCell++) {
					short type = refCell.getElement(nCell).getType();
					rOut.writeShort(type);
							
					if(type == GRObject.TYPEOBJ_TEXT) {
						this.insertTEXT(refCell.getElement(nCell),rOut);
					} else if(type == GRObject.TYPEOBJ_TEXTCONDITION) {
						this.insertTEXTCONDITION(refCell.getElement(nCell),rOut);
					} else if(type == GRObject.TYPEOBJ_IMAGE) {
						this.insertIMAGE(refCell.getElement(nCell),rOut);
					} else if(type == GRObject.TYPEOBJ_SHAPE) {
						this.insertSHAPE(refCell.getElement(nCell),rOut);
					} 
				}
				
				nCol++;
			}
		}
		
		return grtablelist;
	}
	private void insertBARCODE(GRObject grobj, RandomAccessFile rOut) throws IOException {
		GRBarcode grbarcode = (GRBarcode)grobj;
		short typeBarcode = grbarcode.getTypeBarcode();
		
		rOut.writeShort(typeBarcode);
		rOut.writeDouble(grbarcode.getLeft());
		rOut.writeDouble(grbarcode.getTop());
		rOut.writeDouble(grbarcode.getWidth());
		rOut.writeDouble(grbarcode.getHeight());
		rOut.writeShort(grbarcode.getHPosition());
		
		rOut.writeInt((grbarcode.getValue()).length());
		rOut.writeBytes(grbarcode.getValue());
		
		rOut.writeLong(0);	// Address extended property - not used
	}
}
