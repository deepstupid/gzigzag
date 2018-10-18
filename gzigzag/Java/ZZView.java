/*   
ZZUpdateManager.java
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
 * Written by Tuomas Lukka
 */
package org.gzigzag;
import java.util.*;

/** A single view on the structure.
 * This class is mostly to work with ZZUpdateManager which tells these
 * classes to repaint themselves in response to changes in the structure
 * (expose events are separate and handled inside the classes themselves).
 * <p>
 * In event routines, passed instead of cells for update purposes
 * (asking ZZUpdateManager to make this view the fast-updated one).
 * Once ZZWindows are finished, should be replaced with ZZCell of
 * a ZZWindow.
 * <p>
 * Expect a parameter "complexity" to be added to reraster and paintNow
 * in the future in order for the ZZUpdateManager to be able to control
 * the frame rate.
 */
public interface ZZView {
String rcsid = "$Id: ZZView.java,v 1.18 2000/09/19 10:31:58 ajk Exp $";
    /** Called to enquire whether this view needs an update, given
     * that certain cells have been changed.
     * NOT YET USED
     */
    // public boolean needUpdate(ZZCell[] changed); 
	    // XXX dimensions of chgd cells!

    /** Recreate the view in memory (raster the cells etc).
     * @return true if it would be useful for the view to animate 
     * 		to the next view.
     */
    boolean reraster();
    /** Called to tell that this view should paint itself right now.
     * @param  fraction  The fraction between the old and the new views
     *			the animation should be at.
     */
    void paintNow(float fraction); 

    /** Get the cell in the structure representing this view.
     */
    ZZCell getViewcell();
}
