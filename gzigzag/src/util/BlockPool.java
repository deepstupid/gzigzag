/*   
BlockPool.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein, Tuomas Lukka
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
 * Written by Benja Fallenstein and Tuomas Lukka
 */
package org.gzigzag.util;

/** A pool for blocks of throw-away objects.
 * A different implementation of pooling, for experimentation.
 * This class makes the pool logic simpler by not making it possible
 * to release a single object at a time.
 * <p>
 * Therefore, we get rid of the boolean array used and the storage
 * of an integer index per poolable object.
 * <p>
 * The use of this class requires some more care from the higher-level
 * programmer.
 */

public abstract class BlockPool {
public static final String rcsid = "$Id: BlockPool.java,v 1.1 2001/09/03 05:52:21 tjl Exp $";

    static private void pa(String s) { System.out.println(s); }

    /** The initial size of the pool array.
     *  Note: no new objects are created until specifically requested.
     *  Initially, all the pointers in the pool array 
     *  are to <code>null</code>.
     */
    public static final int INITIAL_LENGTH = 20;


    /** The pool of <code>Poolable</code>s.
     *  Valid <i>at least</i> up to <code>n-1</code>. When looking
     *  beyond <code>endIndex-1</code>, one must test for <code>null</code>
     *  entries and possibly create new objects through 
     *  <code>create()</code>.
     */
    protected Object[] pool = new Object[INITIAL_LENGTH];

    /** The number of used entries.
     */
    protected int nPooled = 0;

    /** Create a new object of the type cached by this <code>Pool</code>.
     *  This method is called by the <code>getObject()</code> method
     *  when there is no cached, currently unused instance.
     *  It does not have to worry about putting the object into
     *  the <code>cache</code> array or about calling <code>setPool</code>
     *  in the <code>Poolable</code>: the <code>getObject()</code> method
     *  will take care of that.
     */
    abstract protected Object create();


    /** Get a <code>Placeable</code> object not yet casted to the class
     *  this <code>Pool</code> caches. If there is an unused object instance
     *  available, it is returned (and claimed as used); otherwise, a new 
     *  instance is created and put into the cache.
     */
     public final Object getObject() {
	int i = nPooled;
	//pa("From endIndex: "+i);
	nPooled++;
	if(pool.length == i) ensureLength(i+1);
	
	Object result = pool[i];
	if(result == null) {
	    //pa("...create().");
	    result = create();
	    pool[i] = result;
	}
	return result;
    }

    /** Get the current pool size.
     */
    public int getSize() { return nPooled; }

    /** Set the current pool size.
     * The entries created after getSize() returned size are considered
     * discarded.
     */
    public void setSize(int size) { nPooled = size; }


    //                 ------ Internal stuff ------
    
    /** Ensure that the <code>cache</code> and <code>used</code> arrays
     *  have at least length <code>len</code>. If the length of the arrays
     *  is less than that, longer arrays will be created and the contents
     *  of the old arrays will be copied into the new ones.
     */
    private final void ensureLength(int len) {
	if(pool.length < len) {
	    int nlength = pool.length * 2;
	
	    Object[] npool = new Object[nlength];
	    System.arraycopy(pool, 0, npool, 0, pool.length);
	    pool = npool;

	}
    }
}
