/*   
ThalesClang.java
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

package org.gzigzag.module;

import org.gzigzag.*;
import java.awt.*;
import org.gzigzag.clang.thales.*;

public class ThalesClang {
    static public ZZModule module = new ZZModule() {
            public void action(String id,
                               ZZCell code, 
                               ZZCell target,
                               ZZView view, ZZView cview,
                               String key, Point pt, ZZScene xi) {
                ZZSpace space = code.getSpace();
                if (id.equals("RELOAD")) {
                    Primitive.readPrimitivesFromStructure(code.getSpace());
                } else if (id.equals("INITIALIZE")) {
                    Primitive.register(space, "If");
                } else {
                    throw new ZZError("unknown ThalesClang module command");
                }
            }
            
        };
}
