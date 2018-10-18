/*   
ReliableUDPMessage.java
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
import java.text.*;
import java.util.*;

/** An abstract superclass of all message classes for the Reliable UDP
 * scheme.  
 * The message contains enough metainformation so that delivery 
 * can be guaranteed using an acknowledgment scheme.
 * <p>
 * This superclass contains almost all functionality that
 * ReliableUDPSocket needs from subclasses of this class; all
 * subclasses need to do is implementing their own payload
 * functionality.  The only exception is that the subclass must take
 * care of including the values from getSeq() and getAck() in the UDP
 * payload.  
 */
public abstract class ReliableUDPMessage {
    private long seq = 0;
    private long ack = 0;
    private long timestamp = 0;
    private Peer peer = null;

    static private DateFormat df = DateFormat.getDateInstance();

    public final synchronized long getSeq() { return seq; }
    public final synchronized long getAck() { return ack; }
    public final synchronized long getTimestamp() { return timestamp; }

    public final synchronized void setSeq(long seq) { this.seq = seq; }
    public final synchronized void setAck(long ack) { this.ack = ack; }
    public final synchronized void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public final synchronized Peer getPeer() { return peer; }
    public final synchronized void setPeer(Peer p) { peer = p; }

    /** Return a fresh byte array usable as UDP packet payload.  Note
     * that the return value must encode seq and ack to work with a
     * ReliableUDPSocket; timestamp is optional.  */
    public abstract byte[] toByteArray();

    /** Make a fresh copy of this message. */
    public abstract ReliableUDPMessage copy();

    public synchronized String toString() {
        return "[" + df.format(new Date(timestamp))
            + "] " + peer;
    }

}

