/*
Client.java
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
 * Written by Rauli Ruohonen, Antti-Juhani Kaijanaho and Tuomas Lukka
 */
package org.gzigzag.client;
import org.gzigzag.*;
import org.gzigzag.impl.*;
import org.gzigzag.mediaserver.*;
import org.gzigzag.mediaserver.storage.*;
import org.gzigzag.mediaserver.ids.*;
import java.util.*;
import java.io.*;
import org.gzigzag.vob.VobScene;
import java.awt.Image;
import java.awt.event.MouseEvent;
import org.python.util.PythonInterpreter;

/** GZigZag client main class. 
 *  <p>At the moment, <code>main</code> is here too. It is supplied a 
 *  Mediaserver data pool directory containing client space. 
 *  <code>-dbg class</code> (class name without org.gzigzag) options can be 
 *  given to turn on class-specific debug messages
 */ 
public class Client {
public static final String rcsid = "$Id: Client.java,v 1.15 2002/03/24 19:29:50 bfallenstein Exp $";
    public static boolean dbg = false;
    private static void p(String s) { if(dbg) pa(s); }
    private static void pa(String s) { System.err.println(s); }
    private static void out(String s) { System.out.println(s); }

    /** An instance to get out of static context, dynamic context used for
     *  some behaviour overriding in subclasses. Subclasses should create this
     *  instance in <code>main</code>.
     *  XXX Static and dynamic parts should be thought of and corrected into 
     * something clean, probably <code>main</code> not in Client.
     */
    public static Client client = new Client();
    public static Cell clientCell;

    static final String block = "0000000008000000E7A4980287000482615DF43AFC046CBF46A812C3B855111A4AA8A24D0CA18F";
    /** Dimension and parameter cell identities. */
    public static Cell 
	d1 = Id.space.getCell("home-id:"+block+"-1"), 
	d2 = Id.space.getCell("home-id:"+block+"-2"),
	c_screen = Id.space.getCell("home-id:"+block+"-3"), 
	c_keybindings = Id.space.getCell("home-id:"+block+"-4"), 
	c_window = Id.space.getCell("home-id:"+block+"-5"), 
	c_view = Id.space.getCell("home-id:"+block+"-6"),
	c_bounds = Id.space.getCell("home-id:"+block+"-7"), 
	c_vanishing = Id.space.getCell("home-id:"+block+"-8"), 
	c_dims = Id.space.getCell("home-id:"+block+"-9"),
	c_vstreamv = Id.space.getCell("home-id:"+"0000000008000000E83B0E16550004BB5EFF914594B0B7B730EEE7F962CAD106E265DF4545F213-2");

    protected static GZZ1Space space;
    protected static Cell primspace;
    protected static HashMap screens = new HashMap();
    protected final Obs obs = new Obs() {
	    public void chg() { updateScreens(); }
	};

    /** Whether to use the global cache image or a separate cache for each
     * screen.
     * Currently default is false because it seems to be more efficient that
     * way.
     */
    static boolean useGlobalCache = false;
    static Image globalCache;

    /** Whether the previous operation was one that does not need 
     * animation, such as inserting a character.
     */
    static boolean doNotAnimate = false;

    /** The mediaserver used for loading and saving the client space.
     */
    protected static Mediaserver ms;

    /** A buffered reader reading from stdin.
     * Used for entering commands into the terminal window.
     */
    private static BufferedReader input =
	new BufferedReader(new InputStreamReader(System.in));

    private static Mediaserver.Id head = null; 
    private static void setHead(Mediaserver.Id head) {
        if (head == null) throw new NullPointerException();
        Client.head = head;
    }
    private static Mediaserver.Id getHead() {
        if (head == null) throw new NullPointerException();
        return head;
    }

    private static Mediaserver.Id getPointer() throws IOException {
        out("BOOYAKASHA!");
        PointerSet ps = ms.getPointerSet("gzigzag1_clientspace");
        Map act = ps.getActiveMap();
        if (act.size() == 1) {
            Map.Entry me = (Map.Entry)act.entrySet().iterator().next();
            setHead((Mediaserver.Id)me.getKey());
	    pa("Only one pointer:\n"+getHead().getString()+"\n");
            return (Mediaserver.Id)me.getValue();
        }
        if (act.size() < 1) throw new Error("There was no active pointers");
        out("There is more than one active mediaserver pointer to the "+
	    "clientspace:\n");
        Mediaserver.Id[] ptrs = 
	    (Mediaserver.Id[]) act.keySet().toArray(new Mediaserver.Id[0]);
        for (int i = 0; i < ptrs.length; i++) {
            Mediaserver.Id d = (Mediaserver.Id)act.get(ptrs[i]);
            out("[" + (i + 1) + "]");
            out(ms.getDatum(d).getHeader());
            out("ID: "+d.getString());
            out("");
        }
        out("Please choose which one to use (0 to bail out): ");
        String line;
        int choice;
        while (true) {
            line = input.readLine();
            try {
                choice = Integer.parseInt(line);
             } catch (NumberFormatException _) {
                 choice = -1;
            }
            if (choice < 0 || choice > ptrs.length) {
                out("Please enter a number between 0 and " + ptrs.length);
                continue;
            }
            break;
        }
        if (choice == 0) System.exit(0);
        setHead(ptrs[choice - 1]);
        return (Mediaserver.Id)act.get(getHead());
    }

    protected static void quit() {
	try {
            out("quitting...");
	    /* Set window cursors to root cell to avoid conflicts between 
	       space versions */
	    Cell screen = Params.getParam(clientCell, c_screen);
	    while(screen != null) {
		Cell window = Params.getParam(screen, c_window);
		Cursor.set(window, space.getHomeCell());
		screen = Params.getNextParam(screen);
	    }

	    Mediaserver.Id newId = space.save(ms);
	    if(newId != null) {
		out("Client space saved with id "+newId+", setting pointer");
		ms.setPointer("gzigzag1_clientspace", newId, getHead());
		out("Please restart to get update into effect");
	    } else {
		out("No changes found: not setting pointers.");
	    }
	    System.exit(0);
	} catch(IOException e) {
	    throw new Error("Exception when quitting: "+e.getMessage());
	}
    }

    /** Performs an action following a key stroke. Can be overriden for
     *  application specific behaviour. XXX Will be replaced by clasm code.
     *  @param key    keystroke description like "+" or "Ctrl-Q"
     *  @param screen window the event arrived at
     */
    public boolean hardcodedBinding(String key, 
				    Screen screen) {
	return HardcodedBindings.hardcodedBinding(key, screen);
    }

    /** Performs an action following a mouse click. Can be overriden for
     *  application specific behaviour. XXX Will be replaced by clasm code.
     *  @param me the mouse event
     *  @param sc according vob scene
     *  @param wc according window cell
     */
    public void hardcodedMouse(MouseEvent me, VobScene sc, Cell wc) {
	HardcodedBindings.hardcodedMouse(me, sc, wc);
    }

    /** Resolves the view corresponding to a cell identity. XXX Will probably 
     * be replaced by some general primitive identity code 
     */
    public View hardcodedView(Cell id) {
	View v = null;
	if(Id.equals(id, c_vanishing)) {
	    // v = new VanishingView();
	    // v = new PlainVanishing();
	    v = new SpanView();
	    //s.setJavaObject(vc,new VanishingView());
	} else if (Id.equals(id, c_vstreamv)) {
	    v = new VStreamView(true, false, true);
	} else {
	    pa("No matching view found for view parameter");
	}
	return v; 
    }

    /** Generates or updates the client windows according to the screen list
     *  in the client space.
     */
    void updateScreens() {
	Cell scr = Params.getParam(clientCell, c_screen, obs);
	HashMap nscreens = (HashMap)screens.clone();
	Screen s;
	while (scr != null) {
	    if (screens.containsKey(scr)) {
		s = (Screen)screens.get(scr);
		s.chg(); // XXX Observers should do this on demand
		nscreens.put(scr, s);
		screens.remove(scr);
	    } else {
		s = new FrameScreen(this, scr);
		s.chg();
		nscreens.put(scr, s);
		pa("Adding window "+s+" to update manage");
		UpdateManager.addWindow(s);
	    }
	    scr = Params.getNextParam(scr, obs);
	}
	for (Iterator i = screens.values().iterator(); i.hasNext();) {
	    s = (Screen)i.next();
	    s.die();
	    UpdateManager.rmWindow(s);
	}
	screens = nscreens;
	if (screens.size() == 0) {
	    pa("No screens left, exiting...");
	    System.exit(1);
	}
    }

    static org.gzigzag.util.Dbg debugger = new org.gzigzag.util.Dbg();
    public static void main(String argv[]) throws IOException {
	int i=0;
	while(i < argv.length) {
	    if(argv[i].equals("-dbg")) {
		i++;
		debugger.debugClass(argv[i], true);
	    } else
		break;
	    i++;
	}
	
	Mediaserver[] servs = Synch.getMediaservers(argv, i);

        if (servs.length < 1) {
            pa("Please supply a Mediaserver data pool directory containing"
               + "client space.");
            System.exit(1);
	} else if(servs.length == 1) {
	    ms = servs[0];
        } else {
	    Mediaserver first = servs[0];

            Mediaserver[] other = new Mediaserver[servs.length - 1];
            for (int j = 0; j<other.length; j++)
		other[j] = servs[j+1];

            ms = new MultiplexingMediaserver(first, null, other);
        }
	Mediaserver.Id spaceId = getPointer();
	pa("Starting with space\n"+spaceId.getString());
        space = new PermanentSpace(ms, spaceId);

	clientCell = space.getHomeCell().s(d1);
	if (clientCell == null) {
	    pa("No client cell in space");
	    System.exit(1);
	}

	// Initialize Jython
	//PythonInterpreter.initialize(System.getProperties(), null,
	//			     new String[] {});

	// Start GUI
	client.start();
    }

    public void start() {
	updateScreens();
    }
}
