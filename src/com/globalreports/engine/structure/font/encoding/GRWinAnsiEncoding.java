/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.font.encoding.GRWinAnsiEncoding
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

public class GRWinAnsiEncoding extends GREncoding {
	
	public final String[] octal = {"00", "01", "02", "03", "04", "05", "06", "07",
								   "010","011","012","013","014","015","016","017",
								   "020","021","022","023","024","025","026","027",
								   "030","031","032","033","034","035","036","037",
								   "040","041","042","043","044","045","046","047",
								   "050","051","052","053","054","055","056","057",
								   "060","061","062","063","064","065","066","067",
								   "070","071","072","073","074","075","076","077",
								   "100","101","102","103","104","105","106","107",
								   "110","111","112","113","114","115","116","117",
								   "120","121","122","123","124","125","126","127",
								   "130","131","132","133","134","135","136","137",
								   "140","141","142","143","144","145","146","147",
								   "150","151","152","153","154","155","156","157",
								   "160","161","162","163","164","165","166","167",
								   "170","171","172","173","174","175","176","177",
								   "200","201","202","203","204","205","206","207",
								   "210","211","212","213","214","215","216","217",
								   "220","221","222","223","224","225","226","227",
								   "230","231","232","233","234","235","236","237",
								   "240","241","242","243","244","245","246","247",
								   "250","251","252","253","254","255","256","257",
								   "260","261","262","263","264","265","266","267",
								   "270","271","272","273","274","275","276","277",
								   "300","301","302","303","304","305","306","307",
								   "310","311","312","313","314","315","316","317",
								   "320","321","322","323","324","325","326","327",
								   "330","331","332","333","334","335","336","337",
								   "340","341","342","343","344","345","346","347",
								   "350","351","352","353","354","355","356","357",
								   "360","361","362","363","364","365","366","367",
								   "370","371","372","373","374","375","376","377"};
	
	public final String[] octal2 = {"045","050","051","057","074","076","173","175",
								   "225","260","340","341","350","351","354","355",
								   "362","363","371","372","012","272"};
	public final int[] dec = {37,40,41,47,60,62,123,125,149,176,224,225,232,233,236,237,
	                          242,243,249,250,-1,186};
	
	public GRWinAnsiEncoding() {
		type = "WinAnsiEncoding";
	}
	public int fromOctalToDecimal(String charcode) {
		if(charcode.equals("012"))
			return -1;
		else {
			for(int i = 0;i < octal.length;i++) {
				if(charcode.equals(octal[i]))
					return i;
			}
		}
		
		return 0;
	}
	
}
