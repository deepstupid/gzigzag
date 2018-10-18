/*   
LineBreaker.java
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
import java.awt.FontMetrics;
import java.lang.reflect.*;
import java.util.*;

/** An interface to line breaking algorithms
 * This class knows absolutely nothing about characters, words, or so on.
 * It handles abstract tokens given by integers and asks the related
 * class LineInfo about the goodness of fitting a particular line
 * at a particular point.
 * <p>
 * Specific attention is paid to the billowing problem where
 * we have to render a paragraph so that the cursor is at the line with
 * the first line of the paragraph starts from the beginning of a line.
 * This is achieved by specifying the cursor position (from where the lines
 * should be counted) and the desired line number for that position.
 * This can also be used to render the paragraph starting from the end.
 */

public abstract class LineBreaker {
String rcsid = "$Id: LineBreaker.java,v 1.8 2000/09/19 10:32:00 ajk Exp $";
    static private void pa(String s) { System.out.println(s); }
    /** Break the lines between the given addresses.
     * @return An array: the first element is the line index the rendering
     * starts on, and from the second element onwards the end points of the
     * lines are given.
     */
    public abstract int[] breakLines(LineInfo li);

    static private void test(LineBreaker b, String str, int cursor,
				    SimpleLineInfo.Layout l) {
	SimpleLineInfo inf = new SimpleLineInfo(str, cursor, l);
	int[] foo = b.breakLines(inf);
	pa("Tested '"+str+"'");
	for(int i=0; i<foo.length; i++)
	    pa(""+foo[i]);
    }

    static private class FixFM extends FontMetrics {
	FixFM() { super(null); }
	  public int getAscent()  { return 0; }
	  public int getDescent()  { return 0; }
	  public int getLeading()  { return 0; }
	  public int getMaxAdvance()  { return 10; }
	  public int charWidth(char ch)  { return 10; }
	  public int charsWidth(char data[], int off, int len) {
	    return 10 * len;
	  }
    }

    /** A test routine. Provide name of the class to test first.
     * The class to test must have a public constructor.
     */
    static public void main(String[] argv) {
      try {
	Class cls = Class.forName("org.gzigzag."+argv[0]);
	Constructor ctr = cls.getConstructor(new Class[] {});
	LineBreaker b = (LineBreaker)ctr.newInstance(new Object[] {});

	Vector db = new Vector();
	for(int i=0; i<argv.length; i++)
	    Main.debugClass(argv[i], db);

	final FontMetrics fixwd = new FixFM();

	// Test 1: fixed-width lines, 10 chars each.
	SimpleLineInfo.Layout lay = new SimpleLineInfo.Layout() {
	    public FontMetrics getFontMetrics(int line) {
		return fixwd; 
	    }
	    public int getWidth(int line) {
		return 100;
	    }
	    public int getCenterLine() { return 0; }
	};

	test(b, "", 0, lay);
	test(b, "A", 0, lay);
	test(b, "A B C D E F G H I J K L M N O P Q", 0, lay);

      } catch(Exception e) {
	e.printStackTrace();
	pa(""+e);
      }
    }
}
