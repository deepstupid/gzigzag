/*   
AbstractStepper.java
 *    
 *    Copyright (c) 2002, Benja Fallenstein
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

package org.gzigzag;

/** An abstract implementation of the Stepper interface.
 */

public abstract class AbstractStepper implements Stepper {
String rcsid = "$Id: AbstractStepper.java,v 1.1 2002/03/09 10:55:25 bfallenstein Exp $";

    public boolean h(StepperDim d) {
	return h(d, -1);
    }

    public int hashCode() {
	throw new UnsupportedOperationException("Steppers cannot be hashed.");
    }

    public Object clone() {
	return cloneStepper();
    }

    /** This method is implemented by calling Object's clone().
     *  If subclasses want to use their own implementation of cloning,
     *  they should override this method and not Stepper.clone(), which
     *  is implemented in terms of this method.
     */
    public Stepper cloneStepper() {
	try {
	    return (Stepper)super.clone();
	} catch(CloneNotSupportedException e) {
	    throw new Error("HELP-- clone not supported on Stepper!");
	}
    }

}
