/*   
AddToMediaserver.java
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
 * Written by Benja Fallenstein and Antti-Juhani Kaijanaho
 */
package org.gzigzag.mediaserver;
import org.gzigzag.*;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import org.gzigzag.util.*;
import java.util.*;
import java.io.*;

/** A stand-alone application to add a file to the mediaserver.
 */

public class AddToMediaserver {
public static final String rcsid = "$Id: AddToMediaserver.java,v 1.4 2001/08/07 19:24:46 tjl Exp $";
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) pa(s); }
    protected static void pa(String s) { System.err.println(s); }
    protected static void out(String s) { System.out.println(s); }

    private static  String guessContentType(File file) throws IOException {
        String name = file.getName();
        int inx = name.lastIndexOf(".");
        if (inx == -1 || inx + 1 == name.length())
            throw new IOException("content type not known");
        return MediaTypes.getType(name.substring(inx+1));
    }

    public static void main(String argv[])  {
	try {
	Storer stor;
	if (argv.length < 3) {
	    pa("AddToMediaserver takes three arguments:");
	    pa("The mediaserver directory, the content type of the ");
	    pa("file to add (or \"guess\"), and the filename of the file to add.");
	    return;
	}
        System.out.println(argv[0]);
	stor = new DirStorer(new File(argv[0]));
	String content_type = argv[1];
	File file = new File(argv[2]);
        if (content_type.equals("guess")) content_type = guessContentType(file);
	if(!file.exists()) {
	    pa("File "+file+" doesn't exist");
	    return;
	}
	Mediaserver ms = new SimpleMediaserver(stor,new IDSpace(), 0);
	
	FileInputStream fis = new FileInputStream(file);
	int len = fis.available();
	byte[] bytes = new byte[len];
	fis.read(bytes, 0, len);
	fis.close();

	String id = ms.addDatum(bytes, content_type).getString();
	
	out("The mediaserver block was created successfully.  "+
	    "Its mediaserver ID is: ");
	out(id);

	out("You can view the MS block now by reading it from the ");
	out("mediaserver directory you specified, "+argv[0]+".");

	}catch(IOException e) {
	    e.printStackTrace();
	}
    }
}
