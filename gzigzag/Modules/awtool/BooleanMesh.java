/*   
BooleanMesh.java
 *    
 *    Copyright (c) 2000-2001 Ted Nelson and Tuomas Lukka
 *    Copyright (c) 2000-2001 Benjamin Fallenstein
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
 * Written by Kimmo Wideroos
 */

package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;

public abstract class BooleanMesh {
    public static boolean dbg = false;
    static void p(String s) { if(dbg) System.out.println(s); }
    static void pa(String s) { System.out.println(s); }

    static String d_1 = "d.1"; static String d_2 = "d.2";
    static public ZZCell bStructure = null;
    static public Hashtable bOperators = new Hashtable();

    /*
    */
    public BooleanMesh(ZZCell bs) { 
        bStructure = bs; 
        bOperators.put("AND", new boolean[][]{{false, false}, {false, true}});
        bOperators.put("OR", new boolean[][]{{false, true}, {true, true}});
        bOperators.put("XOR", new boolean[][]{{false, true}, {true, false}});
    }
    
    public boolean evaluate(ZZCell nhc) { return evaluate(bStructure, nhc); }
    
    private boolean evaluate(ZZCell bElem, ZZCell nhc) {
	String bt = bElem.getText();
        p("belem:"+bt+" c:"+nhc.getText());
	ZZCell arg1, arg2;
        int i = 0, j = 0;
	if(bOperators.containsKey(bt)) {
	    // add exception handling here!
	    arg1 = bElem.s(d_1); arg2 = bElem.s(d_2);
            p("arg1:"+arg1.getText());
            p("arg2:"+arg2.getText());
            if(evaluate(arg1, nhc)) i = 1; 
            if(evaluate(arg2, nhc)) j = 1;
            p("i:"+i+" j:"+j);
            boolean retv = ((boolean[][])bOperators.get(bt))[i][j];
            p("RETV: "+retv);
            return retv;
	} else {
	    // compare bElem (=categoryname) and c
            p("TEST! "+nhc.getText()+" =?= "+bElem.getText());
	    return testBelonging(nhc, bElem);
	}
    }

    public abstract boolean testBelonging(ZZCell c, ZZCell category);

}
    



