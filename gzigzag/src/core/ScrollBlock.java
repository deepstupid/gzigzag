/*   
ScrollBlock.java
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

/** A block from which typed spans may be taken.
 * A larger unit of fluid media.
 * @see Span
 * @see Span1D
 * @see TextSpan
 * @see ImageSpan
 */

public abstract class ScrollBlock {
String rcsid = "$Id: ScrollBlock.java,v 1.4 2001/05/15 04:33:13 tjl Exp $";

    final String id;

    public ScrollBlock(String id) { this.id = id; }

    /** Get the current contents of this block as a single span
     * of the appropriate type.
     */
    public abstract Span getCurrent();

    /** Whether this block has been finalized or whether more
     * can be appended.
     */
    public abstract boolean isFinalized();

    /** Get the globally unique identifier of this block.
     * XXX Exact semantics, tmp ids
     */
    public final String getID() { return id; }

    /** Compare this scrollblock with the other one and return 
     * -1, 0 or 1 accordingly.
     * All scrollblocks are well-ordered. 
     * <p>
     * We currently define this to be
     * <pre>getID().compareTo(sb.getID()) </pre>
     * but this may change in the future.
     */
    public final int compareTo(ScrollBlock sb) {
	return getID().compareTo(sb.getID()) ;
    }

}

