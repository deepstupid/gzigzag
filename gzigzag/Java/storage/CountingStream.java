/*   
CountingStream.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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
import java.awt.*;
import java.util.*;
import java.io.*;

/** Something Java should have: we conut our position in the stream.
 */

public class CountingStream extends FilterInputStream {

    int curPos = 0;

    public CountingStream(InputStream is) {
	    super(is);
    }

    public int read() throws IOException {
	int c = super.read();
	curPos++;
	return c;
    }

    public int read(byte b[]) throws IOException {
	int n = super.read(b);
	if(n < 0) return n;
	curPos += n;
	return n;
    }

    public int read(byte b[],
		     int off,
		     int len) throws IOException {
	int n = super.read(b, off, len);
	if(n < 0) return n;
	curPos += n;
	return n;
    }
    
    public long skip(long n) throws IOException {
	long ns = super.skip(n);
	curPos += ns;
	return ns;
    }

    public int getCount() {
	return curPos;
    }

}
