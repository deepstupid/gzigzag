/*   
Mediaserver.java
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

package org.gzigzag.mediaserver;

import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import org.gzigzag.util.*;
import java.io.*;
import java.util.*;

/** Interface to a mediaserver capable of retrieving and adding data.
 */
public interface Mediaserver {

    class InvalidID extends IOException {
        public InvalidID(String s) { super(s); }
    }

    /** The ID handle class.
     * Currently, Ids have two main representations: as octet sequences
     * and as ASCII hex strings. This class is able to handle both.
     */
    class Id { 
	private byte[] bytes;

	public Id(byte[] bytes) {
	    this.bytes = (byte[])bytes.clone();
	}
	public Id(String str) {
	    this.bytes = HexUtil.hexToByteArr(str);
	}

	public String getString() { return HexUtil.byteArrToHex(bytes); }
	public byte[] getBytes() { return bytes; }

	public String toString() { return "[["+getString()+"]]"; }

	public boolean equals(Object o) {
	    if(o==null) return false;
	    if(!(o instanceof Id)) return false;
	    Id oi = (Id)o;
	    if(bytes.length != oi.bytes.length) return false;
	    for(int i=bytes.length-1; i>=0; i--) 
		if(bytes[i] != oi.bytes[i]) return false;
	    return true;
	}

	public int hashCode() {
	    if(bytes.length < 4) return 42;
	    int l = bytes.length;
	    return  (bytes[l-4] << 24) +
		    (bytes[l-3] << 16) +
		    (bytes[l-2] << 8) +
		    (bytes[l-1] << 0) ;
	}

    }

    /** The class representing a mediaserver data block.
     * XXX Make this abstract, and give also an OutputStream version.
     * Make it lazier... Maybe use java.lang.ref weak references
     * for the content.
     */
    interface Block {
        class Trivial implements Block {
            public Trivial(Id id, String contentType, byte[] data, byte[] raw,
                         String header, String source) {
                this.id = id;
                this.contentType = contentType;
                this.data = data;
                this.raw = raw;
                this.header = header;
                this.source = source;
            }
            private Id id;
            private String contentType;
            private byte[] data;
            private byte[] raw;
            private String header;
	    private String poolName;
            private String source;

            public String getSource() { return source; }
            public Id getId() { return id; }
            public String getContentType() { return contentType; }
            public byte[] getBytes() { return data; }
            public byte[] getRaw() { return raw; }
	    public InputStream getInputStream() {
		return new ByteArrayInputStream(getBytes());
	    }
            public String getHeader() { return header; }
	    public String getPoolName() { return poolName; }
	    public String readNthLine(int n) throws IOException {
		throw new UnsupportedOperationException("not tested");
		/**
		// search data for first newline
		int i = -1;
		int start = -1, end = -1; // one past end
		for(i = 0; i<data.length; i++)
		    if(data[i] == 0x0A) {
			start = end + 1; end = i;
			n--;
			if(n < 0) break;
		    }
		if(start < 0)
		    throw new IOException("line not found");
		return new String(data, 0, i);
		**/
	    }
        }

        /** Get the ID of this block.
         */
	Id getId();
        /** Get the MIME Content-Type.
         */
	String getContentType() throws IOException;
        /** Get the decoded data.
         */
	byte[] getBytes() throws IOException;
        /** Get the raw data.
         */
	byte[] getRaw() throws IOException;
        /** Get the raw header, as a string.
         */
	String getHeader() throws IOException;
	/** Read the nth line (starting at 0) of the body.
	 *  Needed to build space histories efficiently...
	 *  XXX make cleaner
	 */
	String readNthLine(int n) throws IOException;
	/** Get an input stream for this block.
	 *  Currently assumes that you always read the whole block in
	 *  from the input stream.
	 */
	InputStream getInputStream() throws IOException;
        /** Get the name of the pool that this block came from. */
        String getSource();
    }

    /** Get a set of the IDs of all entries in the Mediaserver database. Used
     *  in synchronization and pointer tracking.
     */
    Set getIDs() throws IOException;

    /** Get the datum corresponding to the given id.
     */
    Block getDatum(Id id) throws IOException;

    /** Add a new datum, creating a new ID for it. The new datum is associated
     *  with an existing id. (For example, the existing Id might be the 
     *  previous version of the datum; the effect is that the new datum is
     *  stored in the same pool as <code>assocId<code>.)
     *  @return The key of the new datum.
     */
    Id addDatum(byte[] data, String contentType, Id assocId) 
	throws IOException;

    /** Add a new datum, creating a new ID for it.
     *  @return The key of the new datum.
     */
    Id addDatum(byte[] data, String contentType) throws IOException;

    /** Add a new datum, explicitly giving a list of header lines.
     *  @param assocId May be <code>null</code>.
     */
    Id addDatum(byte[] data, String[] header, Id assocId) throws IOException;

    /** Store an existing datum under a given ID.
     * @param data the pure data block obtained from another mediaserver,
     *		containing all header information etc.
     * @throws InvalidID if the ID is
     *           not valid for the given data.
     */
    void storeDatum(Id id, byte[] data) throws IOException;

    /** Cache a mediaserver block.
     *  The block must be from this mediaserver. This operation doesn't need
     *  to do anything, if this mediaserver does not support caching.
     */
    void cache(Block b);

    /** Uncache a mediaserver block.
     *  The block must be from this mediaserver. This operation doesn't need
     *  to do anything, if this mediaserver does not support caching. If the
     *  block wasn't in the cache, this should not issue an error message.
     */
    void uncache(Block b);

    /** (Kludge) Set a String-identified pointer local to this mediaserver.
     *  A pointer points to an ID. That way, the mediaserver can be used
     *  somewhat like a file system, in that there are references that can
     *  be updated. The system isn't very powerful, though, and needs to be
     *  rethought sometime soon.
     *  <p>
     *  XXX THIS EXPLANATION IS OUTDATED! Explain the new pointer system!
     */
    void setPointer(String s, Id id) throws IOException;

    /** (Kludge) Get a String-identified pointer local to this mediaserver.
     *  <code>null</code> if the pointer is not yet set.
     *  @see setPointer
     */
    Id getPointer(String s) throws IOException;

    /** (Kludge) Get the names of all String-identified pointers local to
     *  this mediaserver.
     *  Needed for synching.
     *  @see setPointer, org.gzigzag.impl.Synch
     */
    Set getPointers() throws IOException;

    /** Get the name of the pool this mediaserver stores. */
    String getPoolName() throws IOException;

    /** Set the name of the pool this mediaserver stores. */
    void setPoolName(String s) throws IOException;

    PointerSet getPointerSet(String s) throws IOException;
    void setPointer(String s, Id id, Id obsolete) throws IOException;

}
