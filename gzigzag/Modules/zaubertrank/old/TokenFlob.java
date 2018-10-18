/*   
TokenFlob.java
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

/** A SplitCellFlob1 showing a Zaubertrank token.
 *  This is an own class in order to get the XOR highlighting right.
 */

public class TokenFlob extends SplitCellFlob1 {
public static final String rcsid = "$Id: TokenFlob.java,v 1.1 2001/04/02 20:29:05 bfallenstein Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    // tk for 'token.'
    public TokenFlob tkParent, tkFirstChild, tkNextChild;
    public TokenFlob tkNextPart, tkFirstPart;
    public int tkNumber = -1;
    public ZZCell tkHandle;

    public void setTkParent(TokenFlob parent) {
	tkParent = parent;
	tkNextChild = parent.tkFirstChild;
	parent.tkFirstChild = this;
    }
    public void setTkFirstPart(TokenFlob first) {
	tkFirstPart = first;
	tkNextPart = first.tkNextPart;
	first.tkNextPart = this;
    }

    TokenFlob(int x, int y, int d, int w, int h, ZZCell c, String txt, Font f,
	      FontMetrics fm, SplitCellFlob1 parent, int start, int n, Color bg,
	      ZZCell tkHandle) {
	super(x, y, d, w, h, c, txt, f, fm, parent, start, n, bg);
	this.bg = bg;
	this.tkHandle = tkHandle;
	tkFirstPart = this;
    }

    public void renderChildrenXOR(Graphics g) {
	for(TokenFlob tk = tkFirstChild; tk != null; tk = tk.tkNextChild) {
	    tk.renderXORFrame(g, false, false);
	    tk.renderChildrenXOR(g);
	}
    }

    public boolean renderXOR(Graphics g, int cx, int cy) {
	if(!insideRect(cx, cy)) return false;
	tkFirstPart.renderChildrenXOR(g);
	for(TokenFlob tk = tkFirstPart; tk != null; tk = tk.tkNextPart)
	    tk.renderXORSolid(g);
	return true;
    }
}
