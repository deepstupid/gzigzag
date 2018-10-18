/*   
ZZClangOp.java
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
package org.gzigzag.clang;
import org.gzigzag.*;
import java.util.*;

/** A primitive operation for Clang.
 * NOT YET FINISHED! 
 */

public interface ZZClangOp {
String rcsid = "$Id: ZZClangOp.java,v 1.3 2000/09/19 10:31:58 ajk Exp $";

    /** Execute the operation.
     * The op is to start at cell c and go poswards on d.1 to find
     * its parameters, with which it can call the ctxt object to find
     * out the values.
     * <p>
     * The cursor crs may or may not be the actual cursor of the viewcell
     * of the context: it may also be given negwards on d.1 from the object
     * to allow ops to act on other cursors easily.
     * @param c The cell that made this routine to be called.
     * @param crs The main cursor
     * @param ctxt The context of the operation.
     * @return The it parameter for the next op.
     */
    void exec(ZZCell c, ZZCursor crs, ZZClangContext ctxt);

    /* Be mentally prepared for something like
     *
     *   void compile(ZZClangCompiler clc, ZZCell c, ZZClangContext ctxt);
     *
     * to appear in the future.
     */

    /** Return the name of this operation.
     */
    String name();
}
