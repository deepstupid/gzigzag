/*   
HTTPURL.java
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

package org.gzigzag.mediaserver.http;
import java.io.*;
import java.net.*;

/** A parsed representation of a HTTP URL.
 */
public class HTTPURL {

    /** Create a parsed representation of the given URL.
     * @param url The URL to parse.
     * @throws MalformedURLException The argument is not a HTTP URL.
     */
    public HTTPURL(String url) throws MalformedURLException {
        if (url.length() < 7 
            || !url.substring(0, 7).toLowerCase().equals("http://")) {
            abs_path = url;
            return;
        }
        host = url.substring(7);
        int slash = host.indexOf('/');
        if (slash != -1) {
            abs_path = host.substring(slash + 1);
            host = host.substring(0, slash);
        }
        int col = host.indexOf(':');
        if (col != -1 && col + 1 < host.length()) {
            try {
                port = Integer.parseInt(host.substring(col + 1));
            } catch (NumberFormatException e) {
                throw new MalformedURLException(e.getMessage());
            }
            host = host.substring(0, col);
        }
        int qm = abs_path.indexOf('?');
        if (qm != -1) {
            query = abs_path.substring(qm + 1);
            abs_path = abs_path.substring(0, qm);
        }
        host = unquote(host);
        abs_path = unquote(abs_path);
        query = unquote(query);
    }

    /** Get the host part of this URL.
     * @return The host part of this URL.
     */
    public String getHost() { return host; }

    /** Get the port part of this URL.
     * @return The port part of this URL.
     */
    public int getPort() { return port; }

    /** Get the absolute path part of this URL.
     * @return The absolute path part of this URL.
     */
    public String getPath() { return abs_path; }

    /** Get the query part of this URL.
     * @return The query part of this URL.
     */
    public String getQuery() { return query; }

    private String unquote(String s) throws MalformedURLException {
        if (s == null) return null;
        StringBuffer sb = new StringBuffer();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if (c == '%') {
                if (i + 3 > n) throw new MalformedURLException();
                String hex = s.substring(i + 1, i + 3);
                i += 2;
                int ci;
                try {
                    ci = Integer.parseInt(hex, 16);
                } catch (NumberFormatException e) {
                    throw new MalformedURLException();
                }
                c = (char)ci;
            }
            sb.append(c);
        }
        return new String(sb);
    }

    private String host = null;
    private int port = 80;
    private String abs_path = "/";
    private String query = null;

}
