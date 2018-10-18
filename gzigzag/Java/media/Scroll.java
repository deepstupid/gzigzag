/*   
Scroll.java
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
import java.util.*;
import java.io.*;

/** A pseudo-permanent stable media storage.
 * If you put data in a scroll, it is guaranteed (well, in the development
 * stage only theoretically) that the same address will be eternal for it.
 * If you look for that particular address, you will get the data you stored
 * at it or you will get an error (e.g. if you have expunged some region
 * of a scroll) from it not being accessible.
 */

public abstract class Scroll {
public static final String rcsid = "$Id: Scroll.java,v 1.5 2000/11/03 08:01:05 ajk Exp $";
    public static boolean dbg = true;
    static public final void p(String s) { if(dbg) System.out.println(s); }
    static public final void pa(String s) { if(dbg) System.out.println(s); }

    protected Scroll(String i) { id = i; }

    String id;
    public String getId() { return id; }


    static Hashtable scrolls = new Hashtable();


    public interface Creator {
	    Scroll create(String id, File f);
    }
    static Hashtable scrolltypes = new Hashtable(); // full of creators

    static public void register(String id, Creator c) {
	    scrolltypes.put(id, c);
    }

    static public void register(String id, Scroll s) {
	scrolls.put(id, s);
    }

    /** Get a scroll for the given identifier.
     * This method encapsulates the way the system finds 
     * scrolls and the implementation will change in the future.
     * Currently it looks in the given ZZSpace for a file name.
     */
    static public Scroll obtain(ZZSpace e, String id) {


	Scroll scr;
	if((scr=(Scroll)scrolls.get(id))!=null)
		return scr;

	ZZCell c = e.getHomeCell();
	c = ZZDefaultSpace.findScrollCell(c, id);
	if(c==null) return null;
	ZZCell cfn = c.s("d.2",1);
	File f = new File(cfn.getText());
	ZZCell typ = cfn.s("d.2",1);
	String t = typ.getText();
	if(t.equals("Byte")) {
		scr = new ByteScroll(id, f, "r", "ISO8859_1", true);
	} else if(t.equals("Sound")) {
		ZZCell dur = typ.s("d.2", 1);
		scr = new SoundScroll(id, f, Long.parseLong(dur.getText()));
	} else {
		Creator crea = (Creator)scrolltypes.get(t);
		if(crea==null)
			throw new ZZError("No such scroll type: "+t);
		scr = crea.create(id, f);
	}

	scrolls.put(id, scr);
	return scr;
    }
}
