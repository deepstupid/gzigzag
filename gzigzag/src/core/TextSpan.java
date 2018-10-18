/*   
TextSpan.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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

/** A span - contiguous piece of a permascroll - of text.
 * The natural units for text are (naturally) characters.
 */

public interface TextSpan extends Span1D {
String rcsid = "$Id: TextSpan.java,v 1.2 2001/07/08 07:47:22 tjl Exp $";

    /** Get the string contained in the span.
     */
    String getText();

}
