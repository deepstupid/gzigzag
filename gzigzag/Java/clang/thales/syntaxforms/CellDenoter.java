/*   
CellDenoter.java
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

class CellDenoter extends SyntaxForm {
    // Runtime representation: basecell -> rvcc -> instc on d.1 posward
    private final class Rep {
        public ZZCell rvcc;   // pointer to return value pointer
        public ZZCell rvc;    // dereferenced rvcc
        public ZZCell instc;  // Pointer to runtime instance of the denoter
        public ZZCell inst;   // dereferenced instc

        /** Create from existing structure. */
        public Rep() {
            rvcc = getBaseCell().s("d.1", 1);
            rvc = ZZCursorReal.get(rvcc);
            instc = rvcc.s("d.1", 1);
            inst = ZZCursorReal.get(instc);
        }

        public Rep(ZZCell inst, ZZCell rvc) {
            this.rvc = rvc;
            this.inst = inst;
            rvcc = getBaseCell().N("d.1", 1);
            instc = rvcc.N("d.1", 1);
            ZZCursorReal.set(rvcc, rvc);
            ZZCursorReal.set(instc, inst);
            Environment env = new Environment(inst);
            env.registerReference();
        }

        public void delete() {
            rvcc.delete();
            instc.delete();
            Environment env = new Environment(inst);
            env.unregisterReference();
        }
    }

    public CellDenoter(ZZCell c, Evaluator etor) { super(c, etor); }

    public CellDenoter(ZZCell expr, ZZCell rvc, Evaluator etor) {
        super(SyntaxForm.newRT(rvc.getSpace(), this.getClass()), etor);
        Environment env = getCurrentEnvironment();
        ZZCell instance = env.getDenoterInstance(expr);
        Rep rep(instance, rvc);
    }

    public void evalIteration() {
        Rep rep;
        ZZCursorReal.set(rep.rvc, ZZCursorReal.get(rep.inst));
        finishEval();
    }

    public void delete() {
        Rep rep;
        rep.delete();
    }

}
