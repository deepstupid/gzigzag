/*   
ComputeID.java
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
 * Written by Rauli Ruohonen
 */

package org.gzigzag.mediaserver;
import java.security.*;
import java.io.*;
import java.util.*;
import org.gzigzag.util.*;

public class ComputeID {
    public static void main(String argv[]) throws Exception {
	MessageDigest d = MessageDigest.getInstance("SHA");
	if (argv.length != 1) {
	    System.err.println("Usage: ComputeID <hexl of non-SHA part of id>");
	    System.err.println("Block data is read from stdin.");
	    return;
	}
	byte[] id = HexUtil.hexToByteArr(argv[0]);
	if(id[0] != 0x00)
	    throw new Error("ID format version not supported");
	ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        while(true) {
            int r = System.in.read(buf);
            if(r == -1) break;
            os.write(buf, 0, r);
        }
	byte[] data = os.toByteArray();
	d.reset();
	int l = id.length;
	d.update((byte)((l >>> 24) & 0xff));
	d.update((byte)((l >>> 16) & 0xff));
	d.update((byte)((l >>> 8) & 0xff));
	d.update((byte)(l & 0xff));
	d.update(id);
	d.update(data);
	byte[] hash = d.digest();
	String s = HexUtil.byteArrToHex(id)+HexUtil.byteArrToHex(hash);
	System.out.println(s);
	return;
    }
}
