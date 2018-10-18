/*   
ParamTemplate.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
 *    Copyright (c) 2001, Benja Fallenstein
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
 * Written by Benja Fallenstein
 */
package org.gzigzag.clang;
import org.gzigzag.*;
import java.awt.*;

/** A class providing static methods to deal with the templating of parameters.
 *  A callable has, attached to its rootclone, a template of the parameters
 *  it accepts. An expression calling that callable has the actual parameters
 *  attached to it. The present class is used to find the parameter cells
 *  in expressions corresponding to a specific parameter cell in the template,
 *  and to find parameter cells in the template corresponding to a specific
 *  parameter cell in an expression.
 *  <p>
 *  Looking at the current implementation, these may seem to be too simple to
 *  create a whole class for them. However, consider that this task will be
 *  much harder once the real parsers are in place, i.e. once parameters can
 *  be connected to the root in other ways than in fixed positions on a d.1
 *  rank.
 */

public class ParamTemplate {
String rcsid = "$Id: ParamTemplate.java,v 1.3 2001/04/21 03:01:59 bfallenstein Exp $";
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    /** Take a parameter cell in a root and search the corresponding
     ** parameter cell in an expression calling that root.
     */
    public static ZZCell rootToExpression(ZZCell par, Expression e) {
	int dist = ZZUtil.getDistance(e.main.getRootclone(), par, "d.1");
	return e.getParam("d.1", dist);
    }

    /** Take a parameter cell in an expression and search the corresponding
     ** parameter cell in the root.
     */
    public static ZZCell expressionToRoot(ZZCell par, Expression e) {
	int dist = ZZUtil.getDistance(e.main, par, "d.1");
	ZZCell root = e.main.getRootclone();
	return root.s("d.1", dist);
    }

    /** Take an expression, and a cell which is either a parameter cell
     *  of this expression, or an expression connected on d.expression to
     *  a parameter cell of this expression. In the latter case, find that
     *  parameter first; in any case, as the last step, find the parameter
     *  cell in the root corresponding to the given parameter cell in the
     *  expression.
     */
    public static ZZCell superExpressionToRoot(ZZCell par, Expression e) {
	ZZCell isect = e.main.intersect("d.1", 1, par, "d.expression", 1);
	if(isect == null)
	    isect = e.main.intersect("d.1", -1, par, "d.expression", 1);
	if(isect == null)
	    throw new ZZError("Strange ordering of cells; cannot find the "
			    + "root param for this expression param. exp: "
			    + e.main + ", param: " + par);
	return expressionToRoot(isect, e);
    }

}