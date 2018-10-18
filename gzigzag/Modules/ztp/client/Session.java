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
import java.net.*;
import java.io.*;
import java.util.*;

public class Session implements SafeExit.Cleanupable {

    private String server;
    private Socket sock;
    private int port;
    private LineBufferedIO io;

    public Session(String server, int port) throws UnknownHostException {
        this.server = server;
        this.port = port;

        try {
            sock = new Socket(server, port);
            io = new LineBufferedIO(sock.getInputStream(), sock.getOutputStream());
            
            String line = io.readline();
            Response resp = new Response(line);
            if (resp.major != Response.positiveCompletion) {
                sock.close();
                throw new ZZError("no response from server");
            }
        } catch (IOException e) {
            throw new ZZError("" + e);
        }
        SafeExit.registerObject(this);
    }

    /** Login to server.  If passwd is null, attempt
     * preauthentication.  */
    public void login(String username, String passwd) throws IOException {
        if (passwd == null) {
            io.writeln("AUTH/PRE " + ZTPCommand.encode(username));
        } else {
            io.writeln("AUTH/PASS " + ZTPCommand.encode(username)
                       + " " + ZTPCommand.encode(passwd));
        }
        Response resp = new Response(io.readline());
        if (resp.major != Response.positiveCompletion) {
            throw new ZZError("Unable to login: " + resp.explanation);
        }
    }

    public void close() throws IOException {
        io.writeln("QUIT");
        io.readline();
        sock.close();
        SafeExit.unregisterObject(this);
    }

    public void cleanup() throws Throwable {
        io.writeln("A");
        io.readline();
        io.writeln("QUIT");
        io.readline();
        sock.close();
    }

    public void put(String subspace, Enumeration cells, String[] sdims) throws IOException {
        io.writeln("PUT " + ZTPCommand.encode(subspace));
        Response res = new Response(io.readline());
        if (res.major != Response.positiveIntermediate) {
            throw new ZZError("unable to put: " + res.explanation);
        }
        SubspaceDump.dump(io, cells, sdims, "");
        res = new Response(io.readline());
        if (res.major != Response.positiveCompletion) {
            throw new ZZError("failed to put: " + res.explanation);
        }
    }

    public void get(String subspace, ZZCell basecell) throws IOException {
        io.writeln("GET " + ZTPCommand.encode(subspace));
        Response res = new Response(io.readline());
        if (res.major != Response.positiveIntermediate) {
            throw new ZZError("unable to get: " + res.explanation);
        }

        SubspaceDump sd;
        try {
            sd = new SubspaceDump(io);
        } finally {
            res = new Response(io.readline());
            if (res.major != Response.positiveCompletion) {
                throw new ZZError("failed to get: " + res.explanation);
            }
        }
        
        // Delete the old subspace
        if (basecell != null && basecell.s("d.ztp-subspace") != null) {
            Subspace ss = new Subspace(basecell.s("d.ztp-subspace"));
            String[] sdims = ss.getSoftDims();
            for (Enumeration e = ss.getCells(); e.hasMoreElements();) {
                ZZCell c = (ZZCell)e.nextElement();

                for (int i = 0; i < sdims.length; i++) {
                    if (c.s(sdims[i], 1) != null) c.disconnect(sdims[i], 1);
                    if (c.s(sdims[i], -1) != null) c.disconnect(sdims[i], -1);
                }
                if (c.s("d.ztp-subspace", 1) != null) c.disconnect("d.ztp-subspace", 1);
                if (c.s("d.ztp-subspace", -1) != null) c.disconnect("d.ztp-subspace", -1);
            }
        }

        ZZDimSpace space = (ZZDimSpace)basecell.getSpace();

        // Create the new subspace
        {
            ZZCell oc = basecell;
            for (Enumeration e = sd.cellids.keys(); e.hasMoreElements();) {
                String s = (String)e.nextElement();
                ZZCell c = space.getCellByID(s);
                oc.connect("d.ztp-subspace", 1, c);
                oc = c;
            }
        }
        
        for (Enumeration e = sd.content.keys(); e.hasMoreElements();) {
            String s = (String)e.nextElement();
            ZZCell c = space.getCellByID(s);
            c.setText((String)sd.content.get(s));
        }
        for (Enumeration e = sd.dims.keys(); e.hasMoreElements();) {
            String dimname = (String)e.nextElement();
            Hashtable dim = (Hashtable)sd.dims.get(dimname);
            space.d(dimname);
            for (Enumeration f = dim.keys(); f.hasMoreElements();) {
                String s1 = (String)f.nextElement();
                String s2 = (String)dim.get(s1);
                ZZCell c1 = space.getCellByID(s1);
                ZZCell c2 = space.getCellByID(s2);
                    c1.connect(dimname, c2);
                }
            }


}

}
