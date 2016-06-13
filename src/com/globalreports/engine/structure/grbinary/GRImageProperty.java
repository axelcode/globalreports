/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.grbinary.GRImageProperty
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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class GRImageProperty {
	public static final int TYPEPROP_ID			= 1;
	public static final int TYPEPROP_TYPE		= 2;
	public static final int TYPEPROP_DIMWIDTH	= 3;
	public static final int TYPEPROP_DIMHEIGHT	= 4;
	public static final int TYPEPROP_PATH		= 5;
	
	public static final short  	TYPE_JPEG			= 1;
	public static final short	TYPE_PNG			= 2;
	public static final short	TYPE_BMP			= 3;
	
	private String id;
	private short type;
	private int originalWidth;
	private int originalHeight;
	
	private byte[] streamImage;
	private byte[] mask;
	
	public GRImageProperty() {
		init();
	}
	private void init() {
		type = 0;
		streamImage = null;
	}
	
	public void setId(String value) {
		id = value;
	}
	public String getId() {
		return id;
	}
	public void setType(short value) {
		type = value;
	}
	public short getType() {
		return type;
	}
	public void setOriginalWidth(int value) {
		originalWidth = value;
	}
	public int getOriginalWidth() {
		return originalWidth;
	}
	public void setOriginalHeight(int value) {
		originalHeight = value;
	}
	public int getOriginalHeight() {
		return originalHeight;
	}
	public void setStream(byte[] value) {
		
		switch(type) {
			case TYPE_JPEG:
				streamImage = getStreamJPG(value);
				break;
			
			case TYPE_PNG:
			case TYPE_BMP:
				streamImage = getStreamLossless(value);
				break;
				
			
		}
		
	}
	private byte[] getStreamJPG(byte[] value) {
		byte[] buffer = new byte[(int)value.length];
		System.arraycopy(value, 0, buffer, 0, value.length);
	
		return buffer;
	}
	private byte[] getStreamLossless(byte[] value) {
		// PNG - BMP
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		InputStream input = new ByteArrayInputStream(value);
		BufferedImage image = null;
		try {
			image = ImageIO.read(input);
			
			int height = originalHeight;
	        int width = originalWidth;
	        
	        for (int y = 0; y < height; ++y) {
	            for (int x = 0; x < width; ++x) {
	                Color color = new Color(image.getRGB(x, y));
	                bos.write(color.getRed());
	                bos.write(color.getGreen());
	                bos.write(color.getBlue());
	            }
	        }
	        
	        setMask(image);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		        
        return bos.toByteArray();
        
	}
	private void setMask(BufferedImage image) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		System.out.println(image.getTransparency());
		WritableRaster alpha = image.getAlphaRaster();
		
		if(alpha == null) {
			mask = null;
		} else {
			int[] pixels = alpha.getPixels(0, 0, alpha.getSampleModel().getWidth(), alpha.getSampleModel().getHeight(),(int[]) null);
		
			
			for(int pixel : pixels) {
				bos.write(pixel);
			}
			
			
			mask = bos.toByteArray();
		}
	}
	public void setStream_(byte[] value) {
		streamImage = new byte[(int)value.length];
		
		for(int i = 0;i < value.length;i++)
			streamImage[i] = value[i];
		
	}
	public byte[] getStream() {
		return streamImage;
	}
	public byte[] getMask() {
		return mask;
	}
	public int getSizeImage() {
		if(streamImage == null)
			return 0;
		
		return streamImage.length;
	}
	public int getSizeMask() {
		if(mask == null)
			return 0;
		
		return mask.length;
	}
}
