/*   
MemWritable.java
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
 * Written by Benjamin Fallenstein
 */
package org.gzigzag;
import java.util.*;

/** A writable that stores its contents in memory.
 * Used to make scrolls work in applets. Not necessarily very efficient;
 * however, I don't think that's so much of an issue in applets, where we
 * won't save a lot of data anyway.
 */

public class MemWritable implements Writable {
String rcsid = "$Id: MemWritable.java,v 1.1 2000/12/26 22:48:37 bfallenstein Exp $";

    /** The size of the chunks in which memory is allocated. */
    final int CHUNK = 500;

    /** The data. */
    byte[] d = new byte[CHUNK];

    /** The length of the data actually used.
     * len-1 is the highest index of any data ever written.
     */
    int len = 0;

    public void extend() {
	byte[] nu = new byte[d.length + CHUNK];
	System.arraycopy(d, 0, nu, 0, len);
	d = nu;
    }

    public void write(long offset0, byte[] data) {
	int offset = (int)offset0;
	int n = data.length;
	while(offset + n >= d.length) extend();
	System.arraycopy(data, 0, d, offset, n);
	if(offset + n > len) len = offset + n;
    }
    public byte[] read(long offset0, int n) {
	byte[] res = new byte[n];
	read(offset0, res, 0, n);
	return res;
    }
    public void read(long offset0, byte b[], int off, int n){
	System.out.println("MemWritable.read "+d.length+" "+len+" params "+
			   offset0+" "+b.length+" "+off+" "+n);
	int offset = (int)offset0;
	while(offset + n >= d.length) extend();
	System.arraycopy(d, offset, b, off, n);
    }
    public long length() {
	return len;
    }
    public void flush() {}
}

