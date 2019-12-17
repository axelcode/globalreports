/*
 * ==========================================================================
 * class name  : com.globalreports.engine.structure.GRMeasures
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
package com.globalreports.engine.structure;

public class GRMeasures {
	public static final int		ORIENTATION_PORTRAIT	= 1;
	public static final int		ORIENTATION_LANDSCAPE	= 2;
	
	public static final double	FORMAT_A0[] = {841.0,1189.0};
	public static final double	FORMAT_A1[] = {594.0,841.0};
	public static final double	FORMAT_A2[] = {420.0,594.0};
	public static final double	FORMAT_A3[] = {297.0,420.0};
	public static final double	FORMAT_A4[] = {210.0,297.0};
	public static final double 	FORMAT_A5[] = {148.0,210.0};
	public static final double 	FORMAT_A6[] = {105.0,148.0};
	public static final double 	FORMAT_A7[] = {74.0,105.0};
	public static final double 	FORMAT_A8[] = {52.0,74.0};
	public static final double 	FORMAT_A9[] = {37.0,52.0};
	public static final double 	FORMAT_A10[] = {26.0,37.0};
	
	public static final double	FORMAT_B0[] = {1000.0,1414.0};
	public static final double	FORMAT_B1[] = {707.0,1000.0};
	public static final double	FORMAT_B2[] = {500.0,707.0};
	public static final double	FORMAT_B3[] = {353.0,500.0};
	public static final double	FORMAT_B4[] = {250.0,353.0};
	public static final double 	FORMAT_B5[] = {176.0,250.0};
	public static final double 	FORMAT_B6[] = {125.0,176.0};
	public static final double 	FORMAT_B7[] = {88.0,125.0};
	public static final double 	FORMAT_B8[] = {62.0,88.0};
	public static final double 	FORMAT_B9[] = {44.0,62.0};
	public static final double 	FORMAT_B10[] = {31.0,44.0};
	
	public static final double	FORMAT_C0[] = {917.0,1297.0};
	public static final double	FORMAT_C1[] = {648.0,917.0};
	public static final double	FORMAT_C2[] = {458.0,648.0};
	public static final double	FORMAT_C3[] = {324.0,458.0};
	public static final double	FORMAT_C4[] = {229.0,324.0};
	public static final double 	FORMAT_C5[] = {162.0,229.0};
	public static final double 	FORMAT_C6[] = {114.0,162.0};
	public static final double 	FORMAT_C7[] = {81.0,114.0};
	public static final double 	FORMAT_C8[] = {57.0,81.0};
	public static final double 	FORMAT_C9[] = {40.0,57.0};
	public static final double 	FORMAT_C10[] = {28.0,40.0};
	
	public static final double 	FORMAT_LETTER[] = {216.0,279.0};
	public static final double 	FORMAT_LEGAL[] = {216.0,356.0};
	public static final double 	FORMAT_JUNIORLEGAL[] = {203.0,127.0};
	public static final double 	FORMAT_LEDGER[] = {432.0,279.0};
	public static final double 	FORMAT_TABLOID[] = {279.0,432.0};
	
	public static final double 	FORMAT_ANSIA[] = {216.0,279.0};
	public static final double 	FORMAT_ANSIB[] = {279.0,432.0};
	public static final double 	FORMAT_ANSIC[] = {432.0,559.0};
	public static final double 	FORMAT_ANSID[] = {559.0,864.0};
	public static final double 	FORMAT_ANSIE[] = {864.0,1118.0};
	
	public static final double 	FORMAT_ARCHA[] = {229.0,305.0};
	public static final double 	FORMAT_ARCHB[] = {305.0,457.0};
	public static final double 	FORMAT_ARCHC[] = {457.0,610.0};
	public static final double 	FORMAT_ARCHD[] = {610.0,914.0};
	public static final double 	FORMAT_ARCHE[] = {914.0,1219.0};
	public static final double 	FORMAT_ARCHE1[] = {762.0,1067.0};
	public static final double 	FORMAT_ARCHE2[] = {660.0,965.0};
	public static final double 	FORMAT_ARCHE3[] = {686.0,991.0};
	
	public static synchronized double fromMillimetersToPostScript(double value) {
		return value / .35278;
	}
	public static synchronized double arrotonda(double x) {
		x = Math.floor(x * 100);
		x = x / 100;
		
		return x;
	}
}
