/*   
Util.java
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
import java.util.*;

/** An aggregation of utility methods. */
public class Util {

    public static String getLine(InputStream is) throws IOException {
        StringBuffer sb = new StringBuffer();
        do {
            int b = is.read();
            if (b == -1) {
                if (sb.length() == 0) throw new EOFException();
                break;
            }
            sb.append((char)b);
        } while (!(sb.length() >= 1
                   && sb.charAt(sb.length()-1) == 10));
        if (sb.length() >= 2 && sb.charAt(sb.length()-2) == 13) {
            sb.setLength(sb.length() - 2);
        } else {
            sb.setLength(sb.length() - 1);
        }
        return new String(sb);
    }

}
