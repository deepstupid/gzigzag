/*   
VCServlet.java
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


/** A Virtual Community demo servlet.
 */

public class VCServlet extends ZZServlet {
public static final String rcsid = "$Id: VCServlet.java,v 1.2 2001/02/28 03:02:56 tjl Exp $";

    String getURL(ZZCell c) {
	    return "org.gzigzag.module.VCServlet?"+c.getID();
	}

    void printAuthor(ZZCell txt, PrintWriter wr) {
	ZZCell h = txt.h("d.author", true);
	if(h != null) {
	    wr.print("<a style=\"color: #FF0000;\" href=\""+getURL(h)
		+"\">"+h.getText()+"</a>");
	}
    }

    void printmsg(ZZCell h, PrintWriter wr) {
	printAuthor(h, wr);
	wr.print(": ");
	wr.print("<a href=\""+getURL(h) + "\">"+ h.getText() + "</a>");
    }

    void printRefs(ZZCell thing, PrintWriter wr) {
	wr.print("<p>Referenced in: <ul>");
	while((thing = thing.s("d.kahva")) != null) {
	    ZZCell h = thing.h("d.text");
	    wr.print("<li>");
	    printmsg(h, wr);
	}
	wr.print("</ul>\n");
    }

    void printAuthored(ZZCell thing, PrintWriter wr) {
	wr.print("<p>Messages written: <ul>");
	while((thing = thing.s("d.author")) != null) {
	    ZZCell h = thing.h("d.text");
	    wr.print("<li>");
	    wr.print("<a href=\""+getURL(h) + "\">"+ h.getText() + "</a>");
	}
	wr.print("</ul>\n");
    }

    void printReplies(ZZCell thing, PrintWriter wr) {
	thing = thing.s("d.replies");
	if(thing == null) return;

	wr.print("<p>Replies: <ul>");
	while((thing = thing.s("d.replies-list")) != null) {
	    ZZCell h = thing.h("d.text");
	    wr.print("<li>");
	    printmsg(h, wr);
	}
	wr.print("</ul>\n");
    }


    void printText(ZZCell start, PrintWriter wr) {
	wr.print("<h1>"+start.getText()+"</h1>");
	ZZCell im = start.h("d.images", true);
	if(im != null) {
	    wr.print("<img src=\""+im.getText()+"\">");
	}
	printAuthor(start, wr);
	wr.print("<p>");
	ZZCell orig = start;
	while((start = start.s("d.text")) != null) {
	    ZZCell h = start.h("d.kahva", true);
	    String txt = start.getText();
	    if(h != null) {
		txt = "<a href=\""+getURL(h) + "\">"+ txt + "</a>";
	    }
	    wr.print(txt);
	}
	if(orig.s("d.author", -1) == null) 
	    printAuthored(orig, wr);
	ZZCell inreply = orig.h("d.replies-list").s("d.replies",-1);
	if(inreply != null) {
	    wr.print("<p>In reply to ");
	    printmsg(inreply, wr);
	}
	printReplies(orig, wr);

	printRefs(orig, wr);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
	throws ServletException {
	try {
	synchronized(space) {
	    // resp.getWriter().print("HELLO\n\n!!\n\n");
	    String s = req.getQueryString();
	    PrintWriter wr = resp.getWriter();
	    wr.print("HELLO\n\n!!\n\n Query: '"+s+"'\n");
	    ZZCell c;
	    if(s == null || s.equals(""))
		c = space.getHomeCell().s("d.1");
	    else
		c = space.getCellByID(s);
	    wr.print("<br>Cell content: '"+c.getText()+"'\n");

	    printText(c, wr);
	    
	    wr.close();
	}
	} catch(Exception e) {
	    throw new ServletException("doGet: ", e);
	}
    }

}


