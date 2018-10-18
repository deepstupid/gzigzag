/*
Connector.java
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
 * Written by Tero Mäyränen
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.vob.*;

public interface Connector {
String rcsid = "$Id: Connector.java,v 1.1 2001/10/21 12:58:12 tjl Exp $";
    /** Connect Vobs visibly
     * @param into VobScene for adding connection Vobs 
     *  	and getting Vob coordinates.
     * @param from Starting CellVob of connection
     * @param to Ending CellVob of connection
     * @param angle Starting angle of connection at starting Vob.  
     *		The ending angle
     * 		at the ending Vob is normally angle+180. 
     *		This value could also be used for
     * 		some completely different way of discriminating 
     *		between connections on
     * 		different dimensions, but for most it's the angle.
     *          angle==0 is up (0,1) and from there the angle increases
     *		clockwise.
     */
    void connect(VobScene into, Vob from, Vob to, int angle);
}
