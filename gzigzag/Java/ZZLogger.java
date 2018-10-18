/*   
Main.java
 *    
 *    Copyright (c) 1999, 2000 Ted Nelson and Tuomas Lukka
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
 * Written by Antti-Juhani Kaijanaho and Tuomas Lukka.
 */

package org.gzigzag;
import java.lang.reflect.*;
import java.io.*;

/** A static package for logging things into a file.
 * Used for providing ZZ developers with debug output to help them
 * solve crashes.
 */

public class ZZLogger {
    public static PrintWriter logfile = null;

    private static boolean syslog = false;

    public static void tryInitSyslog() {
        try {
            SysLogger.init();
        } catch (UnsatisfiedLinkError e) {
            exc(e);
            return;
        }
        syslog = true;
    }

    public static void exc(Throwable t, String s) {
	log(""+s);
	exc(t);
    }

    public static void exc(Throwable t) {
	if(t instanceof InvocationTargetException) {
	    Throwable t1 = ((InvocationTargetException)t).getTargetException();
	    verblog("InvokeException content: ");
	    exc(t1);
	}
        log(""+t);
	t.printStackTrace();
	if(logfile != null) {
	    t.printStackTrace(logfile);
	    logfile.flush();
	}
    }

    public static void log(String s) {
        System.err.println(s);
        if (syslog) SysLogger.log(s);
        filelog(s);
    }

    private static void filelog(String s) {
        if (logfile != null) {
            logfile.println(s);
            logfile.flush();
        }
    }

    public static void verblog(String s) {
        if (syslog) SysLogger.verblog(s);
    }
}
