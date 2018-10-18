/*   
ZZConnection.java
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

package org.gzigzag;
/** A simple class defining a data type for connections in a ZZ space.
 *  Note, that nothing says a connection represented by a ZZConnection 
 *  object should exist. (You can test it with exists() method).
 *  @see org.gzigzag.ZZCell#getConns()
 */
public class ZZConnection {
	/** The start cell */
	public ZZCell c1;
	/** The dimension the connection goes along */
	public String dim;
	/** The direction of the connection */
	public int dir;
	/** The end cell */
	public ZZCell c2;
	public ZZConnection() {
	}
	/** Creates an object representing a link in ZZ space.
	 *  @param c1	The first cell
	 *  @param c2	The second cell
	 *  @param dim	The dimension of the connection
	 *  @param dir	The direction of the connection
	 */
	public ZZConnection(ZZCell c1, String dim, int dir, ZZCell c2) {
		this.c1 = c1;
		this.dim = dim;
		this.dir = dir;
		this.c2 = c2;
	}

	/** Tests whether this connection exists in it's space. */
	public boolean exists() {
		if(c1!=null && c2!=null && dim!=null && (dir==1||dir==-1)
		   && c1.s(dim, dir)==c2)
			return true;
		else
			return false;
			
	}
}

