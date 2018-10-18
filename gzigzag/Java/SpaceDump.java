/*   
SpaceDump.java
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
/** A space dump class.  This class is used to dump the whole space
 * into a specially formatted text file designed to allow carrying on
 * information from any space version to the next and to parse the
 * aforementioned text file. */

package org.gzigzag;
import java.io.*;
import java.util.*;

public class SpaceDump {
public static final String rcsid = "$Id: SpaceDump.java,v 1.3 2001/02/20 11:32:45 ajk Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log(s); }
    private static void pa(String s) { ZZLogger.log(s); }

    // this is a useful shorthand
    private static String encode(String s) { return ZTPCommand.encode(s); }

    public String home;
    public Hashtable content = new Hashtable();
    public Hashtable cellswithspans = new Hashtable();
    public Hashtable dims = new Hashtable();
    public Hashtable stringscrolls = new Hashtable();

    public SpaceDump(Reader reader) throws IOException {
        BufferedReader r = new BufferedReader(reader);

        ZTPCommand com;
        try {
            com = new ZTPCommand(r.readLine());
            if (!com.name.toUpperCase().equals("H") || com.args.length != 3
                || !com.args[0].toUpperCase().equals("ZZSpaceDump".toUpperCase()))
                throw new ZZError("This is not a space dump");

            if (!com.args[1].equals("0"))
                throw new ZZError("I don't know how to slurp this file");
        } catch (SyntaxError e) {
            throw new ZZError("This is not a space dump (syntax error: + "
                              + e.getMessage() + ")");
        }

        home = com.args[2];

        while (true) {
            com = new ZTPCommand(r.readLine());

            if (com.name.toUpperCase().equals("C")) {
                if (com.args.length != 2)
                    throw new SyntaxError("wrong number of arguments for C");

                content.put(com.args[0], com.args[1]);
                cellswithspans.remove(com.args[0]);
            } else if (com.name.toUpperCase().equals("R")) {
                if (com.args.length < 3) 
                    throw new SyntaxError("too few arguments to R");

                if (!dims.containsKey(com.args[0])) {
                    dims.put(com.args[0], new Hashtable());
                }

                Hashtable dim = (Hashtable)dims.get(com.args[0]);

                for (int i = 1; i + 1 < com.args.length; i++) {
                    dim.put(com.args[i], com.args[i+1]);
                }
            } else if (com.name.toUpperCase().equals("SP")) {
                if (com.args.length != 2)
                    throw new SyntaxError("wrong number of arguments to SP");

                content.put(com.args[0], com.args[1]);
                cellswithspans.put(com.args[0], com.args[0]);
            } else if (com.name.toUpperCase().equals("SC")) {
                if (com.args.length != 2) 
                    throw new SyntaxError("wrong number of arguments to SC");
            
                String scroll = (String)stringscrolls.get(com.args[0]);
                if (scroll == null) scroll = "";
                scroll += com.args[1];
                stringscrolls.put(com.args[0], scroll);
            } else if (com.name.toUpperCase().equals("F")) {
                break;
            } else {
                throw new SyntaxError("unknown command");
            }
        }
    }

    /** Prints a dump of space to pw.  */
    public static void dump(final PrintWriter pw, final ZZSpace space) {
        pw.println("H ZZSpaceDump 0 " + encode(space.getHomeCell().getID()));

        Stack stack = new Stack();
        for (Enumeration e = space.cells(); e.hasMoreElements();) {
            stack.push(e.nextElement());
        }

        String[] dims = space.dims();

        Hashtable seen = new Hashtable();
        Hashtable scrolls = new Hashtable();
        while (!stack.empty()) {
            ZZCell c = (ZZCell)stack.pop();
            seen.put(c, c);
            p("processing cell " + c);

            Span s = c.getSpan();
            if (s == null) {
                pw.println("C " + encode(c.getID()) + " " + encode(c.getText()));
            } else {
                scrolls.put(s.getStart().getScroll(space), "");
                scrolls.put(s.getEnd().getScroll(space), "");
                pw.println("SP " + encode(c.getID()) + " " + encode("" + s));
            }
            
            for (int i = 0; i < dims.length; i++) {
                ZZCell d = c.s(dims[i]);
                if (d != null) {
                    p("processing connection (" + dims[i] + ": " + c + " -> " + d);
                    if (!seen.containsKey(d)) stack.push(d);
                    pw.println("R " + encode(dims[i]) + " "
                               + encode(c.getID()) + " " + encode(d.getID()));
                }
                d = c.s(dims[i], -1);
                if (d != null) p("processing connection (" + dims[i] + ": " + d + " -> " + c);
                if (d != null && !seen.containsKey(d)) stack.push(d);
            }
        }
        
        for (Enumeration e = scrolls.keys(); e.hasMoreElements();) {
            Scroll s = (Scroll)e.nextElement();
            if (!(s instanceof StringScroll)) {
                ZZLogger.log("ignoring non-string scroll " + s.getId());
                continue;
            }
            StringScroll ss = (StringScroll)s;
            final int ll = 50;
            for (long i = 0; i < ss.curEnd(); i += ll) {
                int l = (int)(ss.curEnd() - i < ll ? ss.curEnd() - i : ll);
                pw.println("SC " + encode(ss.getId()) + " " + encode(ss.getString(i, l)));
            }
        }
        pw.println("F");
    }
}
