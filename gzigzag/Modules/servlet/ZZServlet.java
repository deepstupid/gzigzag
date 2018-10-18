/*   
ZZServlet.java
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
package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;


/** A base class for servlets based on ZZ spaces.
 */

public class ZZServlet extends HttpServlet {
public static final String rcsid = "$Id: ZZServlet.java,v 1.1 2001/01/17 14:27:20 tjl Exp $";

    static Hashtable spaces = new Hashtable();
    static Object lock = new Object();

    static ZZSpace getSpace(String name) {
	synchronized(lock) {
	    ZZSpace s = (ZZSpace)spaces.get(name);
	    if(s == null) {
		File f = new File(name);
		s = new ZZCacheDimSpace(new DirStreamSet(f));
		spaces.put(name, s);
	    }
	    return s;
	}
    }


    ZZSpace space;

    public void init(ServletConfig config) throws ServletException {
	synchronized(lock) {
	    String s = config.getInitParameter("space");
	    if(s == null) {
		// throw new ServletException("No space specified!");
		s = "/tmp/testspace";
	    }
	    space = getSpace(s);
	}
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
	throws ServletException {
	try {
	synchronized(space) {
	    resp.getWriter().print("HELLO\n\n!!\n\n");
	}
	} catch(Exception e) {
	    throw new ServletException("doGet: ", e);
	}
    }

}

