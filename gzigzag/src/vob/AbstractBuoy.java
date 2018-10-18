/*
AbstractBuoy.java
 *    
 *    Copyright (c) 2000, Tuomas Lukka
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
package org.gzigzag.vob;
import java.util.*;
import java.awt.*;

/** A simple defalt implementatio of buoy methods.
 */

public abstract class  AbstractBuoy implements Buoy {
    private Vob anchor;
    public Vob getAnchor() { return anchor; }

    private Dimension prefSize;
    public Dimension getPreferredSize(Dimension dim) {
	if(dim == null) dim = new Dimension();
	dim.width = prefSize.width; dim.height = prefSize.height;
	return dim;
    }

    public AbstractBuoy(Vob anchor, Dimension prefSize) {
	this.anchor = anchor;
	this.prefSize = prefSize;
    }

}
