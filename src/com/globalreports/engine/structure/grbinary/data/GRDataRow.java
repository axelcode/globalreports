/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.grbinary.data.GRDataRow
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

import java.util.Hashtable;
import java.util.Vector;

public class GRDataRow extends GRData {
	
	private Hashtable<String, String> rowData;
	private Vector<GRDataList> grlist;
	
	public GRDataRow() {
		rowData = new Hashtable<String, String>();
		grlist = null;
	}
	
	public void addElement(String key, String value) {
		rowData.put(key,value);
	}
	
	public void addObject(GRDataList refList) {
		if(grlist == null)
			grlist = new Vector<GRDataList>();
		
		grlist.add(refList);
	}
	public String getValue(String nameVariable) {
		return rowData.get(nameVariable);
	}
	public GRDataList getObject(int i) {
		return grlist.get(i);
	}
	public GRDataList getDataList(String name) {
		if(grlist == null)
			return null;
		
		for(int i = 0;i < grlist.size();i++) {
			if(grlist.get(i).getName().equals(name))
				return grlist.get(i);
		}
		
		return null;
	}
	public int getTotaleVariable() {
		return rowData.size();
	}
	public int getTotaleObject() {
		if(grlist == null)
			return 0;
		
		return grlist.size();
	}
	
}
