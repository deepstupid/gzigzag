/*   
StringScroll.java
 *    
 *    Copyright (c) 1999-2000, Ted Nelson and Tuomas Lukka
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
 * Written by Tuomas Lukka,
 * multiformat and UTF-8 format support by Antti-Juhani Kaijanaho
 */
package org.gzigzag;
import java.util.*;
import java.io.*;

/** A scroll of chars. May later have holes etc. from expunging,
 * but basically the point is that the address to content mapping
 * is stable and will not change whatever is done.
 *
 * The new StringScroll format is based on UTF-8.  After the initial
 * signature described shortly, the scroll writable is a plain UTF-8
 * sequence.  The whole StringScroll should be 
 *
 * The signature of the new StringSroll format is the octet sequence
 * given in the "signature" attribute.
 */
public class StringScroll extends Scroll{
public static final String rcsid = "$Id: StringScroll.java,v 1.8 2001/02/09 08:30:54 ajk Exp $";

    /** Number of characters in a new-format "record".  Records are
     * implicit in the actual file; they are explicit only in the
     * in-memory index. */
    static int recordsize = 1000;

    private Writable f;
    private boolean readonly;

    private interface SSImpl {
        char[] get(long start, int n);
        long curEnd();
        long append(char[] bytes);
    }

    private SSImpl ss;

    private byte[] signature = new byte[]{ (byte) 0xef, (byte) 0xbb, (byte) 0xbf,
                                           (byte) 0x20, (byte) 0x20, (byte) 0x5a,
                                           (byte) 0x5a, (byte) 0x53, (byte) 0x53,
                                           (byte) 0x2d, (byte) 0x30, (byte) 0x0a };

    private synchronized void determineFormat() {
        boolean newformat;
        if (f.length() == 0) { 
            if (readonly) {
                newformat = false;
            } else {
                f.write(0, signature);
                newformat = true;
            }
        } else if (f.length() < signature.length) {
            newformat = false;
        } else {
            byte[] sig = f.read(0, signature.length);
            // assume it's new format; check signature and note it's
            // old format if signature does not match
            newformat = true;
            for (int i = 0; i < sig.length; i++) {
                if (sig[i] != signature[i]) {
                    newformat = false;
                    break;
                }
            }
        }
        if (newformat) ss = new UTF8Format(); else ss = new OldFormat();
    }

    public StringScroll(String id, Writable w, boolean ro) {
	super(id);
	this.readonly = ro;
	f = w;
        determineFormat();
    }

    /** Get an array of characters of length n starting from the
     * startth (or so) character. */
    public synchronized char[] get(long start, int n) {
        return ss.get(start, n);
    }

    // XXX ENCODING - currently 2 bytes / char :(
    public String getString(long start, int n) {
        char[] b = get(start,n);
        String s = new String(b);
        // p("GETS: "+start+" "+n+" "+b+" '"+s+"'");
        return s;
    } 
        
    public long curEnd() { 
        return ss.curEnd();
    }

    public synchronized long append(String b) {
        p("Append - length: "+b.length());
        return append(b.toCharArray());
    }

    public synchronized long append(char[] bytes) {
        return ss.append(bytes);
    }

    private class UTF8Format implements SSImpl {
        /** A private copy of recordsize in order to avoid changing
            the parent recordsize from messing up the index. */
        private int recordsize;

        /** We store in the index the offset - counting from the
            writable's start - of each recordsize character so that
            index[n] contains the offset of the character at the
            address n*recordsize. */
        private Vector index;

        /** Cache the address to be given to the next appended character.  */
        private long nextAddr;

        /** Put an offset for an address (which must be a multiple of
         * recordsize) into the index. */
        private void putOffs(long addr, long offs) {
            if (addr % recordsize != 0) {
                throw new ZZError("Address is not a record boundary");
            }

            // FIXME: Vector can index only using int's.  This is bad.
            long idx = addr / recordsize;
            if (idx > Integer.MAX_VALUE) {
                throw new ZZError("Internal error: idx too big");
            }

            if (idx >= index.size()) index.setSize((int) idx + 1);

            index.setElementAt(new Long(offs), (int)idx);

            // sanity check
            if (lookupOffs(addr) != offs) {
                throw new ZZError("Internal error: index add failure");
            }
        }

        /** Look offset up by address (must be a multiple of
         * recordsize). */
        private long lookupOffs(long addr) {
            if (addr % recordsize != 0) {
                throw new ZZError("Address is not in index");
            }

            // FIXME: Vector can index only using int's.  This is bad.
            long idx = addr / recordsize;
            if (idx > Integer.MAX_VALUE) {
                throw new ZZError("Internal error: idx too big");
            }

            return ((Long) index.elementAt((int) idx)).longValue();
        }
            
        public UTF8Format() {
            index = new Vector();
            recordsize = StringScroll.this.recordsize;
            
            // Build the index by scanning the whole scroll
            long offs = signature.length;
            long addr = 0;
            while (offs < f.length()) {
                UTF8Char uc = new UTF8Char(f, offs);
                
                if (addr % recordsize == 0) {
                    putOffs(addr, offs);
                }

                ++addr;
                offs += uc.b.length;
            }
            nextAddr = addr;
        }

        // Implementation of SSImpl

        public char[] get(long start, int n) {
            char[] rv = new char[n];

            int acc = 0;
            // The loop body will read at most n characters starting
            // from address start+acc by slurping the whole record and
            // scanning there.  The body is run repeatedly until we
            // have exactly n characters in rv.
            while (acc < n) {
                // Step 1: Read the current record completely into memory
                long recstart = (start + acc) - (start + acc) % recordsize;
                long recoffs = lookupOffs(recstart);
                byte[] record = f.read(recoffs, recordsize);
                // Step 2: Scan the record for the start address
                long sad = recstart;
                long sof = 0;
                while (sof < record.length) {
                    if (sad == start + acc) break;
                    UTF8Char uc = new UTF8Char(record, (int) sof);
                    sof += uc.b.length;
                    sad++;
                }
                // Step 3: Convert at most n bytes and at most to the
                // end of the record from record[sad] into rv, using acc as
                // the rv index.
                for (int i = (int) sof; i < record.length;) {
                    if (acc == n) break;
                    UTF8Char uc = new UTF8Char(record, i);
                    rv[acc++] = uc.c;
                    i += uc.b.length;
                }
            }

            return rv;
        }

        public long curEnd() {
            return nextAddr;
        }

        public long append(char[] chs) {
            // We want to avoid doing unnecessary writes.  Therefore,
            // we first build an array of UTF-8 characters.  Then we
            // flatten it into an onedimensional array containing the
            // UTF-8 representations in a row and write that.
            UTF8Char[] ucs = new UTF8Char[chs.length];

            long rv = nextAddr;

            int len = 0;
            for (int i = 0; i < chs.length; i++) {
                ucs[i] = new UTF8Char(chs[i]);
                len += ucs[i].b.length;
            }
            
            byte[] block = new byte[len];
            int bi = 0;
            for (int i = 0; i < ucs.length; i++) {
                // Update index
                if ((nextAddr + i) % recordsize == 0) {
                    putOffs(nextAddr + i, f.length() + bi);
                }
                // Copy UTF-8 octets to block.
                for (int j = 0; j < ucs[i].b.length; j++) {
                    block[bi++] = ucs[i].b[j];
                }
            }
            f.write(f.length(), block);
            nextAddr += ucs.length;

            if (nextAddr - nextAddr % recordsize
                != (index.size() - 1) * recordsize) {
                throw new ZZError("Internal error: index not consistent");
            }
            return rv;
        }

    }

    private class OldFormat implements SSImpl {
        public synchronized char[] get(long start, int n) {
            byte[] b = f.read(start*2, n*2);
            char[] c = new char[n];
            for(int i=0; i<n; i++)
                c[i] = (char)((b[i*2]<<8) | (b[i*2+1]<<0));
            return c;
        }

        public long curEnd() { 
            return f.length() / 2;
        }

        public synchronized long append(char[] bytes) {
            if(readonly) throw new ZZError("Can't append to readonly scroll");
            
            byte[] byt = new byte[bytes.length * 2];
            for(int i=0; i<bytes.length; i++) {
                byt[i*2] = (byte)((bytes[i] >> 8) & 0xff);
                byt[i*2+1] = (byte)((bytes[i] >> 0) & 0xff);
            }
            
            // XXX race?
            long c = f.length(); 
            if(c%1 != 0) throw new Error("Argh! Odd number of bytes");
            f.write(c, byt);

            return c / 2;
        }
    }
}



