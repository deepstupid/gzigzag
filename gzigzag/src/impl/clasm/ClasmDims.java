/*   
ClasmDims.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
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
package org.gzigzag.impl.clasm;
import org.gzigzag.*;
import org.gzigzag.impl.Id;

/** Placeholder for the actual dimension IDs to be used with clasm.
 */

public class ClasmDims {
static public String rcsid = "$Id: ClasmDims.java,v 1.13 2001/07/24 13:06:23 raulir Exp $";

    static public String block = "0000000008000000E7A47A129B00046775ACD02C479B0D18B9BDFAFD09B90FE93D8191696CD7E3";

    static private String s = "home-id:" + block + "-";

    static public Cell d_params = Id.space.getCell(s+1);
    static public Cell d_call = Id.space.getCell(s+2);
    static public Cell d_def = Id.space.getCell(s+3);
    static public Cell d_prim = Id.space.getCell(s+4);
}



