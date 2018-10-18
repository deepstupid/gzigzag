/*   
GZZ1NullHandler.java
 *    
 *    Copyright (c) 2001, Benja Fallenstein
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
 * Written by Benja Fallenstein
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.mediaserver.*;
import java.io.*;

/** An event handler for the GZZ1 format parser that does nothing.
 *  Useful for subclasses if you only want to catch specific events.
 */

public class GZZ1NullHandler implements GZZ1Handler {
String rcsid = "$Id: GZZ1NullHandler.java,v 1.4 2001/11/04 18:22:23 tjl Exp $";
    static boolean dbg = false;
    static private void p(String s) { if(dbg) System.out.println(s); }

    class SimpleDim implements GZZ1Handler.SimpleDim {
	public void disconnect(byte[] id1, byte[] id2) {
	}
	public void connect(byte[] id1, byte[] id2) {
	}
	public void close() {
	}
    }

    class LegacyContent implements GZZ1Handler.LegacyContent {
	public void transcludeLegacyContent(byte[] id, int first, int last) {
	}
	public void close() {
	}
    }

    public void start(Mediaserver.Id previous) {
    }

    public GZZ1Handler.SimpleDim dimSection(byte[] cellId) {
	return new SimpleDim();
    }

    public GZZ1Handler.LegacyContent legacyContentSection() {
	return new LegacyContent();
    }

    public NewCells newCellsSection() {
	return new NewCells() {
		public void newCell(byte[] cellId) {}
		public void close() {}
	    };
    }

    public Transcopy transcopySection(byte[] transcopyId,
                            org.gzigzag.mediaserver.Mediaserver.Id spaceId) {
        return new Transcopy() {
                public void transcopy(byte[] cellId) {
                }
                public void close() {}
            };
    }

    public SpanTransclusion spanTransclusionSection() {
	return new SpanTransclusion() {
		public void transclude(byte[] transclusionId,
				       Mediaserver.Id block, int from, int to){
		}
		public void close() {}
	    };
    }

    public void close() {}

}

