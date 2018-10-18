/*
DecoratedVob.java
 *
 *    Copyright (c) 1999-2000, Ted Nelson and Tuomas Lukka
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
 * Written by Tero Mäyränen
 */

package org.gzigzag.impl;
import org.gzigzag.*;
import org.gzigzag.vob.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** A Vob with line decorations
 */

public abstract class DecoratedVob extends Vob {
public static final String rcsid = "$Id: DecoratedVob.java,v 1.2 2002/02/22 17:51:53 deetsay Exp $";
    public static final boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    /**
     * Draw background rectangle first, then lines.
     */
    protected void renderDecorations(Graphics g,
	int mx, int my, int mw, int mh, Vob.RenderInfo info) {

	Color oldfg = g.getColor();

	// background rectangle
	g.setColor(info.getBgColor());
	if (mh > 14) g.fillRect(mx-2, my-2, mw+4, mh+4);
	else g.fillRect(mx-1, my-1, mw+2, mh+2);

//	if(!info.isFast())
	renderLines(g, mx+mw/2, my+mh/2, mw, mh, info);

	g.setColor(oldfg);
    }

    private CellConnector cellConnector;

    private int lineOffset = 0; // offset to points and angles in cellConnector.lineData[]
    private int numLines = 0; // number of lines to read from cellConnector.lineData[]

    /**
     * Add a decorative (and informative) line from this CellVob to a
     * given point, leaving this CellVob at a given angle. To minimalize
     * overhead in creating CellVobs, the line-information (there can be
     * an arbitrary number of lines) is not stored in a table inside
     * CellVob, but instead in cellConnector.lineData[].  The
     * CellConnector is given to CellVobs in the constructor.
     *
     * IMPORTANT, another optimization is that all the lines that
     * are added to the CellVob must be in sequential offsets, as you
     * can see in the code: the offset is only stored for the first
     * line and for the rest of the lines, only numLines is increased.
     * As a consequence, only the first addLine() call for a CellVob needs
     * to have a meaningful offset.
     *
     * @param offset Offset to cellConnector.lineData[] where to find x,
     *	y, and angle for a line.
     */
    public void addLine(int offset) {
	if (numLines == 0) { lineOffset = offset; }
	numLines++;
    }

    /**
     * The line-drawing subroutine that actually forms a visible line
     * on the screen.  Also draws a background for the line.
     */
    private void drawLine(Graphics g,
    		    int x1, int y1, int x2, int y2,
    		    int angle, Vob.RenderInfo info) {

        if (y1 == y2) {
	    g.setColor(info.getBgColor());
	    g.drawLine(x1, y1-1, x2, y1-1);
	    g.drawLine(x1, y1+1, x2, y1+1);
	}
        else if (x1 == x2) {
	    g.setColor(info.getBgColor());
	    g.drawLine(x1-1, y1, x1-1, y2);
	    g.drawLine(x1+1, y1, x1+1, y2);
	}
	else {
	    g.setColor(info.getBgColor());
	    if (angle < -90) {
		g.drawLine(x1-1, y1, x2-1, y2);
		g.drawLine(x1, y1+1, x2, y2+1);
	    }
	    else if (angle < 0) {
		g.drawLine(x1-1, y1, x2-1, y2);
		g.drawLine(x1, y1-1, x2, y2-1);
	    }
	    else if (angle < 90) {
		g.drawLine(x1+1, y1, x2+1, y2);
		g.drawLine(x1, y1-1, x2, y2-1);
	    }
	    else {
		g.drawLine(x1+1, y1, x2+1, y2);
		g.drawLine(x1, y1+1, x2, y2+1);
	    }
	}
	g.setColor(info.getMixedFgColor());
	g.drawLine(x1, y1, x2, y2);
    }
    /**
     * Starting at an angle, draw straight lines on the screen, turning
     * the angle a little bit between each line, so that a straight line
     * can finally be drawn into the endpoint.
     */
    private void renderCurve(Graphics g,
	int x1, int y1, int x2, int y2,
	int angle, int maxCurves, double dist, Vob.RenderInfo info) {

	// this will hopefully end the recursion at some point: when the
	// curve is pointing "close enough" towards the endpoint, draw a
	// single line there
	int angleDiff =
	    (angle - (int)(Math.atan2(x2-x1, y1-y2)*180/Math.PI)) % 180;
	if ((angleDiff >= -15 && angleDiff <= 15) || (maxCurves == 0)) {
	    drawLine(g, x1, y1, x2, y2, angle+angleDiff, info);
	}
	// while it's not, keep curving
	else {
	    int newAngle = (angleDiff < 0 ? angle + 20 : angle - 20);
	    double radAngle = (double)newAngle * Math.PI / 180;

	    int destX = x1 + (int)(Math.sin(radAngle)*dist);
	    int destY = y1 - (int)(Math.cos(radAngle)*dist);

	    drawLine(g, x1, y1, destX, destY, newAngle, info);
	    renderCurve(g, destX, destY, x2, y2, newAngle, maxCurves-1,
		dist * 0.8, info);
	}
    }

    /**
     * Calculate difference between the angle that a line is supposed
     * to be drawn into, and the actual angle between the startpoint
     * and the endpoint.  If they're "close enough", draw a straight
     * line: otherwise start the "renderCurve" routine.
     */
    private void renderLine(Graphics g,
	int x1, int y1, int x2, int y2,
	int angle, int w, int h, Vob.RenderInfo info) {

	if (x2 == x1 && y2 == y1) {
	    if (info.isFast()) return;
	    double radAngle = (double)angle * Math.PI / 180;
	    double vecX = Math.sin(radAngle);
	    double vecY = -Math.cos(radAngle);
	    int destX, destY;
	    double lineLen = ((Math.abs(vecX)*w)+(Math.abs(vecY)*h))*0.7;
	    destX = x1 + (int)(vecX*lineLen);
	    destY = y1 + (int)(vecY*lineLen);
	    double stubAngle = (double)(angle+90) * Math.PI / 180;
	    int stubX = (int)(Math.sin(stubAngle)*3);
	    int stubY = (int)(-Math.cos(stubAngle)*3);
	    drawLine(g, destX+stubX, destY+stubY, destX-stubX, destY-stubY,
		angle+90, info);
	    destX = x1 + (int)(vecX*(lineLen-1));
	    destY = y1 + (int)(vecY*(lineLen-1));
	    drawLine(g, x1, y1, destX, destY, angle, info);
	    return;
	}

	int angleDiff =
	    (angle - (int)(Math.atan2(x2-x1, y1-y2)*180/Math.PI)) % 180;

	// If the starting angle is "close enough" to where the line is
	// supposed to point to, just draw a single straight line
	if (angleDiff >= -15 && angleDiff <= 15)
	    drawLine(g, x1, y1, x2, y2, angle+angleDiff, info);

	// Otherwise draw a curve
	else {
	    double radAngle = (double)angle * Math.PI / 180;
	    double vecX = Math.sin(radAngle);
	    double vecY = -Math.cos(radAngle);
	    int destX, destY;
	    double lineLen = ((Math.abs(vecX)*w)+(Math.abs(vecY)*h))*0.7;
	    destX = x1 + (int)(vecX*lineLen);
	    destY = y1 + (int)(vecY*lineLen);
	    drawLine(g, x1, y1, destX, destY, angle, info);
	    if (!info.isFast())
		renderCurve(g, destX, destY, x2, y2, angle, 9,
		    lineLen/6, info);
	}
    }

    /**
     * Read line-info from cellConnector.lineData[] and call
     * "renderLine" for each line..
     */
    private void renderLines(Graphics g,
	int x, int y, int w, int h, Vob.RenderInfo info) {

	int offset = lineOffset;
	if(cellConnector == null) return;
	/*
	 cellConnector.lineData[] is a table of integers that are the
	 endpoints and angles for lines going out from CellVobs.
	 (allocated outside CellVob to minimize the overhead of creating
	 new CellVobs)
	 Each CellVob contains 2 ints: lineOffset and numLines, which are
	 the offset into the table for the first line leaving this Vob,
	 and the number of lines for this Vob.
	*/
	int ld[] = cellConnector.lineData;
	if(ld == null) return;
	for (int i=0; i<numLines; i++) {
	    renderLine(g, x, y,
		x+ld[offset++], y+ld[offset++], ld[offset++], w, h, info);
	}
    }

    public DecoratedVob(Cell c, CellConnector connector) {
	super(c);
	cellConnector = connector;
    }
}
