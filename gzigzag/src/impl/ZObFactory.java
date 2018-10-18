/*
ZObFactory.java
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
 * Written by Tero Mäyränen
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.impl.zob.*;
import java.lang.reflect.*;

/**
 * ZObFactory
 */

public class ZObFactory implements Function {
public static final String rcsid = "$Id: ZObFactory.java,v 1.2 2002/03/26 16:18:40 deetsay Exp $";

    private static DirectStepperDim ZObTypeDim = new DirectStepperDim(Dims.d_zob_type_id.space.getDim(Dims.d_zob_type_id));

    private static StepperDim d_1 = new DirectStepperDim(Dims.d_user_1_id.space.getDim(Dims.d_user_1_id));
    private static StepperDim d_2 = new DirectStepperDim(Dims.d_user_2_id.space.getDim(Dims.d_user_2_id));

    private static ZObFactory zobFactorySingleton = new ZObFactory();
    public static ZObFactory getFactory() { return zobFactorySingleton; }

    public Object findMember(Stepper s, String id) {
	Stepper member = s.cloneStepper();
	s.s(d_1,1);
	Stepper first = s.cloneStepper();
	while (s.s(d_2,1) && !s.equals(first)) {
	    s.s(d_1,1);
	    if (((Cell)s.getImmutable()).id == id) {
		s.s(d_1,1);
		member.rootClone();
		ZObMember m = (ZObMember)
		    (new ZOb_0000000008000000EC4D544501000466355872D0E82A85FD795CAADD5175D4571B364AB1B81AD7_7()).apply(member);
		//
		String mtype = m.type;
		// If a type is set for this member
		if (!mtype.equals("")) {
		    try {
			return Class.forName(mtype).getConstructor(
			    new Class[] { Class.forName("java.lang.String") } ).newInstance(new Object[] { s.t() });
		    }
		    catch (Exception e) {
			return null;
		    }
		}
		// If a type wasn't set, then the default type is Stepper
		// ZObFactory.apply() can be used to get a ZOb from the Stepper
		else {
//		    return apply(s);	don't apply automatically
		    return s;
		}
	    }
	    else {
		s.s(d_1,-1);
	    }
	}
	return null;
    }

    public Object apply(Stepper s) {
	Stepper zobType = s.cloneStepper();
	zobType.rootClone();
	zobType.h(ZObTypeDim);

	String id = ((Cell)zobType.getImmutable()).id;	// XXX temporary? way to get ID from Stepper
	try {
	    return ((org.gzigzag.Function)Class.forName("org.gzigzag.impl.zob.ZOb-" + id).newInstance()).apply(s);
	}
	catch (Throwable e) {
	    return null;
	}
    }
}
