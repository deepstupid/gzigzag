/*   
AwtUtil.java
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
 * Written by Kimmo Wideroos
 */

package org.gzigzag.module;
import org.gzigzag.*;
import java.util.*;
import java.awt.*;

/** Some general stuff
 */

public class AwtUtil {
    public static boolean dbg = false;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }

    /** return active metrics
     *  @vc view cell 
     */
    /*
    public static AwtMetrics getActiveMetrics(ZZCell awtlocals) {
	ZZCell metr = AwtCursors.get(AwtCursors.c_metrics, awtlocals);
        if(metr==null) {
            metr = findLocals("Metrics", awtlocals, false);
            if(metr == null) 
                throw new ZZError("Awtool2: Couldn't initialize metrics!!");
            metr = metr.s("d.1");
        }

        pa("[AwtUtil]: Metrics: "+metr);
        AwtMetrics M = (AwtMetrics)readModuleZOb(metr);
        M.setCell(metr);
        return M;
    }         
    */
    /* return AwtArtefact or AwtLinkRelation, if possible.
     * 'layer' is needed, because several artefact clones
     * may shere the same text span, and we need to decide 
     * the artefact in concern!
     */
    public static Object getAwtObject(ZZScene xi, Point pt, ZZCell layer) {
	if(pt != null && xi != null) {
	    Object ob = xi.getObjectAt(pt.x, pt.y);
	    if(ob == null) return null;
	    ZZCell ac = disinterCell(ob, layer);
	    if(ac == null) return null;
	    return getAwtObject(ac);
	}
	return null;
    }

    public static Object getAwtObject(ZZCell ac) {
	Object ao = null;
	ZZCell lroot;
	if(AwtArtefact.valid(ac)) {
	    ao = new AwtArtefact(ac);
	} else
	if(AwtLinkRelation.isLinkRelation(ac)) {
	    lroot = AwtLinkRelation.getLinkRoot(ac);
	    ao = new AwtLinkRelation(lroot);
	} else
	if(AwtLayer.valid(ac))
	    ao = new AwtLayer(ac);
	pa("return awtobj: "+ao);
	return ao;
    }

    /** return artefact cell (or link relation cell), if possible 
     */
    public static ZZCell disinterCell(Object ob, ZZCell layer) {
        ZZCell c = null;
        if(ob instanceof ZZCursor) c = ((ZZCursor)ob).get(); 
        else if(ob instanceof ZZCell) c = (ZZCell)ob;
        else return null;
	// might be a nile span
        ZZCell cl = c.h(AwtDim.d_nile);
	if(!cl.equals(c)) cl=cl.s("d.1", -1);
	// now 'cl' is layer / artefact root cell ...
	// ... move to last artefact along 'd.clone'
	cl = cl.h("d.clone", 1);
	for(; cl != null; cl=cl.s("d.clone", -1)) {
	    if(cl.s(AwtDim.d_member, -1) != null && 
	       cl.s(AwtDim.d_member, -1).h(AwtDim.d_layerset).equals(layer)) 
		return cl; 
	} 
	// 'c' might be link relation, dont know yet...
	pa("disinter returns: "+c);
	return c;
    }

    /* is 'sc' 'artef's span cell
     */
    public static boolean isPartOfspan(ZZCell sc, ZZCell artef) {
	ZZCell sch = sc.h(AwtDim.d_nile);
	return sch.findCell("d.clone", 1, artef); 
    }

    /* return view cell
     */
    public static ZZCell launchView(ZZCell home, ZZCell target) {

        //ZZCell awtlocals = target;
                
        // View
        ZZCell rastname, rl, rc;

	rc = findLocals("View", home, true);
        if(rc.s("d.1") == null) {
            rl = rc.N("d.1");
            rl.connect("d.2", 1, rl);
            rl.setText("Awt"); 
            rastname = rl.N("d.1");
            rastname.setText("Awt.View2");
        } else {
            rl = rc.s("d.1");
            rastname = rl.s("d.1");
        }

        // Bindings                
        ZZCell bindhome = findLocals("Bind", home, true);
        if(bindhome.s("d.1") == null) {
            bindhome = bindhome.N("d.1", 1);
            
            AwtNile.awt_nile_actions(bindhome);
            embed_awt_actions(bindhome);
            
        } else bindhome = bindhome.s("d.1");
        
	// Bgpatterns (images)
        ZZCell imageroot = findLocals("Images", home, true);
        if(imageroot.s("d.1") == null) {
            ZZCell images = imageroot.N("d.1"); 
	    images.setText("file:///home/kimmo/awtbgimages/");
            images = images.N("d.1"); 
            //images.setText("default.gif"); images.N("d.1").setText("2");
	    //images = images.N("d.2");
            images.setText("cloud.gif"); images.N("d.1").setText("2");
	    images = images.N("d.2");
            images.setText("default2.gif"); images.N("d.1").setText("3");
	    images = images.N("d.2");
            images.setText("stone.gif"); images.N("d.1").setText("3");
	    images = images.N("d.2");
            //images.setText("moonstone.jpg"); images.N("d.1").setText("2");   
	    //images = images.N("d.2");
            images.setText("colorful1.jpg"); images.N("d.1").setText("1");   
	    images = images.N("d.2");
            //images.setText("colorful2.jpg"); images.N("d.1").setText("4");   
	    //images = images.N("d.2");
            images.setText("#DEFAULT"); images.N("d.1").setText("4");   
        }

        // View metrics
        AwtMetrics M;
        ZZCell metr = findLocals("Metrics", home, true);
        if(metr.s("d.1") == null) {
            Dimension RV = new Dimension(400, 400);
            Dimension VV = new Dimension(1000, 1000);
            ZZCell mnew, metrList;
            
            mnew = metrList = metr.N("d.1");
            
            M = new AwtMetricsNormal(mnew, RV, VV, 0.0,0.0);
            M.cellRepr();
            
            mnew = mnew.N("d.2");
            M = new AwtMetricsFC(mnew, RV, VV, 0.0, 0.0);
            M.cellRepr();
            
            mnew = mnew.N("d.2");
            M = new AwtMetricsFC2(mnew, RV, VV, 0.0, 0.0);
            M.cellRepr();
            
            // make a loop
            mnew.connect("d.2", 1, metrList);
            //AwtCursors.set(AwtCursors.c_metrics, metrList, awtlocals);                    
        } else {
	    M = (AwtMetrics)readModuleZOb(metr.s("d.1"));
	    M.setCell(metr.s("d.1"));
	}

	/*else {
            ZZCell currentMetrics =
                AwtCursors.get(AwtCursors.c_metrics, awtlocals);
            if(currentMetrics == null) {
                currentMetrics = metr.s("d.1");
                AwtCursors.set(AwtCursors.c_metrics, currentMetrics, awtlocals);
            }
            M = (AwtMetrics)readModuleZOb(currentMetrics);
            M.setCell(currentMetrics);
        }
	*/
        
        ZZCell v = ZZDefaultSpace.newToplevelView(rc.getSpace(), 
                                                  "awtool", 0, 0, M.RealView[0], M.RealView[1], 
                                                  rl, null, 
                                                  null, null,
                                                  null,
                                                  new Color(0xE5FFE5));
        
        // Then, set bindings
        ZZCell bcurs = v.getOrNewCell("d.bind", 1);
        ZZCursorReal.set(bcurs, bindhome);
        ZZCursorReal.setColor(bcurs, Color.black);

	if(target.h(AwtDim.d_artefact).equals(target)) {
	    ZZCell layer;
	    layer = home.h(AwtDim.d_artefact, 1);
	    if(home.equals(layer)) layer = home.N(AwtDim.d_artefact); 
	    AwtNile.initSpanCursors(layer);
	    // init style!
	    layer.connect(AwtDim.d_style, 1, 
			  imageroot.s("d.1", 2).h("d.2", 1).zzclone());

	    ZZCursorReal.set(v, layer);
	    
	} else {
	    ZZCursorReal.set(v, target);	    
	}
	return v;
	
    }


    /** Construct and return a ZOb of the given type.
     */
    public static ZOb newModuleZOb(String id) {
    // XXX Figure out if we can cache something.
    // XXX Should ZOb be able to reset? Easier or less easy than
    // recreate? At least ZObs should cache...
        try {
            Class z = Class.forName("org.gzigzag.module."+id);
            return (ZOb)Class.forName("org.gzigzag.module."+id).newInstance();
        } catch(Exception e) {
            ZZLogger.exc(e);
            return null;
        }
    }
    
    /** Construct a ZOb corresponding to a cell and read the params.
     * DOES NOT Go to the rootclone, takes the next cell on d.1 as the ZOb's name,
     * and takes the NEXT cell on d.1 as the head of the list from which to
     * read the structparams.
     */
    /*  Differs from readZOb by considering clones themselves as zobs and
     *  also uses 'newModuleZOb' in order to read ZOb-class.
     */
    static public ZOb readModuleZOb(ZZCell start) {
	//ZZCell c = start.h("d.clone", -1).s("d.1", 1);
        
        if(start.s("d.1") == null) start = start.h("d.clone", -1);
	ZZCell c = start.s("d.1", 1);
	if(c == null)
	    throw new ZZError("Error reading ZOb from cell "+start.getText());
	ZOb z = newModuleZOb(c.getText());
	z.readParams(c.s("d.1", 1));
	return z;
    }


    // This is really hacky part...
    static ZZCell curAct;
    static void a(String key, String act) { a(key, act, null); }
    static void a(String key, String act, ZZCell next) {
	curAct = curAct.N("d.2", 1);
	curAct.setText(key);
	ZZCell n = curAct.N("d.1", 1);
	n.setText("Awt." + act);
	if(next != null)
	    next.insert("d.3", 1, n);
    }

    static void embed_awt_actions(ZZCell start) {
        // awtool acts
        ZZCell awtActs = start.N("d.1", -1); 
        awtActs.setText("Awt core actions");

        curAct = awtActs;
        a("MouseStartDrag", "StartDrag");
        a("MouseDragged1", "Dragged");
        a("MouseReleased1", "StopDrag");
        a("Ctrl-MouseStartDrag", "StartResize");
        a("Ctrl-MouseDragged1", "Resizing");
        a("Ctrl-MouseReleased1", "StopResize");
        a("Shift-MouseStartDrag", "StartCreatingArtefact");
        a("Shift-MouseDragged1", "CreatingArtefact");
        a("Shift-MouseReleased1", "StopCreatingArtefact");
        a("Shift-MouseClicked3", "ChangeOrigo");
        a("MouseClicked3", "FocusOrigoAndSelect");
        a("Shift-MouseClicked1", "SetInput2");
        a("MouseClicked1", "SetInput1");
        a("l", "NewLink");
        a("r", "RemoveLink");
        a("Alt-R", "RemoveArtefact");
        //a("Alt-J", "JoinArtefacts");
        a("Up", "ChangeLayer + 1");
        a("Down", "ChangeLayer - 1");
        a("Shift-Up", "ChangeLayer + END");
        a("Shift-Down", "ChangeLayer - END");
        a("Alt-C", "CloneArtefact");
        a("Alt-T", "TranscludeArtefact");
        a("Alt-Enter", "LaunchView");
        a("m", "ChangeMetrics");
        a("Alt-Up", "ChangeArtefactStyle +");
        a("Alt-Down", "ChangeArtefactStyle -");
        a("g", "GridOnOff");
        a("r", "ResetOrigo");
        a("Alt-H", "MainLayer");
        a("Alt-Q", "Quit"); // saves the space
        a("Alt-N", "NileOnOff", start);

        // hack!! add 'NileOnOff' action into all Nile modes...
	AwtNile.Tokenizer tokenizer = new AwtNile.Tokenizer();
	Nile2Unit u = null;
        for(ZZCell mode=start; mode != null; mode = mode.s("d.1")) {
            curAct = mode;
	    for(ZZCell act=mode.s("d.2"); act != null; act = act.s("d.2")) {
		tokenizer.set_id(act.s("d.1").t());
		u = tokenizer.unit();
		pa("mode="+mode+" act="+act+" unit="+u);
		if(u != null) break;
	    }
	    if(u != null) {
		tokenizer.set_id("SplitNote 0 WORD");
		tokenizer.set_unit(u);
		a("Alt-S", tokenizer.id());
		tokenizer.set_id("JoinArtefacts 0 WORD");
		a("Alt-J", tokenizer.id());
	    }
            a("Alt-N", "NileOnOff", awtActs);
        }
    }


    /** find local variable
     *  @create   create new slot for variable, if it does not exist yet
     */
    public static ZZCell findLocals(String id, ZZCell c, boolean
                                     create) {
	ZZCell locals = c.getSpace().getHomeCell();
        ZZCell retloc = locals.findText(AwtDim.d_locals, 1, id);
        if(retloc == null && create) {
            retloc = locals.h(AwtDim.d_locals, 1).N(AwtDim.d_locals);
            retloc.setText(id);
        }
        return retloc;
    }

    public static void closeView(ZZCell awtlocals) {
        ZZCell rc = awtlocals.findText(AwtDim.d_locals, 1, "View");        
        rc.excise(AwtDim.d_locals);
    }

    static public void renderGrid(FlobSet into, AwtMetrics M, double x1, 
				  double y1, double x2, double y2, 
				  Color color, int density) 
        {
        double[] coord = new double[2], 
	         cstart = new double[2], cinc = new double[2],
                 d = new double[2], dh = new double[2];
        Point p1, p2;
        int i, j, a, b;
        cstart[0] = (x1 < x2) ? x1 : x2;
        cstart[1] = (y1 < y2) ? y1 : x2;
        cinc[0] = Math.abs((x2-x1) / ((double)density + 1.0));
        cinc[1] = Math.abs((y2-y1) / ((double)density + 1.0));
        d[0] = cinc[0] / 5.0;
        d[1] = cinc[1] / 5.0;
        dh[0] = d[0]/2.0;
        dh[1] = d[1]/2.0;
 
        LineDecor.Builder ldb = new LineDecor.Builder(into, color);
 
        for(a=0; a<2; a++) {
            b = (a==0) ? 1 : 0;
            coord[a] = cstart[a];
            for(i=0; i<=density+1; i++) {
                coord[b] = cstart[b]+dh[b]/2.0;
                ldb.startl((density+1)*20, 2000);
                for(j=1; j<=(density+1)*5; j++) {
                    p1 = M.mapToRealView(coord);
                    coord[b] += dh[b];
                    p2 = M.mapToRealView(coord);
                    coord[b] += dh[b];
                    ldb.l(p1.x, p1.y, p2.x, p2.y);
                }
                ldb.endl();
                coord[a] += cinc[a];
            }
        }
    }                       

}








