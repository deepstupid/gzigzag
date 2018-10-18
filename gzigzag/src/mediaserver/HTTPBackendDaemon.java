/*   
HTTPBackendDaemon.java
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
 * Written by Antti-Juhani Kaijanaho
 */

package org.gzigzag.mediaserver;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.http.*;
import org.gzigzag.mediaserver.http.server.*;
import org.gzigzag.mediaserver.ids.*;
import java.io.*;
import java.net.*;
import java.util.*;

/** The Mediaserver HTTP backend daemon. */
public class HTTPBackendDaemon extends HTTPConnection {

    static public boolean dbg = false;
    static private void p(String s) { if(dbg) System.out.println(s); }
    static private void pa(String s) { System.out.println(s); }

    public static class Factory extends HTTPConnection.Factory {
	Mediaserver ms;
	public Factory(Mediaserver ms) { this.ms=ms; }
        public HTTPConnection newConnection(Socket s) throws IOException {
            p("Mediaserver HTTP backend connection");
            return new HTTPBackendDaemon(s, ms, "/mediaserver/");
        }
    }

    protected HTTPBackendDaemon(Socket s, Mediaserver ms, String docroot) throws IOException {
        super(s);
        this.ms = ms;
        this.docroot = docroot;
    }

    private boolean matches(String thing, String substr) {
        return thing.length() >= substr.length() 
            && thing.substring(0, substr.length()).equals(substr);
    }

    /** Check the URL of the request.
     *  If this returns non-null, it has generated an error message that should
     *  be sent to the client.
     *  XXX better use a HTTPException scheme so this doesn't need to
     *  generate messages itself!
     */
    protected HTTPResponse checkURL(HTTPURL url, HTTPResponse.Factory resf) 
	throws IOException {
        if (!matches(url.getPath(), docroot)) {
            return resf.makeError(404, "We don't serve that here.");
        }
        if (url.getQuery() != null) {
            return resf.makeError(403, "No queries please");
        }
	return null;
    }

    protected HTTPResponse doGet(HTTPRequest req, HTTPResponse.Factory resf)
        throws IOException {
	HTTPURL url;
        try {
            url = req.getRequestURL();
        } catch (MalformedURLException e) {
            return resf.makeError(400, "Malformed URL");
        }
	HTTPResponse err = checkURL(url, resf);
	if(err != null)
	    return err;
        if (url.getPath().equals(docroot + "data.txt")) {
            // list data
            HTTPResponse res = resf.makeResponse(200, "OK");
            Writer w = new BufferedWriter(res.getWriter("plain"));
            for (Iterator it = ms.getIDs().iterator(); it.hasNext();) {
                Mediaserver.Id id = (Mediaserver.Id)it.next();
                w.write(id.getString() + "\n");
            }
            w.flush();
            return res;
        }
        if (url.getPath().equals(docroot + "data/")) {
            // list data in HTML
            HTTPResponse res = resf.makeResponse(200, "OK");
            Writer w = new BufferedWriter(res.getWriter("html"));
            w.write("<title>" + url.getPath() + "</title>\n");
            /*
	    w.write("<form action=\"\" enctype=\"multipart/form-data\" ");
	    w.write("method=\"post\">");
	    w.write("Submit file: <input type=\"file\" name=\"file\">");
	    w.write("<input type=\"submit\" value=\"Send\">");
	    w.write("<input type=\"reset\"></form><hr>");
            */
            w.write("<ul>\n");
            for (Iterator it = ms.getIDs().iterator(); it.hasNext();) {
                Mediaserver.Id id = (Mediaserver.Id)it.next();
                w.write("<li><a href=\"" + req.getRequestURI() + id.getString()
                        + "\">" + id.getString() + "</a></li> (<a href=\"" + req.getRequestURI() + "txt/" + id.getString() + "\">txt</a>)\n");
            }
            w.write("</ul>");
            w.flush();
            return res;
        }
        String tmp = docroot + "data/";
        if (matches(url.getPath(), tmp)) {
            String s = url.getPath().substring(tmp.length());
            String ct = "application/octet-stream";
            p(s);
            if (matches(s, "txt/")) {
                s = s.substring("txt/".length());
                ct = "text/plain";
            }
            p(s);
            // get based on an ID
            Mediaserver.Id id = new Mediaserver.Id(s);
            Mediaserver.Block datum;
            try {
                datum = ms.getDatum(id);
            } catch (IOException e) {
                return resf.makeError(404, e.getMessage());
            }
            HTTPResponse res = resf.makeResponse(200, "OK");
            res.setField("Content-Type", ct);
            OutputStream os = res.getOutputStream();
            os.write(datum.getRaw());
            os.close();
	    return res;
        }
        return resf.makeError(404, "We don't serve that here.");
    }

    // XXX should generate a new ID, NOT take one from the client!
    protected HTTPResponse doPost(HTTPRequest req, HTTPResponse.Factory resf) 
        throws IOException {
        return doUnknown(req, resf);
        /*	HTTPURL url;
        try {
            url = req.getRequestURL();
        } catch (MalformedURLException e) {
            return resf.makeError(400, "Malformed URL");
        }
	HTTPResponse err = checkURL(url, resf);
	if(err != null)
	    return err;
        if (url.getPath().equals(docroot + "data/")) {
            // write based on an ID
	    System.out.println("POST...");
	    System.out.println("Transfer-Encoding: " + req.getField("Transfer-Encoding"));
            InputStream is = req.getInputStream();
	    String key = ms.addDatum(is);
            HTTPResponse res = resf.makeResponse(200, "OK");
	    Writer w = new BufferedWriter(res.getWriter("html"));
	    w.write(key);
	    return res;
        }
        return resf.makeError(404, "Cannot POST to this address.");
        */
    }

    private Mediaserver ms;
    private String docroot;

    public static void main(String[] argv) {
        int port = 5555;
        int i;
	for (i = 0; i < argv.length; i++) {
	    if(argv[i].equals("-port")) {
		i++;
                port = Integer.parseInt(argv[i]);
	    } else {
		break;
            }
	}
	if (i != argv.length - 1) {
	    System.err.println("Need a storage directory.");
	    System.exit(1);
	}
        try {
            Storer stor = new DirStorer(new File(argv[i]));
            Mediaserver ms = new SimpleMediaserver(stor,new IDSpace(), 0);
            HTTPServer hs = new HTTPServer(new Factory(ms), port);
	    System.out.println("Listening on port "+port);
            hs.run();
        } catch (IOException e) { e.printStackTrace(); }
    }

}







