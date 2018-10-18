/*   
Peer.java
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
/* Written by Antti-Juhani Kaijanaho */

package org.gzigzag.net;

import java.net.*;

/** A networking peer: a pair (host, port).  This is used by the
 * reliable UDP scheme. */
public final class Peer {
    
    private InetAddress host;
    private int port;

    public Peer(InetAddress host, int port) {
        this.host = host;
        this.port = port;
    }

    public InetAddress getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Peer)) return false;

        Peer p = (Peer)o;
        
        return host.equals(p.host) && port == p.port;
    }

    public int hashCode() {
        return host.hashCode() ^ port;
    }

    public String toString() {
        return host.toString() + ":" + port;
    }

}
