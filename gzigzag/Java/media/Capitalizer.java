/*   
Capitalizer.java
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
 * Capitalizing code written by Tuomas Lukka
 */

package org.gzigzag;
import java.awt.*;
import java.util.*;

/** A class to capitalize first letters of sentences.
 * A sentence is separated by two or more spaces. If you just need to
 * capitalize one string, use the static method single(String). If you need
 * to capitalize a number of concurrent strings, instantiate Capitalizer and
 * run cap(String) over them, in order.
 * <p>
 * The algorithm is tricky and probably needs a lot of adjustment.
 * The first letters of sentences are currently identified by
 * two spaces next to each other.
 */

public class Capitalizer {
public static final String rcsid = "$Id: Capitalizer.java,v 1.1 2000/12/25 19:37:09 bfallenstein Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    /** Have we read a single space? */
    boolean hadSpace = false;

    /** Are we past a sentence boundary (two spaces)? */
    boolean boundary = true;

    public static String single(String s) {
	return new Capitalizer().cap(s);
    }

    public String cap(String s) {
	int len = s.length();
	// Look for the first non-space character the hard way.
	int i;
	for(i=0; i<s.length(); i++) {
	    char c = s.charAt(i);
	    if(Character.isLetter(c)) {
		if(boundary)
		    s = s.substring(0,i) + Character.toUpperCase(c)
			+ s.substring(i+1);
		boundary = false;
		hadSpace = false;
		break;
	    } else
	    if(c == ' ') {
		if(hadSpace) {
		    boundary = true;
		    hadSpace = false;
		} else {
		    hadSpace = true;
		}
	    }
	}
	if(i == s.length()) return s;
	boundary = false;
	hadSpace = false;
	if(len > 1) {
	    char c1 = s.charAt(len-1);
	    if(c1 == ' ') {
		char c2 = s.charAt(len-2);
		boundary = (c2 == ' ');
		hadSpace = (c2 != ' ');
	    }
	}
	// Then, find the other occurrences.
	int ind = i-1;
	while((ind = s.indexOf("  ", ind+1)) >= 0) {
	    if(ind == s.length() - 2) break;
	    if(Character.isLetter(s.charAt(ind+2)))
		s = s.substring(0, ind+2) + 
		    Character.toUpperCase( s.charAt(ind+2)) 
			+ s.substring(ind+3);
	}
	return s;
    }    
}