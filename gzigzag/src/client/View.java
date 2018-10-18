/*
View.java
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
 * Written by Rauli Ruohonen
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.vob.*;

public interface View {
String rcsid = "$Id: View.java,v 1.1 2001/10/21 12:58:12 tjl Exp $";
    /** Draw a representation of the structure into a VobScene.
     * @param int The VobScene to render vobs into.
     * @param window The window cell whose contents are rendered.
     */
    void render(VobScene into, Cell window);
}
