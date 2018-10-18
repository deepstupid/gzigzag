/*   
HBox.java
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
import java.util.*;

/** An interface for linebreaking. A box knows how wide it is at
 * different scales.
 * <p>
 * As for ScalableFont, the "normal" size is scale=1000.
 */

public interface HBox {
String rcsid = "$Id: HBox.java,v 1.7 2001/10/16 22:01:21 tjl Exp $";

    int getWidth(int scale);

    /** Get the ascent of the hbox from the baseline upwards.
     */
    int getHeight(int scale);

    /** Get the descent of the hbox from the baseline upwards.
     */
    int getDepth(int scale);

    /** Return a vob for the contents of this box at the given scale.
     * Note that this function <strong>may</strong> change this HBox and return:
     * the following is a legal implementation:
     * <pre>
     *	public class FooBox extends Vob implements HBox {
     *		int scale;
     *   	...
     *   	...
     *		public Vob getVob(int scale) {
     *			this.scale = scale;
     *			return this;
     *		}
     *  }
     * </pre>
     */
    Vob getVob(int scale);

    /** Set the preceding HBox.
     * This is useful for beaming where
     * shared sequences should be truly shared.
     */
    void setPrev(HBox b);

    /** Inform this HBox where its vob will be placed.
     * XXX Remove this! Wrong abstraction! This information MUST be
     *     inquired from the VobScene, not through the Vob!!! --Tjl
     */
    void setPosition(int depth, int x, int y, int w, int h);

    class Null implements HBox {
	public int getWidth(int scale) { return 0; }
	public int getHeight(int scale) { return 0; }
	public int getDepth(int scale) { return 0; }
	public Vob getVob(int scale) { return null; }
	public void setPrev(HBox b) { }
	public void setPosition(int depth, int x, int y, int w, int h) { }
    }

    /** A useful base class for hboxes that are vobs.
     */
    abstract class VobHBox extends Vob implements HBox {
	public VobHBox(Object key) { super(key); }
	protected int scale = 0;
	public int getScale() { return scale; }

	public Vob getVob(int scale) {
	    this.scale = scale;
	    return this;
	}
	    
	public void setPrev(HBox b) { }
	public void setPosition(int depth, int x, int y, int w, int h) { }
	    
    }

}
