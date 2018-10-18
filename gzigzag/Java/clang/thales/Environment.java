/*   
Task.java
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
 * Written by Antti-Juhani Kaijanaho.
 */


package org.gzigzag.clang.thales.*;

import org.gzigzag.*;

/** A runtime environment.  It contains runtime instances of cell
    denoters.  Remember to register/unregister references (that are in
    the structure (not Java references)). */
class Environment implements Refcounted {
    private ZZCell cell;

    public Environment(ZZCell cell) {
        this.cell = cell.h(DimensionNames.environment);
    }

    public Environment(ZZCell firstparam, Environment parent) {
        cell = firstparam.N(DimensionNames.activations, 1);
        ZZCell prev = cell;
        for (ZZCell c = cell.s(DimensionNames.argument, 1);
             c != null;
             c = c.s(DimensionNames.argument, 1)) {
            ZZCell nc = c.N(DimensionNames.activations, 1);
            prev.connect(DimensionNames.environment, 1, nc);
            prev = nc;
        }
        if (parent != null) ZZCursorReal.set(first, parent.getCell());
        cell.setReferenceCount("0");
        parent.registerReference();
    }

    public ZZCell getCell() { return cell; }

    protected int getReferenceCount() {
        String text = cell.getText();
        return Integer.parseInt(text);
    }

    protected void setReferenceCount(int rc) {
        cell.setText("" + rc);
    }

    public void registerReference() {
        setReferenceCount(getReferenceCount() + 1);
    }

    public void unregisterReference() {
        setReferenceCount(getReferenceCount() - 1);
        if (getReferenceCount() < 1) delete();
    }

    public void delete() {
        ZZCell parent = ZZCursorReal.get(cell);
        ZZCell c = cell;
        while (c != null) {
            ZZCell tmp = c;
            c = c.s(DimensionNames.environment, 1);
            tmp.delete();
        }
        if (parent != null) parent.unregisterReference();
    }

    protected ZZCell getDenoterInstance(ZZCell denoter) {
        ZZCell ecell = cell;
        ZZCell rv = null;
        while (rv == null) {
            rv = denoter.intersect(DimensionNames.activations, 1,
                                   ecell, DimensionNames.environment, 1);
            if (rv == null) { 
                ecell = ZZCursorReal.get(ecell);
                if (ecell == null) {
                    throw new ZZError("accessing inaccessible denoter");
                }
            }
        }
        return rv;
    }

    public ZZCell getDenoterValue(ZZCell denoter) {
        ZZCell dc = getDenoterInstance(denoter);
        return ZZCursorReal.get(dc);
    }

    public void setDenoterValue(ZZCell denoter, ZZCell value) {
        ZZCell dc = getDenoterInstance(denoter);
        ZZCursorReal.set(dc, value);
    }

}
