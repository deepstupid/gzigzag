/*   
Sequence.java
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

class Sequence extends SyntaxForm {
    // Runtime representation: basecell -> rvcc -> tmprc -> csec on
    // d.1 posward
    private final class Rep {
        public ZZCell rvcc;  // pointer to return value pointer
        public ZZCell rvc;   // dereferenced rvcc
        public ZZCell tmprc; // temporary holder of component expression results
        public ZZCell tmpr;  // dereferenced tmprc
        public ZZCell csec;  // pointer to the current subexpression in code
        public ZZCell cse;   // current subexpression in code

        /** Create from existing structure. */
        public Rep() {
            rvcc = getBaseCell().s("d.1", 1);
            rvc = ZZCursorReal.get(rvcc);
            tmprc = rvcc.s("d.1", 1);
            tmpr = ZZCursorReal.get(tmprc);
            csec = tmprc.s("d.1", 1);
            cse = ZZCursorReal.get(csec);
        }

        /** Create also a new structure. */
        public Rep(ZZCell fse, ZZCell rvc) {
            rvcc = getBaseCell().N("d.1", 1);
            ZZCursorReal.set(rvcc, rvc);
            this.rvc = rvc;
            tmprc = rvcc.N("d.1", 1);
            tmpr = null;
            csec = tmprc.N("d.1", 1);
            ZZCursorReal.set(csec, fse);
            cse = fse;
        }

        public void delete() {
            rvcc.delete();
            tmprc.delete();
            csec.delete();
        }

        public Sequence(ZZCell c) { super(c); }
        
        public Sequence(ZZCell expr, ZZCell rvc) {
            super(rvc.getSpace());
            ZZCell fse = expr.s(DimensionNames.sequence, 1);
            if (fse == null) {
                throw new SyntaxError("sequence must have at least one element");
            }
            Rep rep(fse, rvc);
        }
            
        public void evalIteration() {
            Rep rep;

            if (rep.cse == null) {
                ZZCursorReal.set(rvc, tmpr);
                finishEval();
            }

            SyntaxForm sf = code_instantiate(rep.cse, tmprc);
            
            requestEval(sf);

            ZZCursorReal.set(rep.csec,
                             rep.cse.s(DimensionNames.sequence, 1));
        }

    }

}
