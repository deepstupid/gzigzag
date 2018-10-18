/*   
SimpleLineBreaker.java
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
package org.gzigzag;
import java.util.*;

public class SimpleLineBreaker extends LineBreaker {
public static final String rcsid = "$Id: SimpleLineBreaker.java,v 1.11 2000/09/19 10:32:00 ajk Exp $";
    public static boolean dbg = false;
    final static void p(String s) { if(dbg) System.out.println(s); }
    final static void pa(String s) { System.out.println(s); }

    public int[] breakLines(LineInfo li) {
	int n = li.getMax();

	// Can't we do without this?
	if(n==0) return new int[] {0, 0};

	int xc = li.getCursor();
	int l = li.getCenterLine();

	int startmin = 0, startmax = 0;
	boolean gotmin = false, gotmax = false;
	boolean ret = false;
	int curstart = l;
	    
	while(true) {
	    Vector res = new Vector();;
	    int lenn = 0;
	    int line = curstart;
	    for(int cur = 0; cur < n; cur += lenn) {
		lenn = oneGoodLine(cur, n-cur, n-cur, line, li, 6);
		p("OGL RET: "+cur+" "+n+" "+line+" "+li+" : "+lenn);
		p("CURS: "+xc+" "+line+" "+l);
		if(xc >= cur && xc <= cur+lenn) {
		    p("Cursor was on this line");
		    // We hit the cursor here.
		    if(line > l) {
			// line greater -> move upwards. This is a maximum.
			startmax = curstart; gotmax = true;
			curstart -= (line - l);
		    } else if(line < l) {
			// line smaller -> move down. This is a min.
			startmin = curstart; gotmin = true;
			curstart += (l - line);
		    }
		    if(line == l) ret = true;
		}
		res.addElement(new Integer(cur + lenn));
		line ++;
	    }

	    if(gotmin && gotmax && startmax - startmin < 2) {
		ret = true;
		curstart = startmin;
	    } else {
		if(gotmin && curstart <= startmin) {
		    curstart = startmin + 1;
		}
		if(gotmax && curstart >= startmax) {
		    curstart = startmax - 1;
		}
	    }

	    p("Ret: curstart = "+curstart);

	    if(ret) {
		int[] r = new int[res.size() + 1];
		r[0] = curstart;
		for(int i=1; i<r.length; i++) {
		    r[i] = ((Integer)res.elementAt(i-1)).intValue();
		    p("Line "+(i-1)+": "+r[i]);
		}
		return r;
	    }
	}
    }

    /** Starting at x0, find a good amount of text to include in line l.
     */
    int oneGoodLine(int x0, int guessn, int maxn, int l, LineInfo li,
	    int accept) {
	int[] res = new int[2];
	int minn = 0; 
	int splitpen = 0;
	int best = 0;
	int bestpen = 1000000;
	while(true) {
	    li.widthPenalty(x0, guessn, l, res);
	    p("OGL iter: "+x0+" "+minn+" "+guessn+" "+maxn+" R: "+res[0]);
	    int widpen = res[1];
	    int pen = splitpen + widpen;
	    if(pen < bestpen) {
		bestpen = pen; best = guessn;
	    }
	    // if(widpen <= accept) return best;
	    if(res[0] > 1000) {
		p("Too wide");
		maxn = guessn;
		li.split(x0, guessn, x0+(guessn*1000/res[0]), res);
		p("Split res: "+guessn+" "+res[0]+" "+minn);
		if(guessn == res[0] || minn == guessn) return best;
		guessn = res[0];
		splitpen = res[1];
	    } else {
		p("Too narrow");
		minn = guessn;
		li.split(x0+guessn, maxn-guessn, 
			    x0+guessn+
			    ((1000000/(res[0]+1) - 1000) * (maxn-guessn))/1000,
			    res);
		res[0] += guessn;
		if(guessn == res[0] || maxn == guessn) return best;
		guessn = res[0];
		splitpen = res[1];
	    }
	    if(minn >= maxn || guessn <= minn || guessn >= maxn) return best;;
	}
    }


}
