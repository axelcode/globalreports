/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.variable.chart.GRChartVoice
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

import com.globalreports.engine.structure.GRColor;

public class GRChartVoice {
	private String label;
	private double value;
	
	private GRColor colorStroke;
	private GRColor colorFill;
	
	public GRChartVoice(String label, double value) {
		this.label = label;
		this.value = value;
		
		colorStroke = new GRColor(0.0,0.0,0.0);
		colorFill = new GRColor(0.0,0.0,0.0);
		
	}
	public GRChartVoice(String label, double value, String colorStroke, String colorFill) {
		String[] cs = colorStroke.split(" ");
		String[] cf = colorFill.split(" ");
		
		this.label = label;
		this.value = value;
		
		this.colorStroke = new GRColor(Double.parseDouble(cs[0]) / 255, Double.parseDouble(cs[1]) / 255, Double.parseDouble(cs[2]) / 255);
		this.colorFill = new GRColor(Double.parseDouble(cf[0]) / 255, Double.parseDouble(cf[1]) / 255, Double.parseDouble(cf[2]) / 255);
		
	}
	public GRChartVoice(String label, double value, double colorFillRED, double colorFillGREEN, double colorFillBLUE) {
		this(label,value,0.0,0.0,0.0,colorFillRED,colorFillGREEN,colorFillBLUE);
		
	}
	public GRChartVoice(String label, double value, double colorStrokeRED, double colorStrokeGREEN, double colorStrokeBLUE, double colorFillRED, double colorFillGREEN, double colorFillBLUE) {
		this.label = label;
		this.value = value;
		
		colorStroke = new GRColor(colorStrokeRED, colorStrokeGREEN, colorStrokeBLUE);
		colorFill = new GRColor(colorFillRED, colorFillGREEN, colorFillBLUE);
		
	}
	public String getLabel() {
		return label;
	}
	public double getValue() {
		return value;
	}
	public GRColor getColorStroke() {
		return colorStroke;
	}
	public String getColorStrokeString() {
		return colorStroke.getRed() + " " + colorStroke.getGreen() + " " + colorStroke.getBlue();
	}
	public GRColor getColorFill() {
		return colorFill;
	}
	public String getColorFillString() {
		return colorFill.getRed() + " " + colorFill.getGreen() + " " + colorFill.getBlue();
	}
	public double getColorStrokeRED() {
		return colorStroke.getRed();
	}
	public double getColorStrokeGREEN() {
		return colorStroke.getGreen();
	}
	public double getColorStrokeBLUE() {
		return colorStroke.getBlue();
	}
	public double getColorFillRED() {
		return colorFill.getRed();
	}
	public double getColorFillGREEN() {
		return colorFill.getGreen();
	}
	public double getColorFillBLUE() {
		return colorFill.getBlue();
	}
}
