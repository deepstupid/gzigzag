/*   
CharScroll.java
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
 * Written by Tuomas Lukka
 */
package org.gzigzag;
import java.util.*;
import java.io.*;

/** DEPRECATED FOR STRINGSCROLL
 * A scroll of chars. May later have holes etc. from expunging,
 * but basically the point is that the address to content mapping
 * is stable and will not change whatever is done.
 * XXX copied from ByteScroll - sad.
 */
public class CharScroll extends Scroll{
public static final String rcsid = "$Id: CharScroll.java,v 1.5 2000/09/19 10:32:00 ajk Exp $";
	// Cheat: it's just a file
	private RandomAccessFile f;
    // public static final boolean dbg = true;
    // static final void p(String s) { if(dbg) System.out.println(s); }


	private boolean readonly;

	public CharScroll(String id, File fn, String mode, 
		boolean ro) {
	super(id);
	this.readonly = ro;
	try {
		f = new RandomAccessFile(fn, mode);
	} catch(Exception e) {
		ZZLogger.exc(e);
		throw new NullPointerException("Can't get");
	}
	}

	public synchronized char[] get(long start, int n) {
	try {
		f.seek(start*2);
		byte[] ret = new byte[n*2];
		int r = f.read(ret);
		if(r!=n*2) 
			throw new NullPointerException("NOT ENOUGH!");
		char[] c = new char[n];
		for(int i=0; i<n; i++)
		    c[i] = (char)((ret[i*2]<<8) | (ret[i*2+1]<<0));
		return c;
	} catch(Exception e) {
		ZZLogger.exc(e);
		throw new NullPointerException("Can't get");
	}
	}

	// XXX ENCODING
	public String getString(long start, int n) {
		char[] b = get(start,n);
		String s = new String(b);
		p("GETS: "+start+" "+n+" "+b+" '"+s+"'");
		return s;
	} 

	public long curEnd() { try { return f.length()/2; 
	} catch(Exception e) {
		ZZLogger.exc(e);
		throw new NullPointerException("Can't get");
	} 
	}

	public synchronized long append(String b) {
	    return append(b.toCharArray());
	}
	public synchronized long append(char[] bytes) {
	if(readonly) throw new ZZError("Can't append to readonly scroll");
	try {
		long offs = curEnd();
		f.seek(offs*2);
		// f.write(bytes);
		for(int i=0; i<bytes.length; i++) {
		    f.writeChar(bytes[i]);
		}
		p("WRITE: R "+offs);
		return offs;
	} catch(Exception e) {
		ZZLogger.exc(e);
		throw new NullPointerException("Can't get");
	}
	}
}



