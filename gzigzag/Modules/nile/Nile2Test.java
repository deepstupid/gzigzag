/*   
Nile2Test.java
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

package org.gzigzag.module;
import org.gzigzag.*;

/** Some simple tests for Nile.
 */

public final class Nile2Test {
public static final String rcsid = "$Id: Nile2Test.java,v 1.1 2000/12/07 00:37:19 tjl Exp $";
    public static boolean dbg = true;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    static final void pok(String id, boolean ok) {
	if(ok) p("ok "+id);
	else p("not ok "+id);
    }

    public static void main(String[] argv) {
	ZZSpace z = new ZZDimSpace();
	ZZCell home = z.getHomeCell();
	ZZCell stream = home.N("d.1", 1);

	stream.N("d.nile-struct", 1); // Beginning structural marker
	stream = stream.N("d.nile", 1); // Can't be at same cell.
	stream.setText("Hello world a!  How's it going?");

	Nile2Iter it = new Nile2Iter(new ZZCursorVirtual(stream, 4));

	p("Got: "+it.get());

	Nile2Iter nextpart = it.cut();
	it.dumpStream();
	nextpart.dumpStream();

	p("Got: "+it.get());
	pok("Atend", it.get() == -1);
	pok("Prev", it.get(-1) == 'l');

	pok("foll", nextpart.get() == 'o');
	pok("atstart", nextpart.get(-1) == -1);

	it.dumpStream();
	p("Joining");
	it.join(nextpart);

	it.dumpStream();
	nextpart.dumpStream();

	pok("GET1", it.get() == 'o');
	pok("NEXT1", it.next() == ' ');
	pok("NEXT2", it.next() == 'w');
	pok("PREV1", it.prev() == ' ');

	// Next, test word boundaries.
	
	Nile2Unit un = new Nile2Unit.Word();
	pok("PREVWORD1", un.start(it, -1, false));
	pok("PREVWORD2", !un.start(it, -1, false));
	pok("Wordplace", it.get() == 'H' && it.get(1) == 'e');

	pok("NEXTWORD1", un.start(it, 1, true));
	pok("Wordplace2", it.get() == 'H' && it.get(1) == 'e');
	pok("NEXTWORD2", un.start(it, 1, false));
	pok("Wordplace3", it.get() == 'w' && it.get(1) == 'o');
	pok("NEXTWORD2", un.start(it, 1, false));
	pok("Wordplace2.5", it.get() == 'a' && it.get(1) == '!');
	pok("NEXTWORD2", un.start(it, 1, false));
	pok("Wordplace3", it.get() == '!' && it.get(1) == ' ');
	pok("NEXTWORD3", un.start(it, 1, false));
	pok("Wordplace4", it.get() == 'H' && it.get(1) == 'o');


	Nile2Unit un2 = new Nile2Unit.Sentence();
	pok("senst1", un2.start(it, -1, false));
	pok("senst2", !un2.start(it, -1, false));
	pok("Sen", it.get() == 'H' && it.get(1) == 'e');
	pok("senst3", un2.start(it, 1, false));
	pok("Sen", it.get() == 'H' && it.get(1) == 'o');
	it.dumpStream();
	pok("senst4", !un2.start(it, 1, false));
	it.dumpStream();
	pok("senst5", un2.start(it, -1, false));
	pok("senst6", un2.start(it, -1, false));
	pok("senst7", !un2.start(it, -1, false));
	// Now at beginning. 
	Nile2Iter end = (Nile2Iter)it.clone();
	pok("sene1", un2.end(end, 1, true));
	p("At end: "+(end.get(-1))+"||"+end.get());
	end.dumpStream();
	pok("sene2", end.get() == ' ' && end.get(-1) == '!');

	Nile2Iter result = un2.cut(it, end);

	p("Start:");
	it.dumpStream();
	p("End:");
	end.dumpStream();
	p("Res:");
	result.dumpStream();

	// Next, test adjustspaces.

	ZZCell n = stream.N("d.1", 1);
	n.setText("d");
	it.set(new ZZCursorVirtual(n, 0));
	pok("as1", it.get(-1) == -1 && it.get() == 'd' && it.get(1) == -1);
	it.adjustSpaces(0, 1);
	p("Adjusted: ");
	it.dumpStream();
	pok("as2", it.get(-1) == -1 && it.get() == 'd' && it.get(1) == ' ');
	it.adjustSpaces(0, 0);
	p("Adjusted3: ");
	it.dumpStream();
	pok("as3", it.get(-1) == -1 && it.get() == 'd' && it.get(1) == -1);
	it.adjustSpaces(1, 0);
	pok("as4", it.get(-1) == -1 && it.get() == ' ' && it.get(1) == 'd'
			&& it.get(2) == -1);
	it.adjustSpaces(0, 0);
	p("Adjusted5: ");
	it.dumpStream();
	pok("as5", it.get(-1) == -1 && it.get() == 'd' && it.get(1) == -1);
	


	pok("END", true);
	System.exit(0);
    }

}
