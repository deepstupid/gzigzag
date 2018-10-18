/*   
TokenGrouper.java
 *    
 *    Copyright (c) 2000, Benjamin Fallenstein
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
 * Written by Benjamin Fallenstein
 */
 

package org.zaubertrank;
import org.gzigzag.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** A class to group TokenFlobs into their hierarchies.
 */

public class TokenGrouper {
public static final String rcsid = "$Id: TokenGrouper.java,v 1.1 2001/04/02 20:29:05 bfallenstein Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    private int ntoks = 0;
    private Vector parents = new Vector();

    public int addToken(int parent) {
	parents.addElement(new Integer(parent));
	p("TokenGroup cur "+ntoks+" parent "+parent);
	return ntoks++; // i.e., return ntoks and after, increment one
    }

    public void group(final FlobSet into) {
	final TokenFlob[] toks = new TokenFlob[ntoks];

	if(dbg) into.dump();
	into.iterDepth(new FlobSet.DepthIter() {
	public void act(Flob[] flobs, int start, int n) {
	    for(int f = start; f<start+n; f++) {
		Flob cur = flobs[f];
		p("TokenGroup found flob "+cur);
		if(!(cur instanceof TokenFlob)) continue;
		TokenFlob tf = (TokenFlob)cur;
		if(tf.tkNumber >= 0) {
		    if(toks[tf.tkNumber] == null) 
			toks[tf.tkNumber] = tf;
		    else
			tf.setTkFirstPart(toks[tf.tkNumber]);
		}
		p("...is TokenFlob "+tf.tkNumber);
	    }
	}}, false);
	int child = 0;
	for(Enumeration e = parents.elements(); e.hasMoreElements();) {
	    int parent = ((Integer)e.nextElement()).intValue();
	    p("TokenGroup connect parent "+parent+" child "+child);
	    if(parent >= 0 && toks[child] != null && toks[parent] != null) 
		toks[child].setTkParent(toks[parent]);
	    child++;
	}
    }

}

// vim: set syntax=java :
