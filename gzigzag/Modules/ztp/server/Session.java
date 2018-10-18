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
package org.gzigzag.ztp.server;
import org.gzigzag.*;
import org.gzigzag.ztp.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class Session extends Thread {
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log("[" + s + "]"); }
    private static void pa(String s) { ZZLogger.log(s); }

    public ZZSpace space;
    private Socket socket = null;

    interface Message {
        String name();
        String help();
        int prereqs();
        void invoke(Session sess, String[] args) throws IOException;
    }

    private LineBufferedIO io;

    private boolean proper_close = false;

    public LineBufferedIO getIO() { return io; }

    private UserManager userManager;

    public UserManager getUserManager() { return userManager; }

    // prereq constants (orred)
    static final int FL_AUTH = 1;
    static final int FL_SAP = 2;

    private int reqstatus;

    public String user;

    public void setFlag(int flag) { reqstatus |= flag; }
    public void clearFlag(int flag) { reqstatus &= ~flag; }

    private void login(UserRecord ur) throws IOException {
        try {
            if (ur.sap) {
                setFlag(FL_SAP);
            } else {
                clearFlag(FL_SAP);
            }
            user = ur.username;
            setFlag(FL_AUTH);
            io.writeln("2.3.0 OK");
            if (socket != null) {
                pa("connection from " + socket.getInetAddress().getHostName()
                  + " authenticated as " + user);
            }
        } catch (Throwable e) {
            clearFlag(FL_AUTH);
            ZZLogger.exc(e);
            io.writeln("4.3.0 Problem authenticating user");
        }
    }

    Message[] messages = {
        new Message() {
                public String name() { return "QUIT"; }
                public String help() { return "Quit the session"; }
                public int prereqs() { return 0; }
                public void invoke(Session ses, String[] args) throws IOException {
                    clearFlag(FL_AUTH);
                    ses.interrupt();
                    ses.proper_close = true;
                    ses.io.writeln("2.2.1 Be seeing you");
                }
            },
        new Message() {
                public String name() { return "AUTH/PRE"; }
                public String help() { return "Request preauthentication"; }
                public int prereqs() { return 0; }
                public void invoke(Session ses, String[] args) throws IOException {
                    LineBufferedIO io = ses.getIO();
                    if (args.length != 1) {
                        io.writeln("5.0.1 Syntax: AUTH/PRE <username>");
                        return;
                    }
                    UserRecord ur = ses.getUserManager().access(args[0]);
                    if (ur == null || !ur.auth_pre) {
                        io.writeln("5.3.0 Access denied");
                        return;
                    }
                    if (ur.susp) {
                        io.writeln("5.3.1 User disabled");
                        return;
                    }
                    ses.login(ur);
                }
            },
        new Message() {
                public String name() { return "AUTH/PASS"; }
                public String help() { return "Request password authentication"; }
                public int prereqs() { return 0; }
                public void invoke(Session ses, String[] args) throws IOException {
                    LineBufferedIO io = ses.getIO();
                    if (args.length != 2) {
                        io.writeln("5.0.1 Syntax: AUTH/PASS <username> <password>");
                        return;
                    }
                    UserRecord ur = ses.getUserManager().access(args[0]);
                    if (ur == null || !ur.auth_pass || !ur.password.equals(args[1])) {
                        io.writeln("5.3.0 Access denied");
                        return;
                    }
                    if (ur.susp) {
                        io.writeln("5.3.1 User disabled");
                        return;
                    }
                    ses.login(ur);
                }
            },
        new PutMessage(),
        new GetMessage()
    };

    private static Hashtable sessions = new Hashtable();
   

    public static Enumeration getActiveSessions() {
        synchronized (sessions) {
            return sessions.keys();
        }
    }
    
    public Session(ZZSpace space, Socket sock) throws IOException {
        this(space, sock.getInputStream(), sock.getOutputStream());
        socket = sock;
    }
    
    public Session(ZZSpace space, InputStream in, OutputStream out) throws IOException {
        this.space = space;
        io = new LineBufferedIO(in, out);
        socket = null;
    }
    
    public void run() {
        try {
            p("session start");
            if (socket != null) {
                pa("accepting connection from " + socket.getInetAddress().getHostName());
            }
            userManager = UserManager.create(space);
            synchronized (sessions) {
                sessions.put(this, this);
            }
            try {
                io.writeln("2.2.0 Hello there!");
            } catch (IOException e) {
                ZZLogger.exc(e);
            }
            while (!io.isEOF() && !isInterrupted()) {
                try {
                    try {
                        String line = io.readline();
                        p("read " + line);
                        ZTPCommand c = new ZTPCommand(line);
                        p("" + c);
                        Message m = null;
                        for (int i = 0; i < messages.length; i++) {
                            if (messages[i].name().toUpperCase().equals(c.name.toUpperCase())) {
                                m = messages[i];
                            }
                        }
                        if (m == null) {
                            io.writeln("5.0.1 What?");
                        } else {
                            if (((m.prereqs() & reqstatus) ^ m.prereqs()) == 0) {
                                m.invoke(this, c.args);
                            } else {
                                io.writeln("5.0.2 Permission denied");
                            }
                        }
                    } catch (SyntaxError e) {
                        io.writeln("5.0.1 " + e.getMessage());
                    }
                } catch (IOException e) {
                    ZZLogger.exc(e);
                }
            }
        } finally {
            synchronized (sessions) {
                try {
                    if (!proper_close) io.writeln("4.2.1 Aborting");
                    socket.close();
                } catch (IOException e) {
                    ZZLogger.exc(e);
                }
                sessions.remove(this);
            }
            userManager.close();
        }
        if (socket != null) {
            pa("closing connection from " + socket.getInetAddress().getHostName());
        }
        p("session end");
    }
}

