/*   
Version2.java
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
package org.gzigzag.transform;
import java.util.*;
import org.gzigzag.*;

/** ZZspace version changer - from version 2 to version 3.
 * The change in the structure is that now rasters have names,
 * and rasters are called views.
 */

public class Version2 implements VersionChanger {
String rcsid = "$Id: Version2.java,v 1.4 2001/04/21 01:02:31 bfallenstein Exp $";
	static public final void pa(String s) { System.out.println(s); }
	static public final void pan(String s) { System.out.print(s); }
/** Change the space from version 2 to version 3 to make it compatible.
 * Simply recreates the system part of the space, since we now use names for views.
 */
        public int changeVersion(ZZSpace s) {
		pa("Converting from version 2 to version 3:");
		pa("Recreating system space (moving old to OldSystemList)");
		ZZDefaultSpace.create(s.getHomeCell());
                return 3;
        }
}

