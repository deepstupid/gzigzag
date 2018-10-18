/*   
StringSearcher.java
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

/** An interface for storing a set of strings with keys
 * and searchig from them.
 * The search behaviour, i.e. whether we search for initial substrings,
 * case-free substrings or occurrences is not specified by this interface;
 * rather, different objects exist for different algorithms
 * (the Strategy pattern in GOF).
 */
public interface StringSearcher {

    void addString(String s, Object value);

    /** Search for the given string.
     * The returned collection <b>may not be modified</b>.
     */
    java.util.Collection search(String s);
}
