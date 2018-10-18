/*   
ZZClangContext.java
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

/** A context object for Clang.
 * Mostly corresponds to a viewspecs: has a viewCell, 
 * a cursor and other stuff.
 *
 * <p>
 * However, may also be cloned with a new cursor cell as a subcontext.
 * The subcontext is then totally independent of the parent, but starts
 * out with the same values if null.
 * NOT YET FINISHED! 
 * <p>
 * NOTE!!!!!!! getViewCell's viewcell may not be up to date with the ZZCursor
 * operations, if code is compiled to pure java for speed. Sync op?
 */

public interface ZZClangContext {
String rcsid = "$Id: ZZClangContext.java,v 1.3 2000/09/19 10:31:58 ajk Exp $";

    /** Obtain a reference to the main cursor of this context.
     * Ideally, 
     * <pre>
     *  clangcontext.getCursor() == ZZCursorReal(clangcontext.getViewcell())
     * </pre>
     * but this may be optimized away, especially for subcontexts, since
     * the cursor is used a *lot*.
     */
    ZZCursor getCursor();
    /** Obtain a reference to the main viewcell of this context.
     */
    ZZCell getViewspex();

    /** Get a ZZCell that gives the cell that the given parameter
     * ultimately refers to. For example, the parameter could be
     * a variable in which case this routine returns the cell that
     * the variable is set to. Alternatively, the parameter could
     * have an expression using cells in which case the result of
     * the evaluated expression is given.
     * For instance, if the given param is
     * @param param The cell that represents the parameter in the clang script.
     * @return The actual cell that the given parameter means in this
     *		context.
     */
    ZZCell paramAsCell(ZZCell param);

    /** Get a cursor that represents the variable in the given parameter.
     * @param param As for paramAsCell above.
     * @return A cursor representing the given variable, or null
     *		if the param is not a variable.
     */
    ZZCursor paramAsCursor(ZZCell param);

    /** Execute the given parameter cell.
     * Used to implement routines such as if, case and loops.
     * The code given is executed as if on the main level, so all changes
     * to the cursor are reflected back after the code.
     * @param param As for paramAsCell above.
     */
    void execParam(ZZCell param);

    /** Break encapsulation.
     * Obtain a special context thingy given to the implementation.
     * Nasty optimization hack.
     * XXX This should later be done with transient cells.
     */
    Object getSpecial(Object which);

//     ZZClangContext getSubcontext(ZZCell startCursor);
}


