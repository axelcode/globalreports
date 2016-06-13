/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.font.encoding.GREncode_ISO8859
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
package com.globalreports.engine.structure.font.encoding;

public class GREncode_ISO8859 {
	public static String lineASCIIToOct(String value) {
		/*
		StringBuffer buffer = new StringBuffer();
		
		for(int i = 0;i < value.length();i++) {
			buffer.append(GREncode_ISO8859.fromASCIIToOct(value.codePointAt(i)));
			
		}
		
		return buffer.toString();
		*/
		StringBuffer buffer = new StringBuffer();
		int variable = 0;
		
		for(int i = 0;i < value.length();i++) {
			if(value.codePointAt(i) == 92) {
				buffer.append(value.substring(i,i+4));
				i = i + 3;
			} else {
				
		
			if(value.codePointAt(i) == 123) {
				variable++;
				buffer.append(String.valueOf((char)value.codePointAt(i)));
			} else if(value.codePointAt(i) == 125) {
				variable--;
				buffer.append(String.valueOf((char)value.codePointAt(i)));
			} else {
				if(variable == 0)
					buffer.append(GREncode_ISO8859.fromASCIIToOct(value.codePointAt(i)));
				else
					buffer.append(String.valueOf((char)value.codePointAt(i)));
			}
			
			}
		}
		
		return buffer.toString();
	}
	
	private static String fromASCIIToOct(int value) {
		
		switch(value) {
			case 10:
				return "\\012";
			
			case 13:
				return "";
				
			case 37:
				return "\\045";
				
			case 40:
				return "\\050";
				
			case 41:
				return "\\051";
			
			case 42:
				return "\\052";
					
			case 43:
				return "\\053";
				
			case 47:
				return "\\057";
				
			case 60:
				return "\\074";
				
			case 62:
				return "\\076";
			
			case 64:
				return "\\100";
				
			case 92:
				return "\\134";
				
			case 124:
				return "\\174";
				
			case 128:
				return "\\200";
						
			case 176:
				return "\\260";
				
			case 186:
				return "\\272";
			
			case 192:
				return "\\300";
				
			case 193:
				return "\\301";
				
			case 200:
				return "\\310";
				
			case 201:
				return "\\311";
				
			case 204:
				return "\\314";
				
			case 205:
				return "\\315";
				
			case 210:
				return "\\322";
				
			case 211:
				return "\\323";
				
			case 217:
				return "\\331";
				
			case 218:
				return "\\332";
				
			case 224:
				return "\\340";
				
			case 225:
				return "\\341";
				
			case 232:
				return "\\350";
				
			case 233:
				return "\\351";
				
			case 236:
				return "\\354";
				
			case 237:
				return "\\355";
				
			case 243:
				return "\\363";
				
			case 249:
				return "\\371";
				
			case 250:
				return "\\372";
				
			case 8217:
				return "\\222";
				
			case 8364:
				return "\\200";
		}
		
		return String.valueOf((char)value);
	}
}
