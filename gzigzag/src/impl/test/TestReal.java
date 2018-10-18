/*   
TestReal.java
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
import org.gzigzag.vob.*;
import org.gzigzag.util.*;
import org.gzigzag.mediaserver.*;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import java.io.*;
import junit.framework.*;
import java.awt.*;
import java.awt.image.*;

/** Test loading real spaces and doing real things with them.
 */

public class TestReal extends TestCase {
    public static boolean dbg = false;
    final static void p(String s) { if(dbg) System.out.println(s); }

    public TestReal(String name) { super(name);} 

    Mediaserver ms = TestImpl.zms;

    /** Taken from BenchViews.
     */
    public void testLoadGenView() throws Exception {

	PermanentSpace space;
	Mediaserver.Id id;

	View view = new VobVanishingClient();
	Cell wc;
	Cell wc2;

	Mediaserver.Id spaceId = new Mediaserver.Id(
// "0000000008000000E83339CD9500048995FA3C08360525CF735910B9EEFE3CA111CB493009AB0F"
"0000000008000000E8E5FBCABD0004978303974EF3A274467960EEA12D94F05B7BA4F8FEC1AE2B"
	    );
	space = new PermanentSpace(ms, spaceId);

	Cell clientCell = space.getHomeCell().s(Client.d1);

	Cell scr = Params.getParam(clientCell, Client.c_screen);

	wc = Params.getParam(scr, Client.c_window);


	VobScene sc = new TrivialVobScene(new 
				Dimension(640,480));
	view.render(sc, wc);
    }

    public void testLoadSpace() throws Exception {
	Mediaserver.Id spaceId = new Mediaserver.Id("0000000008000000EC965F28E3000493972821DE1F871F2F993510E17BC7F7556C87EC0D753BFA");
	Loader.load(ms, spaceId);
    }
}
