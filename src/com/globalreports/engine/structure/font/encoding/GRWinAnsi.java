/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.font.encoding.GRWinAnsi
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

import com.globalreports.engine.structure.font.ttf.GRCMap;

public class GRWinAnsi {
	public static final char[] WINANSI_ENCODING = {
	        // not used until char 32
	        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	           0, 0, 0, 0, 0, 0, 0, 0, 0, // 0x20
	        ' ', '\u0021', '\"', '\u0023', '$', '%', '&', '\'', '(', ')', '*', '+', ',',
	             '\u002d', '\u002e', '/', // 0x30
	        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=',
	             '>', '?', '@', // 0x40
	        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
	             'O', // 0x50
	        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '\u005b', '\\',
	             '\u005d', '^', '_', // 0x60
	        '\u2018', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
	             'n', 'o', // 0x70
	        'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '\u007b', '\u007c', '\u007d',
	             '\u007e', '\u2022', // 0x80
	        '\u20ac', '\u2022', '\u201a', '\u0192', '\u201e', '\u2026', '\u2020',
	             '\u2021', '\u02c6', '\u2030', '\u0160', '\u2039', '\u0152', '\u2022',
	             '\u017d', '\u2022', // 0x90
	        '\u2022', '\u2018',                             // quoteleft
	        '\u2019',                                       // quoteright
	        '\u201c',                                       // quotedblleft
	        '\u201d',                                       // quotedblright
	        '\u2022',                                       // bullet
	        '\u2013',                                       // endash
	        '\u2014',                                       // emdash
	        '~',
	        '\u2122',                                       // trademark
	        '\u0161', '\u203a', '\u0153', '\u2022', '\u017e', '\u0178', // 0xA0
	             ' ', '\u00a1', '\u00a2', '\u00a3', '\u00a4', '\u00a5',
	             '\u00a6', '\u00a7', '\u00a8', '\u00a9', '\u00aa', '\u00ab',
	             '\u00ac', '\u00ad',  '\u00ae', '\u00af', // 0xb0
	        '\u00b0', '\u00b1', '\u00b2', '\u00b3', '\u00b4',
	             '\u00b5',                      // This is hand-coded, the rest is assumption
	        '\u00b6',                           // and *might* not be correct...
	        '\u00b7', '\u00b8', '\u00b9', '\u00ba', '\u00bb', '\u00bc', '\u00bd',
	             '\u00be', '\u00bf', // 0xc0
	        '\u00c0', '\u00c1', '\u00c2', '\u00c3', '\u00c4', '\u00c5', // Aring
	        '\u00c6',                                            // AE
	        '\u00c7', '\u00c8', '\u00c9', '\u00ca', '\u00cb', '\u00cc',
	             '\u00cd', '\u00ce', '\u00cf', // 0xd0
	        '\u00d0', '\u00d1', '\u00d2', '\u00d3', '\u00d4', '\u00d5',
	             '\u00d6', '\u00d7', '\u00d8',    // Oslash
	        '\u00d9', '\u00da', '\u00db', '\u00dc', '\u00dd', '\u00de',
	             '\u00df', // 0xe0
	        '\u00e0', '\u00e1', '\u00e2', '\u00e3', '\u00e4', '\u00e5', // aring
	        '\u00e6',                                            // ae
	        '\u00e7', '\u00e8', '\u00e9', '\u00ea', '\u00eb', '\u00ec',
	             '\u00ed', '\u00ee', '\u00ef', // 0xf0
	        '\u00f0', '\u00f1', '\u00f2', '\u00f3', '\u00f4', '\u00f5',
	             '\u00f6', '\u00f7', '\u00f8', '\u00f9', '\u00fa', '\u00fb',
	             '\u00fc', '\u00fd', '\u00fe', '\u00ff'
	    };
	
	private int[] width;
	private int[] advanceWidth;
	private float scale;
	private String metrics;
	
	public GRWinAnsi(int[] advanceW, float scale) {
		this.advanceWidth = advanceW;
		this.scale = scale;
		
		init();
	}
	private void init() {
		width = new int[WINANSI_ENCODING.length];
		for(int i = 0;i < WINANSI_ENCODING.length;i++) {
			width[i] = (int)(advanceWidth[0] * scale);
			
		}
	}
	
	public void setMetrics(GRCMap cmap) {
		if(cmap.getFormat() != GRCMap.FORMAT_4) {
			metrics = null;
			return;
		}
		
		metrics = "[";
		for(int i = 0;i < WINANSI_ENCODING.length;i++) {
			Integer index = cmap.getIndexGlyph(WINANSI_ENCODING[i]);
			
			if(index != null) {
				width[i] = (int)(advanceWidth[index] * scale);
			} 
			metrics = metrics + width[i]+" ";
		}
		metrics = metrics + "]";
	}
	public int getMetrics(int index) {
		return width[index];
	}
	public String getMetrics() {
		return metrics;
	}
	public int[] getArrayMetrics() {
		return width;
	}
}
