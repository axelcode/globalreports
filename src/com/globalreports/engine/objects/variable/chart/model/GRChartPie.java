/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.variable.chart.model.GRChartPie
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
package com.globalreports.engine.objects.variable.chart.model;

import java.awt.geom.Point2D;
import java.util.Vector;

import com.globalreports.engine.err.GRValidateException;
import com.globalreports.engine.objects.GRObject;
import com.globalreports.engine.objects.variable.GRChart;
import com.globalreports.engine.objects.variable.chart.GRChartVectorial;
import com.globalreports.engine.objects.variable.chart.GRChartVoice;
import com.globalreports.engine.objects.variable.chart.model.pie.GRChartSlice;
import com.globalreports.engine.structure.GRMeasures;
import com.globalreports.engine.structure.grpdf.GRContext;

public class GRChartPie extends GRChart {
	public static final double HEIGHT_PIE	= 10;	// 10 MM
	
	private double radius;
	
	public GRChartPie(short view) {
		super(view);
	}
	
	public Vector<String> draw(GRContext grcontext) throws GRValidateException {
		Vector<String> stream = new Vector<String>();

		if(view == GRChart.VIEW_2D)
			stream.add(draw2D(grcontext));
		else
			stream.add(draw3D(grcontext));
		
		return stream;
	}
	private Point2D.Double getCenter(double left, double top) {
		double lato;
		
		/* Recupera il quadrato circoscritto del cerchio*/
		if(width <= height)
			lato = width;
		else
			lato = height;
		
		radius = lato / 2;
		
		/* Adesso recupera il centro del cerchio */
		Point2D.Double p2d = new Point2D.Double(left+radius, top-radius);
		
		return p2d;
	}
	private String draw2D(GRContext grcontext) throws GRValidateException {
		StringBuffer content = new StringBuffer();
		double left,top,height;
		
		double totalValue = 0.0;
		
		left = grcontext.getLeft();
		if(this.getHPosition() == GRObject.HPOSITION_ABSOLUTE) {
			top = grcontext.getTop();
		} else {
			top = grcontext.getHPosition();
		}
		
		height = top;
		
		left = GRMeasures.arrotonda(left+this.getLeft());
		top = GRMeasures.arrotonda(top - this.getTop());
		
		Point2D.Double p = this.getCenter(left, top);
		
		// Cicla per tutte le voci
		for(int i = 0;i < grvoice.size();i++) {
			totalValue += grvoice.get(i).getValue();
		}
		
		// Adesso disegna tutti gli spicchi
		int angoloStart = gap;
		int angoloEnd = 0;
		for(int i = 0;i < grvoice.size();i++) {
			GRChartVoice refvoice = grvoice.get(i);
			
			int gradi = (int)(refvoice.getValue() * 360 / totalValue);
			
			if(i == (grvoice.size() - 1))
				angoloEnd = 360 + gap;
			else
				angoloEnd = angoloStart + gradi;
			
			String slice = GRChartVectorial.drawArc(p.getX(), p.getY(), radius, angoloStart, angoloEnd, 1.0, this.getWidthStroke(), refvoice.getColorStroke(), refvoice.getColorFill());
			content.append(slice);
			
			angoloStart = angoloEnd;
		}
		
		// Border
		/*
		content.append("q\n");
		content.append(this.getWidthStroke()+" w\n");
		content.append("0.0 0.0 0.0 RG\n");
		content.append(left+" "+top+" "+this.getWidth()+" "+this.getHeight()+" re\n");
		content.append("S\n");
		*/
		
		/* TEST - Aggiunta del testo */
		/*
		content.append("BT\n");
		content.append("/GRFSYS1 8 Tf\n");
		content.append((left + (radius * 2))+" "+(top-28.34)+" Td\n");
		content.append("(Hello World) Tj\n");
		content.append("ET\n");
		*/
		
		//System.out.println(content.toString());
		return content.toString();
	}
	private String draw3D(GRContext grcontext) throws GRValidateException {
		StringBuffer content = new StringBuffer();
		double left,top,height;
		
		double totalValue = 0.0;
		
		left = grcontext.getLeft();
		if(this.getHPosition() == GRObject.HPOSITION_ABSOLUTE) {
			top = grcontext.getTop();
		} else {
			top = grcontext.getHPosition();
		}
		
		height = top;
		
		left = GRMeasures.arrotonda(left+this.getLeft());
		top = GRMeasures.arrotonda(top - this.getTop());
		
		Point2D.Double p = this.getCenter(left, top);
		
		double CONST_RAD = 3.141592 / 180;
		double sTeta = Math.sin(-CONST_RAD * 0);
		double cTeta = Math.cos(-CONST_RAD * 0);
		
		double X1, X2;
		double Y1, Y2;
		
		// Cicla per tutte le voci
		// Questa la si potrà eliminare conteggiando il totale
		// in fase di aggiunta delle singole voci
		for(int i = 0;i < grvoice.size();i++) {
			totalValue += grvoice.get(i).getValue();
		}
		
		// Adesso crea gli oggetti che conterranno le singole voci
		Vector<GRChartSlice> chartSlice = new Vector<GRChartSlice>();
		int angoloStart = gap;
		int angoloEnd = 0;
		for(int i = 0;i < grvoice.size();i++) {
			GRChartVoice refvoice = grvoice.get(i);
			
			int gradi = (int)(refvoice.getValue() * 360 / totalValue);
			
			if(i == (grvoice.size() - 1))
				angoloEnd = 360 + gap;
			else
				angoloEnd = angoloStart + gradi;
			
			chartSlice.add(new GRChartSlice((i+1),angoloStart, angoloEnd, widthStroke,refvoice.getColorStroke(),refvoice.getColorFill(), this));
		
			angoloStart = angoloEnd;
		}
		
		// Ordina le slice in base alle priorità di rendering
		for(int x = 0;x < chartSlice.size()-1;x++) {
			for(int y = (x+1);y < chartSlice.size();y++) {
				if(chartSlice.get(x).getPriority() > chartSlice.get(y).getPriority()) {
					GRChartSlice tempSliceX = chartSlice.get(x);
					GRChartSlice tempSliceY = chartSlice.get(y);
					
					
					chartSlice.remove(y);
					chartSlice.remove(x);
					
					chartSlice.insertElementAt(tempSliceY, x);
					chartSlice.insertElementAt(tempSliceX, y);
					
				}
			}
		}
		
		String slice;
		for(int i = 0;i < grvoice.size();i++) {
			slice = chartSlice.get(i).draw3D(p.getX(),p.getY());
			content.append(slice);
		}
		
		return content.toString();
	}
	
	public double getRadius() {
		return radius;
	}
	
	public short getTypeChart() {
		return GRChart.TYPECHART_PIE;
	}
	
}
