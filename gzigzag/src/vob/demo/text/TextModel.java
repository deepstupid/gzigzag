/*   
TextModel.java
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
 * Written by Benja Fallenstein and Tuomas Lukka
 */
package org.gzigzag.vob.demo;
import org.gzigzag.vob.*;
import org.gzigzag.impl.*;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

/** A simple model of formatted referential text.
 *  DRAFT.
 *  <p>
 *  doc tbd
 */

public class TextModel {
public static final String rcsid = "$Id: TextModel.java,v 1.10 2001/10/25 12:31:01 bfallenstein Exp $";

    /** Gluestyles.
     */
    static HashMap glues = new HashMap();

    public class Node {
	TextSpan span;
	TextStyle style;

	/** Create a new node and insert into TextModel.nodes. */
	Node(TextSpan span, TextStyle style, int index) { 
	    this.span = span; this.style = style;
	    nodes.add(index, this);
	}

	
	void append(TextSpan s) {
	    int offs = (int)span.length();
	    Span sp = span.append(s);
	    Node n; int noffs;
	    if(sp != null) { 
		span = (TextSpan)sp; n = this; noffs = (int)span.length();
	    } else {
		n = new Node(s, style, nodes.indexOf(this)+1);
		noffs = (int)s.length();
	    }
	    // Move cursors
	    for(Iterator iter = cursors.iterator(); iter.hasNext();) {
		Cursor cur = (Cursor)iter.next();
		if(cur.node == this && cur.offs >= offs) {
		    cur.node = n;
		    cur.offs = noffs;
		}
	    }
	}

	/** Split this node at the given offset.
	 *  After the split, this node contains everything before offs; the
	 *  returned node contains everything after.
	 */
	Node split(int offs) {
	    TextSpan s1 = (TextSpan)span.subSpan(0, offs),
		     s2 = (TextSpan)span.subSpan(offs);

	    span = s1;
	    Node other = new Node(s2, style, nodes.indexOf(this)+1);

	    for(Iterator iter = cursors.iterator(); iter.hasNext();) {
		Cursor cur = (Cursor)iter.next();
		if(cur.node == this && cur.offs > offs) {
		    cur.node = other;
		    cur.offs -= offs;
		}
	    }

	    return other;
	}

	void addToChain(LinebreakableChain ch) {
	    LinebreakableChain.GlueStyle gs = 
		(LinebreakableChain.GlueStyle) glues.get(style);
	    if(gs == null) {
		gs = new LinebreakableChain.GlueStyle(style);
		glues.put(style, gs);
	    }
	    TextSpanVob.addToChain(ch, this, span, style, gs);
	}

	public void setCursorTo() {
	    Cursor c = ((Cursor)cursors.get(0));
	    c.node = this;
	    c.offs = 0; // XXX...
	}
    }

    public class Cursor {
	Node node;
	int offs;

	Cursor(Node node, int offs) {
	    this.node = node; this.offs = offs;
	    cursors.add(this);
	}

	void insert(String s) {
	    for(int i=0; i<s.length(); i++)
		insert(s.charAt(i));
	}

	void insert(char c) {
	    TextScrollBlock sb = (TextScrollBlock)node.span.getScrollBlock();
	    Span s;
	    try {
		s = sb.append(c);
	    } catch(ImmutableException e) {
		throw new Error("Immutable"+e);
	    }
	    insert((TextSpan)s);
	}

	void insert(TextSpan s) {
	    if(offs == 0) {
		int ind = nodes.indexOf(node);
		if(ind != 0) {
		    ((Node)(nodes.get(ind-1))).append((TextSpan)s);
		} else {
		    Node n = new Node(s, 
					((Node)nodes.get(0)).style,
					0);
		    node = n;
		    offs = (int)s.length();
		}
	    } else if(offs == node.span.length()) {
		node.append(s);
	    } else {
		node.split(offs);
		node.append(s);
		// Split
	    }

	}
    }

    LinebreakableChain makeChain() {
	LinebreakableChain ch = new LinebreakableChain();
	for(Iterator iter = nodes.iterator(); iter.hasNext();) {
	    Node n = (Node)iter.next();
	    n.addToChain(ch);
	}
	return ch;
    }

    ArrayList nodes = new ArrayList();
    {
	try {
	new Node(new TransientTextScroll().append(' '), 
		new RawTextStyle(new ScalableFont("SansSerif", 0, 24), 
				Color.black),
		0);
	} catch(Exception e) { throw new Error("ERR"+e); }
    }

    ArrayList cursors = new ArrayList();
    {
	cursors.add(new Cursor((Node)nodes.get(0), 0));
    }

    public Cursor getCursor() { return (Cursor)cursors.get(0); }

    public String toString() {
	StringBuffer s = new StringBuffer();
	s.append("((");
	for(int i=0; i<nodes.size(); i++) {
	    s.append("[");
	    s.append(
	      ((Node)nodes.get(i)).span.getText());
	    s.append("]");
	}
	s.append("))");
	return s.toString();
    }

    public void putTo(VobScene sc, int w) {
	int n = 30;
	int[] lines = new int[n];
	int[] scales = new int[n];
	for(int i=0; i<n; i++) {
	    lines[i] = w; scales[i] = 1000;
	}
    	LinebreakableChain ch = makeChain();
	Linebreaker lb = new SimpleLinebreaker();

	Linebreaker.Broken bro = lb.breakLines(ch, lines, scales, 0, 0);
	bro.putLines(sc, 0, 0, 10);
    }

}
