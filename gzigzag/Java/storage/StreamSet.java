/*   
StreamSet.java
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

/** A multi-file or multi-stream.
 * Used for reading and writing multiple, named streams.
 * They may be stored as a directory or as a single file, interleaved.
 * Make sure you call close() after done with a stream.
 */

public abstract class StreamSet {
String rcsid = "$Id: StreamSet.java,v 1.5 2000/09/18 14:28:22 tjl Exp $";

    /** Obtain the inputstream associated with the given id.
     * @return The input stream, or null if it doesn't exist.
     */
    public abstract InputStream getInputStream(String id);

    /** Obtain an OutputStream for appending to the given id's stream.
     */
    public abstract OutputStream getAppendStream(String id);

    /** Whether a stream exists.
     */
    public abstract boolean exists(String id);

    /** Obtain a writable for a given id.
     * This is so that string scrolls can work for now - they should
     * be changed to use the usual routines.
     */
    public abstract Writable getWritable(String id);

    /** Obtain a streamset for the given file.
     * NOT IMPLEMENTED. Should return either directory or single-file
     * stream set.
     */
    static public StreamSet get(File f) {
	return null;
    }
}


