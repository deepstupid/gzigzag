/*   
CompoundSpace.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
 *    Copyright (c) 2001, Rauli Ruohonen
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
 * Written by Benja Fallenstein, based on code by Rauli Ruohonen
 */

package org.gzigzag.impl;
import java.util.*;
import org.gzigzag.*;
import org.gzigzag.vob.CharRangeIter;

/** A space capable of containing other spaces.
 *  This space does in itself not have functionality for loading and saving.
 * <p>
 * XXX A large amount of complexity is caused by using the Cell's 
 * inclusionObject field as the cell of the space below, since
 * for a while, spaces have been saved so that the creation of a new
 * inclusion and creating connections into the inclusion happen
 * at the same time.
 */

interface CompoundSpaceDim extends Dim {
    CopyableDim getBase();
}

