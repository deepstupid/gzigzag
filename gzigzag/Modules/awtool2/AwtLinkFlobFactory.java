/*   
AwtArtefactFlobFactory.java
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
 * Written by Kimmo Wideroos
 */

package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;

/** 
 */
 
class AwtLinkFlobFactory extends CellFlobFactory1 {
public static final String rcsid = "";
    public static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }

    class AwtLinkFlob extends CellBgFlob {
	
	Point from, to;
	int[] pts = null;
	int R = 3;
	
	public AwtLinkFlob(Point from, Point to, int d, ZZCell c) {
	    super(from.x, from.y, d, to.x-from.x, to.y-from.y, c);
	}
	
	public AwtLinkFlob(int[] p, int d, ZZCell c) {
	    super(p[0], p[1], d, p[p.length-2]-p[0], p[p.length-1]-p[1], c);
	    this.pts = p;
	}
	
	public void render(Graphics g, int mx, int my, int md,
		       int mw, int mh) {
	    int midx, midy;
	    Color oldfg = g.getColor();
	    
	    g.setColor(Color.black);
	    //if(pts == null) {
	    g.drawLine(mx, my, mx+mw, my+mh);
	    int[][] a = arrowhead(mw, mh);
	    if(AwtLinkRelation.linkType(c, AwtLinkRelation.LINK_TO)) {
		g.drawLine(mx+mw, my+mh, mx+mw+a[0][0], my+mh+a[0][1]);
		g.drawLine(mx+mw, my+mh, mx+mw+a[1][0], my+mh+a[1][1]);
	    }
	    //}
	    /*
	      else {
	      for(int i = 0; i<pts.length-4; i=i+2)
	      g.drawLine(pts[i], pts[i+1], pts[i+2], pts[i+3]);
	      }
	    */ 
	    
	    Color[] solids = getSolidColors();
	    
	    if(solids!=null) {
		g.setColor(solids[0]);
		midx = mx+(mw>>1);
		midy = my+(mh>>1);
		g.fillOval(midx-R, midy-R, 2*R, 2*R);
	    }
	    
	    g.setColor(oldfg);
	}

	private int[][] arrowhead(int w, int h) {
	    if(w==0 && h==0) 
		return new int[][] {{0, 0}, {0,0}};
	    double lng = Math.sqrt(w*w+h*h);
	    double i1, i2, j1, j2;
	    i1 = 3*R*w / lng; i2 = 3*R*h / lng;
	    j1 = -i2; j2 = i1;
	    return new int[][] {{(int)(-i1+j1), (int)(-i2+j2)},
				{(int)(-i1-j1), (int)(-i2-j2)}};
	}

	public Object hit(int x, int y) {
	    
	    /*
	      int midx, midy;
	      
	      midx = this.x+(w>>1);
	      midy = this.y+(h>>1);
	      
	      if(x-midx > R) return null;
	      if(x-midx < -R) return null;
	      if(y-midy > R) return null;
	      if(y-midy < -R) return null;
	      
	      return new ZZCursorVirtual(super.c);
	    */
	    
	    double dy, dx, wwhh, k, dpow2;
	    dy = y-this.y;
	    dx = x-this.x;
	    wwhh = w*w+h*h;
	    
	    if(dy*dy+dx*dx>(wwhh+9*R*R)) return null; 
	    if(wwhh<9*R*R) {
		// near point!
		return new ZZCursorVirtual(super.c);
	    }
	    k = (double)(w*dy - h*dx);
	    dpow2 = k*k / wwhh;
	    if(dpow2<225.0) {
		// near enough the line
		return new ZZCursorVirtual(super.c);
	    }
	    return null;
	    
	}
    }
    
    protected AwtMetrics metrics;

    public AwtLinkFlobFactory(AwtMetrics awtm) { this.metrics = awtm; }
    public AwtLinkFlobFactory() { this.metrics = null; }

    public void setMetrics(AwtMetrics awtm) { this.metrics = awtm; }

    public Flob makeFlob(FlobSet into, ZZCell c, ZZCell handleCell,
                         float fract, int x, int y, int d, int w, int h) {

	Flob lf;
	Point p1 = new Point(x, y);
	Point p2 = new Point(w, h);
	Point p = new Point();
	lf = new AwtLinkFlob(p1, p2, d, c);
	CellFlobFactory2.addSolidColors(into, (CellBgFlob)lf);
        into.add(lf);
        
        // draw text, if there is ...
	/* 
        if(!c.t().equals("")) {
            super.centerFlob(into, c, c, 1, p, 0, 0, d);
        } 
	*/
        // if link is operation and has some result artefacts, draw link
        // between link content and result artefacts

	/*
        ZZCell r = AwtLinkRelation.result(c);

        while(r!=null) {
            src = AwtLinkRelation.source(r); trg = AwtLinkRelation.target(r);
            p1 = metrics.mapToRealView(AwtArtefact.getCoord(src));
            p2 = metrics.mapToRealView(AwtArtefact.getCoord(trg));
            into.add(new AwtLinkFlob(p1, p2, d, r));
            r = AwtLinkRelation.next(r);
        }
	*/

        return lf;
    }

    public Flob makeFlob(FlobSet into, ZZCell c, ZZCell handleCell,
                         float fract, double x1, double y1, int d, 
			 double x2, double y2) {
	Point p1 = metrics.mapToRealView(x1, y1);
	Point p2 = metrics.mapToRealView(x2, y2);
	return makeFlob(into, c, handleCell, fract, p1.x, p1.y, d, p2.x, p2.y);
    }

    public Flob placeFlob(FlobSet f, 
			  ZZCell c, ZZCell handleCell,
			  float fract, 
			  int x, int y, int depth,
			  float xfract, float yfract)
	throws UnsupportedOperationException { 
	throw new UnsupportedOperationException();
    }
                        

    // should call centerFlob(..., null)
    public Flob centerFlob(FlobSet f,
			   ZZCell c, ZZCell handleCell,
			   float fract, Point p, int xalign, int yalign,
			   int depth) 
	throws UnsupportedOperationException { 
	throw new UnsupportedOperationException();
    }

    // if d==null, use what getSize would return
    public Flob centerFlob(FlobSet f,
			   ZZCell c, ZZCell handleCell,
			   float fract, Point p, int xalign, int yalign,
			   int depth, Dimension d) 
	throws UnsupportedOperationException { 
	throw new UnsupportedOperationException();
    }

}




