/*   
Put.java
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
 * Written by Antti-Juhani Kaijanaho
 */

package org.gzigzag.ztp.client;
import org.gzigzag.ztp.*;
import org.gzigzag.*;
import java.io.*;
import java.util.*;

public class Put implements Action {

    public String name() { return "PUT"; }
    public boolean needSession() { return true; }
    public void action(RemotePieceData rpd, Session ses) {
        if (rpd.subspace == null) throw new ZZError("there's nothing to put!");
        Subspace ss = new Subspace(rpd.subspace.s("d.ztp-subspace"));
        try {
            ses.put(rpd.subspaceName, ss.getCells(), ss.getSoftDims());
        } catch (IOException e) {
            throw new ZZError("" + e);
        }
    }

}

