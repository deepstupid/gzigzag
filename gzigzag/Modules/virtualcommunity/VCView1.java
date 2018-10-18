/*   
VCView1.java
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
 * Written by Vesa Parkkinen and Tuomas Lukka
 */

package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;

/** First (bad) virtualcommunity view.
 */
public class VCView1 extends VirtualCommunity {
    public static final String rcsid = "$Id: VCView1.java,v 1.4 2001/02/27 23:49:30 veparkki Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log(s); }
    private static void pa(String s) { ZZLogger.log(s); }

    public void raster( FlobSet into, FlobFactory fact,
			ZZCell view, String[] dims, ZZCell accursed){
	ZZCell hist = null, hilite = null;
	if (!nohistory) {
	    hist = ZZDefaultSpace.findInheritableParam(view, "history");
	    if (hist == null) {
		hist = view.h("d.2",1).N("d.2");
		hist.setText("history");
	    }
	    hilite = hist.h("d.1", 1, true);
	}
	accursed=accursed.h(text);
	if (!nohistory) {
	    if (accursed.h("d.1").equals(hist)) {
		hilite = accursed;
		accursed = ZZCursorReal.get(accursed);
	    }
	    if (hilite == null) {
		hilite = hist.N("d.1");
		ZZCursorReal.setcursor(hilite, accursed);
	    }
	    histobs.init(view, hilite);
	    hilite = histobs.histpos;
	}
	Dimension rd = into.getSize();
	int head = rd.height/15;
	if (nohistory) head = 0;
	Dimension d = new Dimension(rd.width,rd.height-head);
	int w  = (int)(d.width * right);
	int h  = (int)(d.height * middleHeight);
	int cx = w/2;
	int cy = (int)( (d.height - h) / 2);
	int vUnit = (int)(cy / 13); 
	int lw = (int)(d.width * left);
	// gapheight=1/13, line=3/13;4*gap+3*lines=1 
	
	int bc = 0;
	
	// pointer
	ZZCell p = accursed;
	// another pointer
	ZZCell ptr = p;
	// used to hold kahva cells
	Hashtable handles = new Hashtable(); 
	Hashtable flobs = new Hashtable();
	
	int fh = d.height;
	int fw = (int) (right * d.width);
        
	Flob centerFlob = null;

	float fr = ((float)d.width) / 100000.0f;

	int magic = 20;
	// Draw the history
	int div = 1;
	if (!nohistory) div = hist.getRankLength("d.1")-1;
	if (div > magic) div = magic;
	int hw = (d.width/div)-2, sep=2;
	if (hw > head*3) { /*sep+=hw-head*3;*/ hw = head*3; }
	int hx = d.width-1-hw;
	ZZCell it = null;
	if (!nohistory) hist.s("d.1");
	int cnt=0;
	if (!nohistory) {
	    while (!it.equals(hilite)) { cnt++; it = it.s("d.1"); }
	    if (cnt*2+1 > magic) cnt = (magic-1)/2;
	    cnt++;
	    while (cnt++ < magic) {
		if (it.s("d.1") != null) it = it.s("d.1");
		else break;
	    }
	}
	cnt = 0;
	centerFlob = new Flob(0, 0, 1, d.width, head, hist) {};
	into.add(centerFlob);
	if (!nohistory)
	    while (!it.equals(hist) && cnt++ < magic) {
		ZZCell hit = ZZCursorReal.get(it);
		int hy = 0;
		if (hilite != null && it.equals(hilite)) hy = 5;
		history.makeFlob(into, hit, it, cfr(fr*hw), hx, hy, 1, hw, head).
		    setParent(centerFlob);
		it = it.s("d.1",-1);
		hx -= sep+hw;
	    }

	//-------------------------------------------
	// we have different possibilities:
	//    author cell
	//     - headcell in the author dim 
	//
	//    product cell
	//     - no author dim
	//     - head cell in the kahva dim
	//
	//    message cell
	//     - have author, not the head cell
	//-------------------------------------------
	
	//if ( fr > 1 ) fr = 1; 
	//if ( fr < 0.7 ) fr = 0.7f;
	p("_____________fr = " +fr); 
	// make the big blob in the middle 
	centerFlob = center.makeFlob( into, accursed, accursed, cfr(fr*w), 
        		 cx, cy+head, 1, w, h , 0.0f, true);
	
	//-------------------------------------------
	// handle the siblings, parent and children
	//-------------------------------------------

        p = accursed; 
        
	int cSibl = 0;
	
        int sY = cy; 
        int sX = cx;

	// siblings above the center blob
	// calculate the room...
	int rc = 0;//p.getRankLength(replies_list) -1;
	while( ( p = p.s(replies_list, -1) ) != null ){
	    rc++;
	}
	rc--;
	
	p("got " + rc + " siblings");
	
	p = accursed; 
	
 	int sroom = (d.height - h)/2;
	if( rc > 0 ) 
	    sroom = sroom / rc;
	//fr = (float)0.7;
	while( ( p = p.s(replies_list, -1) ) != null &&
	       !p.equals( p.h(replies_list, -1)) && ( cSibl < rc ) ) {
            sY = sY - sroom+10;  
	    
            //sibl.makeFlob( into, p, p, (float)1.0,
	    //		   sX, sY, 1, w, 3*vUnit);
	    blobs.makeFlob( into, p, p, cfr(fr*w),
			    sX, sY+head, 1, w, sroom - 20, 0.1f);
	    
            cSibl = cSibl + 1;
	    //fr -= 0.2;
        }
	
	// parent 
	ZZCell parent = accursed.h(replies_list,-1);
	if( !parent.equals(accursed)){
	    int qx = sX-w/2 +2, qy = 5+head, qw = w/2-10, qh = sroom-20;
	    parent = parent.s(replies, -1);
            sibl.makeFlob( into, parent, parent, cfr(fr*(w/2-10)),
			   qx + qw/2, qy + qh/2, 1, qw , qh,
			   -0.1f);
	    parent = parent.h(replies_list, -1, true);
	    if (parent != null) {
		parent = parent.s(replies, -1);
		sibl.makeFlob( into, parent, parent, cfr(fr*(w/2-10)),
			       qx - qw/4, qy - qh/4, 1, qw , qh,
			       0.07f);
	    }
	    /*into.add(new LineDecor(cx,cy+h/2,sX-w/2+2+(w/2-10)/2,
	      5+sroom-20,linecol,2));*/
	}       
	
	p = accursed; 
        cSibl = 0;
	
        sY = cy + h + vUnit; 
	
	rc = 0;
	while( ( p = p.s(replies_list,1) ) != null ){
	    rc++;
	}
	
	p("got " + rc + " siblings");
	
	sroom = (d.height - h)/2 - 6;
	if( rc > 0 ) 
	    sroom = sroom / rc;
	
	p = accursed;
	
	//fr = 0.7f;
	
	// child
	ZZCell child = accursed.h(replies,1);
	if( child != accursed ){
	    int qx = sX+w+10, qy = sY+head, qw = w/2, qh = d.height-5-sY;
	    child = child.s(replies_list, 1);
	    if( child != null) {
		ZZCell child2 = child.s(replies_list, 1);
		if (child2 != null) {
		    sibl.makeFlob( into, child2, child2, cfr(fr*(w/2)),
				   qx+qw/8, qy-qh/2, 1, qw, qh);
		}
		sibl.makeFlob( into, child, child, cfr(fr*(w/2)),
			       qx, /*d.height - 3*vUnit-5*/
			       qy-qh, 1, qw, qh/*3*vUnit*/);
	    }
	    //into.add(new LineDecor(cx+w/2,cy+h,sX+w+10,middle,linecol,2));
	} 
	
	// siblings below the center blob
        while( (p = p.s(replies_list, 1) ) != null && (cSibl < rc) ) {
            blobs.makeFlob( into, p, p, cfr(fr*w),
			    sX, sY+10+head, 1, w, sroom-20);
            cSibl = cSibl + 1;
            sY = sY + sroom;  
        }
		
	int th = 0;
	
	//-------------------------------------------
	// handle the other blobs 
	// depending on the center blob type
	//-------------------------------------------
	
	//-------------------------------------------
	// put all the handles to right side
	// in every case
	//-------------------------------------------
	
 	// loop through the text cells, puts handle cells 
	//  to handles
	// XXX loop	
	p = accursed;
	while( (p = p.s(text,1) ) != null){
            ptr = p.h(handle);
	    if( p.getRankLength(handle) > 1 ) {
		handles.put( ""+bc, ptr);
		bc++;
	    }
	}
	
	if ( bc != 0 ) 
	    fh= d.height / bc;

	// blobs on the right side 
	for( int i = 0; i < bc; i++ ){
	    ptr = (ZZCell)handles.get(""+i);
	    p("th = " + th + " ptr " + ptr); 
	    p("fw = " +fw );
	    flobs.put(ptr, blobs.makeFlob( into, ptr, ptr, cfr(fr*(fw-2*xpad)),
					   d.width - fw-xpad, th+ypad+head, 1, 
					   fw-2*xpad, fh-2*ypad));
	    th += fh;
	}
	
	//-------------------------------------------
	// draw the links. 
	// not correct, but SFW, this is just a demo
	//-------------------------------------------
	for( int i = 0; i< bc; i++ ){
	    Flob flo=(Flob)flobs.get(handles.get(""+i));
	    Point pq = flo.getPoint(-1,0);
	    p("flo: " + flo + "p: " + pq);
	    Point pc = centerFlob.getPoint(1,0);
	    Flob l = new LinkFlob(pc.x,pc.y,pq.x,pq.y-flo.h/5,pq.x,pq.y+flo.h/5);
	    into.add(l);
	}

	bc=0;
	
	//-------------------------------------------
	// Depending the cell type, put the blobs 
	// on the left
	//-------------------------------------------
	
	bc = 0;
	handles = new Hashtable();
	p=accursed.h(author,-1);
	
	// author ?
	if( accursed == p  && p.getRankLength(author) > 1 ){
	    p("AUTHOR DUDE");
	    // XXX loop		    
	    while( (p = p.s(author,1) ) != null){
		//ptr = p.h(text, -1);
		p("message");
		//if( p.getRankLength(handle) > 1 ) {
		handles.put( ""+bc, p);
		bc++;
	    }
	    
	} else if ( accursed == (p=accursed.h(handle,-1)) &&
		    p.getRankLength(handle) > 1){
	    p("PRODUCT");
	    // XXX loop		    
	    while( (p = p.s(handle,1) ) != null){
		ptr = p.h(text, -1);
		//p("message");
		//if( p.getRankLength(handle) > 1 ) {
		handles.put( ""+bc, ptr);
		bc++;
	    }
	    
	}
	
	p("BC = " + bc);
	
	th = 0;
	if ( bc != 0 ) 
	    fh= d.height / bc;
	
	// blobs on the right side 
	for( int i = 0; i < bc; i++ ){
	    ptr = (ZZCell)handles.get(""+i);
	    p("flob message");
	    p("th = " + th + " ptr " + ptr); 
	    flobs.put(ptr,blobs.makeFlob( into, ptr, ptr, cfr(fr*(w-2*xpad)),
					  d.width-w-xpad, th+ypad+head, 1, 
					  w-2*xpad, fh-2*ypad));
	    th += fh;
	}
	
	//-------------------------------------------
	// draw the links. 
	// not correct, but SFW, this is just a demo
	//-------------------------------------------
	for( int i = 0; i< bc; i++ ){
	    Flob flo=(Flob)flobs.get(handles.get(""+i));
	    Point pq = flo.getPoint(-1,0);
	    p("flo: " + flo + "p: " + pq);
	    Point pc = centerFlob.getPoint(1,0);
	    Flob l = new LinkFlob(pc.x,pc.y,pq.x,pq.y-flo.h/5,pq.x,pq.y+flo.h/5);
	    into.add(l);
	}
	/*
	// draw the links
	FlobDecorator linker = new VCLinker();
	linker.decorate(into, "", view);
	*/
	

	
    }


}
