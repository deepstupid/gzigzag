/*   
HTTPResponse.java
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
import org.gzigzag.util.*;
import java.io.*;
import java.util.*;

/** A HTTP response.  
 * @see HTTPResponse.Factory
 */
public class HTTPResponse extends HTTPSendableMessage {

    /** A factory of HTTP responses. */
    public interface Factory {
        /** Create a response.
         * @param code The response code (eg. 404)
         * @param reason A short human-readable string explaining the
         * response.
         * @return A HTTP response with the given code and reason
         */
        HTTPResponse makeResponse(int code, String reason) 
            throws IOException;
        /** Create an error response.  The response will contain a
         * body containing the reason as human-readable text/html.
         * @param code The response code (eg. 404)
         * @param reason A short human-readable string explaining the
         * error.
         * @return A HTTP response with the given code and reason and
         * a human-readable body
         * @throws IOException Indicates a problem in creating the body
         */
        HTTPResponse makeError(int code, String reason)
            throws IOException;
    }

    /** Create a HTTP response that contains a body if the code
     * warrants it.
     * @param os The output stream where this response should be
     * written
     * @param chunked Should the body use HTTP chunked mode?
     * @param code The response code (eg. 404)
     * @param reason A short human-readable string explaining the
     * response.
     */
    public HTTPResponse(OutputStream os, boolean chunked,
                        int code, String reason) throws IOException {
        this(os, chunked, code, reason,
             code <= 199 || code == 204 || code == 304);
    }

    /** Create a HTTP response.
     * @param os The output stream where this response should be
     * written
     * @param chunked Should the body use HTTP chunked mode?
     * @param code The response code (eg. 404)
     * @param reason A short human-readable string explaining the
     * response.
     * @param suppressBody Should the response not contain a body?
     */
    public HTTPResponse(OutputStream os, boolean chunked, int code,
                        String reason, boolean suppressBody) throws IOException {
        super(os, chunked, "HTTP/1.1 " + code + " " + reason, suppressBody);
        if (code < 100 || code > 599) throw new InvalidStatusError();
    }

    /** An error class indicating that the status is invalid. */
    public static class InvalidStatusError extends Error {}

    protected void commitMessageHeaders() throws IOException {
        commit("Accept-Ranges");
        commit("Age");
        commit("ETag");
        commit("Location");
        commit("Proxy-Authenticate");
        commit("Retry-After");
        commit("Server");
        commit("Vary");
        commit("WWW-Authenticate");
    }
}
