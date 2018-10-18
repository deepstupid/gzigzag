/*
CharArrayVobFactory.java
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
import java.awt.Color;
import java.awt.Image;

/** A factory for <code>CharArrayVob</code>s.
 *  This extends <code>CharRangeIter</code>. Generally, it will be passed
 *  to a <code>WordBreaker</code> instance, so that the glue is set
 *  correctly.
 *  <p>
 *  When finished generating the Vobs, the result can be retrieved by
 *  calling <code>getChain</code>.
 */

public class CharArrayVobFactory extends CharRangeIter {
String rcsid = "$Id: CharArrayVobFactory.java,v 1.12 2001/09/27 14:53:43 tjl Exp $";
    static public boolean dbg = false;
    static private void p(String s) { if(dbg) System.out.println(s); }

    protected LinebreakableChain chain = new LinebreakableChain();
    protected TextStyle textStyle;
    protected LinebreakableChain.GlueStyle glueStyle;

    private static char[] emptyCharArray = new char[0];

    private Object streamKey;

    public CharArrayVobFactory(TextStyle textStyle) {
	this(textStyle, null);
    }

    public CharArrayVobFactory(TextStyle textStyle, Object streamKey) {
	this.textStyle = textStyle;
	this.glueStyle = new LinebreakableChain.GlueStyle(textStyle);
	this.streamKey = streamKey;
    }

    public void setStyle(TextStyle textStyle) {
	this.textStyle = textStyle;
    }

    public void setStreamKey(Object streamKey) {
	this.streamKey = streamKey;
    }

    public void range(Object tag, char[] chars, int first, int last) {
	int offs = first;
	int len = last - offs + 1;
	// XXX use tag
	chain.addBox(new CharArrayVob(streamKey, textStyle, chars, offs, len));
    }

    public void object(Object ob) {
	if(ob == WordBreaker.WORD_GLUE)
	    glueStyle.addSpace(chain);
	else if(ob == WordBreaker.SENTENCE_GLUE)
	    glueStyle.addSentenceSpace(chain);
	else if(ob == WordBreaker.NEWLINE_GLUE) {
	    chain.addGlue(0, 10000, 0);
	    chain.addBox(new CharArrayVob(null, textStyle, 
					  emptyCharArray, 0, 0));
	    chain.addGlue(10000, 0, 0);
	} else if(ob instanceof HBox) {
	    chain.addBox((HBox)ob);
	} else if(ob instanceof Image) {
	    chain.addBox(new ImageVob((Image)ob));
	}
    }

    /** Get the chain this factory places vobs into.
     *  Note: the chain will change when calls to 
     *  <code>CharArrayVobFactory.range</code> or <code>.call</code> are
     *  made.
     * This call will erase the existing chain and cause later
     * calls to create a new chain.
     */
    public LinebreakableChain getChain() {
	LinebreakableChain oldChain = chain;
	chain = new LinebreakableChain();
	return oldChain;
    }
}
