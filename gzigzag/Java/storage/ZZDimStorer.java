/*   
ZZDimStorer.java
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
import java.awt.*;
import java.util.*;
import java.io.*;

/** A simple interface between persistent and temporary storage.
 * This class is used simply for transmitting information about 
 * connections.
 * <p>
 * The speciality of this class is that the operations need not make
 * sense atomically: only the end result of several operations counts.
 * So if a is connected to b, then connect(b, -, c), disconnect(a, +), 
 * is perfectly valid.
 * All disconnections must be given in both directions; connects 
 * only once (that's why there's no dir there).
 * <p>
 * If the above seems unclear, just see how it's used and where
 * it's not used: the class that is used for the on-line dims is ZZDimension.
 */

public interface ZZDimStorer {
String rcsid = "$Id: ZZDimStorer.java,v 1.4 2000/09/19 10:32:01 ajk Exp $";
    void storeConnect(String a, String b);
    void storeDisconnect(String a, int dir);
}
