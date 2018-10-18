/*   
BenchViews.java
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
import org.gzigzag.client.Client;
import org.gzigzag.client.*;
import org.gzigzag.mediaserver.*;
import org.gzigzag.vob.*;
import java.util.*;
import java.io.*;
import java.awt.*;

/** Something is slow in views.
 */


public class BenchViews {
public static final String rcsid = "$Id: BenchViews.java,v 1.11 2002/03/18 08:34:07 tjl Exp $";
    static private void p(String s) { System.out.println(s); }

    static private class CreationTest {

	PermanentSpace space, space2;
	Mediaserver ms;
	Mediaserver.Id id;

	//	View view = new VanishingView();
	View plainview = new VobVanishingClient();
	Cell wc;
	Cell wc2;

	CreationTest() throws IOException {
	    ms = TestImpl.zms;
	    Mediaserver.Id spaceId = new Mediaserver.Id(
// "0000000008000000E83339CD9500048995FA3C08360525CF735910B9EEFE3CA111CB493009AB0F"
"0000000008000000E8E5FBCABD0004978303974EF3A274467960EEA12D94F05B7BA4F8FEC1AE2B"
		);
	    space = new PermanentSpace(ms, spaceId);
	    space2 = new PermanentSpace(ms, spaceId);

	    Cell clientCell = space.getHomeCell().s(Client.d1);

	    Cell scr = Params.getParam(clientCell, Client.c_screen);

	    Cell clientCell2 = space2.getHomeCell().s(Client.d1);
	    Cell scr2 = Params.getParam(clientCell2, Client.c_screen);

	    wc = Params.getParam(scr, Client.c_window);

	    wc2 = Params.getParam(scr2, Client.c_window);
	    Cursor.set(wc2, space2.getCell("0000000008000000E7C35AB9FC00043FA763303194CCFB9C66A32C53C148FA8F7733EE16CE7ADD-3"));
	}
	
	void runTests(org.gzigzag.benchmark.Bench b) {

	    b.run(new Object[] {
/*		"BUILDINITIALVIEW", "Build the initial view VOb scene",
		new Runnable() { public void run() {
		    VobScene sc = new TrivialVobScene(new 
					    Dimension(640,480));
		    view.render(sc, wc);
		}},

		"BUILDVIEWLARGER", "Build a larger VOB scene",
		new Runnable() { public void run() {
		    VobScene sc = new TrivialVobScene(new 
					    Dimension(640,480));
		    view.render(sc, wc2);
		}},*/
		"PLAINBUILDINITIALVIEW", "Build the initial view VOb scene",
		new Runnable() { public void run() {
		    VobScene sc = new TrivialVobScene(new 
					    Dimension(640,480));
		    plainview.render(sc, wc);
		}},

		"PLAINBUILDVIEWLARGER", "Build a larger VOB scene",
		new Runnable() { public void run() {
		    VobScene sc = new TrivialVobScene(new 
					    Dimension(640,480));
		    plainview.render(sc, wc2);
		}},



	    });
	}
    }

    public static void main(String[] argv) throws IOException {
	org.gzigzag.benchmark.Bench b = new org.gzigzag.benchmark.Bench(argv);

        System.out.println("BENCHMARK Views");
        System.out.println("=====================");
	System.out.println("");
	new CreationTest().runTests(b);
	System.exit(0);
    }
}
