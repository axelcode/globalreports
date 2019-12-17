/*
 * ==========================================================================
 * class name  : com.globalreports.engine.err.GRException
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
package com.globalreports.engine.err;

public class GRException extends Exception {
	public static final int GRTYPEERROR_GENERIC							= 1;
	public static final int GRTYPEERROR_VALIDATE						= 10;
	public static final int GRTYPEERROR_LAYOUT							= 11;
	public static final int GRTYPEERROR_PDF								= 12;
	public static final int GRTYPEERROR_TEXTCONDITION					= 13;
	public static final int GRTYPEERROR_ATTACHMENT						= 14;
	public static final int GRTYPEERROR_BARCODE							= 15;
	public static final int GRTYPEERROR_COMPILE							= 16;
	
	public static final int	GRCODERROR_VALIDATE_NOTEMPTY				= 10001;
	public static final int	GRCODERROR_VALIDATE_NOTNUMBER				= 10002;
	public static final int	GRCODERROR_VALIDATE_NOTDATE					= 10003;
	public static final int	GRCODERROR_VALIDATE_CONDITION_NOTSATISFIED	= 10004;
	public static final int GRCODERROR_VALIDATE_NOTEMAIL				= 10005;
	public static final int GRCODERROR_VALIDATE_NOTCODICEFISCALE		= 10006;
	
	public static final int GRCODERROR_LAYOUT_OBJECTNOTDEFINED			= 10101;
	public static final int GRCODERROR_LAYOUT_FILENOTFOUND				= 10102;
	public static final int GRCODERROR_LAYOUT_IOREAD					= 10103;
	
	public static final int GRCODERROR_PDF_PATHNOTFOUND					= 10201;
	public static final int GRCODERROR_PDF_IOWRITE						= 10202;
	
	public static final int GRCODERROR_TEXTCONDITION_OPERATORUNKNOW		= 10301;
	
	public static final int GRCODERROR_ATTACHMENT_XREFUNKNOW			= 10401;
	public static final int GRCODERROR_ATTACHMENT_PAGESUNKNOW			= 10402;
	public static final int GRCODERROR_ATTACHMENT_INDEXCORRUPT			= 10403;
	public static final int GRCODERROR_ATTACHMENT_FILENOTFOUND			= 10404;
	public static final int GRCODERROR_ATTACHMENT_IOREAD				= 10405;
	public static final int GRCODERROR_ATTACHMENT_PAGENOTEXISTS			= 10406;
	public static final int GRCODERROR_ATTACHMENT_ENCRYPTNOTSUPPORTED	= 10407;
	
	public static final int GRCODERROR_BARCODE_VALUEINCORRECT			= 10501;
	
	public static final int GRCODERROR_COMPILE_NAMEDOCUMENTMISSING		= 10601;
	public static final int GRCODERROR_COMPILE_TYPENOTDEFINED			= 10602;
	public static final int GRCODERROR_COMPILE_IO						= 10603;
	public static final int GRCODERROR_COMPILE_XMLREAD					= 10604;
	public static final int GRCODERROR_COMPILE_WRITEGRB					= 10605;
	
	private int typeError;
	private int codError;
	
	/**
	 * Crea un oggetto generico, di tipo eccezione.
	 * 
	 * @param err Un intero che descrive il codice di errore generato.<br>
	 * Pu� essere una delle costanti contenute nella classe GRException aventi prefisso GRCODERROR_
	 * 
	 */
	public GRException(int err) {
		this(null,err,GRException.GRTYPEERROR_GENERIC);
	}
	/**
	 * Crea un oggetto generico, di tipo eccezione, con un messaggio di errore personalizzato.
	 * 
	 * @param message Il messaggio di errore che si desidera far visualizzare all'utente
	 * @param err Un intero che descrive il codice di errore generato.<br>
	 * Pu� essere una delle costanti contenute nella classe GRException aventi prefisso GRCODERROR_
	 * 
	 */
	public GRException(String message, int err) {
		this(message,err,GRException.GRTYPEERROR_GENERIC);
	}
	/**
	 * Crea un oggetto generico di tipo eccezione, con un messaggio di errore personalizzato.
	 * 
	 * @param message Il messaggio di errore che si desidera far visualizzare all'utente
	 * @param err Un intero che descrive il codice di errore generato.<br>
	 * Pu� essere una delle costanti contenute nella classe GRException aventi prefisso GRCODERROR_
	 * @param type Un intero che descrive la tipologia di errore generato.<br>
	 * Pu� essere una delle costanti contenute nella classe GRException aventi prefisso GRTYPEERROR_
	 * 
	 */
	public GRException(String message, int err, int type) {
		super(message);
		
		this.codError = err;
		this.typeError = type;
	}
	/**
	 * Ritorna il codice di errore.
	 * 
	 * @return Una delle costanti contenute nella classe GRException aventi prefisso GRCODERROR_
	 */
	public int getCodeError() {
		return codError;
	}
	
}
