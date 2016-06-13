/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.grpdf.GRXrefTable
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
package com.globalreports.engine.structure.grpdf;

import com.globalreports.engine.structure.grpdf.GRXrefElement;

import java.util.Vector;

public class GRXrefTable {
	private Vector<GRXrefElement> grxrefelem;
	private int pointerAddress;
	
	private int idObjRoot;
	private int idObjInfo;
	private long addressPrev;
	
	public GRXrefTable() {
		init();
	}
	private void init() {
		grxrefelem = new Vector<GRXrefElement>();
		pointerAddress = 0;
		addressPrev = 0;
	}
	
	public void addAddress(int numObj, long address) {
		GRXrefElement elem = new GRXrefElement(numObj, address);
		
		boolean flagInsert = false;
		
		for(int i = 0;i < grxrefelem.size();i++) {
			int temp = grxrefelem.get(i).getNumberObj();
			
			if(numObj < temp) {
				grxrefelem.add(i,elem);
				flagInsert = true;
				break;
			}
		}
		if(!flagInsert)
			grxrefelem.add(elem);
		
		
	}
	public String getAddress() {
		String ritorno = grxrefelem.get(pointerAddress).getAddressToString();
		pointerAddress++;
		
		return ritorno;
	}
	public long getAddress(int index) {
		return grxrefelem.get(index).getAddress();
	}
	public int getTotalReference() {
		return grxrefelem.size();
	}
	public void ordinaElementi() {
		// Procede ad ordinare gli elementi
	}
	
	public void setRoot(int value) {
		idObjRoot = value;
	}
	public int getRoot() {
		return idObjRoot;
	}
	public void setInfo(int value) {
		idObjInfo = value;
	}
	public int getInfo() {
		return idObjInfo;
	}
	public void setPrev(long value) {
		addressPrev = value;
	}
	public long getPrev() {
		return addressPrev;
	}
}
