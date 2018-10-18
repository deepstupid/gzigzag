/*   
HTTPServer.java
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

package org.gzigzag.mediaserver.http.server;
import org.gzigzag.mediaserver.http.*;
import java.io.*;
import java.net.*;

/** A HTTP server class.  An application wishing to use this HTTP
 * server should instantiate this class and then start the listener
 * thread by calling the start method.  It should also create its own
 * derivative HTTPConnection that handles the serving of content as
 * the application sees fit.
 * @see HTTPConnection
 */
public class HTTPServer extends Thread {

    /** Instantiate this HTTP server.
        @param connf A factory of application-specific HTTP connection
        objects
        @param port The TCP port to bind to
        @throws IOException Indicates a failure to bind to the port
    */
    public HTTPServer(HTTPConnection.Factory connf, int port) 
        throws IOException {
        this.connf = connf;
        lsock = new ServerSocket(port);
    }

    /** Instantiate this HTTP server.
        @param connf A factory of application-specific HTTP connection
        objects
        @param port The TCP port to bind to
        @param backlog XXX see java.net.ServerSocket
        @throws IOException Indicates a failure to bind to the port
    */
     public HTTPServer(HTTPConnection.Factory connf, int port, int backlog)
        throws IOException {
        this.connf = connf;
        lsock = new ServerSocket(port, backlog);
    }

    /** Instantiate this HTTP server.
        @param connf A factory of application-specific HTTP connection
        objects
        @param port The TCP port to bind to
        @param backlog XXX see java.net.ServerSocket
        @param bindAddr XXX see java.net.ServerSocket
        @throws IOException Indicates a failure to bind to the port
    */
    public HTTPServer(HTTPConnection.Factory connf, int port, int backlog,
                      InetAddress bindAddr) throws IOException {
        this.connf = connf;
        lsock = new ServerSocket(port, backlog, bindAddr);
    }

    public void run() {
        try {
            while (!isInterrupted()) {
                Socket csock = lsock.accept();
                HTTPConnection sess =
                    connf.newConnection(csock);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private ServerSocket lsock;
    private HTTPConnection.Factory connf;

    public static void main(String[] argv) {
        try {
            HTTPServer hs = new HTTPServer(new HTTPConnection.Factory(), 5555);
            hs.run();
        } catch (IOException e) { e.printStackTrace(); }
    }

}
