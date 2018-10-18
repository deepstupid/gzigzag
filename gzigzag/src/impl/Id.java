/*
Id.java
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
import java.util.*;

/** WHY IS THIS NOT DOCUMENTED?!?!?!! --TJL.
 */

public class Id {
    public static final Space space =
	new WrapperSpace("home-id", new FullSpace());
    public static final Space blocks =
	new WrapperSpace("home-block", new FullSpace());

    private static HashMap cache = new HashMap();
    private static HashMap getCacheMap(Space s) { 
	HashMap res = (HashMap)cache.get(s);
	if(res != null) return res;
	res = new HashMap();
	cache.put(s, res);
	return res;
    }

    public static void cleanCache(Space s) {
	cache.remove(s);
    }

    public static Cell getBlock(String id, Space s) {
	// return new Cell(s, blocks.getCell("home-block:"+id).id);
	return s.getCell(blocks.getCell("home-block:"+id).id);
    }

    public static String stripHome(String id) {
	if (id.startsWith("home-id")) return id.substring(8);
	throw new ZZError("Id.stripHome called with id: "+id);
    }
    public static String stripBlock(String id) {
	if (id.startsWith("home-block")) return id.substring(11);
	throw new ZZError("Id.stripBlock called with id: "+id);
    }

    public static Cell get(Cell c) { return get(c, c.space); }
    private static Cell getImpl(Cell c, Space s) {
	HashMap cacheMap = getCacheMap(s);
	Cell res = (Cell)cacheMap.get(c);
	if(res != null) return res;

	String id = c.id;
	if(id.lastIndexOf('$') >= 0)
	    throw new IllegalArgumentException("VStream cells have no "+
					       "identities: "+id);
	int idx = id.lastIndexOf(":");
	int idy = id.lastIndexOf(";");
	if(idy > idx) idx = idy;
	if (idx != -1) id = id.substring(idx+1);
	res = s.getCell("home-id:"+id);
	cacheMap.put(c, res);
	return res;
    }

    public static Cell get(Cell c, Space s) {
	if (!getImpl(c, s).id.equals(Dims.d_clone_id.id))
	    c = c.getRootclone();
	return getImpl(c, s);
    }
    public static boolean equals(Cell c1, Cell c2) {
	c1 = get(c1); c2 = get(c2);
	// The strings are interned -- == ok.
	return c1.id == c2.id;
    }
}
