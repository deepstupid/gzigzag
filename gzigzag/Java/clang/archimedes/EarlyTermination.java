/*   
EarlyTermination.java
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

/** An Archimedes object to represent early terminations of execution.
 *  Early termination is the Archimedes way to implement both control flow
 *  statements like break, and exception handling. With exception handling,
 *  the "early termination" thing is probably clear-- it works just like
 *  exceptions work in Java (except that in Archimedes, you don't have throws
 *  clauses built into the language). The interesting thing is that the control
 *  flow statements work the same way-- they "bubble up" in the expression
 *  tree until some expression catches them (a loop in the case of break or
 *  continue). Currently all early terminations are implemented using Java
 *  Throwables, which is of course an abuse, but consider it this way: an
 *  uncaught early termination (e.g. a break without a surrounding loop)
 *  <em>is</em> an error, so when you actually see such an error on the user
 *  interface, it is an error proper. (The error message should probably be
 *  phrased accordingly, i.e. "Uncaught early termination X").
 *  <p>
 *  <b>Semantics.</b> If an early termination occurs inside an expression, and
 *  that expression does not catch and handle this specific type of early
 *  termination, the expression has to terminate evaluation immediately and
 *  return control to the next higher expression, not returning a return value,
 *  but passing the early termination. (Note that the equivalent of try/finally
 *  statements is a special case of an early termination handler-- it does
 *  something, then invokes (re-raises, re-throws) the early termination again.)
 *  <p>
 *  <b>Function declarations.</b> Early terminations do by default cross
 *  the line of function definitions. For example, if function A calls function
 *  B, and in function B, an early termination occurs, say a break statement,
 *  and if in function B, that that early termination is not catched, then
 *  the early termination raises to function A. This is exactly the right thing
 *  for exceptions; but if the early termination is a break statement, and
 *  there is no surrounding loop in B, we usually consider this a syntax error;
 *  by no means do we want a loop that, by chance, surrounds the call to B in
 *  A, to be aborted. However, that's not always the case: most specifically,
 *  the break() statement itself may be a function (it's an Archimedes
 *  principle that functions declared in the space should have the same
 *  "privileges" like primitives, e.g. raising early terminations). Now, of
 *  course, it's <em>not</em> a syntax error if there is no loop to be broken
 *  inside break() itself!
 *  <p>
 *  The plan is that later, when we have more things sublanguages can specify,
 *  one of them is which kinds of early terminations are made into errors when
 *  leaving the space of a function. Another issue involved here is that
 *  e.g. loop statements (which can be functions declarated in the space)
 *  do callbacks for the "blocks" they contain, and early terminations raised
 *  in these blocks, ones which do not concern the loop in question, should
 *  bubble up outside the loop, because the loop returns control to the
 *  function in which the early termination was raised; and it <em>should</em>
 *  continue to bubble up <em>inside that very function</em> (otherwise it
 *  wouldn't be useful at all).
 *  <p>
 *  Yet another issue are return statements, which are early terminations in
 *  their own right, but aren't catched by any expression in the tree in
 *  particular, but rather when at the crossing line between the topmost
 *  expression inside the function, and the expression calling the function.
 *  <p>
 *  A possibly very good solution would be to make sublanguages attached to
 *  a function have as one of their properties an "even more top-level"
 *  callable (function or primitive), that is the real top-level callable
 *  of each function declaration using that sublanguage; it would get a chance
 *  to catch early terminations <em>after</em> the top-level callable connected
 *  to the function definition, and it would then get a chance to return
 *  something (if it doesn't re-raise the early termination). That would also
 *  make building type-checking atop of Archimedes more easy even if return
 *  statements are the ones really returning the data, and the top-level
 *  expression is just a block (execution) expression that always returns null.
 *  <p>
 *  Note: Currently, no traceback information is associated with early
 *  terminations. This shall change later on.
 */

public class EarlyTermination extends ZZError {
String rcsid = "$Id: EarlyTermination.java,v 1.2 2001/04/19 20:39:17 bfallenstein Exp $";

    public final ZZCell cell;

    public EarlyTermination(ZZCell cell) {
	super(cell.t());
	this.cell = cell;
    }

}


