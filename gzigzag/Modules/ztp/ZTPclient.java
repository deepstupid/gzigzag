/*   
ZTPclient.java
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
package org.gzigzag.module;
import org.gzigzag.*;
import org.gzigzag.ztp.*;
import org.gzigzag.ztp.client.*;
import java.awt.*;
import java.net.*;
import java.io.*;

public class ZTPclient {
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log(s); }
    private static void pa(String s) { ZZLogger.log(s); }
    
    private static Action[] actions = { new Get(), new Put() };

    public static ZZModule module = new ZZModule() {
            public void action(String id,
                               ZZCell code, 
                               ZZCell target,
                               ZZView view, ZZView cview, String key,
                               Point pt, ZZScene xi) {
                for (int i = 0; i < actions.length; i++) {
                    if (id.toUpperCase().equals(actions[i].name().toUpperCase())) {
                        RemotePieceData rpd = new RemotePieceData();
                        String err = rpd.readParams(target);
                        if (err != null && !err.equals("")) throw new ZZError(err);
                        Session ses = null;
                        if (actions[i].needSession()) {
                            try {
                                ses = new Session(rpd.serverFQDN, rpd.serverPort);
                                ses.login(rpd.userName, rpd.userPassword);
                            } catch (UnknownHostException e) {
                                throw new ZZError(""+e);
                            } catch (IOException e) {
                                throw new ZZError(""+e);
                            }
                        }
                        actions[i].action(rpd, ses);
                        try {
                            if (ses != null) ses.close();
                        } catch (IOException e) {
                            throw new ZZError(""+e);
                        }
                        return;
                    }
                }
                throw new ZZError("unknown ZTPclient action");
            }
        };

}
