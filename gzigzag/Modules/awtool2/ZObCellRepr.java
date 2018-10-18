/*   
ZOb.java
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
 * Written by Kimmo Wideroos
 */
 
package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;

/** Absract class for ZObs which are able to be rolled back into the cellstructure.
 */

public abstract class ZObCellRepr {
    private ZZCell _rootcell = null; 
    public ZZCell getCell() { return this._rootcell; }
    public void setCell(ZZCell c) { this._rootcell = c; }

    public String getZObClassName() { 
        String cn = getClass().getName();
        int i = cn.lastIndexOf(".");
        return cn.substring(i+1, cn.length()); 
    }

    abstract String getZObName();

    /** Write the parameters of this ZOb back in the cellstructure.
     *  Returns a string describing the errors encountered, or null if
     *  everything went fine..
     *  @param start    starting cell
     *  @param clone    whether or not to make the new ZOb centralcell a clone
     *                  of the former centralcell (if cells equal, the whole 
     *                  issue is trivial).
     */
    abstract String cellRepr(ZZCell start, boolean clone);

    public String cellRepr() {return cellRepr(getCell(), false); }
}
 
