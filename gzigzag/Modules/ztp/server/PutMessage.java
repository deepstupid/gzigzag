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

class PutMessage implements Session.Message {
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log("[" + s + "]"); }
    private static void pa(String s) { ZZLogger.log(s); }

    static String prefix = "ztpspace!";

    public String name() { return "PUT"; }
    public String help() { return "Puts a subspace to the server"; }
    public int prereqs() { return Session.FL_AUTH; }
    
    public void invoke(Session sess, String[] args) throws IOException {
        LineBufferedIO io = sess.getIO();
        ZZSpace space = sess.space;
        if (args.length != 1) {
            io.writeln("5.0.1 Syntax: PUT <subspace>");
            return;
        }

        if (!args[0].equals("ALL")) {
            io.writeln("5.4.1 No such subspace");
            return;
        }

        io.writeln("3.4.0 Please proceed with subspace data, end with 'F'");
        

        SubspaceDump sd;
        try {
             sd = new SubspaceDump(io);
        } catch (SubspaceDump.AbortedException e) {
            io.writeln("5.4.0 " + e.getMessage());
            return;
        } catch (SyntaxError e) {
            io.writeln("5.0.1 " + e.getMessage());
            return;
        }

        // Verify that the cells all have a good ID.
        for (Enumeration e = sd.cellids.keys(); e.hasMoreElements();) {
            String s = (String)e.nextElement();
            if (s.indexOf("@") == -1) {
                io.writeln("5.4.2 Cannot put cells lacking a space ID");
                return;
            }
        }

        synchronized (space) {
            // Delete old subspace
            for (ZZCell c = space.getHomeCell().s("d.ztpspace", 1); c != null; ) {
                ZZCell s = c.s("d.ztpspace", 1);
                if (c.getID().substring(0, prefix.length()).equals(prefix)) {
                    c.delete();
                }
                c = s;
            }
            
            // Create the new subspace
            {
                ZZCell oc = space.getHomeCell();
                for (Enumeration e = sd.cellids.keys(); e.hasMoreElements();) {
                    String s = (String)e.nextElement();
                    ZZCell c = space.getCellByID(prefix + s);
                    oc.connect("d.ztpspace", 1, c);
                    oc = c;
                }
            }
            for (Enumeration e = sd.content.keys(); e.hasMoreElements();) {
                String s = (String)e.nextElement();
                ZZCell c = space.getCellByID(prefix + s);
                c.setText((String)sd.content.get(s));
            }
            for (Enumeration e = sd.dims.keys(); e.hasMoreElements();) {
                String dimname = (String)e.nextElement();
                Hashtable dim = (Hashtable)sd.dims.get(dimname);
                ((ZZDimSpace)space).d(dimname);
                for (Enumeration f = dim.keys(); f.hasMoreElements();) {
                    String s1 = (String)f.nextElement();
                    String s2 = (String)dim.get(s1);
                    ZZCell c1 = space.getCellByID(prefix + s1);
                    ZZCell c2 = space.getCellByID(prefix + s2);
                    c1.connect(prefix + dimname, c2);
                }
            }
            space.commit();
        }
        io.writeln("2.4.0 Subspace successfully put");
    }


}
