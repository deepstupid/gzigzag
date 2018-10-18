/*   
SyntaxForm.java
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

package org.gzigzag.clang.thales;

import org.gzigzag.*;
import java.lang.reflect.*;

/** Syntax form.  Subclasses must have at least two constructors: one
    taking a ZZCell as an argument (the cell must be used to deduce
    existing runtime state) and another taking two ZZCells as
    arguments (the first cell must be used for parsing the expression,
    the other cell must be understood to mean the cell that should be
    updated to point to the result of the expression).  */
public abstract class SyntaxForm implements Refcounted {
    
    static protected ZZCell newRT(ZZSpace space, Class cl) {
        String name = cl.getName();
        name = name.substring(name.lastIndexOf(".")+1);
        ZZCell home = space.getHomecell();
        ZZCell namec = home.findText(DimensionNames.syntaxformlist, 1, name);
        if (namec == null) {
            namec = home.N(DimensionNames.syntaxformlist, 1);
            namec.setText(name);
        }
        return namec.zzclone();
    }

    static public SyntaxForm rt_instantiate(ZZCell c, Evaluator etor) {
        ZZCell sfc = c.h("d.clone");

        if (sfc == null) throw new ZZError("no d.clone headcell");

        if (sfc.h(DimensionNames.syntaxformlist) != sfc.getHomecell()) {
            throw new ZZError("not in syntax form list");
        }
        
        Object o;

        try {
            Class cl = Class.forName("org.gzigzag.clang.thales.syntaxform"
                                     + name);
            Class cellcl = Class.forName("org.gzigzag.ZZCell");
            Constructor ctor = cl.getConstructor(new Class[] { cellcl });
            o = ctor.newInstance(new Object[] { c } );
        } catch (ClassNotFoundException e) {
            throw new ZZError("syntax form not found (" + e + ")");
        } catch (InstantiationException e) {
            throw new ZZError("problems instantiating syntax form: " + e);
        } catch (IllegalAccessException e) {
            throw new ZZError("" + e);
        }
        
        if (!(o instanceof SyntaxForm)) throw new InvalidException();

        SyntaxForm rv = (SyntaxForm) o;

        rv.etor = etor;

        return rv;
    }

    static public SyntaxForm code_instantiate(ZZCell code, ZZCell rv, 
                                              Evaluator etor) {
        ZZCell sfc = code.h(DimensionNames.syntaxform);

        if (sfc == null) throw new ZZError("invalid syntax form");

        Object o;

        try {
            Class cl = Class.forName("org.gzigzag.clang.thales.syntaxform"
                                     + sfc);
            Class cellcl = Class.forName("org.gzigzag.ZZCell");
            Constructor ctor = cl.getConstructor(new Class[] { cellcl, cellcl });
            o = ctor.newInstance(new Object[] { c, rv } );
        } catch (ClassNotFoundException e) {
            throw new ZZError("syntax form not found (" + e + ")");
        } catch (InstantiationException e) {
            throw new ZZError("problems instantiating syntax form: " + e);
        } catch (IllegalAccessException e) {
            throw new ZZError("" + e);
        }
        
        if (!(o instanceof SyntaxForm)) throw new InvalidException();

        SyntaxForm rv = (SyntaxForm)o;
        rv.etor = etor;
        return rv;
    }

    private ZZCell base;
    private Evaluator etor = null;

    /** Construct oneself using an existing syntax form runtime
        cell. */
    protected SyntaxForm(ZZCell c) {
        base = c;
    }

    /** Construct oneself using a new syntax form runtime cell. */
    protected SyntaxForm(ZZSpace s) {
        this(SyntaxForm.newRT(s, this.getClass()));
    }

    protected SyntaxForm code_instantiate(ZZCell code, ZZCell rv) {
        SyntaxForm.code_instantiate(code, rv, etor);
    }

    protected final ZZCell getBaseCell() { return base; }
    protected final void requestEval(SyntaxForm sf) { etor.request(sf); }
    protected final void finishEval() { etor.finishEval(this); }
    protected final void setCurrentEnvironment(Environment e) {
        etor.setCurrentEnvironment(e);
    }
    protected final Environment getCurrentEnvironment() {
        return etor.getCurrentEnvironment();
    }

    public abstract void evalIteration();

    /** Delete the runtime representation unconditionally.  */
    protected abstract void delete();

    private int getReferenceCount() {
        String text = cell.getText();
        if (text.equals("")) {
            setReferenceCount(0);
            return getReferenceCount();
        }
        return Integer.parseInt(text);
    }

    private void setReferenceCount(int rc) {
        cell.setText("" + rc);
    }

    public final void registerReference() {
        setReferenceCount(getReferenceCount() + 1);
    }

    public final void unregisterReference() {
        setReferenceCount(getReferenceCount() - 1);
        if (getReferenceCount() < 1) {
            delete();
            cell.delete();
        }
    }

}
