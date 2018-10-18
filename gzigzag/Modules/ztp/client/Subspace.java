/*   
Put.java
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
 * Written by Antti-Juhani Kaijanaho
 */

package org.gzigzag.ztp.client;
import org.gzigzag.ztp.*;
import org.gzigzag.*;
import java.util.*;

class Subspace {

    private String[] sdims;
    private Hashtable cells = new Hashtable();

    public String[] getSoftDims() { return sdims; }
    public Enumeration getCells() { return cells.keys(); }

    public Subspace(ZZCell base) {
        String[] origdims = base.getSpace().dims();
        Vector dimv = new Vector();
        
        for (int i = 0; i < origdims.length; i++) {
            if (origdims[i].equals("d.ztp-subspace")
                || origdims[i].equals("d.cellcreation")) {
                continue;
            }
            dimv.addElement(origdims[i]);
        }
        
        sdims = new String[dimv.size()];
        for (int i = 0; i < sdims.length; i++) {
            sdims[i] = (String)dimv.elementAt(i);
        }

        if (base == null) return;
        
        Stack stack = new Stack();
        stack.push(base);
        Hashtable seen = new Hashtable();
        while (!stack.empty()) {
            ZZCell c = (ZZCell)stack.pop();
            
            for (int i = 0; i < sdims.length; i++) {
                ZZCell d = c.s(sdims[i], 1);
                if (d != null && !seen.containsKey(d)) {
                    stack.push(d);
                    seen.put(d, d);
                }
                d = c.s(sdims[i], -1);
                if (d != null && !seen.containsKey(d)) {
                    stack.push(d);
                    seen.put(d, d);
                }
            }
            cells.put(c, c);
        }
    }
        
}
