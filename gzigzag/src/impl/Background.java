/*   
Background.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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


package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.mediaserver.Mediaserver;
import java.io.IOException;
import java.awt.*;
import java.awt.image.*;
import org.gzigzag.util.*;
import java.util.*;

/** Perform tasks in the background, in a queue.
 * E.g. loading images.
 */

public class Background {
    public static boolean dbg = false;
    final static void p(String s) { if(dbg) System.out.println(s); }
    final static void pa(String s) { System.out.println(s); }

    static public void addTask(Runnable r) {
	synchronized(queue) {
	    queue.add(r);
	    queue.notifyAll();
	}
    }

    static Vector queue = new Vector();

    static Thread bgThread = new Thread() {
	public void run() {
	    try {
	    while(true) {
		synchronized(queue) {
		    if(queue.size() == 0)
			queue.wait();
		    Runnable r = (Runnable)queue.get(0);
		    queue.remove(0);
		    p("Going to run "+r);
		    r.run();
		    p("Did run "+r);
		}
	    }
	    } catch(InterruptedException e) {
		throw new Error("Interrupted");
	    }
	}
    };

    static {
	bgThread.setDaemon(true);
	bgThread.start();
	bgThread.setPriority(Thread.MIN_PRIORITY);
    }



}
