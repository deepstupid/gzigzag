/*   
CellStepper.java
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

package org.gzigzag.impl;
import org.gzigzag.*;

/** A direct implementation of steppers in terms of Cell objects - abstract
 * base class.
 */

public abstract class CellStepper implements Stepper {

    protected Cell c;

    public CellStepper(Cell c) { this.c = c; }

    public void set(Cell c) { this.c = c; }

    public boolean isNull() { return c!=null; }

    public boolean h(StepperDim d) { return h(d, -1); }

    public StepperDim asDim() {
	if(c == null) return null;
	return new DirectStepperDim(c.space.getDim(c));
    }
    public StepperDim getDim(String s) {
	if(c == null) return null;
	return new DirectStepperDim(c.space.getDim(s));
    }

    /** Equality is defined simply on whether we point to the same
     * cell, nothing else.
     */
    public boolean equals(Object o) {
	CellStepper other = (CellStepper) o;
	if(c == null)
	    return other.c == null;
	return c == other.c || c.equals(other.c);
    }
    public int hashCode() {
	if(c == null) return 0;
	return c.hashCode();
    }

}
