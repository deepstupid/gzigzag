/*   
HTTPConnection.java
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

package org.gzigzag.mediaserver.http.server;
import org.gzigzag.mediaserver.http.*;
import java.io.*;
import java.net.*;

/** A HTTP connection handler.  An application wishing to implement
 * HTTP serving should derive from this class and override some or all
 * of the do* methods - and also it should derive from the Factory
 * class and give an instance of the derivative to the HTTPServer
 * class constructor.  A new thread is spawned by instantiating this
 * class.  <p> By default, this class implements a minimal HTTP server
 * connection that does not serve any content.
 * @see HTTPServer
 * @see HTTPConnection.Factory
 */
public class HTTPConnection {

    static public boolean dbg = false;
    static private void p(String s) { if(dbg) System.out.println(s); }
    static private void pa(String s) { System.out.println(s); }


    /** A factory of application-specific HTTP connection objects.
     * @see HTTPConnection
     */
    public static class Factory {
        /** Create a new connection object.
         * @param s The socket for this connection
         * @throws IOException XXX
        */
        public HTTPConnection newConnection(Socket s) throws IOException {
            p("httpconn");
            return new HTTPConnection(s);
        }
    }

    /** Create a new connection object.
     * @param s The socket for this connection
     * @throws IOException Indicates a problem with the socket
     */
    protected HTTPConnection(Socket s) throws IOException {
        this(s.getInputStream(), s.getOutputStream());
    }

    /** Create a new connection object.
     * @param is The input stream for this connection
     * @param os The output stream for this connection
     */
    protected HTTPConnection(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
        thread.start();
    }

    /** Close this connection forcefully.  This ends the thread
     * handling this connection and sends a notice to the client if
     * necessary.  */
    public void close() {
        if (thread != null) {
            try {
                thread.interrupt();
                thread.join();
                thread = null;
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** An end-of-connection hook.  A subclass can override this
     * method and thus be informed when the connection is closed for
     * some reason or another.  By default, this method is a
     * no-op.  */
    protected void endOfConnection() {}

    /** HTTP OPTIONS method handler.  This method is called when the
     * HTTP OPTIONS request is to be processed.  By default, doUnknown
     * is called.  
     * @param req The HTTP request from the client
     * @param resf A factory of HTTP responses
     * @return A HTTP response to be sent to the client
     * @throws IOException
     */
    protected HTTPResponse doOptions(HTTPRequest req, HTTPResponse.Factory resf) 
        throws IOException {
        return doUnknown(req, resf);
    }
    
    /** HTTP GET method handler.  This method is called when the HTTP
     * GET request is to be processed.  By default, it is responded to
     * with a 404.
     * @param req The HTTP request from the client
     * @param resf A factory of HTTP responses
     * @return A HTTP response to be sent to the client
     * @throws IOException
     */
    protected HTTPResponse doGet(HTTPRequest req, HTTPResponse.Factory resf)
        throws IOException {
        return resf.makeError(404, "Not found");
    }

    /** HTTP HEAD method handler.  This method is called when the HTTP
     * HEAD request is to be processed.  By default, doGet is called.
     * Note that the response factory given will create automatically
     * a response that will not include a body, so it is quite safe
     * (and recommended for most situations) to leave handling of the
     * HTTP HEAD requests to doGet - GET and HEAD should respond with
     * identical headers.
     * @param req The HTTP request from the client
     * @param resf A factory of HTTP responses
     * @return A HTTP response to be sent to the client
     * @throws IOException
     */
    protected HTTPResponse doHead(HTTPRequest req, HTTPResponse.Factory resf)
        throws IOException {
        return doGet(req, resf);
    }

    /** HTTP POST method handler.  This method is called when the HTTP
     * POST request is to be processed.  By default, doUnknown is
     * called.
     * @param req The HTTP request from the client
     * @param resf A factory of HTTP responses
     * @return A HTTP response to be sent to the client
     * @throws IOException
     */
    protected HTTPResponse doPost(HTTPRequest req, HTTPResponse.Factory resf) 
        throws IOException {
        return doUnknown(req, resf);
    }

    /** HTTP PUT method handler.  This method is called when the HTTP
     * PUT request is to be processed.  By default, doUnknown is
     * called.
     * @param req The HTTP request from the client
     * @param resf A factory of HTTP responses
     * @return A HTTP response to be sent to the client
     * @throws IOException
     */
    protected HTTPResponse doPut(HTTPRequest req, HTTPResponse.Factory resf) 
        throws IOException {
        return doUnknown(req, resf);
    }

    /** HTTP DELETE method handler.  This method is called when the
     * HTTP DELETE request is to be processed.  By default, doUnknown
     * is called.
     * @param req The HTTP request from the client
     * @param resf A factory of HTTP responses
     * @return A HTTP response to be sent to the client
     * @throws IOException
     */
    protected HTTPResponse doDelete(HTTPRequest req, HTTPResponse.Factory resf) 
        throws IOException {
        return doUnknown(req, resf);
    }

    /** HTTP TRACE method handler.  This method is called when the
     * HTTP TRACE request is to be processed.  By default, doUnknown
     * is called.
     * @param req The HTTP request from the client
     * @param resf A factory of HTTP responses
     * @return A HTTP response to be sent to the client
     * @throws IOException
     */
    protected HTTPResponse doTrace(HTTPRequest req, HTTPResponse.Factory resf) 
        throws IOException {
        return doUnknown(req, resf);
    }

    /** HTTP CONNECT method handler.  This method is called when the
     * HTTP CONNECT request is to be processed.  By default, doUnknown
     * is called.
     * @param req The HTTP request from the client
     * @param resf A factory of HTTP responses
     * @return A HTTP response to be sent to the client
     * @throws IOException
     */
    protected HTTPResponse doConnect(HTTPRequest req, HTTPResponse.Factory resf) 
        throws IOException {
        return doUnknown(req, resf);
    }

    /** Unknown method handler.  This method is called when the
     * an unknown HTTP request is to be processed.  By default, it is
     * responded with a 501.  
     * @param req The HTTP request from the client
     * @param resf A factory of HTTP responses
     * @return A HTTP response to be sent to the client
     * @throws IOException
     */
    protected HTTPResponse doUnknown(HTTPRequest req, HTTPResponse.Factory resf) 
        throws IOException {
        return resf.makeResponse(501, "Not implemented");
    }

    /** HTTP method dispatcher.  This method is called when a HTTP
     * request is to be processed.  By default it delegates the
     * request to the individual do* methods.  Note that subclasses
     * should override the do* methods whenever possible and override
     * this method only when there is no suitable do* method
     * available.
     * @param req The HTTP request from the client
     * @param resf A factory of 
     */
    protected HTTPResponse dispatch(HTTPRequest req, HTTPResponse.Factory resf) 
        throws IOException {
        String method = req.getMethod();
        if (method.equals("HEAD"))    return doHead(req, resf);
        if (method.equals("GET"))     return doGet(req, resf);
        if (method.equals("POST"))    return doPost(req, resf);
        if (method.equals("PUT"))     return doPut(req, resf);
        if (method.equals("DELETE"))  return doDelete(req, resf);
        if (method.equals("OPTIONS")) return doOptions(req, resf);
        if (method.equals("TRACE"))   return doTrace(req, resf);
        if (method.equals("CONNECT")) return doConnect(req, resf);
        /* */                         return doUnknown(req, resf);
    }


    private final InputStream is;
    private final OutputStream os;
    private Thread thread = 
        new Thread() {
                public final void run() {
                    while (!isInterrupted()) {
                        try {
                            iter(this);
                        } catch (IOException _) {
                            error(os, 503, "IO error");
                            break;
                        } catch (ParseException _) {
                            error(os, 400, "Request syntax terror");
                            break;
                        } catch (Throwable t) {
			    System.out.println("Caught in HTTPConnection "+
					       "mainloop:");
			    t.printStackTrace();
			    error(os, 500, "Internal error");
			    break;
			}
                    }
                    endOfConnection();
                    try { is.close(); } catch (IOException _) {}
                    try { os.close(); } catch (IOException _) {}
                    thread = null;
                }
            };

    void iter(Thread t) throws IOException, ParseException {
        final HTTPRequest req = new HTTPRequest(is);
        final boolean is11 = req.getHTTPVersion().equals("HTTP/1.1");
        final boolean close = !is11
            || HTTPRequest.doesCSLContainThis(req.getField("Connection"),
                                              "close");
        final boolean chunked = is11 && !close;
        p("" + is11 + " " + close + " "  + chunked);
        HTTPResponse res = 
            handleRequest(req, new HTTPResponse.Factory() {
                    private void common(HTTPResponse r) {
                    }
                    public HTTPResponse makeResponse(int code,
                                                     String reason)
                        throws IOException{
                        HTTPResponse r;
                        if (req.getMethod().equals("HEAD")) {
                            r = new HTTPResponse(os, chunked,
                                                 code, reason,
                                                 true);
                        } else {
                            r= new HTTPResponse(os, chunked,
                                                code, reason);
                        }
                        return r;
                    }
                    public HTTPResponse makeError(int code, String reason) 
                        throws IOException  {
                        HTTPResponse r;
                        if (req.getMethod().equals("HEAD")) {
                            r = new HTTPResponse(os, false, code, reason,
                                                 true);
                        } else {
                            r = new HTTPResponse(os, false, code, reason);
                        }
                        if (!chunked) r.setField("Connection", "close");
                        Writer o = r.getWriter("html");
                        o.write("<title>" + reason + "</title>\n<h1>" + code
                                + " - " + reason + "</h1>\n");
                        return r;
                    }
                });
        res.close();
        if (!chunked) t.interrupt();
    }

    private HTTPResponse handleRequest(HTTPRequest req,
                                       HTTPResponse.Factory resf)
        throws IOException {
        if (req.getHTTPVersion().equals("HTTP/1.1")
            && req.getField("Host") == null) {
            return resf.makeError(400, "Missing Host field, " +
                                  "read RFC 2616 section 14.23");
        }
        return dispatch(req, resf);
    }

    private static void error(OutputStream s, int code, String reason) {
        try {
            HTTPResponse res = new HTTPResponse(s, false, code, reason);
            res.setField("Connection", "close");
            Writer o = res.getWriter("html");
            o.write("<title>" + reason + "</title>\n<h1>" + code
                    + "</h1> - " + reason + "\n");
            res.close();
        } catch (IOException e) { 
            e.printStackTrace();
        }
    }


}
