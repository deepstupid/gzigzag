/*   
CountingReader.java
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
 * Written by Benja Fallenstein
 */
package org.gzigzag.util;
import java.io.*;

/** A <code>FilterReader</code> that keeps track of where it is.
 *  This can be asked how many characters it has already read.
 */

public final class CountingReader extends FilterReader {
public static final String rcsid = "$Id: CountingReader.java,v 1.1 2001/08/09 23:46:05 bfallenstein Exp $";

    int position = 0;

    /** Create a new <code>CountingReader</code> reading from <code>in</code>.
     */
    public CountingReader(Reader in) {
	super(in);
    }

    /** Return the number of characters already read.
     *  XXX returns a wrong value if more than <code>Integer.MAX_INTEGER</code>
     *      characters have been read.
     */
    public int getPosition() {
	return position;
    }

    public int read() throws IOException {
	position++;
	return super.read();
    }

    public int read(char cbuf[], int off, int len) throws IOException {
	position += len;
	return super.read(cbuf, off, len);
    }

    public long skip(long n) throws IOException {
	position += n;
	return super.skip(n);
    }

    public boolean markSupported() {
	return false;
    }

    public void mark(int readAheadLimit) throws IOException {
	throw new UnsupportedOperationException("mark");
    }

    public void reset() throws IOException {
	throw new UnsupportedOperationException("reset");
    }
}
