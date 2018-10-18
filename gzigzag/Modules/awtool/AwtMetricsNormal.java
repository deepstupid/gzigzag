/*   
AwtMetricsNormal.java
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
 * Written by Kimmo Wideroos
 */
 
package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;

/** Default metrics: Euclidean, no focus+context (no scaling)
 */


public class AwtMetricsNormal extends AwtMetrics {
    public String getZObName() { 
        return "Normal(Euclidean)";
    }

    public double[] transformAtFocusNbhood(double x, double y, double focusCoeff) {
        return new double[] { x, y };
    }

    public double[] inverseTransformAtFocusNbhood(double x, double y, double focusCoeff) {
        return new double[] { x, y };
    }

    public double transformScalingValue(double s) { return 1.0; }

    public double focusCoefficientMinimum() { return 1.0; }
    public double focusCoefficientMaximum() { return 1.0; }

    public AwtMetricsNormal() { super(); }

    public AwtMetricsNormal(ZZCell start, Dimension rv, Dimension vv, 
                            double ox, double oy) { 
        super(start, rv, vv, ox, oy);
    }
}
 
