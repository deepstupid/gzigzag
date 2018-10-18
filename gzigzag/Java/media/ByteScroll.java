/*   
ByteScroll.java
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

/** A scroll of bytes. May later have holes etc. from expunging,
 * but basically the point is that the address to content mapping
 * is stable and will not change whatever is done.
 */
public class ByteScroll extends Scroll{
public static final String rcsid = "$Id: ByteScroll.java,v 1.4 2000/09/19 10:32:00 ajk Exp $";
	// Cheat: it's just a file
	private RandomAccessFile f;

	private String encoding;

	private boolean readonly;

	public ByteScroll(String id, File fn, String mode, String encoding,
		boolean ro) {
	super(id);
	this.encoding = encoding;
	this.readonly = ro;
	try {
		f = new RandomAccessFile(fn, mode);
	} catch(Exception e) {
		ZZLogger.exc(e);
		throw new NullPointerException("Can't get");
	}
	}

	public synchronized byte[] get(long start, int n) {
	try {
		f.seek(start);
		byte[] ret = new byte[n];
		int r = f.read(ret);
		if(r!=n) 
			throw new NullPointerException("NOT ENOUGH!");
		return ret;
	} catch(Exception e) {
		ZZLogger.exc(e);
		throw new NullPointerException("Can't get");
	}
	}

	// XXX ENCODING
	public String getString(long start, int n) {
		byte[] b = get(start,n);
		try {
		return new String(b, "ISO8859_1");
		} catch(Exception e) {
		ZZLogger.exc(e);
		throw new NullPointerException("Can't get");
	} 
	}

	public long curEnd() { try { return f.length(); 
	} catch(Exception e) {
		ZZLogger.exc(e);
		throw new NullPointerException("Can't get");
	} 
	}

	public synchronized long append(byte[] bytes) {
	if(readonly) throw new ZZError("Can't append to readonly scroll");
	try {
		long offs = curEnd();
		f.seek(offs);
		f.write(bytes);
		return offs;
	} catch(Exception e) {
		ZZLogger.exc(e);
		throw new NullPointerException("Can't get");
	}
	}
}


