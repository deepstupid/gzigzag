/*   
PutMessage.java
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
package org.gzigzag.ztp.server;
import org.gzigzag.*;
import org.gzigzag.ztp.*;
import java.io.*;
import java.util.*;

class GetMessage implements Session.Message {
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log("[" + s + "]"); }
    private static void pa(String s) { ZZLogger.log(s); }

    static String prefix = PutMessage.prefix;

    public String name() { return "GET"; }
    public String help() { return "Gets a subspace from the server"; }
    public int prereqs() { return Session.FL_AUTH; }
    
    public void invoke(Session sess, String[] args) throws IOException {
        LineBufferedIO io = sess.getIO();
        ZZSpace space = sess.space;
        if (args.length != 1) {
            io.writeln("5.0.1 Syntax: GET <subspace>");
            return;
        }

        if (!args[0].equals("ALL")) {
            io.writeln("5.4.1 No such subspace");
            return;
        }

        io.writeln("3.4.1 Proceeding with subspace data ...");

        synchronized (space) {
            // Find all relevant dimensions
            String[] dims;
            {
                Vector dimv = new Vector();
                for (ZZCell c = space.getHomeCell().s("d.masterdim", 1); c != null;
                     c = c.s("d.masterdim", 1)) {
                    String name = c.getText();
                    if (name.length() >= prefix.length()
                        && name.substring(0, prefix.length()).equals(prefix)) {
                        dimv.addElement(name.substring(prefix.length()));
                    }
                }
                dims = new String[dimv.size()];
                for (int i = 0; i < dims.length; i++) {
                    dims[i] = (String)dimv.elementAt(i);
                }
            }
            
            class Enum implements Enumeration {
                Enumeration e;
                Object cur;
                private void Cur() {
                    cur = null;
                    while (e.hasMoreElements()) {
                        cur = e.nextElement();
                        ZZCell c = (ZZCell)cur;
                        p("considering " + c);
                        if (c.getID().substring(0, prefix.length()).equals(prefix))
                            p("OK");
                            break;
                    }
                }
                public Enum(Enumeration e) {
                    this.e = e;
                    Cur();
                }
                public boolean hasMoreElements() {
                    return cur != null;
                }
                public Object nextElement() {
                    Object rv = cur;
                    if (rv == null) throw new NoSuchElementException();
                    Cur();
                    p("nextElement = " + rv);
                    return rv;
                }
            };

            SubspaceDump.dump(io,
                              new Enum(space.getHomeCell().s("d.ztpspace").enumRank("d.ztpspace")),
                              dims, prefix);

        }            

        io.writeln("2.4.1 Subspace successfully got");

    }

}
