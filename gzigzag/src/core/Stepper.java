/*   
Stepper.java
 *    
 *    Copyright (c) 1999-2001, Ted Nelson and Tuomas Lukka
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

package org.gzigzag;

/** A read-only stepper. Steppers are a different alternative to the current Cell API,
 * and might be easier to use in the long run. Benefits:
 * 1) More efficient to wrap i.e. put a virtual stepper around a given stepper.
 * 2) Easier to put in several different views to a space: e.g.
 * 	- usual view
 * 	- a view that records all cells stepped through and puts an observer on them
 * 	  so that when one changes, a given function is called.
 * 3) Easier to compile to more efficient code using a preprocessor
 * <p>
 * The idea is that the front-end of GZigZag would be completely implemented 
 * using Steppers.
 * <p>
 * Steppers can be singular, i.e. null, not pointing anywhere.
 * <p>
 * We shall make the following restrictions on steppers in order to 
 * maintain code clarity:
 *
 * <UL>
 *	<LI><P>A stepper passed as a parameter shall not be modified.</P>
 *          <p>(Benja:) What does this mean? That we may not move it?!?
 *          Or that, when we finish, we have to move it back to 
 *          where we started?</p>
 *	<LI><P>Steppers shall not be assigned objects' fields. Even new
 *          steppers created by <code>clone()</code> may only be assigned
 *          to local variables:</P>
 * </UL>
 * <pre>
 * 	int f(Stepper s0) {
 * 		Stepper s = s0.clone();
 *		s.s(d1);
 *		Stepper s1 = s.clone();
 *		...
 * 	}
 * </pre>
 * <p>These restrictions are a little strange for Java but the point is
 * to emphasize the functional aspects and obtain referential
 * transparency.
 */

public interface Stepper extends Cloneable {
String rcsid = "$Id: Stepper.java,v 1.6 2002/02/08 07:57:12 tjl Exp $";

    /** Step along the given dimension.
     * @return Whether the operation was successful or not.
     * If not successful, the stepper is moved as far as possible.
     * <p>
     * <b>Benja:</b> This means that if this was not successful, we don't
     * know where we are: if a call to <code>s(d, 4)</code> returned false,
     * have we moved 0, 1, 2 or 3 steps? -- I think it would be better
     * if the stepper didn't move when there's no cell there...
     * <p>
     * <b>Tjl:</b> Yes, but in a situation where that matters, you can step
     * one step at a time. The point is that most often, <i>steps</i> will 
     * be either 1 or -1.
     * <p>
     * <b>Benja:</b> I don't see the point; in that case, my proposed
     * semantics would <em>exactly</em> match the semantics given above.
     * And if you have to move one step at a time (almost) always, why
     * bother allowing abs(steps)&gt;1 in the first place?
     */
    boolean s(StepperDim d, int steps);

    // Benja: I think we should also have a boolean function that does not
    // move, but simply tests whether there's something in a specific place.
    // (Currently we do 'cell.s(dim) != null' quite a lot)
    //
    // Tuomas: but isn't that mostly so that we actually *will* move there
    // right afterwards? Let's see code that uses steppers first...
    // If it proves then to be useful, we'll add it.
    //
    // Benja: Fine, let's see. (I was thinking of e.g. checking whether
    // a rank loops. Actually, when using steppers, that should maybe
    // a method in its own right-- stuff like this does not warrant the
    // hazzle to move to the beginning of a rank and then manually 
    // *move back*, or, alternatively, create a new stepper object.)

    /** Go to the headcell.
     * @return true if the stepper was moved, i.e. we were not already on 
     * 	the headcell.
     */
    boolean h(StepperDim d, int dir);
    boolean h(StepperDim d);

    /** Return the text in the current cell.
     * May return null.
     */
    String t();

    /** Return true if this stepper is singular, i.e.it doesn't
     * point to a real cell.
     */
    boolean isNull();

    /** Return the (single) span in the current cell.
     * May return null.
     */
    Span getSpan(Cell c);

    StepperDim asDim();
    StepperDim getDim(String s);

    /** Go to the root clone of the current cell.
     */
    boolean rootClone();

    /** Check whether two steppers are on the same cell.
     * <p>
     * <b>Benja:</b>
     * The specification of <code>equals()</code> (in JDK) says it's
     * supposed to be "consistent:"
     * <blockquote>
     * </blockquote>
     * I think we should not override <code>equals()</code>, and instead
     * have a different method that tests whether two steppers are on the
     * same cell (this would also be shorter to write).
     */
    boolean equals(Object o);

    /** Steppers may not be hashed: the hashcode routine must throw an error.
     * Putting steppers in as hash keys mustn't be done because
     * they are mutable. Use the getImmutable() method.
     * <p>
     * <b>Benja:</b>
     * Of course, if we didn't use <code>equals()</code> for comparing
     * steppers' positions, implementors wouldn't have to worry about this,
     * because then, steppers would be hashed like all mutable objects:
     * by object reference.
     */
    int hashCode();

    /** Return an immutable object that represents the cell this stepper
     * is on. This object can be used as a hash key: it must have reasonable 
     * hashCode() and equals() functions.
     * <p>
     * The returned objects need not be canonical, i.e., it is not necessary that
     * == would work for comparison.
     */
    Object getImmutable();

    Stepper cloneStepper();

}
