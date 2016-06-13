/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.font.GRFontDataStream
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
package com.globalreports.engine.structure.font;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class GRFontDataStream {
	public static final int EOF		= -1;
		
	private byte[] buffer;
	int pointer;
	
	public GRFontDataStream(byte[] buff) {
		if(buff != null) {
			buffer = new byte[buff.length];
			System.arraycopy(buff, 0, buffer, 0, buff.length);
			
			pointer = 0;
		} else
			pointer = EOF;
	}
	
	public void seek(int index) {
		if(buffer == null)
			return;
		
		if(index > buffer.length)
			return;
		
		pointer = index;
	}
	public float readDecimal() {
		float ritorno = 0;
		ritorno = readShort();
		
		ritorno = ritorno + (readUShort() / 65536.0f);
		
		return ritorno;
	}
	public int read() {
		int b;
		
		try {
			b = buffer[pointer];
			pointer++;
		} catch(ArrayIndexOutOfBoundsException e) {
			return EOF;
		}
		
		return (b+256)%256;
	}
	public byte readByte() {
		byte b;
		
		try {
			b = buffer[pointer];
			pointer++;
		} catch(ArrayIndexOutOfBoundsException e) {
			return EOF;
		}
		
		return (byte)b;
	}
	public byte[] read(int length) {
        byte[] data = new byte[length];
        int amountRead = 0;
        int totalAmountRead = 0;
        // read at most numberOfBytes bytes from the stream.
        while (totalAmountRead < length && (amountRead = read(data, totalAmountRead, length - totalAmountRead)) != -1) {
            totalAmountRead += amountRead;
        }

        return data;
    }
	public int read(byte[] b) {
		System.arraycopy(buffer,pointer,b,0,b.length);
		pointer = pointer + b.length;
		return b.length;
	}
	public int read(byte[] b, int off, int len) {
        int amountRead = Math.min( len, buffer.length-pointer);
            
        System.arraycopy(buffer,pointer,b, off, amountRead );
        pointer+=amountRead;
        return amountRead;
       
     }
	public int readUShort() {
		int c1 = read();
		int c2 = read();
		
		if((c1 | c2) < 0) {
			return 0;
			//System.out.println("ERR: READUSHORT");
		}
		
		return (c1 << 8) + (c2 << 0);
	}
	public short readShort() {
		int c1 = read();
		int c2 = read();
		
		if((c1 | c2) < 0) {
			System.out.println("ERR: READSHORT");
		}
		
		return (short)((c1 << 8) + (c2 << 0));
	}
	public int readInt() {
		int c1 = read();
		int c2 = read();
		int c3 = read();
		int c4 = read();
		
		if((c1 | c2 | c3 | c4) < 0) {
			System.out.println("ERR: READINT");
		}
		
		return ((c1 << 24) + (c2 << 16) + (c3 << 8) + (c4 << 0));
	}
	public long readUInt() {
		long b1 = read();
		long b2 = read();
		long b3 = read();
		long b4 = read();
		
		if(b4 < 0) {
			System.out.println("ERR: READUINT");
			
			
		}
		return (b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0);
	}
	public long readLong() {
		return ((long)(readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
	}
	public String readLine() {
		String line = null;
		int b;
		
		int startBuffer = pointer;
		int lenBuffer = 0;
		
		if(buffer == null)
			return null;
		
		while((b = read()) != EOF) {
			switch(b) {
				case 10:
					return new String(buffer,startBuffer,lenBuffer);
					
				case 13:
					if(read() != 10) {
						rewind(1);
					} 
					return new String(buffer,startBuffer,lenBuffer);
			}
			
			lenBuffer++;
		}
		
		return new String(buffer,startBuffer,lenBuffer);
	}
	public String readLine(int length) {
		byte[] buffer = read(length);
		
		return new String(buffer,0,length);
	}
	public Calendar readDate() {
        long secondsSince1904 = readLong();
        Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(1904, 0, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long millisFor1904 = cal.getTimeInMillis();
        millisFor1904 += (secondsSince1904 * 1000);
        cal.setTimeInMillis(millisFor1904);
        return cal;
    }
	public int[] readUShortArray(int length) {
        int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = readShort();
        }
        return array;
    }
	public int getFilePointer() {
		return pointer;
	}
	public int length() {
		if(buffer == null)
			return 0;
		
		return buffer.length;
	}
	public void rewind(int numBytes) {
		pointer = pointer - numBytes;
		
		if(pointer < 0)
			pointer = 0;
	}
}

