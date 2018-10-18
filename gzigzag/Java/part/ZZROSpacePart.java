/*   
ZZROSpacePart.java
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

/** <b>EXPERIMENTAL:</b> A read-only ZZSpacePart.
 */

public abstract class ZZROSpacePart extends ZZSpacePart {
public static final String rcsid = "$Id: ZZROSpacePart.java,v 1.3 2000/11/06 12:39:39 tjl Exp $";

    public ZZROSpacePart(ZZSpace space, String id) {
	super(space, id);
    }

    public void setContent(ZZCellHandle c, Object o) {
	return;
    }
}

