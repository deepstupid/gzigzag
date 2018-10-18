/*   
ZZCursor.java
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

/** A thing that points to a cell or to an offset at a cell.
 * It is possible to have read-only ZZCursor objects which will
 * throw Errors for the modification attempts.
 */

public abstract class ZZCursor {
String rcsid = "$Id: ZZCursor.java,v 1.7 2000/11/25 00:36:33 tjl Exp $";
    /** Get the cell the cursor is pointing to. 
     */
    public abstract ZZCell get();
    /** Set the cursor to point to a cell.
     */
    public abstract void set(ZZCell c);
    /** Get the offset of this cursor.
     */
    public abstract int getOffs();
    /** Set the offset of this cursor.
     */
    public abstract void setOffs(int i);


    /** Set the cursor and offset to the given cursor.
     */
    public void set(ZZCursor c) {
	set(c.get());
	setOffs(c.getOffs());
    }

    /** The constant to specify that there is no offset 
     * for this cursor.
     */
    static public final int NO_OFFSET = -100;

    public boolean equals(Object o) {
	if(this == o) return true;
	if(!(o instanceof ZZCursor)) return false;
	ZZCursor oth = (ZZCursor) o;
	return (oth.get() == get() && oth.getOffs() == getOffs());
    }

    public String toString() {
	return "CURS: "+getOffs()+" of "+get()+"  Normal: "+super.toString();
    }

}
