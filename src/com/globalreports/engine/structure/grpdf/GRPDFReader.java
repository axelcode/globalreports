/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.grpdf.GRPDFReader
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalreports.engine.err.GRAttachmentEncryptNotSupported;
import com.globalreports.engine.err.GRAttachmentException;
import com.globalreports.engine.err.GRAttachmentFileNotFoundException;
import com.globalreports.engine.err.GRAttachmentIOReadException;
import com.globalreports.engine.err.GRAttachmentIndexException;
import com.globalreports.engine.err.GRAttachmentPageNotExists;
import com.globalreports.engine.err.GRAttachmentRootException;
import com.globalreports.engine.err.GRAttachmentXrefUnknowException;
import com.globalreports.engine.filter.GRFlateDecode;
import com.globalreports.engine.io.GRPDFInputStream;

public class GRPDFReader {
	private GRPDFInputStream pdf;
	private GRCrossReferenceTable xreftable;
	
	private GRXrefTable xrefnew;
	private GRPDFObject[] refobj;
	private Hashtable<Integer, Integer> indexObject;
	private int startIndex;
	private int totaleOggetti;
	private int totObjectForPage;
	
	private int root;
	private Vector<Integer> pages;
	private String kids;
	
	public GRPDFReader(String pathPdf) throws GRAttachmentException {
		totaleOggetti = 0;
		root = -1;
		pages = null;
		kids = "";
		
		// Legge il file
		this.readFile(pathPdf);
		
		// Acquisisce la struttura
		this.readStructure();
		
	}
	private void readFile(String pathPdf) throws GRAttachmentException {
		// Acquisisce l'intero file pdf sotto forma di bytes
		RandomAccessFile raf;
		
		try {
			raf = new RandomAccessFile(pathPdf,"r");
			
			int lenFile = (int)raf.length();
			byte[] buffer = new byte[lenFile];
			raf.read(buffer);
			
			raf.close();
			
			pdf = new GRPDFInputStream(buffer);
		} catch (FileNotFoundException e) {
			throw new GRAttachmentFileNotFoundException(pathPdf);
		} catch (IOException e) {
			throw new GRAttachmentIOReadException(pathPdf);
		}
	}
	private void readStructure() throws GRAttachmentException {
		
		readXref();
		
		refobj = new GRPDFObject[xreftable.length()];
		
		// Legge tutti gli oggetti presenti nel PDF.
		long startTime = System.currentTimeMillis();
		for(int i = 1;i < xreftable.length();i++) {
			
			GRCrossElement element = xreftable.getElement(i);
			
			//System.out.println("Inserisco elementi: "+i+" - "+element.getType());
			
			if(element != null) {
				
				if(element.getType() == GRCrossElement.CROSSTYPE_OBJECT) {
					
					GRPDFObject obj = GRPDFContentLibrary.readObject(pdf,i,xreftable);
					
					// Ogni oggetto lo aggiunge alla struttura
					refobj[i] = obj;
					totaleOggetti++;
				} else if(element.getType() == GRCrossElement.CROSSTYPE_OBJSTREAM) {
					int objstm = element.getReferenceObject();
					long address = xreftable.getElement(element.getReferenceObject()).getAddress();
					int index = xreftable.getElement(i).getIndex();
					
					if(refobj[objstm] == null) {
						// Quello che segue viene fatto in quanto ci sono casi in cui
						// Nella CRS si fa riferimento a oggetti STM successivi (e quindi non ancora acquisiti)
						// La cosa brutta di questa procedura è che ci si ritrova a leggere
						// Due volte lo stesso oggetto (verrà acquisito anche nel proseguio
						// del ciclo for)
						GRPDFObject obj = GRPDFContentLibrary.readObject(pdf,objstm,xreftable);
						
						// Ogni oggetto lo aggiunge alla struttura
						refobj[objstm] = obj;
					}
					
					refobj[i] = refobj[objstm].getObjStm(index);
					
				}
			}
			
			//if(obj.getType() == GRPdfObject.TYPE_CATALOG)
			//	root = obj.getIndex();
			
		}
		long estimatedTime = System.currentTimeMillis() - startTime;
		
		//System.out.println("Tempo di esecuzione: "+estimatedTime);
		
		// /Root
		int indexPages = readRoot();
		
		// /Pages. Estrae i /Kids
		this.readKids(indexPages);
		
	}
	private void readXref() throws GRAttachmentXrefUnknowException, GRAttachmentEncryptNotSupported {
		int startXRef;
		
		startXRef = findStartXrefAddress();
		
		if(startXRef == -1)
			throw new GRAttachmentXrefUnknowException();
		
		xreftable = new GRCrossReferenceTable();
		
		// Si posizione all'inizio di startxref per leggere l'indirizzo di inizio della tabella
		pdf.seek(startXRef);	
		pdf.readLine();	// startxref
						
		int address = Integer.parseInt(pdf.readLine());	// Indirizzo
		
		this.readXref(address);
	}
	private void readXref(int address) throws GRAttachmentXrefUnknowException, GRAttachmentEncryptNotSupported {
		String REG_CROSSREFERENCESTREAM = "(\\/Root ([0-9]+)|\\/Size ([0-9]+)|\\/Predictor ([0-9]+)|\\/Length ([0-9]+)|\\/Prev ([0-9]+)|\\/Index[ ]{0,}\\[([0-9]+) ([0-9]+)|\\/W[ ]{0,}\\[([0-9]+) ([0-9]+) ([0-9]+)|\\/Encrypt ([0-9]+) 0 R)";
		
		String strTrailer = "";
		String lineTemp;
		int xrefStart;
		int xrefEnd;
		int startAddress;
		
		Pattern pattern;
		Matcher matcher;
		
		pdf.seek(address);
		String linexref = pdf.readLine(); // A perdere. xref
		
		// TEST
		/*
		pdf.seek(10427);
		byte[] test = new byte[451];
		pdf.read(test);
		byte[] bufftest = GRPDFContentLibrary.FlateDecode(test, true);
		
		RandomAccessFile fp;
		try {
			fp = new RandomAccessFile("C:\\prova8.txt","rw");
			fp.write(bufftest);
			fp.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("BUFFER: "+bufftest.length);
		System.exit(0);
		*/
		// TEST FINE
		if(linexref.equals("xref")) {	// Cross Reference Table
			
			// Legge il range di indirizzi
			lineTemp = pdf.readLine();
			String[] rangexref = lineTemp.split(" ");
			xrefStart = Integer.parseInt(rangexref[0]);
			xrefEnd = xrefStart + Integer.parseInt(rangexref[1]);
			
			// Salva l'inizio della xref
			startAddress = pdf.getFilePointer();
			
			// Si sposta alla fine per leggere il trailer
			pdf.seek((20 * Integer.parseInt(rangexref[1])) + startAddress);
			
			// Legge il trailer
			while(!(lineTemp = pdf.readLine()).contains("startxref")) {
				strTrailer = strTrailer + lineTemp;
			}
			
			int prev = readTrailer(strTrailer);
			
			if(prev > 0)
				readXref(prev);
			
			pdf.seek(startAddress);
			
			for(int i = xrefStart;i < xrefEnd;i++) {
				lineTemp = pdf.readLine();
				
				String[] elem = lineTemp.split(" ");
				
				if(elem[2].equals("f"))
					xreftable.addElement(GRCrossElement.CROSSTYPE_FREEOBJECT,xrefStart,Long.parseLong(elem[0]),Integer.parseInt(elem[1]));
				else	
					xreftable.addElement(GRCrossElement.CROSSTYPE_OBJECT,xrefStart,Long.parseLong(elem[0]),Integer.parseInt(elem[1]));
			
			}
			
		} else {	// Cross Reference Stream
			int prev = 0;
			int length = 0;
			int size = 0;
			int predictor = 0;
			int indexArray[] = new int[2];
			int wArray[] = new int[3];
			
			xrefStart = 0;
			xrefEnd = 0;
			
			// Legge il record contenente i dati dello stream
			lineTemp = pdf.readLine();
				
			pattern = Pattern.compile(REG_CROSSREFERENCESTREAM);
			matcher = pattern.matcher(lineTemp);
			while(matcher.find()) {
				if(matcher.group(2) != null)
					root = Integer.parseInt(matcher.group(2));
				if(matcher.group(3) != null)
					size = Integer.parseInt(matcher.group(3));
				if(matcher.group(4) != null)
					predictor = Integer.parseInt(matcher.group(4));
				if(matcher.group(5) != null)
					length = Integer.parseInt(matcher.group(5));
				if(matcher.group(6) != null)
					prev = Integer.parseInt(matcher.group(6));
				if(matcher.group(7) != null) {
					
					xrefStart = Integer.parseInt(matcher.group(7));
					xrefEnd = Integer.parseInt(matcher.group(8));
				}
				if(matcher.group(9) != null) {
					wArray[0] = Integer.parseInt(matcher.group(9));
					wArray[1] = Integer.parseInt(matcher.group(10));
					wArray[2] = Integer.parseInt(matcher.group(11));
					
				}
				if(matcher.group(12) != null) {
					throw new GRAttachmentEncryptNotSupported();
				}
			}
			
			// Salva l'inizio della xref
			startAddress = pdf.getFilePointer();
			
			if(prev > 0)
				readXref(prev);
			
			pdf.seek(startAddress);
			byte[] b = new byte[length];
			pdf.read(b);
			byte[] buff = GRFlateDecode.decode(b, true);
			
			readCrossReferenceStream(buff, wArray[0], wArray[1], wArray[2], predictor, xrefStart, xrefEnd, size);

		}
		
	}
	private void readCrossReferenceStream(byte[] buff, int w1, int w2, int w3, int predictor, int indexStart, int indexEnd, int size) {
		int lenStream = w1 + w2 + w3;
		int startAddress = 0;
		int indexElement = 0;
		
		if(indexEnd == 0)
			indexEnd = size;
		
		if(predictor >= 10)
			lenStream++;
		
		byte[] lastline = new byte[lenStream];
		byte[] currentline = new byte[lenStream];
		byte[] addr = new byte[w2];
		
		for(int i = 0;i < indexEnd;i++) {
			for (int p = 0; p < lenStream; p++) {
                int up = buff[(i * lenStream) + p] & 0xff;
                int prior = lastline[p] & 0xff;
                currentline[p] = (byte) ((up + prior) & 0xff);
            }
			System.arraycopy(currentline, 0, lastline, 0, lenStream);
			
			startAddress = 0;
			startAddress = startAddress + w1;
			if(predictor >= 10)
				startAddress++;
			
			System.arraycopy(currentline, startAddress, addr, 0, w2);
			
			if(w3 > 0)
				indexElement = currentline[1+w1+w2];
			
			xreftable.addElement(currentline[1],indexStart,GRPDFContentLibrary.bytesToLong(addr),indexElement);
			/*
			if(currentline[1] == 0) {
				xreftable.addElement(GRCrossElement.CROSSTYPE_FREEOBJECT,indexStart,GRPDFContentLibrary.bytesToLong(addr),Integer.parseInt(""+currentline[1+w1+w2]));
			} else if(currentline[1] == 1) {
				System.out.println("INSERISCO: "+GRPDFContentLibrary.bytesToLong(addr));
				xreftable.addElement(GRCrossElement.CROSSTYPE_OBJECT,indexStart,GRPDFContentLibrary.bytesToLong(addr),Integer.parseInt(""+currentline[1+w1+w2]));
			} else if(currentline[1] == 2) {
				xreftable.addElement(GRCrossElement.CROSSTYPE_OBJSTREAM,indexStart,GRPDFContentLibrary.bytesToLong(addr),Integer.parseInt(""+currentline[1+w1+w2]));
			}
			*/
			/*
			System.out.print("ADDRESS: ("+indexStart+") ");
			System.out.print(currentline[1]+" ");
			
            System.out.print(GRPDFContentLibrary.bytesToLong(addr));
            if(w3 == 0)
            	System.out.println(" 0");
            else
            	System.out.println(" "+currentline[1+w1+w2]);
			*/
            indexStart++;
		}
		//System.out.println(buff.length);
		
	}
	private int readRoot() throws GRAttachmentRootException {
		String REG_DICT_PAGES		= "(\\/Pages ([0-9]+) 0 R)";
		
		int indexPages = 0;
		Pattern pattern;
		Matcher matcher;
		
		if(root == -1)
			throw new GRAttachmentRootException();
		
		
		GRPDFObject objRoot = refobj[root];
		pattern = Pattern.compile(REG_DICT_PAGES);
		matcher = pattern.matcher(objRoot.getDictionary());
		
		while(matcher.find()) {
			indexPages = Integer.parseInt(matcher.group(2));
		}
		
		if(indexPages == 0)
			throw new GRAttachmentRootException();
		
		return indexPages;
	}
	private void readArrayReferences(String key) {
		
	}
	private void readKids(int index) throws GRAttachmentException {
		String REG_DICT_KIDS		= "(\\/Kids[ ]{0,}(\\[[ ]{0,}([0-9]+ 0 R[ ]{0,}){1,}\\]))";
		String REG_KIDS = "(([0-9]+) 0 R)";

		GRPDFObject obj = refobj[index];
		
		if(isPage(obj)) {
			// /Page. La aggiunge
			if(pages == null)
				pages = new Vector<Integer>();
			
			pages.add(index);
		} else {
			// /Pages. Legge il riferimento del /Kids
			Pattern pattern;
			Matcher matcher;
			
			pattern = Pattern.compile(REG_DICT_KIDS);
			matcher = pattern.matcher(obj.getDictionary());
			while(matcher.find()) {
				// Estrae tutti i figli
				
				Pattern pKids = Pattern.compile(REG_KIDS);
				Matcher mKids = pKids.matcher(matcher.group(2));
				
				while(mKids.find()) {
					readKids(Integer.parseInt(mKids.group(2)));
				}
				
			}
		}
		
	}
	private boolean isPage(GRPDFObject obj) {
		String REG_TYPE_PAGE		= "(\\/Type[ ]{0,}\\/Page[^s])";
		
		Pattern pattern;
		Matcher matcher;
		
		pattern = Pattern.compile(REG_TYPE_PAGE);
		matcher = pattern.matcher(obj.getDictionary());
			
		while(matcher.find()) {
			return true;
		}
			
		return false;
	}
	private int readTrailer(String trailer) {
		String REG_SIZE	= "(\\/Size ([0-9]+))";
		String REG_ROOT	= "(\\/Root ([0-9]+) [0-9]+ R)";
		String REG_PREV	= "(\\/Prev ([0-9]+))";
		
		long valueSize = 0;
		int valuePrev = 0;
		int valueRoot = 0;
		
		Pattern pattern;
		Matcher matcher;
		
		// Size
		pattern = Pattern.compile(REG_SIZE);
		matcher = pattern.matcher(trailer);
		while(matcher.find()) {
			if(xreftable == null)
				xreftable = new GRCrossReferenceTable();
			
			valueSize = Long.parseLong(matcher.group(2));
		}
			
		// Prev
		pattern = Pattern.compile(REG_PREV);
		matcher = pattern.matcher(trailer);
		while(matcher.find()) {
			valuePrev =Integer.parseInt(matcher.group(2));
		}
		
		// Root
		pattern = Pattern.compile(REG_ROOT);
		matcher = pattern.matcher(trailer);
		while(matcher.find()) {
			valueRoot = Integer.parseInt(matcher.group(2));
		}
		
		if(valueRoot > 0)
			root = valueRoot;
		
		return valuePrev;
		
	}
	private int readElementXref(int start, int end)  {
		// Legge gli elementi contenuti nella xref.
		// Ritorna il numero di elementi letti. Se errore ritorna -1
		String lineTemp;
		
		int totElementi = 0;
		
		if(xreftable == null)
			return -1;
		
		for(int i = start;i < end;i++) {
			
			lineTemp = pdf.readLine();
			
			String[] elem = lineTemp.split(" ");
			
			
			//xref.addAddress(i, Long.parseLong(elem[0]));
			//xrefaddress[i] = Long.parseLong(elem[0]);
			
			totElementi++;
		}
		
		return totElementi;
	}
	private int findStartXrefAddress() throws GRAttachmentXrefUnknowException {
		byte[] buffer = new byte[64];
		byte[] startXRef = {'s','t','a','r','t','x','r','e','f'};
		
		int fInitialPointer = pdf.length();
		
		pdf.seek(fInitialPointer - buffer.length);
		pdf.read(buffer);
		
		int pointer = arrayBytesContains(buffer, startXRef);
		if(pointer == -1)
			throw new GRAttachmentXrefUnknowException();
		
		pointer = fInitialPointer - buffer.length + pointer;
		
		return pointer;
	}
	private int arrayBytesContains(byte[] b1, byte[] b2) {
		
		if(b1.length < b2.length)
			return -1;
		
		for(int i = 0;i < b1.length;i++) {
			int x = 0;
			
			for(x = 0;x < b2.length;x++) {
				if(b1[i+x] != b2[x])
					break;
			}

			if(x == b2.length)
				return i;
		}
		return -1;
	}
	
	/*
	 * METODI AGGIUNTI COPIATI DA GRATTACHPDF
	 * 
	 */
	public String getKids() {
		
		return kids;
	}
	public int getNumPages() {
		if(pages == null)
			return 0;
		
		return pages.size();
	}
	public int getTotaleObjects() {
		if(refobj == null)
			return 0;
		
		return refobj.length;
	}
	public GRPDFPage getPage(int index) throws GRAttachmentPageNotExists {
		try {
			return new GRPDFPage(refobj[pages.get(index)]);
		} catch (GRAttachmentIndexException e) {
			System.out.println("ERRORE");
			e.printStackTrace();
		} catch(ArrayIndexOutOfBoundsException e) {
			throw new GRAttachmentPageNotExists();
		}
		
		return null;
	}
	public String extraxtTextFromPage(int index) throws GRAttachmentPageNotExists {
		StringBuffer text = new StringBuffer();
		
		index--;
		try {
			GRPDFPage page = new GRPDFPage(refobj[pages.get(index)]);
			Vector<Integer> c = page.getIndexContents();
			
			if(c == null)
				return "";
			
			for(int i = 0;i < c.size();i++) {
				GRPDFObject ob = refobj[c.get(i)];
				
				if(ob.getLenStream() > 0) {
					byte[] buff = GRFlateDecode.decode(ob.getStream(), true);
					
					Vector<String> p = GRPDFContentLibrary.extractText(new String(buff, 0, buff.length));
					
					if(p != null) {
						for(int x = 0;x < p.size();x++) {
							text.append(p.get(x));
						}
					}
				}
			}
			
			
		} catch (GRAttachmentIndexException e) {
			System.out.println("ERRORE");
			e.printStackTrace();
		} catch(ArrayIndexOutOfBoundsException e) {
			throw new GRAttachmentPageNotExists();
		}
		
		return text.toString();
	}
	public Vector<String> extraxtTextFromPageIntoParagraph(int index) throws GRAttachmentPageNotExists {
		Vector<String> paragrafo = new Vector<String>();
		
		try {
			GRPDFPage page = new GRPDFPage(refobj[pages.get(index)]);
			Vector<Integer> c = page.getIndexContents();
			
			if(c == null)
				return paragrafo;
			
			for(int i = 0;i < c.size();i++) {
				GRPDFObject ob = refobj[c.get(i)];
				
				if(ob.getLenStream() > 0) {
					byte[] buff = GRFlateDecode.decode(ob.getStream(), true);
					
					Vector<String> p = GRPDFContentLibrary.extractText(new String(buff, 0, buff.length));
					
					if(p != null) {
						for(int x = 0;x < p.size();x++) {
							paragrafo.add(p.get(x));
						}
					}
				}
			}
			
			
		} catch (GRAttachmentIndexException e) {
			System.out.println("ERRORE");
			e.printStackTrace();
		} catch(ArrayIndexOutOfBoundsException e) {
			throw new GRAttachmentPageNotExists();
		}
		
		return paragrafo;
	}
	public void getContentPage(int index) throws GRAttachmentPageNotExists {
		try {
			GRPDFPage page = new GRPDFPage(refobj[pages.get(index)]);
			Vector<Integer> c = page.getIndexContents();
			
			getText(c.get(0));
			
		} catch (GRAttachmentIndexException e) {
			System.out.println("ERRORE");
			e.printStackTrace();
		} catch(ArrayIndexOutOfBoundsException e) {
			throw new GRAttachmentPageNotExists();
		}
	}
	public String getText(int index) {
		
		/* Visualizzare lo stream decodificato */
		GRPDFObject ob = refobj[index];
		
		if(ob.getLenStream() > 0) {
			byte[] buff = GRFlateDecode.decode(ob.getStream(), true);
			
			Vector<String> p = GRPDFContentLibrary.extractText(new String(buff, 0, buff.length));
			//System.out.println(p.get(1));
			return new String(buff,0,buff.length);
		}
		
		return null;
	}
	public void newDocument(GRXrefTable xref) {
		
		this.xrefnew = xref;
		int totObj = 0;
		kids = "";
		indexObject = new Hashtable<Integer, Integer>();
		
	}
	public int write(RandomAccessFile raf, GRXrefTable xref, int gapObj) throws IOException, GRAttachmentException {
		xrefnew = xref;
		int totObj = 0;
		indexObject = new Hashtable<Integer, Integer>();
		
		for(int indexPage = 0;indexPage < this.getNumPages();indexPage++) {
			totObj = totObj + this.writePage(raf, indexPage, (totObj + gapObj));
		}
		
		return totObj;
	}
	
	public int writePage(RandomAccessFile raf, int numPage, int startIndex) throws IOException, GRAttachmentException {
		StringBuffer content = new StringBuffer();
		
		content.append("q\n");
		content.append("0.5 w\n");
		content.append("1.0 1.0 1.0 RG\n");
		
		content.append("28.34 28.34 141.73 56.68 re\n");
		content.append("S\n");
		
		content.append("Q\n");
		return this.writePage(raf, numPage, startIndex, null);
	}
	public int writePage(RandomAccessFile raf, int numPage, int startIndex, String content) throws IOException, GRAttachmentException {
		
		String REG_REF = "(([0-9a-zA-Z]+) ([0-9]+) 0 R)|(\\[[ ]{0,}([0-9]+) 0 R)";
		String WORD_PARENT = "(\\/Parent [0-9]+ 0 R)";
		String WORD_CONTENT = "(\\/Contents ([0-9]+) 0 R)|(\\/Contents[ ]{0,}\\[[ 0-9R]+\\])";
		String WORD_FONT = "(\\/Font[ ]{0,}<<[ ]{0,}([a-zA-Z0-9_ \\/]{0,})>>)";
		
		this.startIndex = startIndex;
		totObjectForPage = 0;
		
		Pattern pattern;
		Matcher matcher;
		String valuePage = refobj[pages.get(numPage)].getDictionary();
		GRPDFPage page = new GRPDFPage(refobj[pages.get(numPage)]);
		
		// Inserisce le risorse
		String resources = page.getResources();

		if(resources != null) {
			pattern = Pattern.compile(REG_REF);
			matcher = pattern.matcher(resources);
			while(matcher.find()) {
				writeObject(raf, Integer.parseInt(matcher.group(3)));
			}
		} else {
			writeObject(raf, page.getIndexResources());
		}
		
		// Aggiunge il contenuto
		Vector<Integer> indexContents = page.getIndexContents();
		if(indexContents == null)
			throw new GRAttachmentIndexException("/Contents");
		
		for(int i = 0;i < indexContents.size();i++)
			writeObject(raf, indexContents.get(i));
		
		if(content != null) {
			
			// Aggiunge il contenuto 
			//String buffContent = content.getContentStream();
			String buffContent = "";
			
			GRPDFObject newObj = new GRPDFObject(refobj.length+1);
			newObj.setDictionary("<</Length "+buffContent.length()+">> stream");
			newObj.setStream(buffContent.getBytes());
			writeContent(raf, newObj);
			
			// Aggiorna i riferimenti ai contenuti
			pattern = Pattern.compile(WORD_CONTENT);
			matcher = pattern.matcher(valuePage);
			
			while(matcher.find()) {
				valuePage = valuePage.replace(matcher.group(1),  "/Contents ["+matcher.group(2)+" 0 R "+(refobj.length+1)+" 0 R] ");
			}
		}
				
		
		// Aggiunge la pagina
		valuePage = getNewReference(valuePage);
		pattern = Pattern.compile(WORD_PARENT);
		matcher = pattern.matcher(valuePage);
		
		while(matcher.find()) {
			valuePage = valuePage.replace(matcher.group(1), "/Parent 2 0 R");
		}
		
		// Se presente un contenuto aggiuntivo aggiunge la risorsa
		if(content != null) {
			
			pattern = Pattern.compile(WORD_FONT);
			matcher = pattern.matcher(valuePage);
					
			while(matcher.find()) {
				valuePage = valuePage.replace(matcher.group(1),  "/Font <</GRF1 3 0 R "+matcher.group(2)+">> ");
			}
		}			
					
		kids = kids + this.startIndex + " 0 R ";
		
		startObject(raf, page.getIndex());
		
		raf.writeBytes(valuePage+"\n");
		raf.writeBytes("endobj\n");
		
		return totObjectForPage;
	}
	private String getNewReference(String value) {
		String REG_REFERENCE = "(([0-9a-zA-Z]+) ([0-9]+) 0 R)|(\\/([0-9a-zA-Z]+)[ ]{0,}\\[[ 0-9R]+\\])";
		
		Pattern pattern;
		Matcher matcher;
		int newIndex = 0;
		String oldToken, newToken;
		
		pattern = Pattern.compile(REG_REFERENCE);
		matcher = pattern.matcher(value);
		
		while(matcher.find()) {
			if((matcher.group(2) == null || !matcher.group(2).equals("Parent"))) {
				/*
				System.out.println("MATCHER 1:"+matcher.group(1));
				System.out.println("MATCHER 2:"+matcher.group(2));
				System.out.println("MATCHER 3:"+matcher.group(3));
				System.out.println("MATCHER 4:"+matcher.group(4));
				System.out.println("MATCHER 5:"+matcher.group(5));
				*/
				if(matcher.group(3) == null) {
					oldToken = matcher.group(4);
					newToken = "/" + matcher.group(5) + " [";
					/*
					System.out.println("CERCO: "+value);
					System.out.println("M4: "+matcher.group(4));
					System.out.println("M5: "+matcher.group(5));
					*/
					Vector<Integer> v = GRPDFContentLibrary.getRef(matcher.group(4), matcher.group(5));
					if(v != null) {
						//System.out.println(indexObject.size());
						for(int i = 0;i < v.size();i++) {
							newIndex = indexObject.get(v.get(i));
							
							newToken = newToken + newIndex + " 0 R ";
						}
						
						newToken = newToken + "]";
						value = value.replace(oldToken, newToken);
					}
						
				} else {
					oldToken = matcher.group(1);
					newToken = matcher.group(2);
					
					newIndex = Integer.parseInt(matcher.group(3));
					newIndex = indexObject.get(newIndex);
					
					value = value.replace(oldToken, newToken+" "+newIndex+" 0 R");
				}
				
				
				
			}
		}
		
		return value;
	}	
	private void writeContent(RandomAccessFile raf, GRPDFObject obj) throws IOException {
		startObject(raf, obj.getIndex());
		
		raf.writeBytes(getNewReference(obj.getDictionary())+"\n");
		if(obj.hasStream()) {
			raf.write(obj.getStream());
			raf.writeBytes("\n");
			raf.writeBytes("endstream\n");
		}

		raf.writeBytes("endobj\n");
	}
	private void writeObject(RandomAccessFile raf, int index) throws IOException {
		
		//System.out.println("Scrivo oggetto: "+index);
		
		GRPDFObject obj = refobj[index];
		if(indexObject.get(obj.getIndex()) != null) 
			return;
		
		Vector<Integer> v = GRPDFContentLibrary.getRef(obj.getDictionary());
		
		if(v != null) {
			for(int i = 0;i < v.size();i++)
				writeObject(raf, v.get(i));
		}
		/*
		pattern = Pattern.compile(REG_REF);
		matcher = pattern.matcher(obj.getDictionary());
		while(matcher.find()) {
			System.out.println("WRITEOBJ: "+matcher.group(1));
			if(matcher.group(3) == null)
				writeObject(raf, Integer.parseInt(matcher.group(5)));
			else
				writeObject(raf, Integer.parseInt(matcher.group(3)));
			
		}
		*/
		
		/* Visualizzare lo stream decodificato 
		if(index == 26) {
			byte[] buff = GRPDFContentLibrary.FlateDecode(obj.getStream(), true);
			System.out.println(new String(buff, 0, buff.length));
			System.out.println("LENGTH: "+buff.length);
		}
		*/
		startObject(raf, obj.getIndex());
		
		raf.writeBytes(getNewReference(obj.getDictionary())+"\n");
		if(obj.hasStream()) {
			//byte[] buff = GRFlateDecode.decode(obj.getStream(), true);
			//System.out.println("BUFF: "+new String(buff));
			//byte[] buff2 = GRFlateDecode.encode(buff);
			//raf.write(buff2);
						
			raf.write(obj.getStream());
			raf.writeBytes("\n");
			raf.writeBytes("endstream\n");
		}

		raf.writeBytes("endobj\n");
		
	}
	private void startObject(RandomAccessFile raf, int index) throws IOException {
		indexObject.put(index, startIndex);
		
		//System.out.println("VECCHIO: "+index+" - NUOVO: "+startIndex);
		
		// Aggiorna l'address nella xref
		xrefnew.addAddress(startIndex, raf.getFilePointer());
		
		// Scrive l'apertura
		raf.writeBytes(startIndex+" 0 obj\n");
		
		startIndex++;
		totObjectForPage++;
	}
}
