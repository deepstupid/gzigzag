/*   
SimpleCellTexter.java
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


package org.gzigzag.impl;
import org.gzigzag.*;
import java.util.ArrayList;

/** A simple implementation of CellTexter.
 * Not very efficient.
 */

public class SimpleCellTexter extends CellTexter {
    Space space;
    Dim dim;

    public SimpleCellTexter(Space s, Dim d) {
	this.space = s;
	this.dim = d;
    }

    TextSpan[] arrType = new TextSpan[0];

    public TextSpan[] getSpans(Cell c, Obs o) {
	if(c.s(dim, -1) != null) return null;
	Span sp = c.getSpan(o);
	if(sp != null) {
	    if(sp instanceof TextSpan)
		return new TextSpan[] {(TextSpan)sp};
	    else
		return null;
	}
	ArrayList v = new ArrayList();
	while(c.s(dim) != null) {
	    v.add(c.getSpan());
	}
	return (TextSpan[])v.toArray(arrType);
    }

    // public void setSpans(Cell c, TextSpan[] spans);

}
