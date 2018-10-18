/*   
P2PStorageCache.java
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

package org.gzigzag.p2p;

/** An implementation of a simple persistent file to unique id mapper.
 * This implementation is a bit strange (on purpose) to prevent synchronization
 * etc. problems: each block is stored in a unique file, and the beginning
 * of the file is a header which describes the file from the storagecache
 * perspective (unique id etc) after which the content follows as a single 
 * block. Because of this, several processes can access the same directory
 * without conflicts: all simply read the directory of files first and never
 * read files and write files by first writing into temporary files and then
 * renaming them.
 * <p>
 * Thus, there is no central directory of any kind.
 * Also, when moving files between mediaservers, the whole file can be
 * copied.
 * Of course, starting up can be slow as the beginning of every file
 * has to be read.
 */

public class P2PStorageCache {
public static final String rcsid = "$Id: P2PStorageCache.java,v 1.3 2001/04/21 01:11:35 bfallenstein Exp $";

    int MAGIC1 = 0x425673af;

    class Header1 implements java.io.Serializable {
	int magic;
	String id;
	String fromServer;
	String originallyFrom;
    }

    

    /** Get the length of the bits pointed to by the given id.
     */
    long length(UniqueID id) { return 0; }

    /** Get the bytes from the given id.
     */
    byte[] get(UniqueID id) { return null; }

    /** Get an input stream from the given id.
     */
    java.io.InputStream getInputStream(UniqueID id) { return null; }

    /** Store the given block in the cache.
     */
    void cache(UniqueID id) {}

    /** Remove the given block from the cache.
     */
    void uncache(UniqueID id) {}

    /** A structure showing a single cached entry.
     */
    class InCache {
        UniqueID id;
        /** For future extension.
         */
        long offs, len;
    }
    /** Get a listing of the local cache/storage.
     */
    InCache[] getDirectory() { return null; }

    /** Create a new globally unique id that can then be written to.
     */
    UniqueID.Writable newUniqueID() { return null; }
}

