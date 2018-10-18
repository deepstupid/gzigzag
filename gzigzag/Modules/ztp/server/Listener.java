/*   
Listener.java
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
import java.net.*;
import java.io.*;

public class Listener extends Thread {
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log("[" + s + "]"); }
    private static void pa(String s) { ZZLogger.log(s); }

    private ZZSpace space;
    private ServerSocket lsock;
    public Listener(ZZSpace space, ServerSocket lsock) {
        this.space = space;
        this.lsock = lsock;
    }
    public void run() {
        pa("Listener starts");
        while (!isInterrupted()) {
            try {
                p("waiting for connections");
                Socket csock = lsock.accept();
                p("connection request received");
                Session sess = new Session(space, csock);
                sess.start();
            } catch (IOException e) {
                ZZLogger.exc(e);
            }
            
        }
        pa("Listener stops");
    }
}

