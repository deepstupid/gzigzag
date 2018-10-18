/*   
CellTexter.java
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
/*
 * 	`He's got a good memory, you've got to grant him that,' said
 *    Didactylos. `Show him some more scrolls.'
 *	`How will we know he's remembered them?' Urn demanded, unrolling
 *    a scroll of geometrical theorems. `He can't read! And even if he 
 *    could read, he can't write!'
 *	`We shall have to teach him.'
 *		- Terry Pratchett, Small Gods, p.214
 */

package org.gzigzag;

/** An interface for getting and setting referential text in cells.
 */

public abstract class CellTexter {
    public String getText(Cell c, Obs o) {
	TextSpan[] spans = getSpans(c, o);
	if(spans == null) return null;
	StringBuffer sb = new StringBuffer();
	for(int i=0; i<spans.length; i++) 
	    sb.append(spans[i].getText());
	return sb.toString();
    }

    public int length(Cell c, Obs o) {
	return getText(c, o).length();
    }

    /*
    public abstract void insert(Cell c, int offs, char ch);
    public abstract void insert(Cell c, int offs, TextSpan s);

    public abstract void delete(Cell c, int offs, int offs2);

    public void delete(Cell c, int offs) {
	delete(c, offs, length(c, null));
    }

    public abstract void copy(Cell from, int offs, int len, Cell to, int tooffs);
    */

    public abstract TextSpan[] getSpans(Cell c, Obs o);
    // public abstract void setSpans(Cell c, TextSpan[] spans);
}


