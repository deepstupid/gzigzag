/*   
SimpleLineInfo.java
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
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
package org.gzigzag;
import java.util.*;
import java.io.*;
import java.awt.*;

/** A simple implementation of LineInfo, for plain strings in a list
 * of fonts.
 */

public class SimpleLineInfo implements LineInfo {
public static final String rcsid = "$Id: SimpleLineInfo.java,v 1.8 2000/09/19 10:32:00 ajk Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }

    public interface Layout {
	FontMetrics getFontMetrics(int line);
	int getWidth(int line);
	int getCenterLine();
    }

    String s;
    Layout l;
    int cursor;

    SimpleLineInfo(String s, int cursor, Layout l) { this.s = s; this.l = l; 
	this.cursor = cursor;}

    public int getMax() { return s.length(); }
    public int getCursor() { return cursor; }
    public int getCenterLine() { return l.getCenterLine(); }

    public void split(int x0, int n, int sug, int[] ret) {
	int ind = s.indexOf(' ', x0+1);
	
	if(ind >= x0+n || ind < 0) {
	    // split word
	    ind = 1;
	    ret[0] = 1;
	    ret[1] = 500;
	}  else {
	    while(true) {
		int ind2 = s.indexOf(' ', ind+1);
		if(ind2 < 0) break;
		if(ind2 >= x0+n) break;
		if(ind2 >= sug) break;
		ind = ind2;
	    }
	    ret[0] = ind - x0;
	    ret[1] = 0;
	}
	p("Split: "+x0+" "+n+" "+sug+" "+ret[0]+" "+ret[1]);
    }

    public void widthPenalty(int x0, int n, int line, int[] ret) {
	p("WP: "+x0+" "+n+" "+line);
	String subs = s.substring(x0, x0+n).trim();
	int w = l.getFontMetrics(line).stringWidth(subs);
	int want = l.getWidth(line);
	ret[0] = (1000*w) / want;
	if(w > want) {
	    ret[1] = (w-want) * (w-want) / 3;
	} else {
	    ret[1] = (want - w) / 2;
	}
	p("WP: "+x0+" "+n+" "+line+" "+want+" "+w+" Ret: "+ret[0]+" "+ret[1]);
    }
}


