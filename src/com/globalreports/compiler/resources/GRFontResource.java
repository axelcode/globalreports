/*
 * ==========================================================================
 * class name  : com.globalreports.compiler.resources.GRFontResource
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
package com.globalreports.compiler.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;
import java.util.zip.Deflater;

public class GRFontResource {
	private String 	id;
	private String 	name;
	private short 	type;
	
	protected int lenOriginalStream;
	protected int lenCompressedStream;
	protected byte[] stream;
	
	public GRFontResource(Vector<String> pathFont, String name, int contatore) {
		contatore++;
		
		this.name = name;
		this.id = "f"+contatore;
		this.lenOriginalStream = -1;
		this.lenCompressedStream = -1;
		
		init(pathFont);
		
	}
	private void init(Vector<String> pathFont) {
		boolean fontTrovato = false;
		//type = SUBTYPE_TRUETYPE;
		
		/* Cicla per tutti i path fino a che non trova il font */
		for(int i = 0;i < pathFont.size();i++) {
			fontTrovato = createFont(pathFont.get(i), name);
		
			if(fontTrovato)
				break;
		}
			
		
	}
	protected boolean createFont(String pathFont, String fileName) {
		RandomAccessFile raf;
		
		int lenByte;
		byte[] font;
		byte[] fontCompress;
		boolean fileExists = false;
		
		/* Esegue un test sulle desinenze finali.
		 * Questo serve per semplificare la vita allo sviluppatore di layout
		 * che magari non si ricorda il nome esatto del font
		 */
		if(fileName.length() > 11) {
			if(fileName.endsWith("-BoldItalic")) {
				fileName = fileName.substring(0,fileName.indexOf("-BoldItalic"))+"z";
			}
		}
		if(fileName.length() > 7) {
			if(fileName.endsWith("-Italic")) {
				fileName = fileName.substring(0,fileName.indexOf("-Italic"))+"i";
			}
		}
		if(fileName.length() > 5) {
			if(fileName.endsWith("-Bold")) {
				fileName = fileName.substring(0,fileName.indexOf("-Bold"))+"b";
			}
		}
		// Verifica che il font sia presente nel percorso specificato
		File f = new File(pathFont+fileName+".ttf");
		if(f.exists()) {
			fileExists = true;
		}
		
		if(!fileExists) {
			// TO DO: Va gestita la mancata corrispondenza del file
			System.out.println("ERRORE: Font non trovato!");
			
			return false;
		}
		
		try {
			
			raf = new RandomAccessFile(pathFont+fileName+".ttf","r");
			
			lenByte = (int)raf.length();
			font = new byte[lenByte];
			fontCompress = new byte[lenByte];
			
			raf.read(font);
			raf.close();
			
			Deflater compresser = new Deflater();
			compresser.setInput(font);
			compresser.finish();
			
			int lenCompress = compresser.deflate(fontCompress);
					
			lenOriginalStream = lenByte;
			lenCompressedStream = lenCompress;
			
			stream = new byte[lenCompressedStream];
			System.arraycopy(fontCompress, 0, stream, 0, lenCompressedStream);
		} catch(FileNotFoundException fnfe) {
			return false;
		} catch(IOException ioe) {
			System.out.println("GRFontFile::elabora::IOException: "+ioe.getMessage());
		}
		
		return true;
	}
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public short getType() {
		return type;
	}
	public byte[] getStream() {
		if(stream == null)
			return null;
		
		return stream;
	}
	public int getLenOriginalStream() {
		return lenOriginalStream;
	}
	public int getLenCompressedStream() {
		return lenCompressedStream;
	}
}
