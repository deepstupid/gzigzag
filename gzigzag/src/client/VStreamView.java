/*
VStreamView.java
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
 * Written by Benja Fallenstein
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.vob.*;
import java.awt.*;
import java.util.*;

/** A view showing a VStream.
 */

public class VStreamView implements View {
String rcsid = "$Id: VStreamView.java,v 1.2 2001/10/25 12:31:01 bfallenstein Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) System.out.println(s); }
    private static void pa(String s) { System.out.println(s); }

    TextStyle style = new RawTextStyle(new ScalableFont("serif", 0, 24),
				       java.awt.Color.black);

    TextStyle headingstyle = new RawTextStyle(new ScalableFont("serif", 
				    java.awt.Font.BOLD, 36),
				       java.awt.Color.black);

    boolean billow = false;
    boolean buoys = false;
    boolean multiparagraph = false;

    Linebreaker lbr = new SimpleLinebreaker();

    int[] uglyscales = new int[1000];
    { for(int i=0; i<uglyscales.length; i++) uglyscales[i] = 1000; }

    public VStreamView() { this(false, false, false); }
    public VStreamView(boolean billow) {  this(billow, false, false); }
    public VStreamView(boolean billow, boolean buoys) 
	    {  this(billow, buoys, false); }
    public VStreamView(boolean billow, boolean buoys, boolean multiparagraph) {
	this.billow = billow; this.buoys = buoys;
	this.multiparagraph = multiparagraph;
    }

    final int billowedScale(int line, int nlines) {
	// Distance from the center line.
	int dist = Math.abs(line - (nlines/2));
	return 1500 - (1000 * dist / nlines);
    }

    Dim headingDim = null;
    Dim streamDim = null;

    public void setStyle(CharArrayVobFactory fact, Cell c) {
	if(headingDim == null) {
	    headingDim = c.space.getDim(Dims.d_user_1_id);
	    streamDim = c.space.getDim(Dims.d_user_2_id);
	}
	if(headingDim.s(c, -1) != null) 
	    fact.setStyle(headingstyle);
	else 
	    fact.setStyle(style);
	fact.setStreamKey(c);
    }

    public void render(VobScene sc, Cell window) {
	//pa("Start VStreamView.render");

	Cell c = Cursor.get(window); // .getRootclone();
	VStreamDim vs = ((SimpleSpanSpace)window.space).getVStreamDim();
	
	Dimension size = sc.getSize();
	int w = size.width;
        int n = (size.height) / style.getHeight("xxx", 1000);
	if(n < 0) n = 10;

	int whole_w = w;
	int x = 0;
	BuoyPlacer bp1 = null, bp2 = null;
	if(buoys) {
	    w = w / 3;
	    int bw = (int)(w * 0.8);
	    bp1 = new BuoyPlacer(new Rectangle(0, 0, bw, size.height));
	    bp2 = new BuoyPlacer(new Rectangle(2*w, 0, bw, size.height));
	    x = w;
	} 

	CellVobFactory cvf = new CellVobFactory(c);

        CharArrayVobFactory fact = new CharArrayVobFactory(style, c);
        WordBreaker breaker = new WordBreaker(fact);
	LineCursorFilter lcf = new LineCursorFilter(breaker, 
						    style, Color.black);
	ConnectionFilter cf = new ConnectionFilter(lcf, style, bp1, bp2, cvf);

	Map extra = new HashMap();
	extra.put(CharRangeIter.CURSOR, window);

	setStyle(fact, c);
        vs.iterate(cf, c, extra);
        LinebreakableChain ch = fact.getChain();
        //pa("Chain length: "+ch.length());

        int[] lines = new int[n];
        int[] scales = new int[1];
        for(int i=0; i<n; i++) {
            lines[i] = w; 
        }
	scales[0] = 1000;

        Linebreaker.Broken bro = 
	    lbr.breakLines(ch, lines, scales, 0, 0);

	if(multiparagraph) {
	    int uh = (size.height-bro.height)/2;
	    int lh = size.height-uh;
	    bro.putLines(sc, x, uh, 10);
	    Cell uc = c.s(streamDim, -1);
	    Cell lc = c.s(streamDim, 1);
	    bro = null; // allow it to be garbaged.
	    while(uh > 0 || lh < size.height) {
		if(billow) scales[0] *= 0.8;

		uh -= 7 * scales[0] / 1000;
		lh += 7 * scales[0] / 1000;
		if(uc == null) uh = -1;
		else {
		    setStyle(fact, uc);
		    vs.iterate(cf, uc, null);
		    ch = fact.getChain();
		    bro = lbr.breakLines(ch, lines, scales, 0, 0);

		    uh -= bro.height;
		    bro.putLines(sc, x, uh, 10);

		    uc = uc.s(streamDim, -1);
		}
		if(lc == null) lh = size.height;
		else {
		    setStyle(fact, lc);
		    vs.iterate(cf, lc, null);
		    ch = fact.getChain();
		    bro = lbr.breakLines(ch, lines, scales, 0, 0);

		    bro.putLines(sc, x, lh, 10);
		    lh += bro.height;

		    lc = lc.s(streamDim, 1);
		}
	    }
	} else {
	    bro.putLines(sc, x, 0, 10);
	}


	if(buoys) {
	    bp1.place(sc);
	    bp2.place(sc);
	}

	//pa("VStreamView.render completed");
    }
}
