/*   
NoteVob.java
 *    
 *    Copyright (c) 1999-2001, Ted Nelson and Tuomas Lukka
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
 * Written by Tuukka Hastrup
 */
package org.gzigzag.modules.pp.vob;
import org.gzigzag.*;
import org.gzigzag.vob.*;
import java.awt.*;

public class NoteVob extends NameVob {
public static final String rcsid = "$Id: NoteVob.java,v 1.4 2002/03/02 17:43:33 vegai Exp $";


    public NoteVob(Cell cell, Font f, FontMetrics fm, int pad) {
	super(cell, cell.t(), f, fm, pad);
    }
}
