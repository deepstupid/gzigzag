/*   
VCView2.java
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
 * Written by Tuomas Lukka and Vesa Parkkinen
 */


package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;

/** Second, cardstack-like virtualcommunity view.
 */

public class VCView2 extends VirtualCommunity {
    public static final String rcsid = "$Id: VCView2.java,v 1.20 2001/02/28 10:18:20 tjl Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log(s); }
    private static void pa(String s) { ZZLogger.log(s); }

    public void childSeries( FlobSet into, ZZCell cur, int x, int y,
	    int w, float fract, int orot, int dir, int depth) {
	ZZCell s;

	if(cur.s(replies) != null) {
	    s = cur.s(replies).s(replies_list);
	    if(s != null)
		siblingSeries(into, s, x, y, 
		    w, fract*magfract, orot, 1, depth+1);
	}
    }

    static int borderLimit = 10;

    final int rotinc = 1;
    final int SCOFFS = 10;

    double[]sins = new double[SCOFFS * 2];
    double[]coss = new double[SCOFFS * 2];

    // Point mkp(

    void addVert(Point p, double h, int rot) {
	p("Addvert: "+p+" "+h+" "+rot);
	p.x -= (int)(sins[rot+SCOFFS] * h);
	p.y += (int)(coss[rot+SCOFFS] * h);
	p("Ret: "+p);
    }
    

    void addHoriz(Point p, double w, int rot) {
	p.x += (int)(coss[rot+SCOFFS] * w);
	p.y += (int)(sins[rot+SCOFFS] * w);
    }

    int getWid(int w, float fract) {
	return (int)(w * Math.sqrt(fract));
    }

    int overlap = 10;
    int bend = -5;
  
    public void siblingSeries( FlobSet into, ZZCell cur, int x, int y, 
		    int w,
		    float fract, int orot, int dir, int depth) {
	if(depth >= view2depth) return;
	Dimension rd = into.getSize();
	ZZCell s;
	int rot = 0;//orot;
	
	Point curpoint = new Point(x,y);

	Point upstart, downstart;

	Point tmp = new Point();

    // We do things differently depending on where we are.
    // If going to positive direction, first see if we have odd
    // or even number of cells and either put a cell in the middle
    // or not

	boolean domiddle = true;

	if(dir == 1) {
	    cur = cur.h(replies_list);
	    int count = cur.getRankLength(replies_list) - 1;
	    if(count % 2 == 0) {
		domiddle = false;
	    }
	    // Either upper or middle
	    cur = cur.s(replies_list, (count+1)/2);
	}

	int cw = getWid(w,fract);

	if(domiddle) {
	    int h = sibl.getHeight(cur, cfr(fract), cw);
	    // h /= depth+1;
	    // h = (int)(80*fract);

	    // If not parent, put children for this center blob
	    if(dir >= 0) {
		Point px = new Point(curpoint);
		addHoriz(px, cw, rot);
		childSeries(into, cur, 
		    px.x, px.y,
		    w, fract, orot, 1, depth);
	    }

	    addVert(curpoint, -h/2, rot);
	    sibl.makeFlob(into, cur, cur, cfr(fract), 
		curpoint.x, curpoint.y, 10, cw, h, 
		rotunit * orot,
		(dir == 0));

	    upstart = new Point(curpoint);
	    downstart = new Point(curpoint);
	    addVert(downstart, h, rot);

	} else {
	    upstart = new Point(curpoint);
	    downstart = new Point(curpoint);
	}

	float cf = fract;

	s=cur;

	if(domiddle) s = s.s(replies_list, -1);
	for(; s != null; s = s.s(replies_list, -1)) {

	    // if(cy <= borderLimit) break;
	    // Don't do relcell
	    if(s.s(replies_list, -1) == null) break;
	    cf *= magfract;
	    rot -= rotinc;
	    cw = getWid(w,cf);

	    int ch = sibl.getHeight(s, cfr(cf), cw);
	    // ch /= depth+1;
	    // ch = (int)(80*cf);
	    addVert(upstart, -ch + overlap, rot);
	    addHoriz(upstart, bend, rot);
	    sibl.makeFlob(into, s, s, cfr(cf), 
		upstart.x, upstart.y, 
		10, cw, ch, 
		rotunit * rot,
		false);
	    tmp.setLocation(upstart);
	    addVert(tmp, ch/2, rot);
	    addHoriz(tmp, cw, rot);
	    childSeries(into, s, 
		tmp.x, tmp.y,
		w, cf, rot, 1, depth);
	}

	cf = fract;

	s=cur;
	rot = orot;
	while((s = s.s(replies_list, +1))!=null) {
	    // if(cy >= rd.height-borderLimit) break;
	    cf *= magfract;
	    rot += rotinc;
	    cw = getWid(w,cf);

	    addHoriz(downstart, bend, rot);
	    addVert(downstart, - overlap, rot);

	    int ch = sibl.getHeight(s, cfr(cf), cw);
	    // ch = (int)(80*cf);
	    // ch /= depth+1;
	    sibl.makeFlob(into, s, s, cfr(cf), 
		downstart.x, downstart.y, 
		10, cw, ch, 
		rotunit * rot,
		false);
	    tmp.setLocation(downstart);
	    addVert(tmp, ch/2, rot);
	    addHoriz(tmp, cw, rot);

	    childSeries(into, s, 
		tmp.x, tmp.y,
		w, cf, rot, 1, depth);
	    addVert(downstart, ch, rot);
	}
	

	// Parents
	s = cur.h(replies_list).s(replies, -1);
	if(dir <= 0 && s != null) 
	    siblingSeries(
		into, s, x - getWid(w, fract*magfract), 
		    y, w, (float)(fract * magfract),
		    orot, -1, depth+1);
		    






    }

    public void raster( FlobSet into, FlobFactory fact,
			ZZCell view, String[] dims, ZZCell accursed){
	accursed=accursed.h(text);

	Dimension rd = into.getSize();
	Dimension d = rd;
	Dimension dprod = new Dimension(rd.width,70);
    // Current rotation
	double rot = 0;

    {
	for(int i=0; i<SCOFFS*2; i++) {
	    sins[i] = Math.sin((i-SCOFFS)*rotunit);
	    coss[i] = Math.cos((i-SCOFFS)*rotunit);
	}
    }

	p("Rotunit: "+rotunit);

	siblingSeries(into, accursed, 
	    (int)(view2ctr[0] * d.width),
	    (int)(view2ctr[1] * d.height),
	    (int)(view2wid * d.width),
		(float)view2fract, 0, 0, 0);

	if(isProd(accursed)){ 
	    messagesFromProd(into,accursed,(float)1.0);
	    return;
	}else if(isAuthor(accursed)){
	    messagesFrom(into,accursed,(float)1.0, author);
	    return;
	}
	
	addAuthor(into,accursed,1.0f);
	
	drawProducts(into, (float)1.0, dprod);
    }
}



