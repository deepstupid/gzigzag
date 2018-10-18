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

/** Focus+context metrics1
 */


public class AwtMetricsFC extends AwtMetrics {
    public String getZObName() { 
        return "Focus+Context";
    }

    public double[] transformAtFocusNbhood(double x, double y, double focusCoeff) {
        double dx, dy, x2, y2;

        x2 = x*focusCoeff; 
        y2 = y*focusCoeff;

        if(x2<1 && x2>-1) {
            dx = x2; 
        } else {
            dx = x2*x2; 
            dx = x2>0 ? dx : -dx;
        }

        if(y2<1 && y2>-1) { 
            dy = y2;
        } else {
            dy = y2*y2; 
            dy = y2>0 ? dy : -dy;
        }

        return new double[] { dx/focusCoeff, dy/focusCoeff };
    }

    public double[] inverseTransformAtFocusNbhood(double x, double y, double focusCoeff) {
        double dx, dy, x2, y2;

        x2 = x*focusCoeff;
        y2 = y*focusCoeff;

        if(x2<1 && x2>-1) {
            dx = x2; 
        } else {
            dx = (x2>0) ? Math.sqrt(x2) : -Math.sqrt(-x2);
        }

        if(y2<1 && y2>-1) { 
            dy = y2;
        } else {
            dy = (y2>0) ? Math.sqrt(y2) : -Math.sqrt(-y2);
        }

        return new double[] { dx/focusCoeff, dy/focusCoeff };
    }

    public double transformScalingValue(double s) { return 1.0/s; }

    public double focusCoefficientMinimum() { return 4.0; }
    public double focusCoefficientMaximum() { return 8.0; }

    public AwtMetricsFC() { super(); }

    public AwtMetricsFC(ZZCell start, Dimension rv, Dimension vv, 
                            double ox, double oy) { 
        super(start, rv, vv, ox, oy);
    }
}
 
