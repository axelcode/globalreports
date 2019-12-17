/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.grpdf.GRPageStream
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

import java.util.Vector;

import com.globalreports.engine.objects.GRSystemObject;

public class GRPageStream {
	private double width;
	private double height;
	private int pagine;
	private Vector<String> contentStream;
	
	private Vector<GRSystemObject> grsysObj;
	
	public GRPageStream(double width, double height) {
		this.width = width;
		this.height = height;
	
		pagine = 0;
		contentStream = new Vector<String>();
		grsysObj = null;
	}
	
	public double getPageWidth() {
		return width;
	}
	public double getPageHeight() {
		return height;
	}
	public void addContent(String content) {
		contentStream.add(content);
		
		pagine++;
	}
	public String getPageContent(int index) {
		return contentStream.get(index);
	}
	public int getPage() {
		return contentStream.size();
	}
	
	public void setGRSystemObject(Vector<GRSystemObject> grsys) {
		grsysObj = grsys;
	}
	public void setGRSystemObject(Vector<GRSystemObject> grsys, double widthPage, double heightPage) {
		if(grsys != null) {
			for(int i = 0;i < grsys.size();i++)
				grsys.get(i).setPageDimension(widthPage, heightPage);
		}
		
		grsysObj = grsys;
	}
	public Vector<GRSystemObject> getGRSystemObjec() {
		return grsysObj;
	}
}
