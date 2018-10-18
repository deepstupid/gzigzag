/*   
TestLines.java
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
import java.io.*;
import java.awt.*;

/** Some tests for line breaking.
 */

public class TestLines {
public static final String rcsid = "$Id: TestLines.java,v 1.10 2001/02/23 15:01:10 tjl Exp $";
    static final boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }

    Frame f = new Frame();

/* Jikes won't eat this any more in new versions due to äö.
    static final String str = "Pikku stringi testataksemme rivinpilkkomista. "+
"Tällä kertaa kirjoitammekin sen suomeksi - englanninkielisiä lukijoitamme "+
"varmaankin hämmästyttää. Etenkin ääkköset. Tämä merkkijono piirretään "+
"näytölle monta kertaa erilaisena, siis \"kursori\" eri kohdalle laitettuna. "+
"Tästä voidaan sitten nähdä, toimiiko rivien pilkkominen palasiksi oikein. " +
"Itse asiassa merkkijonoa tulee vielä jonkin verran pidentää koska muuten " +
"emme ehdi nähdä kaikkia efektejä. Hauskaahan tästä tulee vasta sitten " +
"kun voimme oikeasti muokata näin näytettyä tekstiä mutta tämä testi on " +
"tärkeä askel siihen suuntaan mentäessä."
;

*/

    static final String str = "lsjifs lefjis elfj sef jsle fjslf ";

    class MyLayout implements SimpleLineInfo.Layout {
	int c; 
	MyLayout(int c) { this.c = c; }
	public FontMetrics getFontMetrics(int line) {
	    return fm[find(c, line)];
	}
	public int getWidth(int line) {
	    return width(c, line);
	}
	public int getCenterLine() { return 0; }

    }

    public static void main(String[] argv) {
	TestLines t = new TestLines();
	t.br();
    }

    int NTESTS = 1;

    int[][] lines = new int[NTESTS][];
    Font[] fonts = new Font[64];
    FontMetrics[] fm = new FontMetrics[fonts.length];
    { for(int i=0; i<fonts.length; i++) {
	fonts[i] = new Font("SansSerif", Font.PLAIN, i+1);
	// fm[i] = Toolkit.getDefaultToolkit().getFontMetrics(fonts[i]);
	fm[i] = f.getFontMetrics(fonts[i]);
      }
    }

    /** Give the index to the fonts[] array for the given line of
     * the given case. 
     */
    int find(int cas, int line) {
	if(cas == 0) 
	    return 10 + line;
	else if(cas == 1) {
	    int w = 30 - line;
	    if(w < 3) return 3;
	    return w;
	} else if(cas == 2) {
	    return (int)(8 + 10 / (1 + (line * line)/30.0));
	}
	return -1;
    }

    int width(int cas, int line) {
	if(cas == 0)  {
	    int w = 100 + 20 * line;
	    if(w > 400) w = 400;
	    return w;
	} else if(cas == 1) {
	    int w =300 - 20 * line; 
	    if(w < 50) return 50;
	    return w;
	} else if(cas == 2) {
	    return (int)(100 + 200.0 / (1 + (line * line)/40.0));
	}
	return -1;
    }

    int curs = 0;
    int dir = 1;

public TestLines() {
}

public void br() {


    Component cp;
    f.add(
     cp= new Component() {
     Image cache;
    public void paint(Graphics g0) {
	Thread.dumpStack();
	if(lines == null) return;
	if(cache==null) {
	    cache = createImage(800,800);
	}
	Graphics g = cache.getGraphics();
	g.setColor(getBackground());
	g.fillRect(0, 0, 800, 800);
	for(int set = 0; set < lines.length; set++) {
	    if(lines[set] == null) return;
	    int cur = 0;
	    int x = 20 + 400 * set;
	    int y = 100;
	    int line = lines[set][0];
	    for(int i=0; i<lines[set].length-1; i++) {

		int cas = 2;

		FontMetrics metr = fm[find(cas, line)];
		y += metr.getLeading() + metr.getAscent();
		g.setColor(Color.black);
		g.setFont(fonts[find(cas, line)]);
		g.drawString(str.substring(cur, lines[set][i+1]).trim(), x, y);
		if(curs >= cur && curs < lines[set][i+1]) {
		    // Trim is wrong :(
		    int cw = metr.stringWidth(str.substring(cur, curs).trim());
		    g.setColor(Color.blue);
		    g.drawLine(x+cw, y-metr.getAscent(), 
			       x+cw, y+metr.getDescent());
		}

		g.setColor(Color.red);
		g.drawLine(x+width(cas, line), y-metr.getAscent(), 
			   x+width(cas,line), y+metr.getDescent());
		cur = lines[set][i+1];
		y += metr.getDescent();
		line ++;
	    }
	}
	g0.drawImage(cache, 0, 0, null);
    }
    });
    f.setSize(800,800);
    
    SimpleLineBreaker slb = new SimpleLineBreaker();
    f.show(); 

    while(true) {
	for(int i=0; i<lines.length; i++) {
	    int[] brk = slb.breakLines(
		    new SimpleLineInfo(str, curs, new MyLayout(2)));
	    lines[i] = brk;
	}
	cp.paint(cp.getGraphics());
	for(int k = 0; k<5; k++) {
	    curs += dir; 
	    if(curs <= 0 || curs >= str.length()-5) dir = -dir;
	}
    }
}
}
