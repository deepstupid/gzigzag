/*   
ZobDoc.java
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
import java.util.*;
import java.io.*;
import com.sun.javadoc.*;

/** A javadoc doclet for extracting ZOb documentation.
 * Note that compiling and running this file requires the <b>tools.jar</b> file
 * from JDK 1.3.
 */

public class ZobDoc extends Doclet {
public static final String rcsid = "$Id: ZobDoc.java,v 1.2 2000/11/05 18:22:54 tjl Exp $";

    static String[][] lists = new String[][] {
	{ "org.gzigzag.FlobRaster", "Views", "views.html" },
	{ "org.gzigzag.RasterFlobFactory", "CellViews", "cellviews.html" }
    };

    PrintWriter[] listpws = new PrintWriter[lists.length];

    String outdir = null;

    public static boolean start(RootDoc root) {
	ZobDoc zd = new ZobDoc();
	return zd.go(root);
    }

    public boolean go(RootDoc root) {
	try {
	    readOptions(root.options());

	    for(int i=0; i<lists.length; i++) {
		listpws[i] = new PrintWriter(new FileWriter(
		    new File(outdir, lists[i][2])));
	    }

	    ClassDoc[] classes = root.classes();
	    for(int i=0; i < classes.length; i++) {
		ClassDoc[] ifaces = classes[i].interfaces();
		boolean iszob = false;
		for(int j=0; j<ifaces.length; j++) {
		    if(ifaces[j].qualifiedName().equals("org.gzigzag.ZOb"))
			iszob = true;
		}
		if(!iszob) continue;
		String qname = classes[i].qualifiedName();
		String filename = qname+".html";
		// XXX Modules go wrong. Hmm.. should correct.
		String zobname = null;
		if(qname.indexOf("org.gzigzag.module.") == 0) 
		    zobname = "."+qname.substring(19);
		else if(qname.indexOf("org.gzigzag.") == 0)
		    zobname = qname.substring(12);
		else 
		    zobname = qname;

		for(int j=0; j<ifaces.length; j++) {
		    for(int l=0; l<lists.length; l++) {
			if(ifaces[j].qualifiedName().equals(lists[l][0])) {
			    listpws[l].println(filename);
			}
		    }
		}


		// Now we have the zob.
    //	    System.out.println(classes[i].qualifiedName());

		PrintWriter pr = new PrintWriter(
		    new FileWriter(new File(outdir, filename)));

		printDoc(zobname, pr, classes[i]);

		pr.close();
	    }

	    for(int i=0; i<lists.length; i++) {
		listpws[i].close();
	    }
	} catch(Exception e) {
	    ZZLogger.exc(e);
	    return false;
	}
	return true;
    }

    public static int optionLength(String opt) {
	if(opt.equals("-d"))
	    return 2;
	return 0;
    }

    public void readOptions(String[][] options) {
        for (int i = 0; i < options.length; i++) {
            String[] opt = options[i];
	    if (opt[0].equals("-d")) {
		outdir = opt[1];
	    }
        }
    }


    public void printDoc(String zobname, PrintWriter pw, ClassDoc cl) {
	pw.println("<H1>"+zobname+"</H1>");
	pw.println("<H2 id=\"descr\">Description</H2>");
	pw.println(cl.commentText());
//	System.out.println(cl.commentText());

	pw.println("<H2 id=\"params\">Structural parameters</H2>");
	FieldDoc[] fields = cl.fields();
	for(int i=0; i<fields.length; i++) 
		printField(pw, fields[i]);

	pw.flush();
    }

    public void printField(PrintWriter pw, FieldDoc f) {
	Tag[] tg = f.tags();
	boolean str = false;
	for(int i=0; i<tg.length; i++) {
	    if(tg[i].name().equals("@structparam")) str = true;
	    System.out.println("Tag: "+tg[i].name()+" "+tg[i].kind());
	}
	if(!str) {
	    System.out.println("Not structparam "+f.name());
	    return;
	}
	pw.println("<H3>"+f.name()+"</H3>");
	pw.println(f.commentText());
    }
}

