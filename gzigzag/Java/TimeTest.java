/*   
TimeTest.java
 *    
 *    Copyright (c) 2000, Ted Nelson, Tuomas Lukka and Miika Pekkarinen
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
 * Written by Miika Pekkarinen
 * Rewritten by Tuomas Lukka
 */

package org.gzigzag;

import org.gzigzag.*;
import java.awt.*;
import java.util.*;
import java.io.*;


public class TimeTest {
    static public abstract class Test {
	public String name;
	public abstract void test(ZZCell home, int nrounds);
    }

    static Test newtest = new Test() { { name = "New"; }
	public void test(ZZCell home, int nrounds) {
	    for(int i=0; i<nrounds; i++)
		home.N("d.3", 1);
	}
    },
    instest = new Test() { { name = "Insert"; }
	public void test(ZZCell home, int nrounds) {
	    ZZCell c = home;
	    for(int i=0; i<nrounds; i++) {
		c = c.s("d.3", 1);
		home.insert("d.4", 1,c);
	    }
	}
    },
    delneightest = new Test() { { name = "Delete"; } 
	public void test(ZZCell home, int nrounds) {
	    for(int i=0; i<nrounds; i++)
		home.s("d.3", 1).delete();
	}
    },
    neigh1test = new Test() { { name = "Neigh1"; } 
	public void test(ZZCell home, int nrounds) {
	    for(int i=0; i<nrounds; i++)
		home.s("d.3", 1);
	}
    },
    neigh2test = new Test() { { name = "Neigh2"; } 
	public void test(ZZCell home, int nrounds) {
	    for(int i=0; i<nrounds; i++)
		home = home.s("d.3", 1);
	}
    },
    cont1test = new Test() { { name = "Cont1"; } 
	public void test(ZZCell home, int nrounds) {
	    ZZCell c = home.s("d.3", 1);
	    for(int i=0; i<nrounds; i++)
		c.setText("Blah");
	}
    },
    cont2test = new Test() { { name = "Cont2"; } 
	public void test(ZZCell home, int nrounds) {
	    ZZCell c = home;
	    for(int i=0; i<nrounds; i++)
		(c=c.s("d.3", 1)).setText("Blah");
	}
    };

  static void p(String s) { System.out.println(s); }
  static void pn(String s) { System.out.print(s); }

  public static void test(ZZSpace[] sp, Test[] tsts, boolean warmup ) 
	throws Exception{
    p("Testing...");

    int nrounds = 8;
    long[][][] times = new long[sp.length][nrounds][tsts.length];

    for(int iSpace = 0; iSpace < sp.length; iSpace++)
    {
      ZZCell home = sp[iSpace].getHomeCell();
      for(int i = 0; i < nrounds; i++)
      {
        // Creating cells
	for(int tst = 0; tst < tsts.length; tst++) {
	    long t0 = System.currentTimeMillis();
	    tsts[tst].test(home, (warmup ? 1 : 1000));
	    times[iSpace][i][tst] = System.currentTimeMillis() - t0;
	}
      }
      p(""+sp[iSpace]);
      for(int tst=0; tst<tsts.length; tst++) {
	  pn("  "+tsts[tst].name+"\n\t");
	  long tot = 0;
	  for(int i=0; i<nrounds; i++) {
	    tot += times[iSpace][i][tst];
	    pn(times[iSpace][i][tst]+"\t");
	  }
	  p("==\t"+(tot / nrounds));
      }
    }
  }

  public static void main(String[] argv) {
      try {
	File f1 = new File("/tmp/zztimetst"+Math.random());
	File f2 = new File("/tmp/zztimetst"+Math.random());
	ZZSpace spaces [] = new ZZSpace[] {
	    new ZZCacheDimSpace(new DirStreamSet(f1)),
	};
	for(int i=0; i<spaces.length; i++)
	    ZZDefaultSpace.create(spaces[i].getHomeCell());

	  Test[] tsts = new Test[] {
	    newtest,
	    instest,
	    neigh1test,
	    neigh2test,
	    cont1test,
	    cont2test,
	    delneightest
	  };
	  test(spaces, tsts, true);
	  test(spaces, tsts, true);
	  test(spaces, tsts, true);
	  test(spaces, tsts, false);
      } catch(Exception e)
	{
	  e.printStackTrace();
	  System.out.println(e);
	}
  System.exit(0);
  }
}



