/*   
CellIds.java
 *
 *    Copyright (c) 2001 by Benja Fallenstein
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
 * Written by Benja Fallenstein
 */
package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.mediaserver.Mediaserver;
import java.util.*;

/** A mapping from String cell ids to small objects.
 *  This is an efficiency hack, because dealing with full cell ids takes up
 *  a lot of memory and is slow. We map String ids to <code>CellIds.Id</code>
 *  objects; the following invariants must hold:
 *  <ul>
 *    <le><code>get(s).equals(get(s));</code></le>
 *    <le><code>get(s).getString().equals(s);</code></le>
 *    <le><code>!get(s).equals(get(t));</code></le>
 *  </ul>
 *  where <code>s</code> and <code>t</code> are two arbitrary, different
 *  Strings. In other words: there must be an <code>Id</code> object for
 *  every <code>String</code>; this object must equal all other <code>Id</code>
 *  objects for the same <code>String</code>; it must not equal any
 *  <code>Id</code> object for a different <code>String</code>; and
 *  <code>Id.getString()</code> must return the String the <code>Id</code>
 *  object was constructed for (not the same object, of course, but an
 *  equal one).
 */

public interface CellIds {
     
     interface Id {
	boolean equals(Object other);
	int hashCode();
	
	/** Get the String representation of this id. */
	String getString();
     }

     /** Get the id for the given String representation. */
     Id get(String repr);

     /** Return the same as <code>get(new String(repr, offs, len))</code>.
      */
     Id get(byte[] repr, int offs, int len);

     /** Set the id of the currently read Mediaserver block.
      *  Used to expand local ids of the form "-xxx" into "msid-xxx".
      */
     void setBlockId(Mediaserver.Id msid);
}
