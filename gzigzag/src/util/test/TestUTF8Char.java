/*   
TestUTF8Char.java
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
import junit.framework.*;

public class TestUTF8Char extends ZZTestCase {
public static final String rcsid = "$Id: TestUTF8Char.java,v 1.1 2001/07/25 06:58:24 ajk Exp $";

    public TestUTF8Char(String name) { super(name); }

    public void testReflexivity() throws java.io.IOException {
        for (char c = Character.MIN_VALUE; c < Character.MAX_VALUE; c++) {
            UTF8Char to = new UTF8Char(c);
            UTF8Char from = new UTF8Char(to.b);
            assertEquals(c, from.c);
        }
    }

    private static class CharDatum {
        public char c;
        public byte[] b;
        public CharDatum(char c, byte[] b) {
            this.c = c;
            this.b = b;
        }
    }

    public void testCharacters() throws java.io.IOException {
        CharDatum[] data = new CharDatum[] {
            new CharDatum((char)0x2260, new byte[]{ (byte)0xe2, (byte)0x89, (byte)0xa0 }),
            new CharDatum((char)0xa9, new byte[]{ (byte)0xc2, (byte)0xa9 })
        };
        for (int i = 0; i < data.length; i++) {
            UTF8Char to = new UTF8Char(data[i].c);
            assertEquals(data[i].b, to.b);
            UTF8Char from = new UTF8Char(data[i].b);
            assertEquals(data[i].c, from.c);
        }
    }

}
