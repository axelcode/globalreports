/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.grbinary.data.GRValidate
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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.globalreports.engine.err.*;

public abstract class GRValidate {
	private final static String OPERATOR_BASIC_NOTEMPTY	= "$NOTEMPTY";
	private final static String OPERATOR_BASIC_NUMBER	= "$NUMBER";
	private final static String OPERATOR_BASIC_DATE		= "$DATE";
	private final static String OPERATOR_BASIC_EMAIL	= "$EMAIL";
	private final static String OPERATOR_BASIC_CODICEFISCALE = "$CODICEFISCALE";
	
	private final static String REGEX_CONDITION			= "([!=])([a-zA-Z0-9, &$%;\\\\\"\'\\?\\^\\.\\{\\}\\-\\/]+)";
	private final static String OPERATOR_OR				= "\\$OR\\(.+\\)";
	
	private static boolean isNotEmpty(String valore) {
		if(valore == null || valore.trim().equals(""))
			return false;
		
		return true;
	}
	private static boolean isNumber(String valore) {
		if(valore == null)
			return false;
		
		try {
			Double.parseDouble(valore.replace(".","").replace(",","."));
		} catch(NumberFormatException nfe) {
			return false;
		}
		
		return true;
	}
	private static boolean isDate(String valore) {
		if(valore == null)
			return false;
		
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		format.setLenient(false);
		
		valore = valore.replace(".","/").replace("-","/");
		try {
			format.parse(valore);
		} catch (ParseException e) {
			return false;
		}
		
		return true;
	}
	private static boolean isEmail(String value) {
		if(value == null)
			value = "";
			
		String reg = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		
		return value.matches(reg);
			
	}
	private static boolean isCodiceFiscale(String value) {
		if(value == null)
			value = "";
		
		String reg = "^([A-Za-z]{6})([0-9]{2})[ABCDEHLMPRSTabcdehlmprst]([0-9]{2})[A-Za-z]([0-9]{3})[A-Za-z]$";
		
		return value.matches(reg);
	}
	private static boolean isValidoAND(String condition, String value) {
		Pattern pattern = Pattern.compile(REGEX_CONDITION);
		Matcher matcher = pattern.matcher(condition);
		
		if(value == null)
			value = "";
			
		while(matcher.find()) { 
			if(matcher.group(1).equals("!")) {	// Diverso da
				if(matcher.group(2).equals(value))
					return false;
			} else if(matcher.group(1).equals("=")) {	// Uguale a
				if(!matcher.group(2).equals(value))
					return false;
			}
		}
		
		return true;
	}
	private static boolean isValidoOR(String condition, String value) {
		Pattern pattern = Pattern.compile(REGEX_CONDITION);
		Matcher matcher = pattern.matcher(condition);
		
		if(value == null)
			value = "";
						
		while(matcher.find()) { 
			if(matcher.group(1).equals("!")) {	// Diverso da
				if(!matcher.group(2).equals(value))
					return true;
			} else if(matcher.group(1).equals("=")) {	// Uguale a
				if(matcher.group(2).equals(value))
					return true;
			}
		}
		
		return false;
	}
	public static void validate(String condizione, String valore, String nomeVariabile)
			throws GRValidateEmptyException
				   ,GRValidateNotNumberException
				   ,GRValidateNotDateException
				   ,GRValidateNotEmailException
				   ,GRValidateNotCodFiscaleException
				   ,GRValidateConditionNotSatisfiedException {
		// Suddivide la condizione in tanti token
		String cond[] = condizione.split(";");
		
		// Cicla per tutte le condizioni. Se anche una sola ritorna FALSE allora ritorner� un errore ed il PDF non verr� creato
		for(int i = 0;i < cond.length;i++) {
			// Verifica sulla tipologia del dato
			if(cond[i].equals(GRValidate.OPERATOR_BASIC_NOTEMPTY)) {
				if(!GRValidate.isNotEmpty(valore)) 
					throw new GRValidateEmptyException(nomeVariabile);
			} else if(cond[i].equals(GRValidate.OPERATOR_BASIC_NUMBER)) {
				if(!GRValidate.isNumber(valore))
					throw new GRValidateNotNumberException(nomeVariabile);
			} else if(cond[i].equals(GRValidate.OPERATOR_BASIC_DATE)) {
				if(!GRValidate.isDate(valore))
					throw new GRValidateNotDateException(nomeVariabile);
			} else if(cond[i].equals(GRValidate.OPERATOR_BASIC_EMAIL)) {
				if(!GRValidate.isEmail(valore))
					throw new GRValidateNotEmailException(nomeVariabile);
			} else if(cond[i].equals(GRValidate.OPERATOR_BASIC_CODICEFISCALE)) {
				if(!GRValidate.isCodiceFiscale(valore))
					throw new GRValidateNotCodFiscaleException(nomeVariabile);
			} else if(cond[i].matches(GRValidate.OPERATOR_OR)) {
				if(!GRValidate.isValidoOR(cond[i],valore)) 
					throw new GRValidateConditionNotSatisfiedException(nomeVariabile);
			} else {
				if(!GRValidate.isValidoAND(cond[i],valore)) {
					throw new GRValidateConditionNotSatisfiedException(nomeVariabile);
				}
			}
						
		}

	}
}