/*   
TestHTTPUtil.java
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
import java.util.*;
import java.io.*;
import junit.framework.*;

public class TestHTTPUtil extends ZZTestCase {
public static final String rcsid = "$Id: TestHTTPUtil.java,v 1.1 2001/07/25 08:09:10 ajk Exp $";

    public TestHTTPUtil(String name) { super(name); }

    public void testGetLine() throws IOException {
        String material = "Test\r\nFoo\nBar\r\n";
        InputStream is = new ByteArrayInputStream(material.getBytes("UTF-8"));
        String line = Util.getLine(is);
        assertEquals("Test", line);
        line = Util.getLine(is);
        assertEquals("Foo", line);
        line = Util.getLine(is);
        assertEquals("Bar", line);
    }
    
}
