/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.grbinary.data.GRData
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
package com.globalreports.engine.structure.grbinary.data;

import java.awt.Color;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.*;
import org.jdom.input.*;

import com.globalreports.engine.err.GRValidateException;
import com.globalreports.engine.structure.font.encoding.GREncode_ISO8859;

public class GRData {
	protected final String REG_VARIABLE = "[{]([a-zA-Z0-9_]+)(:{0,1})([a-zA-Z0-9, =!$%&;\\\\\"\'\\?\\^\\.\\-\\/\\(\\)]{0,})[}]";
	
	private File fileXml;
	private String pathXml;
	private Hashtable<String, String> xml;
	
	private Vector<GRDataList> dataList;
	private Stack<GRDataList> stackDataList;
	private GRDataList refDataList;
	private Stack<GRDataRow> stackDataRow;
	private GRDataRow refDataRow;
	
	public GRData() {
		
	}
	public GRData(String pathXml) {
		this.pathXml = pathXml;
		fileXml = null;
		
		xml = new Hashtable<String, String>();
		
		stackDataList = null;
		stackDataRow = null;
		
		this.readSource();
		
	}
	public GRData(File fileXml) {
		this.fileXml = fileXml;
		
		xml = new Hashtable<String, String>();
		
		stackDataList = null;
		stackDataRow = null;
		
		this.readSource();
	}
	private void readSource() {
		try {
			
			SAXBuilder builder = new SAXBuilder();
			Document document;
			
			if(fileXml == null)
				document = builder.build(new StringReader(pathXml));
			else
				document = builder.build(fileXml);
					
			Element rootElement = document.getRootElement();
			List children = rootElement.getChildren();
			
			Iterator iterator = children.iterator();
			while (iterator.hasNext()){
				readChild((Element)iterator.next());
			} 
		} catch(JDOMException jde) {
			System.out.println("grpdf::GRData::readSource::JDOMException: "+jde.getMessage());
		} catch(IOException ioe) {
			System.out.println("grpdf::GRData::readSource::IOException: "+ioe.getMessage());
		} 
		
	}
	
	private void readChild(Element element) {
		if(element.getName().equals("data")) {
			readData(element);
		} else if(element.getName().equals("list")) {
			if(dataList == null)
				dataList = new Vector<GRDataList>();
			readList(element);
			
			dataList.add(refDataList);
		} 
		
	}
	private void readData(Element el) {
		List children = el.getChildren();
		Iterator iterator = children.iterator();
		
		while(iterator.hasNext()) {
			Element element = (Element)iterator.next();
			
			if(((Element)element.getParent()).getName().equals("data")) {
				xml.put(element.getName(),element.getValue());
			}
		}
		
	} 
	private void readList(Element element) {
		List children = element.getChildren();
		
		for(int i = 0;i < children.size();i++) {
			Element e = (Element)children.get(i);
			
			if(((Element)e.getParent()).getName().equals("list")) {
				if(e.getName().equals("name")) {
					refDataList = new GRDataList(e.getValue());
				} else if(e.getName().equals("row")) {
					refDataList.addRow();
					readRowList(e);
										
				}
			} 
		}
		
	} 
	private void readRowList(Element element) {
		List children = element.getChildren();
	
		for(int i = 0;i < children.size();i++) {
			Element e = (Element)children.get(i);
			if(((Element)e.getParent()).getName().equals("row")) {
				if(e.getName().equals("list")) {
					if(stackDataList == null)
						stackDataList = new Stack<GRDataList>();
					
					stackDataList.push(refDataList);
					readList(e);
					
					GRDataList dl = refDataList;
					refDataList = stackDataList.pop();
					refDataList.addObjectRow(dl);
				} else {
					refDataList.addElementRow(e.getName(), e.getValue());
				}
			}
			
		}
	}
	
	public String getValue(String nameVariable) {
		return xml.get(nameVariable);
	}
	
	public GRDataList getDataList(String name) {
		if(dataList == null)
			return null;
		
		for(int i = 0;i < dataList.size();i++) {
			if(dataList.get(i).getName().equals(name)) {
				return dataList.get(i);
			}
		}
		
		return null;
	}

	public String addVariables(String value) throws GRValidateException {
		
		String valueVariable;
		Pattern pattern = Pattern.compile(REG_VARIABLE);
		Matcher matcher = pattern.matcher(value);
		
		while(matcher.find()) { 
			valueVariable = getValue(matcher.group(1));
			
			/* Il primo step è quello di validare la variabile (qualora sia richiesto) */
			if(matcher.group(2).length() > 0) {
				if(matcher.group(3).startsWith("FUNCTION")) {
					// Opzioni di formattazione
					valueVariable = GRFormat.formato(matcher.group(3), valueVariable, matcher.group(1));
				} else {
					// Opzioni di validazione
					GRValidate.validate(matcher.group(3), valueVariable, matcher.group(1));
					
				}
				
			}
			
			if(valueVariable != null) {
				value = value.replace(matcher.group(0),GREncode_ISO8859.lineASCIIToOct(valueVariable));
			} else {
				value = value.replaceAll(REG_VARIABLE, "");
			}
		}
		
		return value;
	}

}

