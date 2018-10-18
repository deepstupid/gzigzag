/*   
DiffSpace.java
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
import org.gzigzag.mediaserver.*;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import org.gzigzag.util.*;
import java.util.*;
import java.io.*;

public class DiffSpace {
public static final String rcsid = "$Id: DiffSpace.java,v 1.3 2002/03/10 01:16:23 bfallenstein Exp $";
    protected static void p(String s) { System.out.println(s); }
    protected static void pa(String s) { System.err.println(s); }

    static Space space;

    static String blockChars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static boolean[] isBlockChar = new boolean[128];
    static {
	for(int i=0; i<blockChars.length(); i++)
	    isBlockChar[blockChars.charAt(i)] = true;
    }

    public static String id(byte[] b) {
	String s = new String(b);
	String t = space.getCell(s).t();
	int nIdChars = 0;
	for(int i=0; i<s.length()+1; i++) {
	    char c = (i < s.length() ? s.charAt(i) : 0);
	    // p("IDLOOP: '"+s+"' "+i+" "+isBlockChar[c]);
	    if(isBlockChar[c]) {
		nIdChars ++;
	    } else {
		if(nIdChars >= 6) {
		    s = s.substring(0, i - nIdChars) + "..."
		       + s.substring(i-6);
		    i -= nIdChars;
		    i+=5 + 2;
		}
		nIdChars = 0;
	    }
	}
	return s+ " ("+t+") ";
    }

    public static void main(String argv[])  {
	try {
	Storer stor;
	if (argv.length < 1) {
	    pa("No filename given: aborting");
	    return;
	} else {
	    if(!new File(argv[0]).exists()) {
		pa("Mediaserver dir does not exist: aborting");
		return;
	    }
	    stor = new DirStorer(new File(argv[0]));
	}
	Mediaserver ms = new SimpleMediaserver(stor,new IDSpace(), 0);

	Mediaserver.Id spaceId = new Mediaserver.Id(argv[1]);
	space = new PermanentSpace(ms, spaceId);

	GZZ1Handler h = new GZZ1Handler() {
	    public void start(Mediaserver.Id previous) {
		p("Previous: "+previous);
	    }

	    public GZZ1Handler.SimpleDim dimSection(byte[] cellId) {
		p("Dimension: "+cellId);
		return new SimpleDim() {
		    public void disconnect(byte[] id1, byte[] id2) {
			p("Disconnect "+id(id1)+" --- "+id(id2));
		    }
		    public void connect(byte[] id1, byte[] id2) {
			p("Connect "+id(id1)+" -X- "+id(id2));
		    }
		    public void close() {
		    }

		};
	    }



	    public GZZ1Handler.LegacyContent legacyContentSection() {
		return null;
	    }


	    public Transcopy transcopySection(byte[] transcopyId,
		org.gzigzag.mediaserver.Mediaserver.Id spaceIdTP) {
		return null;
	    }
	    public SpanTransclusion spanTransclusionSection() {
		return null;
	    }
	    public NewCells newCellsSection() {
		return null;
	    }

	    public void close() {}

	};

	String s = new String(ms.getDatum(spaceId).getBytes());
	Reader r = new StringReader(s);
	p("Read: "+s.length());
	GZZ1Reader.read(r, h);
	p("Done");



	}catch(IOException e) {
	    e.printStackTrace();
	}
    }
}
