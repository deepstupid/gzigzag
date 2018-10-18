/*   
PUIClipboard.java
 *    
 *    Copyright (c) 2001, Tuomas Lukka
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
 * Written by Tuukka Hastrup by cutting and pasting from old code 
 * by Tuomas Lukka
 */
package org.gzigzag.util;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.awt.datatransfer.*;

/** Handle copy and paste of PUI clipboard
 */

public class PUIClipboard {
public static final String rcsid = "$Id: PUIClipboard.java,v 1.2 2001/12/15 11:53:26 tuukkah Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.err.println(s); }
    private static void out(String s) { System.out.println(s); }

    static private Clipboard getClipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    static public String getText()
    {
        Clipboard clipboard = getClipboard();
        Transferable content = clipboard.getContents(new Object());
        String s = "";
        if(content != null) {
	    try {
                s = (String) content.getTransferData(DataFlavor.stringFlavor);
            } catch (Exception e) {
		if(!dbg)
		    pa("Paste failed: "+e.toString());
		else
		    e.printStackTrace();
		return null;
	    }
        }
        return s;
    }

    /*
    static public void puiCopy(String str) {
        p("PuiCOPY '"+str+"'");
        Clipboard clipboard = getPUIClipboard();
        StringSelection contents = new StringSelection(str);
        clipboard.setContents(contents, new ClipboardOwner() {
            public void lostOwnership(Clipboard cb, Transferable t) {}
        });
    }

    static public void puiCopy(ZZCell from, String dim)
    {
        StringBuffer cont = new StringBuffer(from.getText());
        if(dim != null) {
            ZZCell cur = from.s(dim, 1);
            while(cur != null) { // XXX Use LoopDetector
                String s = cur.getText();
                if(s.equals(""))
                    cont.append('\n');
                else
                    cont.append(' ').append(s);
                cur = cur.s(dim, 1);
            }
        }
        puiCopy(cont.toString());
    }


    static public void puiCopy(ZZCell from) {
        puiCopy(from.getText());
    }

    
    */
}
