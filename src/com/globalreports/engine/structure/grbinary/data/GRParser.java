/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.grbinary.data.GRParser
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalreports.engine.err.GRTextConditionOperatorUnknowException;

public class GRParser {
	private final static int TYPEVALUE_UNDEFINED	= 0;
	private final static int TYPEVALUE_STRING		= 1;
	private final static int TYPEVALUE_INTEGER		= 2;
	
	private GRData grdata;
	
	private String REG_TEXT = "((\\{([^\\}]+)\\}[ ]{0,}(<>|=|<=|>=|<|>)[ ]{0,}('([^']+)'|([0-9]+)))[ ]{0,}(AND|OR){0,1})";
	private String REG_PARENTESI = "(\\(([^)^(]*)\\))";
	private String REG_CONDITION = "(TRUE|FALSE){1}[ ]{0,}(AND|OR){0,1}";
	private String condizione;
	
	public GRParser(GRData grdata) {
		this.grdata = grdata;
		
		condizione = "";
	}
	
	public void setCondition(String value) {
		this.condizione = value;
	}
	
	public boolean verify() throws GRTextConditionOperatorUnknowException {
		if(grdata == null)
			return false;
		
		String condizione = this.condizione;
		String risultato;
		//System.out.println("COND: "+condizione);
		String token = estraiParentesi(condizione);
		//System.out.println("TOKEN: "+token);
		while(token != null) {
			risultato = estraiCondizione(token);
			
			token = estraiParentesi(token);
			
		}
		
		risultato = estraiCondizione(condizione);
		//System.out.println("RIS: "+risultato);
		boolean e = esito(risultato);
		
		return e;
	}
	private boolean esito(String value) {
		int ritEsito = 0;
		int andamento = 0;	// 0: Inizio - 1: AND - 2: OR
		
		Pattern pattern = Pattern.compile(REG_CONDITION);
		Matcher matcher = pattern.matcher(value);
		
		while(matcher.find()) {
			if(matcher.group(1).equals("FALSE")) {
				if(matcher.group(2) != null && matcher.group(2).equals("AND"))
					return false;
				
				if(andamento == 0)
					ritEsito = 0;
				else if(andamento == 1)
					return false;
			} else
				ritEsito = 1;
						
			if(matcher.group(2) != null) {
				if(matcher.group(2).equals("AND"))
					andamento = 1;
				else
					andamento = 2;
			} 
			
		}
		
		if(ritEsito == 0)
			return false;
		
		return true;
	}
	private String estraiParentesi(String value) {
		Pattern pattern = Pattern.compile(REG_PARENTESI);
		Matcher matcher = pattern.matcher(value);
		
		if(matcher.find()) {
			return matcher.group(2);
		}
		
		return null;
	}
	private String estraiCondizione(String value) throws GRTextConditionOperatorUnknowException {
		String risultato = null;

		Pattern pattern = Pattern.compile(REG_TEXT);
		Matcher matcher = pattern.matcher(value);
		
		while(matcher.find()) { 
			
			risultato = verificaCondizione(matcher.group(3),matcher.group(4),matcher.group(5));
			
			String token = matcher.group(2);
			value = value.replace(token,risultato);
			
		}
		
		return value;
	}
	private String verificaCondizione(String variabile, String operatore, String valore) throws GRTextConditionOperatorUnknowException {
		/* Definisce il tipo di variabile */
		int type = TYPEVALUE_UNDEFINED;
		boolean r = false;
		
		if(valore.matches("'[^']+'")) {
			type = TYPEVALUE_STRING;
			valore = valore.replace("'","");
		} else {
			type = TYPEVALUE_INTEGER;
		}
		
		if(operatore.equals("=")) {
			r = ugualeA(grdata.getValue(variabile),valore,type);
		} else if(operatore.equals("<>")) {
			r = diversoDa(grdata.getValue(variabile),valore,type);
		} else if(operatore.equals(">")) {
			r = maggioreDi(grdata.getValue(variabile),valore,type);
		} else if(operatore.equals(">=")) {
			/* Prima verifica che sia maggiore */
			r = maggioreDi(grdata.getValue(variabile),valore,type);
			if(!r) {
				/* Nel caso in cui sia false verifica se � uguale */
				r = ugualeA(grdata.getValue(variabile),valore,type);
			}
		} else if(operatore.equals("<")) {
			r = minoreDi(grdata.getValue(variabile),valore,type);
		} else if(operatore.equals("<=")) {
			/* Prima verifica che sia maggiore */
			r = minoreDi(grdata.getValue(variabile),valore,type);
			if(!r) {
				/* Nel caso in cui sia false verifica se � uguale */
				r = ugualeA(grdata.getValue(variabile),valore,type);
			}
		} else {
			throw new GRTextConditionOperatorUnknowException(operatore);
		}
		
		if(r)
			return "TRUE";
		else
			return "FALSE";
		
	}
	
	private boolean maggioreDi(String a, String b, int type) {
		if(a == null || b == null)
			return false;
		
		if(type == TYPEVALUE_INTEGER) {
			// NUMERICO
			Double objA = Double.parseDouble(a);
			Double objB = Double.parseDouble(b);
			
			if(objA.compareTo(objB) > 0) {
				return true;
			} else {
				return false;
			}
		} else {
			if(a.compareTo(b) > 0) {
				return true;
			} else {
				return false;
			}
		}
	}
	private boolean minoreDi(String a, String b, int type) {
		if(a == null || b == null)
			return false;
		
		if(type == TYPEVALUE_INTEGER) {
			// NUMERICO
			Double objA = Double.parseDouble(a);
			Double objB = Double.parseDouble(b);
			
			if(objA.compareTo(objB) < 0) {
				return true;
			} else {
				return false;
			}
		} else {
			if(a.compareTo(b) < 0) {
				return true;
			} else {
				return false;
			}
		}
	}
	private boolean ugualeA(String a, String b, int type) {
		
		if(a == null || b == null)
			return false;
		
		if(type == TYPEVALUE_INTEGER) {
			// NUMERICO
			Double objA = Double.parseDouble(a);
			Double objB = Double.parseDouble(b);
			
			if(objA.compareTo(objB) == 0) {
				return true;
			} else {
				return false;
			}
		} else {
			if(a.equals(b))
				return true;
			else
				return false;
		}
		
	}
	private boolean diversoDa(String a, String b, int type) {
		if(a == null || b == null)
			return false;
		
		if(type == TYPEVALUE_INTEGER) {
			// NUMERICO
			Double objA = Double.parseDouble(a);
			Double objB = Double.parseDouble(b);
			
			if(objA.compareTo(objB) != 0) {
				return true;
			} else {
				return false;
			}
		} else {
			if(!a.equals(b))
				return true;
			else
				return false;
		}
	}
	
}
