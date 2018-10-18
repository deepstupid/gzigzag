/*   
Writable.java
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
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
 * Written by Tuomas Lukka
 */
package org.gzigzag;
import java.util.*;

/** A part of a multi-file or other nearly append-only storage.
 */

public interface Writable {
String rcsid = "$Id: Writable.java,v 1.5 2000/09/19 10:32:01 ajk Exp $";
    void write(long offset, byte[] data);
    byte[] read(long offset, int n);
    void read(long offset, byte b[], int off, int len);
    long length();
    void flush();
}

