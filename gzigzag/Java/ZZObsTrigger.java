/*   
ZZObs.java
 *    
 *    Copyright (c) 2000, Ted Nelson and Tuomas Lukka
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
 * Written by Tuomas Lukka, queuing and enable/disable by Rauli Ruohonen
 */

package org.gzigzag;
import java.util.*;

/** A class used to trigger ZZObses.
 */

public class ZZObsTrigger {
String rcsid = "$Id: ZZObsTrigger.java,v 1.7 2001/04/17 16:40:59 ajk Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) ZZLogger.log(s); }
    private static void pa(String s) { ZZLogger.log(s); }
    
    // XXX Store weak references, not hard ones!
    private static Vector allTrigs = new Vector();
    private static Hashtable obsQueue = new Hashtable();
    private static Hashtable obsDisabled = new Hashtable();

    ZZObsTrigger() {
	allTrigs.addElement(this);
    }

    /** An object which keeps track of the observations of a single
     * ZZObs.
     */
    class Gob {
	/** Linked list of Gobs, plus list of all objects that this
	 * Gob observes.
	 * This is a tricky part and this is also what makes us blazingly
	 * fast. And difficult to explain. 
	 * <p>
	 * First of all, there is a key for every object that this
	 * gob's <code>obs</code> has been <code>addObs</code>'ed for.
	 * The value depends on whether there are other gobs observing
	 * the same thing. If there are, then the hashtables form a linked
	 * list. If not, or if this is the last element of the linked 
	 * list, the value in the Hashtable is <strong>this gob</strong>.
	 * That may seem odd until you realize that 1) hashtables can't
	 * store nulls, and 2) <code>this</code> is a pointer that is
	 * very likely to be in a register so fast to compare with
	 * the reference.
	 */
	Hashtable next = new Hashtable();
	/** The backward links. Only necessary links are stored. */
	Hashtable prev = new Hashtable();
	ZZObs obs;
    }

    /** Key: ZZObs; Value: Gob. */
    Hashtable gobs = new Hashtable();

    /** Key: Object (trigger); Value: Gob. */
    Hashtable trigs = new Hashtable();

    
    /** Called when something is changed.
     * This call removes instances of the ZZObses triggered from
     * <em>all</em> existing ZZObsTrigger objects.
     */
    public synchronized void chg(Object o) {
	Gob g,pg=null;
	Hashtable next=trigs;
	while((g = (Gob)next.get(o)) !=null && g!=pg) {
	    pg=g;
	    if(!chg(g.obs)) next=g.next;
	}
	// XXX This shouldn't be necessary! (views should use triggers)
	ZZUpdateManager.chg();
    }
    public static boolean chg(ZZObs obs) {
	// XXX This shouldn't be necessary! (views should use triggers)
	ZZUpdateManager.chg();
	synchronized(obsQueue) {
	    if(obsDisabled.containsKey(obs)) return false;
	    obsQueue.put(obs,obs);
	    for(int i=0; i<allTrigs.size(); i++)
		((ZZObsTrigger)allTrigs.elementAt(i)).rmObs(obs); 
	}
	return true;
    }

    /** Called to add an observer.
     */
    public synchronized void addObs(Object o, ZZObs obs) {
	Object go = gobs.get(obs);
	Gob g;
	if(go==null) {
	    gobs.put(obs, g = new Gob());
	    g.obs = obs;
	} else {
	    g = (Gob)go;
	}
	if(g.next.get(o) != null) return; // Already observing.
	Gob t = (Gob)trigs.get(o);
	trigs.put(o, g);
	if(t==null) g.next.put(o,g); // First observer for this object.
	else {
	    g.next.put(o,t);
	    t.prev.put(o,g);
	}
    }

    /** Called to remove observers.
     */
    public synchronized void rmObs(ZZObs obs) {
	Gob g = (Gob)gobs.get(obs);
	if(g == null) return; // Wasn't observing anything
	gobs.remove(obs);
	for(Enumeration e = g.next.keys(); e.hasMoreElements(); ) {
	    Object o = e.nextElement();
	    Gob ng = (Gob)g.next.get(o);
	    Gob pg = (Gob)g.prev.get(o);
	    if(pg==null) {
		if(ng==g) trigs.remove(o);
		else {
		    trigs.put(o,ng);
		    ng.prev.remove(o);
		}
	    } else {
		if(ng==g) pg.next.put(o,pg);
		else {
		    pg.next.put(o,ng);
		    ng.prev.put(o,pg);
		}
	    }
	}
    }

    /** Does the actual triggering.
     * XXX We must continue triggering until no new triggers are created,
     * which is bad if there are lots of (or infinite amount, in case of a
     * bug) triggers. This is because we must run triggers in the AWT event
     * processing thread (+), and unless we use JDK 1.2 "EventQueue.invoke*()"
     * methods or resort to some horrid dummy-event kludge, we can't expect
     * any triggers we leave behind to be triggered until the next AWT event,
     * which is unacceptable. ("- See, it doesn't display the new info if I
     * don't move the mouse around! - Are you *sure* it isn't a M$ product?")
     * This isn't a real problem at the moment, though.
     *
     * (+) The event thread must always be runnable, or we risk a deadlock,
     *     and most of ZZ structure -using code isn't thread-safe => only the
     *     event thread may call general ZZ structure -using code.
     */
    public static void runObsQueue() {
	p("runObsQueue enter");
	synchronized(obsQueue) {
	    while(obsQueue.size()>0) {
		p("STARTTRIGSEQ "+(obsQueue.size()));
		/* Trigger the queued observers in some order */
		Hashtable q=obsQueue;
		obsQueue=new Hashtable();
		for(Enumeration e=q.keys();e.hasMoreElements();)
		    try {
			((ZZObs)e.nextElement()).chg();
		    } catch(Exception ex) {
			ZZLogger.exc(ex);
			System.out.println("EXCEPTION WHILE TRIGGERING!");
		    }
		p("ENDTRIGSEQ "+(obsQueue.size()));
	    }
	}
	p("runObsQueue leave");
    }

    /** Enable/disable an observer.
     * If an observer is disabled, chg(o) won't trigger it. chg(o) calls
     * made before calling this method will be unaffected - that is, if
     * obs is in obsQueue, it will be triggered whether it is enabled or not.
     */
    public static void setEnabled(ZZObs obs,boolean t) {
	if(t) obsDisabled.remove(obs);
	else obsDisabled.put(obs,obs);
    }
}
