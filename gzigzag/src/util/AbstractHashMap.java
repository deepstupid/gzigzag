/*
AbstractHashMap.java
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
import java.util.*;

/** An abstract implementation of <code>Map</code>.
 *  In contrast to <code>java.util.AbstractMap</code>, subclasses
 *  of <code>AbstractHashMap</code> only need to implement
 *  <code>get()</code>, <code>put()</code>, <code>size()</code>,
 *  and <code>keySet()</code>.
 *  <p>
 *  <code>put(key, null)</code> must remove any mapping for
 *  <code>key</code>. The <code>Map</code> <b>must not</b> support
 *  <code>null</code> values.
 */

public abstract class AbstractHashMap implements Map {
		
    public boolean isEmpty() {
	return size() == 0;
    }

    public boolean containsKey(Object key) {
	return get(key) != null;
    }

    public boolean containsValue(Object value) {
	for(Iterator i = keySet().iterator(); i.hasNext();) {
	    Object v = get(i.next());
	    if(value == null) {
		if(v == null) return true;
	    } else {
	        if(value.equals(v)) return true;
	    }
	}
	return false;
    }

    public Object remove(Object key) {
	return put(key, null);
    }

    public void putAll(Map t) {
	for(Iterator i = t.keySet().iterator(); i.hasNext(); ) {
	    Object key = i.next(), value = t.get(key);
	    put(key, value);
	}
    }

    public void clear() {
	for(Iterator i = keySet().iterator(); i.hasNext(); ) {
	    put(i.next(), null);
	}
    }

    public Collection values() {
	final Set keys = keySet();
	return new AbstractCollection() {
            public int size() {
                return AbstractHashMap.this.size();
            }
	    public Iterator iterator() {
		final Iterator iter = keys.iterator();
		return new Iterator() {
		    public boolean hasNext() {
			return iter.hasNext();
		    }
		    public Object next() {
			return get(iter.next());
		    }
		    public void remove() {
			iter.remove();
		    }
		};
	    }
	};
    }

    public Set entrySet() {
	final Set keys = keySet();
	return new AbstractSet() {
	    public int size() {
		return AbstractHashMap.this.size();
	    }
	    public Iterator iterator() {
		final Iterator iter = keys.iterator();
		return new Iterator() {
		    public boolean hasNext() {
			return iter.hasNext();
		    }
		    public Object next() {
			final Object key = iter.next(), 
				     value = get(key);
			return new Map.Entry() {
			    public Object getKey() { return key; }
			    public Object getValue() {
				return value;
			    }
			    public boolean equals(Object other) {
				if(!(other instanceof Map.Entry))
				    return false;
				Map.Entry e = (Map.Entry)other;
				return key.equals(e.getKey()) &&
				       value.equals(e.getValue());
			    }
			    public int hashCode() {
				int k = 0, v = 0;
				if(key != null) k = key.hashCode();
				if(value != null)
				    v = value.hashCode();
				return k ^ v;
			    }
			    public Object setValue(Object nval) {
				// XXX any problems with coexisting
				//     Map.Entrys for the same key
				//     here? Some scheme for
				//     throwing
				//     ConcurrentModificationExc ?
				
				// XXX JDK 1.4 doesn't accept this!
				// value = nval;

				return put(key, nval);
			    }
			};
		    }
		    public void remove() {
			iter.remove();
		    }
		};
	    }
	};
    }
    
    /** Very slow! */
    public boolean equals(Object o) {
	if(o instanceof Map)
	    return entrySet().equals(((Map)o).entrySet());
	else
	    return false;
    }

    /** Very slow! */
    public int hashCode() {
	return entrySet().hashCode();
    }
}
