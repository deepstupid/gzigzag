/*   
MandatoryEncodings.java
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

/** An aggregation of implementations of the mandatory HTTP
 * transfer-encodings.
 */
class MandatoryEncodings {
    static public boolean dbg = false;
    static private void p(String s) { if(dbg) System.out.println(s); }

    public static void initialize() {
        new ChunkedEncoding();
        new IdentityEncoding();
    }

    private static void read(final InputStream is,
                             final byte[] b, 
                             final int start, 
                             final int len) throws IOException {
        int read = 0;
        while (read < len) {
            int r = is.read(b, start + read, len - read);
            if (r == -1) throw new EOFException("premature end of data");
            read += r;
        }
    }

    /** HTTP/1.1 identity transfer coding, either with content-length
     * or without it (see RFC 2616 section 4.4).  */
    private static class IdentityEncoding extends TransferEncodingHandler {
        
        public IdentityEncoding() {
            super("identity");
        }

        public InputStream decode(final InputStream is,
                                  final long transfer_length)
            throws IOException {
            return new InputStream() {
                    private long read = 0;
        
                    public synchronized int read() throws IOException {
                        if (transfer_length < 0) {
                            return is.read();
                        }
                        if (read == transfer_length) {
                            return -1;
                        }
                        int r = is.read();
                        if (r == -1) {
                            throw new EOFException("premature end of data");
                        }
                        read++;
                        return r;
                    }

                    public int available() throws IOException {
                        long a = is.available();
                        long b = transfer_length - read;
                        if (a > b) return (int)b;
                        return (int)a;
                    }

                    public void close() throws IOException {
                        is.close();
                    }


                };
        }

        public OutputStream encode(OutputStream os) throws IOException {
            return os;
        }


    }

    /** HTTP/1.1 chunked transfer coding (see RFC 2616 section 3.6.1). */
    private static class ChunkedEncoding extends TransferEncodingHandler {

        public ChunkedEncoding() {
            super("chunked");
        }

        private static final byte[] closingChunk = new byte[] { 48, 13, 10 };

        public OutputStream encode(final OutputStream os)
            throws IOException {
            return new OutputStream() {
                    private byte[] buf = new byte[4096];
                    private int bi = 0;
                    public synchronized void write(int b) throws IOException {
                        if (buf == null)
                            throw new IOException("stream is closed");
                        if (bi == buf.length) flush();
                        buf[bi++] = (byte)(b & 0xff);
                    }
                    private synchronized void writeBuf() throws IOException {
                        if (buf == null)
                            throw new IOException("stream is closed");
                        if (bi == 0) return;
                        final String hex = Integer.toHexString(bi);
                        final int hexl = hex.length();
                        final byte[] b = new byte[hexl + bi + 4];
                        for (int i = 0; i < hexl; i++) {
                            b[i] = (byte)hex.charAt(i);
                        }
                        b[hexl + 0] = 13;
                        b[hexl + 1] = 10;
                        for (int i = 0; i < bi; i++) {
                            b[hexl + 2 + i] = buf[i];
                        }
                        b[hexl + 2 + bi + 0] = 13;
                        b[hexl + 2 + bi + 1] = 10;
                        os.write(b);
                        bi = 0;
                    }
                    public synchronized void flush() throws IOException {
                        writeBuf();
                        os.flush();
                    }

                    public synchronized void close() throws IOException {
                        if (buf == null) return;
                        writeBuf();
                        buf = null;
                        os.write(closingChunk);
                        os.close();
                    }
                    public synchronized void finalize() throws Throwable {
                        try {
                            if (buf != null) close();
                        } catch (Throwable _) {}
                    }
                }; 
                
        }
        
        public InputStream decode(final InputStream is,
                                           long transfer_length) 
            throws IOException {
            return new InputStream() {
                    private byte[] buf = new byte[0];
                    private int dl = 0;
                    private int bi = 0;
                    private boolean eof = false;
                    
                    private synchronized void readChunk() throws IOException {
                        // we ignore chunk-extension
                        String line = Util.getLine(is);
                        try {
                            int sc = line.indexOf(';');
                            if (sc == -1) sc = line.length();
                            String s = line.substring(0, sc).trim();
                            p("line = " + line + ";sc = " + sc + "; s = " + s);
                            dl = Integer.parseInt(s, 16);
                        } catch (NumberFormatException e) {
                            throw new IOException(e.getMessage());
                        }
                        if (dl < 0) throw new IOException("negative size");
                        if (dl == 0) { eof = true; return; }
                        if (dl > buf.length) buf = new byte[dl];
                        MandatoryEncodings.read(is, buf, 0, dl);
                        if (is.read() != '\r' || is.read() != '\n')
                            throw new IOException("CRLF expected");
                        bi = 0;
                    }

                    public synchronized int read() throws IOException {
                        if (buf == null)
                            throw new IOException("stream is closed");
                        if (bi == dl) readChunk();
                        if (eof) return -1;
                        return buf[bi++];
                    }

                    public synchronized int read(byte b[],
                                                 int off,
                                                 int len) throws IOException {
                        if (buf == null)
                            throw new IOException("stream is closed");
                        if (bi == dl) readChunk();
                        if (eof) return -1;
                        int n = dl < len ? dl : len;
                        for (int i = 0; i < n; i++) {
                            b[off + i] = buf[bi + i];
                        }
                        bi += n;
                        return n;
                    }
                    
                    public synchronized long skip(long len) throws IOException {
                        if (buf == null)
                            throw new IOException("stream is closed");
                        if (bi == dl) readChunk();
                        if (eof) return 0;
                        long n = dl < len ? dl : len;
                        bi += n;
                        return n;
                    }
     
                    public synchronized int available() throws IOException {
                        if (buf == null)
                            throw new IOException("stream is closed");
                        return dl - bi;
                    }

                    public synchronized void close() throws IOException {
                        if (buf == null)
                            throw new IOException("stream is closed");
                        is.close();
                        buf = null;
                    }
                };
        }
    }


}
