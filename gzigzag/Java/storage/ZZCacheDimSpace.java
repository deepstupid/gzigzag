/*   
ZZCacheDimSpace.java
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
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
package org.gzigzag;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

/** A dimspace that uses a StreamSet to be persistent.
 * XXX Triggers to undo events?!?!? Existing ZZCells deleted??
 */

public class ZZCacheDimSpace extends ZZDimSpace {
public static final String rcsid = "$Id: ZZCacheDimSpace.java,v 1.28 2001/03/08 13:54:20 ajk Exp $";

    protected int maxtimestamp;

    StreamSet strs;

    // XXX Stamps!
    UndoList ul = new UndoList(2935);
    ZZCacheContentStore ct = new ZZCacheContentStore();

    Hashtable scrolls = new Hashtable();
    int timestamp = 1;

    synchronized public int stamp() { return ul.stamp(); }
    synchronized public void undo() { ul.undo(); }
    synchronized public void redo() { ul.redo(); }
    synchronized public int commit() {
        if (readonly) return timestamp;
	p("COMMIT!");
	// Put in time of last save. Not used anywhere yet.
	ct.put("savetime", ""+System.currentTimeMillis());
	for(Enumeration e = dims.keys(); 
	    e.hasMoreElements(); ) {
	    String dname = (String)e.nextElement();
	    ZZDimension d = (ZZDimension)dims.get(dname);
	    if(d instanceof ZZCacheDimension) {
		ZZCacheDimension cd = (ZZCacheDimension)d;
		cd.startCommit();
	    }
	}
	ct.startCommit();
	ul.commit();
	int stmp = ++timestamp;
	SimpleDimFile outf = new SimpleDimFile();
	for(Enumeration e = dims.keys(); 
	    e.hasMoreElements(); ) {
	    String dname = (String)e.nextElement();
	    ZZDimension d = (ZZDimension)dims.get(dname);
	    if(d instanceof ZZCacheDimension) {
		ZZCacheDimension cd = (ZZCacheDimension)d;

		GZZ0 writ = new GZZ0(strs, dname);

		writ.startWrite(stmp, outf);
		cd.endCommit(outf);
		writ.endWrite(false);
	    }
	}
	SimpleContentFile coutf = new SimpleContentFile();
	GZZ0 cwrit = new GZZ0(strs, "CONTENT");
	cwrit.startWrite(stmp, coutf);
	ct.endCommit(coutf);
	cwrit.endWrite(true);
        for (Enumeration e = spaceParts.keys(); e.hasMoreElements();) {
            String s = (String)e.nextElement();
            ZZSpacePart sp = (ZZSpacePart)spaceParts.get(s);
            sp.postCommitHook();
        }
	return stmp;
    }
    
    public ZZCacheDimSpace(StreamSet w) {
        this(w, -1);
    }

    public ZZCacheDimSpace(ZZCacheDimSpace cds, int tstamp) {
        this(cds.strs, tstamp);
    }

    public ZZCacheDimSpace(StreamSet w, int tstamp) {
        this(w, tstamp, (tstamp != -1));
    }

    public ZZCacheDimSpace(StreamSet w, int tstamp, boolean readonly) {
        super(readonly);

	strs = w;

	ct.setUndoList(ul);
	ct.setSpace(this);

        maxtimestamp = tstamp;

	if(strs.exists("CONTENT")) {
	    SimpleContentFile cinf = new SimpleContentFile();
	    GZZ0 crea = new GZZ0(strs, "CONTENT");
	    cinf.startRead(ct);
	    timestamp = crea.read(cinf, tstamp);
	    cinf.endRead();
	} else {
	    // The id of the next free cell must be stored in the space,
	    // this is a kludge.
	    ct.put("nextfreeid", nextID);
	    // The home cell is called HOME
	    ct.put("1", "HOME");
	}
	nextID = (String)ct.get("nextfreeid");

	spaceParts.put("bintree", new BinaryTreePart(this, "bintree"));
	spaceParts.put("imptree", new ImpliedTreePart(this, "imptree"));
	spaceParts.put("imptree2", new ImpliedTreePart2(this, "imptree2"));
	spaceParts.put("doubler", new DoubleStepPart(this, "doubler"));
        spaceParts.put("version", new VersionPart(this, "version"));

	spaceParts.put("gentreetest", new GenTreePart(this, "gentreetest",
		new GenTreeTestModel()));

	try {
	    spaceParts.put("nile1str", new GenTreePart(this, "nile1str",
		(GenTreePartModel)
		    Class.forName("org.gzigzag.module.Nile1GenTreeModel").
			newInstance()));
	} catch(Exception e) {
	    p("Nile module not found - this is not fatal unless you know what nile is. Don't worry.");
	}

	try {
	    Class ztp = Class.forName("org.zaubertrank.ZaubertrankPart");
	    Class zzsp = Class.forName("org.gzigzag.ZZSpace");
	    Constructor ztpc = ztp.getConstructor(new Class[] {
			zzsp, "".getClass() });
	    ZZSpacePart sp = (ZZSpacePart)
		ztpc.newInstance(new Object[] { this, "zaubertrank" });
	    spaceParts.put("zaubertrank", sp);
        } catch (ClassNotFoundException e) {
            // ignore: that Zaubertrank stack trace is annoying
	} catch(Throwable t) {
	    ZZLogger.exc(t);
	    // the zaubertrank isn't compiled by standard, so don't tell
	}

    }

    protected void setNextID(String s) {
        super.setNextID(s);
        ct.put("nextfreeid", nextID);        
    }

    protected Span getSpan(String id) {
	Object o = ct.get(id); 
	if(!(o instanceof Span))
	    return null;
	return (Span)o;
    }
    protected String getText(String id) {
	Object o = ct.get(id); 
	if(o==null) return "";
	if(o instanceof Span) 
	    o = ((Span)o).getString(getHomeCell());
	return (String)o;
    }

    protected void setText(String id, Object cont) {
        if (readonly) throw new ZZError("readonly space");
	ct.put(id, cont);
    }



    protected ZZDimension createDimension(String s) {
	if(!validDim(s)) return null;
	if(s.indexOf(':') != -1) return createPartDimension(s);


        ZZDimension d;
        if(s.equals("d.cellcreation")) {
	    d = new IDDimension();
        } else {
	    ZZCacheDimension dim = new ZZCacheDimension(readonly);
	    dim.setUndoList(ul);
	    InputStream is = strs.getInputStream(s);
	    if(strs.exists(s)) {
		SimpleDimFile inf = new SimpleDimFile();
		inf.startRead(dim);
		GZZ0 rea = new GZZ0(strs, s);
		rea.read(inf, maxtimestamp);
		inf.endRead();
	    }
	    d = dim;
        }
	d.setSpace(this);
	return d;
    }


    public String getIDOrNull() {
        Object o = ct.get("spaceid");
        if (!(o instanceof String)) {
            return null;
        }
        return (String)o;
    }

    

    public void setID(String id) {
        if (readonly) throw new ZZError("readonly space");
        ct.put("spaceid", id);
    }

    public StringScroll getStringScroll() {
	return getStringScroll("text");
    }
    public StringScroll getStringScroll(String name) {
	StringScroll scr;
	if((scr = (StringScroll)scrolls.get(name)) == null) {
	    Writable wr = strs.getWritable("STRSCR_"+name);
	    scr = new StringScroll(name, wr, readonly);
	    // XXX ???
	    Scroll.register(name, scr);
	    scrolls.put(name, scr);
	}
	return scr;
    }


}
