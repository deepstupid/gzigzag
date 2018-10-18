/*   
Complex.java
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

package org.gzigzag.map;

import java.util.*;

// under construction, not tested yet...

public class Complex {
    protected static final float pi2 =  (float)(2.0 * Math.PI);

    private float r = (float)0.0; // real part
    private float i = (float)0.0; // imaginary part

    // constructors
    public Complex() {
        this((float)0.0, (float)0.0);
    }

    public Complex(float r) {
        this(r, (float)0.0);
    }

    public Complex(float r, float i) {
        this.r = r;
        this.i = i;
    }

    public Complex(Complex z) {
        this.r = z.r;
        this.i = z.i;
    }

    public void set(float r) {
	this.r = r;
	this.i = (float)0.0;
    }

    public void set(float r, float i) {
	this.r = r;
	this.i = i;
    }

    public void set(Complex z) {
	r = z.r;
	i = z.i;
    }

    // infinity test
    public boolean isInfinite () {
        return (Float.isInfinite(r) || Float.isInfinite(i));
    }

    // is not a number -test
    public boolean isNaN() {
        return (Float.isNaN(r) || Float.isNaN(i));
    }

    // get real part
    public float r() { return r; }

    // get the other one
    public float i() { return  i; }

    // abs
    public float abs() { return (float)Math.sqrt(r*r+i*i); }

    // polar / theta
    public float arg() { return (float)Math.atan2(i, r); }

    // complex conjugate
    public void conj() { i = -i; }

    // multiply by z
    public void mul(Complex z) {
	float old_r = r;
        r = r*z.r - i*z.i;
        i = old_r*z.i + i*z.r;
    }

    // multiply by z
    public void mul(float r, float i) {
	float old_r = this.r;
        this.r = this.r*r - this.i*i;
        this.i = old_r*i + this.i*r;
    }

    // multibly by scalar
    public void mul(float s) {
        r *= s;
        i *= s;
    }

    // add complex z
    public void add(Complex z) {
        r += z.r;
        i += z.i;
    }

    // add scalar
    public void add(float r) {
        this.r += r;
    }

    // substract z
    public void sub(Complex z) {
        r -= z.r;
        i -= z.i;
    }

    // sub scalar
    public void sub(float r) {
        this.r -= r;
    }

    // divide by complex z
    public void div(Complex z) {
        float l2 = z.r*z.r + z.i*z.i;
	float old_r = r;
        r = r*z.r/l2 + i*z.i/l2;
        i = -old_r*z.i/l2 + i*z.r/l2;
    }

    // divide by scalar
    public void div(float s) {
        r /= s;
        i /= s;
    }

    public void neg() {
	r = -r;
	i = -i;
    }

    public void log() {
	float r_new = (float)Math.log(abs());
	i = arg(); // uses r...
	r = r_new;
    }

    public boolean near(Complex z, float tol) {
	if(r-tol < z.r && z.r < r+tol && i-tol < z.i && z.i < i+tol)
	    return true;
	return false;
    }

    // guess what
    public String toString () {
        return "( "+r+", "+i+" )";
    }

}



