/*   
FileWritable.java
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
 * Written by Tuomas Lukka & Vesa Parkkinen
 */
package org.gzigzag;
import java.util.*;
import java.io.*;

/** A Writable that is just a file.
 * 
 *  Default: caching off..  
 */

// Beginning of block: 
// long: index of next block
// long: offset in subfile0 to the name.

public class FileWritable implements Writable {
    public static final String rcsid =
	"$Id: FileWritable.java,v 1.6 2000/09/19 10:32:00 ajk Exp $";
    public static final boolean dbg = false;
    static final void p(String s)  { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s);         }
    
    public static final int BUF_SIZE = 512;
    
    RandomAccessFile f;
    
    byte[] r_buffer;
    byte[] w_buffer;
    
    boolean cache;
    boolean dirty;
    
    // read buffer offset
    long  off;
    // write buffer offset
    long  w_off;
    // read bytes in buffer
    int   r_size;
    // bytes written 
    int written;
    
    /*
     * XXX TODO: not quite there yet...
     */
    public void write(long offset, byte[] data) {
	//p("write");
	try {
	    if( ! cache ){
		f.seek(offset);
		f.write(data);
		return;
	    }else {
		// clear read cache
		if ( offset < off + r_size &&
		     offset + data.length > off ) {

		    off = 0;
		    r_size = 0;
		}
		
		if ( data.length > BUF_SIZE ){
		    //p(" > ");
		    flush();
		    f.seek(offset);
		    f.write(data);
		    return;
		}
		if ( offset + data.length < w_off ){
		    //p(" under ");
		    f.seek(offset);
		    f.write(data);
		    //return;
		}else if ( offset > w_off + written ){
		    //p(" over ");
		    flush();
		    w_off = offset;
		    written = data.length;
		    System.arraycopy( data, 0, w_buffer, 0, written );
		    dirty = true;
		}else if ( offset < w_off && offset + data.length > w_off ){
		    //p(" partial ");
		    flush();
		    f.seek(offset);
		    f.write(data);
		   
		} else if( offset == w_off + written && 
			   BUF_SIZE - written > data.length ) {
		    //p(" hit: " + offset);
		    System.arraycopy( data, 0, w_buffer, written, data.length);
		    written += data.length;
		    //p(" written = " + written);
		    dirty = true;
		}else {
		    //p(" other ");
		    flush();
		    f.seek(offset);
		    f.write(data);
		    return;
		}
		
	    }
	} catch(Exception e) {
	    ZZLogger.exc(e);
	    throw new ZZError(" "+e);
	}
    }
    

    public byte[] read(long offset, int n) {
	//p("read");
	try {

	    byte[] b = new byte[n];

	    if( ! cache ){ 
		f.seek(offset);
		f.read(b);
		return b;
	    } else {
		if( offset < w_off + written && 
		    offset + n > w_off ) {
			flush();
		    }
		if ( offset > off && offset - off + n < r_size ){
		    
		    System.arraycopy(r_buffer, (int)(offset - off), b, 0, n );
		    return b;
		}
		
		f.seek(offset);
		
		r_size = f.read(r_buffer);
		System.arraycopy(r_buffer, 0, b, 0, n );
		off = offset;
		return b;
	    }
	    
	} catch(Exception e) {
	    ZZLogger.exc(e);
	    throw new ZZError(" read "+offset+" "+n+" "+e);
	}
    }
    

    public void read(long offset, byte b[], int o, int n) {
	//p("read2");
	try {
	    if (! cache ){
		f.seek(offset);
		f.read(b, o, n);
	    }else {
		if( offset + o  < w_off + written && 
		    offset + o + n > w_off ) {
		    flush();
		}

		if ( offset + o> off && offset + o - off + n < r_size ){
		    
		    System.arraycopy(r_buffer, (int)(offset + o - off), 
				     b, o, n );
		}else {
		    f.seek(offset + o);
		    r_size = f.read(r_buffer);
		    System.arraycopy(r_buffer, 0, b, o, n );
		    off = offset + o;
		}
	    }
	    
	} catch(Exception e) {
	    ZZLogger.exc(e);
	    throw new ZZError(" read " +e);
	}
	
    }
    
    public long length() { 
	try {
	    return f.length();	    

	} catch(Exception e) {
	    ZZLogger.exc(e);
	    throw new ZZError(" "+e);
	}
    }
    

    public void flush() { 
	if ( ! cache ) return;
	if( ! dirty || written == 0) return;
	    
	try{ 
	    
	    f.seek(w_off);
	    f.write(w_buffer, 0, written);
	    dirty = false;
	    //p("FLUSH " +  written);
	    written = 0;
	    
	} catch(Exception e) {
	    ZZLogger.exc(e);
	    throw new ZZError(" "+e);
	}
	
    }
    
    public FileWritable(RandomAccessFile f) {

	/*
	 * set this to true, if you want to test caching
	 */
	this(f, false);	
    }
    
    public FileWritable(RandomAccessFile f, boolean b) {
	//p("FW: create");
	this.f = f;
	cache = b;
	
	if ( cache ){ 
	    r_buffer = new byte[BUF_SIZE];
	    w_buffer = new byte[BUF_SIZE];
	}else{ 
	    r_buffer = null;
	}
	dirty = false;
	r_size = 0;	
    }
}



