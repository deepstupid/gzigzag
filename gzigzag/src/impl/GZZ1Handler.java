/*   
GZZ1Handler.java
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
 * Written by Tuomas Lukka
 */

package org.gzigzag.impl;
import org.gzigzag.mediaserver.Mediaserver;
import java.io.IOException;

/** An event handler for the GZZ1 format parser.
 */

public interface GZZ1Handler {
String rcsid = "$Id: GZZ1Handler.java,v 1.16 2001/11/01 22:39:08 bfallenstein Exp $";

    /** Handles the changes to a simple dimension.
     */
    interface SimpleDim {
	void disconnect(byte[] id1, byte[] id2);
	void connect(byte[] id1, byte[] id2);
	/** The section is over.
	 */
	void close();
    }

    /** Handles transcluding legacy file format text.
     *  The file format used to allow setting the text of a cell as a string--
     *  this is not allowed any more. In order to still support the legacy
     *  mediaserver blocks, we need to convert the text in there to spans.
     *  Here is how we do it.
     *  <p>
     *  First off, the requirements. The old texts need to be converted to
     *  vstreams. And, the cells in the vstream need to have <i>the same
     *  ids</i> every time they are loaded. (Otherwise, it would not be save
     *  to use the cells in some other space.) Finally, we really don't want to
     *  autoconvert again, so we have to do some magic on reading it.
     *  <p>
     *  To fulfill these, we transclude the old text as a single span. As the
     *  text scroll, we use the diff containing the text-- that fulfills the
     *  requirement of nonchanging ids. Also, as the transclusion id, we
     *  simply use the cell that will contain the content; again, this
     *  helps fulfilling the requirement of always having the same ids.
     *  Using the diff blocks as text scrolls is of course a kludge, but
     *  a nice one <code>:-)</code>.
     *  <p>
     *  This has to do exactly two things: the same as a span transclusion
     *  with the same parameters; and connecting the resulting span to
     *  the cell referenced by <code>id</code> as a vstream (possibly
     *  disconnecting a previous vstream connected to it).
     *  <p>
     *  (Note: This does not handle newlines and backslashes correctly;
     *  a former newline becomes <code>\n</code>, and a former backslash
     *  becomes doubled. Too bad. In other words: I think we can definitely
     *  live with it.)
     *  @param id The cell id.
     *  @param first The first character int the current diff that is to
     *               be transcluded.
     *  @param last The last character in the current diff that is to
     *              be transcluded.
     */
    interface LegacyContent {
	void transcludeLegacyContent(byte[] id, int first, int last)
	    throws IOException;
	/** The section is over.
	 */
	void close();
    }

    /** Handles a set of transcopies.
     */
    interface Transcopy {
	void transcopy(byte[] id);
        /** The section is over.
         */
        void close();
    }

    /** Handles a set of span transclusions.
     */
    interface SpanTransclusion {
	void transclude(byte[] tid, Mediaserver.Id block, int first, int last)
	    throws IOException;
	/** The section is over.
	 */
	void close();
    }

    /* Handles creation of new cells.
     */
    interface NewCells {
	void newCell(byte[] cellId);
        /** The section is over.
         */
        void close();
    }

    // XXX delete

    /** Start the file.
     * @param previous The mediaserver id which is the previous
     * 			version of the space. <code>null</code>
     *                  if this is the first version.
     */
    void start(org.gzigzag.mediaserver.Mediaserver.Id previous);

    /** Return a SimpleDim handler for the dimension in this cellId.
     */
    SimpleDim dimSection(byte[] cellId);

    /** Return a handler for the legacy content section.
     */
    LegacyContent legacyContentSection();

    /* Return a handler for the new cells section.
     */
    NewCells newCellsSection();

    Transcopy transcopySection(byte[] transcopyId,
			       org.gzigzag.mediaserver.Mediaserver.Id spaceId);

    /** Return a handler for the span transclusion section.
     */
    SpanTransclusion spanTransclusionSection();

    void close();

    interface IDSettable extends GZZ1Handler {
	void setId(Mediaserver.Id id);
    }

}
