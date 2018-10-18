/*
CharRangeIter.java
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
 * Written by Benja Fallenstein
 */

package org.gzigzag.vob;
import java.util.*;
import org.gzigzag.*;

/** A callback object for processing character streams efficiently.
 *  This is a callback object, i.e. an object passed to a function that
 *  iterates over some stream of characters. In order to support efficient
 *  storage and retrieval, characters are stored in <code>char</code> arrays,
 *  and only ranges inside that array are processed: that way, the text can
 *  be stored e.g. in one big array, and still ranges from that array can be
 *  processed. 
 *  <p>
 *  In GZigZag, we use it to iterate over spans, which are ranges
 *  in scroll blocks which in turn store <code>char</code> arrays. The
 *  <code>range</code> function is thus passed a <code>char</code> array and
 *  the index of the first and last characters to be processed in that range
 *  (empty ranges are not allowed/possible).
 *  <p>
 *  The text can possibly be attributed with markup (see 
 *  <code>markupStart</code>). If this is not desired, the
 *  <code>markupStart</code> and <code>markupEnd</code> methods can simply
 *  be ignored.
 */

public abstract class CharRangeIter {
String rcsid = "$Id: CharRangeIter.java,v 1.5 2001/08/12 08:22:47 bfallenstein Exp $";

    /** A special object to mark the end of the stream.
     *  After all character ranges in the stream have been iterated through,
     *  the last call to this callback object should be
     *  <code>object(END_OF_STREAM)</code>.
     */
    public static final Object END_OF_STREAM = new Object();

    /** A special object to mark the current cursor location.
     */
    public static final Object CURSOR = new Object();



    /** Process a range of characters.
     *  @param tag A tag whose meaning is implementation-defined, usually to
     *             be used as a vob key.
     *  @param chars The array of characters this range is in.
     *  @param first The offset of the range's first character, in the array.
     *  @param last The offset of the range's last character, in the array.
     *  @see #markupStart
     */
    public abstract void range(Object tag, char[] chars, int first, int last);

    /** Handle a special object in the stream, e.g. an image or formula.
     *  This can be used to process anything that is not a stream of
     *  characters. Note: <code>WordBreaker</code> uses this to pass on
     *  glue (as <code>WordBreaker.WORD_GLUE</code> and
     *  <code>WordBreaker.SENTENCE_GLUE</code> objects).
     *  <p>
     *  Generally, objects that are not reccognized by an implementation
     *  should be ignored.
     */
    public void object(Object o) {
    }

    /** The following ranges of characters have a given markup.
     *  Subsequent calls to <code>range</code>, up to the next call to
     *  <code>markupEnd(markup)</code>, are to be treated as being attributed
     *  with <code>markup</code>.
     *  <p>
     *  The markup needs <em>not</em> be hierarchical: a calling sequence
     *  <code>markupStart(a); markupStart(b); markupEnd(a); markupEnd(b);</code>
     *  or similar is entirely legal.
     *  <p>
     *  Default: do nothing.
     *  @param markup The markup in an implementation-defined format.
     *  @see #markupEnd, #range
     */
    public void markupStart(Object markup) {
    }
 
    /** The following ranges of characters do not have a given markup.
     *  @see #markupStart
     */   
    public void markupEnd(Object markup) {
    }


    /** A char range iter that proxies all callbacks to another
     *  char range iter.
     */
    public static class Filter extends CharRangeIter {
	public CharRangeIter base;
	public Filter(CharRangeIter base) { this.base = base; }
	
	public void range(Object tag, char[] chars, int first, int last) {
	    base.range(tag, chars, first, last);
	}
	public void object(Object ob) { base.object(ob); }
	public void markupStart(Object markup) { base.markupStart(markup); }
	public void markupEnd(Object markup) { base.markupEnd(markup); }
    }
}
