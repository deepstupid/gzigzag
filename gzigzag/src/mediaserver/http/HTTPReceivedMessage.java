/*   
HTTPReceivedMessage.java
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
import java.io.*;
import java.net.*;
import java.util.*;

/** A HTTP message, receiver's POV. */
public class HTTPReceivedMessage {

    static public boolean dbg = false;
    static private void p(String s) { if(dbg) System.out.println(s); }

    /** Create a HTTP message reading from is
     * @param is The input stream where the request is to be read
     * @throws ParseException Indicates a syntax error in the request
     * @throws IOException Indicates an IO problem
    */
    public HTTPReceivedMessage(InputStream is) throws ParseException, IOException {
        String line = getLine(is);
        if (line.length() == 0) line = getLine(is);
        startLine = line;
        parseHeader(is);
        parseBody(is);
    }

    /** Get the body of a request header field.
     * @param hdr The name of the header field.
     * @return The body of the field or null if there was no such
     * field.
     */
    public String getField(String hdr) {
        return getField(hdr, null);
    }

    /** Get the body of a request header field.
     * @param hdr The name of the header field.
     * @param dflt Default value to return if there is no such field.
     * @return The body of the field or dflt
     */
    public String getField(String hdr, String dflt) {
        Object o = headers.get(hdr.toLowerCase());
        if (o == null) return dflt;
        return (String)o;
    }

    public void clearField(String hdr) {
        headers.remove(hdr.toLowerCase());
    }

    /** Enumerate the header field names.
     * @return An enumeration of the names of those header fields that
     * were present in the request.
     */
    public Iterator enumerateFieldNames() {
        return headers.keySet().iterator();
    }

    /** Get an input stream to the body of the request.
     * @return An input stream to the body of the request.
     */
    public InputStream getInputStream() {
        return bis;
    }

    /** A comma-separated list tokenizer.  XXX should probably create
     * a CSL class...
     * @param csl The comma-separated list.
     * @return An enumeration of the values in the csl from first to
     * last.
     */
    public static Enumeration tokenizeCSL(final String csl) {
        if (csl == null) return new Enumeration() {
                public boolean hasMoreElements() { return false; }
                public Object nextElement() {
                    throw new NoSuchElementException();
                }
            };
        return new Enumeration() {
                private int inx = 0;
                private final String s = csl.trim();
                public boolean hasMoreElements() {
                    return s.length() > 0 && inx != -1;
                }
                public Object nextElement() {
                    if (!hasMoreElements()) new NoSuchElementException();
                    int comma = s.indexOf(',', inx);
                    if (comma == -1) {
                        String rv = s.substring(inx).trim();
                        inx = -1;
                        return rv;
                    }
                    String rv = s.substring(inx, comma);
                    inx = comma + 1;
                    return rv;
                }
                
            };
    }

    /** Does a comma-separated list contain the needle?
     * @param csl The haysack
     * @param needle The needle
     * @returns Whether the haysack contains the needle
     */
    public static boolean doesCSLContainThis(String csl, String needle) {
        for (Enumeration e = tokenizeCSL(csl); e.hasMoreElements();) {
            if (((String)e.nextElement()).equals(needle)) return true;
        }
        return false;
    }

    /** A comma-separated list tokenizer.  XXX should probably create
     * a CSL class...
     * @param csl The comma-separated list.
     * @return An enumeration of the values in the csl from last to
     * first.
     */
    public static Enumeration tokenizeCSL_r(final String csl) {
        if (csl == null) return new Enumeration() {
                public boolean hasMoreElements() { return false; }
                public Object nextElement() {
                    throw new NoSuchElementException();
                }
            };
        return new Enumeration() {
                private final String s = csl.trim();
                private int inx = s.length();
                public boolean hasMoreElements() {
                    return s.length() > 0 && inx != -1;
                }
                public Object nextElement() {
                    if (!hasMoreElements()) throw new NoSuchElementException();
                    int comma = s.lastIndexOf(',', inx);
                    if (comma == -1) {
                        String rv = s.substring(0, inx).trim();
                        inx = -1;
                        return rv;
                    }
                    String rv = s.substring(comma,  inx);
                    inx = comma - 1;
                    return rv;
                }
                
            };
    }

    public void flush() throws IOException { bis.flush(); }

    protected final String startLine;

    private HashMap headers = new HashMap();
    private static HashMap encHandlers = new HashMap();

    private FlushableInputStream bis;

    private static String getLine(InputStream is) throws IOException {
        return Util.getLine(is);
    }

    private String unfoldField(String field) {
        StringBuffer sb = new StringBuffer();
        int n = field.length();
        boolean last_was_wsp = false;
        for (int i = 0; i < n; i++) {
            char c = field.charAt(i);
            if (c == 9 || c == 10 || c == 13 || c == 32) {
                if (!last_was_wsp) sb.append(' ');
                last_was_wsp = true;
            } else {
                sb.append(c);
                last_was_wsp = false;
            }
        }
        return new String(sb);
    }
    
    private void parseHeader(InputStream is) throws ParseException, IOException {
        String line = getLine(is);
        while (true) {
            StringBuffer fieldb = new StringBuffer();
            if (line.length() == 0) break;
            do {
                fieldb.append(line);
                line = getLine(is);
            } while (line.length() > 0 && (line.charAt(0) == 32
                                         || line.charAt(0) == 9));
            String field = new String(fieldb);
            int col = field.indexOf(':');
            if (col == -1) {
                p(field);
                throw new ParseException("missing a colon");
            }
            String name = field.substring(0, col).toLowerCase();
            String body = unfoldField(field.substring(col + 1)).trim();
            if (headers.containsKey(name)) {
                body = (String)headers.get(name) + ", " + body;
            }
	    p(field);
            headers.put(name, body);
            if (line.length() == 0) break;
        }
    }

    private void parseBody(final InputStream pis)
        throws ParseException, IOException {
        InputStream is = new FilterInputStream(pis) {
                public void close() throws IOException {}
            };
        String transencs = getField("Transfer-Encoding");
        if (transencs == null || transencs.length() == 0)
            transencs = "identity";

        int contentlength = Integer.parseInt(getField("Content-Length", "-1"));
        
        try {
            for (Enumeration e = tokenizeCSL_r(transencs);
                 e.hasMoreElements();) {
                String s = (String)e.nextElement();
                TransferEncodingHandler teh = TransferEncodingHandler.get(s);
                is = teh.decode(is, contentlength);
                contentlength = -1;
            }
        } catch (java.util.NoSuchElementException e) {
            throw new ParseException(e.getMessage());
        }

        bis = new FlushableInputStream(is);

    }

    private static class FlushableInputStream extends InputStream {
        public FlushableInputStream(InputStream is) {
            this.is = is;
        }

        public synchronized int read() throws IOException {
            return is.read();
        }
        public synchronized int read(byte[] b) throws IOException {
            return is.read(b);
        }
        public synchronized int read(byte[] b, int off, int len) throws IOException {
            return is.read(b, off, len);
        }
        public synchronized long skip(long n) throws IOException {
            return is.skip(n);
        }
        public synchronized int available() throws IOException {
            return is.available();
        }
        

        public void mark(int readlimit) { }
        public void reset() throws IOException {
            throw new IOException("mark not supported");
        }
        public synchronized void close() throws IOException {
            is.close();
        }
        public synchronized boolean markSupported() { return false; }

        public synchronized void flush() throws IOException {
            byte[] arr = new byte[16];
            int n = 0;
            while (true) {
                if (n == arr.length) {
                    byte[] narr = new byte[2 * arr.length];
                    for (int i = 0; i < n; i++) narr[i] = arr[i];
                    arr = narr;
                }
                int read = is.read(arr, n, arr.length - n);
                if (read == -1) break;
            }
            byte[] narr = new byte[n];
            for (int i = 0; i < n; i++) narr[i] = arr[i];
            is.close();
            is = new ByteArrayInputStream(narr);
        }

        private InputStream is;
    }

}
