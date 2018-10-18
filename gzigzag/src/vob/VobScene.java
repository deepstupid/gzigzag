/*   
VobScene.java
 *    
 *    Copyright (c) 2000-2001, Ted Nelson and Tuomas Lukka
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
package org.gzigzag.vob;
import java.awt.*;

/** A scene into which Vobs are placed.
 * <p>
 * Some architectural musings:
 * <p>
 * We should separate the classes for the different areas:
 * <ul>
 *  <li> Depth-sorting, storing coordinates, drawing
 *		 - this needs to support cheap hierarchical compositing
 *  <li> Indexing
 *  <li> Matching and animation
 *  <li> Matching and connecting
 * </ul>
 * Right now, VobScene tries, by get() to do some of the indexing.
 * <p>
 * How should the Vob hierarchy and indexing interact?
 * Which of the following queries need to be efficient?
 * <ul>
 *  <li> Get matching vobs in the whole hierarchy
 *  <li> Get matching vobs in a subtree starting at given vob
 *  <li> Get matching vobs in a particular node of the hierarchy
 * </ul>
 * If only the first one is necessary, separation is simpler.
 * Otherwise, the parent has to be usable as a secondary key always...
 * Hmm... giving the parent index might be workable.
 * <p>
 * A scheme using integer arrays:
 * <pre>
 *  int[] nthdepth
 * </pre>
 */

public interface VobScene extends VobPlacer {

    /** Get an enumeration of all keys of vobs in this vobset.
     *  The <code>null</code> "key" is not included.
     */
    java.util.Iterator keys();

    /** Get an enumeration of all vobs.
     */
    java.util.Iterator vobs();

    /** Get the first vob placed into this vobset with the given key.
     */
    Vob get(Object key);

    /** Get the next vob placed into this vobset with the given key.
     *  This is used for requesting multiple vobs with the same key
     *  efficiently, without creating array objects. If <code>prev</code> is
     *  <code>null</code>, <code>get(key)</code> must be returned. If there
     *  are no more vobs, null is returned.
     * <p>
     * XXX This is efficient only when there are few vobs with the same
     * key, otherwise this is at least O(n log n) to get them all
     * (O(n^2) with trivial implementation).
     */
    Vob getNext(Object key, Vob prev);



    /** Get the scene coordinates of the given vob.
     *  In order not to create superfluous objects, the coords are written
     *  into an existing object.
     */
    void getCoords(Vob vob, Vob.Coords writeInto);

    /** Get the Vob that is topmost at the given scene coordinates.
     * XXX During interpolation as well?
     */
    Vob getVobAt(int x, int y);



    /** Render this vobset to the given Graphics object,
     * interpolated.
     * @param g The graphics to draw into
     * @param fg The default foreground color
     * @param bg The background color
     * @param interpTo The vobset towards which to interpolate.
     *			May be null, if fract=0
     * @param fract The fraction by which to interpolate 
     *			towards the other flobset. 0 = this, 1 = other.
     * XXX RenderInfo -like thing here too?
     */
    void render(Graphics g, Color fg, Color bg, VobScene interpTo, float fract);



    /** Create a new VobScene inside this VobScene, possibly
     * surrounded 
     * by a box.
     *  The subvobset will have translated coordinates and a size according to
     *  the <code>x, y, w, h</code> parameters after it is added using
     * VobPlacer.put.
     * <p>
     * <strong>Important:</strong>
     *  No Vobs shall be added to a VobSet between the calls to 
     * 	createSubScene and placing the subScene with put().
     *  Also, no vobs shall be added to a subscene 
     *  after it has been added to the vobset.
     * @param key
     * The key of this subscene.
     * @param style
     *  specifies which kind of box to draw around this vobset.
     * @param w,h
     *  The width and height of the subscene returned.
     *  XXX Interaction with put()
     * @return The 
     * subvobset is returned as a VobBox, which is a Vob as well as a
     *  VobScene: this allows interpolation, connections etc. easily.
     *  @see VobBox
     */
    VobBox createSubScene(Object key, BoxStyle style, int w, int h);

}
