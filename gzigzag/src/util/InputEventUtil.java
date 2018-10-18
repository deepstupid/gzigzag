/*   
InputEventUtil.java
*    
*    Copyright (c) 1999-2001, Ted Nelson and Tuomas Lukka
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
 * Written by Tuomas Lukka and Benja Fallenstein
 */
/*
 *   `Did you remember to leave the keys?'
 *          - Sign on the door of a Jyu guest appartment
 */
package org.gzigzag.util;
import java.awt.*;
import java.awt.event.*;

/** Hacks to work around Java's inconsistent, ideosyncratic key events.
 *  Also generates meaningful names for key events.
 *  <p>
 *  Everything in this class is static, but there are (static) variables
 *  for the "detected state of the system": e.g., how this Java VM handles
 *  umlauts.
 *  <p>
 *  Currently, detects whether we get umlauts through KEY_PRESSED; if not,
 *  invokes them at KEY_TYPED.
 */

public class InputEventUtil {
public static final String rcsid = "$Id: InputEventUtil.java,v 1.19 2002/03/30 07:57:05 bfallenstein Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    static public String getKeyEventName(KeyEvent k) {
	/*
	 * This is quite complicated - more complicated than it
	 * should be but Java seems to screw up key events quite
	 * nicely for us...
	 * We want to have Shift-Alt-X, for example, which on some
	 * Javas needs quite a bit of attention.
	 */


	/* A try on doing it the way they'd want us to do it */
	if("typed".equals(System.getProperty("gzigzag.keyevents")))
	   return getKeyEventName2(k);

	// Unscrew umlauts first.
	k = keyEventHack(k);

	if(k.getID() != k.KEY_PRESSED) return null;

	// What we'll return.
	String t;

	char c= k.getKeyChar();
	int kc = k.getKeyCode();
	String kt = KeyEvent.getKeyText(kc);
	String kbychar = new String(new char[] {c});
	p("Have: "+c+" "+kc+" '"+kt+"' '"+kbychar+"'");
	/** Map key codes to key names.
	 *  We need this because KeyEvent.getKeyText() is (urks) localized. That's
	 *  all very nice, but unusable for our purposes: on a German system, Java
	 *  generates key names like "Unten" for the "Down" cursor key
	 *  (hey, nobody said it'd be localized <em>well</em> ;) ), i.e. events
	 *  inconsistent with other localizations.
	 *  XXX move to own function, NEVER use getKeyText!
	 */
	if(kc == k.VK_DELETE)
	    t = "Delete";
	else if(kc == k.VK_BACK_SPACE)
	    t = "Backspace";
	else if(kc == k.VK_ESCAPE)
	    t = "Escape";
	else if(kc == k.VK_LEFT)
	    t = "Left";
	else if(kc == k.VK_RIGHT)
	    t = "Right";
	else if(kc == k.VK_UP)
	    t = "Up";
	else if(kc == k.VK_DOWN)
	    t = "Down";
	else if(kc == k.VK_PAGE_UP)
	    t = "PgUp";
	else if(kc == k.VK_PAGE_DOWN)
	    t = "PgDown";
	else if(kc == k.VK_ENTER)
	    t = "Enter";
	else if(kc == k.VK_NUMPAD4)
	    t = "Numpad-Left";
	else if(kc == k.VK_NUMPAD6)
	    t = "Numpad-Right";
	// else if(Character.isLetterOrDigit(c) || Character.isSpaceChar(c))
	else if (Character.getType(c)!=Character.CONTROL) // XXX?
	    t = kbychar;
        else if((c == k.CHAR_UNDEFINED
		 // k.CHAR_UNDEFINED changed between JDK1.1 and 1.2
		 || c == 0x0 || c == 0xFFFF) // needed for cross-compiling
		&& kc != 0)
            t = kt;
	else
	    t = kt; // bychar; // XXX
	p("Chose: "+t);
	if(t.equals("\n")) t = "Enter";
	if(t.equals("\t")) t = "Tab";
	if(t.equals("\033")) t = "Esc";
        if(           t.equals("Left") || // These need shift.
                      t.equals("Right") ||
                      t.equals("Up") ||
                      t.equals("Down") ||
		      t.equals("PgUp") ||
		      t.equals("PgDown") ||
		      t.equals("Home") ||
		      t.equals("End")) {
            if(k.isShiftDown()) t = "Shift-"+t;
	} else if(t.length() == 1) {
	    if(!k.isShiftDown() && !k.isAltDown() &&
	       !k.isControlDown()) t = t.toLowerCase();
	} else if(t.equals("") ||
	   Character.isISOControl(t.charAt(0)) ||
	   k.isAltDown() || k.isControlDown()
	   ) {
	    p("Maybe rechoosing");
	    if(kc != 0)
		t = kt;
	    if(k.isShiftDown()) t = "Shift-"+t;
	}
	if(k.isAltDown()) t = "Alt-"+t;
	if(k.isControlDown()) t = "Ctrl-"+t;

	p("KEYTEXT: '" + t + "', was by kt: '"+kt+"' and by char: '"
	  +kbychar+"'");

	return t;
    }

    public static String getKeyEventName2(KeyEvent k) {
	    p("getKeyEventName2() active");
	/* First we'll check for normal typing as this is the way
	   They say everyone should do it */
	if(k.getID() == k.KEY_TYPED) {
	    char c = k.getKeyChar();
	    p("KEY_TYPED event: '"+c+"'");
	    if(c == k.CHAR_UNDEFINED
		 // k.CHAR_UNDEFINED changed between JDK1.1 and 1.2
		 || c == 0x0 || c == 0xFFFF) // needed for cross-compiling
		return null;
	    k.consume();
	    return new String(new char[] {c});
        }

	if(k.getID() != k.KEY_RELEASED)
	    return null;

	// We'll act on KEY_RELEASED as that's what kaffe sends us

	// What we'll return.
	String t=null, modifiers="";

	if(k.isShiftDown()) modifiers = "Shift-"+modifiers;
	if(k.isAltDown()) modifiers = "Alt-"+modifiers;
	if(k.isControlDown()) modifiers = "Ctrl-"+modifiers;
	//if(k.isMetaDown()) modifiers = "Meta-"+modifiers;


	char c= k.getKeyChar();
	int kc = k.getKeyCode();
	String kt = KeyEvent.getKeyText(kc);
	String kbychar = new String(new char[] {c});
	p("KEY_RELEASED event: "+c+" "+kc+" '"+kt+"' '"+kbychar+"'");
	/** Map key codes to key names.
	 *  We need this because KeyEvent.getKeyText() is (urks) localized. That's
	 *  all very nice, but unusable for our purposes: on a German system, Java
	 *  generates key names like "Unten" for the "Down" cursor key
	 *  (hey, nobody said it'd be localized <em>well</em> ;) ), i.e. events
	 *  inconsistent with other localizations.
	 *  XXX move to own function, NEVER use getKeyText!
	 */
	if(kc == k.VK_DELETE)
	    t = "Delete";
	else if(kc == k.VK_BACK_SPACE)
	    t = "Backspace";
	else if(kc == k.VK_ESCAPE)
	    t = "Escape";
	else if(kc == k.VK_LEFT)
	    t = "Left";
	else if(kc == k.VK_RIGHT)
	    t = "Right";
	else if(kc == k.VK_UP)
	    t = "Up";
	else if(kc == k.VK_DOWN)
	    t = "Down";
	else if(kc == k.VK_PAGE_UP)
	    t = "PgUp";
	else if(kc == k.VK_PAGE_DOWN)
	    t = "PgDown";
	else if(kc == k.VK_ENTER)
	    t = "Enter";
	else if(kc == k.VK_NUMPAD4)
	    t = "Numpad-Left";
	else if(kc == k.VK_NUMPAD6)
	    t = "Numpad-Right";

	if(t!=null) {
	    p("Returning \""+modifiers+t+"\"");
	    return modifiers+t;
	}
	p("Returning null");
	return null;
    }

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
