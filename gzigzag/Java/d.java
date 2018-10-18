/*   
d.java
 *    
 *    Copyright (c) 1999, Ted Nelson and Tuomas Lukka
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

/** A funny hack to allow funny code.
 * This class defines names for many of the common dimensions. If
 * you have imported the package, you can then refer to d.clone
 * as d.clone, without the quotation marks, and the constant in this
 * class will be there.
 * <p>
 * It is unfortunate that d.1 cannot be done with this, but this can
 * be a blessing since code shouldn't usually be hard-coded to use
 * the numbered dimensions.
 * <p>
 * This class may have to be retired once dimensions are cells in order
 * to allow different views to work on different dimensions.
 * @deprecated Not useful any more.
 */

public class d {
public static final String rcsid = "$Id: d.java,v 1.8 2000/10/26 18:09:29 tjl Exp $";
	public static final String clone="d.clone";
	public static final String mark="d.mark";
	public static final String cursor="d.cursor";
//	public static final String cursor-cargo="d.cursor-cargo";
	public static final String preflet="d.preflet";
}
