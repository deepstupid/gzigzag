/*   
BenchPool.java
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
import org.gzigzag.benchmark.*;

/** Benchmark creating objects normally vs. requesting them
 *  from a <code>Pool</code>.
 */

public class BenchPool {
public static final String rcsid = "$Id: BenchPool.java,v 1.3 2001/09/03 05:52:21 tjl Exp $";
    static private void pa(String s) { System.out.println(s); }


    static final int NOBJS = 500;


    static class Dummy extends Pool.AbstractPoolable {
	int x, y, z;
	Object a, b, c;
	
	private Dummy() {}
	
	Dummy(int X, int Y, int Z, Object A, Object B, Object C) {
	    x = X; y = Y; z = Z; a = A; b = B; c = C;
	}
    }

    static class DummyPool extends Pool {
	public Poolable create() {
	    return new Dummy();
	}
	
	final Dummy get(int X, int Y, int Z, Object A, Object B, Object C) {
	    Dummy d = (Dummy)getObject();
	    d.x = X; d.y = Y; d.z = Z; d.a = A; d.b = B; d.c = C;
	    return d;
	}
    }

    static DummyPool dummyPool = new DummyPool();
    
// Block
    static class BlockDummy {
	int x, y, z;
	Object a, b, c;
	
	private BlockDummy() {}
	
	BlockDummy(int X, int Y, int Z, Object A, Object B, Object C) {
	    x = X; y = Y; z = Z; a = A; b = B; c = C;
	}
    }

    static class BlockDummyPool extends BlockPool {
	public Object create() {
	    return new BlockDummy();
	}
	
	final BlockDummy get(int X, int Y, int Z, Object A, Object B, Object C) {
	    BlockDummy d = (BlockDummy)getObject();
	    d.x = X; d.y = Y; d.z = Z; d.a = A; d.b = B; d.c = C;
	    return d;
	}
    }

    static BlockDummyPool blockDummyPool = new BlockDummyPool();


// Std
    static class Empty extends Pool.AbstractPoolable {}

    static class EmptyPool extends Pool {
	public Poolable create() {
	    return new Empty();
	}
	
	Empty get() {
	    return (Empty)getObject();
	}
    }

    static EmptyPool emptyPool = new EmptyPool();


// Block
    static class BlockEmpty {}

    static class BlockEmptyPool extends BlockPool {
	public Object create() {
	    return new BlockEmpty();
	}
	
	BlockEmpty get() {
	    return (BlockEmpty)getObject();
	}
    }

    static BlockEmptyPool blockEmptyPool = new BlockEmptyPool();



    

    public static void main(String[] argv) {
	Bench b = new Bench(argv);
	b.run(new Object[] {
	    "CREATE", "Create "+NOBJS+" new objects with 6 members "+
		      "normally",
		new Runnable() {
		    Dummy[] ds = new Dummy[NOBJS];
		    Object o = new Object();
		    
		    public void run() {
			for(int i=0; i<NOBJS; i++) {
			    ds[i] = new Dummy(0, 0, 0, o, o, o);
			}
			for(int i=0; i<NOBJS; i++) {
			    ds[i] = null;
			}
		    }		
		},
	    "REQUEST", "Request/release "+NOBJS+" objects with 6 members "+
			"from pool",
		new Runnable() {
		    Dummy[] ds = new Dummy[NOBJS];
		    Object o = new Object();
		    
		    public void run() {
			for(int i=0; i<NOBJS; i++) {
			    ds[i] = dummyPool.get(0, 0, 0, o, o, o);
			}
			for(int i=0; i<NOBJS; i++) {
			    ds[i].release();
			    ds[i] = null;
			}
		    }		
		},
	    "BLOCKREQUEST", "Request/release "+NOBJS+" objects with 6 members "+
			"from blockpool",
		new Runnable() {
		    BlockDummy[] ds = new BlockDummy[NOBJS];
		    Object o = new Object();
		    
		    public void run() {
			int s = blockDummyPool.getSize();
			for(int i=0; i<NOBJS; i++) {
			    ds[i] = blockDummyPool.get(0, 0, 0, o, o, o);
			}
			blockDummyPool.setSize(s);
			for(int i=0; i<NOBJS; i++) {
			    ds[i] = null;
			}
		    }		
		},


	    "CREATEEMPTY", 
	    "Create "+NOBJS+" new, empty objects normally",
		new Runnable() {
		    Empty[] es = new Empty[NOBJS];
		    
		    public void run() {
			for(int i=0; i<NOBJS; i++) {
			    es[i] = new Empty();
			}
			for(int i=0; i<NOBJS; i++) {
			    es[i] = null;
			}
		    }		
		},
	    "REQUESTEMPTY", 
	    "Request/release "+NOBJS+" empty objects from pool",
		new Runnable() {
		    Empty[] es = new Empty[NOBJS];
		    
		    public void run() {
			for(int i=0; i<NOBJS; i++) {
			    es[i] = emptyPool.get();
			}
			for(int i=0; i<NOBJS; i++) {
			    es[i].release();
			    es[i] = null;
			}
		    }		
		},
	    "BLOCKREQUESTEMPTY", 
	    "Request/release "+NOBJS+" empty objects from blockpool",
		new Runnable() {
		    BlockEmpty[] es = new BlockEmpty[NOBJS];
		    
		    public void run() {
			int s = blockEmptyPool.getSize();
			for(int i=0; i<NOBJS; i++) {
			    es[i] = blockEmptyPool.get();
			}
			blockEmptyPool.setSize(s);
			for(int i=0; i<NOBJS; i++) {
			    es[i] = null;
			}
		    }		
		},


	    "CREATENONULL", 
	    "Create "+NOBJS+" new, empty objects, not nulling them",
		new Runnable() {
		    Empty[] es = new Empty[NOBJS];
		    
		    public void run() {
			for(int i=0; i<NOBJS; i++) {
			    es[i] = new Empty();
			}
		    }		
		},
	    "REQUESTNONULL", 
	    "Request/release "+NOBJS+" empty objects from pool, not nulling them",
		new Runnable() {
		    Empty[] es = new Empty[NOBJS];
		    
		    public void run() {
			for(int i=0; i<NOBJS; i++) {
			    es[i] = emptyPool.get();
			}
			for(int i=0; i<NOBJS; i++) {
			    es[i].release();
			}
		    }		
		},
	    "BLOCKREQUESTNONULL", 
	    "Request/release "+NOBJS+" empty objects from blockpool",
		new Runnable() {
		    BlockEmpty[] es = new BlockEmpty[NOBJS];
		    
		    public void run() {
			int s = blockEmptyPool.getSize();
			for(int i=0; i<NOBJS; i++) {
			    es[i] = blockEmptyPool.get();
			}
			blockEmptyPool.setSize(s);
		    }		
		},

	    "CREATESINGLE", "Create a single, empty object normally",
		new Runnable() {
		    Empty e;
		    public void run() {
			e = new Empty();
		    }		
		},
	    "REQUESTSINGLE", 
	    "Request/release a single, empty object from pool",
		new Runnable() {
		    Empty e;
		    public void run() {
			e = emptyPool.get();
			e.release();
		    }		
		},		
	    "BLOCKREQUESTSINGLE", 
	    "Request/release a single, empty object from pool",
		new Runnable() {
		    BlockEmpty e;
		    public void run() {
			int s = blockEmptyPool.getSize();
			e = blockEmptyPool.get();
			blockEmptyPool.setSize(s);
		    }		
		},		



	    "CREATEINMETHOD", "Create object normally, never store outside method",
		new Runnable() {
		    public void run() {
			Empty e = new Empty();
		    }		
		},
	    "REQUESTINMETHOD", 
	    "Request/release object from pool, never store outside method",
		new Runnable() {
		    public void run() {
			Empty e = emptyPool.get();
			e.release();
		    }		
		},
		
	    "CREATEMANYINMETHOD", "Create "+NOBJS+" objects normally, never store outside method",
		new Runnable() {
		    public void run() {
			for(int i=0; i<NOBJS; i++) {
			    Empty e = new Empty();
			}
		    }		
		},
	    "REQUESTMANYINMETHOD", 
	    "Request/release "+NOBJS+" objects from pool, never store outside method",
		new Runnable() {
		    public void run() {
			for(int i=0; i<NOBJS; i++) {
			    Empty e = emptyPool.get();
			    e.release();
			}
		    }		
		},			
	});
    }
}
