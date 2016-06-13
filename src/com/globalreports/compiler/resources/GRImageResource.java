/*
 * ==========================================================================
 * class name  : com.globalreports.compiler.resources.GRImageResource
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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;

public class GRImageResource {
	private final short		TYPE_JPEG			= 1;
	private final short		TYPE_PNG			= 2;
	private final short		TYPE_BMP			= 3;
	
	private String 	id;
	private short 	type;
	private int		width;
	private int 	height;
	private String	pathFile;
	private long	lenStreamImage;
	private byte[] 	streamImage;
	
	public GRImageResource(String pathImage, int contatore) {
		contatore++;
		
		this.id = "Im"+contatore;
		this.pathFile = pathImage;
		
		type = TYPE_JPEG;
		streamImage = null;
		
		lenStreamImage = init(pathImage);
		
	}
	private long init(String pathImage) {
		String estensione = pathImage.substring(pathImage.lastIndexOf('.') + 1).toUpperCase();
		estensione = estensione.toUpperCase();
		
		int ritorno = 0;
		
		pathFile = pathImage;
		
		try {
			BufferedImage img;
			img = ImageIO.read(new File(pathImage));
			
			width = img.getWidth();
			height = img.getHeight();
			
			// Acquisisce lo stream
			if(estensione.equals("JPG") || estensione.equals("JPEG")) {
				type = TYPE_JPEG;
				
			} else if(estensione.equals("PNG")) {
				type = TYPE_PNG;
				
			} else if(estensione.equals("BMP")) {
				type = TYPE_BMP;
			}
				
			streamImage = getStreamBytes();
			
		} catch(FileNotFoundException fnfe) {
			System.out.println("GRImageProperty::setImage::FileNotFoundException: "+fnfe.getMessage());
			ritorno = -1;
		} catch(IOException ioe) {
			System.out.println("GRImageProperty::setImage::IOException: "+ioe.getMessage());
			ritorno = -2;
		}
		
		if(streamImage == null)
			return 0;
		
		return streamImage.length;
	}
	public void setId(String value) {
		this.id = value;
	}
	public String getId() {
		return id;
	}
	public void setPath(String value) {
		this.pathFile = value;
	}
	public String getPath() {
		return pathFile;
	}
	public short getTypeImage() {
		return type;
	}
	public void setDimensionWidth(int value) {
		width = value;
	}
	public int getDimensionWidth() {
		return width;
	}
	public void setDimensionHeight(int value) {
		height = value;
	}
	public int getDimensionHeight() {
		return height;
	}
	public byte[] getImage() {
		return streamImage;
	}
	public int getSizeImage() {
		
		return streamImage.length;
	}
	
	private byte[] getStreamBytes() {
		RandomAccessFile raf;
		byte[] stream = null;
		
		try {
			raf = new RandomAccessFile(pathFile,"r");
				
			stream = new byte[(int)raf.length()];
			raf.read(stream);
				
			raf.close();
		} catch(FileNotFoundException fnfe) {
			System.out.println("GRImageProperty::setImage::FileNotFoundException: "+fnfe.getMessage());
			
			return null;
		} catch(IOException ioe) {
			System.out.println("GRImageProperty::setImage::IOException: "+ioe.getMessage());
			
			return null;
		}
		
		return stream;
	}
}
