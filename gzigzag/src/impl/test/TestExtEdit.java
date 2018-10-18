/*   
TestExtEdit.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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
import org.gzigzag.util.*;
import junit.framework.*;
import java.io.*;
import java.util.*;

/** Test external editing
 */

public class TestExtEdit extends ZZTestCase {
public static final String rcsid = "$Id: TestExtEdit.java,v 1.9 2002/03/10 01:16:23 bfallenstein Exp $";

    static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }

    public TestExtEdit(String s) { super(s); }

/* A test that fails for Kaffe 1.0.6
    public void testSubList() {
	List l = new ArrayList();
	l.add(new Object ());
	l.add(new Object ());
	l.add(new Object ());
	List l2 = l.subList(1, 3);
	p(" "+l2);
    }
 */

    ExternalEditor.DynEdit dyn = new ExternalEditor.DynEdit();

    public void testDyn() {
	ExternalEditor.DynEdit dyn = new ExternalEditor.DynEdit();
	ExternalEditor.ExtRep from = new ExternalEditor.ExtRep("abcdefg");
	ExternalEditor.ExtRep to = new ExternalEditor.ExtRep("acdehfgbla");
	ExternalEditor.ExtRep nfrom = new ExternalEditor.ExtRep(), 
			    nto = new ExternalEditor.ExtRep();
	p("Once");
	dyn.edit(from, to, nfrom, nto);
	p(" "+nfrom);
	p(" "+nto);
    }

    public void testDyn2() {
	ExternalEditor.DynEdit dyn = new ExternalEditor.DynEdit();
	ExternalEditor.ExtRep from = new ExternalEditor.ExtRep("abcdef");
	ExternalEditor.ExtRep to = new ExternalEditor.ExtRep("ldefgabch");
	ExternalEditor.ExtRep nfrom = new ExternalEditor.ExtRep(), 
			    nto = new ExternalEditor.ExtRep();
	p("Twice");
	dyn.edit(from, to, nfrom, nto);
	p(" "+nfrom);
	p(" "+nto);

	to = nto; from = nfrom;
	nfrom = new ExternalEditor.ExtRep();
	nto = new ExternalEditor.ExtRep();

	dyn.edit(from, to, nfrom, nto);
	p(" "+nfrom);
	p(" "+nto);

	nfrom.combineMatches(nto);

	p(" "+nfrom);
	p(" "+nto);

	assertEquals(new Character('l'), nto.objects.get(0));
	assertEquals(new Character('g'), nto.objects.get(2));
	assertEquals(new Character('h'), nto.objects.get(4));

	ExternalEditor.Match m = (ExternalEditor.Match)nto.objects.get(1);
	assertEquals(3, m.length());
	assertEquals(new Character('d'), m.objects.get(0));
	assertEquals(new Character('e'), m.objects.get(1));
	assertEquals(new Character('f'), m.objects.get(2));
    }

    public void testExtSave() throws UnsupportedEncodingException {
	String s = "abc{[[s1]]}def{[[s2]]}";
	String enc = "ISO8859_1";
	byte[] b = s.getBytes(enc);
	ExternalEditor.ExtRep from = new ExternalEditor.ExtRep(b, enc);
	assertEquals(new Character('c'), from.objects.get(2));
	assertEquals(new ExternalEditor.Special("s1"), from.objects.get(3));
	assertEquals(new Character('d'), from.objects.get(4));
	assertEquals(8, from.length());
	byte[] nb = from.getBytes(enc);
	assertEquals(b, nb);
    }

    public void testSimpleExtEdit() throws Exception {
	ExternalEditor.ExtRep from = new ExternalEditor.ExtRep("abcdef");
	ExternalEditor.ExtRep to = 
		ExternalEditor.execEditor("/usr/bin/perl -pi -es/bcd/foo/g",
		    from, "ISO8859_1");
	assertEquals(new Character('a'), to.objects.get(0));
	assertEquals(new Character('f'), to.objects.get(1));
	assertEquals(new Character('o'), to.objects.get(2));
	assertEquals(new Character('o'), to.objects.get(3));
	assertEquals(new Character('e'), to.objects.get(4));
	assertEquals(6, to.length());
    }


    SimpleSpanSpace space;

    String s1, s2, s3, s4, s5;
    Cell c1, c1c, c2, c3, c4, c5;
    
    Cell vs = Dims.d_vstream_id;
    VStreamDim dim;
    Dim vdim;

    TransientTextScroll block;

    public void simpleSpaceSetUp() throws Exception {
	space = new PermanentSpace(TestImpl.zms);
        dim = space.getVStreamDim();
	vdim = (Dim)dim;
        block = new TransientTextScroll();

        c1 = space.N();
        c2 = space.N();
        c3 = space.N();
        c4 = space.N();
        c5 = space.N();
        c1c = c1.zzclone();	
    }

    public void testSpans() throws Exception {
	simpleSpaceSetUp();

	Cell sp = space.makeSpanRank("Testing spans");

	c1.connect(vdim, sp);

	Cell sp1 = c1.s(vdim);
	Cell sp2 = sp1.s(vdim);

	ExternalEditor.Matching m = new ExternalEditor.Matching(c1, "ISO8859_1");

	m.edit("/usr/bin/perl -pi -es/ng(\\s)sp/ng\\1the\\1sp/g");

	m.matchDynEdit();

	m.commit();

	assertEquals("Testing the spans", c1.t());

	Cell sp1new = c1.s(vdim);
	Cell sp2new = sp1new.s(vdim);

	assertTrue(sp1.getSpan().equals(sp1new.getSpan()));
	assertTrue(! sp1.getSpan().equals(sp2new.getSpan()));
	assertTrue(sp2.getSpan().equals(sp2new.getSpan()));

	

    }


}





