/*   
Main.java
 *    
 *    Copyright (c) 1999, 2000 Ted Nelson and Tuomas Lukka
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
 * Written by Tuomas Lukka, version control by Tuukka Hastrup,
 * space locking by Antti-Juhani Kaijanaho.
 */

package org.gzigzag;

import org.gzigzag.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

/** The class to run to start the ZZ client.
 * This class parses the command line, prepares the space and starts the
 * client. If needed, a new client space with default settings is created.
 *
 * The one obligatory argument to give this program is the name of the file
 * to use for the cellscroll of the main space.
 * Additionally, the following options can be given.
 * <dl>
 *   <dt> <b>-new</b>
 *   		<dd> 	Recreate the default client space starting from
 *		     	the main cell. This is important if the internal
 *		     	client structures used by GZigZag have changed since
 *		     	the space was created, or if this is a new space.
 *			If the cellscroll file given does not exist,
 *			this is the default.
 *			If the file already exists, the old system list
 *			and the list on d.2 down from the home cell
 *			are placed on the d.2 list, at a cell named
 * 			OldSysList (see ZZDefaultSpace).
 *   <dt> <b>-convert</b>
 *		<dd>	Convert an old, incompatible, space to compatible form.
 *			When conversion is needed, it will be told by the 
 *			program. Taking backup before conversion is 
 *			recommended. After conversion you might need to use
 *			<b>new</b> and <b>dcold</b> to catch up with 
 *			configuration changes.
 *   <dt> <b>-import</b> XMLfile
 *		<dd>	When creating a new space, you can import an XML
 *			file describing the space.
 *   <dt> <b>-export</b> XMLfile
 *		<dd>	Export the whole space to the specified file
 *   <dt> <b>-recrmdim</b>
                <dd>    Recreate master dimension list from data on disk.      
 *   <dt> <b>-dbg</b> classname
 *		 <dd>   Turn on debugging in the given class. For developers
 *			only.
 *   <dt> <b>-pda</b>
 *               <dd>   Run in PDA-mode.
 *   <dt> <b>-span</b>
 *		<dd>    Save information about cells whose spans overlap.
 *			This may be slow at the moment, but is necessary
 *			to show e.g. the overlaps of two nile text streams.
 *   <dt> <b>-ztpserv PORT</b>
 *              <dd>    Invoke the ZTP server code instead of the ZZ client code.
 *                      Listen on the TCP port PORT for incoming connections.
 *   <dt> <b>-dump</b>
 *              <dd>    Dump the space to the file CELLSCROLLFILE.dmp (where
 *                      CELLSCROLLFILE is the name of the space file).
 *
 *   <dt> <b>-slurp FILENAME</b>
 *              <dd>    Slurp the space from the file called FILENAME.
 *   <dt> <b>-versioneddims</b>
 *              <dd>    Show connections from old versions along regular dimensions
 *                      in addition to the special version: dimensions.
 * 
 * </dl>
 * <p>
 * Some Filenames that start with ':' are special. For example, 
 * ':DS' starts with the ZZDimSpace instead of normal persistent space.
 * You should probably only use these if you are hacking the core.
 * <p>
 * Example:
 * <pre>
 * 	java foo.Main -new /tmp/foo
 * </pre>
 * opens the cellscroll at /tmp/foo and creates the new client structure.
 * XXX Make a good system for specifying the type.
 * <p>
 * Currently, if you give extra arguments after the file name, the system
 * creates a ZZSlicedDimSpace instead.
 * BE VERY CAREFUL WITH THIS! Once you put some slices there with a given
 * root space, never leave them out or confusion will result.
 * This feature should NOT be used if you're not prepared to hack the
 * storage/*Slice* code.
 */

public class Main {
public static final String rcsid = "$Id: Main.java,v 1.70 2001/03/29 13:06:07 ajk Exp $";
    public static final boolean dbg = true;
    public static int zzclientversion = 2;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }
    /** For storing the classes whose debug prints are turned on.
     * Otherwise they might get garbage collected and debugging not
     * stay turned on.
     */
    static Vector dbgclasses = new Vector();

    /** Make this class print debug info.
     * Any callers from outside Main must provide their own Vector
     * to put the class objects in in order to avoid garbage collection.
     */
    public static void debugClass(String name, Vector into) {
	try {
	    p("Turning on debugging for class "+name);
	    Class clazz = Class.forName("org.gzigzag."+name);
	    Field f = clazz.getField("dbg");
	    f.setBoolean(null, true);
	    into.addElement(clazz);
	    into.addElement(f);
	} catch(Exception e) {
	    e.printStackTrace();
	    pa(""+e);
	    throw new Error("Error while turning on debug info");
	}
    }


    public static void main(String argv[]) {

	pa("Starting gZigZag client, build: "+BuildInfo.info);
	pa("Clientspace version used by program is "+zzclientversion+".");
	try {
	    Class primact = Class.forName("org.gzigzag.ZZPrimitiveActions");
	    // *** Parse command line

	    if(argv.length < 1) {
		System.err.println("No filename given");
                SafeExit.exit(1);
	    }
            CommandLineOptionsParser clop = new CommandLineOptionsParser();
            String[] noargs = new String[0];
            clop.put("pda", noargs, "run in PDA mode");
            clop.put("new", noargs, 
                           "Recreate the default client space "
                           + "starting from the main cell. This "
                           + "is important if the internal client "
                           + "structures used by GZigZag have changed "
                           + "since the space was created, or if this "
                           + "is a new space. If the cellscroll file "
                           + "given does not exist, this is the default. "
                           + "If the file already exists, the old system "
                           + "list and the list on d.2 down from the home "
                           + "cell are placed on the d.2 list, at a cell "
                           + "named OldSysList (see ZZDefaultSpace).");
            clop.put("import", new String[] { "XMLFILE" }, 
                           "When creating a new space, you can import an XML "
                           + "file describing the space.");
            clop.put("export", new String[] { "XMLFILE" },
                           "Export the whole space to the specified file");
            clop.put("convert", noargs,
                           "Convert an old, incompatible, space to compatible "
                           + "form. When conversion is needed, it will be "
                           + "told by the program. Taking backup before "
                           + "conversion is recommended. After conversion you "
                           + "might need to use -new and -dcold to catch up "
                           + "with configuration changes.");
            clop.put("xmlencoding", new String[] { "ENCODING" },
                           "Specify the character encoding used by the "
                           + "XML export function.");
            clop.put("ztpserv", new String[] { "PORT" },
                           "Invoke the ZTP server code instead of the ZZ "
                           + "client code. Listen on the TCP port PORT for "
                           + "incoming connections.");
            clop.put("recrmdim", noargs,
                           "Recreate master dimension list from data on disk.");
            clop.put("foolconvert", noargs, "???");
            clop.put("textlog", noargs, "???");
            clop.put("verboselog", noargs, "???");
            clop.put("span", noargs, "Save information about cells whose "
                           + "spans overlap. This may be slow at the moment, "
                           + "but is necessary to show e.g. the overlaps of "
                           + "two nile text streams.");
            clop.put("dump", noargs, "Dump the space to the file "
                           + "CELLSCROLLFILE.dmp (where CELLSCROLLFILE is the "
                           + "name of the space file).");
            clop.put("slurp", new String[] { "FILENAME" },
                           "Slurp the space from the file called FILENAME.");
            clop.put("versioneddims", noargs,
                           "Show connections from old versions along regular "
                           + "dimensions in addition to the special version: "
                           + "dimensions.");
            clop.put(new CommandLineOptionsParser.Option("dbg", new String[] { "CLASSNAME" },
                                                         "Turn on debugging "
                                                         + "in the given "
                                                         + "class. For "
                                                         + "developers	only.") {
                    public void action(CommandLineOptionsParser clops,
                                       String[] args) {
                        debugClass(args[0], dbgclasses);
                    }
                    
                });
            clop.putHelp("help", "shows this help screen", true,
                         "Usage:\nGZigZag takes one mandatory parameter: the name "
                         + "of the directory where the space is (to be) stored. "
                         + "Additionally, one or more of the following options "
                         + "may be specified before the directory name.",
                         "");
            int cur = clop.parse(argv);

            // Since local classes cannot modify these, we have to do
            // this ugly thing...
	    boolean newspace=clop.seen("new"); // New client space?
	    boolean textlog=clop.seen("textlog");
            boolean verboselog=clop.seen("verboselog");
	    boolean pda=clop.seen("pda");
	    boolean span=clop.seen("span"); // Use a SpanSet to find overlapping spans?
            boolean ztpserv=clop.seen("ztpserv");
            boolean dump=clop.seen("dump");
            String slurpf=clop.getArg("slurp", 0);
            int ztpport=Integer.parseInt(clop.getArg("ztpserv", 0, "0"));

            boolean recreateMasterDimList=clop.seen("recrmdim");               
	    boolean convert=clop.seen("convert"); // Convert client space version?
	    boolean foolconvert=clop.seen("foolconvert");
	    String importFile=clop.getArg("import", 0, "");  // If nonempty, it'll be imported
	    String exportFile=clop.getArg("export", 0, "");  // If nonempty, we'll export
            String encoding = clop.getArg("xmlencoding", 0, "UTF-8"); // for exporting                         
            clop = null;  // we no longer need it, throw it away

	    if ( argv[cur].startsWith("-") ){
		// we don't want filenames to look like args
		System.err.println("Unknown arg "+argv[cur]);
		cur++;
		//SafeExit.exit(1);
	    }

	    if(argv.length < cur+1) {
		System.err.println("No filename given");
		SafeExit.exit(1);
	    }

	    if(textlog) {
		ZZPrimitiveActions.textlog = 
		    new PrintWriter(new FileOutputStream(
			argv[cur] + ".txt", true));
	    }

            if(verboselog) {
                ZZLogger.logfile =
                    new PrintWriter(new FileOutputStream(argv[cur] + ".log", true));
                                                         
            }

	    // *** Prepare the space: create ZZSpace objects

            System.runFinalizersOnExit(true); // needed for unlocking a space

	    String[] slices = null;

	    ZZSpace space;

	    if(argv[cur].equals(":DS")) {
	        space = new ZZDimSpace();
	        newspace = true;
	    } else { // Space stored in file
		File f = new File(argv[cur]);
		if(!f.exists()) newspace = true;
		if(argv.length > cur+1) {
		    System.err.println("SLICE TEST MODE!!!!!");
		    slices = new String[argv.length-(cur+1)];
		    System.arraycopy(argv, cur+1, slices, 0,
				argv.length-(cur+1));
		}
		space = new ZZCacheDimSpace(new DirStreamSet(f));
		if(span) ((ZZDimSpace)space).spanset = new SpanTree();
	    }	

	    // And now we switch spaces if there are slices.
	    if(slices != null) {
		throw new ZZError("slices are broken");
		//ZZDimSpace[] ss = new ZZDimSpace[slices.length+1];
		//ss[0] = (ZZDimSpace)space;
		//for(int i=0; i<slices.length; i++)
		//    ss[i+1] = new ZZPersistentDimSpace(
		//	new DirSet(new File(slices[i]), "rw"));
		// space = new ZZSlicedDimSpace(ss);
		//throw new Error("No sliced stuff right now!");
	    }

            if (slurpf != null) {
                if (!newspace)
                    throw new ZZError("cannot slurp into an existing space");

                Reader r = new FileReader(slurpf);
                SpaceDump sd = new SpaceDump(r);

                // First, instantiate the scrolls
                for (Enumeration e = sd.stringscrolls.keys(); e.hasMoreElements();) {
                    String name = (String)e.nextElement();
                    StringScroll ss = space.getStringScroll(name);
                    String content = (String)sd.stringscrolls.get(name);

                    if (ss == null)
                        throw new ZZError("cannot create the string scroll " + name);

                    if (ss.curEnd() != 0)
                        throw new ZZError("the string scroll `" + name
                                          + "' is not initially empty!");

                    ss.append(content);
                }

                // Then put in cell content
                for (Enumeration e = sd.content.keys(); e.hasMoreElements();) {
                    String cid = (String)e.nextElement();
                    ZZCell c = space.getCellByID(cid);
                    String content = (String)sd.content.get(cid);

                    if (sd.cellswithspans.containsKey(cid)) {
                        c.setSpan(Span.parse(content));
                    } else {
                        c.setText(content);
                    }
                }

                // Then make the connections
                for (Enumeration e = sd.dims.keys(); e.hasMoreElements();) {
                    String dname = (String)e.nextElement();
                    
                    // we ignore d.cellcreation
                    if (dname.equals("d.cellcreation")) continue;

                    Hashtable dim = (Hashtable)sd.dims.get(dname);
                    
                    for (Enumeration f = dim.keys(); f.hasMoreElements();) {
                        String cid = (String)f.nextElement();
                        String did = (String)dim.get(cid);
                        ZZCell c = space.getCellByID(cid);
                        ZZCell d = space.getCellByID(did);
                        c.connect(dname, d);
                    }
                }

                space.commit();
                ZZLogger.log("Done.");
                SafeExit.exit(0);
            }

            if (dump) {
                    PrintWriter pw = new
                        PrintWriter(new FileOutputStream(argv[cur]
                                                         + ".dmp", false));
                    SpaceDump.dump(pw, space);
                    pw.flush();
                    pw.close();
                    ZZLogger.log("Done.");
                    SafeExit.exit(0);
            }

            if (ztpserv && ztpport > 0) {
                ZZLogger.tryInitSyslog();
                try {
                    Class ztpcl = Class.forName("org.gzigzag.module.ZTPserver");
                    Constructor ztpcons = ztpcl.getConstructor(new Class[] {
                        Class.forName("org.gzigzag.ZZSpace"),
                        Class.forName("java.lang.Integer")});
                    Object ztp = ztpcons.newInstance(new Object[] { space, new Integer(ztpport) });
                    p("main exiting - waiting for connections");
                } catch (ClassNotFoundException e) {
                    ZZLogger.exc(e);
                }
                return;
            }

	    if (recreateMasterDimList) {
		space.recreateMasterDimList();
		space.commit();
	    }

	    if(newspace && !importFile.equals("")) { 
                // XXX should be "if a space with only the home cell"
                // NOTE: Introspection is used so that there will not
                // be a compile-time dependency on the XML module.
	        Object xml = (Class.forName("org.gzigzag.module.XML").newInstance());
		Method get = xml.getClass().getMethod("load", new Class[] {Class.forName("java.io.File"), Class.forName("org.gzigzag.ZZCell")});
		get.invoke(xml, new Object[] {new File(importFile), space.getHomeCell()});
                space.commit();
	        newspace = false; // It's old then
	    }
	    if(!exportFile.equals("")) {
                // NOTE: Introspection is used so that there will not
                // be a compile-time dependency on the XML module.
                Object xml = (Class.forName("org.gzigzag.module.XML").newInstance());
                Method get = xml.getClass().getMethod("export", new Class[] {Class.forName("java.io.File"), Class.forName("org.gzigzag.ZZSpace"), Class.forName("java.lang.String")});
                p("Invoke: "+get+" "+xml);
                get.invoke(xml, new Object[] {new File(exportFile), space, encoding});  
		p("File exported - exiting.");
		SafeExit.exit(0);
	    }
														
	    // *** Prepare client space and check version

            // Ted asked for this kind of an announcement
            if(newspace) {
                System.err.println("Creating or recreating the system part of the space");
            }
	    ZZCell home = space.getHomeCell();
	    if(newspace) {
		ZZDefaultSpace.create(space.getHomeCell());
	    }
	    if (newspace) { // Might be re-init
		home.getOrNewCell("d.gzz-space-version", 1)
		    .setText(""+zzclientversion);
	    } else {
		ZZCell vercell = home.s("d.gzz-space-version",1);
		if(vercell==null) {
		    pa("Version cell not found: setting version to zero.");
		    vercell = home.N("d.gzz-space-version", 1);
		    vercell.setText("0");
		}
		pa("Clientspace versionstring: '"+vercell.getText()+"'");
		int fileversion; 
		try {
			fileversion = Integer.parseInt(vercell.getText());
		} catch (NumberFormatException e) {
			fileversion = 0;
		}

		pa("Clientspace version in prepared space is "+fileversion+".");
		if(fileversion > zzclientversion)
		    quit("Can't use files from clients that new. " +
			 "Please upgrade client.");
		if(fileversion < zzclientversion && !convert)
		    quit("Files from older client. Please use " +
			 "option -convert after backing up the file.");
		if(convert && !foolconvert) {
		    if(fileversion == zzclientversion)
			quit("File is already of same version.");
		    convert(fileversion, zzclientversion, space);
		    vercell.setText(""+zzclientversion);
                    vercell.getSpace().commit();
		    SafeExit.exit(0);
		}
	    }

    /*		// Print all cells
	    ZZCell i;
	    p("Cells:");
	    for(Enumeration e=space.cells();e.hasMoreElements();)
	    {
		    i=(ZZCell)e.nextElement();
		    p(i.getID()+": "+i.getText());
	    }*/

	    // Print all dimensions
	    String[] dims = space.dims();
	    p("Dimensions: ("+dims.length+")");
	    for(int s=0;s<dims.length;s++) p(dims[s]);

	    // *** Start client

	    // ZZCell clientcell = space.getHomeCell().findText(
	    //	    "d.2",1,"ClientCell");
	    // if(clientcell==null)
	    //  throw new ZZError("No client cell found on d.2!!");
	    // ZZCell dimlist = ZZDefaultSpace.findDefaultDimlist(home);

	    // ZZSimpleClient2 cli = new ZZSimpleClient2(clientcell,
	    //	    dimlist, dimlist);

	    // *** Initialize ZZWindows
	    if(pda) {
		pa("PDA mode: Creating root window.");
		Container c=new Window((Frame)null);
		c.setLocation(0,0);
		c.setSize(320,240);
		c.setVisible(true);
		ZZWindows.init(space,c);
	    } else ZZWindows.init(space,null);

	    // Make sure no undo to before start.
	    space.commit();

	} catch(Throwable e) {
	    ZZLogger.exc(e, "In main");
	}
	ZZObsTrigger.runObsQueue();
	p("Main exiting - on to the events..");
    }

    /** Convert a space from older client to be compatible with the current 
     *  form. Classes org.gzigzag.transform.Version[number] will be used
     *  one after another until the current version is reached.
     *  @param fileversion		Version of loaded space
     *  @param zzclientversion	Version of current client
     *  @param s			The space to be converted
     */
    static void convert(int fileversion, int zzclientversion, ZZSpace s) {
	try {
	    org.gzigzag.transform.VersionChanger vchg;
	    System.out.println("Converting space to current version:");
	    while(fileversion < zzclientversion) {
		vchg = (org.gzigzag.transform.VersionChanger)
			(Class.forName(
			"org.gzigzag.transform.Version"+fileversion)
		       ).newInstance();
		fileversion = vchg.changeVersion(s);
	    }
	    System.out.println("Conversion done, new file version "+zzclientversion+".");
	} catch (Exception e) {
		e.printStackTrace();
		System.out.println(e);
	}
    }

    static void quit(String msg) {
	System.out.println(msg);
	SafeExit.exit(5);
    }

}
