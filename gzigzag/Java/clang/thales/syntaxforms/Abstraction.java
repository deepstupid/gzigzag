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

public class Abstraction extends SyntaxForm {
public static final String rcsid = "$Id: Abstraction.java,v 1.4 2000/10/18 14:35:31 tjl Exp $";

    // Runtime representation basecell -> rvcc -> bodyc -> envc posward on d.1
    private final class Rep {
        public ZZCell rvcc;
        public ZZCell rvc;
        public ZZCell bodyc;
        public ZZCell body;
        public ZZCell envc;      // pointer to inherited environment
        public Environment env;  // dereferenced envc

        /** Create from existing structure. */
        public Rep() {
            rvcc = getBaseCell().s("d.1", 1);
            rvc = ZZCursorReal.get(rvcc);
            bodyc = rvcc.s("d.1", 1);
            body = ZZCursorReal.get(bodyc);
            envc = bodyc.s("d.1", 1);
            env = new Environment(ZZCursorReal.get(envc));
        }

        public Rep(ZZCell body, Environment env, ZZCell rvc

    }
    
    

}
