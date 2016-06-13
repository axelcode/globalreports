/*
 * ==========================================================================
 * class name  : com.globalreports.engine.objects.dynamic.GRDynamicObject
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
package com.globalreports.engine.objects.variable.dynamic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalreports.engine.err.GRValidateException;
import com.globalreports.engine.objects.GRObject;
import com.globalreports.engine.objects.variable.GRVariableObject;
import com.globalreports.engine.structure.font.encoding.GREncode_ISO8859;
import com.globalreports.engine.structure.grbinary.GRPage;
import com.globalreports.engine.structure.grbinary.data.GRDataRow;
import com.globalreports.engine.structure.grbinary.data.GRFormat;
import com.globalreports.engine.structure.grbinary.data.GRValidate;

public abstract class GRDynamicObject extends GRVariableObject {
	protected final String REG_VARIABLE = "[{]([a-zA-Z0-9_]+)(:{0,1})([a-zA-Z0-9, =!$%&;\\\\\"\'\\?\\^\\.\\-\\/\\(\\)]{0,})[}]";
	
	protected GRPage grpage;
	protected GRObject grfather;	// Se il disegno è stato richiesto dalla pagina
									// questa proprietà assume null. Altrimenti
									// sarà un riferimento all'oggetto che lo ha
									// richiamato
	
	public GRDynamicObject(short type) {
		super(type);
		
	}
	
	public void setPage(GRPage grpage) {
		this.grpage = grpage;
	}
	
	public String addVariables(GRDataRow grdata, String value) throws GRValidateException {
		if(grdata == null) 
			return value.replaceAll(REG_VARIABLE, "");
		
		String valueVariable;
		Pattern pattern = Pattern.compile(REG_VARIABLE);
		Matcher matcher = pattern.matcher(value);
		
		while(matcher.find()) { 
			valueVariable = grdata.getValue(matcher.group(1));
			
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
