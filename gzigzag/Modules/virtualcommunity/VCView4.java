/*   
VCView4.java
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


package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;

/** Second, cardstack-like virtualcommunity view.
 */


public class VCView4 extends VirtualCommunity {
    public static final String rcsid = "$Id: VCView4.java,v 1.8 2001/02/28 10:18:20 tjl Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log(s); }
    private static void pa(String s) { ZZLogger.log(s); }

    int xstep = 40;
    int ystep = 40;

    void mksideFlobs(FlobSet into, ZZCell cur, int x, int y, int w, int h, int d) {
	cur = cur.s(replies);
	if(cur == null) return;
	y += 2*h/3;
	h -= 2*h/3;
	while((cur = cur.s(replies_list)) != null) {
	    if(into.findFlob(null, cur) != null)
		return;
	    sibl.makeFlob(into, cur, cur, 0.5f, 
		x+w, y, d, w/3, h, 0, false);
	    y -= h/2;
	    h /= 2;
	    if(h == 0) return;
	}
    }

    Point singleSiblingSide(FlobSet into, ZZCell outside, int x, int y,
	int w, float fract, int dir, int depth, int side) {
	ZZCell cur = outside;
	for(int i=1; (cur=cur.s(replies_list,side))!=null; i++) {
	    if(cur.s(replies_list, -1) == null) break;
	    float mult = fract / i;
	    x -= side * mult * xstep;
	    y += side * mult * ystep;
	    int h = sibl.getHeight(cur, cfr(fract), w);
	    sibl.makeFlob(into, cur, cur, cfr(fract),
		    x, y, 1+50*depth + 2*i, w, h,
		0, false);
	    mksideFlobs(into, cur, x, y, w+1, h, 1+50*depth);
	}
	return new Point(x,y);
    }
    
    public void siblingSeries( FlobSet into, ZZCell cur, int x, int y, 
			       int w,
			       float fract, int dir, int depth) {
	// dept
	int h = sibl.getHeight(cur, cfr(fract), w);
	p("Sibser: "+cur+" "+h+" "+w+" "+x+" "+y);
	sibl.makeFlob(into, cur, cur, cfr(fract),
	    x, y, 1+50*depth, w+1,h, 
	    0, (depth <= 2));
	if(depth > 2)
	    mksideFlobs(into, cur, x, y, w+1, h, 1+50*depth);
	Point p0 = singleSiblingSide(into, cur, x, y, w, fract, dir, depth, 1);
	Point p1 = singleSiblingSide(into, cur, x, y, w, fract, dir, depth, -1);
	into.add(new LineDecor(p0.x, p0.y, p1.x, p1.y, Color.red, 1));
    }
    

    public void raster( FlobSet into, FlobFactory fact,
			ZZCell view, String[] dims, ZZCell accursed){
	accursed=accursed.h(text);
	Dimension rd = into.getSize();
	Dimension dprod = new Dimension(rd.width,70);
	Dimension d = rd;

	ZZCell cur = accursed;

	int x = d.width / 3;
	int y = d.height / 3;
	int w = d.width / 3;
	float fr = 1.3f;

	ZZCell child = accursed.s(replies, 1);

	if(child != null) {
	    child = child.s(replies_list, 1);
	    siblingSeries(into, child, 2 * d.width / 3, d.height / 2, w, 
		(float)(fr * 1.0), 0, 1);
	}

	for(int pd = 0; pd<4 && cur != null; pd++) {
	    siblingSeries(into, cur, x, y, w,
		    fr, 0, 2 + pd);
	    cur = cur.h(replies_list).s(replies,-1);
	    fr *= 0.9;
	    w *= 0.9;
	    x -= 70;
	    y -= 70;
	}
	
	if(isProd(accursed)){
	    messagesFromProd(into,accursed,(float)1.0);
	    return;
	} else if(isAuthor(accursed)){
	    messagesFrom(into,accursed,(float)1.0, author);
	    return;
	}
	
	addAuthor(into,accursed,1.0f);
	
	drawProducts(into, (float)1.0, dprod);
	
    }
}



