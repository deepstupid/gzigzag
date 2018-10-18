Title: A plain VStreamDim implementation
Rcs-Id: $Id: PlainVStreamDim.ly,v 1.10 2002/03/27 07:24:17 bfallenstein Exp $

<h1>A simple implementation of VStreamDim</h1>

<i>Written by Benja Fallenstein</i>

This is a _plain_ implementation of |VStreamDim|, avoiding the
complications of both the |BlockedVStreamDim| and the |LinkedVStreamDim|
implementations.

Like the simplest dimension implementations, we use |Map|s for storing the
posward and negward connections.

-- PlainVStreamDim fields:
	private Map cn, cp;

However, normally if a cell is not in one of the maps, that means the cell
has no connection in that direction. With ordinary cells, that's true for
|PlainVStreamDim|, too. However, for VStream cells it means being
connected to the next cell in the range; if a VStream cell is not connected,
we use a special object to represent that.

	static public final Object UNCONNECTED = new Object();

The only problems with this scheme are that iterating through a range
and getting headcells
become inefficient. Now these _are_ common operations, but we will
put in a cache that can handle them efficiently, later.


<h2>Encapsulation</h2>

The above is a nice scheme if we do not have to think about it.
If we have to deal with the different meanings of |null| each time
we deal with one of our |Map|s, we will go insane.

Therefore, we'll use two helper functions that'll deal with that
complexity for us, and then mostly forget about it. |:)|

|get()| gets a connection from one of the maps.

-- PlainVStreamDim methods:
	protected final Cell get(Cell key, int dir) {
		-- Check key is not null.
		Map map = (dir>0) ? cp : cn;

(We'll write these only once, and have them work for both directions.
|get()| operates on one of the two |Map|s, depending on its |dir| argument.)

		Object o = map.get(key);
		
We go through the different cases. If |o| is |null|, we have to return
the next cell in the VStream, if this is a cell in a VStream range.

		if(o == null) {
			if(!isVStreamCell(key))
				return null;
			else
				return getNextInRange(key, dir);
		}

|getNextInRange()| returns |null| if |key| is not a VStream cell, or if it is
at the end of a transcluded range.

If |o| is |UNCONNECTED|, this is a VStream cell with no connection in
that direction.

		else if(o == UNCONNECTED)
			return null;

Otherwise, this is a |Cell| which we return.

		else
			return (Cell)o;
	}

|put()| puts something into one of the |Map|s. Note that |put()| deals
only with one of the |Map|s, just like |get()|.

|key| may not be |null|, |value| may be.

	protected void put(Cell key, int dir, Cell value) {
		-- Check key is not null.

		Map map = (dir>0) ? cp : cn;

First case: |key| is not a vstream cell.

		if(!isVStreamCell(key))
			map.put(key, value);

Second case: |key| is a vstream cell, and |value| is |null| (this is
a disconnect).

		else if(value == null)
			map.put(key, UNCONNECTED);

Third case: |key| is a vstream cell, and |value| is the next cell in the
vstream range.

		else if(isNextInRange(key, dir, value))
			map.put(key, null);

Fourth case: |key| is a vstream cell, and |value| is a cell, but not the
next in the range.

		else
			map.put(key, value);
	}



<h2>Basic dimension functionality</h2>

We implement the abstract methods of |AbstractDim| in a straight-forward way.

-- PlainVStreamDim methods:
	public Cell s(Cell c, int steps, Obs o) {
		int dir = (steps>0) ? 1 : -1;
		int count = dir*steps;
		
		for(int i=0; i<count; i++) {
			if(c == null) return null;
			c = get(c, dir);
		}
		
		return c;
	}

	public void connect(Cell c, Cell d) {
		-- Check whether c or d is already connected.
		-- Check whether connection would cause a loop.

		//put(c, 1, d);
		//put(d, -1, c);

		//if(c.id.startsWith("0000000008000000E830E3BF2C0004A58091D33E4C146B3E7EF6910C9B2FAD6B009134A5E459E4-1;")) System.out.println("Conn: "+c+" "+d);

		((SimpleSpanSpace)space).invalidateCache(h(c));
		((SimpleSpanSpace)space).invalidateCache(d);

		-- Update cache after connect().

		put(c, 1, d);
		put(d, -1, c);
	}

	public void disconnect(Cell c, int dir) {
		((SimpleSpanSpace)space).invalidateCache(h(c));

		Cell d = get(c, dir);

		//if(c.id.startsWith("0000000008000000E830E3BF2C0004A58091D33E4C146B3E7EF6910C9B2FAD6B009134A5E459E4-1;")) System.out.println("Disc: "+c+" "+d);

		if(d != null) {
			put(c, dir, null);
			put(d, -dir, null);
		}

		-- Update cache after disconnect().
	}

	public void addRealNegSides(Set set) {
		for(Iterator i=cp.entrySet().iterator(); i.hasNext();) {
			Map.Entry e = (Map.Entry)i.next();

			if(e.getValue() == UNCONNECTED)
				continue;

			Cell c = (Cell)e.getKey();
			set.add(c.id);
		}
	}

We do not support circular ranks, so |isCircularHead()| is always false.

	public boolean isCircularHead(Cell c, Obs o) {
		return false;
	}

Checks... (*XXX* put somewhere else)

-- Check whether c or d is already connected:
	if(s(c) != null || s(d, -1) != null)
		throw new ZZAlreadyConnectedException("",c,s(c),s(d, -1),d);

-- Check whether connection would cause a loop:
	if(h(d, 1).equals(c))
		throw new ZZError("d.vstream connection would create loop: "+
				  c+"; "+d);

-- Check key is not null:
	if(key == null)
		throw new NullPointerException("key");




<h2>Span transclusion notification (XXX rename)</h2>

-- PlainVStreamDim fields:
	private Map cache = new HashMap();

-- PlainVStreamDim methods:
	public void notifyTransclusion(Cell first, int length) {
		SpanSpacepart spanpart = (SpanSpacepart)first.spacepart;

		Object ref = first.inclusionObject;
		int i = first.inclusionIndex;

		Cell last = spanpart.getCell(ref, i + length - 1);

		Cell before = spanpart.getCell(ref, i - 1);
		Cell after = spanpart.getCell(ref, i + length);
		
		cn.put(first, UNCONNECTED);
		cp.put(last, UNCONNECTED);

		Tree t = (Tree)cache.get(first.inclusionObject);
		if(t == null)
			cache.put(first.inclusionObject, t = new Tree());

		t.insert(before, first);
		t.insert(last, after);
	}


<h2>|getNextInRange| and so on</h2>

-- PlainVStreamDim methods:
	final protected boolean isVStreamCell(Cell c) {
		try {
			return c.spacepart instanceof SpanSpacepart;
		} catch(RuntimeException e) {
			throw new Error("exc "+e+" cell "+
					(c!=null ? c.id : "null"));
		}
	}

	final protected boolean isNextInRange(Cell c, int dir, Cell d) {
		if(!isVStreamCell(c) || !isVStreamCell(d))
			return false;

		if(c.inclusionObject != d.inclusionObject)
			return false;
		
		if(Math.abs(dir) != 1) throw new Error("abs(dir) != 1");

		return c.inclusionIndex + dir == d.inclusionIndex;
	}

	final protected Cell getNextInRange(Cell c, int dir) {
		if(!isVStreamCell(c))
			throw new IllegalArgumentException("no vs cell:"+c.id);

		return c.spacepart.getCell(c.inclusionObject,
					   c.inclusionIndex + dir);
	}



<h2>Caching</h2>

<h3>Cache access</h3>

As mentioned before, iterating through vstreams and getting endcells is
horribly inefficient with the scheme above. To help this, we will
introduce a cache that helps us to get quickly from one end of
a range of vstream cells to the other end.

We'll encapsulate accesses to this cache in the |jump()| method.
Given a cell and a direction, it will see whether we are
in a range of vstream cells; if so, it'll return the range's end.
If not, it'll return what it got.

Now, considering that we do not have looping ranks, we can implement |h()| 
very easily, alternating between |jump()| and |s()|.

-- PlainVStreamDim methods:
	public Cell h(Cell c, int dir, Obs o) {
		if(c == null) return null;
		while(true) {

First we try to jump. If |jump()| returns something else than |null|,
we go where it points us.

			c = getRangeEnd(c, dir);

Then, we use |s()| to see what's next on the rank. If there's something,
we go there; if there's nothing, we've reached the endcell, so we return it.

			Cell next = s(c, dir);
			if(next == null) return c;
			c = next;
		}
	}



<h3>Cache updating</h3>

-- Update cache after disconnect():
	if(d != null && isNextInRange(c, dir, d)) {
		Tree t = (Tree)cache.get(c.inclusionObject);

		if(dir > 0)
			t.insert(c, d);
		else
			t.insert(d, c);
	}

-- Update cache after connect():
	if(isNextInRange(c, 1, d)) {
		Tree t = (Tree)cache.get(c.inclusionObject);

		t.delete(c, d);
	}


<h2>Iteration</h2>

*XXX*

-- PlainVStreamDim methods:
	public Cell getRangeEnd(Cell c, int dir) {
		if(!isVStreamCell(c))
			return c;

Getting to the end of a range with |cp| and |cn| lookups is _slow_ if
the range is thousands of characters long (there's one hashtable
lookup per character). Thus, we need to optimize this.

		Tree t = (Tree)cache.get(c.inclusionObject);
		if(dir > 0)
			return t.next(c);
		else
			return t.prev(c);
	}




-- PlainVStreamDim methods:	
	public void iterate(org.gzigzag.vob.CharRangeIter i,
			    Cell stream, Map extra) {
		Cell c = s(stream);

		while(c != null) {
			if(!isVStreamCell(c))
				i.object(c);

			else {
				Cell d = getRangeEnd(c, 1);
				
				SpanSpacepart.Ref r =
					(SpanSpacepart.Ref)c.inclusionObject;

				i.range(r.block, r.block.getCharArray(),
					c.inclusionIndex, d.inclusionIndex);

				c = d;
			}

			c = s(c);
		}

		i.object(i.END_OF_STREAM);
	}


<h2>Appendix: Java bureaucracy</h2>

-- file "PlainVStreamDim.java":
	/** THIS FILE WAS GENERATED AUTOMATICALLY FROM PlainVStreamDim.ly.
	 *  DO NOT EDIT THIS FILE!
	 */

	package org.gzigzag.impl;
	import java.util.*;
	import org.gzigzag.*;
	import org.gzigzag.vob.CharRangeIter;
	import org.gzigzag.util.*;

	
	public class PlainVStreamDim extends AbstractVStreamDim 
				     implements CopyableDim {

		public PlainVStreamDim(Space s) {
			super(s);
			cp = new HashMap(); cn = new HashMap();
		}

		public PlainVStreamDim(Space s, PlainVStreamDim o) {
			super(s);
			cp = new HashMap(o.cp); 
			cn = new HashMap(o.cn);
		}

		public Dim makeCopy(Space s, ObsTrigger o) {
			return new PlainVStreamDim(s, this);
		}

		// FIELDS

		-- PlainVStreamDim fields.

		// METHODS

		-- PlainVStreamDim methods.

		// INNER CLASSES

		-- PlainVStreamDim inner classes.
	}


<h2>Legal matters</h2>

Copyright (c) 2002 by Benja Fallenstein

XXX license statement (LGPL+XPL as usual)