/*   
SimplePlayer.java
 *    
 *    Copyright (c) 2001, Ted Nelson, Tuomas Lukka and Vesa Parkkinen
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

/** A linux sound Player using GSM compression. Requires the associated native
 * library.
 */

public class SimplePlayer implements Player {

    private int handle;

    public void init(String file){
	handle = player_init(file);
    }

    public void play( long from, long to, boolean loop){
	player_start(handle, from, to);
    }
    
    public void stop(){
	player_stop(handle);
    }

    
    
    public long getCurrentOffset(){
	return player_getCurrentOffset(handle);
    }
    
    static {
	System.loadLibrary("recorder");
    }
    
    private native int player_init(String file);
    private native void player_start(int handle, long from, long to);
    private native long player_getCurrentOffset(int handle);
    private native void player_stop(int handle );
    
}
