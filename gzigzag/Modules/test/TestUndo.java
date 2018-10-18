/*   
TestUndo.java
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
import java.io.*;

public class TestUndo {
String rcsid = "$Id: TestUndo.java,v 1.8 2000/11/30 08:44:33 ajk Exp $";

    public static final boolean dbg = true;
    final static void p(String s) { if(dbg) System.out.println(s); }

    /*

    static void printdim(ZZDimension d, int max) {
	for(int i=1; i<=max; i++) {
	    p(i+"\t+"+ d.s(""+i, 1, null)+
			"\t-"+ d.s(""+i, -1, null));
	}
	p("");
    }

    static String ct2str(Object o) {
	if(o instanceof String) 
	    return "'"+o+"'";
	else if(o instanceof Span)
	    return "SPAN: "+o;
	else if(o == null)
	    return "null";
	throw new Error("Invalid content "+o);
    }

    static void printct(ContentStore st, int max) {
	for(int i=1; i<=max; i++) {
	    p(i+"\t"+ct2str(st.get(""+i)));
	}
	p("");
    }

    public static void main(String[] argv) {
	{
	    ZZCacheDimension d = new ZZCacheDimension();
	    UndoList ul = new UndoList(42);
	    d.setUndoList(ul);
		    
	    ZZDimStorer pstor = new ZZDimStorer() {
		public void storeConnect(String a, String b) {
		    p("Storeconn: "+a+" "+b);
		}
		public void storeDisconnect(String a, int dir) {
		    p("Storedisc: "+a+" "+dir);
		}
	    };

	    ContentStorer cstor = new ContentStorer() {
		public void putContent(String id, Object ct) {
		    p("StoreCont: "+id+" "+ct2str(ct)+"");
		}
	    };

	    d.connect("1", "2");
	    d.connect("3", "4");

	    ul.stamp();
	    printdim(d, 4);

	    ul.undo();
	    p("undo:");
	    printdim(d, 4);

	    ul.redo();
	    p("redo:");
	    printdim(d, 4);

	    p(" Next, more difficult test.");

	    d.disconnect("1", 1);
	    d.connect("1", "3");

	    ul.stamp();
	    printdim(d, 4);

	    ul.undo();
	    p("undo:");
	    printdim(d, 4);

	    ul.undo();
	    p("undo:");
	    printdim(d, 4);

	    ul.redo();
	    p("redo:");
	    printdim(d, 4);

	    ul.redo();
	    p("redo:");
	    printdim(d, 4);

	    p("Next, whether commit does the right ops...");

	    d.startCommit();
	    ul.commit();
	    d.endCommit(pstor);

	    p("Commit finished");

	    p("More edits");

	    d.disconnect("3", 1);
	    d.connect("2", "4");
	    printdim(d, 4);

	    p("Commit ops...");
	    d.startCommit();
	    ul.commit();
	    d.endCommit(pstor);
	    p("Commit finished");

	    p("Next, we'll test the simple file format.");

	    SimpleDimFile f = new SimpleDimFile();
	    ByteArrayOutputStream bo = new ByteArrayOutputStream();

	    f.startWrite(new DataOutputStream(bo));

	    f.storeConnect("Foo", "Bar");
	    f.storeConnect("1", "2");
	    f.storeDisconnect("a", 1);
	    f.storeDisconnect("b", -1);
	    f.storeDisconnect("3", 1);
	    f.endWrite();

	    byte[] bytes = bo.toByteArray();
	    bo.reset();

	    p("Wrote ops. Now read.");
	    // f.read(new DataInputStream(new ByteArrayInputStream(bytes)), pstor);


	    p("Finally, test the content storage as well");

	    ZZCacheContentStore ccs = new ZZCacheContentStore();
	    ccs.setUndoList(ul);

	    ul.stamp();

	    ccs.put("1", "foo");
	    ccs.put("2", "bar");
	    ccs.put("3", Span.parse("1.5:1.8"));

	    ul.stamp();
	    ccs.put("2", "txt");
	    ul.stamp();
	    printct(ccs, 4);

	    ul.undo();
	    p("Undo: ");
	    printct(ccs, 4);

	    ul.undo();
	    p("Undo: ");
	    printct(ccs, 4);

	    ul.redo();
	    p("Redo: ");
	    printct(ccs, 4);

	    ul.redo();
	    p("Redo: ");
	    printct(ccs, 4);

	    SimpleContentFile cf = new SimpleContentFile();
	    cf.startWrite(new DataOutputStream(bo));

	    cf.putContent("A", "FOO");
	    cf.putContent("B", "bar");
	    cf.putContent("C", Span.parse("1.5:1.8"));
	    cf.endWrite();
	    bytes = bo.toByteArray();
	    bo.reset();

	    p("Wrote txts; read:");
	    // cf.read(new DataInputStream(new ByteArrayInputStream(bytes)), cstor);
	    

	}

    }
    */
}
