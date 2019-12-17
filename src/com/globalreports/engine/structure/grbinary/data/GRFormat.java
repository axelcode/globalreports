/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.grbinary.data.GRFormat
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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalreports.engine.err.GRValidateNotNumberException;

public abstract class GRFormat {
	private final static String FUNCTION_FORMATNUMBER		= "FORMATNUMBER\\([0-9]\\)";
	private final static String FUNCTION_LOWERCASE			= "LOWERCASE";
	private final static String FUNCTION_UPPERCASE			= "UPPERCASE";
	private final static String FUNCTION_FIRSTUPPERCASE		= "FIRSTUPPERCASE";
	private final static String FUNCTION_ABS				= "ABS";
	
	public static String formato(String condizione, String valore, String nomeVariabile) throws GRValidateNotNumberException {
		String typeFunction;
		String valueReturn;

		if(valore == null || valore.length() == 0)
			return valore;
		
		valueReturn = valore;
		
		String arrCondition[] = condizione.split(";");
		for(int i = 0;i < arrCondition.length;i++) {
			typeFunction = arrCondition[i].substring(9,arrCondition[i].length()-1);
			
			if(typeFunction.matches(GRFormat.FUNCTION_FORMATNUMBER)) {
				valueReturn = GRFormatNumber(typeFunction, valueReturn);
				
				if(valueReturn == null)
					throw new GRValidateNotNumberException(nomeVariabile);
				
			} else if(typeFunction.matches(GRFormat.FUNCTION_LOWERCASE)) {
				valueReturn = valueReturn.toLowerCase();
			} else if(typeFunction.matches(GRFormat.FUNCTION_UPPERCASE)) {
				valueReturn = valueReturn.toUpperCase();
			} else if(typeFunction.matches(GRFormat.FUNCTION_FIRSTUPPERCASE)) {
				if(valueReturn.length() == 1)
					valueReturn = valueReturn.toUpperCase();
				else 
					valueReturn = valueReturn.substring(0,1).toUpperCase() + valueReturn.substring(1).toLowerCase();
			} else if(typeFunction.matches(GRFormat.FUNCTION_ABS)) { 
				if(valueReturn.charAt(0) == '-')
					valueReturn = valueReturn.substring(1);
			} 
		}
		
		return valueReturn;
	}
	
	private static String GRFormatNumber(String funct, String value) {
		if(value == null)
			return null;

		int decimalNumber = 0;
		String reg = "\\(([0-9])\\)";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(funct);
		
		while(matcher.find()) {
			decimalNumber = Integer.parseInt(matcher.group(1));
		}
		
		NumberFormat formatN = NumberFormat.getNumberInstance(Locale.ITALY);
		formatN.setMinimumFractionDigits(decimalNumber);
		formatN.setMaximumFractionDigits(decimalNumber);
		
		try {
			return formatN.format(Double.parseDouble(value.replace(',','.')));
			
		} catch(NumberFormatException nfe) {
			return null;
		}
		
	}
	
}
