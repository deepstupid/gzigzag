/*   
SafeExit.java
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
 * Written by Antti-Juhani Kaijanaho.
 */

package org.gzigzag;

import java.util.*;

/** A class that can be told of things that need to be done at
    shutdown and that will do them when it is asked to kill the
    program. Since Java does not provide pre-shutdown hooks, we need
    to provide them ourselves.  */
public class SafeExit {

    /** An object that wants to be called at exit time for cleanup.
     */
    public interface Cleanupable {
	/** Perform any cleanup operations necessary for this object.
	 */
        void cleanup() throws Throwable;
    }

    private static Vector things_to_clean_up = new Vector();

    public static void registerObject(Cleanupable o) {
        things_to_clean_up.addElement(o);
    }

    public static void unregisterObject(Cleanupable o) {
        things_to_clean_up.removeElement(o);
    }

    /** This is a safe exit procedure that kills all threads first,
        then cleans up those things that are registered for cleanup
        and then exits. */
    public static void exit(int exitcode) {
        // The following is from "The Java Programming Language",
        // Second Edition, p. 329--330

        // this needs the threads to be modified so that they end on
        // interrupt or something, so now this is disabled
//          Thread myThread = Thread.currentThread();
//          ThreadGroup thisGroup = myThread.getThreadGroup();
//          int count = thisGroup.activeCount();
//          Thread[] threads = new Thread[count + 20];
//          thisGroup.enumerate(threads);

//          for (int i = 0; i < threads.length; i++) {
//              if (threads[i] != null && threads[i] != myThread) {
//                  threads[i].interrupt();
//              }
//          }

//          for (int i = 0; i < threads.length; i++) {
//              if (threads[i] != null && threads[i] != myThread) {
//                  try {
//                      threads[i].join();
//                  } catch (InterruptedException e) {
//                      // ignore
//                  }
//              }
//          }

        //  Ok, now to our own stuff: clean up
        for (Enumeration e = things_to_clean_up.elements();
             e.hasMoreElements();) {
            Cleanupable o = (Cleanupable) e.nextElement();
            try {
                o.cleanup();
            } catch (Throwable t) {
		ZZLogger.exc(t, "At cleanup");
            }
        }

        System.exit(exitcode);
    }

}
