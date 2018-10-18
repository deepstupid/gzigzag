/*   
Pool.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
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
 * Written by Benja Fallenstein
 */
package org.gzigzag.util;

/** A pool for throw-away objects.
 *  This is an experiment for solving our problem with object creation
 *  cost (esp. on Kaffe). It caches throw-away objects like vobs, and allows
 *  them to be explicitly released-- but still keeps a reference to them,
 *  so that they won't be gc'ed. The next time an object of that type is
 *  needed, the released object will be used.
 *  <p>
 *  This means that every object of that type (a <code>Pool</code> instance
 *  is type-specific) needs to be created through the pool, so that an
 *  old, released instance can be used if there is one.
 *  <p>
 *  Note that <code>Pool</code> needs to be subclassed, and the abstract
 *  <code>create()</code> method has to be implemented. This method creates
 *  a new instance of the class the <code>Pool</code> caches.
 *  Also, by convention a <code>get(</code>...<code>)</code> should be
 *  implemented that takes the same parameters as the constructor of the
 *  served class; for example, a <code>FooPool</code> class that caches
 *  instances of class <code>Foo</code>, which has the constructors
 *  <code>Foo()</code> and <code>Foo(int i)</code>, should define the
 *  methods <code>Foo FooPool.get()</code> and 
 *  <code>Foo FooPool.get(int i)</code>.
 *  <p>
 *  The <code>get(</code>...<code>)</code> method should call
 *  <code>getObject()</code>, defined in <code>Pool</code>, cast the
 *  result to the correct class (in our example, <code>Foo</code>),
 *  and do whatever the corresponding constructor would do.
 *  <p>
 *  <strong>Caution:</strong> This code makes the assumption that all
 *  object instances requested from it will be released relatively soon.
 *  If references are held for a longer time, those object instances must be
 *  created normally; otherwise, <code>Pool</code> might trample a lot
 *  of memory.
 *  <p>
 *  <code>XXX</code> Currently, the object instances are kept forever. At
 *  some point, we will probably want to have something like a
 *  <code>pack()</code> method that throws away all currently unused,
 *  cached object instances, because we will e.g. have more exotic Vob
 *  types which we use a lot for some views, but not at all most
 *  of the time.
 */

public abstract class Pool {
public static final String rcsid = "$Id: Pool.java,v 1.2 2001/08/26 00:29:11 bfallenstein Exp $";

    static private void pa(String s) { System.out.println(s); }

    /** The initial size of the cache array.
     *  Note: no new objects are created until specifically requested.
     *  Initially, all the pointers in the cache array 
     *  are to <code>null</code>.
     */
    public static final int INITIAL_LENGTH = 20;


    public interface Poolable {
	/** Store the information necessary for releasing this poolable.
	 *  <code>Poolable.release()</code> needs two informations to
	 *  work correctly: the <code>Pool</code> this <code>Poolable</code>
	 *  is stored in, and the index of this <code>Poolable</code>
	 *  inside that <code>Pool</code>.
	 */
	void setPool(Pool pool, int poolIndex);
	
	/** Release this <code>Poolable</code> instance.
	 *  Makes this instance available for future use. This is never
	 *  called by <code>Pool</code>; rather, it exists here as a
	 *  convention for naming this functionality.
	 *  <p>
	 *  This <strong>must not do anything</strong> if
	 *  <code>setPool</code> has never been called.
	 *  Otherwise, must call <code>pool.release(poolIndex)</code>, where
	 *  <code>pool</code> and <code>poolIndex</code> are the parameters
	 *  passed to the last <code>setPool</code> call.
	 */
	void release();
    }

    /** An abstract implementation of <code>Poolable</code>.
     *  This should be subclassed instead of implementing 
     *  <code>Poolable</code> whereever possible. The only reason for
     *  implementing <code>Poolable</code> instead is subclassing a
     *  non-<code>Poolable</code> class and intending to create a
     *  <code>Pool</code> for that class. (When implementing Poolable
     *  directly, it will normally suffice to copy&paste the whole body
     *  of <code>AbstractPoolable</code> into the implementing class.)
     */
    public static class AbstractPoolable implements Poolable {
	private Pool myPool = null;
	private int myPoolIndex;
	
	public final void setPool(Pool pool, int poolIndex) {
	    myPool = pool;
	    myPoolIndex = poolIndex;
	}
	
	public final void release() {
	    if(myPool != null)
		myPool.release(myPoolIndex);
	}
    }


    /** The cache of <code>Poolable</code>s.
     *  Valid <i>at least</i> up to <code>endIndex-1</code>. When looking
     *  beyond <code>endIndex-1</code>, one must test for <code>null</code>
     *  entries and possibly create new objects through 
     *  <code>create()</code>.
     */
    protected Poolable[] cache = new Poolable[INITIAL_LENGTH];

    /** An array of the same length as <code>cache</code>, determining for
     *  every element of <code>cache</code> whether it is currently used.
     *  Unused elements can be claimed inside the <code>getObject()</code>
     *  method. (Note, though, that it may be <code>null</code>, in which
     *  case it will be initialized though the
     *  <code>create()</code> method.
     *  @see #cache, #getObject
     */
    protected boolean[] used = new boolean[INITIAL_LENGTH];

    /** The index of the first used object in the <code>cache</code> array.
     *  <code>startIndex</code> must be <code>&lt;= endIndex</code>.
     *  <p>
     *  Note: there won't be any used objects with an index 
     *  <code>&lt; startIndex</code> or <code>&gt;= endIndex</code>, but
     *  there may be unused objects <code>&gt;= startIndex</code> and
     *  <code>&lt; endIndex</code>, because objects can be released in a
     *  different order than they were requested in the first place.
     */
    protected int startIndex = 0;

    /** The index of the last used object in the <code>cache</code> array,
     *  plus one.
     */
    protected int endIndex = 0;

    /** Create a new object of the type cached by this <code>Pool</code>.
     *  This method is called by the <code>getObject()</code> method
     *  when there is no cached, currently unused instance.
     *  It does not have to worry about putting the object into
     *  the <code>cache</code> array or about calling <code>setPool</code>
     *  in the <code>Poolable</code>: the <code>getObject()</code> method
     *  will take care of that.
     */
    abstract protected Poolable create();



    /** Get a <code>Placeable</code> object not yet casted to the class
     *  this <code>Pool</code> caches. If there is an unused object instance
     *  available, it is returned (and claimed as used); otherwise, a new 
     *  instance is created and put into the cache.
     */
    protected final Poolable getObject() {
	//pa("getObject() ...");
	if(startIndex > 0) {
	    startIndex--;
	    used[startIndex] = true;
	    //pa("From startIndex: "+startIndex);
	    return cache[startIndex];
	} else {
	    int i = endIndex;
	    //pa("From endIndex: "+i);
	    endIndex++;
	    ensureLength(endIndex);
	    
	    used[i] = true;
	    Poolable result = cache[i];
	    if(result == null) {
		//pa("...create().");
		result = create();
		result.setPool(this, i);
		cache[i] = result;
	    }
	    return result;
	}
    }

    /** Release the object instance at the given index in the pool.
     *  This is normally called by the <code>Poolable.release()</code>
     *  convenience function.
     */
    public final void release(int index) {
	//pa("Release: "+index);
	if(index < startIndex || index >= endIndex)
	    throw new IndexOutOfBoundsException("startIndex: "+startIndex+
						", index: "+index+
						", endIndex: "+endIndex);
	used[index] = false;
	if(endIndex == index + 1) {
	    //pa("Move endIndex back to " + index);
	    for(endIndex = index; endIndex >= startIndex; endIndex--) {
		//pa("Move endIndex further back to " + endIndex);
		if(endIndex == startIndex || used[endIndex - 1]) break;
	    }
	} else if(startIndex == index) {
	    //pa("Move startIndex forward to " + (index+1));
	    for(startIndex = index+1; startIndex < endIndex; startIndex++) {
		//pa("Move startIndex further forward to " + startIndex);
		if(used[startIndex]) break;
	    }
	}
    }



    //                 ------ Internal stuff ------
    
    /** Ensure that the <code>cache</code> and <code>used</code> arrays
     *  have at least length <code>len</code>. If the length of the arrays
     *  is less than that, longer arrays will be created and the contents
     *  of the old arrays will be copied into the new ones.
     */
    private final void ensureLength(int len) {
	if(cache.length < len) {
	    int nlength = cache.length * 2;
	
	    Poolable[] ncache = new Poolable[nlength];
	    System.arraycopy(cache, 0, ncache, 0, cache.length);
	    cache = ncache;

	    boolean[] nused = new boolean[nlength];
	    System.arraycopy(used, 0, nused, 0, used.length);
	    used = nused;
	}
    }
}
