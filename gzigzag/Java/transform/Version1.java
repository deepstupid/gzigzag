/*   
Version0.java
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
 * Written by Benjamin Fallenstein
 */
package org.gzigzag.transform;
import java.util.*;
import org.gzigzag.*;

/** ZZspace version changer - from version 1 to version 2.
 */

public class Version1 implements VersionChanger {
String rcsid = "$Id: Version1.java,v 1.3 2000/09/19 10:32:01 ajk Exp $";
	static public final void pa(String s) { System.out.println(s); }
	static public final void pan(String s) { System.out.print(s); }
/** Change the space from version 1 to version 2 to make it compatible.
 *  Change to 3-dimension cursing system, allowing every cell to be accursed:
 *  <ol>
 *  <li> fold d.cursor's headcells onto TMPDIM
 *  <li> change d.cursor to d.cursor-list
 *  <li> change TMPDIM to d.cursor
 *  </ol>
 *  @param s	The space to convert
 */
        public int changeVersion(ZZSpace s) {
		pa("Converting from version 1 to version 2:");
		pa("Change to 3-dimension cursing system.");
		pan("Folding d.cursor's headcells to TMPDIM... ");
                (new FoldDim("d.cursor", "TMPDIM", s)).transform();
		pa("done.");
		pan("Renaming to d.cursor and d.cursor-list... ");
                (new ChangeDim("d.cursor", "d.cursor-list", s)).transform();
		pan("d.cursor-list done... ");
                (new ChangeDim("TMPDIM", "d.cursor", s)).transform();
		pa(" d.cursor done.");
		pa("Converted to version 2.");
                return 2;
        }
}

