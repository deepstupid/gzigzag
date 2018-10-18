/*   
Run.java
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
package org.gzigzag.client;
import org.gzigzag.impl.*;
import java.io.*;

/** Run the client.
 *  This application syncs with the public repository and runs the GZZ client
 *  without needing to be given any commandline parameters.
 *  <p>
 *  Z directory used is './Z'.
 */

public class Run {
public static final String rcsid = "$Id: Run.java,v 1.1 2002/02/16 23:31:04 bfallenstein Exp $";
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) pa(s); }
    protected static void pa(String s) { System.err.println(s); }
    protected static void out(String s) { System.out.println(s); }

    static String[] sync_args = {"-ro", "-dir", "Z", "-url",
				 "http://himalia.it.jyu.fi/ms/gzz-base"};
    static String[] client_args = {"Z"};

    public static void main(String argv[]) throws IOException {
	File z = new File("Z");
	if(!z.exists())
	    z.mkdir();

	File prop = new File("Z/properties");
	if(!prop.exists()) {
	    Writer w = new FileWriter(prop);
	    w.write("simplemediaserver.poolname=gzz-base\n");
	    w.close();
	}

	Synch.main(sync_args);
	Client.main(client_args);
    }
}
