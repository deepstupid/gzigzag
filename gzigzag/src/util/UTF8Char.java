/*   
UTF8Char.java
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
package org.gzigzag.util;

import java.io.*;

public class UTF8Char {
public static final String rcsid = "$Id: UTF8Char.java,v 1.1 2001/07/25 06:58:24 ajk Exp $";

    public char c;   
    public byte[] b; 

    /* UTF-8 <-> Java char conversion is table-driven.  This code is
     * adapted from CatDVI. */
    
    private static class ConvOctet {
        /** The number of variable bits, 1-8 */
        public int numbits;
        /** The template for this octet */
        public byte octet;

        public ConvOctet(int n, byte o) {
            numbits = n;
            octet = o;
        }

        public ConvOctet(int n, int o) {
            this(n, (byte) o);
        }
    }

    private static class ConvEntry {
        /** Maximum UCS-4 value encodable using this entry. */
        public char maxval;
        /** Info on each octet. */
        ConvOctet[] template;

        public ConvEntry(char maxval, ConvOctet[] template) {
            this.maxval = maxval;
            this.template = template;
        }

        public ConvEntry(int maxval, ConvOctet[] template) {
            this((char) maxval, template);
        }

        public final int len() { return template.length; }
    }

    private static final ConvEntry[] convtbl =
        new ConvEntry[] {
            new ConvEntry (0x7f,
                           new ConvOctet[] {
                               new ConvOctet(7, 0)
                           }),
            new ConvEntry (0x7ff,
                           new ConvOctet[] {
                               new ConvOctet(5, 0xc0),
                               new ConvOctet(6, 0x80)
                           }),
            new ConvEntry (0xffff,
                           new ConvOctet[] {
                               new ConvOctet(4, 0xe0),
                               new ConvOctet(6, 0x80),
                               new ConvOctet(6, 0x80)
                           }),
            new ConvEntry (0x1fffff,
                           new ConvOctet[] {
                               new ConvOctet(3, 0xf0),
                               new ConvOctet(6, 0x80),
                               new ConvOctet(6, 0x80),
                               new ConvOctet(6, 0x80)
                           }),
            new ConvEntry (0x3ffffff,
                           new ConvOctet[] {
                               new ConvOctet(2, 0xf4),
                               new ConvOctet(6, 0x80),
                               new ConvOctet(6, 0x80),
                               new ConvOctet(6, 0x80),
                               new ConvOctet(6, 0x80)
                           }),
            new ConvEntry (0x7fffffff, 
                           new ConvOctet[] {
                               new ConvOctet(1, 0xfc),
                               new ConvOctet(6, 0x80),
                               new ConvOctet(6, 0x80),
                               new ConvOctet(6, 0x80),
                               new ConvOctet(6, 0x80),
                               new ConvOctet(6, 0x80),
                           })
        };

    /** Convert a Java char into an UTF-8 octet stream. */
    public static byte[] charToUTF8(char c) throws IOException {
        // Stage 1: Determine which ConvEntry to use.  We use the
        // maxval attribute for this: the ConvEntry with smallest
        // maxval where the char fits is used.
        ConvEntry e = null;
        for (int i = 0; i < convtbl.length; i++) {
            if (c <= convtbl[i].maxval) {
                e = convtbl[i];
                break;
            }
        }
        if (e == null) 
            throw new UTFDataFormatException("character has no UTF-8 representation");
        
        // Stage 2: Do the conversion from right to left.
        byte[] rv = new byte[e.len()];
        for (int i = rv.length - 1; i >= 0; --i) {
            /* At each iteration we first apply the initial bit
               pattern and then patch the rest with the indicated
               number of lower-order bits in code.  When we are done,
               we shift these out. */  
            int numbits = e.template[i].numbits;
            rv[i] = e.template[i].octet;
            rv[i] |= ((1 << numbits) - 1) & c;
            c = (char) (c >> numbits);
        }
        return rv;
    }

    private static class UTF8ToCharRV {
        public char c;
        public int len;
        public UTF8ToCharRV(char c, int len) {
            this.c = c;
            this.len = len;
        }
    }

    /** Determine the ConvEntry to be used.  We determine this
        using the first octet's template. */
    private static ConvEntry UTF8ConvEntry(byte o) {
        for (int i = 0; i < convtbl.length; i++) {
            byte b = (byte) (o & ~((1 << convtbl[i].template[0].numbits) - 1));
            if (convtbl[i].template[0].octet == b) {
                return convtbl[i];
            }
        }
        return null;
    }
    /** Find out the length of an UTF-8 sequence based on the first
     * octet. */
    public static int UTF8Len(byte o) throws IOException {
        ConvEntry e = UTF8ConvEntry(o);
        if (e == null)
            throw new UTFDataFormatException("this is no UTF-8 character");
        return e.len();
    }

    private static UTF8ToCharRV UTF8ToChar(byte[] array, int start) throws IOException {
        ConvEntry e = UTF8ConvEntry(array[start]);
        if (e == null) return null;

        // Decode the octet stram.
        char rv = 0;
        for (int i = 0; i < e.template.length; i++) {
            byte mask = (byte) ((1 << e.template[i].numbits) - 1);
            byte fixed = (byte) (array[start + i] & ~mask);
            byte bits = (byte) (array[start + i] & mask);
            if (fixed != e.template[i].octet)
                throw new UTFDataFormatException("Invalid UTF-8 character");
            rv = (char) ((rv << e.template[i].numbits) | bits);
        }
        return new UTF8ToCharRV(rv, e.template.length);
    }

    private static void init(UTF8Char c, UTF8ToCharRV r, byte[] a, int start) throws IOException {
        if (r == null) throw new UTFDataFormatException("Invalid UTF-8 character");
        c.c = r.c;
        c.b = new byte[r.len];
        for (int i = 0; i < c.b.length; i++) {
            byte b = a[start + i];
            c.b[i] = b;
        }
    }

    private UTF8Char(UTF8ToCharRV r, byte[] a, int start) throws IOException {
        init(this, r, a, start);
    }

    public UTF8Char tryFromUTF8(byte[] a, int start) throws IOException {
        UTF8ToCharRV r = UTF8ToChar(a, start);
        if (r == null) return null;
        return new UTF8Char(r, a, start);
    }

    /** Create an UTF-8 char from the byte stream (bytes after the
     * character are ignored).  You can determine how many bytes was
     * used by invoking UTF8Len on the created object. */
    public UTF8Char(byte[] a) throws IOException { this(a, 0); }
    public UTF8Char(byte[] a, int start) throws IOException {
        init(this, UTF8ToChar(a, start), a, start);
    }

    public UTF8Char(char c) throws IOException {
        this.c = c;
        b = charToUTF8(c);
    }
    

}

