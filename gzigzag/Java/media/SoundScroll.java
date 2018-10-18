/*   
SoundScroll.java
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
package org.gzigzag;
import java.io.*;
import java.util.*;

/** A scroll of sound. 
 * The fact that you need to specify the duraction when creating is not good but
 * we can fix that later somehow.
 */
public class SoundScroll extends Scroll {
public static final String rcsid = "$Id: SoundScroll.java,v 1.3 2000/09/19 10:32:00 ajk Exp $";

    // public abstract void preparePlayer();
    /** XXX change to play(Span) and get the next at runtime?
     */
    protected SoundScroll(String i) { super(i); }

    Vector obses = new Vector();
    public void addObs(SoundObs o) {
	    obses.addElement(o);
    }
    protected void inform(int event, long l) {
	    for(int i = 0; i<obses.size(); i++)
		    ((SoundObs)obses.elementAt(i)).playingAt(event,l);
    }

    // Cheat: it's currently just a file.
    File f;
    public File getFile() { return f; }
    long d;
    public long getDurationNanoseconds() { return d; }
    public SoundScroll(String id, File fn, long dur) {
	super(id);
	f = fn;
	d = dur;
    }
    public SoundScroll(String id, String fn, long dur) {
	super(id);
	f = new File(fn);
	d = dur;
    }
}
