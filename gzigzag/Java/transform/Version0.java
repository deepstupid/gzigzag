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
 * Written by Tuukka Hastrup
 */
package org.gzigzag.transform;
import java.util.*;
import org.gzigzag.*;

/** ZZspace version changer - from version 0 to version 1.
 */

public class Version0 implements VersionChanger {
String rcsid = "$Id: Version0.java,v 1.7 2000/10/18 14:35:32 tjl Exp $";
	static public final void pa(String s) { System.out.println(s); }
	static public final void pan(String s) { System.out.print(s); }
/** Change the space from version 0 to version 1 to make it compatible:
 *  <ul>
 *  <li> d.mycursor is renamed to d.cursor-cargo
 *  <li> d.cursor-cargo and d.cursor directions are reversed
 *  <li> System cell "FlobCanvasRasters" is renamed as "FlobRasters"
 *  </ul>
 *  Note that flobs will not work before a new client space is created
 *  with command line options <b>new</b> and <b>dcold</b>.
 *  @param s	The space to convert
 */
        public int changeVersion(ZZSpace s) {
		pa("Converting from version 0 to version 1:");
		pa("Cursor dimensions rearranged for consistency.");
		pan("Changing d.mycursor connections to d.cursor-cargo:");
                (new ChangeDim("d.mycursor", "d.cursor-cargo", s)).transform();
		pa(" Name changed.");
		pan("Reversing connections along d.cursor and d.cursor-cargo:");
                (new ReverseDim("d.cursor", s)).transform();
                (new ReverseDim("d.cursor-cargo", s)).transform();
		pa(" Headcell (negend) now accursed.");
		pa("The new flob code won't work unless you create "
		 + "new client space (options -new and -dcold).");
		pa("Changing the system cell \"FlobCanvasRasters\" "
		 + "to \"Flobrasters\" to respect code changes.");
		ZZCell frasters = s.getHomeCell()
				   .findText("d.2", 1, "FlobCanvasRasters");
		if(frasters != null) {
			frasters.setText("FlobRasters");
			frasters.disconnect("d.1", 1);
			frasters.N("d.1", 1).setText("dummy");
		}
                return 1;
        }
}

