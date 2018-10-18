/*   
TestSynch.java
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
 * Written by Antti-Juhani Kaijanaho
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.mediaserver.*;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import junit.framework.*;
import java.io.*;
import java.util.*;

/** Test synch
 */

public class TestSynch extends TestCase {
public static final String rcsid = "$Id: TestSynch.java,v 1.3 2002/03/10 11:46:02 bfallenstein Exp $";
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) pa(s); }
    protected static void pa(String s) { System.err.println(s); }
    protected static void out(String s) { System.out.println(s); }

    public TestSynch(String s) { super(s); }

    public void testSynchDifferentPools() throws IOException {
        Mediaserver m1 = new SimpleMediaserver(new TransientStorer(), new IDSpace(), 0);
        Mediaserver m2 = new SimpleMediaserver(new TransientStorer(), new IDSpace(), 0);
        m1.setPoolName("a");
        m2.setPoolName("b");
        try {
            Synch.sync(m1, m2, false);
            fail("Synch did not notice that the mediaservers are in different pools");
        } catch (IOException _) {
            // nothing
        }
    }

}

