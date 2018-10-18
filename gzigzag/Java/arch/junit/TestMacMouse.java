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

/** A JUnit test for the Macintosh mouse hack.
 *  The Macintosh has only one mouse key, so we need to emulate at least the
 *  third (usually rightmost) mouse button; we can do that with using
 *  Meta (Apple) Key-Mouse event. Additionally, when dragging, Apple's/Sun's
 *  VM issues mouse events with no mouse button; this needs to be fixed, too.
 */

public class TestMacMouse extends TestCase {
public static final String rcsid = "$Id: TestMacMouse.java,v 1.2 2001/06/16 09:52:05 tjl Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) ZZLogger.log(s); }
    static final void pa(String s) { ZZLogger.log(s); }

    public TestMacMouse(String s) { super(s); }

    /** The fake component we use to issue key events. */
    static final Component fake = new Button();

    public void testMacMouse() {
	ZZKeyHacks.reset();
	
	long time = System.currentTimeMillis();

	MouseEvent e = new MouseEvent(fake, MouseEvent.MOUSE_DRAGGED, time,
				      InputEvent.META_MASK, 0, 0, 0, false);
	MouseEvent f = ZZKeyHacks.mouseEventHack(e);
	assertTrue((f.getModifiers() & f.BUTTON1_MASK) == 0);
	assertTrue((f.getModifiers() & f.BUTTON3_MASK) != 0);
	
	e = new MouseEvent(fake, MouseEvent.MOUSE_DRAGGED, time, 0, 0, 0, 0,
			   false);
	f = ZZKeyHacks.mouseEventHack(e);
	assertTrue((f.getModifiers() & f.BUTTON1_MASK) != 0);
	assertTrue((f.getModifiers() & f.BUTTON3_MASK) == 0);
	
	e = new MouseEvent(fake, MouseEvent.MOUSE_RELEASED, time,
			   InputEvent.META_MASK | InputEvent.BUTTON1_MASK,
			   0, 0, 0, false);
	f = ZZKeyHacks.mouseEventHack(e);
	assertTrue((f.getModifiers() & f.BUTTON1_MASK) == 0);
	assertTrue((f.getModifiers() & f.BUTTON3_MASK) != 0);
	
	e = new MouseEvent(fake, MouseEvent.MOUSE_PRESSED, time,
			   InputEvent.BUTTON1_MASK, 0, 0, 0, false);
	f = ZZKeyHacks.mouseEventHack(e);
	assertTrue((f.getModifiers() & f.BUTTON1_MASK) != 0);
	assertTrue((f.getModifiers() & f.BUTTON3_MASK) == 0);
    }

}
