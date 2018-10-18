/*   
BenchVStreams.java
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

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.mediaserver.*;
import org.gzigzag.vob.*;
import org.gzigzag.client.Client;
import org.gzigzag.client.*;
import java.util.*;
import java.io.*;
import java.awt.*;

/** Test vstream speed.
 */

// 0000000008000000E83655154A00046326316BE429B5B7BA26F85E0DDA056FE67CDA38F0EA9CC4
// 0000000008000000E830CA6D5C00046DA13D804DCA6B1DB7264D56874235AFFD74C4B0D53FD9A1-3:0000000008000000E836550B4C00040DA1F81ABD5F95FE56FDFEF26FB62E564EA79F3B042022CC-3

public class BenchVStreams {
public static final String rcsid = "$Id: BenchVStreams.java,v 1.5 2002/03/18 08:34:07 tjl Exp $";
    static private void p(String s) { System.out.println(s); }

    static private class CreationTest {

	PermanentSpace space, space2;
	Mediaserver ms;
	Mediaserver.Id id;

	View view = new VobVanishingClient();
	Cell c1;
	Cell wc;
	Cell wc2;

	CreationTest() throws IOException {
	    ms = TestImpl.zms;
	    Mediaserver.Id spaceId = new Mediaserver.Id(
"0000000008000000E83655154A00046326316BE429B5B7BA26F85E0DDA056FE67CDA38F0EA9CC4"
		);
	    space = new PermanentSpace(ms, spaceId);
	    space2 = new PermanentSpace(ms, spaceId);

	    Cell clientCell = space.getHomeCell().s(Client.d1);
	    Cell scr = Params.getParam(clientCell, Client.c_screen);

	    Cell clientCell2 = space2.getHomeCell().s(Client.d1);
	    Cell scr2 = Params.getParam(clientCell2, Client.c_screen);

	    wc = Params.getParam(scr, Client.c_window);

	    wc2 = Params.getParam(scr2, Client.c_window);
	    Cursor.set(wc2, c1 = space2.getCell(
"0000000008000000E830CA6D5C00046DA13D804DCA6B1DB7264D56874235AFFD74C4B0D53FD9A1-3:0000000008000000E836550B4C00040DA1F81ABD5F95FE56FDFEF26FB62E564EA79F3B042022CC-3"
	    ));
	}
	
	void runTests(org.gzigzag.benchmark.Bench b) {

	    b.run(new Object[] {
		"GETTEXT_VSTREAM", "Get the text for a medium-sized vstream",
		new Runnable() { public void run() {
		    String s = c1.t();
		}},


	    });
	}
    }

    public static void main(String[] argv) throws IOException {
	org.gzigzag.benchmark.Bench b = new org.gzigzag.benchmark.Bench(argv);

        System.out.println("BENCHMARK VStreams");
        System.out.println("=====================");
	System.out.println("");
	new CreationTest().runTests(b);
	System.exit(0);
    }
}

