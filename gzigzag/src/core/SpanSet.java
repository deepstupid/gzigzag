/*   
SpanSet.java
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

/** An interface to Flobs that contain spans. This is more a Map than
 * a Set...
 * XXX Extend with links, i.e. one-level indirection between
 * overlaps.
 * XXX Rename to SpanMap!!!
 */

public interface SpanSet {
String rcsid = "$Id: SpanSet.java,v 1.4 2001/10/01 16:45:04 tjl Exp $";

    /** Add a span.
     */
    void addSpan(Span s, Object obj);

    /** Get the objects corresponding to the spans entered that overlap
     * the given span. XXX Should be enumeration?
     */
    java.util.Collection overlaps(Span s);

    /** Get an unmodifiable collection containing all the key spans of this set.
     */
    java.util.Collection spans();
}



