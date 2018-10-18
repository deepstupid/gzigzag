/*   
VersionChanger.java
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
package org.gzigzag.transform;
import java.util.*;
import org.gzigzag.*;

/** A general interface for changing ZZspaces between versions.
 */
public interface VersionChanger {
String rcsid = "$Id: VersionChanger.java,v 1.4 2000/09/19 10:32:01 ajk Exp $";
    /** Takes the space which conforms to client space version of the 
     *  converter class. Needed transformations are then carried out
     *	to make the space conform to a newer version returned.
     *  @param s	The space to convert
     *  @return		The new version of the space
     */
    int changeVersion(ZZSpace s);
}

