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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.InflaterInputStream;

import com.globalreports.engine.io.GRPDFInputStream;

public abstract class GRPDFContentLibrary {
	private static final String REG_STREAM_LENGTH =  "(\\/Length[ ]+([0-9]+)([ ]{0,}0 R){0,1})";
	
	public static Vector<String> extractText(String value) {
		StringBuffer buffer = null;
		Vector<String> paragrafo = null;
		
		boolean flagParagrafo = false;
		boolean flagQuadra = false;
		boolean flagTonda = false;
		
		int puntatore = 0;
		try {
		while(puntatore < value.length()) {
			if(value.substring(puntatore,puntatore+2).equals("BT")) {
				if(paragrafo == null)
					paragrafo = new Vector<String>();
				
				buffer = new StringBuffer();
				flagParagrafo = true;
				
				puntatore = puntatore + 2;
			} else if(value.substring(puntatore,puntatore+2).equals("ET")) {
				paragrafo.add(buffer.toString());
				flagParagrafo = false;
				
				puntatore = puntatore + 2;
			} else if(value.codePointAt(puntatore) == 40) {
				if(flagParagrafo)
					flagTonda = true;
				
				puntatore++;
			} else if(value.codePointAt(puntatore) == 41) {
				if(flagParagrafo)
					flagTonda = false;
				
				puntatore++;
			} else {
				if(flagTonda) 
					buffer.append(value.substring(puntatore,puntatore+1));
				
				puntatore++;
			}
		}
		} catch(StringIndexOutOfBoundsException e) {}
		
		return paragrafo;
	}
	public static Vector<Integer> getRef(String value) {
		return getRef(value,"[a-zA-Z0-9]+");
	}
	public static Vector<Integer> getRef(String value, String param) {
		if(param.equals("Widths") || param.equals("CropBox") || param.equals("Annots"))
			return null;
		Vector<Integer> ref = null;
		String REGEX = "(\\/{0,1}"+param+" ([0-9]+) 0 R)|(\\/{0,1}"+param+"[ ]{0,}(\\[([ ]{0,}([0-9]+) 0 R){1,})[ ]{0,}\\])";
		String REGEX_2 = "(([0-9]+) 0 R)";
		
		Pattern pattern1, pattern2;
		Matcher matcher1, matcher2;
		
		pattern1 = Pattern.compile(REGEX);
		matcher1 = pattern1.matcher(value);
		while(matcher1.find()) {
			if(ref == null)
				ref = new Vector<Integer>();
			
			if(matcher1.group(2) != null) 
				ref.add(Integer.parseInt(matcher1.group(2)));
			else {
				pattern2 = Pattern.compile(REGEX_2);
				matcher2 = pattern2.matcher(matcher1.group(3));
				while(matcher2.find()) {
					ref.add(Integer.parseInt(matcher2.group(2)));
					
				}
			}
			
		}
		
		return ref;
	}
	
	public static GRPDFObject readObject(GRPDFInputStream pdf, int index, GRCrossReferenceTable xref) {
		String line;
		byte[] stream;
		StringBuffer content = new StringBuffer();
		GRPDFObject obj = new GRPDFObject(index);
		
		pdf.seek((int)(xref.getElement(index).getAddress()));
		
		line = pdf.readLine();
		
		while(!line.endsWith("%%EOF")) {
			if(line.endsWith("endobj")) {
				if(line.length() > 6)
					content.append(line.substring(0,line.indexOf("endobj")));
				
				break;
			}
				
			content.append(line+" ");
			
			if(line.endsWith("stream")) {
				int len = getLengthIndirect(content.toString(), pdf, xref);
				
				stream = new byte[len];
				
				pdf.read(stream);
				
				obj.setStream(stream);
				break;
			} else
				line = pdf.readLine();
		}
		
		
		line = content.toString();
		line = line.substring(line.indexOf("0 obj")+5);
		line = line.trim();
	
		obj.setDictionary(line);
		
		return obj;
	}
	public static GRPDFObject readObject(RandomAccessFile raf, int index, long[] xref) throws IOException {
		String line;
		byte[] stream;
		StringBuffer content = new StringBuffer();
		GRPDFObject obj = new GRPDFObject(index);
		
		raf.seek(xref[index]);
		
		line = raf.readLine();
		
		while(!line.endsWith("%%EOF")) {
			if(line.endsWith("endobj")) {
				if(line.length() > 6)
					content.append(line.substring(0,line.indexOf("endobj")));
				
				break;
			}
				
			content.append(line+" ");
			
			
			if(line.endsWith("stream")) {
				int len = getLength(content.toString(), raf, xref);
				//System.out.println("OGGETTO: "+index+" - LENGTHSTREAM: "+len);
				stream = new byte[len];
				
				raf.read(stream);
				
				obj.setStream(stream);
				break;
			} else
				line = raf.readLine();
		}
		
		
		line = content.toString();
		line = line.substring(line.indexOf("0 obj")+5);
		line = line.trim();
		obj.setDictionary(line);
		
		return obj;
	}
	public static GRPDFObject readObject(RandomAccessFile raf, int index, GRXrefTable xref) throws IOException {
		String line;
		byte[] stream;
		StringBuffer content = new StringBuffer();
		GRPDFObject obj = new GRPDFObject(index);
		
		raf.seek(xref.getAddress(index));
		
		line = raf.readLine();
		while(!line.endsWith("%%EOF")) {
			if(line.endsWith("endobj")) {
				if(line.length() > 6)
					content.append(line.substring(0,line.indexOf("endobj")));
				
				break;
			}
				
			content.append(line+" ");
			
			
			if(line.endsWith("stream")) {
				int len = getLength(content.toString(), raf, xref);
				//System.out.println("OGGETTO: "+index+" - LENGTHSTREAM: "+len);
				stream = new byte[len];
				
				raf.read(stream);
				
				obj.setStream(stream);
				break;
			} else
				line = raf.readLine();
		}
		
		
		line = content.toString();
		line = line.substring(line.indexOf("0 obj")+5);
		line = line.trim();
		obj.setDictionary(line);
		
		return obj;
	}
	
	public static GRPDFObject readObject(RandomAccessFile raf, int index) throws IOException {
		String line;
		byte[] stream;
		StringBuffer content = new StringBuffer();
		GRPDFObject obj = new GRPDFObject(index);
		
		line = raf.readLine();
		while(!line.endsWith("%%EOF")) {
			if(line.endsWith("endobj")) {
				if(line.length() > 6)
					content.append(line.substring(0,line.indexOf("endobj")));
				
				break;
			}
				
			content.append(line+" ");
			
			
			if(line.endsWith("stream")) {
				//int len = getLength(content.toString());
				int len = 1;
				stream = new byte[len];
				
				raf.read(stream);
				
				obj.setStream(stream);
				break;
			} else
				line = raf.readLine();
		}
		
		
		line = content.toString();
		line = line.substring(line.indexOf("0 obj")+5);
		line = line.trim();
		obj.setDictionary(line);
		
		return obj;
		
	}
	
	private static int getLengthIndirect(String value, GRPDFInputStream pdf, GRCrossReferenceTable xref) {
		Pattern pattern;
		Matcher matcher;
		
		pattern = Pattern.compile(REG_STREAM_LENGTH);
		matcher = pattern.matcher(value);
		while(matcher.find()) {
			if(matcher.group(3) == null)
				return Integer.parseInt(matcher.group(2));
			
			// Salva il puntatore al file
			int oldPointer = pdf.getFilePointer();
			
			// Legge l'oggetto puntato e cerca di recuperare la dimensione
			GRPDFObject pdfObject = readObject(pdf, Integer.parseInt(matcher.group(2)), xref);
			
			// Ripristina il puntatore al file
			pdf.seek(oldPointer);
			return Integer.parseInt(pdfObject.getDictionary());
		}
		return -1;
	}
	private static int getLength(String value, RandomAccessFile raf, long[] xref) throws IOException {
		Pattern pattern;
		Matcher matcher;
		
		pattern = Pattern.compile(REG_STREAM_LENGTH);
		matcher = pattern.matcher(value);
		while(matcher.find()) {
			if(matcher.group(3) == null)
				return Integer.parseInt(matcher.group(2));
			
			// Salva il puntatore al file
			long oldPointer = raf.getFilePointer();
			
			// Legge l'oggetto puntato e cerca di recuperare la dimensione
			GRPDFObject pdfObject = readObject(raf, Integer.parseInt(matcher.group(2)), xref);
			
			// Ripristina il puntatore al file
			raf.seek(oldPointer);
			return Integer.parseInt(pdfObject.getDictionary());
		}
		return -1;
	}
	private static int getLength(String value, RandomAccessFile raf, GRXrefTable xref) throws IOException {
		Pattern pattern;
		Matcher matcher;
		
		pattern = Pattern.compile(REG_STREAM_LENGTH);
		matcher = pattern.matcher(value);
		while(matcher.find()) {
			if(matcher.group(3) == null)
				return Integer.parseInt(matcher.group(2));
			
			// Salva il puntatore al file
			long oldPointer = raf.getFilePointer();
			
			// Legge l'oggetto puntato e cerca di recuperare la dimensione
			GRPDFObject pdfObject = readObject(raf, Integer.parseInt(matcher.group(2)), xref);
			
			// Ripristina il puntatore al file
			raf.seek(oldPointer);
			return Integer.parseInt(pdfObject.getDictionary());
		}
		return -1;
	}
	
	public static byte[] ASCIIHexDecode(final byte in[]) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean first = true;
        int n1 = 0;
        for (int k = 0; k < in.length; ++k) {
            int ch = in[k] & 0xff;
            if (ch == '>')
                break;
            if (isWhiteSpace(ch))
                continue;
            int n = getHex(ch);
            if (n == -1)
                break;//throw new RuntimeException(MessageLocalization.getComposedMessage("illegal.character.in.asciihexdecode"));
            if (first)
                n1 = n;
            else
                out.write((byte)((n1 << 4) + n));
            first = !first;
        }
        if (!first)
            out.write((byte)(n1 << 4));
        return out.toByteArray();
    }
	public static byte[] ASCII85Decode(final byte in[]) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int state = 0;
        int chn[] = new int[5];
        for (int k = 0; k < in.length; ++k) {
            int ch = in[k] & 0xff;
            if (ch == '~')
                break;
            if (isWhiteSpace(ch))
                continue;
            if (ch == 'z' && state == 0) {
                out.write(0);
                out.write(0);
                out.write(0);
                out.write(0);
                continue;
            }
            if (ch < '!' || ch > 'u')
                break;//throw new RuntimeException(MessageLocalization.getComposedMessage("illegal.character.in.ascii85decode"));
            chn[state] = ch - '!';
            ++state;
            if (state == 5) {
                state = 0;
                int r = 0;
                for (int j = 0; j < 5; ++j)
                    r = r * 85 + chn[j];
                out.write((byte)(r >> 24));
                out.write((byte)(r >> 16));
                out.write((byte)(r >> 8));
                out.write((byte)r);
            }
        }
        int r = 0;
        // We'll ignore the next two lines for the sake of perpetuating broken PDFs
//        if (state == 1)
//            throw new RuntimeException(MessageLocalization.getComposedMessage("illegal.length.in.ascii85decode"));
        if (state == 2) {
            r = chn[0] * 85 * 85 * 85 * 85 + chn[1] * 85 * 85 * 85 + 85 * 85 * 85  + 85 * 85 + 85;
            out.write((byte)(r >> 24));
        }
        else if (state == 3) {
            r = chn[0] * 85 * 85 * 85 * 85 + chn[1] * 85 * 85 * 85  + chn[2] * 85 * 85 + 85 * 85 + 85;
            out.write((byte)(r >> 24));
            out.write((byte)(r >> 16));
        }
        else if (state == 4) {
            r = chn[0] * 85 * 85 * 85 * 85 + chn[1] * 85 * 85 * 85  + chn[2] * 85 * 85  + chn[3] * 85 + 85;
            out.write((byte)(r >> 24));
            out.write((byte)(r >> 16));
            out.write((byte)(r >> 8));
        }
        return out.toByteArray();
    }
	/* DA IMPLEMENTARE
	public static byte[] LZWDecode(final byte in[]) {
		
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        LZWDecoder lzw = new LZWDecoder();
        lzw.decode(in, out);
        return out.toByteArray();
    }
    */
	private static boolean isWhiteSpace(int value) {
		if(value == 0 || value == 9 || value == 10 || value == 12 || value == 13 || value == 32)
			return true;
		
		return false;
	}
	private static boolean isDelimiter(int value) {
		if(value == '(' || value == ')' || value == '<' || value == '>' || value == '[' || value == ']' || value == '/' || value == '%')
			return true;
		
		return false;
	}
	private static int getHex(int v) {
        if (v >= '0' && v <= '9')
            return v - '0';
        if (v >= 'A' && v <= 'F')
            return v - 'A' + 10;
        if (v >= 'a' && v <= 'f')
            return v - 'a' + 10;
        return -1;
    }
	
	public static long bytesToLong(byte[] b) {
	    long result = 0;
	    for (int i = 0; i < b.length; i++) {
	        result <<= 8;
	        result |= (b[i] & 0xFF);
	    }
	    return result;
	}
}
