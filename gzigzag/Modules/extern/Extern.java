/*   
Extern.java
 *    
 *    Copyright (c) 2000, Ted Nelson, Tuomas Lukka and Vesa Parkkinen
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
 * Written by Vesa Parkkinen
 */
package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.io.*;
import java.awt.*;

/**
 * Module to handle external commands. At the moment only 
 * EXECBROWSER is implemented. To start netscape execbrowser with
 * empty target cell. If there is text in the target cell, 
 * netscape -remote openURL("") is executed.
 */
public class Extern {

    public static final String rcsid = "$Id: Extern.java,v 1.4 2000/09/19 10:32:02 ajk Exp $";
    public static final boolean dbg = true;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }


    static public ZZModule module = new ZZModule() {
	    public void action(String id, ZZCell code, 
			       ZZCell target,
			       ZZView v, ZZView cv, 
			       String key, Point pt, 
			       ZZScene xi) {
	    
	    
		try {
		p("Extern ACTION!");
		/* 
		 * XXX:
		 * Add a chech to see whether a netscape or mozilla 
		 * is running.
		 * 
		 */
		if(id.equals("EXECBROWSER")) {
		    Process p = null;
		    String s = target.getText(); 
		    if ( ! s.equals( "" ) ){
			p("netscape -remote openURL(" + s + ")" );
			p = Runtime.getRuntime().exec(
			     "netscape -remote openURL(" + s + ") ");
			p("" + p);
		    
		    }
		    else {
			Runtime.getRuntime().exec("netscape");
		    }
		} else {
		    pa("NO SUCH METHOD!! ");
		}
		
		}
		catch (IOException ex){
		    p("" + ex);
		}
	    }
	    
	};
}
