/*   
Primitive.java
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
 * Written by Antti-Juhani Kaijanaho.
 */

package org.gzigzag.clang.thales.syntaxform;

import org.gzigzag.*;
import org.gzigzag.clang.thales.*;
import java.util.*;

public class Primitive extends SyntaxForm {
public static final String rcsid = "$Id: Primitive.java,v 1.7 2000/10/18 14:35:31 tjl Exp $";
    public static boolean dbg = false;
    final static void p(String s) { if(dbg) System.out.println(s); }
    final static void pa(String s) { System.out.println(s); }

    // Runtime representation basecell -> rvcc -> primc posward on d.1
    private final class Rep {
        public ZZCell rvcc;  // pointer to return value pointer
        public ZZCell rvc;   // dereferenced rvcc
        public ZZCell primc; // pointer to the primitive cell
        public ZZCell prim;  // the primitive cell

        /** Create from existing structure. */
        public Rep() {
            rvcc = getBaseCell().s("d.1", 1);
            rvc = ZZCursorReal.get(rvcc);
            primc = rvcc.s("d.1", 1);
            prim = ZZCursorReal.get(rvc);
        }

        /** Create also a new structure. */
        public Rep(ZZCell prim, ZZCell rvc) {
            this.rvc = rvc;
            rvcc = getBaseCell().N("d.1", 1);
            ZZCursorReal.set(rvcc, rvc);
            this.prim = prim;
            primc = rvcc.N("d.1", 1);
            ZZCursorReal.set(primc, prim);
        }

        public void delete() {
            rvcc.delete();
            primc.delete();
        }
    }

    public Primitive(ZZCell c) { super(c); }
    public Primitive(ZZCell expr, ZZCell rvc) {
        super(rvc.getSpace());
        Rep rep(expr.h("d.clone", -1), rvc);
    }

    public void evalIteration() {
        Rep rep;

        ZZCell rv = rep.rvc.N();
        rv.connect(DimensionNames.applicable, 1, rep.prim);
        ZZCursorReal.set(rep.rvc, rv);
        finishEval();
    }

    public void delete() {
        Rep rep;
        rep.delete();
    }



    static private Hashtable prims = new Hashtable();
    static private Hashtable optocell = new Hashtable();
    
    static public PrimitiveOperation get(ZZCell c) {
        ZZCell id = c.h("d.clone", -1);
        synchronized (prims) {
            return (PrimitiveOperation) prims.get(id);
        }
    }
    
    static public class PrimitiveProblemException extends ZZError {
        public PrimitiveProblemException(String s) {
            super(s);
        }
    }
    
    static public class DuplicateException extends PrimitiveProblemException {
        public DuplicateException() {
            this("duplicate Thales Clang primitive cell");
        }
        public DuplicateException(String s) {
            super(s);
        }
    }
    
    static public class InvalidException extends PrimitiveProblemException {
        public InvalidException() {
            this("invalid Thales Clang primitive cell");
        }
        public InvalidException(String s) {
            super(s);
        }
    }
    
    static private void readAPrimitive(ZZCell c) {
        ZZCell op = c.s(DimensionNames.primbinding, 1);
        
        if (op == null) throw new InvalidException();
        
        Object o;
        
        try {
            Class cl = Class.forName("org.gzigzag.clang.thales.primitive." + op.getText());
            o = cl.newInstance();
        } catch (ClassNotFoundException e) {
            throw new InvalidException("primitive not found (" + e + ")");
        } catch (InstantiationException e) {
            throw new InvalidException("problems instantiating primitive: " + e);
        } catch (IllegalAccessException e) {
            throw new InvalidException("" + e);
        }
        
        if (!(o instanceof PrimitiveOperation)) throw new InvalidException();
            
        // XXX: is this necessary or desirable?
        if (optocell.containsKey(o)) throw new DuplicateException();
            
        synchronized (prims) {
            prims.put(c, o);
            optocell.put(o, c);
        }
    }
    
    static public void readPrimitivesFromStructure(ZZSpace space) {
        ZZCell home = space.getHomeCell();
        
        synchronized (prims) {
            for (ZZCell c = home.s(DimensionNames.primitives, 1);
                 c != null;
                 c = c.s(DimensionNames.primitives, 1)) {
                try {
                    readAPrimitive(c);
                } catch (PrimitiveProblemException e) {
                    pa("" + e);
                }
            } 
        }
    }

    static public void register(ZZSpace space, String mc) {
        ZZCell home = space.getHomeCell();
        
        synchronized (prims) {
            ZZCell id = home.N();
            ZZCell name = id.N(DimensionNames.primbinding, 1);

            name.setText(mc);

            try {
                readAPrimitive(id);
            } catch (ZZError e) {
                id.delete();
                name.delete();
                throw e;
            } 

            home.insert(DimensionNames.primitives, 1, id);
        }
    }

} 




