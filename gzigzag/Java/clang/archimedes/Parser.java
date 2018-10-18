/*   
Parser.java
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

/** A simple preliminary parser for the Archimedes Procedural Layer.
 *  Parses arguments connected to the function on d.1.
 */

public class Parser {
String rcsid = "$Id: Parser.java,v 1.4 2001/04/15 11:33:57 bfallenstein Exp $";

    private static final void pa(String s) { System.out.println(s); }

    /** Parse the values in the expression into the variables in the function.
     *  This is call-by-value.
     *  @param pattern The function definition to parse against.
     *  @param toParse The expression to parse.
     *  @param oldContext The namespace of the expression (to pass to the evaluator).
     *  @param newContext The namespace of the function (to put values into).
     */
    public static void parse(ZZCell pattern, ZZCell toParse,
			     Namespace oldContext, Namespace newContext) {
	parseSide(pattern, toParse, oldContext, newContext, -1);
	parseSide(pattern, toParse, oldContext, newContext, 1);
    }

    /** Parse in one direction. */
    private static void parseSide(ZZCell pattern, ZZCell toParse, 
				  Namespace oldContext, Namespace newContext, 
				  int dir) {
	
	/** The current cell in the pattern. */
	ZZCell pc = pattern.s("d.1", dir);

	/** The current cell in what we parse. */
	ZZCell tc = toParse.s("d.1", dir);

	/** Move through pattern and toParse simultaneously.
	 *  At every iteration, we have a pair of cells in pattern and toParse.
	 *  The toParse cell is evaluated and put into the variable referenced
	 *  by the pattern cell.
	 */
	while(pc != null) {
	    if(tc == null)
		throw new MissingTermError("When parsing "+toParse+" "+
		    "against pattern "+pattern+", for the parameter "+pc+
		    ", no value was found.");
	    if(pc.equals(pattern))
		throw new SyntaxError("Looping d.1 rank in pattern "+pattern);
	    ZZCell value = Archimedes.getValue(tc, oldContext);
	    
	    Thunk t = new Thunk(tc.getSpace(), value);
	    newContext.put(pc, t);

	    pc = pc.s("d.1", dir); tc = tc.s("d.1", dir);
        }
    }

}