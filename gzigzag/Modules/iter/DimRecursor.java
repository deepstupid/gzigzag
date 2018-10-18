/*   
DimRecursor.java
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
 * Written by Benjamin Fallenstein
 */

package org.gzigzag;
import java.awt.*;
import java.util.*;

/** The dimension recursion the Vanishing view did formerly.
 * XXX remove verbose debugging messages
 */

public class DimRecursor extends Iter.Any {
public static final String rcsid = "$Id: DimRecursor.java,v 1.1 2000/11/16 20:28:53 bfallenstein Exp $";
    public static final boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }

	public DimRecursor(String[] dims, Iter.Dim iter, boolean included,
			   int maxlevel) {
	    this.dims = dims;
	    this.iter = iter;
	    this.included = included;
	    this.maxlevel = maxlevel;
	    this.higher = new Vector[dims.length];
	    this.curr = new Vector[dims.length];
	}
	
	public Iter.Dim iter;
	public String[] dims;
	public int dim, level;

	protected Enumeration enum;
	protected Vector[] curr, higher;
	
	protected int higherdim, maxlevel;
	
	protected static final Object NULL = new Object();
	
	public void doStart() {
	    Vector v = new Vector();
	    v.addElement(root);
	    enum = v.elements();
	    higher[0] = v;
	    for(int i=0; i<dims.length; i++) {
		curr[i] = new Vector();
	    }
	    dim = 0; level = 0; higherdim = 0;
	}
	
	public Iter.Step getNext() {
	    if(level == 0) {
		iter.start(root, dims[0]);
		level = 1;
	    } else {
		iter.next();
		p("Got next: "+iter.here);
	    }
	
	    while(iter.here == null) {
		p("null");
		while(!enum.hasMoreElements()) {
		    if(level > 1) {
			higherdim++;
		        p("no el on hdim "+higherdim);
		        if(higherdim == dim) higherdim++;
		    }
		    if(level == 1 || higherdim >= dims.length) {
			p("Iter.Dims -- no higher dim on dim "+dim);
			dim++;
			higherdim = 0;
			if(dim >= dims.length) {
			    p("Iter.Dims -- no dim on level "+level);
			    level++;
			    if(level > maxlevel) return  null;
			    dim = 0; higherdim = 1;
			    p("Iter.Dims level: "+level);
			    boolean doContinue = false;
			    for(int d=0; d<dims.length; d++) {
				if(curr[d].size() > 0) doContinue = true;
				higher[d] = curr[d];
				curr[d] = new Vector();
			    }
			    if(!doContinue) return null;
			}
		    }
		    if(level > 1) enum = higher[higherdim].elements();
		    else enum = higher[0].elements();
		}
		iter.start((Iter.Step)enum.nextElement(), dims[dim]);
	    }
	    
	    curr[dim].addElement(iter.here);
	    p("ret: "+iter.here.cell);
	    return iter.here;
	}
	
	public void stop() { iter.stop(); }
}
