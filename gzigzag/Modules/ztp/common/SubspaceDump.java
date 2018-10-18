/*   
SubspaceDump.java
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
package org.gzigzag.ztp;
import org.gzigzag.*;
import java.io.*;
import java.util.*;

public class SubspaceDump {
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log("[" + s + "]"); }
    private static void pa(String s) { ZZLogger.log(s); }

    public static class AbortedException extends ZZError {
        public AbortedException(String s) {
            super(s);
        }
    }

    /** Cell content.  This hashtable maps cell IDs into cell
     * content. */
    public Hashtable content = new Hashtable();
    
    /* Dimensions.  This hashtable maps dimension names into
     * hashtables that map cells to cells (key connected to datum
     * posward along the dimension). */
    public Hashtable dims = new Hashtable();


    /* Cell IDs.  This hashtable contains an entry for every cell that
     * has a content entry or a connection entry. */
    public Hashtable cellids = new Hashtable();

    public SubspaceDump(LineBufferedIO io) throws IOException {
        boolean ok = true;
        while (!io.isEOF()) {
            try {
                String line = io.readline();
                ZTPCommand cmd = new ZTPCommand(line);
                
                if (cmd.name.toUpperCase().equals("C")) {
                    if (cmd.args.length != 2) {
                        p("C args len = " + cmd.args.length);
                        ok = false;
                        continue;
                    }
                    Object o = content.get(cmd.args[0]);
                    String s = "";
                    if (o != null) s = (String)o;
                    s += cmd.args[1];
                    content.put(cmd.args[0], s);
                    cellids.put(cmd.args[0], cmd.args[0]);
                } else if (cmd.name.toUpperCase().equals("R")) {
                    if (cmd.args.length < 3) {
                        p("R args len = " + cmd.args.length);
                        ok = false;
                        continue;
                    }
                    Hashtable dim = (Hashtable)dims.get(cmd.args[0]);
                    if (dim == null) {
                        dim = new Hashtable();
                        dims.put(cmd.args[0], dim);
                    }
                    for (int i = 1; ok && i + 1 < cmd.args.length; i++) {
                        Object o = dim.get(cmd.args[i]);
                        if (o != null && !((String)o).equals(cmd.args[i+1])) {
                            p("existing connection (" + cmd.args[i] + ", " + (String)o + ") will be broken"+
                              " by new connection (" + cmd.args[i] + ", " + cmd.args[i+1]);
                            ok = false;
                            continue;
                        }
                        dim.put(cmd.args[i], cmd.args[i+1]);
                        cellids.put(cmd.args[i], cmd.args[i]);
                        cellids.put(cmd.args[i+1], cmd.args[i+1]);
                    }
                } else if (cmd.name.toUpperCase().equals("F")) {
                    break;
                } else if (cmd.name.toUpperCase().equals("A")) {
                    throw new AbortedException("Subspace data transfer aborted");
                } else {
                    p("unknown command");
                    ok = false;
                    continue;
                }
            } catch (SyntaxError e) {
                ZZLogger.exc(e);
                ok = false;
            }
        }
        if (!ok) throw new SyntaxError("Syntax error is subspace data");
    }

    public static void dump(LineBufferedIO io, Enumeration cells,
                            String[] sdims, String prefix) throws IOException {

        for (; cells.hasMoreElements();) {
            ZZCell c = (ZZCell)cells.nextElement();
            String cid = c.getGlobalID().substring(prefix.length());
            p("cell " + c);
            io.writeln("C " + ZTPCommand.encode(cid) + " " + ZTPCommand.encode(c.getText()));
            // Find connections from this cell
            for (int i = 0; i < sdims.length; i++) {
                String dim = sdims[i];
                p("dim " + dim);
                ZZCell d = c.s(prefix + dim);
                if (d != null) {
                    if (!d.getGlobalID().substring(0, prefix.length()).equals(prefix)) continue;
                    String did = d.getGlobalID().substring(prefix.length());
                    io.writeln("R " + ZTPCommand.encode(dim) 
                               + " " + ZTPCommand.encode(cid)
                               + " " + ZTPCommand.encode(did));
                }
            }
        }
        io.writeln("F");
    }
}
