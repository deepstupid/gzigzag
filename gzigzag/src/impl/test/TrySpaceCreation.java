/*   
BenchSpaceCreation.java
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

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.mediaserver.*;
import java.util.*;
import java.io.*;

/** Try creating a space. */

public class TrySpaceCreation {
public static final String rcsid = "$Id: TrySpaceCreation.java,v 1.2 2002/03/10 01:16:23 bfallenstein Exp $";
    static private void p(String s) { System.out.println(s); }

	Mediaserver ms;

	TrySpaceCreation() throws IOException {
	    ms = TestImpl.zms;
	}

	Mediaserver.Id id2 = new Mediaserver.Id("0000000008000000E83CA694D500045EA1CB3B2366CAE2E3F75C2426035C373D362CD429D93062");
	

    public static void main(String[] argv) throws IOException {
	TrySpaceCreation t = new TrySpaceCreation();

	try {
	    System.out.println("START");
	    new PermanentSpace(t.ms, t.id2);
	    System.out.println("SECOND");
            new PermanentSpace(t.ms, t.id2);
	    System.out.println("END");
	} catch(IOException e) {
	    throw new ZZError(e+": "+e.getMessage());
	}

    }
}
