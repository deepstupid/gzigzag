/*   
HTTPRequest.java
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

package org.gzigzag.mediaserver.http.client;
import org.gzigzag.mediaserver.http.*;
import java.io.*;
import java.net.*;
import java.util.*;

/** A HTTP request. */
public class HTTPRequest extends HTTPSendableMessage {

    static public boolean dbg = false;
    static private void p(String s) { if(dbg) System.out.println(s); }

    /** Create a HTTP request writing to os
     * @param os The input stream where the request is to be sent
     * @throws ParseException Indicates a syntax error in the request
     * @throws IOException Indicates an IO problem
    */
    public HTTPRequest(OutputStream os, String method,
                       String uri, String httpvers, boolean withBody)
        throws IOException {
        super(os, true, method + " " + uri + " " + httpvers, !withBody);
        this.method = method;
        this.uri = uri;
        this.httpVersion = httpvers;
    }

    public final String method;
    public final String uri;
    public final String httpVersion;

    protected void commitMessageHeaders() throws IOException {
        commit("Accept");
        commit("Accept-Charset");
        commit("Accept-Encoding");
        commit("Accept-Language");
        commit("Authorization");
        commit("Expect");
        commit("From");
        commit("Host");
        commit("If-Match");
        commit("If-Modified-Since");
        commit("If-None-Match");
        commit("If-Range");
        commit("If-Unmodified-Since");
        commit("Max-Forwards");
        commit("Proxy-Authentication");
        commit("Range");
        commit("Referer");
        commit("TE");
        commit("User-Agent");
    }  
    
    
}
