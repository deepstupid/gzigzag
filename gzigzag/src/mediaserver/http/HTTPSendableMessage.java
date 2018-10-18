/*   
HTTPSendableMessage.java
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

package org.gzigzag.mediaserver.http;
import org.gzigzag.util.*;
import java.io.*;
import java.util.*;

/** A HTTP message, sender's POV.  
 */
public class HTTPSendableMessage {
    static public boolean dbg = false;
    static private void p(String s) { if(dbg) System.out.println(s); }

    /** Create a HTTP message for sending.
     * @param os The output stream where this response should be
     * written
     * @param chunked Should the body use HTTP chunked mode?
     * @param suppressBody Should the response not contain a body?
     */
    public HTTPSendableMessage(OutputStream os, boolean chunked,
                               String startLine,
                               boolean suppressBody) throws IOException {
        this.os = os;
        this.startLine = startLine + "\r\n";
        OutputStream s = new IdentityOutputStream();
        if (suppressBody) {
            p("null");
            s = new NullOutputStream();
        } else if (chunked) {
            p("chunked");
            s = TransferEncodingHandler.get("chunked").encode(s);
            setField("Transfer-Encoding", "chunked");
            this.chunked = true;
        } else {
            p("identity");
            s = TransferEncodingHandler.get("identity").encode(s);
        }
        this.bos = s;
    }

    /** An error indicating that (this part of) the message has
     * already been sent to the network. */
    public static class CommittedError extends Error {}

    /** An error class indicating an internal problem in synchronizing
     * messages. */
    public static class ResponseSynchronizationError extends Error {}

    /** Start sending this message.  This commits those header fields
     * that have been set. 
     * @throws IOException There was a problem in sending data to the
     * network.
     */
    public void commit() throws IOException {
        if (!startCommit()) return;
        byte[] ba = new byte[startLine.length()];
        for (int i = 0; i < ba.length; i++) ba[i] = (byte)startLine.charAt(i);
        os.write(ba);
        os.flush();
        commitFields();
    }

    /** Check whether the given header field has already been sent to
     * the network. 
     * @param name The name of the header field to check for
     * @return true if the header field has been sent
     */
    public synchronized boolean isFieldCommitted(String name) {
        Object o = header.get(name);
        if (o == null) return false;
        FieldData fd = (FieldData)o;
        return fd.committed;
    }

    /** Set the body of the given header field.  This is allowed only
     * if the header field has not been sent to the network yet.
     * @param name The name of the header field
     * @param body The body of the header field
     */
    public synchronized void setField(String name, String body) {
        assertFieldNotCommitted(name);
        header.put(name.toLowerCase(), new FieldData(name, body));
    }

    /** Delete the header field given.  This is allowed only if the
     * header field has not been sent to the network yet.
     * @param name The name of the header field to delete.
     */
    public synchronized void clearField(String name) {
        assertFieldNotCommitted(name);
        header.remove(name.toLowerCase());
    }

    /** Close this message, sending those header fields and that part
     * of the body that has not been sent yet.
     * @throws IOException There was a problem sending data to the
     * network
     */
    public synchronized void close() throws IOException {
        commit();
        bos.close();
        endCommit();
    }

    /** Append to a CSL header field.  This method appends the given
     * data to the body of the field as a new item in a
     * comma-separated list.  If the header field has already been
     * sent, the new data will be sent in the postamble of the HTTP
     * message (the field will be reassembled by the receiver).
     * @param name The name of the header field
     * @param body The data to append as a new item of a CSL
     */
    public synchronized void appendField(String name, String body) {
        String key = name.toLowerCase();
        if (header.containsKey(key)) {
            FieldData fd = (FieldData)header.get(key);
            if (fd.committed) fd.body = "";
            fd.body += ", " + body; // XXX check if this is right for
                                    // committed fields
            fd.committed = false;
            header.put(key, fd);
        } else {
            header.put(key, new FieldData(name, body));
        }
    }

    /** Get the output stream to the body of this message.
     * @return An output stream to the body of this message.
     */
    public OutputStream getOutputStream() { return bos; }

    /** Get a writer to the body of this message.
     * @return An UTF-8 writer that writes to the body of
     * this message
     */
    public Writer getWriter(String subtype) {
        setField("Content-Type", "text/" + subtype + "; charset=UTF-8");
        return new TextPlainWriter(getOutputStream());
    }

    protected synchronized void commit(String name) throws IOException {
        Object o = header.get(name.toLowerCase());
        if (o == null) return;
        FieldData fd = (FieldData)o;
        if (fd.committed) return;
        fd.committed = true;
        os.write(formatField(fd.name + ": " + fd.body));
        os.flush();
    }

    protected void commitMessageHeaders() throws IOException {
    }

    private static final class FieldData {
        public String name;
        public String body;
        public boolean committed;
        public FieldData(String name, String body) {
            this.name = name;
            this.body = body;
            committed = false;
        }
    }

    private String startLine;
    private HashMap header = new HashMap();
    private final OutputStream os;
    private final OutputStream bos;
    private boolean hasBody = false;
    private static final byte[] crlf = new byte[] { 13, 10 };
    private boolean committed = false;
    private boolean chunked;

    private void assertFieldNotCommitted(String name) {
        if (committed && !chunked) throw new CommittedError();
        Object o = header.get(name.toLowerCase());
        if (o == null) return;
        FieldData fd = (FieldData)o;
        if (fd.committed) throw new CommittedError();
    }

    private final class NullOutputStream extends OutputStream {
        public void write(int b) throws IOException {
            // do nothing
        }
    }

    private final class IdentityOutputStream extends OutputStream {
        private boolean closed = false;
        public synchronized void write(int b) throws IOException {
            startWrite();
            p("write " + b);
            //Thread.currentThread().dumpStack();
            os.write(b);
        }
        public void write(byte ba[]) throws IOException {
            startWrite();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(ba);
            p("write " + baos);
            //Thread.currentThread().dumpStack();
            os.write(ba);
        }
        public void write(byte ba[],
                          int off,
                          int len) throws IOException {
            startWrite();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(ba);
            p("write " + baos + "[" + off + " " + len + "]");
            //Thread.currentThread().dumpStack();
            os.write(ba, off, len);
        }
        public void flush() throws IOException {
            os.flush();
        }
        public void close() throws IOException {
            //Thread.currentThread().dumpStack();
            p("closed = " + closed + "; chunked = " + chunked);
            if (closed) return;
            closed = true;
            if (!chunked) os.close(); else commitFields();
            endCommit();
        }


        private void startWrite() throws IOException {
            if (!hasBody) {
                commit();
            }
            hasBody = true;
        }
    }

    private static final class TextPlainWriter extends Writer {
        private final OutputStream os;
        private byte[] buf = new byte[64];
        private int bi;
        public TextPlainWriter(OutputStream os) { this.os = os; }
        private void ch(char c) throws IOException {
            UTF8Char uc = new UTF8Char(c);
            if (bi + uc.b.length > buf.length) {
                byte[] nb = new byte[buf.length * 2];
                for (int j = 0; j < buf.length; j++) nb[j] = buf[j];
                buf = nb;
            }
            for (int j = 0; j < uc.b.length; j++) buf[bi + j] = uc.b[j];
            bi += uc.b.length;
        }
        public void write(char cbuf[],
                          int off,
                          int len) throws IOException {
            synchronized (lock) {
                for (int i = off; i < off + len; i++) {
                    if (cbuf[i] == '\n') {
                        ch('\r');
                        ch('\n');
                    } else {
                        ch(cbuf[i]);
                    }
                }
            }
            p("!!!");
            os.write(buf, 0, bi);
        }
        public void flush() throws IOException { os.flush(); }
        public void close() throws IOException { flush(); os.close(); }
    }

    private synchronized void commitFields() throws IOException {
        //Thread.currentThread().dumpStack();
        startCommit();
        // general headers
        commit("Cache-Control");
        commit("Connection");
        commit("Date");
        commit("Pragma");
        commit("Trailer");
        commit("Transfer-Encoding");
        commit("Upgrade");
        commit("Via");
        commit("Warning");
        commitMessageHeaders();
        // Entity headers
        for (Iterator i = header.keySet().iterator(); i.hasNext();) {
            String name = (String)i.next();
            commit(name);
        }
        os.write(crlf);
    }
    private static byte[] formatField(String field) {
        StringBuffer sb = new StringBuffer();
        int ll = 0;
        final int n = field.length();
        int lbp = -1;
        int ti = 0;
        for (int i = 0; i < n; i++) {
            char c = field.charAt(i);
            if (c == ' ') {
                lbp = ti;
            }
            sb.append(c);
            ti++;
            ll++;
            if (lbp != -1 && ll > 75) {
                sb.insert(lbp, '\r');
                sb.insert(lbp + 1, '\n');
                ti += 2;
                lbp = -1;
                ll = 0;
            }
        }
        sb.append("\r\n");
        byte[] rv = new byte[sb.length()];
        for (int i = 0; i < rv.length; i++) rv[i] = (byte)sb.charAt(i);
        return rv;
    }

    private boolean startCommit() {
        synchronized (active) {
            Object o = active.get(os);
            if (o == null) {
                active.put(os, this);
                committed = true;
                return true;
            }
            if (o == this) return false;
            throw new ResponseSynchronizationError();
        }
    }

    private void endCommit() {
        active.remove(os);
    }

    private static HashMap active = new HashMap();

}
