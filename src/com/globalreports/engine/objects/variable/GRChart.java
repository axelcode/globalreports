/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.variable.GRChart
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
package com.globalreports.engine.objects.variable;

import java.awt.geom.Point2D;
import java.util.Vector;

import com.globalreports.engine.err.GRValidateException;
import com.globalreports.engine.objects.GRObject;
import com.globalreports.engine.objects.variable.chart.GRChartVectorial;
import com.globalreports.engine.objects.variable.chart.GRChartVoice;
import com.globalreports.engine.objects.variable.chart.model.GRChartBar;
import com.globalreports.engine.objects.variable.chart.model.GRChartPie;
import com.globalreports.engine.structure.GRColor;
import com.globalreports.engine.structure.GRMeasures;
import com.globalreports.engine.structure.grpdf.GRContext;

public abstract class GRChart extends GRVariableObject {
	public static final int VIEW_2D				= 1;
	public static final int VIEW_3D				= 2;
	public static final int	LEGEND_EMPTY		= 0;
	public static final int LEGEND_VISIBLE		= 1;
	
	public static final short VALUELABEL_NOTHING	= 0;
	public static final short VALUELABEL_TOP		= 1;
	public static final short VALUELABEL_INSIDE		= 2;
	public static final short VALUELABEL_BOTTOM		= 3;
	
	public static final short TYPECHART_PIE		= 1;
	public static final short TYPECHART_BAR		= 2;
	
	protected double widthStroke;
	protected double left;
	protected double top;
	protected double width;
	protected double height;
	
	protected short view;
	protected int gap;
	protected String id;
	
	protected double borderStroke;	// Perimetro di definizione del grafico
	protected short valueLabel;		// Definisce come visualizzare i singoli valori
									// all'interno del grafico
	protected short labelx;			// true/false: per visualizzare le etichette
									// sull'asse delle ascisse
	protected short labely;			// true/false: per visualizzare le etichette
									// sull'asse delle ordinate
	protected double barRatio;		// Larghezza in percentuale della barra rispetto
									// allo spazio a lei designato.
	protected String name;			// Nome associato al blocco dati nell'xml
	
	protected Vector<GRChartVoice> grvoice;
	
	protected GRChart(short view) {
		super(GRObject.TYPEOBJ_CHART);
		
		this.view = view;
		
		this.borderStroke = 0.0;
		this.valueLabel = VALUELABEL_NOTHING;
		
		this.labelx = 0;	// hide
		this.labely = 0;	// hide
		
		this.barRatio = 0.4;
		this.name = "";
	}
	
	public static GRChart createChart(short typeChart, short view) {
		GRChart grchart = null;
		
		switch(typeChart) {
			case TYPECHART_PIE:
				grchart = new GRChartPie(view);
				break;
			
			case TYPECHART_BAR:
				grchart = new GRChartBar(view);
				break;
		}
		
		return grchart;
	}
	public static short typeChartFromStringToShort(String value) {
		if(value.equals("pie")) 
			return TYPECHART_PIE;
		else if(value.equals("bar"))
			return TYPECHART_BAR;
		
		return -1;
	}
	public static short viewChartFromStringToShort(String value) {
		if(value.equals("2d"))
			return VIEW_2D;
		else if(value.equals("3d"))
			return VIEW_3D;
		
		return -1;
	}
	
	public short getView() {
		return view;
	}
	public double getLeft() {
		return left;
	}
	public void setLeft(double left) {
		this.left = left;
	}
	public double getTop() {
		return top;
	}
	public void setTop(double top) {
		this.top = top;
	}
	public double getWidth() {
		return width;
	}
	public void setWidth(double width) {
		this.width = width;
	}
	public double getHeight() {
		return height;
	}
	public void setHeight(double height) {
		this.height = height;
	}
	public void setNameXml(String value) {
		this.id = value;
		this.name = value;
	}
	public void setGap(int value) {
		this.gap = value;
	}
	public void setWidthStroke(double value) {
		this.widthStroke = value;
	}
	public double getWidthStroke() {
		return widthStroke;
	}
	public void setBorderStroke(double value) {
		this.borderStroke = value;
	}
	public double getBorderStroke() {
		return borderStroke;
	}
	public void setValueLabel(String value) {
		if(value.equals("nothing"))
			this.valueLabel = VALUELABEL_NOTHING;
		else if(value.equals("top"))
			this.valueLabel = VALUELABEL_TOP;
		else if(value.equals("inside"))
			this.valueLabel = VALUELABEL_INSIDE;
		else if(value.equals("bottom"))
			this.valueLabel = VALUELABEL_BOTTOM;
	}
	public void setValueLabel(short value) {
		this.valueLabel = value;
	}
	public short getValueLabel() {
		return valueLabel;
	}
	public void setLabelX(short value) {
		this.labelx = value;
	}
	public void setLabelX(String value) {
		if(value.equals("visible"))
			this.labelx = 1;
		else if(value.equals("hide"))
			this.labelx = 0;
	}
	public short getLabelX() {
		return labelx;
	}
	public void setLabelY(short value) {
		this.labely = value;
	}
	public void setLabelY(String value) {
		if(value.equals("visible"))
			this.labely = 1;
		else if(value.equals("hide"))
			this.labely = 0;
	}
	public short getLabelY() {
		return labely;
	}
	public int getGap() {
		return gap;
	}
	public void setBarRatio(String value) {
		
		if(value.endsWith("%")) {
			barRatio = Double.parseDouble(value.substring(0,value.length()-1));
			barRatio = barRatio / 100;
		} else {
			barRatio = Double.parseDouble(value);
		}
	}
	public void setBarRatio(double value) {
		this.barRatio = value;
	}
	public double getBarRatio() {
		return barRatio;
	}
	public void setName(String value) {
		this.name = value;
	}
	public String getName() {
		return name;
	}
	public void addVoice(String label, double value, double colorFillRED, double colorFillGREEN, double colorFillBLUE) {
		if(grvoice == null)
			grvoice = new Vector<GRChartVoice>();
		
		grvoice.add(new GRChartVoice(label, value, colorFillRED, colorFillGREEN, colorFillBLUE));
	}
	public void addVoice(String label, double value, double colorStrokeRED, double colorStrokeGREEN, double colorStrokeBLUE, double colorFillRED, double colorFillGREEN, double colorFillBLUE) {
		if(grvoice == null)
			grvoice = new Vector<GRChartVoice>();
		
		grvoice.add(new GRChartVoice(label, value, colorStrokeRED, colorStrokeGREEN, colorStrokeBLUE, colorFillRED, colorFillGREEN, colorFillBLUE));
	}
	public Vector<GRChartVoice> getVoice() {
		return grvoice;
	}
	public int getTotaleDataVoice() {
		if(grvoice == null)
			return 0;
		
		return grvoice.size();
	}
	@Override
	public double getMaxHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Vector<String> draw(GRContext grcontext) throws GRValidateException {
		Vector<String> stream = new Vector<String>();

		return stream;
	}
	
	/* Metodi astratti */
	public abstract short getTypeChart();
}
