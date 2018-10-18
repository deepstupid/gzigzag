/*   
ReliableUDPSocket.java
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

import org.gzigzag.*;
import java.io.*;
import java.net.*;
import java.util.*;


/** A socket for sending and receiving reliable UDP messages.  
 * This
 * reliable UDP scheme guarantees delivery of a message but does not
 * guarantee order of delivery.  
 * It does not specify a format for the
 * UDP payload.  
 * Users of this class must implement
 * ReliableUDPMessageFactory and pass that implementation to the
 * constructor.  
 */
public class ReliableUDPSocket implements SafeExit.Cleanupable {
public static final String rcsid = "$Id: ReliableUDPSocket.java,v 1.3 2001/03/18 20:22:11 tjl Exp $";
    public static boolean dbg = false;
    protected static void p(String s) { if(dbg) ZZLogger.log(s); }
    protected static void pa(String s) { ZZLogger.log(s); }

    /** Construct a reliable UDP socket and bind it to some local
     * port. */
    public ReliableUDPSocket(ReliableUDPMessageFactory fact) throws SocketException {
        this(fact, new DatagramSocket());
    }

    /** Construct a reliable UDP socket and bind it to the given local
     * port. */
    public ReliableUDPSocket(ReliableUDPMessageFactory fact, int port) throws SocketException {
        this(fact, new DatagramSocket(port));
    }

    private ReliableUDPSocket(ReliableUDPMessageFactory fact, DatagramSocket socket) {
        this.fact = fact;
        this.socket = socket;
        this.local = new Peer(socket.getLocalAddress(), socket.getLocalPort()); 
        qflusher.start();
    }

    /** Send the given message. */
    public void send(ReliableUDPMessage rum) {
        p("==> " + rum);
        ReliableUDPMessage m = rum.copy();
        m.setTimestamp(System.currentTimeMillis());
        PeerEntry pe = getPeer(m);
        pe.setseq(m);
        pe.enqueue(m);
    }

    /** Receive a message. */
    public ReliableUDPMessage receive() throws IOException {
        synchronized (buf) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            ReliableUDPMessage m;
            PeerEntry pe;
            do {
                socket.receive(packet);
                byte[] bytes = new byte[packet.getLength()];
                byte[] origbytes = packet.getData();
                for (int i = 0; i < bytes.length; i++) bytes[i] = origbytes[i];
                m = fact.makeMessage(bytes);
                m.setPeer(new Peer(packet.getAddress(), packet.getPort()));
                pe = getPeer(m);
                pe.acknowledge(m);
                pe.process_ack(m);
            } while (pe.duplicate(m));
            p("<== " + m);
            return m;
        }
    }

    /** Get the local address and port. */
    public Peer getLocal() {
        return local;
    } 

    /** Get an array of currently active peers. */
    public Peer[] getActivePeers() {
        synchronized (peers) {
            Peer[] rv = new Peer[peers.size()];
            int i = 0;
            for (Enumeration e = peers.elements(); e.hasMoreElements();) {
                rv[i++] = (Peer)e.nextElement();
            }
            return rv;
        }
    }

    /** Return a rough measure of the roundtrip time between us and
     * the indicated peer, given in milliseconds.  If the peer is not
     * active, the return value is Long.MAX_VALUE.  Note that this
     * will not cause packets to be sent.  */
    public long getPeerLag(Peer p) {
        synchronized (peers) {
            if (!peers.containsKey(p)) return Long.MAX_VALUE;
            PeerEntry pe = getPeer(p);
            return pe.getMeasAverage();
        }
    }

    public void close() {
        try {
            socket.close();
            qflusher.interrupt();
            qflusher.join();
            qflusher = null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void cleanup() {
        close();
    }

    // PRIVATE STUFF

    private final ReliableUDPMessageFactory fact;
    private final DatagramSocket socket;
    private final Peer local;
    private final Hashtable peers = new Hashtable();
    private final byte[] buf = new byte[65536];
    private QueueFlusher qflusher = new QueueFlusher();

    private final class PeerEntry {
        private final class Outgoing {
            public ReliableUDPMessage m;
            public long time_entered = 0;
            public long last_sent = 0;
        }
        
        private Vector outgoing = new Vector();
        private Vector received = new Vector();
        private boolean needAck = false;
        private long ack = 0;
        private long seq = 1;
        private Peer peer;
        private long lastSend = 0;

        // for lag measurement
        private long[] meas = new long[16];  // any number, really
        private int measi = 0;
        private int num_meas = 0;

        public PeerEntry(Peer peer) { this.peer = peer; }

        private synchronized void addReceived(Long l) {
            received.addElement(l);
            int n = received.size();
            int i;
            for (i = n - 1; i >= 1; i++) {
                Long a = (Long)received.elementAt(i - 1);
                Long b = (Long)received.elementAt(i);
                if (a.longValue() < b.longValue()) break;
                received.setElementAt(a, i);
                received.setElementAt(b, i - 1);
            }
            if (n >= 2 && received.elementAt(i).equals(received.elementAt(i+1))) {
                received.removeElementAt(i+1);
            }
        }

        public synchronized boolean duplicate(ReliableUDPMessage rum) {
            if (rum.getSeq() <= seq) return true;
            if (received.contains(new Long(rum.getSeq()))) return true;
            return false;
        }

        private synchronized void addMeasurement(long m) {
            meas[measi] = m;
            measi = (measi + 1) % meas.length;
            if (num_meas < meas.length) ++num_meas;
        }

        public synchronized long getMeasAverage() {
            if (num_meas == 0) return Long.MAX_VALUE;
            long sum = 0;
            for (int i = 0; i < num_meas; i++) {
                sum += meas[i];
            }
            return sum / num_meas;
        }

        public synchronized void acknowledge(ReliableUDPMessage rum) {
            needAck = true;
            if (rum.getAck() > ack) addReceived(new Long(rum.getSeq()));
                
            int n = received.size();
            int i;
            for (i = 0; i < n; i++) {
                long l = ((Long)received.elementAt(i)).longValue();
                if (l != ack + i + 1) break;
            }
            Vector v = new Vector();
            ack += i;
            for (int j = i; j < n; i++) {
                v.addElement(received.elementAt(i));
            }
            received = v;
        }

        public synchronized void process_ack(ReliableUDPMessage rum) {
            int n = outgoing.size();
            Vector v = new Vector();
            for (int i = 0; i < n; i++) {
                Outgoing og = (Outgoing)outgoing.elementAt(i);
                if (og.m.getSeq() > rum.getAck()) {
                    v.addElement(og);
                } else if (og.m.getSeq() == rum.getAck()) {
                    addMeasurement(System.currentTimeMillis() - og.last_sent);
                }
            }
            outgoing = v;
        }

        public synchronized void enqueue(ReliableUDPMessage m) {
            Outgoing og = new Outgoing();
            og.m = m;
            og.time_entered = System.currentTimeMillis();
            og.last_sent = 0;
            
            outgoing.addElement(og);
            trySend(og);
        }

        public synchronized void setseq(ReliableUDPMessage rum) {
            rum.setSeq(seq++);
        }

        public synchronized void flush() {
            int n = outgoing.size();
            for (int i = 0; i < n; i++) {
                Outgoing og = (Outgoing)outgoing.elementAt(i);
                trySend(og);
                if (og.last_sent - og.time_entered > 10 * 60 * 1000) {
                    peers.remove(peer);
                    return;
                }
            }
            if (needAck) {
                Outgoing og = new Outgoing();
                og.m = fact.makeNoOp();
                og.m.setSeq(0);
                trySend(og);
            }
        }

        private void trySend(Outgoing og) {
            long now = System.currentTimeMillis();
            if (now - og.last_sent < 500) return;
            og.m.setAck(ack);
            needAck = false;
            lastSend = now;
            byte[] bytes = og.m.toByteArray();
            DatagramPacket dp = new DatagramPacket(bytes, bytes.length,
                                                   peer.getHost(), peer.getPort());
            try {
                socket.send(dp);
            } catch (IOException e) {
                needAck = true;
            }
            og.last_sent = now;
        }
        
    }

    private PeerEntry getPeer(ReliableUDPMessage rum) {
        Peer p = rum.getPeer();
        return getPeer(p);
    }

    private PeerEntry getPeer(Peer p) {
        synchronized (peers) {
            if (!peers.containsKey(p)) {
                peers.put(p, new PeerEntry(p));
            }
            return (PeerEntry)peers.get(p);
        }
    }

    private final class QueueFlusher extends Thread {
        public void run() {
            while (!isInterrupted()) {
                try {
                    sleep(500);
                    Peer[] ps = getActivePeers();
                    for (int i = 0; i < ps.length; i++) {
                        getPeer(ps[i]).flush();
                        yield();
                    }
                } catch (InterruptedException e) {
                    interrupt();
                }
            }
        }
    }

}
