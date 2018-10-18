/*   
FlobRaster.java
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

/** A fairly general interface for drawing a structure
 * on the screen using flobs.
 * The parameters have been read through the ZOb interface so
 * we don't need to worry about those here.
 */

public interface FlobView {
String rcsid = "$Id$";
    /** Draw a representation of the structure into the FlobSet.
     * An implementation of this routine will usually place a number
     * of Flobs and Renderables into 
     * The parameters dims and accursed are redundant but since they
     * are used so often, it's just good to have them.
     * @param into The FlobSet to place the flobs and decorations into.
     * @param fact A factory for constructing flobs. This may be used
     *		   or ignored by the FlobRaster. In general, it should
     *		   be used to draw the "run-of-the-mill" flobs.
     * @param view The viewspecs cell in the structure.
     * @param dims Three first dims 
     * @param accursed The cell that the cursor is on
     * @see Flob
     * @see Renderable
     * @see FlobSet
     * @see RasterFlobFactory
     */
    void raster(FlobSet into, FlobFactory fact,
		ZZCell view, String[] dims, ZZCell accursed);
}




