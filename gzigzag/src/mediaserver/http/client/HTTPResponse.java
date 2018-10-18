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

package org.gzigzag.mediaserver.http.client;
import org.gzigzag.mediaserver.http.*;
import org.gzigzag.util.*;
import java.io.*;
import java.util.*;

/** A HTTP response.  
 */
public class HTTPResponse extends HTTPReceivedMessage {

    public HTTPResponse(InputStream is) throws ParseException, IOException {
        super(is);
        int sp1 = startLine.indexOf(' ');
        if (sp1 == -1 || sp1 + 1 >= startLine.length())
            throw new ParseException("malformed status line");
        int sp2 = startLine.indexOf(' ', sp1 + 1);
        if (sp1 == -1) sp2 = startLine.length();
        httpVersion = startLine.substring(0, sp1);
        status = Integer.parseInt(startLine.substring(sp1 + 1, sp2));
        reason = startLine.substring(sp2 + 1);
    }

    public final int status;
    public final String httpVersion;
    public final String reason;

}
