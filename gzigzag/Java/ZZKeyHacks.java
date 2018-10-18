/*   
ZZKeyHacks.java
 *    
 *    Copyright (c) 1999, Ted Nelson and Tuomas Lukka
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
package org.gzigzag;
import java.awt.*;
import java.awt.event.*;

/** Hacks to work around Java's inconsistent, ideosyncratic key events.
 *  Everything in this class is static, but there are (static) variables
 *  for the "detected state of the system": e.g., how this Java VM handles
 *  umlauts.
 *  <p>
 *  Currently, detects whether we get umlauts through KEY_PRESSED; if not,
 *  invokes them at KEY_TYPED.
 */

public class ZZKeyHacks {
public static final String rcsid = "$Id: ZZKeyHacks.java,v 1.2 2001/04/13 22:40:14 bfallenstein Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) ZZLogger.log(s); }
    static final void pa(String s) { ZZLogger.log(s); }

	// Java is really screwy about what keyevents get
	// pressed and what are typed. We want to catch both.

	// It seems that at least on my platform and keybindings,
	// this is the totally unreasonable but working way to get
	// them. 

	// Ouch.

        // FIXME: This is a REALLY REALLY BAD QUICK HACK to make the
        // Finnish special letters work.  The problem?  We don't get a
        // KEY_PRESSED for them.  Damn Java!

    /** Whether we ever got an umlaut from a KEY_PRESSED event.
     *  If this is set to true, we ignore KEY_TYPED events. If it is set to
     *  false, KEY_TYPED events with umlauts in them get processed like
     *  KEY_PRESSED events. As KEY_PRESSED events occur before KEY_TYPED 
     *  events, this is a gain.
     */
    static boolean gotPressedUmlaut;

    /** The start of the Unicode character range we treat as umlauts. */
    static final int firstumlaut = 0x00C0;

    /** The end of the Unicode character range we treat as umlauts. */
    static final int lastumlaut = 0x00FD;

    /** Do the hacks on this key event.
     *  Currently, does the umlaut trick, as well as the testing whether we
     *  need it.
     */
    static public final KeyEvent keyEventHack(KeyEvent e) {
	if(gotPressedUmlaut) return e;
		
        int id = e.getID(), c = e.getKeyChar();
	boolean umlaut = (c >= firstumlaut) && (c <= lastumlaut);
	
	if(!umlaut) return e;

	if(id == e.KEY_PRESSED) {
	    gotPressedUmlaut = true;
        } else if(id == e.KEY_TYPED) {
	    return new KeyEvent(e.getComponent(), e.KEY_PRESSED, e.getWhen(), 
				e.getModifiers(), e.getKeyCode(),
				e.getKeyChar());
        }

	return e;
    }

    /** Do the hacks on this mouse event.
     *  Currently, sets button 3 if Meta is pressed, and sets button 1 if
     *  no button is pressed. (These are important for Macintosh.) No
     *  environment detection of any sort is done.
     */
    static public final MouseEvent mouseEventHack(MouseEvent e) {
	int mods = e.getModifiers();
	if((mods & e.META_MASK) != 0) {
	    if((mods & e.BUTTON1_MASK) != 0) mods -= e.BUTTON1_MASK;
	    if((mods & e.BUTTON2_MASK) != 0) mods -= e.BUTTON2_MASK;
	    mods |= e.BUTTON3_MASK;
	    return new MouseEvent(e.getComponent(), e.getID(), e.getWhen(),
				  mods, e.getX(), e.getY(), e.getClickCount(),
				  e.isPopupTrigger());
	}
	if((mods & e.BUTTON1_MASK) == 0 &&
	   (mods & e.BUTTON2_MASK) == 0 &&
	   (mods & e.BUTTON3_MASK) == 0) {
	    mods |= e.BUTTON1_MASK;
	    return new MouseEvent(e.getComponent(), e.getID(), e.getWhen(),
				  mods, e.getX(), e.getY(), e.getClickCount(),
				  e.isPopupTrigger());
	}
	return e;
    }

    /** Reset the state, so as if the program was fired up anew.
     *  This sets back all information gathered about the Java VM so far
     *  and re-initializes the class. Currently this is used for testing
     *  purposes only: the TestUmlauts JUnit test suite needs to be able to
     *  run different tests emulating different kinds of VMs.
     */
    static public void reset() {
	gotPressedUmlaut = false;
    }

    // Initialize the class by calling reset() a first time.
    { reset(); }
}

