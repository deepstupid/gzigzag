/*   
TestCursors.java
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
import junit.framework.*;

/** Test the Cursor class.
 */

public class TestCursors extends TestCase {
public static final String rcsid = "$Id: TestCursors.java,v 1.8 2002/03/13 17:28:23 bfallenstein Exp $";

    PermanentSpace s;
    
    public PermanentSpace getSpace() throws Exception { 
	return new PermanentSpace(TestImpl.zms);
    }

    public TestCursors(String s) throws Exception { 
	super(s); 
	this.s = getSpace();
    }
    
    public void testCursors() {
	Cell root = s.N();
	Cell curs1 = s.N();
	Cell curs1c1 = s.N();
	Cell curs1c2 = s.N();
	Cell curs2 = s.N();
	Cell curs3 = s.N();
	Cursor.set(curs1, root);
	Cursor.setCargo(curs1c1, curs1);
	Cursor.set(curs1c2, curs1c2);
	Cursor.setCargo(curs1c2, curs1);
	Cursor.set(curs2, root);
	Cursor.set(curs3, curs2);
	assertEquals(root, Cursor.get(curs1));
	assertEquals(root, Cursor.get(curs1c1));
	assertEquals(root, Cursor.get(curs1c2));
	assertEquals(root, Cursor.get(curs2));
	assertEquals(curs2, Cursor.get(curs3));
	Cursor.set(curs2, null);
	assertEquals(null, Cursor.get(curs2));
	assertEquals(null, Cursor.get(Cursor.get(curs3)));
	assertEquals(root, Cursor.get(curs1c2));
	Cursor.setCargo(curs1c1, curs3);
	assertEquals(root, Cursor.get(curs1c2));
	assertEquals(curs2, Cursor.get(curs1c1));
	assertEquals(curs2, Cursor.get(curs3));
	assertEquals(null, Cursor.get(Cursor.get(curs1c1)));
	Cursor.set(curs2, curs1);
	assertEquals(curs1, Cursor.get(Cursor.get(curs1c1)));
	Cursor.set(curs3, curs1c1);
	assertEquals(curs1c1, Cursor.get(curs1c1));
	Cursor.setCargo(curs1c1, null);
	assertEquals(null, Cursor.get(curs1c1));
	assertEquals(root, Cursor.get(curs1c2));
	Cursor.set(curs1c2, null);
	assertEquals(null, Cursor.get(curs1c2));
	assertEquals(null, Cursor.get(curs1));
	Cursor.set(curs1, root);
	Cursor.setCargo(curs1c1, curs1);
	assertEquals(root, Cursor.get(curs1));
	assertEquals(root, Cursor.get(curs1c1));
	assertEquals(root, Cursor.get(curs1c2));
	Cursor.setCargo(curs1c1, curs3);
	Cursor.set(curs3, curs1c1);
	assertEquals(curs1c1, Cursor.get(curs3));
	assertEquals(curs1c1, Cursor.get(curs1c1));
	assertEquals(root, Cursor.get(curs1));
	assertEquals(root, Cursor.get(curs1c2));
	Cursor.setCargo(curs1c2, null);
	assertEquals(null, Cursor.get(curs1c2));
    }

    /** Test the cursors-in-vstream stuff.
     */
    public void testPositions1() {
	Dim vstream = s.getDim(Dims.d_vstream_id);

	Cell c1 = s.N();
	Cell c2 = s.N();
	c1.setText("Testing-1-2-3");

	c2.setText("abc");
	Cell c3 = c2.s(vstream, 1);
	c3.disconnect(vstream, -1);

	Cell streamEnd = c1.h(vstream, 1);

	Cell curs1 = s.N();
	Cursor.set(curs1, c1);
	assertEquals(0, Cursor.getSide(curs1));
	assertEquals(c1, Cursor.get(curs1));
	assertEquals(null, Cursor.getVStreamCellBefore(curs1));

	Cursor.set(curs1, c1, -1);
	assertEquals(-1, Cursor.getSide(curs1));
	assertEquals(c1, Cursor.get(curs1));
	assertEquals(streamEnd, Cursor.getVStreamCellBefore(curs1));

	Cursor.set(curs1, c2);
	assertEquals(c2, Cursor.get(curs1));
	assertEquals(null, Cursor.getVStreamCellBefore(curs1));

	Cursor.setVStreamCellBefore(curs1, streamEnd);
	assertEquals(c1, Cursor.get(curs1));
	assertEquals(streamEnd, Cursor.getVStreamCellBefore(curs1));

	Cursor.setVStreamCellBefore(curs1, c1.s(vstream, 2));
	
	VStreamDim vsd = s.getVStreamDim();
	Cells.vStreamInsert(vsd, curs1, c3);
	assertEquals("Teabcsting-1-2-3", c1.t());
	
    }
}
