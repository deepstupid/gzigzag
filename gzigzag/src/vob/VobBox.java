/*   
VobBox.java
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
 * Written by Benja Fallenstein
 */
package org.gzigzag.vob;
import java.awt.*;

/** A VobScene that is a Vob at the same time.
 *  This is simply an abstract subclass of Vob, implementing
 *  VobScene at the same time. Its purpose is to serve as the 
 *  return value of VobScene.createSubVobScene. 
 *  This is because sub-vobsets <strong>must</strong>
 *  be vobs: otherwise, they cannot be interpolated, connected etc.
 *  reasonably. 
 * <p>
 * OTOH, they obviously must still be vobsets. <code>;)</code>
 *  Implementations of VobScene are expected to use specific subclasses of
 *  VobBox, written just for this specific kind of VobScene.
 *  <p>
 *  One reason for having these both-in-one is to avoid creating superfluous
 *  objects: we want sub-vobsets to be really, really cheap, because we want
 *  to put e.g. the contents of all GZigZag cells into their own vobsets.
 *  Basically everything you see a pretty box around...
 *  <p>
 *  One thing to be done is to complement <code>BoxStyle</code> with something
 *  like a "CoordTranslator" object or some such which does coordinate
 *  translations, rotations etc; important point being to create rotated
 *  stuff etc. without special awareness from the different VobScene
 *  implementations. (Yet another hook!) Putting this in BoxStyle would be bad,
 *  because then you couldn't render the same boxes as usual around e.g.
 *  rotated stuff.
 *  <p>
 *  The BoxVob name is a legacy of Vesa's and my (Benja's) old vob draft and
 *  doesn't really apply because this isn't subclassed to specify a kind of
 *  boxing around a vobset-- that's done by <code>BoxStyle</code>. It would be
 *  better to call this beast a VobSceneVob, but that has another meaning
 *  currently-- please think about renaming this as VobSceneVob.
 */

public abstract class VobBox extends Vob implements VobScene {
		
    public VobBox(Object key) { super(key); }


}
