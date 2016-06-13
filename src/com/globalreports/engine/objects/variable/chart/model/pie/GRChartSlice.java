package com.globalreports.engine.objects.variable.chart.model.pie;

import com.globalreports.engine.objects.variable.chart.GRChartVectorial;
import com.globalreports.engine.objects.variable.chart.GRChartVoice;
import com.globalreports.engine.objects.variable.chart.model.GRChartPie;
import com.globalreports.engine.structure.GRColor;

public class GRChartSlice {
	private static final int 	DX_SX	= 100;
	private static final int	DX_DX	= 200;
	private static final int	SX_SX	= 300;
	private static final int	SX_DX	= 400;
	
	private int progressivo;
	private int angoloStart;
	private int angoloEnd;
	
	private GRChartPie pieFather;
	
	private double widthStroke;
	private GRColor colorStroke;
	private GRColor colorFill;
	
	private int priority;
	private int quadranteS;
	private int quadranteE;
	
	public GRChartSlice(int progressivo, int angoloStart, int angoloEnd, double widthStroke, GRColor colorStroke, GRColor colorFill, GRChartPie pieFather) {
		this.progressivo = progressivo;
		this.angoloStart = angoloStart;
		this.angoloEnd = angoloEnd;
		
		this.widthStroke = widthStroke;
		this.colorStroke = colorStroke;
		this.colorFill = colorFill;
		
		this.pieFather = pieFather;
		
		this.setPriority();
	}
	
	private void setPriority() {
		int as = angoloStart;
		int ae = angoloEnd;
		
		while(as > 360)
			as -= 360;
		while(ae > 360)
			ae -= 360;
		
		if(as >= 0 && as < 90) 
			quadranteS = 1;
		else if(as >= 90 && as < 180)
			quadranteS = 2;
		else if(as >= 180 && as < 270)
			quadranteS = 3;
		else
			quadranteS = 4;
		
		if(ae >= 0 && ae < 90) 
			quadranteE = 1;
		else if(ae >= 90 && ae < 180)
			quadranteE = 2;
		else if(ae >= 180 && ae < 270)
			quadranteE = 3;
		else
			quadranteE = 4;
		
		if(quadranteS == 1 || quadranteS == 4) {
			if(quadranteE == 1 || quadranteE == 4)
				priority = DX_DX + (100 - progressivo);
			else
				priority = DX_SX + progressivo;
		} else {
			if(quadranteE == 2 || quadranteE ==3) 
				priority = SX_SX + progressivo;
			else 
				priority = SX_DX + progressivo;
		}
	}
	
	public String draw3D(double x, double y) {
		StringBuffer content = new StringBuffer();
		String slice;
		
		double CONST_RAD = 3.141592 / 180;
		double sTeta = Math.sin(-CONST_RAD * 0);
		double cTeta = Math.cos(-CONST_RAD * 0);
		
		double X1, X2;
		double Y1, Y2;
		
		
		/* TEST 
		double rad = CONST_RAD * ((angoloEnd - angoloStart) / 2);
		X2 = 15 * Math.cos(rad);
		Y2 = (15 * 0.35) * Math.sin(rad);
		
		X1 = X2 * cTeta + Y2 * sTeta;
		Y1 = -X2 * sTeta + Y2 * cTeta;
		  
		x = X1;
		y = Y1;
		*/
		
		System.out.println("QUADRANTE S: "+quadranteS);
		System.out.println("QUADRANTE E: "+quadranteE);
		System.out.println("ANGOLO START: "+angoloStart);
		System.out.println("ANGOLO END: "+angoloEnd);
		
		System.out.println("PRIORITY: "+priority);
		
		switch(quadranteS) {
		case 1:
			slice = getFace(x, y, angoloStart);
			content.append(slice);
			
			if(quadranteE == 2 || quadranteE == 3) {
				slice = getFace(x, y, angoloEnd);
				content.append(slice);
			} 
			
			if(quadranteE == 3 || quadranteE == 4) {
				slice = getCircle(x, y, 180, angoloEnd);
				content.append(slice);
			}
			
			slice = GRChartVectorial.drawArc(x, y, pieFather.getRadius(), angoloStart, angoloEnd, 0.35, widthStroke, colorStroke, colorFill);	
			content.append(slice);
			
			break;
			
		case 2:
			if(quadranteE == 2 || quadranteE == 3) {
				slice = getFace(x, y, angoloEnd);
				content.append(slice);
			}
			
			if(quadranteE == 3 || quadranteE == 4) {
				slice = getCircle(x, y, 180, angoloEnd);
				content.append(slice);
			}
			
			if(quadranteE == 1) {
				slice = getCircle(x, y, 180, 360);
				content.append(slice);
			}
			
			slice = GRChartVectorial.drawArc(x, y, pieFather.getRadius(), angoloStart, angoloEnd, 0.35, widthStroke, colorStroke, colorFill);	
			content.append(slice);
			
			break;
			
		case 3:
			if(quadranteE == 2 || quadranteE == 3) {
				slice = getFace(x, y, angoloEnd);
				content.append(slice);
			}
			
			if(quadranteE == 3 || quadranteE == 4) {
				slice = getCircle(x, y, angoloStart, angoloEnd);
				content.append(slice);
			}
			
			if(quadranteE == 1 || quadranteE == 2) {
				slice = getCircle(x, y, angoloStart, 360);
				content.append(slice);
			}
			
			slice = GRChartVectorial.drawArc(x, y, pieFather.getRadius(), angoloStart, angoloEnd, 0.35, widthStroke, colorStroke, colorFill);	
			content.append(slice);
			
			break;
			
		case 4:
			slice = getFace(x, y, angoloStart);
			content.append(slice);
			
			if(quadranteE == 2 || quadranteE == 3) {
				slice = getFace(x, y, angoloEnd);
				content.append(slice);
			}
			
			if(quadranteE == 1 || quadranteE == 2 || quadranteE == 3) {
				slice = getCircle(x, y, angoloStart, 360);
				content.append(slice);
			}
			
			if(quadranteE == 3) {
				slice = getCircle(x, y, 180, angoloEnd-360);
				content.append(slice);
			}
			
			if(quadranteE == 4) {
				slice = getCircle(x, y, angoloStart, angoloEnd);
				content.append(slice);
			}
			
			
			
			slice = GRChartVectorial.drawArc(x, y, pieFather.getRadius(), angoloStart, angoloEnd, 0.35, widthStroke, colorStroke, colorFill);	
			content.append(slice);
			
			break;
			
		}
				
		
		return content.toString();
	}
	
	private String getFace(double x, double y, int angolo) {
		StringBuffer content = new StringBuffer();
		
		double CONST_RAD = 3.141592 / 180;
		double sTeta = Math.sin(-CONST_RAD * 0);
		double cTeta = Math.cos(-CONST_RAD * 0);
		
		double X1, X2;
		double Y1, Y2;
		
		double rad = CONST_RAD * angolo;
		X2 = pieFather.getRadius() * Math.cos(rad);
		Y2 = (pieFather.getRadius() * 0.35) * Math.sin(rad);
		
		X1 = X2 * cTeta + Y2 * sTeta;
		Y1 = -X2 * sTeta + Y2 * cTeta;
		
		content.append("q\n");
		content.append(widthStroke+" w\n");
		content.append(colorStroke.getRed()+" "+colorStroke.getGreen()+" "+colorStroke.getBlue()+" RG\n");
		content.append(colorFill.getRed()+" "+colorFill.getGreen()+" "+colorFill.getBlue()+" rg\n");
		
		content.append((x + X1)+" "+(y + Y1)+" m\n");
		content.append(x+" "+y+" l\n");
		content.append(x+" "+(y-pieFather.HEIGHT_PIE)+" l\n");
		content.append((x + X1)+" "+(y + Y1 - pieFather.HEIGHT_PIE)+" l\n");
		content.append((x + X1)+" "+(y + Y1)+" l\n");
		
		content.append("B\n");
		content.append("Q\n");
		
		return content.toString();
	}
	private String getCircle(double x, double y, int as, int ae) {
		StringBuffer content = new StringBuffer();
		
		double CONST_RAD = 3.141592 / 180;
		double sTeta = Math.sin(-CONST_RAD * 0);
		double cTeta = Math.cos(-CONST_RAD * 0);
		
		double X1, X2;
		double Y1, Y2;
		
		content.append("q\n");
		content.append(widthStroke+" w\n");
		content.append(colorStroke.getRed()+" "+colorStroke.getGreen()+" "+colorStroke.getBlue()+" RG\n");
		content.append(colorFill.getRed()+" "+colorFill.getGreen()+" "+colorFill.getBlue()+" rg\n");
		
		for(int index = as;index <= ae;index++) {
			double rad = CONST_RAD * index;
			X2 = pieFather.getRadius() * Math.cos(rad);
			Y2 = (pieFather.getRadius() * 0.35) * Math.sin(rad);
			
			X1 = X2 * cTeta + Y2 * sTeta;
			Y1 = -X2 * sTeta + Y2 * cTeta;
			
			if(index == as)
				content.append((x + X1)+" "+(y + Y1)+" m\n");
			else	
				content.append((x + X1)+" "+(y + Y1)+" l\n");
			
			if(index == ae) {
				content.append((x + X1)+" "+(y + Y1 - pieFather.HEIGHT_PIE)+" l\n");
			}
		}
		
		double yNew = y - pieFather.HEIGHT_PIE;
		for(int index = ae;index >= as;index--) {
			double rad = GRChartVectorial.radianti(index);
			X2 = pieFather.getRadius() * Math.cos(rad);
			Y2 = (pieFather.getRadius() * 0.35) * Math.sin(rad);
							
			X1 = X2 * cTeta + Y2 * sTeta;
			Y1 = -X2 * sTeta + Y2 * cTeta;
			
			content.append((x + X1)+" "+(yNew + Y1)+" l\n");
			
			if(index == as) {
				content.append((x + X1)+" "+(yNew + Y1 + pieFather.HEIGHT_PIE)+" l\n");
			}
		}
		
		content.append("B\n");
		content.append("Q\n");
		return content.toString();
	}
	
	public int getPriority() {
		return priority;
	}
}
