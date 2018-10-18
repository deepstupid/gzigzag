/*   
InitialStringSearcher.java
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
 * Written by Tuomas Lukka
 */
package org.gzigzag.util;
import java.util.*;

/** Searches for strings having the parameter string
 * as the initial substring.
 * Initial, not terribly efficient implementation.
 * Assumes that we will search for a few of the first characters.
 * <p>
 * Note that searching for a single character will return nothing.
 * Searching for more than 6 characters will also return nothing.
 */
public class InitialStringSearcher implements StringSearcher {

    /*
    class Node {
	char ch;
	Node nextSibling;
	Node firstChild;
	String whole;
	Object value;
    }

    Node root = new Node();

    void makeOrGet(Node n, int depth, char ch) {
	if(n.firstChild != null) {
	}
	Node prev = n.firstChild;
	n.firstChild = new Node();
	n.firstChild.ch = ch;
	n.firstChild.nextSibling = prev;
	return n.firstChild;
    }

    public void addString(String s, Object value) {
	Node cur = root;
	for(int i=0; i<s.length(); i++) {
	    cur = makeOrGet(cur, i, s.charAt(i));
	    if(cur.firstChild == null) 
		break;
	}
	cur.whole = s;
	cur.value = value;
    }
    */

    Map[] sets = new Map[7];

    {
	for(int i=2; i<sets.length; i++)
	    sets[i] = new HashMap();
    }

    public void addString(String s, Object value) {
	for(int i=0; i<sets.length; i++) {
	    if(sets[i] == null) continue;
	    String key = s.substring(0, i);
	    List list = (List) sets[i].get(key);
	    if(list == null) {
		sets[i].put(key, list = new ArrayList());
	    }
	    list.add(value);
	}
    }

    public java.util.Collection search(String s) {
	if(s.length() >= sets.length) return null;
	if(sets[s.length()] == null) return null;
	return (Collection)sets[s.length()].get(s);
    }


}
