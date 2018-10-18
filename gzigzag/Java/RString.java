/*   
RString.java
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

/** A potentially referential immutable text string.
 */

public interface RString {
String rcsid = "$Id: RString.java,v 1.1 2000/10/20 11:35:29 tjl Exp $";

    String txt();
    
    RString substring(int start);
    RString substring(int start, int end);

    Span[] getSpans();
    Span[] getSpans(int start);
    Span[] getSpans(int start, int end);

}
