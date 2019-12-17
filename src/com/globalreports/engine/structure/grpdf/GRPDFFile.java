/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.grpdf.GRPDFContentLibrary
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
package com.globalreports.engine.structure.grpdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import com.globalreports.engine.GRPDF;
import com.globalreports.engine.err.GRAttachmentException;
import com.globalreports.engine.err.GRAttachmentFileNotFoundException;
import com.globalreports.engine.err.GRAttachmentIOReadException;
import com.globalreports.engine.err.GRAttachmentPageNotExists;
import com.globalreports.engine.err.GRCompileException;
import com.globalreports.engine.err.GRException;
import com.globalreports.engine.err.GRLayoutException;
import com.globalreports.engine.err.GRPdfIOWriteException;
import com.globalreports.engine.err.GRPdfPathNotFoundException;
import com.globalreports.engine.objects.GRRectangle;
import com.globalreports.engine.objects.GRShape;
import com.globalreports.engine.structure.GRMeasures;

public class GRPDFFile {
	public static final int ALLPAGES	= -1;
	
	private Vector<GRPDFReader> pdfReader;
	private GRPDFReader grfile;
	
	public GRPDFFile() {
		pdfReader = new Vector<GRPDFReader>();
	}
	public GRPDFFile(String namePDF) throws GRAttachmentException {
	
		grfile = new GRPDFReader(namePDF);
		
		pdfReader = new Vector<GRPDFReader>();
		
	}
	public void appendGRPDFReader(GRPDFReader pdf) {
		pdfReader.add(pdf);
	}
	public void appendPDF(String namePDF) throws GRAttachmentException {
		pdfReader.add(new GRPDFReader(namePDF));
	}
	
	public void appendJPG(String nameJPG, double left, double top, double width, double height) throws GRException {
		String nameFileTemp = this.createGRX(nameJPG, left, top, width, height);
	
		pdfReader.add(new GRPDFReader(nameFileTemp));
		
		// Dopo aver aggiunto il pdf temporaneo in memoria, lo cancella
		File k = new File(nameFileTemp);
		k.delete();
	}
	public void appendJPG(String nameJPG) throws GRException {
		String nameFileTemp = this.createGRX(nameJPG);
		
		pdfReader.add(new GRPDFReader(nameFileTemp));
		
		File k = new File(nameFileTemp);
		k.delete();
	}
	public int getNumFiles() {
		return pdfReader.size();
	}
	public GRPDFReader getGRPDFReader(int numberFile) {
		return pdfReader.get(numberFile);
	}
	public synchronized int mergePDF(String namePDF) throws GRPdfPathNotFoundException, GRPdfIOWriteException {
		GRXrefTable xref;
		int totPages = 0;
		int totObjects = 3;
		int gapObj = 0;
		String kids = "";
		RandomAccessFile rPdf = null;
		
		if(pdfReader == null || pdfReader.size() == 0)
			return 0;
		
		try {
			// Se il file esiste lo cancella
			File f = new File(namePDF);
			f.delete();
			
			xref = new GRXrefTable();
			
			rPdf = new RandomAccessFile(namePDF,"rw");
			// START PDF
			rPdf.writeBytes("%PDF-1.4\n");
				
			// Cicla per tutte le pagine di ogni files allegato
			// Di ogni pagina inserisce:
			// 1. /Type/Page
			// 2. Le /Resources contenute nella pagina
			// 3. Il /Contents
			for(int i = 0;i < pdfReader.size();i++) {
				
				GRPDFReader attach = pdfReader.get(i);
				attach.newDocument(xref);
				totPages = totPages + attach.getNumPages();
				
				totObjects = totObjects + attach.write(rPdf, xref, totObjects);
				
				kids = kids + attach.getKids();
				
				// Aggiorna il gap
				gapObj = gapObj + totObjects;
			}
			
			totObjects = totObjects - 3;
			
			// Catalog
			xref.addAddress(1,rPdf.getFilePointer());
			rPdf.writeBytes("1 0 obj\n");	// Indirizzo fisso
			rPdf.writeBytes("<</Type/Catalog\n");
			rPdf.writeBytes("/Pages 2 0 R\n");	// Indirizzo fisso
			rPdf.writeBytes(">>\n");
			rPdf.writeBytes("endobj\n");
			totObjects++;
			
			// Pages
			xref.addAddress(2,rPdf.getFilePointer());
			rPdf.writeBytes("2 0 obj\n");	// Indirizzo fisso	
			rPdf.writeBytes("<</Type/Pages\n");
			rPdf.writeBytes("/Count "+totPages+"\n");
			rPdf.writeBytes("/Kids ["+kids+"]\n");
			rPdf.writeBytes(">>\n");
			rPdf.writeBytes("endobj\n");
			totObjects++;
			
			// INFO
			xref.addAddress(totObjects+1, rPdf.getFilePointer());
			rPdf.writeBytes((totObjects+1)+" 0 obj\n");
						
			rPdf.writeBytes("<<\n");
			
			rPdf.writeBytes("/Producer("+GRPDF.GR_PRODUCER+" - v"+GRPDF.GR_VERSION_MAJOR+"."+GRPDF.GR_VERSION_MINOR+")\n");
			if(GRPDF.GR_CREATOR != null)
				rPdf.writeBytes("/Creator("+GRPDF.GR_CREATOR+")\n");
			
			
			rPdf.writeBytes(">>\n");
			
			rPdf.writeBytes("endobj\n");
			totObjects++;
			
			// XREF
			long pointerxref = rPdf.getFilePointer();
			rPdf.writeBytes("xref\n");
			rPdf.writeBytes("0 "+(totObjects+1)+"\n");
			
			rPdf.writeBytes("0000000000 65535 f \n");
			
			for(int i = 0;i < xref.getTotalReference();i++)
				rPdf.writeBytes(xref.getAddress()+" 00000 n \n");
			
			
			rPdf.writeBytes("trailer <</Size "+(totObjects+1)+" /Root 1 0 R /Info "+totObjects+" 0 R >>\n");
			rPdf.writeBytes("startxref\n");
			rPdf.writeBytes(""+pointerxref+"\n");
			
			// END PDF
			rPdf.writeBytes("%%EOF");
			rPdf.close();
		} catch(FileNotFoundException fnfe) {
			throw new GRPdfPathNotFoundException(namePDF);
		} catch(IOException ioe) {
			//this.clear(rPdf,namePDF);
			
			throw new GRPdfIOWriteException(namePDF);
		} catch(GRAttachmentException ae) {
			System.out.println("GRATTACHMENTEXCEPTION: "+ae.getMessage());
		} 
		
		return totPages;
	}
	
	// Test - Crea un PDF da un'immagine
	private String createGRX(String pathJPG) throws GRException {
		return this.createGRX(pathJPG,0.0,0.0,210.0,297.0);
	}
	private String createGRX(String pathJPG, double left, double top, double width, double height) throws GRException {
		RandomAccessFile raf;
		
		String nameFileTemp = null;
		
		// Recupera le informazioni riguardo l'immagine.
		// Questo serve anche a sapere se l'immagine che si tenta di allegare esiste oppure no
		File img = new File(pathJPG);
		//System.out.println("ABS PAT: "+img.getAbsolutePath());
		//System.out.println("ABS PAT: "+img.getName());
		//System.out.println("ABS PAT: "+img.getPath());
		//System.out.println("ABS PAT: "+img.getParent());
		
		if(!img.exists())
			throw new GRAttachmentFileNotFoundException(img.getName());
		
		nameFileTemp = img.getName()+"_"+System.currentTimeMillis();
		// Crea il GRX che conterrà l'immagine
		
		try {
			raf = new RandomAccessFile(img.getParent()+"//"+nameFileTemp+".grx","rw");
		
			raf.writeBytes("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			raf.writeBytes("<globalreports edit=\"GlobalReports Editor\" version=\"0.7\">\n");
			raf.writeBytes("<grinfo>\n");
			raf.writeBytes("<namedocument>"+nameFileTemp+"</namedocument>\n");
			raf.writeBytes("<pathfont>C:\\Windows\\Fonts\\</pathfont>\n");
			raf.writeBytes("</grinfo>\n");
			
			raf.writeBytes("<page>\n");
			raf.writeBytes("<typography>MM</typography>\n");
			raf.writeBytes("<pagewidth>210</pagewidth>\n");
			raf.writeBytes("<pageheight>297</pageheight>\n");
			raf.writeBytes("<grbody>\n");
			
			raf.writeBytes("<image>\n");
			raf.writeBytes("<path>"+img.getParent()+"//"+img.getName()+"</path>\n");
			raf.writeBytes("<left>"+left+"</left>\n");
			raf.writeBytes("<top>"+top+"</top>\n");
			raf.writeBytes("<width>"+width+"</width>\n");
			raf.writeBytes("<height>"+height+"</height>\n");
			raf.writeBytes("<hposition>absolute</hposition>\n");
			raf.writeBytes("</image>\n");
			
			raf.writeBytes("</grbody>\n");
			raf.writeBytes("</page>\n");
			raf.writeBytes("</globalreports>\n");
			
			raf.close();
		} catch (FileNotFoundException e) {
			throw new GRAttachmentFileNotFoundException(img.getName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new GRAttachmentIOReadException(img.getName());
		}
		
		// Adesso crea il pdf dal GRX
		GRPDF pdf = new GRPDF();
		//pdf.compile(img.getParent()+"//"+nameFileTemp+".grx");
		pdf.setLayout(img.getParent()+"//"+nameFileTemp+".grx");
		pdf.writePDF(img.getParent()+"//"+nameFileTemp+".pdf");
		
		// Cancella tutti i file temporanei creati
		File k;
		k = new File(img.getParent()+"//"+nameFileTemp+".grx");
		k.delete();
		k = new File(img.getParent()+"//"+nameFileTemp+".grb");
		k.delete();
		return img.getParent()+"//"+nameFileTemp+".pdf";
	}
	
	public void drawGRRectangle(double left, double top, double width, double height, double widthStroke) {
		GRShape shape = new GRRectangle();
		
		shape.setPosition(GRMeasures.fromMillimetersToPostScript(left), GRMeasures.fromMillimetersToPostScript(top));
		shape.setDimension(GRMeasures.fromMillimetersToPostScript(width), GRMeasures.fromMillimetersToPostScript(height));
		
		try {
			GRPDFPage grpage = pdfReader.get(0).getPage(0);
			
		} catch (GRAttachmentPageNotExists e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * Restituisce il numero di pagine di cui è composto il file PDF acquisito
	 * 
	 * @return Il numero di pagine totali del PDF letto.
	 * @throws GRAttachmentException Se la lettura del file presenta dei problemi.
	 */
	public int getNumPages() {
		if(grfile == null)
			return 0;
		
		
		return grfile.getNumPages();
		
	}
	
	/**
	 * Crea un nuovo file PDF ottenuto dall'estrapolazione del numero di pagine specificato dal PDF acquisito.
	 * 
	 * @param namePDF Il nome da assegnare al PDF che si sta creando
	 * @param startPage Il numero di pagina iniziale da cui iniziare l'estrapolazione
	 * @param endPage Il numero di pagina finale relativa all'estrapolazione
	 * 
	 * @return Il numero di pagine di cui è formato il PDF creato. Questo potrebbe differire nel caso in cui la pagina finale specificata nel range sia superiore al numero di pagine totali presenti nel PDF acquisito.
	 * 
	 * @throws GRPdfPathNotFoundException
	 * @throws GRPdfIOWriteException
	 * @throws GRAttachmentException
	 */
	public int createPDF(String namePDF, int startPage, int endPage) throws GRPdfPathNotFoundException, GRPdfIOWriteException, GRAttachmentException {
		GRXrefTable xref;
		int totPages = 0;
		int totObjects = 3;
		int gapObj = 0;
		String kids = "";
		RandomAccessFile rPdf = null;
		
		try {
			// Se il file esiste lo cancella
			File f = new File(namePDF);
			f.delete();
			
			xref = new GRXrefTable();
			
			rPdf = new RandomAccessFile(namePDF,"rw");
			// START PDF
			rPdf.writeBytes("%PDF-1.4\n");
			
			grfile = pdfReader.get(0);
			grfile.newDocument(xref);
			
			xref.addAddress(3, rPdf.getFilePointer());
			rPdf.writeBytes("3 0 obj\n");
			rPdf.writeBytes("<</Type/Font /Subtype/Type1 /Name/GRF1 /BaseFont/Helvetica >>\n");
			rPdf.writeBytes("endobj\n");
			
			totObjects++;
			
			for(int i = startPage;i <= endPage;i++) {
				totObjects = totObjects + grfile.writePage(rPdf, (i-1), totObjects);
				
				totPages++;
			}
			kids = grfile.getKids();
			totObjects = totObjects - 3;
			
			// Catalog
			long addressCatalog = rPdf.getFilePointer();
			xref.addAddress(1,rPdf.getFilePointer());
			rPdf.writeBytes("1 0 obj\n");	// Indirizzo fisso
			rPdf.writeBytes("<</Type/Catalog\n");
			rPdf.writeBytes("/Pages 2 0 R\n");	// Indirizzo fisso
			rPdf.writeBytes(">>\n");
			rPdf.writeBytes("endobj\n");
			totObjects++;
			
			// Pages
			long addressPages = rPdf.getFilePointer();
			xref.addAddress(2,rPdf.getFilePointer());
			rPdf.writeBytes("2 0 obj\n");	// Indirizzo fisso	
			rPdf.writeBytes("<</Type/Pages\n");
			rPdf.writeBytes("/Count "+totPages+"\n");
			rPdf.writeBytes("/Kids ["+kids+"]\n");
			rPdf.writeBytes(">>\n");
			rPdf.writeBytes("endobj\n");
			totObjects++;
			
			// INFO
			xref.addAddress(totObjects+1, rPdf.getFilePointer());
			rPdf.writeBytes((totObjects+1)+" 0 obj\n");
						
			rPdf.writeBytes("<<\n");
			rPdf.writeBytes("/Producer(0.0.0)\n");
			//if(GR_CREATOR != null)
			//	rPdf.writeBytes("/Creator("+GRPDF.GR_CREATOR+")\n");
			rPdf.writeBytes(">>\n");
			
			rPdf.writeBytes("endobj\n");
			totObjects++;
			
			// XREF
			long pointerxref = rPdf.getFilePointer();
			rPdf.writeBytes("xref\n");
			rPdf.writeBytes("0 "+(totObjects+1)+"\n");
			
			rPdf.writeBytes("0000000000 65535 f \n");
			
			for(int i = 0;i < xref.getTotalReference();i++)
				rPdf.writeBytes(xref.getAddress()+" 00000 n \n");
			
			
			rPdf.writeBytes("trailer <</Size "+(totObjects+1)+" /Root 1 0 R /Info "+totObjects+" 0 R >>\n");
			rPdf.writeBytes("startxref\n");
			rPdf.writeBytes(""+pointerxref+"\n");
			
			// END PDF
			rPdf.writeBytes("%%EOF");
			rPdf.close();
		} catch(FileNotFoundException fnfe) {
			throw new GRPdfPathNotFoundException(namePDF);
		} catch(IOException ioe) {
			throw new GRPdfIOWriteException(namePDF);
		} 
		
		return totPages;
	}
	/**
	 * Crea un nuovo file PDF estrapolando la pagina specifica dal PDF acquisito.
	 * 
	 * @param namePDF Il nome da assegnare al PDF che si sta creando
	 * @param numPage Il numero di pagina che si intende estrapolare
	 * 
	 * @return Il numero di pagine di cui è formato il PDF creato.
	 * 
	 * @throws GRPdfPathNotFoundException
	 * @throws GRPdfIOWriteException
	 * @throws GRAttachmentException
	 */
	public int createPDF(String namePDF, int numPage) throws GRPdfPathNotFoundException, GRPdfIOWriteException, GRAttachmentException {
		return this.createPDF(namePDF, numPage, numPage);
	}
	/**
	 * Crea un nuovo file PDF clonando il file PDF acquisito.
	 * 
	 * @param namePDF Il nome da assegnare al PDF che si sta creando
	 * 
	 * @return Il numero di pagine di cui è formato il PDF creato.
	 * 
	 * @throws GRPdfPathNotFoundException
	 * @throws GRPdfIOWriteException
	 * @throws GRAttachmentException
	 */
	public int createPDF(String namePDF) throws GRPdfPathNotFoundException, GRPdfIOWriteException, GRAttachmentException {
		if(grfile.getNumPages() == 0)
			return 0;
		
		return this.createPDF(namePDF,1,grfile.getNumPages());
	}
	
}
