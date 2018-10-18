/*   
SimpleRecorder.java
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
import java.net.*;
import java.awt.*;

/**
 * Simple (linux) native recorder class to be used in 
 * sound stream handling in zz.
 */
public class SimpleRecorder implements Recorder {
    
    int handle = 0;
    
    // inits recorder with a output file name
    public void init(String file, long offset){
	handle = recorder_init(file, offset);
    }
    
    public void start(){
	recorder_start( handle );
    }
    
    
    public void stop(){
	recorder_stop(handle);
    }

    public long getCurrentOffset(){
	return recorder_getCurrentOffset(handle);
    }
    
    
    static { 
	System.loadLibrary("recorder");
    }
    
    private native void recorder_start(int handle);
 
    private native void recorder_stop(int handle );
    
    private native int recorder_init(String file, long offset);

    private native long recorder_getCurrentOffset(int handle);
}
