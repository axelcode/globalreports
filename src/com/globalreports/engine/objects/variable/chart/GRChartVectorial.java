/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.variable.chart.GRChartVectorial
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
package com.globalreports.engine.objects.variable.chart;

import java.awt.Color;

import com.globalreports.engine.structure.GRColor;

public class GRChartVectorial {
	private static final double PI	= 3.141592;
	private static final double C	= PI / 180;
	
	public static synchronized double radianti(int gradi) {
		return C * gradi;
	}
	public static synchronized String drawArc(double x, double y, double radius, int start, int end, double ratio, double widthStroke, GRColor cStroke, GRColor cFill) {
		StringBuffer content = new StringBuffer();
		
		double sTeta = Math.sin(-C * 0);
		double cTeta = Math.cos(-C * 0);
		
		double X1, X2;
		double Y1, Y2;
		
		double cRED, cGREEN, cBLUE;
		double fRED, fGREEN, fBLUE;
		
		//Ray = Ray / .35278;
		//x = x / .35278;
		//y = y / .35278;
		//y = 841.88 - y;
		
		cRED = cStroke.getRed();
		cGREEN = cStroke.getGreen();
		cBLUE = cStroke.getBlue();
		
		fRED = cFill.getRed();
		fGREEN = cFill.getGreen();
		fBLUE = cFill.getBlue();
		
		content.append("q\n");
		content.append(widthStroke+" w\n");
		content.append(cRED+" "+cGREEN+" "+cBLUE+" RG\n");
		content.append(fRED+" "+fGREEN+" "+fBLUE+" rg\n");
		
		//if(end >= 180 && end <= 270) {
			content.append(x+" "+y+" m\n");
			for(int i = start;i <= end;i++) {
				double rad = C * i;
				X2 = radius * Math.cos(rad);
				Y2 = (radius * ratio) * Math.sin(rad);
				
				X1 = X2 * cTeta + Y2 * sTeta;
				Y1 = -X2 * sTeta + Y2 * cTeta;
				
				content.append((x + X1)+" "+(y + Y1)+" l\n");
				
			}
			content.append(x+" "+y+" l\n");
			content.append("B\n");
		//}
		
		
		
		content.append("Q\n");
		
		return content.toString();
		
	}
}
