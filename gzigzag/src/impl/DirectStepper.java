/*   
DirectStepper.java
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

/** A direct implementation of steppers in terms of Cell objects, with or
 * without observing.
 */

public class DirectStepper extends CellStepper {

    final Obs obs;

    public DirectStepper(Cell c) { super(c); this.obs = null; }
    public DirectStepper(Cell c, Obs obs) { super(c); this.obs = obs; }

    public boolean s(StepperDim d0, int steps) {
	if(c== null) return false;
	Dim d = ((DirectStepperDim)d0).dim;
	Cell n = d.s(c, steps, obs);
	if(n == null) return false;
	c = n;
	return true;
    }
    public boolean h(StepperDim d0, int dir) {
	if(c== null) return false;
	Dim d = ((DirectStepperDim)d0).dim;
	Cell n = d.h(c, dir, obs);
	if(c.equals(n)) return false;
	c = n;
	return true;
    }
    public String t() {
	if(c == null) return null;
	return c.t(obs);
    }
    public Span getSpan(Cell c) {
	if(c == null) return null;
	return c.getSpan(obs);
    }

    public boolean rootClone() {
	if(c == null) return false;
	Cell n = c.getRootclone(obs);
	if(c.equals(n)) return false;
	c = n;
	return true;
    }

    public Object getImmutable() { return c; }

    public Obs getObs() { return obs; }

    public Stepper cloneStepper() {
	return new DirectStepper(c, obs);
    }
}


