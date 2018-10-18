/*   
WordBreaker.java
 *    
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
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

/** A filter for <code>CharRangeIter</code> breaking at word boundaries and
 ** adding glue.
 *  When producing <code>HBox</code>es for line breaking, each word needs to
 *  be in one or more <code>HBox</code>es; in other words, a single
 *  <code>HBox</code>may not contain characters from more than one word.
 *  <p>
 *  This class provides a <em>filter</em> for <code>CharRangeIter</code>:
 *  it is constructed with a <code>CharRangeIter</code> instance to which it
 *  proxies the method calls it gets itself. However, when it gets a range
 *  of characters containing more than one word, it does multiple calls to
 *  the <code>range</code> method of the underlying iter, so that one range
 *  does not contain characters from more than one word.
 *  <p>
 *  Additionally, at the end of a word, this calls <code>object</code>
 *  in the underlying <codde>CharRangeIter</code> with
 *  <code>WordBreaker.WORD_GLUE</code> as its parameter. That way, the
 *  underlying <code>CharRangeIter</code> can add appropriate glue between
 *  <code>HBox<code>es.
 *  <p>
 *  XXX implement firstCap
 */

public class WordBreaker extends CharRangeIter.Filter {
String rcsid = "$Id: WordBreaker.java,v 1.6 2001/08/12 08:38:03 bfallenstein Exp $";
   
    private static void pa(String s) { System.out.println(s); }

    public static final Object WORD_GLUE = new Object();
    public static final Object SENTENCE_GLUE = new Object();
    public static final Object NEWLINE_GLUE = new Object();

    protected CharRangeIter iter;
    protected boolean hadSpace = false;

    public WordBreaker(CharRangeIter iter) { super(iter); }

    public void range(Object tag, char[] chars, int first, int last) {
	//pa("Start WordBreaker.range: "+first+", "+last);
	for(int i=first; i<=last; i++) {
	    char c = chars[i];
	    if(c == ' ')
		hadSpace = true;
	    else if(c == '\n') {
		// XXX hadSpace and firstcap interaction
		if(i > first) {
		    //pa("Range before line break: "+i);
		    super.range(tag, chars, first, i-1);
		}
		//pa("Line break: "+i);
		super.object(NEWLINE_GLUE);
		// Skip the newline character-- it's never printed for now.
		first = i + 1;
		hadSpace = false;
	    } else if(hadSpace) {
		if(i > first) {
		    //pa("Range before word break: "+i);
		    super.range(tag, chars, first, i-1);
		}
		//pa("Word break: "+i);
		super.object(WORD_GLUE);
		first = i;
		hadSpace = false;
	    }
	}
	//pa("Remaining stuff out");
	super.range(tag, chars, first, last);
    }

    public void object(Object o) {
	if(o == END_OF_STREAM)
	    super.object(NEWLINE_GLUE);
	else if(o instanceof HBox && hadSpace) {
	    super.object(WORD_GLUE);
	    hadSpace = false;
	}
	super.object(o);
    }
}
