/*   
IDSpace.java
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
 * Written by Benja Fallenstein, Tuomas Lukka and Antti-Juhani Kaijanaho
 */

package org.gzigzag.mediaserver.ids;
import org.gzigzag.mediaserver.*;

import java.security.*;
import java.io.*;

/** A space of globally unique IDs.
 *  Now with support for two ID schemes: '00' and '01'. Only '01' is
 *  generated; '00' can be verified, but is never generated.
 *  <p>
 *  '00' IDs contain some additional information, '01' IDs contain
 *  only an SHA-1 hash. Note that the data that produces the hash
 *  in the '00' IDs contains the part of the ID that is not part of
 *  the hash-- i.e., part of the ID is prepended to the actual data
 *  before hashing. '01' hashes do not do this. At first glance, this
 *  seems to be consistent, since there does not seem to be anything
 *  else in the '01' IDs except the hash-- but there is something else:
 *  the '01' ID format byte.
 *  <p>
 *  Yet, hashing that byte too would make the model harder to understand,
 *  and it's not like there would be much point in it. So there's a minor
 *  inconsistency between '00' and '01' IDs which you sometimes may have
 *  to worry about.
 */
public class IDSpace implements Serializable {

    transient private MessageDigest d;
    // transient private SecureRandom r = new SecureRandom();
    transient private java.util.Random r;

    public IDSpace() {
	try {
	    d = MessageDigest.getInstance("SHA");
	} catch(Exception e) {
	    e.printStackTrace();
	    System.out.println(e);
	    throw new Error("Can't create IDSpace: "+e);
	}
    }

    /** Put the data in an old-style ('00') id and data arrays into the 
     * signature object,
     * preparing to sign the combination.
     * <p>
     * The old format is: 4 bytes = integer length of the id,
     * the id and the data.
     */
    public void put00Data(MessageDigest d, byte[] id, byte[] data) {
	int l = id.length;
	d.update((byte)((l >>> 24) & 0xff));
	d.update((byte)((l >>> 16) & 0xff));
	d.update((byte)((l >>> 8) & 0xff));
	d.update((byte)(l & 0xff));
	d.update(id);
	d.update(data);
    }

    /** Sign the specified id and data, using the private key of this
     * idspace.
     * @param id For old-style ('00') IDs, the initial part of the ID, 
     *           <b>without</b> the hash. For new-style IDs, 'null'.
     * @param data The data part of the id.
     */
    synchronized private byte[] getHash(byte[] id, byte[] data) {
	d.reset();
	if(id != null)
	    put00Data(d, id, data);
	else
	    d.update(data);
	return d.digest();
    }

    /** Create a fresh ID for the given block of data.
     * The ID contains a cryptographic hash of the data to avoid
     * spoofing. (Actually, the current ID format is just an SHA-1
     * hash with a '01' byte prepended.)
     * @param data The block of data that the new ID will refer to
     */
    public Mediaserver.Id createID(byte[] data) {
	try {
	    ByteArrayOutputStream os = new ByteArrayOutputStream();
	    //DataOutputStream dos = new DataOutputStream(os);
	    os.write(0x01); // id format version

	    byte[] hash = getHash(null, data);

	    os.write(hash);

	    return new Mediaserver.Id(os.toByteArray());
	} catch(IOException e) {
	    throw new Error("Really unexpected");
	}

    }

    /** Check that the hash code in the id is correct.
     * Note that this function does <b>not</b> detect
     * spoofing by new ids and data blocks, but only
     * the changing of the data block behind a known
     * id.
     */
    public boolean checkData(Mediaserver.Id idid, byte[] data) {
	byte[] id = idid.getBytes();
	if(id[0] != 0x00 && id[0] != 0x01)
	    throw new Error("ID format version not supported: "+id[0]);

	byte[] idstart = null;
	if(id[0] == 0x00) {
	    idstart = new byte[id.length-20];
	    System.arraycopy(id, 0, idstart, 0, idstart.length);
	}

	byte[] hash = getHash(idstart, data);
	for(int i=0; i<20; i++) {
	    if(hash[i] != id [ id.length - 20 + i ] )
		return false;
	}
	return true;
    }

    /** Return an ID-checking InputStream.
     *  When <em>closed</em>, the returned InputStream must throw an exception
     *  if the ID is not correct.
     */
    public InputStream checkInputStream(Mediaserver.Id idid, InputStream is) {
	byte[] id = idid.getBytes();
        if(id[0] != 0x00 && id[0] != 0x01)
            throw new Error("ID format version not supported");
	
	byte[] idstart = null;
	if(id[0] == 0x00) {
	    idstart = new byte[id.length-20];
	    System.arraycopy(id, 0, idstart, 0, idstart.length);
	}
	
	byte[] hash = new byte[20];
	System.arraycopy(id, id.length-20, hash, 0, 20);
	
	return new IDCheckInputStream(is, idstart, hash);
    }

}
