/*   
ZZCacheContentStore.java
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
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
package org.gzigzag;
import java.util.*;

/** A locally cached content storage, which tracks text changes.
 */
public class ZZCacheContentStore implements ContentStore,
		ContentStorer {
public static final String rcsid = "$Id: ZZCacheContentStore.java,v 1.7 2000/09/19 10:32:01 ajk Exp $";
    public static boolean dbg = false;
    final static void p(String s) { if(dbg) System.out.println(s); }
    final static void pa(String s) { System.out.println(s); }

    UndoList undo;
    public void setUndoList(UndoList l) { undo = l; }

    ZZCacheDimSpace ds;
    public void setSpace(ZZCacheDimSpace s) { ds = s; }

    /** The current contents. */
    Hashtable c = new Hashtable();

    /** The earlier connection on each cell's poswards or negwards
     * direction at commit time.
     * This list is created at commit time by reversing all operations
     * so we can then compare these values and the current values to
     * see which have changed.
     * Hmm.. do we really even need comm?
     */
    Hashtable com;

    static final Object nolla = new Object();

    synchronized public void startCommit() {
	com = new Hashtable();
    }

    synchronized public void endCommit(ContentStorer sto) {
	for(Enumeration e = com.keys(); 
	    e.hasMoreElements();) {
	    String id = (String)e.nextElement();
	    Object chgfrom = com.get(id);
	    Object chgto = c.get(id);
	    if(chgfrom == nolla) chgfrom = null;
	    if(chgfrom == chgto ||
		(chgfrom != null && chgfrom.equals(chgto)))
		    continue;
	    sto.putContent(id, chgto);
	}
	com = null;
    }

    /** The abstracted operation of connecting two cells.
     * Parameters: cell1 cell2.
     * The cells are explicitly assumed to be not connected before
     * this.
     */
    protected class ChangeContent implements UndoList.Op {
	public void undo(Object[] list, int nth) {
	    String id = (String)list[nth+1];
	    Object before = list[nth+2];
	    Object after = list[nth+3];
	    if(before == null)
		c.remove(id);
	    else
		c.put(id, before);
	    ds.invalidateText(id);
	}
	public void redo(Object[] list, int nth) {
	    String id = (String)list[nth+1];
	    Object before = list[nth+2];
	    Object after = list[nth+3];
	    c.put(id, after);
	    ds.invalidateText(id);
	}
	public void commit(Object[] list, int nth) {
	    String id = (String)list[nth+1];
	    Object before = list[nth+2];
	    Object after = list[nth+3];
	    if(before == null)
		com.put(id, nolla);
	    else
		com.put(id,before);
	}
    }

    protected ChangeContent mychange = new ChangeContent();

    public Object get(String id) { 
	return c.get(id); 
    }
    public void put(String id, Object o) { 
	undo.add(mychange, id, c.get(id), o);
	p("SetText: "+id+" '"+o+"'");
	c.put(id, o); 
    }
    public void putContent(String id, Object o) {
	c.put(id, o);
    }

}
