/*   
ObsTrigger.java
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

package org.gzigzag;

/** An interface for attaching and triggering Obses.
 * Basically, each observer (Obs) may observe several 
 * combinations of (Object, String) pairs. The Objects are
 * expected to have '==' equality, generally they will be Dims.
 * <p>
 * This interface is meant for synchronous use: 
 * no Obs.chg() methods are called before 
 * @see Obs
 */

public interface ObsTrigger {
String rcsid = "$Id: ObsTrigger.java,v 1.3 2001/07/08 21:25:12 tjl Exp $";

    
    /** Add an observer.
     */
    void addObs(Obs o, Object obj, Object code);

    /** Remove all observations that the given observer is making.
     */
    void rmObs(Obs o);

    /** Signal that the observers for the given pair should
     * be triggered.
     * All observers that are triggered are then removed from 
     * further processing.
     */
    void chg(Object obj, Object code);

    /** Call the changed observers.
     */
    void callQueued();
    
}
