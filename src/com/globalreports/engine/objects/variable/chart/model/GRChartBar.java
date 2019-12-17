package com.globalreports.engine.objects.variable.chart.model;

import java.util.Vector;

import com.globalreports.engine.err.GRValidateException;
import com.globalreports.engine.objects.GRObject;
import com.globalreports.engine.objects.variable.GRChart;
import com.globalreports.engine.objects.variable.chart.GRChartVoice;
import com.globalreports.engine.structure.GRMeasures;
import com.globalreports.engine.structure.font.system.GRArial;
import com.globalreports.engine.structure.grbinary.data.GRDataChart;
import com.globalreports.engine.structure.grbinary.data.GRDataList;
import com.globalreports.engine.structure.grbinary.data.GRDataVoice;
import com.globalreports.engine.structure.grpdf.GRContext;

public class GRChartBar extends GRChart {
	private double section = 5.0;
	
	public GRChartBar(short view) {
		super(view);
		
		valueLabel = VALUELABEL_TOP;
	}

	public Vector<String> draw(GRContext grcontext) throws GRValidateException {
		Vector<String> stream = new Vector<String>();

		if(view == GRChart.VIEW_2D)
			stream.add(draw2D(grcontext));
		else
			stream.add(draw3D(grcontext));
		
		return stream;
	}
	
	private String draw2D(GRContext grcontext) throws GRValidateException {
		StringBuffer content = new StringBuffer();
		double left,top,height;
		
		double totalValue = 0.0;
		double maxValue = 0.0;
		
		double topValue = 40.0;		// Valore max delle ordinate
		
		left = grcontext.getLeft();
		if(this.getHPosition() == GRObject.HPOSITION_ABSOLUTE) {
			top = grcontext.getTop();
		} else {
			top = grcontext.getHPosition();
		}
		
		height = top;	// Stronzata????
		
		left = GRMeasures.arrotonda(left+this.getLeft());
		top = GRMeasures.arrotonda(top - this.getTop() - this.getHeight());
		
		GRDataChart dataChart = grdata.getDataChart(this.getName());
		
		if(dataChart != null) {
			grvoice.clear();
			
			for(int i = 0;i < dataChart.getTotaleElement();i++) {
				GRDataVoice refVoice = dataChart.getElement(i);
				
				String l = refVoice.getValue("label");
				double v = Double.parseDouble(refVoice.getValue("value"));
				String cs = refVoice.getValue("colorstroke");
				String cf = refVoice.getValue("colorfill");
				
				GRChartVoice voice = new GRChartVoice(l,v,cs,cf);
				grvoice.add(voice);
			}
			//GRChartVoice tvoice = new GRChartVoice(dataChart.get)
		}
		
		// Definisce la larghezza totale disponibile ad ogni barra
		double widthAreaBar = width / grvoice.size();
		
		// Se richiesto disegna il rettangolo ove comparirà il grafico
		if(borderStroke > 0.0) {
			// Bordo esterno
			content.append("q\n");
			content.append(borderStroke + " w\n");
			content.append("0.0 0.0 0.0 RG\n");
			
			content.append(left+" "+top+" "+this.getWidth()+" "+this.getHeight()+" re\n");
			
			content.append("S\n");
			content.append("Q\n");
			
			// Suddivide le aree ove verranno disegnate le barre
			double leftBorder = 0;
			for(int i = 0;i < (grvoice.size()-1);i++) {
				leftBorder += widthAreaBar;
				
				content.append("q\n");
				content.append(borderStroke + " w\n");
				content.append("0.0 0.0 0.0 RG\n");
				
				content.append(GRMeasures.arrotonda(left + leftBorder)+" "+GRMeasures.arrotonda(top)+" m\n");
				content.append(GRMeasures.arrotonda(left + leftBorder)+" "+GRMeasures.arrotonda(top + super.height)+" l\n");

				content.append("S\n");
			}
			leftBorder += widthAreaBar;
			// Se è specificato un valore per la sezione inserisce anche le righe orizzontali
			if(section > 0.0) {
				double heightSection = super.height / (topValue / section);
				double topSection = top + heightSection;
				
				for(double y = section;y < topValue;y += section) {
					content.append("q\n");
					content.append(borderStroke + " w\n");
					content.append("0.0 0.0 0.0 RG\n");
					
					content.append(GRMeasures.arrotonda(left)+" "+GRMeasures.arrotonda(topSection)+" m\n");
					content.append(GRMeasures.arrotonda(left + leftBorder)+" "+GRMeasures.arrotonda(topSection)+" l\n");

					content.append("S\n");
					
					topSection += heightSection;
				}
			}
		}
		// Imposta il grcontext
		grcontext.setHPosition(top);
		grcontext.setMaxHeight(height+this.getHeight());
					
		// Procede con il disegno delle barre
		/* 
		 * La larghezza di ogni singola barra è data dalla seguente formula:
		 * 
		 * LARGHEZZA DELLO SPAZIO TOTALE A DISPOSIZIONE DELLA SINGOLA BARRA * RATIO
		 */
		double widthBar = barRatio * widthAreaBar;
		
		double leftBar = 0;
		
		for(int i = 0;i < grvoice.size();i++) {
			double leftTemp = left + leftBar + ((widthAreaBar - widthBar) / 2);
			
			content.append("q\n");
			content.append("0.5 w\n");
			content.append(grvoice.get(i).getColorStrokeString()+" RG\n");
			
			/*
			 * L'altezza della barra è data dalla seguente formula:
			 * 
			 * (VALORE DA RAPPRESENTARE * ALTEZZA MAX DEL PERIMETRO) / VALORE MAX SULL'ASSE DELLE ORDINATE
			 */
			double hTemp = (grvoice.get(i).getValue() * this.getHeight()) / topValue;
			
			content.append(leftTemp+" "+top+" "+widthBar+" "+hTemp+" re\n");
			content.append(grvoice.get(i).getColorFillString()+" rg\n");
			content.append("B\n");
			content.append("Q\n");
			
			if(valueLabel != VALUELABEL_NOTHING) {
				content.append("70 Tz\n");
				content.append("BT\n");
				content.append("/GRFSYS1 7 Tf\n");
				
				content.append(leftTemp + " " + (top + hTemp+2)+" Td\n");
				content.append("("+grvoice.get(i).getValue()+") Tj\n");
				
				content.append("ET\n");
				content.append("100 Tz \n");
			}
			
			leftBar += widthAreaBar;
		}
		
		// Verifica se deve renderizzare anche ascisse e ordinate
		if(labelx == 1) {
			
			double leftLabel = left + (widthAreaBar / 2);
			for(int x = 0;x < grvoice.size();x++) {
				content.append("BT\n");
				content.append("/GRFSYS1 6 Tf\n");
				
				//content.append(cTeta+" "+sTeta+" "+(sTeta * -1)+" "+cTeta+" "+leftLabel+" "+(top-5)+" Tm\n");
				//content.append("("+grvoice.get(x).getLabel()+") Tj\n");
				
				
				String t = getLabelFormatted(grvoice.get(x).getLabel(),leftLabel,top,-90);
				content.append(t);
				
				content.append("ET\n");
				
				leftLabel += widthAreaBar;
			}
		}
		if(labely == 1) {
			double heightSection = super.height / (topValue / section);
			double topLabel = (top - 2);
			
			for(int y = 0;y < grvoice.size();y++) {
				content.append("BT\n");
				content.append("/GRFSYS1 7 Tf\n");
				
				content.append((left - 15) + " " + topLabel +" Td\n");
				content.append("("+Math.round(section * y)+") Tj\n");
				content.append("ET\n");
				
				topLabel += heightSection;
				
				if((section * y) >= topValue)
					break;
			}
		}
		
		return content.toString();
	}
	private String draw3D(GRContext grcontext) throws GRValidateException {
		return null;
	}
	public short getTypeChart() {
		return GRChart.TYPECHART_BAR;
	}
	
	private String getLabelFormatted(String value, double leftLabel, double topLabel, double rotate) {
		StringBuffer content = new StringBuffer();
		Vector<String> token = new Vector<String>();
		
		double PI = 3.14159;
		double C = PI / 180;
		
		double sTeta = Math.round(Math.sin(C * rotate));
		double cTeta = Math.round(Math.cos(C * rotate));
		
		double dimMax = 42519.41;
		
		double dimToken = 0.0;
		double dimTemp = 0.0;
		
		value = value.trim();
		int startStream = 0;
		int endStream = 0;
		
		for(int i = 0;i < value.length();i++) {
			double dimChar = 0.0;
			char c = value.charAt(i);
			
			dimChar = GRArial.getCharacterWidth(c) * 6.0;
			if(dimToken + dimChar > dimMax) {
				
				String stream = value.substring(startStream,endStream);
				
				// Crea un nuovo token
				token.add(stream);
				
				// Imposta i parametri
				i = endStream;
				
				startStream = endStream + 1;
				endStream = 0;
				dimToken = 0;
				
				
			} else {
				if(c == ' ') {
					endStream = i;
				}
				
				dimToken += dimChar;
			}	
			
		}
		token.add(value.substring(startStream));
		
		// A questo punto costruisce lo stream da restituire al chiamante
		int gap = 3 * (token.size() - 1);
		
		double dimMaxStringa = 0;
		for(int i = 0;i < token.size();i++) {
			double dimStringa = GRArial.getStringWidth(token.get(i), 6.0);
			if(dimMaxStringa < dimStringa)
				dimMaxStringa = dimStringa;
		}
		
		for(int i = 0;i < token.size();i++) {
			double dimStringa = GRArial.getStringWidth(token.get(i), 6.0);
			
			double gapTop = ((dimMaxStringa - dimStringa) / 1000) / 2;
			
			content.append(cTeta+" "+sTeta+" "+(sTeta * -1)+" "+cTeta+" "+(leftLabel+gap)+" "+(topLabel-5 - gapTop)+" Tm\n");
			content.append("("+token.get(i)+") Tj\n");
			
			gap -= 7;
			
		}
		
		return content.toString();
	}
	private void appoggio() {
		/*
		double PI = 3.14159;
		double C = PI / 180;
		double rotate = 0.0;
		
		double sTeta = Math.round(Math.sin(C * rotate));
		double cTeta = Math.round(Math.cos(C * rotate));
		//sTeta = -1;
		//cTeta = 0;
		
		content.append("1 1 0 rg\n");	// Colorfill
		content.append("1 0 0 RG\n");	// Colorstroke
		content.append("7 Tr\n");
		
		content.append("BT\n");
		content.append("/GRFSYS1 140 Tf\n");
		//content.append("0 1 -1 0 128 750 Tm\n");
		
		content.append(cTeta+" "+sTeta+" "+(sTeta * -1)+" "+cTeta+" "+left+" "+(top-10)+" Tm\n");
		//content.append("128.34 150 Td\n");
		content.append("(Stampato con tecnologia GlobalReports - www.globalreports.it) Tj\n");
		content.append("ET\n");
		*/
	}
}
