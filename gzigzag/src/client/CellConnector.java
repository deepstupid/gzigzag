/*
CellConnector.java
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
import java.awt.*;

public class CellConnector implements Connector {
String rcsid = "$Id: CellConnector.java,v 1.1 2001/10/21 12:58:12 tjl Exp $";

    private Vob.Coords fromCoords = new Vob.Coords();
    private Vob.Coords toCoords = new Vob.Coords();

    public int lineData[] = new int[3000];
    private int linePointer = 0;
    /**
     * add given coordinates + angle to the lineData[] table and
     * make the table larger if necessary, and return the pointer
     * to the added data.
     */
    private int addLineData(int x, int y, int a) {

	int r = linePointer;

	if (linePointer == lineData.length) {
	    int newLineData[] = new int[lineData.length + 3000];
	    for (int i=0; i<lineData.length; i++) newLineData[i] = lineData[i];
	    lineData = newLineData;
	}
	lineData[linePointer++] = x;
	lineData[linePointer++] = y;
	lineData[linePointer++] = a;

	return r;
    }

    public void restart() { linePointer = 0; }

    /**
     * Connect two Vobs with a line in a given angle - this only works
     * for DecoratedVobs!
     *
     * If (to == null) then add only a "stub" in the given angle =)
     */
    public void connect(VobScene into, Vob from, Vob to, int angle) {

	if (from == null) return;

	if (to == null) {
	    ((DecoratedVob)from).addLine(addLineData(0, 0, angle));
	}
	else {

	    into.getCoords(from, fromCoords);
	    into.getCoords(to, toCoords);

	    int pox = fromCoords.x + fromCoords.width/2;
	    int poy = fromCoords.y + fromCoords.height/2;

	    int tox = toCoords.x + toCoords.width/2;
	    int toy = toCoords.y + toCoords.height/2;

	    ((DecoratedVob)from).addLine(addLineData(
		((pox + tox) / 2) - pox,
		((poy + toy) / 2) - poy,
		angle));
	}
    }
}
