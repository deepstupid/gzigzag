/*   
ColorUtil.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
 *
 *    You may use and distribute under the terms of either the GNU Lesser
 *    General Public License, either version 2 of the license or,
 *    at your choice, any later version. Alternatively, you may use and
 *    distribute under the terms of the XPL.
 *
 *    See the LICENSE.lgpl and LICENSE.xpl files for the specific terms of 
 *    the licenses.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the README
 *    file for more details.
 *
 */
/*
 * Written by Tuomas Lukka
 */
package org.gzigzag.util;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;

/** Some color handling utilities.
 */

public final class ColorUtil {
public static final String rcsid = "$Id: ColorUtil.java,v 1.4 2001/05/10 19:42:55 tjl Exp $";

    public static Color[] fadingColors_line(int n) {
	Color[] res = new Color[n];
	for(int i=0; i<n; i++) {
	    int bluegreen = (0xFF * i)/n;
	    res[i] = new Color(0xFF0000 + (bluegreen << 8) + bluegreen);
	}
	return res;
    }

    public static Color[] fadingColors_solid(int n) {
	Color[] res = new Color[n];
	for(int i=0; i<n; i++) {
	    float f = i/(float)(n-1);
	    res[i] = Color.getHSBColor(f/2, 0.8f*(1-f), 1);
	}
	return res;
    }

    public static Color[] fadingColors_solid_darker(int n) {
	Color[] res = new Color[n];
	for(int i=0; i<n; i++) {
	    float f = i/(float)(n-1);
	    res[i] = Color.getHSBColor(f/2, 0.8f*(1-f), 0.9f);
	}
	return res;
    }

}
