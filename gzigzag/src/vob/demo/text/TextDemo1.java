/*   
TextDemo1.java
 *    
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
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
 * Written by Benja Fallenstein and Tuomas Lukka
 */

package org.gzigzag.vob.demo;
import org.gzigzag.vob.*;
import org.gzigzag.*;
import org.gzigzag.impl.*;
import org.gzigzag.test.*;
import java.util.*;
import java.awt.*;
import junit.framework.*;
import java.awt.event.*;

/** At the same time, a runnable demo and a test case.
 */

public class TextDemo1 extends TestCase {

    TextModel m1 = new TextModel();
    TextModel m2 = new TextModel();

    LinebreakableChain ch;

    TextBeamer beamer;

    String d1 = "Trying to come up with simple examples that illustrate "+
     "our general Vob design is not easy; however, the most difficult part "+
     "of this demo was selecting the underlying text stream to use. "+
     "As you can see, we didn't manage to make a choice: we wrote our own. "+
     "Here, you can see how the referential text model allows displaying "+
     "reorganizations of the original text in a meaningful manner. ";

    int[] str2 = new int[] {
	d1.indexOf("Here, y"), d1.length(),
	0, d1.indexOf("As you can"),
	-1, -1,
	d1.indexOf("As you can"), d1.indexOf("Here, y"),
    };
    String[] d2str = new String[] {
	"To clearly demonstrate this view, the text needs to "+
	"have been rearranged and edited. "
    };


    int width = 800; int height = 600;

    Frame frame = new Frame("Vob text demo 1");
    Canvas c = new Canvas() {
	Image cache ;
	public void update(Graphics g) {
	    if(cache == null) cache = createImage(width, height);
	    Graphics g1 = cache.getGraphics();
	    g1.clearRect(0, 0, width, height);
	    sc.render(g1, Color.black, Color.white, null, 0);
	    g.drawImage(cache, 0, 0, null);
	    getToolkit().sync();
	}
    } ;

    VobScene sc = new TrivialVobScene(new Dimension(width, height));

    {
	c.setSize(width, height);
	c.setBackground(Color.white);
	frame.add(c); frame.pack(); frame.show();
	c.addMouseMotionListener(new MouseMotionAdapter() {
	    public void mouseMoved(MouseEvent e) {
		synchronized(TextDemo1.this) {
		    beamer.setBeamCenter(e.getX(), e.getY());
		    c.repaint(1);
		}
	    }
	});
    }

    public void setUp() {
	boolean ok = false; // dispose windows if trouble.
	try {
	    synchronized (this) {
		TextModel.Cursor curs = m1.getCursor();

		TextScrollBlock sb = new TransientTextScroll();
		// TestUtil.append(sb, "abc def ghi jkl mno pqr stu vwx");
		TestUtil.append(sb, d1);
		TextSpan all = (TextSpan)sb.getCurrent();

		curs.insert(all);

		curs = m2.getCursor();
		for(int i=0; i<str2.length; i+=2) {
		    if(str2[i] < 0) 
			curs.insert(d2str[-str2[i]-1]);
		    else
			curs.insert((TextSpan)all.subSpan(str2[i], str2[i+1]));
		}

		VobBox subsc = sc.createSubScene(m1, null, width/2-20, height);
		m1.putTo(subsc, width/2-60);
		sc.put(subsc, 1, 0, 100, width/2-20, height);

		subsc = sc.createSubScene(m2, null, width/2-20, height);
		m2.putTo(subsc, width/2-60);
		sc.put(subsc, 1, width/2+60, 0, width/2-20, height);

		beamer = new TextBeamer();
		beamer.decorate(sc);
		beamer.putInto(sc);
	    }

	    c.update(c.getGraphics());
	    c.repaint();
	    ok = true;
	} finally {
	    if(!ok) tearDown();
	}
    }

    public void tearDown() {
	frame.dispose();
    }

    public TextDemo1(String s) {
	super(s);
    }

    public static void main(String[] argv) {
	TextDemo1 foo = new TextDemo1("");
	foo.setUp();
	try {
	Thread.sleep(1000000000);
	} catch(Exception e) {
	    throw new Error(" "+e);
	}
    }






// TESTS

    public void testChain() {
	ch = m1.makeChain(); 
	assertEquals(" "+m1, 67, ch.length());
    }

    public void testScene() {
	// Need to find abc
	int n = 0; int nt = 0;
	String txt = null;
	for(Iterator i = sc.vobs(); i.hasNext(); ) {
	    Vob v = (Vob) i.next();
	    if(v instanceof TextVob) {
		TextVob t = (TextVob)v;
		if(t.getText().equals("come")) return;
		nt ++;
		txt = t.getText();
	    }
	    n++;
	}
	fail("abc Not found " + n + " "+ nt + " '" + txt + "' ");
    }

}
