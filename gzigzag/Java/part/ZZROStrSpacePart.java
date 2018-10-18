/*   
ZZROStrSpacePart.java
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

/** <b>EXPERIMENTAL:</b> A read-only part of a ZZ space that only operates
 * on IDs.
 */

public abstract class ZZROStrSpacePart extends ZZROSpacePart {
public static final String rcsid = "$Id: ZZROStrSpacePart.java,v 1.3 2000/11/06 12:39:39 tjl Exp $";

    public ZZROStrSpacePart(ZZSpace space, String id) {
	super(space, id);
    }

    public Object parseIDPart(String idPart) {
	return null;
    }

    public String generateID(Object parsed) {
	return null;
    }
    public String generateIDPart(Object parsed) {
	return null;
    }
}

