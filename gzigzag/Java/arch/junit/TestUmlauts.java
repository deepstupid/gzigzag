/*   
TestUmlauts.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
 *    Copyright (c) 2001, Benja Fallenstein
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
package org.gzigzag.test;
import org.gzigzag.*;
import junit.framework.*;
import java.awt.*;
import java.awt.event.*;

/** A JUnit test for the umlauts hack.
 *  Some Java VMs don't issue KEY_PRESSED events for umlauts; others do. The
 *  ZZKeyHacks class converts umlaut KEY_TYPED events to KEY_PRESSED events
 *  if no KEY_PRESSED event has ever been issued for an umlaut. This test case
 *  contains tests emulating both kinds of VMs.
 */

public class TestUmlauts extends TestCase {
public static final String rcsid = "$Id: TestUmlauts.java,v 1.3 2001/06/16 09:52:05 tjl Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) ZZLogger.log(s); }
    static final void pa(String s) { ZZLogger.log(s); }

    public TestUmlauts(String s) { super(s); }

    /** The unicode numbers of the umlauts we test.
     *  (Some compilers seemingly don't like umlaut characters in the input
     *  stream...)
     */
    static final char[] umlauts = {
	0xC4, 0xC5, 0xE4, 0xE5, 0xF6, 0xFC
    };

    /** The fake component we use to issue key events. */
    static final Component fake = new Button();

    /** A shorthand for the virtual keycode we use in our events, VK_UNDEFINED. */
    static final int nocode = KeyEvent.VK_UNDEFINED;

    public void testGoodJava() {
	/** IMPORTANT: Reset ZZKeyHacks VM detection first */
	ZZKeyHacks.reset();
	
	for(int i=0; i<umlauts.length; i++) {
	    char c = umlauts[i];
	    long time = System.currentTimeMillis();

	    KeyEvent e = new KeyEvent(fake, KeyEvent.KEY_PRESSED, time, 0, 
				      nocode, c);
	    KeyEvent f = ZZKeyHacks.keyEventHack(e);
	    assertTrue(f.getID() == f.KEY_PRESSED);
	    
	    e = new KeyEvent(fake, KeyEvent.KEY_TYPED, time+5, 0, nocode, c);
	    f = ZZKeyHacks.keyEventHack(e);
	    assertTrue(f.getID() != f.KEY_PRESSED);
	}
    }

    public void testBadJava() {
	/** IMPORTANT: Reset ZZKeyHacks VM detection first */
	ZZKeyHacks.reset();
	
	for(int i=0; i<umlauts.length; i++) {
	    char c = umlauts[i];
	    long time = System.currentTimeMillis();

	    KeyEvent e = new KeyEvent(fake, KeyEvent.KEY_TYPED, time, 0, 
				      nocode, c);
	    KeyEvent f = ZZKeyHacks.keyEventHack(e);
	    assertTrue(f.getID() == f.KEY_PRESSED);
	}
    }

}
