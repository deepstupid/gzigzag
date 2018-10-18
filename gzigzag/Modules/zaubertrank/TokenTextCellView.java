/*   
TokenTextCellView.zob
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

package org.gzigzag;
import java.util.*;
import java.awt.*;

/** A cell view for showing (and editing) the text of a zaubertrank token.
 *  (This is to be used with ideosyncratic, not as the main cell view for a
 *  window.) The text for a token is connected to the token poswards on
 *  d.zt-text. Variables are recognized by them being clones. Their names are
 *  simply the content of the cloned cell. Thus, clones are shown with a
 *  different background/foreground; that's all we do to distinguish variables.
 */
 
public class TokenTextCellView extends FTextCellView {
public static final String rcsid = "$Id: TokenTextCellView.java,v 1.1 2001/04/01 17:45:17 bfallenstein Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log(s); }
    private static void pa(String s) { ZZLogger.log(s); }

    public static String zttext = "d.zt-text";
    public static Color varbg = Color.darkGray;
    public static Color varfg = Color.gray;

    /** Get the FText for this cell at the given scale.
     *  scale == fract * 1000.
     */
    protected FText getFText(ZZCell c0, int scale) {
	ZZCell cells[] = c0.readRank(zttext, 1, false);
	FText.Part parts[] = new FText.Part[cells.length];

	for(int i=0; i<cells.length; i++) {
	    ZZCell c = cells[i];
	    if(c.getRootclone().equals(c))
		parts[i] = new FText.CellPart(c, 0, -1, f(scale), fm(scale),
					      null, null);
	    else
		parts[i] = new FText.CellPart(c, 0, -1, f(scale), fm(scale),
					      varbg, varfg);
	}
			
	return new FText(parts);
    }
}
