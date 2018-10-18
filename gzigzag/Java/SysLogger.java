/*   
SysLogger.java
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
 * Written by Antti-Juhani Kaijanaho
 */

package org.gzigzag;

class SysLogger {

    private static native void openlog();
    private static native void syslog(String msg, boolean verbose);
    
    public static synchronized void init() throws UnsatisfiedLinkError {
        System.loadLibrary("gzigzag-syslog");
        openlog();
    }

    public static synchronized void log(String s) {
        syslog(s, false);
    }

    public static synchronized void verblog(String s) {
        syslog(s, true);
    }

}
