/*   
BeamDecor2.java
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
 * Written by Tuomas Lukka
 */
/*
 * Modified by Kimmo Wideroos
 */
 
package org.gzigzag;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** A class for drawing curved line decorations.
 * Beams are currently irregular octagons.
 * NOTE: Beams are rendered in reverse order: the first beam put in here
 * becomes the topmost.
 */

public class BeamDecor2 extends CoordDecor {
public static final String rcsid = "";
    public static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }

    Color[] colors;

    // textframe polygon array indices: LY1 = left_y_upper, etc.
    static int LY1=0, LY2=1, LX=2, RY1=3, RY2=4, RX=5, XF=6, XB=7;

    /** A simple class that knows how to efficiently build
     * these things.
     */
    static public class Builder extends CoordDecor.Builder {
	Color []col;
	public Builder(FlobSet into) {
	    super(into);
	}

	protected void makeRoom(int l) {
	    if(col != null && nth >= col.length)
		endl();
	    super.makeRoom(l);
	}

	protected void alloc() {
	    super.alloc();
	    col = new Color[np/16];
	}



        protected void enlargePolygon(int[] p, int p_segm, int[] xy, int xy_segm) {
            if(xy[xy_segm+0]==p[p_segm+XF]) {
                // enlarge to the right
                p[p_segm+XF] = xy[xy_segm+6];
                if(p[p_segm+RX]<p[p_segm+XF])
                    p[p_segm+RX]=p[p_segm+XF];
                return;
            }

            if(xy[xy_segm+6]==p[p_segm+LX]) {
                // enlarge to the left
                p[p_segm+XB] = xy[xy_segm+0];
                if(p[p_segm+LX]>p[p_segm+XB])
                    p[p_segm+LX]=p[p_segm+XB];
                return;
            }

            if(xy[xy_segm+1]>=p[p_segm+LY2]) {
                // enlarge downwards
                p[p_segm+XF] = xy[xy_segm+6];
                p[p_segm+RY2] = p[p_segm+LY2];
                p[p_segm+LY2] = xy[xy_segm+3];
                if(xy[xy_segm+0]<p[p_segm+LX])
                    p[p_segm+LX] = xy[xy_segm+0];
                if(p[p_segm+RX]<p[p_segm+XF])
                    p[p_segm+RX]=p[p_segm+XF];
                return;
            }

            if(xy[xy_segm+3]<=p[p_segm+RY1]) {
                // enlarge upwards
                p[p_segm+XB] = xy[xy_segm+0];
                p[p_segm+LY1] = p[p_segm+RY1];
                p[p_segm+RY1] = xy[xy_segm+7];
                if(xy[xy_segm+6]>p[p_segm+RX])
                    p[p_segm+RX] = xy[xy_segm+6];
            }
        }

	/** Make frames around transcluded texts + draw a connection between
         *  them.
	 */
	public final void c(int []xy, Color c) {
            boolean new_beam = false;
            if(nth>0 && c.equals(col[nth-1])) new_beam = true;
            c(xy, c, new_beam);
        }

        //  @new_beam      start creation of a new frame pair
        public final void c(int []xy, Color c, boolean new_beam) {
	    p("curve: "+p+" "+col+" "+curp+" "+nth);
            if(curp==0 || new_beam) {
                makeRoom(16);
                // about xy-array:
                // xy[odd] are x-coordinates, xy[even] y's
                // 1st point is rectangle's upper left corner,
                // others follow counter-clockwise
                for(int i=0; i<16; i+=8) {
                    p[curp+LX+i] =  xy[i];
                    p[curp+LY1+i] = xy[1+i];
                    p[curp+LY2+i] = xy[3+i];
                    p[curp+RX+i] =  xy[6+i];
                    p[curp+RY1+i] = xy[7+i];
                    p[curp+RY2+i] = xy[5+i];
                    p[curp+XF+i] =  xy[6+i];
                    p[curp+XB+i] =  xy[i];
                }
                col[nth] = c;
                curp += 16;
                nth++;
            } else {
                enlargePolygon(p, curp-16, xy, 0);
                enlargePolygon(p, curp-8, xy, 8);
            }
	}

	protected Renderable create() {
	    Color[] col2 = col;
	    col = null;
	    return new BeamDecor2(p, np, d, col2);
	}

    }

    public BeamDecor2(int[] coords, int n, int d, Color[] cols) {
	super(coords, n, d);
	this.colors = cols;
    }


    /** distance^2 
     *  @n              size of xfrom[] and yfrom[]
     *  @denserer       virtualpoint-realpoint-cardinality-ratio
     *  @vfrom          virtualindex of starting point
     *  @vto                  "       " end point
     *  NB! virtualpoint values are interpolated from xfrom[] and yfrom[] points.
     */ 
    private int dist_2(int[] xfrom, int[] yfrom, int vfrom, int[] xto, int[]
                     yto, int vto, int n, int denserer) {

        int N = denserer*n;

        vfrom = (vfrom+N) % N;
        vto = (vto+N) % N;

        int rfrom1 = vfrom/denserer;
        int rfrom2 = (rfrom1+1) % n;

        int rto1 = vto/denserer;
        int rto2 = (rto1+1) % n;

        int vfoffs = vfrom % denserer;

        int vtoffs = vto % denserer;

        int dx = vtoffs*xto[rto2] + (denserer-vtoffs)*xto[rto1] 
                -vfoffs*xfrom[rfrom2] - (denserer-vfoffs)*xfrom[rfrom1]; 
                
        int dy = vtoffs*yto[rto2] + (denserer-vtoffs)*yto[rto1] 
                -vfoffs*yfrom[rfrom2] - (denserer-vfoffs)*yfrom[rfrom1];  

        return (dx*dx + dy*dy)/(denserer*denserer);
    }


    public void render(Graphics g) {
	Color old = null;
	old = g.getColor();

	int[][] x1 = new int[2][8];
	int[][] y1 = new int[2][8];

	int[][] x2 = new int[2][8];
	int[][] y2 = new int[2][8];
        
        int t, i, j, k;

        p("Start rendering beams!");

        for(i=(n-16)-n%16; i>=0; i-=16) {
            g.setColor(colors[i/16]);
            for(j=0; j<2; j++) {

                k = i+(j<<3); // k = i + 8j 

                t=coords[k+XB]; 
                x1[j][0]=t; x2[j][0]=t-1; x1[j][1]=t; x2[j][1]=t-1;
                t=coords[k+LX]; 
                x1[j][2]=t; x2[j][2]=t-1; x1[j][3]=t; x2[j][3]=t-1;
                t=coords[k+XF]; 
                x1[j][4]=t; x2[j][4]=t+1; x1[j][5]=t; x2[j][5]=t+1;
                t=coords[k+RX]; 
                x1[j][6]=t; x2[j][6]=t+1; x1[j][7]=t; x2[j][7]=t+1;

                t=coords[k+RY1]; 
                y1[j][0]=t; y2[j][0]=t-1; y1[j][7]=t; y2[j][7]=t-1;
                t=coords[k+LY1]; 
                y1[j][1]=t; y2[j][1]=t-1; y1[j][2]=t; y2[j][2]=t-1;
                t=coords[k+LY2]; 
                y1[j][3]=t; y2[j][3]=t+1; y1[j][4]=t; y2[j][4]=t+1;
                t=coords[k+RY2]; 
                y1[j][5]=t; y2[j][5]=t+1; y1[j][6]=t; y2[j][6]=t+1;
               
                g.drawPolygon(x1[j], y1[j], 8);       
                g.drawPolygon(x2[j], y2[j], 8);       
            }

            int c = 8, dens = 4, N = dens*c;
            int dmin, d1, d2;
            int n, m, offs = N>>1;
            
            d1 = dist_2(x1[0], y1[0], 0, x1[1], y1[1], offs, c, dens);
            d2 = dist_2(x1[0], y1[0], offs, x1[1], y1[1], 0, c, dens);

            n = 0; 
            m = offs; 
            dmin = d1;
            
            if(d2<dmin) { 
                n = offs; dmin = d2;
                m = 0;
            }

            for(offs = offs>>1; offs>0; offs = offs>>1) {

                d1 = dist_2(x1[0], y1[0], n-offs, x1[1], y1[1], m-offs, c, dens);
                d2 = dist_2(x1[0], y1[0], n+offs, x1[1], y1[1], m+offs, c, dens);

                if(d1<dmin && d1<d2) {
                    n = n-offs; dmin = d1; 
                    m = m-offs;
                } else
                if(d2<dmin) { 
                    n = n+offs; dmin = d2;
                    m = m+offs;
                }
            }

            n = (n+N) % N; // n e {0...N-1} 
            m = (m+N) % N;
                
            int toffs = n % dens;
            int foffs = m % dens;
            int n_dens = n/dens;
            int m_dens = m/dens;
                
            int from_x = ((dens-foffs)*x1[0][n_dens]+foffs*x1[0][(n_dens+1)%c])/dens; 
            int from_y = ((dens-foffs)*y1[0][n_dens]+foffs*y1[0][(n_dens+1)%c])/dens;
            int to_x = ((dens-toffs)*x1[1][m_dens]+toffs*x1[1][(m_dens+1)%c])/dens;
            int to_y = ((dens-toffs)*y1[1][m_dens]+toffs*y1[1][(m_dens+1)%c])/dens;
            
            g.drawLine(from_x, from_y, to_x, to_y);
            g.drawLine(from_x+1, from_y, to_x+1, to_y);
            g.drawLine(from_x, from_y+1, to_x+1, to_y);
        }
        
	g.setColor(old);
    }

}
