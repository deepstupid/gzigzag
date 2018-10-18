/*   
LineBufferedIO.java
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
package org.gzigzag.ztp;
import org.gzigzag.module.*;
import org.gzigzag.*;
import java.io.*;

/** Line-buffered UTF-8 I/O. */
public class LineBufferedIO {
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log(s); }
    private static void pa(String s) { ZZLogger.log(s); }

    InputStream in;
    OutputStream out;
    String buffer = new String();
    public LineBufferedIO(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }
    
    public synchronized void flush() throws IOException {
        for (int i = 0; i < buffer.length(); i++) {
            UTF8Char ch = new UTF8Char(buffer.charAt(i));
            out.write(ch.b, 0, ch.b.length);
        }
        out.flush();
        if (buffer.charAt(buffer.length()-1) == '\n') {
            p("==> " + buffer.substring(0, buffer.length()-1));
        } else {
            p("==> " + buffer);
        }
        buffer = "";
    }
    public synchronized void write(String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            buffer += s.charAt(i);
            if (s.charAt(i) == '\n') flush();
        }
    }
    public synchronized void writeln(String s) throws IOException {
        write(s + "\n");
    }
    
    private boolean ineof = false;
    
    public boolean isEOF() { return ineof; }
    
    /** Reads one UTF-8 character.  If EOF is reached, the EOF
     * flag is set and the returned value will be undefined. */
    public char getc() throws IOException {
        if (ineof) return '\n';
        int o = in.read();
        if (o == -1) {
            ineof = true;
            return '\n';
        }
        int len = UTF8Char.UTF8Len((byte) o);
        byte[] bs = new byte[len];
        bs[0] = (byte)o;
        for (int i = 1; i < len; i++) {
            o = in.read();
            if (o == -1) {
                ineof = true;
                throw new IOException("file ended before char was complete");
            }
            bs[i] = (byte)o;
        }
        return (new UTF8Char(bs)).c;
    }
    
    /* Reads until the end of line (including the EOL character),
     * or to EOF.  */
    public String readline() throws IOException {
        String rv = new String();
        
        char c;
        do {
            c = getc();
            if (isEOF()) break;
            rv += c;
        } while (c != '\n');
        
        if (rv.charAt(rv.length()-1) == '\n') {
            p("<== " + rv.substring(0, rv.length()-1));
        } else {
            p("<== " + rv);
        }
        return rv;
    }
    
}

