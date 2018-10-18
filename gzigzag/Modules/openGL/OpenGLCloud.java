/*   
OpenGLCloud.java
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
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/** 
 * A Simple OpenGL test.
 */

public class OpenGLCloud {
public static final String rcsid = "$Id: OpenGLCloud.java,v 1.4 2000/10/18 14:35:32 tjl Exp $";
    public static final boolean dbg = true;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }
    
    OpenGLCloud(){
	
    }
    
    static public ZZModule module = new ZZModule() {
	    ZZOpenGLGraphics o;
	    int xk = -20;
	    public void action(String id,
			       ZZCell code, 
			       ZZCell target,
			       ZZView view, ZZView cview, 
			       String key, Point pt, ZZScene xi) {
		
		ZZCell viewCell = view.getViewcell();
		ZZCell viewCursor = ZZCursorReal.get(viewCell);
		
		Object ob = null;
		if(pt != null && xi != null)
		    ob = xi.getObjectAt(pt.x, pt.y);
		
		
		
		p("openGL action! '"+id+"' key: "+key);
		if(id.equals("TEST")) {
		    
		} else if(id.equals("OPENWIN")) {
		    p("openGL OPENWIN");
		    o = new ZZOpenGLGraphics();
		    o.openWindow(400,400);
		    
		    o.addKeyListener(new KeyListener(){
			    
			    public void keyTyped(KeyEvent e){
				p("****KEY TYPED: " + e);
				
			    }
			    
			    public void keyPressed(KeyEvent e){
				p("****KEY PRESSED: " + e);
				key( e.getKeyChar() );
			    }
			    
			    public void keyReleased(KeyEvent e){
				p("****KEY RELEASED: " + e);
			    }
			    
			});

		    o.putLine(20,20,30,10, "TEST");
		    
		    o.putText( "Just testing",-20,30);
		    o.putText( "Another String...",-20,20);
		    o.putText( "Line ",-20,10);
		    o.putText( "Last line",-20,0);
		    o.putArrow(-10, -10, 10,10,new Color(1,0,0),"TEST");
		    
		    /*
		      ZZCell c = target;
		      for( int i = 0; i < 5 ; i++){
		      o.putCell(c, 0,0 - i*10,25,8,"TEST");
		      c = c.s("d.2", 1);
		      if ( c != null ){
		      
		      o.putLine(12,0-i*10,12,50 - i*10 - 2, "TEST");
		      }else 
		      break;
		      }
		      c = target.s("d.2", -1);;
		      if( c != null ) 
		      for( int i = 1; i < 5 ; i++){
		      o.putCell(c, 0, i*10,25,8,"TEST");
		      c = c.s("d.2", -1);
		      if ( c != null ){
		      o.putLine(12,i*10+10,12, i*10 + 2, "TEST");
		      }else 
		      break;
		      }
		    */
		    
		    o.start();
		}
	    }
	    public void key(char k){
		p("===KEY===");
		xk += 10;
		o.putText( "" + k ,xk ,-20);
		}

	    public ZZRaster getRaster(String id){
		return null;
	    }
	    
	};
}
