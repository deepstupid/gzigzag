/*   
UserManager.java
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
package org.gzigzag.ztp.server;
import org.gzigzag.*;
import org.gzigzag.ztp.*;
import java.util.*;

/** User manager for ZTP server.  It enforces single-threaded access
 * to the user table in the space. */
class UserManager {
    public static boolean dbg = true;
    private static void p(String s) { if(dbg) ZZLogger.log("[" + s + "]"); }
    private static void pa(String s) { ZZLogger.log(s); }

    private static Hashtable managers = new Hashtable();
    
    private int refcount;

    private ZZSpace space;
    
    private boolean live = false;
    
    private void assertLiving() {
        if (!live) throw new ZZError("UserManager closed");
    }
    
    /*
      private ZZCell userlist;
    */
    
    private UserManager(ZZSpace space) {
        synchronized (managers) {
            this.space = space;
            if (managers.containsKey(space)) {
                throw new ZZError("there already is a UserManager for that space");
            }
            managers.put(space, this);
            live = true;
        }
        
        refcount = 0;

        // NOT NOW
        //// we find the user list here for convenience
        //ZZCell home = space.getHomeCell();
        //userlist = home.findText("d.system", 1, "ZTPUserList");
        //if (userlist == null) {
        ///*   userlist = home.N("d.system", 1);
        //userlist.setText("ZTPUserList");*/
        //throw new ZZError("no user list in space!");
        //}
    }


    public static UserManager create(ZZSpace space) {
        UserManager rv;
        synchronized (managers) {
            Object o = managers.get(space);
            if (o == null) {
                rv = new UserManager(space);
            } else {
                rv = (UserManager)o;
            }
        }
        ++rv.refcount;
        return rv;
    }

    public void close() {
        if (--refcount < 1) {
            synchronized (managers) {
                live = false;
                managers.remove(space);
            }
        }
    }

    public synchronized UserRecord access(String username) {
        assertLiving();

        UserRecord rv = new UserRecord();

        rv.username = "adm";
        rv.password = "mda";
        rv.susp = false;
        rv.chpass = false;
        rv.sap = true;
        rv.auth_pre = true;
        rv.auth_pass = true;

        return username.equals(rv.username) ? rv : null;
    }

    public interface RecordHandler {
        void doit(UserRecord ur);
    }

    public synchronized void access_atomic(String username, RecordHandler rh) {
        UserRecord ur = access(username);
        rh.doit(ur);
    }

    public synchronized void replace(UserRecord ur) {
        throw new ZZError("user record replace not implemented at this time");
    }

    public synchronized void replace_atomic(String username, RecordHandler rh) {
        UserRecord ur = access(username);
        rh.doit(ur);
        replace(ur);
    }
    
    public synchronized void delete(String uname) {
        throw new ZZError("user record deletion not implemented at this time");
    }
    
    public synchronized void insert(UserRecord ur) {
        throw new ZZError("user record insertion not implemented at this time");
    }

}
