/*
Params.java
*    
*    Copyright (c) 2001, Benja Fallenstein
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

package org.gzigzag.client;
import java.util.*;
import org.gzigzag.impl.*;
import org.gzigzag.*;
import org.gzigzag.util.*;

/** Static methods to read parameters of an object.
 */

public class Params {
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.out.println(s); }

    
    // THE DIMENSIONS

    private static Cell d1 = Client.d1;
    private static Cell d2 = Client.d2;



    // THE METHODS
    
    /** Get a parameter of some cell.
     *  Poswards on c, there's supposed to be a list of clones of parameter
     *  types. When a clone of param is found, the cell one step poswards on
     *  d.1 is returned. Returns null if no value is found.
     */
    public static Cell getParam(Cell c, Cell param) {
        return getParam(c,param,null);
    }
    public static Cell getParam(Cell c, Cell param, Obs o) {
        param = param.getRootclone(o);
        for(Cell d=c.s(d2,o); d!=null && !d.equals(c); d=d.s(d2,o))
            if(Id.equals(d.getRootclone(o), param))
                return d.s(d1,o);
        return null;
    }
    public static Cell getNextParam(Cell c) { return getNextParam(c,null); }
    public static Cell getNextParam(Cell c, Obs o) {
        c = c.s(d1,-1,o);
        return getParam(c, c, o);
    }
    public static Cell[] getCells(Cell start, Cell dim,
                                  int nmax, Obs obs) {
        LinkedList l = new LinkedList();
        int i = 0;
        Cell orig = start;
        for(; i<nmax; i++) {
            l.add(start);
            start = start.s(dim, 1, obs);
            if(start == null || start.equals(orig)) break;
        }
        return (Cell[])(l.toArray(new Cell[0]));
    }
    // XXX Should use getCells()

    /** Get a list of strings in cells starting from start (INCLUSIVE).
     */
    public static String[] getStrings(Cell start, Cell dim,
				      int nmax, Obs obs) {
        String[] r = new String[nmax];
        int i=0;
        for(; i<nmax; i++) {
            if(i!=0) {
                start = start.s(dim, 1, obs);
                if(start == null) break;
            }
            r[i] = start.t(obs);
        }
        if(i == nmax) return r;
        String[] r2 = new String[i];
        System.arraycopy(r, 0, r2, 0, i);
        return r2;
    }

    /** Get a list of integers in cells starting from start (INCLUSIVE).
     */
    public static int[] getInts(Cell start, Cell dim,
				int nmax, Obs obs) {
        String[] s = getStrings(start, dim, nmax, obs);
        int[] r = new int[s.length];
        for(int i=0; i<r.length; i++) {
            try {
                r[i] = Integer.parseInt(s[i]);
            } catch (NumberFormatException e) {
                pa(s[i]);
                //              e.printStackTrace();
                //              System.err.println(e);
                r[i] = 0;
            }
        }
        return r;
    }
}
