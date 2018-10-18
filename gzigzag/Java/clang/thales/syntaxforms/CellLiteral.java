/*   
CellLiteral.java
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

class CellLiteral extends SyntaxForm {
    // Runtime representation: basecell -> rvcc -> litc on d.1 posward
    private final class Rep {
        public ZZCell rvcc; // pointer to the return value pointer
        public ZZCell rvc; // dereferenced rvcc
        public ZZCell litc; // pointer to literal value

        /** Create from existing structure. */
        public Rep() {
            rvcc = getBaseCell().s("d.1", 1);
            rvc = ZZCursorReal.get(rvcc);
            litc = rvcc.s("d.1", 1);
        }

        /** Create also a new structure. */
        public Rep(ZZCell lit, ZZCell rvc) {
            this.rvc = rvc;
            rvcc = getBaseCell().N("d.1", 1);
            ZZCursorReal.set(rvcc, rvc);
            litc = rvcc.N("d.1", 1);
            ZZCursorReal.set(litc, lit);
        }

        public void delete() {
            rvcc.delete();
            litc.delete();
        }
    }

    public CellLiteral(ZZCell c, Evaluator etor) {
        super(c, etor);
    }

    public CellLiteral(ZZCell expr, ZZCell rvc, Evaluator etor) {
        super(SyntaxForm.newRT(rvc.getSpace(), this.getClass()), etor);
        Rep rep(ZZCursorReal.get(expr), rvc);
    }

    public void evalIteration() {
        Rep rep;
        ZZCursorReal.set(rep.rvc, ZZCursorReal.get(rep.litc));
        finishEval();
    }

    protected void delete() {
        Rep rep;
        rep.delete();
    }
}
