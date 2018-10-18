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

class Task {
    private ZZCell cell;

    public Environment getCurrentEnvironment() {
        return new Environment(ZZCursorReal.get(cell));
    }

    public void setCurrentEnvironment(Environment e) {
        ZZCursorReal.set(cell, e.getCell());
    }

    public void push(ZZCell c) {
        ZZCell ct = peek();
        if (ct != null) {
            c.connect(DimensionNames.stack, 1, ct);
            cell.disconnect(DimensionNames.stackptr, 1);
        }
        cell.connect(DimensionNames.stackptr, 1, c);
    }

    public void pop() {
        ZZCell ct = peek();
        if (ct == null) throw ZZError("empty Thales Clang runtime stack");
        ZZCell nt = ct.s(DimensionNames.stack, 1);
        cell.disconnect(DimensionNames.stackptr, 1);
        if (nt != null) cell.connect(DimensionNames.stackptr, 1, nt);
        nt.disconnect(DimensionNames.stack, -1);
    }

    public ZZCell peek() {
        return cell.s(DimensionNames.stackptr, 1);
    }

    public void remove(ZZCell c) {
        {
            ZZCell nei = c.s(DimensionNames.stackptr, -1);
            if (nei.equals(cell)) {
                pop();
                return;
            }
        }

        for (ZZCell i = peek(); i != null && !i.equals(c);
             i = i.s(DimensionNames.stack, 1)) {
            // nothing
        }
        if (i == null) throw ZZError("cannot remove that which is not there");

        c.deleteFromRank(DimensionNames.stack);
    }

    public boolean empty(ZZCell c) {
        return peek() == null;
    }

}
