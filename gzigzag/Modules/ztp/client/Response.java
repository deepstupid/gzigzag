/*   
Session.java
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
package org.gzigzag.ztp.client;
import org.gzigzag.*;
import org.gzigzag.ztp.*;

class Response {
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log("[" + s + "]"); }
    private static void pa(String s) { ZZLogger.log(s); }

    public int major;
    public int minor;
    public int ident;

    // Major codes
    public final static int positivePreliminary = 1;
    public final static int positiveCompletion = 2;
    public final static int positiveIntermediate = 3;
    public final static int transientNegativeCompletion = 4;
    public final static int permanentNegativeCompletion = 5;

    // Minor codes
    public final static int syntaxCode = 0;
    public final static int informationCode = 1;
    public final static int connectionCode = 2;
    public final static int authenticationCode = 3;
    public final static int subspaceCode = 4;

    public String explanation;

    public Response(String line) {
        try {
            ZTPCommand.RV r;

            r = ZTPCommand.ASCII_number(line);
            p("r = " + r);
            if (r == null || r.rest.charAt(0) != '.') throw new SyntaxError("bad response line: " + line);
            major = Integer.parseInt(r.token);
            
            r = ZTPCommand.ASCII_number(r.rest.substring(1));
            p("r = " + r);
            if (r == null || r.rest.charAt(0) != '.') throw new SyntaxError("bad response line: " + line);
            minor = Integer.parseInt(r.token);
            
            r = ZTPCommand.ASCII_number(r.rest.substring(1));
            p("r = " + r);
            if (r == null || r.rest.charAt(0) != ' ') throw new SyntaxError("bad response line: " + line);
            ident = Integer.parseInt(r.token);

            r = ZTPCommand.lwsp(r.rest);
            explanation = r.rest;
        } catch (NumberFormatException e) {
            throw new ZZError("" + e);
        }
    }

}
