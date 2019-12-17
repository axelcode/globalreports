/*
 * ==========================================================================
 * class name  : com.globalreports.engine.GRPDF
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
package com.globalreports.engine;

import java.io.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

import com.globalreports.compiler.GRCompiler;
import com.globalreports.engine.err.*;
import com.globalreports.engine.filter.GRFlateDecode;
import com.globalreports.engine.objects.GRSystemObject;
import com.globalreports.engine.structure.grbinary.GRDocument;
import com.globalreports.engine.structure.grbinary.GRGlobalPDF;
import com.globalreports.engine.structure.grbinary.GRLayout;
import com.globalreports.engine.structure.grbinary.data.GRData;
import com.globalreports.engine.structure.grpdf.GRPDFFile;
import com.globalreports.engine.structure.grpdf.GRPageStream;
import com.globalreports.engine.structure.grpdf.GRXrefTable;
import com.globalreports.engine.structure.grbinary.GRImageProperty;

public class GRPDF {
	/**
	 * Il nome della libreria con il relativo copyright
	 */
	public static final String 	GR_PRODUCER			= "Global Reports (C) 2015-2018";
	/**
	 * Il numero di versione relativo al rilascio corrente
	 */
	public static final int		GR_VERSION_MAJOR	= 1;
	/**
	 * Il numero di versione relativo al rilascio corrente
	 */
	public static final int		GR_VERSION_MINOR	= 31;

	public static final int		GR_VERSION_BETAVERSION = 1;
	
	private GRGlobalPDF grglobal;			// Oggetto globale che rappresenta la sessione corrente;
	
	private GRLayout grlayout;				// Rappresenta il layout in formato GRB
	private GRData grdata;					// Rappresenta il file xml contenente i dati variabili
	
	private GRPDFFile pdfAttach;			// Un oggetto che contiene tutti i pdf che si
											// vogliono aggiungere in coda al pdf generato
											// da GlobalReports
	private GRPDFFile pdfAttachXml;			// Contiene i PDF da allegare che
											// vengono specificati a runtime
											// nell'xml data
	
	public static String GR_TITLE;
	public static String GR_AUTHOR;
	public static String GR_CREATOR;
	
	
	/** 
	 * Crea un oggetto <i>GlobalReports Engine</i> per la generazione di files .PDF
	 */
	public GRPDF() {
		GR_CREATOR = null;
		
		pdfAttach = null;
		grglobal = null;
		
	}
	private synchronized void appendPDFXml(String pathPDF) throws GRAttachmentException {
		if(pdfAttachXml == null)
			pdfAttachXml = new GRPDFFile();
		
		pdfAttachXml.appendPDF(pathPDF);
			
	}
	/**
	 * Aggiunge un allegato in formato PDF, da inserire in coda al PDF generato
	 * 
	 * @param pathPDF Il nomde del file .pdf comprensivo del percorso ove è collocato.
	 * @throws GRAttachmentException Se la lettura dell'allegato presenta dei problemi.
	 */
	public synchronized void appendPDF(String pathPDF) throws GRAttachmentException {
		if(pdfAttach == null)
			pdfAttach = new GRPDFFile();
		
		pdfAttach.appendPDF(pathPDF);
		
	}
	/**
	 * Svuota la memoria dalla presenza di eventuali allegati
	 * 
	 */
	public void clearAttached() {
		pdfAttach = null;
	}
	/**
	 * Imposta il titolo da dare al documento PDF. Questo comparirà nella finestra <i>Proprietà</i> sotto la voce <b>Titolo</b>
	 * 
	 * @param value Il titolo da associare al PDF.
	 */
	public void setTitle(String value) {
		GR_TITLE = value;
	}
	/**
	 * Imposta il nome del proprietario del documento PDF. Questo comparirà nella finestra <i>Proprietà</i> sotto la voce <b>Autore</b>
	 * 
	 * @param value Il nome del proprietario del documento PDF.
	 */
	public void setAuthor(String value) {
		GR_AUTHOR = value;
	}
	/**
	 * Imposta il nome dell'applicazione del file all'interno delle proprietà del PDF. Questo comparirà nella finestra <i>Proprietà</i> sotto la voce <b>Applicazione</b>
	 * 
	 * @param value Il nome dell'applicazione da associare alla creazione del file.
	 */
	public void setCreator(String value) {
		GR_CREATOR = value;
	}
	/**
	 * Imposta il layout da utilizzare per la generazione del file PDF
	 * 
	 * @param pathLayout Il nome del file .grb comprensivo del percorso ove è collocato.
	 * @throws GRLayoutException Se il layout non è presente.
	 */
	public synchronized void setLayout(String pathLayout) throws GRLayoutException {
		grlayout = new GRLayout(pathLayout);
		
	}
	
	public synchronized void clearResources() {
		System.out.println(grlayout);
	}
	private int allegaDaXml(String pathPDF) throws GRAttachmentException, GRPdfPathNotFoundException, GRPdfIOWriteException {
		
		if(pdfAttachXml == null)
			return -1;
		
		// Genera un vector temporaneo
		GRPDFFile pdfTemp = new GRPDFFile();
		pdfTemp.appendPDF(pathPDF);
		for(int i = 0;i < pdfAttachXml.getNumFiles();i++) {
			pdfTemp.appendGRPDFReader(pdfAttachXml.getGRPDFReader(i));
		}
		
		int pagineFinali = pdfTemp.mergePDF(pathPDF);
		
		return pagineFinali;
	}
	private int allega(String pathPDF) throws GRAttachmentException, GRPdfPathNotFoundException, GRPdfIOWriteException {
		if(pdfAttach == null)
			return -1;
		
		// Genera un vector temporaneo
		GRPDFFile pdfTemp = new GRPDFFile();
		pdfTemp.appendPDF(pathPDF);
		for(int i = 0;i < pdfAttach.getNumFiles();i++) {
			pdfTemp.appendGRPDFReader(pdfAttach.getGRPDFReader(i));
		}
		
		int pagineFinali = pdfTemp.mergePDF(pathPDF);
		
		return pagineFinali;
		
	}
	private int generaPDF(String namePDF) throws GRPdfPathNotFoundException,
												 GRPdfIOWriteException,
												 GRValidateException,
												 GRBarcodeException {
		RandomAccessFile rPdf = null;
		GRDocument grdocument;
		GRXrefTable grxref;
		long startXRef;
		String addressKids = "";	// Serve per avere la stringa degli indirizzi delle pagine precompilata
		String addressFonts = "";	// Come per i kids precompila la stringa dei riferimenti
		String addressImages = "";	// Stringa dei riferimenti alle immagini
		int numberObj = 3;	// I primi 2 sono rispettivamente:
							// 1: /Type/Catalog
							// 2: /Type/Pages
		
		
		grglobal = new GRGlobalPDF();	// Istanzia l'oggetto di sistema
		
		try {
			// Se il file esiste lo cancella
			File f = new File(namePDF);
			f.delete();
			
			rPdf = new RandomAccessFile(namePDF,"rw");
			grdocument = grlayout.getDocument();
			grxref = new GRXrefTable();
			
			// START PDF
			rPdf.writeBytes("%PDF-1.4\n");
			
			// Inserisce eventuali immagini
			for(int i = 0;i < grdocument.getTotaleImage();i++) {
				
				// XOBJECT
				grxref.addAddress(numberObj, rPdf.getFilePointer());
				rPdf.writeBytes(numberObj+" 0 obj\n");
				addressImages = addressImages + "/"+grdocument.getImageId(i)+" "+numberObj+" 0 R ";
				numberObj++;
				
				rPdf.writeBytes("<< /Type /XObject\n");
				rPdf.writeBytes("/Subtype /Image\n");
				rPdf.writeBytes("/ProcSet [/PDF/Text/ImageB/ImageC/ImageI]\n");
				rPdf.writeBytes("/Width "+grdocument.getImageOriginalWidth(i)+"\n");
				rPdf.writeBytes("/Height "+grdocument.getImageOriginalHeight(i)+"\n");
				rPdf.writeBytes("/ColorSpace /DeviceRGB\n");
				rPdf.writeBytes("/BitsPerComponent 8\n");
				
				byte[] bufferImage = null;
				
				switch(grdocument.getImageType(i)) {
					case GRImageProperty.TYPE_JPEG:
						rPdf.writeBytes("/Length "+grdocument.getImageSizeStream(i)+"\n");
						rPdf.writeBytes("/Filter/DCTDecode\n");
						bufferImage = grdocument.getImageStream(i);
						break;
						
					case GRImageProperty.TYPE_PNG:
						bufferImage = GRFlateDecode.encode(grdocument.getImageStream(i));
						rPdf.writeBytes("/Length "+bufferImage.length+"\n");
						rPdf.writeBytes("/Filter/FlateDecode\n");
						rPdf.writeBytes("/SMask "+(numberObj)+" 0 R\n");
						
					case GRImageProperty.TYPE_BMP:
						bufferImage =  GRFlateDecode.encode(grdocument.getImageStream(i));
						rPdf.writeBytes("/Length "+bufferImage.length+"\n");
						rPdf.writeBytes("/Filter/FlateDecode\n");
						
				}
			
				rPdf.writeBytes("/Interpolate true\n");
				rPdf.writeBytes(">>\n");
				rPdf.writeBytes("stream\n");
				
				rPdf.write(bufferImage);
				//rPdf.write(grdocument.getImageStream(i));
				
				rPdf.writeBytes("\n");
				rPdf.writeBytes("endstream\n");
				rPdf.writeBytes("endobj\n");
				
				// SMASK SE RICHIESTO
				if(grdocument.getImageType(i) == GRImageProperty.TYPE_PNG) {
					grxref.addAddress(numberObj, rPdf.getFilePointer());
					rPdf.writeBytes(numberObj+" 0 obj\n");
					numberObj++;
					
					byte[] bufferMask =  GRFlateDecode.encode(grdocument.getImageMask(i));
					
					rPdf.writeBytes("<< /Type /XObject\n");
					rPdf.writeBytes("/Subtype /Image\n");
					rPdf.writeBytes("/Width "+grdocument.getImageOriginalWidth(i)+"\n");
					rPdf.writeBytes("/Height "+grdocument.getImageOriginalHeight(i)+"\n");
					rPdf.writeBytes("/ColorSpace /DeviceGray\n");
					//rPdf.writeBytes("/Matte[0 0 0]\n");	???? SERVE????
					rPdf.writeBytes("/BitsPerComponent 8\n");
					rPdf.writeBytes("/Length "+bufferMask.length+"\n");
					rPdf.writeBytes("/Filter/FlateDecode\n");
					rPdf.writeBytes("/Interpolate false\n");
					rPdf.writeBytes(">>\n");
					rPdf.writeBytes("stream\n");
					rPdf.write(bufferMask);
					rPdf.writeBytes("\n");
					rPdf.writeBytes("endstream\n");
					rPdf.writeBytes("endobj\n");
				}
			}
			
			// Inserisce i font
			for(int i = 0;i < grdocument.getTotaleFont();i++) {
				// FONT FILE2 - STREAM EMBEDDED
				grxref.addAddress(numberObj, rPdf.getFilePointer());
				rPdf.writeBytes(numberObj+" 0 obj\n");
				numberObj++;
				
				rPdf.writeBytes("<<\n");
				rPdf.writeBytes("/Length "+grdocument.getFontLenCompressedStream(i)+"\n");
				rPdf.writeBytes("/Length1 "+grdocument.getFontLenOriginalStream(i)+"\n");
				rPdf.writeBytes("/Filter /FlateDecode\n");
				rPdf.writeBytes(">>\n");
				rPdf.writeBytes("stream\n");
				rPdf.write(grdocument.getFontStream(i));
				rPdf.writeBytes("\n");
				rPdf.writeBytes("endstream\n");
				rPdf.writeBytes("endobj\n");
				
				// FONT DESCRIPTOR
				grxref.addAddress(numberObj, rPdf.getFilePointer());
				rPdf.writeBytes(numberObj+" 0 obj\n");
				numberObj++;
				
				rPdf.writeBytes("<</Type/FontDescriptor\n");
				rPdf.writeBytes("/FontName /"+grdocument.getFontName(i)+"\n");
				rPdf.writeBytes("/FontBBox "+grdocument.getFontBBox(i)+"\n");
				rPdf.writeBytes("/Flags "+grdocument.getFontFlags(i)+"\n");
				rPdf.writeBytes("/CapHeight "+grdocument.getFontCapHeight(i)+"\n");
				rPdf.writeBytes("/Ascent "+grdocument.getFontAscent(i)+"\n");
				rPdf.writeBytes("/Descent "+grdocument.getFontDescent(i)+"\n");
				rPdf.writeBytes("/ItalicAngle "+grdocument.getFontItalicAngle(i)+"\n");
				rPdf.writeBytes("/StemV "+grdocument.getFontStemV(i)+"\n");
				rPdf.writeBytes("/FontFile2 "+(numberObj-2)+" 0 R\n");
				rPdf.writeBytes(">>\n");
				rPdf.writeBytes("endobj\n");
				
				// FONT
				grxref.addAddress(numberObj, rPdf.getFilePointer());
				rPdf.writeBytes(numberObj+" 0 obj\n");
				addressFonts = addressFonts + "/"+grdocument.getFontId(i)+" "+numberObj+" 0 R ";
				numberObj++;
				
				rPdf.writeBytes("<</Type/Font\n");
				rPdf.writeBytes("/Subtype/"+grdocument.getFontType(i)+"\n");
				rPdf.writeBytes("/Name/"+grdocument.getFontId(i)+"\n");
				rPdf.writeBytes("/BaseFont/"+grdocument.getBaseFont(i)+"\n");
				rPdf.writeBytes("/Encoding/"+grdocument.getFontEncoding(i)+"\n");
				rPdf.writeBytes("/FontDescriptor "+(numberObj-2)+" 0 R\n");
				rPdf.writeBytes("/FirstChar "+grdocument.getFontFirstChar(i)+"\n");
				rPdf.writeBytes("/LastChar "+grdocument.getFontLastChar(i)+"\n");
				rPdf.writeBytes("/Widths "+grdocument.getFontWidths(i)+"\n");
				rPdf.writeBytes(">>\n");
				rPdf.writeBytes("endobj\n");
				
			}
			
			// Aggiunta del font di sistema
			boolean isSystemFont = false;
			if(isSystemFont) {
				// FONT
				grxref.addAddress(numberObj, rPdf.getFilePointer());
				rPdf.writeBytes(numberObj+" 0 obj\n");
				addressFonts = addressFonts + "/GRFSYS1 "+numberObj+" 0 R ";
				numberObj++;
				
				rPdf.writeBytes("<</Type/Font\n");
				rPdf.writeBytes("/Subtype /Type1\n");
				rPdf.writeBytes("/Name /GRFSYS1\n");
				rPdf.writeBytes("/BaseFont /Helvetica\n");
				rPdf.writeBytes("/Encoding /WinAnsiEncoding\n");
				rPdf.writeBytes(">>\n");
				rPdf.writeBytes("endobj\n");
			}
			
			// Page
			// Cicla per ogni pagina presente nel documento
			int totalePaginePdf = 0;
			int paginaCorrente = 1;
			
			// Crea un vector contenente TUTTI gli stream di ogni singola pagina
			// del PDF finale che si andrà a creare.
			// Questa gestione viene eseguita per avere previa conoscenza sul
			// numero di pagine e informazioni del PDF. Tali info verranno utilizzate
			// nel caso in cui ci siano variabili di documento.
			Vector<GRPageStream> contentStream = new Vector<GRPageStream>();
			
			for(int i = 0;i < grdocument.getNumberPages();i++) {
				GRPageStream page = new GRPageStream(grdocument.getPageWidth(i),grdocument.getPageHeight(i));
				Vector<String> streamDocument = grdocument.getPageContent(i,grdata);
				
				for(int v = 0;v < streamDocument.size();v++) {
					page.addContent(streamDocument.get(v));
					
					totalePaginePdf++;
				}
				
				//page.setGRSystemObject(grdocument.getSystemObject(i));
				page.setGRSystemObject(grdocument.getSystemObject(i), grdocument.getPageWidth(i),grdocument.getPageHeight(i));
				
				contentStream.add(page);
				
			}
			grglobal.setPagineTotale(totalePaginePdf);
			
			// Adesso cicla per tutte le pagine acquisite e le inserisce nel documento
			for(int i = 0;i < contentStream.size();i++) {
				GRPageStream page = contentStream.get(i);
				for(int v = 0;v < page.getPage();v++) {
					grglobal.increasesCurrentPage();
					
					// Prima inserisce i contenuti e poi /Type/Page
					// Content stream
					grxref.addAddress(numberObj, rPdf.getFilePointer());
					rPdf.writeBytes(numberObj+" 0 obj\n");
					int pointerContentStream = numberObj;
					numberObj++;
				
					String streamPage = page.getPageContent(v);
					
					if(streamPage.length() == 0)	{
						// Se lo stream è zero produce un contenuto nullo
						rPdf.writeBytes("<</Length 0>>\n");
						rPdf.writeBytes("stream\n");
						rPdf.writeBytes("endstream\n");
						rPdf.writeBytes("endobj\n");
					} else {
						/* FLATEDECODE */
						
						/* Gestione degli oggetti di sistema */
						Vector<GRSystemObject> grsys = page.getGRSystemObjec();
						if(grsys != null) {
							for(int gi = 0;gi < grsys.size();gi++) {
								streamPage = streamPage + grsys.get(gi).draw(grglobal);
								
							}
						}
							
						//streamPage = streamPage + "BT\n510.0 75.0 Td\n/f1 7.0 Tf\n0.0 0.0 0.0 rg\n[(Pagina "+paginaCorrente+" di "+totalePaginePdf+")] TJ\nET\n";
						//streamPage = streamPage + "1 0 0 1 28.0 0 Tm\n";
						//System.out.println("STREAM: "+streamPage);
						byte[] bStream =  GRFlateDecode.encode(streamPage.getBytes());
						
						//byte[] bStream =  GRFlateDecode.encode(contentStream.get(v).getBytes());
						rPdf.writeBytes("<</Filter/FlateDecode /Length "+bStream.length+">>\n");
						rPdf.writeBytes("stream\n");
						rPdf.write(bStream,0,bStream.length);
						rPdf.writeBytes("\n");
											
						rPdf.writeBytes("endstream\n");
						rPdf.writeBytes("endobj\n");
						
					}
				
					// /Type/Page
					grxref.addAddress(numberObj, rPdf.getFilePointer());
					rPdf.writeBytes(numberObj+" 0 obj\n");
					addressKids = addressKids + numberObj+" 0 R ";
					numberObj++;
								
					rPdf.writeBytes("<</Type/Page\n");
					rPdf.writeBytes("/Parent 2 0 R\n");	// L'indirizzo 2 è fisso
					rPdf.writeBytes("/MediaBox[0 0 "+page.getPageWidth()+" "+page.getPageHeight()+"]\n");
					//rPdf.writeBytes("/TrimBox[0 0 195.27 241.88]\n");
					//rPdf.writeBytes("/CropBox[0 0 195.27 241.88]\n");
					
					rPdf.writeBytes("/Contents "+pointerContentStream+" 0 R\n");
					rPdf.writeBytes("/Resources <</Font <<"+addressFonts+">> ");
					if(grdocument.getTotaleImage() > 0)
						rPdf.writeBytes("/XObject <<"+addressImages+">>");
					rPdf.writeBytes(" >>\n");
					rPdf.writeBytes(">>\n");
					rPdf.writeBytes("endobj\n");
					
					//totalePaginePdf++;
					paginaCorrente++;
				}
			}
			
			// Catalog
			grxref.addAddress(1,rPdf.getFilePointer());
			rPdf.writeBytes("1 0 obj\n");	// Indirizzo fisso
			
			rPdf.writeBytes("<</Type/Catalog\n");
			rPdf.writeBytes("/Pages 2 0 R\n");	// Indirizzo fisso
			rPdf.writeBytes(">>\n");
			rPdf.writeBytes("endobj\n");
						
			// Pages
			grxref.addAddress(2, rPdf.getFilePointer());
			rPdf.writeBytes("2 0 obj\n");	// Indirizzo fisso
									
			rPdf.writeBytes("<</Type/Pages\n");
			rPdf.writeBytes("/Count "+totalePaginePdf+"\n");
			rPdf.writeBytes("/Kids ["+addressKids+"]\n");
			rPdf.writeBytes(">>\n");
			rPdf.writeBytes("endobj\n");
							
			// INFO
			grxref.addAddress(numberObj, rPdf.getFilePointer());
			rPdf.writeBytes(numberObj+" 0 obj\n");
			numberObj++;
						
			rPdf.writeBytes("<<\n");
			
			if(GR_TITLE != null)
				rPdf.writeBytes("/Title("+GR_TITLE+")\n");
			if(GR_AUTHOR != null)
				rPdf.writeBytes("/Author("+GR_AUTHOR+")\n");
			rPdf.writeBytes("/Producer("+GR_PRODUCER+" - v"+GR_VERSION_MAJOR+"."+GR_VERSION_MINOR+")\n");
			if(GR_CREATOR != null)
				rPdf.writeBytes("/Creator("+GR_CREATOR+")\n");
			
			// Impostazione della data di creazione del documento
			Calendar calendar = new GregorianCalendar();
			StringBuffer dataCreazione = new StringBuffer();
			dataCreazione.append("D:");
			dataCreazione.append(calendar.get(Calendar.YEAR));
			if((calendar.get(Calendar.MONTH)+1) < 10)
				dataCreazione.append("0");
			dataCreazione.append((calendar.get(Calendar.MONTH)+1));
			if(calendar.get(Calendar.DAY_OF_MONTH) < 10)
				dataCreazione.append("0");
			dataCreazione.append(calendar.get(Calendar.DAY_OF_MONTH));
			if(calendar.get(Calendar.HOUR_OF_DAY) < 10)
				dataCreazione.append("0");
			dataCreazione.append(calendar.get(Calendar.HOUR_OF_DAY));
			if(calendar.get(Calendar.MINUTE) < 10)
				dataCreazione.append("0");
			dataCreazione.append(calendar.get(Calendar.MINUTE));
			if(calendar.get(Calendar.SECOND) < 10)
				dataCreazione.append("0");
			dataCreazione.append(calendar.get(Calendar.SECOND));
			dataCreazione.append("+02'00'");
			rPdf.writeBytes("/CreationDate("+dataCreazione.toString()+")\n");
			
			rPdf.writeBytes(">>\n");
			
			rPdf.writeBytes("endobj\n");
						
			// XREF
			startXRef = rPdf.getFilePointer();
			rPdf.writeBytes("xref\n");
						
			rPdf.writeBytes("0 "+numberObj+"\n");
			rPdf.writeBytes("0000000000 65535 f \n");
			for(int i = 0;i < grxref.getTotalReference();i++)
				rPdf.writeBytes(grxref.getAddress()+" 00000 n \n");
						
			rPdf.writeBytes("trailer <</Size "+numberObj+" /Root 1 0 R /Info "+(numberObj-1)+" 0 R >>\n");
			rPdf.writeBytes("startxref\n");
			rPdf.writeBytes(""+startXRef+"\n");
			
			// END PDF
			rPdf.writeBytes("%%EOF");
			rPdf.close();
							
			return totalePaginePdf;
			
		} catch(FileNotFoundException fnfe) {
			throw new GRPdfPathNotFoundException(namePDF);
		} catch(IOException ioe) {
			this.clear(rPdf,namePDF);
			
			throw new GRPdfIOWriteException(namePDF);
		} catch(GRValidateException grve) {
			this.clear(rPdf,namePDF);
			
			throw grve;
		} 
	}
	/**
	 * Genera un file PDF nel percorso specificato.
	 * 
	 * @param namePDF
	 * @return Il numero di pagine di cui è formato il PDF generato. Nel caso di zero o numero negativo, significa che si è generato un errore durante la creazione del file.
	 * @throws GRPdfPathNotFoundException
	 * @throws GRPdfIOWriteException
	 * @throws GRValidateException
	 */
	public synchronized int writePDF(String namePDF) throws GRException {
		
		grdata = null;
		return this.generaPDF(namePDF);
	}
	/** Genera un file PDF nel percorso specificato.
	 * 
	 * @param namePDF Il nome del file PDF da generare comprensivo del percorso ove generarlo
	 * @param allega Se true, aggiunge in coda al PDF generato i files memorizzati come allegati. 
	 * @return Il numero di pagine di cui è formato il PDF generato. Nel caso di zero o numero negativo, significa che si è generato un errore durante la creazione del file.
	 * @throws GRPdfPathNotFoundException
	 * @throws GRPdfIOWriteException
	 * @throws GRValidateException
	 */
	public synchronized int writePDF(String namePDF, boolean allega) throws GRException {
		
		grdata = null;
		int totalePagine = generaPDF(namePDF);
		
		if(allega) {
			int pagineFinali = allega(namePDF);
			
			if(pagineFinali != -1)
				totalePagine = pagineFinali;
		}
		
		return totalePagine;
	}
	/** Genera un file PDF nel percorso specificato.
	 * 
	 * @param namePDF Il nome del file PDF da generare comprensivo del percorso ove generarlo
	 * @param nameXML Una stringa contenente un xml nel formato <i>GlobalReports xml</i>. E' l'xml che contiene le variabili da mappare a run time in fase di generazione del PDF.
	 * @return Il numero di pagine di cui è formato il PDF generato. Nel caso di zero o numero negativo, significa che si �� generato un errore durante la creazione del file.
	 * @throws GRPdfPathNotFoundException
	 * @throws GRPdfIOWriteException
	 * @throws GRValidateException
	 * 
	 */
	public synchronized int writePDF(String namePDF, String nameXML) throws GRException {
		if(!nameXML.equals("")) {
			grdata = new GRData(nameXML);
		}
		
		return this.generaPDF(namePDF);
		//return this.writePDF(namePDF);
		
	}
	/** Genera un file PDF nel percorso specificato.
	 * 
	 * @param namePDF Il nome del file PDF da generare comprensivo del percorso ove generarlo
	 * @param nameXML Una stringa contenente un xml nel formato <i>GlobalReports xml</i>. E' l'xml che contiene le variabili da mappare a run time in fase di generazione del PDF.
	 * @param allega Se true, aggiunge in coda al PDF generato i files memorizzati come allegati. 
	 * @return Il numero di pagine di cui è formato il PDF generato. Nel caso di zero o numero negativo, significa che si �� generato un errore durante la creazione del file.
	 * @throws GRPdfPathNotFoundException
	 * @throws GRPdfIOWriteException
	 * @throws GRValidateException
	 * 
	 */
	public synchronized int writePDF(String namePDF, String nameXML, boolean allega) throws GRException {
		if(!nameXML.equals("")) {
			grdata = new GRData(nameXML);
		}
		
		int totalePagine = generaPDF(namePDF);
		
		if(allega) {
			int pagineFinali = allega(namePDF);
			
			if(pagineFinali != -1)
				totalePagine = pagineFinali;
		}
		
		// Gestione degli allegati da xml
		if(grdata != null) {
			Vector<String> attachedList = grdata.getAttachedList();
			if(attachedList != null) {
				for(int i = 0;i < attachedList.size();i++) {
					try {
						appendPDFXml(attachedList.get(i));
					} catch(Exception e) {
						
					}	// Se non trova il file prosegue
				}
			}
			
			int pagineFinali2 = allegaDaXml(namePDF);
			if(pagineFinali2 != -1)
				totalePagine = pagineFinali2;
			
			pdfAttachXml = null;
		}
		
		return totalePagine;
		
		//return this.writePDF(namePDF, allega);
	
	}
	/** Genera un file PDF nel percorso specificato.
	 * 
	 * @param namePDF Il nome del file PDF da generare comprensivo del percorso ove generarlo
	 * @param nameXML Un oggetto di tipo File contenente un xml nel formato <i>GlobalReports xml</i>. E' l'xml che contiene le variabili da mappare a run time in fase di generazione del PDF.
	 * @return Il numero di pagine di cui è formato il PDF generato. Nel caso di zero o numero negativo, significa che si ��� generato un errore durante la creazione del file.
	 * @throws GRPdfPathNotFoundException
	 * @throws GRPdfIOWriteException
	 * @throws GRValidateException
	 */
	public synchronized int writePDF(String namePDF, File nameXML) throws GRException {
		
		if(!nameXML.equals("")) {
			grdata = new GRData(nameXML);
		}
		
		return this.generaPDF(namePDF);
		//return this.writePDF(namePDF);
	
	}
	/** Genera un file PDF nel percorso specificato.
	 * 
	 * @param namePDF Il nome del file PDF da generare comprensivo del percorso ove generarlo
	 * @param nameXML Un oggetto di tipo File contenente un xml nel formato <i>GlobalReports xml</i>. E' l'xml che contiene le variabili da mappare a run time in fase di generazione del PDF.
	 * @param allega Se true, aggiunge in coda al PDF generato i files memorizzati come allegati. 
	 * @return Il numero di pagine di cui è formato il PDF generato. Nel caso di zero o numero negativo, significa che si ��� generato un errore durante la creazione del file.
	 * @throws GRPdfPathNotFoundException
	 * @throws GRPdfIOWriteException
	 * @throws GRValidateException
	 */
	public synchronized int writePDF(String namePDF, File nameXML, boolean allega) throws GRException {
		
		if(!nameXML.equals("")) {
			grdata = new GRData(nameXML);
		}
		
		int totalePagine = generaPDF(namePDF);
		
		// Gestione degli allegati da codice
		if(allega) {
			int pagineFinali = allega(namePDF);
			
			if(pagineFinali != -1)
				totalePagine = pagineFinali;
		}
		
		// Gestione degli allegati da xml
		if(grdata != null) {
			Vector<String> attachedList = grdata.getAttachedList();
			if(attachedList != null) {
				for(int i = 0;i < attachedList.size();i++) {
					try {
						appendPDFXml(attachedList.get(i));
					} catch(Exception e) {}	// Se non trova il file prosegue
				}
			}
			
			int pagineFinali2 = allegaDaXml(namePDF);
			if(pagineFinali2 != -1)
				totalePagine = pagineFinali2;
			
			pdfAttachXml = null;
		}
		
		return totalePagine;
		
		//return this.writePDF(namePDF, allega);
	
	}
	/** Genera un file GRB partendo da un file GRX di cui il percorso viene passato come membro del metodo.
	 * 
	 * @param nameFileGR Il nome del file GRX sorgente da cui dovrà essere ricavato il file GRB binario
	 * @return Il nome del file GRB compilato, comprensivo di estensione
	 * @throws GRCompileException
	 */
	public synchronized String compile(String nameFileGR) throws GRCompileException {
		return this.compile(nameFileGR, null);
		
	}
	/** Genera un file GRB partendo da un file GRX di cui il percorso viene passato come membro del metodo.
	 *  Questa viene usata quando si desidera ottenere un file compilato con un nome predefinito. Se viene utilizzato questo metodo
	 *  il valore all'interno del tag <namedocument> del file grx viene ignorato.
	 *  
	 * @param nameFileGR Il nome del file GRX sorgente da cui dovrà essere ricavato il file GRB binario
	 * @param nameFileGRB Il nome del file GRB compilato da utilizzare come layout
	 * @return Il nome del file GRB compilato, comprensivo di estensione
	 * @throws GRCompileException
	 */
	public synchronized String compile(String nameFileGR, String nameFileGRB) throws GRCompileException {
		GRCompiler compiler = new GRCompiler(nameFileGR);
		
		if(nameFileGRB == null)
			compiler.compile();
		else
			compiler.compile(nameFileGRB);
		
		String nameGrb = compiler.writeGRB();
		
		return nameGrb;
	}
	private void clear(RandomAccessFile raf, String namePDF) {
		if(raf == null)
			return;
			
		try {
			raf.close();	// Chiude il canale
		} catch(IOException io) {
		
		} finally {
			File f = new File(namePDF);
			f.delete();
		}
	}
	
	public static void main(String[] args) {
		String fileSource = null;
		String fileOutput = null;
		String fileXml = null;
		
		boolean cmd_view = false;
		boolean cmd_version = false;
		
		if(args.length == 0) {
			System.out.println("Use: <source file (grx | grs)> [destination file pdf] [data file xml] [param]");
			System.out.println("Please read the -help documentation!");
			System.exit(0);
		}
		
		for(int i = 0;i < args.length;i++) {
			if(args[i].codePointAt(0) == 45) {
				/* Comando */
				if(args[i].equals("-view") || args[i].equals("-w"))
					cmd_view = true;
				else if(args[i].equals("-help") || args[i].equals("-h")) 
					viewHelp();
				else if(args[i].equals("-version") || args[i].equals("-v"))
					viewVersion();
				else {
					/* Parametro di troppo. Errore */
					System.out.println("Warning! The param "+args[i]+" is not recognized! Please read the -help documentation!");
					System.exit(0);
				}
			} else {
				if(fileSource == null)
					fileSource = args[i];
				else if(fileOutput == null)
					fileOutput = args[i];
				else if(fileXml == null)
					fileXml = args[i];
				else {
					/* Parametro di troppo. Errore */
					System.out.println("Warning! The param "+args[i]+" is not recognized! Please read the -help documentation!");
					System.exit(0);
				}
			}
		}
		
		GRPDF grpdf = new GRPDF();
		
		if(fileOutput == null) {
			/* Il file pdf prenderà il nome del file sorgente */
			fileOutput = fileSource.substring(0,fileSource.length() - 4) + ".pdf";
		}
		
		try {
			grpdf.setLayout(fileSource);
			
			if(fileXml == null)
				grpdf.writePDF(fileOutput);
			else
				grpdf.writePDF(fileOutput, new File(fileXml), true);
			
			if(cmd_view) {
				try {
					java.awt.Desktop.getDesktop().open(new File(fileOutput));
				} catch(Exception e) {
					System.out.println(e.getMessage());
				}
			}
		} catch(GRException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void viewHelp() {
		System.out.println("Parameters:");
		System.out.println("\t-grb\t\tGenerate GRB file from source file");
		System.out.println("\t-grs\t\tGenerate GRS file from source file");
		
		System.out.println("\t-help    | -h\tView the help documentation");
		System.out.println("\t-version | -v\tPrint library version");
		System.out.println("\t-view    | -w\tLaunch the pdf after generation");
		
		System.exit(0);
	}
	private static void viewVersion() {
		System.out.println(GRPDF.GR_PRODUCER);
		System.out.println("Version: "+GRPDF.GR_VERSION_MAJOR+"."+GRPDF.GR_VERSION_MINOR);
	
		System.exit(0);
	}
}
